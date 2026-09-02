#!/usr/bin/env python3
"""
Дизайн диалогового окна Swing → LibGDX (из DialogBoxRenderer.java).

    python tools/swing_to_gdx_dialog_box.py

Генерирует:
  src/main/java/com/witcher/ui/intro/view/IntroDialogTheme.java
  src/main/resources/gdx/layout/dialog_box_theme.json
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SWING = ROOT / "src/main/java/com/witcher/ui/graphics/DialogBoxRenderer.java"
OUT_JAVA = ROOT / "src/main/java/com/witcher/ui/intro/view/IntroDialogTheme.java"
OUT_JSON = ROOT / "src/main/resources/gdx/layout/dialog_box_theme.json"

DESIGN_W, DESIGN_H = 480, 360

THEME: dict = {
    "boxBg": [10, 8, 4, 220],
    "boxBorder": [140, 100, 35, 255],
    "speech": [220, 190, 100, 255],
    "hint": [180, 160, 120, 180],
    "narrator": [160, 145, 120, 255],
    "geralt": [160, 205, 235, 255],
    "duke": [218, 165, 32, 255],
    "gradientTop": [20, 16, 8, 80],
    "gradientBottom": [5, 4, 2, 0],
    "frameOuter": [235, 200, 110, 255],
    "frameInner": [218, 165, 32, 255],
    "frameDark": [60, 45, 15, 200],
    "cornerGold": [255, 215, 0, 220],
    "innerStroke": [255, 245, 160, 255],
    "boxFillAlphaMul": 0.90,
    "cornerSize": 12,
    "frameOuterOffset": 2,
    "frameInnerThickness": 2,
    "frameDarkInset": 4,
    "innerStrokeAlphaMul": [0.65, 0.45, 0.25],
    "speakerNamePadH": 6,
    "speakerNamePadV": 2,
    "speakerNameBoxPad": 12,
    "speakerNameOffsetY": 2,
    "speakerNameLiftExtra": 18,
    "shadowRgb": [0, 0, 0, 140],
    "outlineRgb": [12, 8, 4, 220],
    "dialogHeightRatio": 0.30,
    "dialogBottomMarginRatio": 0.02,
    "dialogFontSizeRatio": 0.040,
    "dialogPadRatio": 0.02,
    "dialogMinHeight": 52,
    "dialogMinWidth": 200,
}


def parse_color_field(text: str, field: str) -> list[int] | None:
    m = re.search(
        rf"{re.escape(field)}\s*=\s*new Color\((\d+),\s*(\d+),\s*(\d+)(?:,\s*(\d+))?\)",
        text,
    )
    if not m:
        return None
    r, g, b = int(m.group(1)), int(m.group(2)), int(m.group(3))
    a = int(m.group(4)) if m.group(4) else 255
    return [r, g, b, a]


def parse_int(text: str, pattern: str, key: str) -> None:
    m = re.search(pattern, text)
    if m:
        THEME[key] = int(m.group(1))


def parse_float(text: str, pattern: str, key: str) -> None:
    m = re.search(pattern, text)
    if m:
        THEME[key] = float(m.group(1).rstrip("f"))


def parse_sources() -> None:
    if not SWING.is_file():
        return
    t = SWING.read_text(encoding="utf-8")
    mapping = {
        "boxBg": "BOX_BG",
        "boxBorder": "BOX_BORDER",
        "speech": "SPEECH_COLOR",
        "hint": "HINT_COLOR",
        "narrator": "NARRATOR_COLOR",
        "geralt": "GERALT_COLOR",
        "duke": "DUKE_COLOR",
    }
    for key, field in mapping.items():
        c = parse_color_field(t, field)
        if c:
            THEME[key] = c

    parse_int(t, r"int cornerSize = (\d+)", "cornerSize")
    parse_float(t, r"alpha \* (0\.\d+f)\)", "boxFillAlphaMul")
    parse_float(t, r"heightRatio <= 0\.11f.*?fontSize = Math\.max\(12, \(int\) \(sh \* (0\.\d+f)\)\)",
                "dialogFontSizeRatio")
    parse_float(t, r"this\(sw, sh, (0\.\d+f), 1\.0f\)", "dialogHeightRatio")
    parse_float(t, r"boxY = sh - boxH - \(int\) \(sh \* (0\.\d+f)\)", "dialogBottomMarginRatio")
    parse_float(t, r"pad = \(int\) \(sw \* (0\.\d+f)\)", "dialogPadRatio")

    m = re.search(r"new Color\((\d+), (\d+), (\d+), 80\)", t)
    if m:
        THEME["gradientTop"] = [int(m.group(1)), int(m.group(2)), int(m.group(3)), 80]
    m = re.search(r"new Color\((\d+), (\d+), (\d+), 0\)", t)
    if m:
        THEME["gradientBottom"] = [int(m.group(1)), int(m.group(2)), int(m.group(3)), 0]
    m = re.search(r"new Color\(235, 200, 110", t)
    if m:
        THEME["frameOuter"] = [235, 200, 110, 255]
    m = re.search(r"g\.setColor\(new Color\(218, 165, 32, alpha255\)\)", t)
    if m:
        THEME["frameInner"] = [218, 165, 32, 255]


def preview_box(sw: int, sh: int) -> dict:
    r = THEME
    box_h = max(int(r["dialogMinHeight"]), int(sh * r["dialogHeightRatio"]))
    box_y = sh - box_h - int(sh * r["dialogBottomMarginRatio"])
    pad = int(sw * r["dialogPadRatio"])
    font = max(12, int(sh * r["dialogFontSizeRatio"]))
    return {
        "box": {"x": 0, "y": box_y, "w": sw, "h": box_h},
        "text": {"x": pad, "y": box_y + pad, "maxW": sw - pad * 2},
        "fontSize": font,
        "speakerPlateY": box_y - font - int(r["speakerNameOffsetY"]),
    }


def rgba_const(name: str, rgba: list[int]) -> str:
    r, g, b, a = rgba
    return (
        f"    public static final int {name}_R = {r};\n"
        f"    public static final int {name}_G = {g};\n"
        f"    public static final int {name}_B = {b};\n"
        f"    public static final int {name}_A = {a};\n"
    )


def emit_java() -> str:
    t = THEME
    return (
        "package main.java.com.witcher.ui.intro.view;\n\n"
        "/**\n"
        " * Цвета и параметры диалогового окна — tools/swing_to_gdx_dialog_box.py\n"
        " * Источник: DialogBoxRenderer.java (Swing).\n"
        " */\n"
        "public final class IntroDialogTheme {\n\n"
        + rgba_const("BOX_BG", t["boxBg"])
        + rgba_const("BOX_BORDER", t["boxBorder"])
        + rgba_const("SPEECH", t["speech"])
        + rgba_const("HINT", t["hint"])
        + rgba_const("NARRATOR", t["narrator"])
        + rgba_const("GERALT", t["geralt"])
        + rgba_const("DUKE", t["duke"])
        + rgba_const("GRADIENT_TOP", t["gradientTop"])
        + rgba_const("GRADIENT_BOTTOM", t["gradientBottom"])
        + rgba_const("FRAME_OUTER", t["frameOuter"])
        + rgba_const("FRAME_INNER", t["frameInner"])
        + rgba_const("FRAME_DARK", t["frameDark"])
        + rgba_const("CORNER_GOLD", t["cornerGold"])
        + rgba_const("INNER_STROKE", t["innerStroke"])
        + rgba_const("SHADOW", t["shadowRgb"])
        + rgba_const("OUTLINE", t["outlineRgb"])
        + "\n"
        f"    public static final float BOX_FILL_ALPHA_MUL = {t['boxFillAlphaMul']}f;\n"
        f"    public static final int CORNER_SIZE = {t['cornerSize']};\n"
        f"    public static final int FRAME_OUTER_OFFSET = {t['frameOuterOffset']};\n"
        f"    public static final int FRAME_INNER_THICKNESS = {t['frameInnerThickness']};\n"
        f"    public static final int FRAME_DARK_INSET = {t['frameDarkInset']};\n"
        f"    public static final float INNER_STROKE_ALPHA_1 = {t['innerStrokeAlphaMul'][0]}f;\n"
        f"    public static final float INNER_STROKE_ALPHA_2 = {t['innerStrokeAlphaMul'][1]}f;\n"
        f"    public static final float INNER_STROKE_ALPHA_3 = {t['innerStrokeAlphaMul'][2]}f;\n"
        f"    public static final int SPEAKER_NAME_PAD_H = {t['speakerNamePadH']};\n"
        f"    public static final int SPEAKER_NAME_PAD_V = {t['speakerNamePadV']};\n"
        f"    public static final int SPEAKER_NAME_BOX_PAD = {t['speakerNameBoxPad']};\n"
        f"    public static final int SPEAKER_NAME_OFFSET_Y = {t['speakerNameOffsetY']};\n"
        f"    public static final int SPEAKER_NAME_LIFT_EXTRA = {t['speakerNameLiftExtra']};\n"
        f"    public static final float DIALOG_HEIGHT_RATIO = {t['dialogHeightRatio']}f;\n"
        f"    public static final float DIALOG_BOTTOM_MARGIN_RATIO = {t['dialogBottomMarginRatio']}f;\n"
        f"    public static final float DIALOG_FONT_SIZE_RATIO = {t['dialogFontSizeRatio']}f;\n"
        f"    public static final float DIALOG_PAD_RATIO = {t['dialogPadRatio']}f;\n"
        f"    public static final float DIALOG_MIN_HEIGHT = {t['dialogMinHeight']}f;\n"
        f"    public static final float DIALOG_MIN_WIDTH = {t['dialogMinWidth']}f;\n\n"
        "    public static int packRgb(int r, int g, int b) {\n"
        "        return (r << 16) | (g << 8) | b;\n"
        "    }\n\n"
        "    public static int speechRgb() {\n"
        "        return packRgb(SPEECH_R, SPEECH_G, SPEECH_B);\n"
        "    }\n\n"
        "    public static int narratorRgb() {\n"
        "        return packRgb(NARRATOR_R, NARRATOR_G, NARRATOR_B);\n"
        "    }\n\n"
        "    private IntroDialogTheme() {\n"
        "    }\n"
        "}\n"
    )


def main() -> None:
    parse_sources()
    OUT_JAVA.parent.mkdir(parents=True, exist_ok=True)
    OUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUT_JAVA.write_text(emit_java(), encoding="utf-8")
    payload = {
        "design": [DESIGN_W, DESIGN_H],
        "theme": THEME,
        "preview480x360": preview_box(DESIGN_W, DESIGN_H),
    }
    OUT_JSON.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"OK {OUT_JAVA.relative_to(ROOT)}")
    print(f"OK {OUT_JSON.relative_to(ROOT)}")
    print("  preview:", json.dumps(payload["preview480x360"], ensure_ascii=False))


if __name__ == "__main__":
    main()
