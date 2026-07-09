#!/usr/bin/env python3
"""
Окно истории интро Swing → LibGDX (из IntroScreen.drawHistoryOverlay).

    python tools/swing_to_gdx_history_panel.py

Генерирует:
  src/main/java/com/witcher/ui/intro/view/IntroHistoryTheme.java
  src/main/resources/gdx/layout/history_panel_theme.json
"""

from __future__ import annotations

import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INTRO_SCREEN = ROOT / "src/main/java/com/witcher/ui/graphics/IntroScreen.java"
UI_CHROME = ROOT / "src/main/java/com/witcher/ui/graphics/UiChrome.java"
OUT_JAVA = ROOT / "src/main/java/com/witcher/ui/intro/view/IntroHistoryTheme.java"
OUT_JSON = ROOT / "src/main/resources/gdx/layout/history_panel_theme.json"

DESIGN_W, DESIGN_H = 480, 360

THEME = {
    "dimAlpha": 0.62,
    "fontSizeRatio": 0.034,
    "fontSizeMin": 11,
    "titleSizeDelta": 1,
    "hintSizeDelta": -1,
    "hintSizeMin": 10,
    "lineGap": 4,
    "padRatio": 0.022,
    "padMin": 10,
    "headerGap": 8,
    "footerGap": 6,
    "titleRgb": [218, 165, 32],
    "dividerRgb": [100, 80, 45],
    "dividerAlpha": 160,
    "speakerRgb": [180, 150, 90],
    "bodyRgb": [210, 195, 155],
    "hintRgb": [150, 130, 95],
    "hintAlpha": 200,
    "closeBtnSize": 18,
    "closeBtnMarginX": 6,
    "closeBtnMarginY": 5,
    "panelWRatio": 0.82,
    "panelHRatio": 0.72,
    "panelMinW": 280,
    "panelMinH": 200,
}


def parse_float(text: str, pattern: str, key: str) -> None:
    m = re.search(pattern, text)
    if m:
        THEME[key] = float(m.group(1).rstrip("f"))


def parse_int(text: str, pattern: str, key: str) -> None:
    m = re.search(pattern, text)
    if m:
        THEME[key] = int(m.group(1))


def parse_rgb(text: str, pattern: str, key: str) -> None:
    m = re.search(pattern, text)
    if m and m.lastindex and m.lastindex >= 3:
        THEME[key] = [int(m.group(1)), int(m.group(2)), int(m.group(3))]


def parse_sources() -> None:
    if INTRO_SCREEN.is_file():
        t = INTRO_SCREEN.read_text(encoding="utf-8")
        parse_float(t, r"getFadeAlpha\(\) \* (0\.\d+f)\)", "dimAlpha")
        parse_float(t, r"int fontSize = Math\.max\(11, \(int\) \(sh \* (0\.\d+f)\)\)", "fontSizeRatio")
        parse_int(t, r"Math\.max\((\d+), \(int\) \(sh \* 0\.034f\)\)", "fontSizeMin")
        parse_int(t, r"pad = Math\.max\((\d+), \(int\) \(sw \* 0\.022f\)\)", "padMin")
        parse_rgb(t, r'new Color\((\d+),\s*(\d+),\s*(\d+)', "titleRgb")
        parse_rgb(t, r'new Color\((\d+),\s*(\d+),\s*(\d+),\s*Math\.max', "dividerRgb")
        parse_rgb(t, r'new Color\(180,\s*150,\s*90', "speakerRgb")
        parse_rgb(t, r'new Color\(210,\s*195,\s*155', "bodyRgb")
        parse_rgb(t, r'new Color\(150,\s*130,\s*95', "hintRgb")
    if UI_CHROME.is_file():
        t = UI_CHROME.read_text(encoding="utf-8")
        parse_int(t, r"BTN_SIZE = (\d+)", "closeBtnSize")
        parse_int(t, r"panelW - BTN_SIZE - (\d+)", "closeBtnMarginX")
        parse_int(t, r"panelY \+ (\d+)", "closeBtnMarginY")


def preview(sw: int, sh: int) -> dict:
    t = THEME
    pw = max(int(t["panelMinW"]), int(sw * t["panelWRatio"]))
    ph = max(int(t["panelMinH"]), int(sh * t["panelHRatio"]))
    px = (sw - pw) // 2
    py = (sh - ph) // 2
    pad = max(int(t["padMin"]), int(sw * t["padRatio"]))
    return {"panel": {"x": px, "y": py, "w": pw, "h": ph}, "pad": pad,
            "fontSize": max(int(t["fontSizeMin"]), int(sh * t["fontSizeRatio"]))}


def rgba(name: str, rgb: list[int], alpha: int | None = None) -> str:
    r, g, b = rgb[:3]
    a = alpha if alpha is not None else (rgb[3] if len(rgb) > 3 else 255)
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
        " * Окно истории интро — tools/swing_to_gdx_history_panel.py\n"
        " * Источник: IntroScreen.drawHistoryOverlay (Swing).\n"
        " */\n"
        "public final class IntroHistoryTheme {\n\n"
        f"    public static final float DIM_ALPHA = {t['dimAlpha']}f;\n"
        f"    public static final float FONT_SIZE_RATIO = {t['fontSizeRatio']}f;\n"
        f"    public static final int FONT_SIZE_MIN = {t['fontSizeMin']};\n"
        f"    public static final int TITLE_SIZE_DELTA = {t['titleSizeDelta']};\n"
        f"    public static final int HINT_SIZE_DELTA = {t['hintSizeDelta']};\n"
        f"    public static final int HINT_SIZE_MIN = {t['hintSizeMin']};\n"
        f"    public static final int LINE_GAP = {t['lineGap']};\n"
        f"    public static final float PAD_RATIO = {t['padRatio']}f;\n"
        f"    public static final int PAD_MIN = {t['padMin']};\n"
        f"    public static final int HEADER_GAP = {t['headerGap']};\n"
        f"    public static final int FOOTER_GAP = {t['footerGap']};\n"
        + rgba("TITLE", t["titleRgb"])
        + rgba("DIVIDER", t["dividerRgb"], t["dividerAlpha"])
        + rgba("SPEAKER", t["speakerRgb"])
        + rgba("BODY", t["bodyRgb"])
        + rgba("HINT", t["hintRgb"], t["hintAlpha"])
        + f"    public static final int CLOSE_BTN_SIZE = {t['closeBtnSize']};\n"
        f"    public static final int CLOSE_BTN_MARGIN_X = {t['closeBtnMarginX']};\n"
        f"    public static final int CLOSE_BTN_MARGIN_Y = {t['closeBtnMarginY']};\n"
        f"    public static final float PANEL_W_RATIO = {t['panelWRatio']}f;\n"
        f"    public static final float PANEL_H_RATIO = {t['panelHRatio']}f;\n"
        f"    public static final int PANEL_MIN_W = {t['panelMinW']};\n"
        f"    public static final int PANEL_MIN_H = {t['panelMinH']};\n\n"
        "    public static int fontSize(int viewH) {\n"
        "        return Math.max(FONT_SIZE_MIN, Math.round(viewH * FONT_SIZE_RATIO));\n"
        "    }\n\n"
        "    public static int pad(int viewW) {\n"
        "        return Math.max(PAD_MIN, Math.round(viewW * PAD_RATIO));\n"
        "    }\n\n"
        "    private IntroHistoryTheme() {\n"
        "    }\n"
        "}\n"
    )


def main() -> None:
    parse_sources()
    OUT_JAVA.parent.mkdir(parents=True, exist_ok=True)
    OUT_JSON.parent.mkdir(parents=True, exist_ok=True)
    OUT_JAVA.write_text(emit_java(), encoding="utf-8")
    payload = {"design": [DESIGN_W, DESIGN_H], "theme": THEME, "preview480x360": preview(DESIGN_W, DESIGN_H)}
    OUT_JSON.write_text(json.dumps(payload, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    print(f"OK {OUT_JAVA.relative_to(ROOT)}")
    print(f"OK {OUT_JSON.relative_to(ROOT)}")
    print("  preview:", json.dumps(payload["preview480x360"], ensure_ascii=False))


if __name__ == "__main__":
    main()
