#!/usr/bin/env python3
"""
Нарезка ассетов лавки под виртуальное разрешение GameWindow (480×360, отображение ×2).

Исходники: src/main/resources/assets/sprites/lavka/
Результат:  src/main/resources/assets/sprites/lavka/1x/

Запуск из корня проекта:
    python tools/bake_lavka_assets.py
"""

from __future__ import annotations

import json
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/resources/assets/sprites/lavka"
DST = SRC / "1x"
SCREEN_SAVER = ROOT / "src/main/resources/assets/sprites/screen saver"

VIRTUAL_W, VIRTUAL_H = 480, 360
CARD_W, CARD_H = 54, 81
CARD_ART = 38
GRID_COLS = 5
GRID_ROWS = 2
PANEL_W = 380
DETAIL_PANEL_W = 292
BTN_W, BTN_H = 100, 30
HUD_H = 58
ROW_H = 24
ROW_W = DETAIL_PANEL_W - 16
CHAR_H = round(VIRTUAL_H * 0.82)

PANEL_H = 22 + 6 + GRID_ROWS * CARD_H + (GRID_ROWS - 1) * 6 + 6 + BTN_H + 8


def content_bounds(img: Image.Image) -> tuple[int, int, int, int]:
    """x, y, w, h — без прозрачных и почти чёрных пикселей."""
    rgba = img.convert("RGBA")
    w, h = rgba.size
    step = max(1, min(w, h) // 256)
    min_x, min_y = w, h
    max_x, max_y = 0, 0
    for y in range(0, h, step):
        for x in range(0, w, step):
            r, g, b, a = rgba.getpixel((x, y))
            if a <= 20:
                continue
            if r < 24 and g < 24 and b < 24:
                continue
            min_x = min(min_x, x)
            min_y = min(min_y, y)
            max_x = max(max_x, x)
            max_y = max(max_y, y)
    if max_x < min_x:
        return 0, 0, w, h
    return min_x, min_y, max_x - min_x + 1, max_y - min_y + 1


def crisp_resize(img: Image.Image, dst_w: int, dst_h: int) -> Image.Image:
    """Только NEAREST; перед финалом — деление пополам, без «мыла»."""
    work = img.convert("RGBA")
    while work.width > dst_w * 2 and work.height > dst_h * 2:
        work = work.resize((work.width // 2, work.height // 2), Image.Resampling.NEAREST)
    if work.width != dst_w or work.height != dst_h:
        work = work.resize((dst_w, dst_h), Image.Resampling.NEAREST)
    return work


def crop_region(img: Image.Image, box: tuple[int, int, int, int]) -> Image.Image:
    x, y, w, h = box
    return img.crop((x, y, x + w, y + h))


def load_src(rel: str, fallback: Path | None = None) -> Image.Image | None:
    path = SRC / rel
    if not path.is_file() and fallback and fallback.is_file():
        path = fallback
    if not path.is_file():
        return None
    return Image.open(path)


def bake_image(img: Image.Image, dst_w: int, dst_h: int, out_rel: str, *, crop: bool = False) -> dict | None:
    work = img
    src_size = list(img.size)
    if crop:
        work = crop_region(work, content_bounds(work))
    out = crisp_resize(work, dst_w, dst_h)
    out_path = DST / out_rel
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out.save(out_path, optimize=True)
    print(f"  OK {out_rel}: {src_size} -> {dst_w}x{dst_h} ({out_path.stat().st_size // 1024} KB)")
    return {
        "output": f"1x/{out_rel}",
        "size": [dst_w, dst_h],
        "bytes": out_path.stat().st_size,
    }


def portrait_fallback(name: str) -> Path:
    mapping = {
        "geralt_portrait_shop.png": SCREEN_SAVER / "geralt_portrait.png",
        "duke_portrait_shop.png": SCREEN_SAVER / "duke_portrait.png",
        "duke_portrait_fun_shop.png": SCREEN_SAVER / "duke_portrait_fun.png",
    }
    return mapping.get(name, SRC / name)


def main() -> None:
    print(f"Источник: {SRC}")
    print(f"Выход:    {DST}")
    print(f"Виртуалка {VIRTUAL_W}x{VIRTUAL_H}, портреты h={CHAR_H}, иконка карты {CARD_ART}px\n")

    manifest: list[dict] = []

    jobs: list[tuple[str, int, int, dict]] = [
        ("ui/shop_hud_bar.png", PANEL_W, HUD_H, {"crop": True}),
        ("ui/shop_catalog_panel.png", PANEL_W, PANEL_H, {"crop": False}),
        ("ui/shop_catalog_panel.png", DETAIL_PANEL_W, 232, {"crop": False, "out": "ui/shop_catalog_panel_detail.png"}),
        ("ui/shop_card_front.png", CARD_W, CARD_H, {"crop": True}),
        ("ui/shop_card_back.png", CARD_W, CARD_H, {"crop": True}),
        ("ui/shop_card_hover.png", CARD_W, CARD_H, {"crop": True}),
        ("ui/shop_card_selected.png", CARD_W, CARD_H, {"crop": True}),
        ("ui/shop_btn_buy_disabled.png", BTN_W, BTN_H, {}),
        ("ui/shop_btn_buy_normal.png", BTN_W, BTN_H, {}),
        ("ui/shop_row_normal.png", ROW_W, ROW_H, {}),
        ("ui/shop_row_hover.png", ROW_W, ROW_H, {}),
        ("ui/shop_row_selected.png", ROW_W, ROW_H, {}),
        ("ui/icon_legendary_frame.png", CARD_ART, CARD_ART, {"crop": True}),
        ("icons/icon_crown.png", 18, 18, {"crop": True}),
        ("icons/icon_crown_small.png", 10, 10, {"crop": True, "src": "icons/icon_crown.png"}),
        ("icons/icon_armor_chest.png", CARD_ART, CARD_ART, {"crop": True}),
        ("icons/icon_armor_legs.png", CARD_ART, CARD_ART, {"crop": True}),
        ("icons/icon_armor_gloves.png", CARD_ART, CARD_ART, {"crop": True}),
        ("icons/icon_armor_boots.png", CARD_ART, CARD_ART, {"crop": True}),
        ("icons/icon_potion.png", CARD_ART, CARD_ART, {"crop": True}),
    ]

    for rel, w, h, opts in jobs:
        src_rel = opts.get("src", rel)
        out_rel = opts.get("out", rel)
        img = load_src(src_rel)
        if img is None:
            print(f"  SKIP (нет файла): {src_rel}")
            continue
        info = bake_image(img, w, h, out_rel, crop=opts.get("crop", False))
        if info:
            manifest.append(info)

    counter_src = SRC / "ui/shop_counter_foreground.png"
    if counter_src.is_file():
        counter_h = (VIRTUAL_H - 54) - (4 + HUD_H + 2) - 4
        info = bake_image(Image.open(counter_src), VIRTUAL_W, counter_h, "ui/shop_counter_foreground.png", crop=False)
        if info:
            manifest.append(info)

    for name in ("geralt_portrait_shop.png", "duke_portrait_shop.png", "duke_portrait_fun_shop.png"):
        img = load_src(name, portrait_fallback(name))
        if img is None:
            print(f"  SKIP portrait: {name}")
            continue
        cropped = crop_region(img, content_bounds(img))
        cw = round(cropped.width * (CHAR_H / cropped.height))
        info = bake_image(cropped, cw, CHAR_H, name, crop=False)
        if info:
            manifest.append(info)

    bg = load_src("merchant_bg_lavka.png")
    if bg is not None:
        info = bake_image(bg, VIRTUAL_W, VIRTUAL_H, "merchant_bg_lavka.png", crop=False)
        if info:
            manifest.append(info)

    meta = {
        "virtualResolution": [VIRTUAL_W, VIRTUAL_H],
        "panelW": PANEL_W,
        "detailPanelW": DETAIL_PANEL_W,
        "cardSize": [CARD_W, CARD_H],
        "cardArt": CARD_ART,
        "charHeight": CHAR_H,
        "assets": manifest,
    }
    meta_path = DST / "manifest.json"
    meta_path.write_text(json.dumps(meta, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"\nГотово: {len(manifest)} файлов -> {meta_path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
