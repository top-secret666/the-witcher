#!/usr/bin/env python3
"""
Раскладка интро Swing → LibGDX (480×360, Y сверху).

    python tools/swing_to_gdx_intro_layout.py

Генерирует:
  src/main/java/com/witcher/ui/intro/view/IntroLayout.java
  src/main/resources/gdx/layout/intro_layout.json
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INTRO_SCREEN = ROOT / "src/main/java/com/witcher/ui/graphics/IntroScreen.java"
DIALOG_LAYOUT = ROOT / "src/main/java/com/witcher/ui/intro/view/IntroDialogLayout.java"
CHAR_LAYOUT = ROOT / "src/main/java/com/witcher/ui/intro/view/IntroCharacterLayout.java"
VN_UI = ROOT / "src/main/java/com/witcher/ui/intro/IntroVnUi.java"
OUT_JAVA = ROOT / "src/main/java/com/witcher/ui/intro/view/IntroLayout.java"
OUT_JSON = ROOT / "src/main/resources/gdx/layout/intro_layout.json"

DESIGN_W, DESIGN_H = 480, 360

RATIOS = {
    "dialogHeightRatio": 0.30,
    "dialogWidthRatio": 1.0,
    "dialogBottomMarginRatio": 0.02,
    "dialogFontSizeRatio": 0.040,
    "dialogPadRatio": 0.02,
    "dialogMinHeight": 52,
    "dialogMinWidth": 200,
    "characterHeightRatio": 0.85,
    "dialogZoneRatio": 0.15,
    "characterFeetInsetRatio": 0.15,
    "characterMarginXRatio": 0.02,
    "characterActiveShiftRatio": 0.03,
    "characterLiftRatio": 0.06,
    "characterRaiseRatio": 0.08,
    "characterScaleBoost": 0.06,
    "characterShopShrink": 0.92,
    "vnFontSizeRatio": 0.031,
    "vnFontMin": 10,
    "vnButtonPadV": 8,
    "vnButtonGapRatio": 0.09,
    "vnButtonGapMin": 28,
    "vnTextWidthFactor": 0.55,
    "vnRowBottomMargin": 6,
    "vnRowGapBelowDialog": 4,
    "vnHistoryPanelWRatio": 0.82,
    "vnHistoryPanelHRatio": 0.72,
    "vnHistoryPanelMinW": 280,
    "vnHistoryPanelMinH": 200,
    "bgFadeMul": 0.82,
    "cursorW": 14,
    "cursorHotspotX": 3,
    "cursorHotspotY": 3,
}


def parse_ratio(text: str, pattern: str, key: str) -> None:
    m = re.search(pattern, text)
    if m:
        RATIOS[key] = float(m.group(1).rstrip("f"))


def parse_sources() -> None:
    if INTRO_SCREEN.is_file():
        t = INTRO_SCREEN.read_text(encoding="utf-8")
        parse_ratio(t, r"sw\s*\*\s*(0\.\d+f)\s*\*\s*activeAnim", "characterActiveShiftRatio")
        parse_ratio(t, r"getFadeAlpha\(\)\s*\*\s*(0\.\d+f)", "bgFadeMul")
    if DIALOG_LAYOUT.is_file():
        t = DIALOG_LAYOUT.read_text(encoding="utf-8")
        parse_ratio(t, r"new Layout\(sw, sh,\s*(0\.\d+f),\s*(0\.\d+f)\)", "dialogHeightRatio")
    if CHAR_LAYOUT.is_file():
        t = CHAR_LAYOUT.read_text(encoding="utf-8")
        parse_ratio(t, r"baseCharScale\s*=\s*\(sh\s*\*\s*(0\.\d+f)\)\s*/\s*spriteH", "characterHeightRatio")
        parse_ratio(t, r"dialogZone\s*=\s*\(int\)\s*\(sh\s*\*\s*(0\.\d+f)\)", "dialogZoneRatio")
        parse_ratio(t, r"ch\s*\*\s*(0\.\d+f)\)", "characterFeetInsetRatio")
    if VN_UI.is_file():
        t = VN_UI.read_text(encoding="utf-8")
        parse_ratio(t, r"sh\s*\*\s*(0\.\d+f)\)", "vnFontSizeRatio")


def preview_layout(sw: int, sh: int) -> dict:
    r = RATIOS
    box_h = max(int(r["dialogMinHeight"]), int(sh * r["dialogHeightRatio"]))
    box_y = sh - box_h - int(sh * r["dialogBottomMarginRatio"])
    font = max(12, int(sh * r["dialogFontSizeRatio"]))
    btn_h = max(int(r["vnFontMin"]), int(sh * r["vnFontSizeRatio"])) + int(r["vnButtonPadV"])
    vn_y = box_y + box_h + int(r["vnRowGapBelowDialog"])
    if vn_y + btn_h > sh - int(r["vnRowBottomMargin"]):
        vn_y = sh - btn_h - int(r["vnRowBottomMargin"])
    char_scale = (sh * r["characterHeightRatio"]) / 1280
    ch = int(1280 * char_scale)
    base_y = sh - int(sh * r["dialogZoneRatio"]) - ch + int(ch * r["characterFeetInsetRatio"])
    return {
        "dialogBox": {"x": (sw - sw) // 2, "y": box_y, "w": sw, "h": box_h},
        "vnRowY": vn_y,
        "characterExample": {"y": base_y, "h": ch, "scale": round(char_scale, 4)},
        "fontSize": font,
    }


def emit_java() -> str:
    r = RATIOS
    return (
        "package main.java.com.witcher.ui.intro.view;\n\n"
        "/**\n"
        " * Раскладка интро — tools/swing_to_gdx_intro_layout.py\n"
        f" * Кадр Swing: {DESIGN_W}×{DESIGN_H}, Y сверху.\n"
        " */\n"
        "public final class IntroLayout {\n\n"
        f"    public static final float DESIGN_W = {DESIGN_W}f;\n"
        f"    public static final float DESIGN_H = {DESIGN_H}f;\n\n"
        f"    public static final float DIALOG_HEIGHT_RATIO = {r['dialogHeightRatio']}f;\n"
        f"    public static final float DIALOG_WIDTH_RATIO = {r['dialogWidthRatio']}f;\n"
        f"    public static final float DIALOG_BOTTOM_MARGIN_RATIO = {r['dialogBottomMarginRatio']}f;\n"
        f"    public static final float DIALOG_FONT_SIZE_RATIO = {r['dialogFontSizeRatio']}f;\n"
        f"    public static final float DIALOG_PAD_RATIO = {r['dialogPadRatio']}f;\n"
        f"    public static final float DIALOG_MIN_HEIGHT = {r['dialogMinHeight']}f;\n"
        f"    public static final float DIALOG_MIN_WIDTH = {r['dialogMinWidth']}f;\n"
        f"    public static final float CHARACTER_HEIGHT_RATIO = {r['characterHeightRatio']}f;\n"
        f"    public static final float DIALOG_ZONE_RATIO = {r['dialogZoneRatio']}f;\n"
        f"    public static final float CHARACTER_FEET_INSET_RATIO = {r['characterFeetInsetRatio']}f;\n"
        f"    public static final float CHARACTER_MARGIN_X_RATIO = {r['characterMarginXRatio']}f;\n"
        f"    public static final float CHARACTER_ACTIVE_SHIFT_RATIO = {r['characterActiveShiftRatio']}f;\n"
        f"    public static final float CHARACTER_LIFT_RATIO = {r['characterLiftRatio']}f;\n"
        f"    public static final float CHARACTER_RAISE_RATIO = {r['characterRaiseRatio']}f;\n"
        f"    public static final float CHARACTER_SCALE_BOOST = {r['characterScaleBoost']}f;\n"
        f"    public static final float CHARACTER_SHOP_SHRINK = {r['characterShopShrink']}f;\n"
        f"    public static final float VN_FONT_SIZE_RATIO = {r['vnFontSizeRatio']}f;\n"
        f"    public static final float VN_FONT_MIN = {r['vnFontMin']}f;\n"
        f"    public static final float VN_BUTTON_PAD_V = {r['vnButtonPadV']}f;\n"
        f"    public static final float VN_BUTTON_GAP_RATIO = {r['vnButtonGapRatio']}f;\n"
        f"    public static final float VN_BUTTON_GAP_MIN = {r['vnButtonGapMin']}f;\n"
        f"    public static final float VN_TEXT_WIDTH_FACTOR = {r['vnTextWidthFactor']}f;\n"
        f"    public static final float VN_ROW_BOTTOM_MARGIN = {r['vnRowBottomMargin']}f;\n"
        f"    public static final float VN_ROW_GAP_BELOW_DIALOG = {r['vnRowGapBelowDialog']}f;\n"
        f"    public static final float VN_HISTORY_PANEL_W_RATIO = {r['vnHistoryPanelWRatio']}f;\n"
        f"    public static final float VN_HISTORY_PANEL_H_RATIO = {r['vnHistoryPanelHRatio']}f;\n"
        f"    public static final float VN_HISTORY_PANEL_MIN_W = {r['vnHistoryPanelMinW']}f;\n"
        f"    public static final float VN_HISTORY_PANEL_MIN_H = {r['vnHistoryPanelMinH']}f;\n"
        f"    public static final float BG_FADE_MUL = {r['bgFadeMul']}f;\n"
        f"    public static final float CURSOR_W = {r['cursorW']}f;\n"
        f"    public static final float CURSOR_HOTSPOT_X = {r['cursorHotspotX']}f;\n"
        f"    public static final float CURSOR_HOTSPOT_Y = {r['cursorHotspotY']}f;\n\n"
        "    private IntroLayout() {\n"
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
        "ratios": RATIOS,
        "preview480x360": preview_layout(DESIGN_W, DESIGN_H),
        "gdxNotes": {
            "rectY": "viewH - swingTopY - height",
            "textBaseline": "viewH - swingY",
        },
    }
    OUT_JSON.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"OK {OUT_JAVA.relative_to(ROOT)}")
    print(f"OK {OUT_JSON.relative_to(ROOT)}")
    print("  preview:", json.dumps(payload["preview480x360"], ensure_ascii=False))


if __name__ == "__main__":
    main()
