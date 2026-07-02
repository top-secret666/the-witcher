#!/usr/bin/env python3
"""
Нарезка ассетов лавки под виртуальное разрешение GameWindow (480x360, scale x2).

Исходники: src/main/resources/assets/sprites/lavka/
Результат:  src/main/resources/assets/sprites/lavka/1x/

Запуск из корня проекта:
    python tools/bake_lavka_assets.py
"""

from __future__ import annotations

import json
import os
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "src/main/resources/assets/sprites/lavka"
DST = SRC / "1x"

VIRTUAL_W, VIRTUAL_H = 480, 360
CARD_W, CARD_H = 54, 81
CARD_ART = 32
GRID_COLS = 5
GRID_ROWS = 2
PANEL_W = 380
BTN_W, BTN_H = 100, 30
HUD_H = 58
PANEL_HEADER_H = 8
PANEL_H = PANEL_HEADER_H + 4 + GRID_ROWS * CARD_H + (GRID_ROWS - 1) * 6 + 6 + BTN_H + 8
CHAR_H = round(VIRTUAL_H * 0.70)


def ensure_weapon_icon() -> None:
    """Заглушка, пока нет своего icon_weapon.png."""
    path = SRC / "icons/icon_weapon.png"
    if path.is_file():
        return
    path.parent.mkdir(parents=True, exist_ok=True)
    size = 64
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    px = img.load()
    cx = size // 2
    steel = (185, 195, 210, 255)
    gold = (210, 170, 55, 255)
    grip = (90, 55, 30, 255)
    for y in range(12, 46):
        px[cx - 1, y] = steel
        px[cx, y] = steel
    for y in range(46, 54):
        for dx in (-1, 0, 1):
            px[cx + dx, y] = grip
    for x in range(cx - 4, cx + 5):
        px[x, 54] = gold
        px[x, 55] = gold
    for x in range(cx - 1, cx + 2):
        px[x, 10] = steel
        px[x, 11] = steel
    img.save(path)
    print(f"  PLACEHOLDER icons/icon_weapon.png ({size}x{size})")


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
    work = img.convert("RGBA")
    while work.width > dst_w * 2 and work.height > dst_h * 2:
        work = work.resize((work.width // 2, work.height // 2), Image.NEAREST)
    return work.resize((dst_w, dst_h), Image.NEAREST)


def crisp_resize_icon(img: Image.Image, dst_w: int, dst_h: int) -> Image.Image:
    """Иконки: только деление на 2, без финального «ломающего» ресайза если получилось ровно."""
    work = img.convert("RGBA")
    while work.width > dst_w and work.height > dst_h:
        if work.width == dst_w * 2 and work.height == dst_h * 2:
            work = work.resize((dst_w, dst_h), Image.NEAREST)
            return work
        if work.width > dst_w * 2 and work.height > dst_h * 2:
            work = work.resize((work.width // 2, work.height // 2), Image.NEAREST)
            continue
        break
    if work.width != dst_w or work.height != dst_h:
        work = work.resize((dst_w, dst_h), Image.NEAREST)
    return work


def crop_region(img: Image.Image, box: tuple[int, int, int, int]) -> Image.Image:
    x, y, w, h = box
    return img.crop((x, y, x + w, y + h))


def cover_size(src_w: int, src_h: int, view_w: int, view_h: int) -> tuple[int, int]:
    scale = max(view_w / src_w, view_h / src_h)
    return round(src_w * scale), round(src_h * scale)


def char_size(src_w: int, src_h: int, target_h: int) -> tuple[int, int]:
    target_w = round(src_w * (target_h / src_h))
    return target_w, target_h


def bake_file(rel: str, dst_w: int, dst_h: int, *, crop: bool = False) -> dict | None:
    src_path = SRC / rel
    if not src_path.is_file():
        print(f"  SKIP (нет файла): {rel}")
        return None

    img = Image.open(src_path)
    if crop:
        box = content_bounds(img)
        img = crop_region(img, box)

    out = crisp_resize(img, dst_w, dst_h)
    out_path = DST / rel
    out_path.parent.mkdir(parents=True, exist_ok=True)
    out.save(out_path, optimize=True)

    info = {
        "source": rel,
        "sourceSize": list(img.size) if crop else list(Image.open(src_path).size),
        "output": f"1x/{rel}",
        "outputSize": [dst_w, dst_h],
        "bytes": out_path.stat().st_size,
    }
    print(f"  OK {rel}: {info['sourceSize']} -> {dst_w}x{dst_h} ({info['bytes'] // 1024} KB)")
    return info


def main() -> None:
    print(f"Источник: {SRC}")
    print(f"Выход:    {DST}\n")

    ensure_weapon_icon()

    manifest: list[dict] = []

    jobs: list[tuple[str, int, int, dict]] = [
        ("ui/shop_hud_bar.png", PANEL_W, HUD_H, {"crop": True}),
        ("ui/shop_catalog_panel.png", PANEL_W, PANEL_H, {}),
        ("ui/shop_card_front.png", CARD_W, CARD_H, {}),
        ("ui/shop_card_back.png", CARD_W, CARD_H, {}),
        ("ui/shop_card_hover.png", CARD_W, CARD_H, {}),
        ("ui/shop_card_selected.png", CARD_W, CARD_H, {}),
        ("ui/shop_btn_buy_disabled.png", BTN_W, BTN_H, {}),
        ("ui/shop_btn_buy_normal.png", BTN_W, BTN_H, {}),
        ("icons/icon_crown.png", 18, 18, {"crop": True, "icon": True}),
        ("icons/icon_crown_small.png", 10, 10, {"crop": True, "src": "icons/icon_crown.png", "icon": True}),
        ("icons/icon_duke_seal.png", 32, 32, {"crop": True, "icon": True}),
        ("icons/icon_armor_chest.png", CARD_ART, CARD_ART, {"crop": True, "icon": True}),
        ("icons/icon_armor_legs.png", CARD_ART, CARD_ART, {"crop": True, "icon": True}),
        ("icons/icon_armor_gloves.png", CARD_ART, CARD_ART, {"crop": True, "icon": True}),
        ("icons/icon_armor_boots.png", CARD_ART, CARD_ART, {"crop": True, "icon": True}),
        ("icons/icon_potion.png", CARD_ART, CARD_ART, {"crop": True, "icon": True}),
        ("icons/icon_weapon.png", CARD_ART, CARD_ART, {"crop": True, "icon": True}),
        ("icons/icon_armor_set.png", CARD_ART, CARD_ART, {"crop": True, "icon": True}),
        ("icons/icon_inventory_bag.png", 32, 32, {"crop": True, "icon": True}),
    ]

    for rel, w, h, opts in jobs:
        src_rel = opts.get("src", rel)
        src_path = SRC / src_rel
        if not src_path.is_file():
            print(f"  SKIP: {rel}")
            continue
        img = Image.open(src_path)
        if opts.get("crop"):
            img = crop_region(img, content_bounds(img))
        resize = crisp_resize_icon if opts.get("icon") else crisp_resize
        out = resize(img, w, h)
        out_path = DST / rel
        out_path.parent.mkdir(parents=True, exist_ok=True)
        out.save(out_path, optimize=True)
        manifest.append({
            "output": f"1x/{rel}",
            "size": [w, h],
            "bytes": out_path.stat().st_size,
        })
        print(f"  OK {rel}: -> {w}x{h}")

    for name in ("geralt_portrait_shop.png", "duke_portrait_shop.png", "duke_portrait_fun_shop.png"):
        src_path = SRC / name
        if not src_path.is_file():
            fb = ROOT / f"src/main/resources/assets/sprites/screen saver/{name.replace('_shop', '')}"
            if name == "geralt_portrait_shop.png":
                fb = ROOT / "src/main/resources/assets/sprites/screen saver/geralt_portrait.png"
            if fb.is_file():
                src_path = fb
        if not src_path.is_file():
            print(f"  SKIP portrait: {name}")
            continue
        img = Image.open(src_path)
        cw, ch = char_size(img.width, img.height, CHAR_H)
        out = crisp_resize(img, cw, ch)
        out_path = DST / name
        out.save(out_path, optimize=True)
        manifest.append({"output": f"1x/{name}", "size": [cw, ch]})
        print(f"  OK {name}: -> {cw}x{ch}")

    bg_path = SRC / "merchant_bg_lavka.png"
    if bg_path.is_file():
        img = Image.open(bg_path)
        bw, bh = cover_size(img.width, img.height, VIRTUAL_W, VIRTUAL_H)
        out = crisp_resize(img, bw, bh)
        out_path = DST / "merchant_bg_lavka.png"
        out.save(out_path, optimize=True)
        manifest.append({"output": "1x/merchant_bg_lavka.png", "size": [bw, bh]})
        print(f"  OK merchant_bg_lavka.png: -> {bw}x{bh}")

    meta = {
        "virtualResolution": [VIRTUAL_W, VIRTUAL_H],
        "panelW": PANEL_W,
        "cardSize": [CARD_W, CARD_H],
        "assets": manifest,
    }
    meta_path = DST / "manifest.json"
    meta_path.write_text(json.dumps(meta, indent=2, ensure_ascii=False), encoding="utf-8")
    print(f"\nГотово: {len(manifest)} файлов, manifest -> {meta_path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
