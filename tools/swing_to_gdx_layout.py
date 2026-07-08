#!/usr/bin/env python3
"""
Перевод координат Swing → LibGDX (дизайн 480×360, Swing-Y сверху).

Читает исходники Swing, извлекает доли экрана (sw*0.45f и т.п.) и генерирует
Java-классы раскладки для GDX. Запуск из корня:

    python tools/swing_to_gdx_layout.py
    python tools/swing_to_gdx_layout.py --target 1920 1080 --scale 4

Результат:
  core/src/main/java/main/java/com/witcher/gdx/layout/MenuLayout.java
  src/main/resources/gdx/layout/menu_layout.json
"""

from __future__ import annotations

import argparse
import json
import re
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SWING_MENU = ROOT / "src/main/java/com/witcher/ui/graphics/MainMenuScreen.java"
OUT_JAVA = ROOT / "core/src/main/java/main/java/com/witcher/gdx/layout/MenuLayout.java"
OUT_JSON = ROOT / "src/main/resources/gdx/layout/menu_layout.json"

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
    "cursorW": 28,
    "cursorHotspotX": 4,
    "cursorHotspotY": 4,
    "textAnchorX": [0.43, 0.47, 0.47],
    "textAnchorY": 0.54,
    "textFontMin": 16,
    "textFontHeightRatio": 0.36,
}


def parse_swing_ratios(path: Path) -> dict[str, float]:
    """Извлекает ключевые доли из Swing MainMenuScreen."""
    text = path.read_text(encoding="utf-8")
    found: dict[str, float] = {}
    rules = [
        ("logoY", r"logoY\s*=\s*\(int\)\s*\(\s*sh\s*\*\s*(0\.\d+f)\s*\)"),
        ("signW", r"signW\s*=\s*\(int\)\s*\(\s*sw\s*\*\s*(0\.\d+f)\s*\)"),
        ("buttonW", r"plankW\s*=\s*\(int\)\s*\(\s*sw\s*\*\s*(0\.\d+f)\s*\)"),
        ("textFontHeightRatio", r"r\.height\s*\*\s*(0\.\d+f)\)"),
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
    anchors = ", ".join(f"{a}f" for a in r["textAnchorX"])
    return (
        "package main.java.com.witcher.gdx.layout;\n\n"
        "/**\n"
        " * Раскладка главного меню — сгенерировано tools/swing_to_gdx_layout.py.\n"
        " * Координаты в пространстве Swing: 480×360, Y сверху вниз.\n"
        " * Рендер через main.java.com.witcher.gdx.graphics.SwingCoords.\n"
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
        f"    public static final float TEXT_ANCHOR_Y = {r['textAnchorY']}f;\n"
        f"    public static final float TEXT_FONT_MIN = {r['textFontMin']}f;\n"
        f"    public static final float TEXT_FONT_HEIGHT_RATIO = {r['textFontHeightRatio']}f;\n"
        f"    public static final float[] TEXT_ANCHOR_X = {{ {anchors} }};\n\n"
        "    private MenuLayout() {\n"
        "    }\n\n"
        "    public static float logoY(float viewH) {\n"
        "        return viewH * LOGO_Y_RATIO;\n"
        "    }\n\n"
        "    public static float signW(float viewW) {\n"
        "        return viewW * SIGN_W_RATIO;\n"
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

    tw, th = args.target
    preview = layout_at_resolution(tw, th)
    preview["source"] = str(SWING_MENU.relative_to(ROOT))
    preview["ratios"] = MENU_RATIOS
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
