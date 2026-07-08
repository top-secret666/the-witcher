#!/usr/bin/env python3
"""
Перевод координат Swing → LibGDX (дизайн 480×360, Swing-Y сверху).

Читает исходники Swing, извлекает доли экрана (sw*0.45f и т.п.) и генерирует
Java-классы раскладки для GDX. Запуск из корня:

    python tools/swing_to_gdx_layout.py
    python tools/swing_to_gdx_layout.py --target 1920 1080 --scale 4

Результат:
  src/main/java/com/witcher/ui/menu/view/MenuLayout.java
  src/main/resources/gdx/layout/menu_layout.json
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    Image = None  # type: ignore

ROOT = Path(__file__).resolve().parents[1]
SWING_MENU = ROOT / "src/main/java/com/witcher/ui/graphics/MainMenuScreen.java"
OUT_JAVA = ROOT / "src/main/java/com/witcher/ui/menu/view/MenuLayout.java"
OUT_JSON = ROOT / "src/main/resources/gdx/layout/menu_layout.json"
BAKED_BUTTONS = ROOT / "src/main/resources/assets/sprites/menu/1x/buttons"
BUTTON_SHEET = ROOT / "src/main/resources/assets/sprites/menu/menu_buttons_sheet.png"

DESIGN_W, DESIGN_H = 480, 360

# Доли, синхронизированы с Swing MainMenuScreen (ручной эталон + парсинг)
MENU_RATIOS = {
    "logoY": 0.035,
    "signW": 0.45,
    "titleLogoW": 0.31,
    "innerLogoWOfSign": 0.70,
    "innerLogoOffsetYOfSign": 0.05,
    "logoMarginBottom": 16,
    "contentMarginBottom": 16,
    "buttonGapOfAvailable": 0.04,
    "buttonW": 0.62,
    "helpYFromBottom": 8,
    "cursorW": 14,
    "cursorHotspotX": 3,
    "cursorHotspotY": 3,
    "textAnchorX": [0.5, 0.5, 0.5],
    "textAnchorY": [0.46, 0.46, 0.46],
    "textFontMin": 12,
    "textFontHeightRatio": 0.28,
}


def analyze_button_ink_center(img: Image.Image) -> dict[str, float]:
    """
    Центр «пергаментной» зоны кнопки (без печати, заклёпки, монет внизу).
    Возвращает доли 0..1 относительно обрезанного спрайта кнопки.
    """
    rgba = img.convert("RGBA")
    w, h = rgba.size
    inner_xs: list[int] = []
    inner_ys: list[int] = []
    for y in range(h):
        for x in range(w):
            r, g, b, a = rgba.getpixel((x, y))
            if a < 20:
                continue
            if r < 30 and g < 30 and b < 30:
                continue
            # без верхних углов (печать/заклёпка) и нижнего декора
            if not (0.16 * h <= y <= 0.76 * h and 0.10 * w <= x <= 0.90 * w):
                continue
            if r > 90 and g > 70 and b > 40 and r > b:
                inner_xs.append(x)
                inner_ys.append(y)
    if not inner_xs:
        return {"inkCx": 0.5, "inkCy": 0.46, "inkW": 1.0, "inkH": 1.0}
    left, right = min(inner_xs), max(inner_xs)
    top, bottom = min(inner_ys), max(inner_ys)
    return {
        "inkCx": (left + right) / 2 / w,
        "inkCy": (top + bottom) / 2 / h,
        "inkW": (right - left + 1) / w,
        "inkH": (bottom - top + 1) / h,
    }


def load_button_frame(row: int, state: int = 0) -> Image.Image | None:
    if Image is None:
        return None
    baked = BAKED_BUTTONS / f"btn_{row}_{state}.png"
    if baked.is_file():
        return Image.open(baked)
    if not BUTTON_SHEET.is_file():
        return None
    sheet = Image.open(BUTTON_SHEET).convert("RGBA")
    cols, rows = 3, 3
    cw, ch = sheet.width // cols, sheet.height // rows
    return sheet.crop((state * cw, row * ch, (state + 1) * cw, (row + 1) * ch))


def analyze_menu_button_text_anchors(button_count: int = 3) -> dict:
    """Считает якоря подписи по baked-спрайтам кнопок."""
    if Image is None:
        print("  [warn] Pillow ne ustanovlen — якоря текста iz MENU_RATIOS")
        return {
            "textAnchorX": MENU_RATIOS["textAnchorX"],
            "textAnchorY": MENU_RATIOS["textAnchorY"],
            "perButton": [],
        }
    per_button: list[dict] = []
    ax: list[float] = []
    ay: list[float] = []
    for row in range(button_count):
        img = load_button_frame(row, 0)
        if img is None:
            ax.append(0.5)
            ay.append(0.46)
            continue
        ink = analyze_button_ink_center(img)
        per_button.append({"row": row, **ink, "size": [img.width, img.height]})
        ax.append(round(ink["inkCx"], 4))
        ay.append(round(ink["inkCy"], 4))
    return {"textAnchorX": ax, "textAnchorY": ay, "perButton": per_button}


def parse_swing_ratios(path: Path) -> dict[str, float]:
    """Извлекает ключевые доли из Swing MainMenuScreen."""
    text = path.read_text(encoding="utf-8")
    found: dict[str, float] = {}
    rules = [
        ("logoY", r"logoY\s*=\s*\(int\)\s*\(\s*sh\s*\*\s*(0\.\d+f)\s*\)"),
        ("signW", r"signW\s*=\s*\(int\)\s*\(\s*sw\s*\*\s*(0\.\d+f)\s*\)"),
        ("buttonW", r"plankW\s*=\s*\(int\)\s*\(\s*sw\s*\*\s*(0\.\d+f)\s*\)"),
    ]
    for key, pat in rules:
        m = re.search(pat, text)
        if m:
            found[key] = float(m.group(1).rstrip("f"))
    return found


def swing_rect_y(top_y: float, height: float, view_h: float = DESIGN_H) -> float:
    """Swing top-left → LibGDX bottom-left Y."""
    return view_h - top_y - height


def swing_text_baseline(top_y: float, view_h: float = DESIGN_H) -> float:
    return view_h - top_y


def layout_at_resolution(w: int, h: int) -> dict:
    """Пример раскладки в пикселях Swing для целевого разрешения."""
    r = MENU_RATIOS
    logo_y = h * r["logoY"]
    sign_w = w * r["signW"]
    return {
        "design": [DESIGN_W, DESIGN_H],
        "target": [w, h],
        "scaleUniform": min(w / DESIGN_W, h / DESIGN_H),
        "scaleStretchX": w / DESIGN_W,
        "scaleStretchY": h / DESIGN_H,
        "logoY": logo_y,
        "signW": sign_w,
        "helpBaselineSwingY": h - r["helpYFromBottom"],
        "transforms": {
            "rectGdxY": "viewH - swingTopY - height",
            "textGdxY": "viewH - swingTopY",
            "pointerSwingY": "(1 - screenY/fbH) * viewH → gdxToSwingY",
        },
    }


def emit_java() -> str:
    r = MENU_RATIOS
    anchors_x = ", ".join(f"{a}f" for a in r["textAnchorX"])
    anchors_y = r["textAnchorY"]
    if isinstance(anchors_y, (int, float)):
        anchors_y = [anchors_y] * len(r["textAnchorX"])
    anchors_y_str = ", ".join(f"{a}f" for a in anchors_y)
    return (
        "package main.java.com.witcher.ui.menu.view;\n\n"
        "/**\n"
        " * Раскладка главного меню — сгенерировано tools/swing_to_gdx_layout.py.\n"
        " * Координаты в пространстве Swing: 480×360, Y сверху вниз.\n"
        " * TEXT_ANCHOR_* — центр зоны пергамента на спрайте кнопки.\n"
        " */\n"
        "public final class MenuLayout {\n\n"
        f"    public static final float DESIGN_W = {DESIGN_W}f;\n"
        f"    public static final float DESIGN_H = {DESIGN_H}f;\n\n"
        f"    public static final float LOGO_Y_RATIO = {r['logoY']}f;\n"
        f"    public static final float SIGN_W_RATIO = {r['signW']}f;\n"
        f"    public static final float TITLE_LOGO_W_RATIO = {r['titleLogoW']}f;\n"
        f"    public static final float INNER_LOGO_W_OF_SIGN = {r['innerLogoWOfSign']}f;\n"
        f"    public static final float INNER_LOGO_OFFSET_Y_OF_SIGN = {r['innerLogoOffsetYOfSign']}f;\n"
        f"    public static final float LOGO_MARGIN_BOTTOM = {r['logoMarginBottom']}f;\n"
        f"    public static final float CONTENT_MARGIN_BOTTOM = {r['contentMarginBottom']}f;\n"
        f"    public static final float BUTTON_GAP_OF_AVAILABLE = {r['buttonGapOfAvailable']}f;\n"
        f"    public static final float BUTTON_W_RATIO = {r['buttonW']}f;\n"
        f"    public static final float HELP_Y_FROM_BOTTOM = {r['helpYFromBottom']}f;\n"
        f"    public static final float CURSOR_W = {r['cursorW']}f;\n"
        f"    public static final float CURSOR_HOTSPOT_X = {r['cursorHotspotX']}f;\n"
        f"    public static final float CURSOR_HOTSPOT_Y = {r['cursorHotspotY']}f;\n"
        f"    public static final float TEXT_FONT_MIN = {r['textFontMin']}f;\n"
        f"    public static final float TEXT_FONT_HEIGHT_RATIO = {r['textFontHeightRatio']}f;\n"
        f"    public static final float[] TEXT_ANCHOR_X = {{ {anchors_x} }};\n"
        f"    public static final float[] TEXT_ANCHOR_Y = {{ {anchors_y_str} }};\n\n"
        "    private MenuLayout() {\n"
        "    }\n\n"
        "    public static float logoY(float viewH) {\n"
        "        return viewH * LOGO_Y_RATIO;\n"
        "    }\n\n"
        "    public static float signW(float viewW) {\n"
        "        return viewW * SIGN_W_RATIO;\n"
        "    }\n\n"
        "    public static float textAnchorX(int buttonIndex) {\n"
        "        if (buttonIndex >= 0 && buttonIndex < TEXT_ANCHOR_X.length) {\n"
        "            return TEXT_ANCHOR_X[buttonIndex];\n"
        "        }\n"
        "        return 0.5f;\n"
        "    }\n\n"
        "    public static float textAnchorY(int buttonIndex) {\n"
        "        if (buttonIndex >= 0 && buttonIndex < TEXT_ANCHOR_Y.length) {\n"
        "            return TEXT_ANCHOR_Y[buttonIndex];\n"
        "        }\n"
        "        return 0.5f;\n"
        "    }\n\n"
        "    public static float buttonFontSize(float buttonHeight) {\n"
        "        return Math.max(TEXT_FONT_MIN, buttonHeight * TEXT_FONT_HEIGHT_RATIO);\n"
        "    }\n"
        "}\n"
    )


def main() -> None:
    parser = argparse.ArgumentParser(description="Swing → GDX layout generator")
    parser.add_argument("--target", nargs=2, type=int, metavar=("W", "H"),
                        default=[DESIGN_W, DESIGN_H], help="целевое разрешение для preview")
    parser.add_argument("--scale", type=int, default=3, help="pixel scale для справки")
    parser.add_argument("--bake", action="store_true", help="нарезать ассеты меню (bake_menu_assets.py)")
    args = parser.parse_args()

    if SWING_MENU.is_file():
        parsed = parse_swing_ratios(SWING_MENU)
        for k, v in parsed.items():
            if k in MENU_RATIOS:
                MENU_RATIOS[k] = v
                print(f"  parsed {k} = {v}")

    text_layout = analyze_menu_button_text_anchors()
    MENU_RATIOS["textAnchorX"] = text_layout["textAnchorX"]
    MENU_RATIOS["textAnchorY"] = text_layout["textAnchorY"]
    for row in text_layout.get("perButton", []):
        print(
            f"  button {row['row']}: ink center ({row['inkCx']:.3f}, {row['inkCy']:.3f})"
            f" size {row.get('size')}"
        )

    tw, th = args.target
    preview = layout_at_resolution(tw, th)
    preview["source"] = str(SWING_MENU.relative_to(ROOT))
    preview["ratios"] = MENU_RATIOS
    preview["textLayout"] = text_layout
    preview["referenceFrame"] = {
        "pixelScale": args.scale,
        "frameW": DESIGN_W * args.scale + 20,
        "frameH": DESIGN_H * args.scale + 30 + 20,
    }

    OUT_JAVA.parent.mkdir(parents=True, exist_ok=True)
    OUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUT_JAVA.write_text(emit_java(), encoding="utf-8")
    OUT_JSON.write_text(json.dumps(preview, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    print(f"OK {OUT_JAVA.relative_to(ROOT)}")
    print(f"OK {OUT_JSON.relative_to(ROOT)}")
    print(f"  uniform scale {tw}x{th}: {preview['scaleUniform']:.4f}")

    if args.bake:
        import subprocess
        bake = ROOT / "tools/bake_menu_assets.py"
        print(f"\n=== bake {bake.name} ===")
        subprocess.run([sys.executable, str(bake)], cwd=ROOT, check=True)


if __name__ == "__main__":
    main()
