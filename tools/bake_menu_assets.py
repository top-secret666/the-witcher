#!/usr/bin/env python3
"""
Нарезка ассетов меню под виртуальный кадр 480×360 (как Swing + bake_lavka_assets).

Исходники: src/main/resources/assets/sprites/menu/
Результат:  src/main/resources/assets/sprites/menu/1x/

Запуск:
    python tools/bake_menu_assets.py
    python tools/swing_to_gdx_layout.py --bake
"""

from __future__ import annotations

import json
import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]
MENU_SRC = ROOT / "src/main/resources/assets/sprites/menu"
MENU_DST = MENU_SRC / "1x"
LOGO_SRC = ROOT / "src/main/resources/assets/sprites"

VIRTUAL_W, VIRTUAL_H = 480, 360
PIXEL_SCALE = 3

# Синхронизировано с MenuLayout / Swing MainMenuScreen
SIGN_W_RATIO = 0.45
BUTTON_W_RATIO = 0.62
INNER_LOGO_W_OF_SIGN = 0.70
CURSOR_W = 28
BUTTON_COLS, BUTTON_ROWS = 3, 3
TITLE_LOGO_COLS, TITLE_LOGO_ROWS = 2, 3


def content_bounds(img: Image.Image) -> tuple[int, int, int, int]:
    rgba = img.convert("RGBA")
    w, h = rgba.size
    min_x, min_y, max_x, max_y = w, h, -1, -1
    for y in range(h):
        for x in range(w):
            r, g, b, a = rgba.getpixel((x, y))
            if a <= 4:
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


def remove_near_black(img: Image.Image) -> Image.Image:
    rgba = img.convert("RGBA")
    px = rgba.load()
    w, h = rgba.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = px[x, y]
            if a == 0 or (r < 18 and g < 18 and b < 18):
                px[x, y] = (0, 0, 0, 0)
    return rgba


def trim_transparent(img: Image.Image) -> Image.Image:
    box = content_bounds(img)
    return img.crop((box[0], box[1], box[0] + box[2], box[1] + box[3]))


def crisp_resize(img: Image.Image, dst_w: int, dst_h: int) -> Image.Image:
    work = img.convert("RGBA")
    while work.width > dst_w * 2 and work.height > dst_h * 2:
        work = work.resize((work.width // 2, work.height // 2), Image.NEAREST)
    return work.resize((max(1, dst_w), max(1, dst_h)), Image.NEAREST)


def cover_size(src_w: int, src_h: int, view_w: int, view_h: int) -> tuple[int, int]:
    scale = max(view_w / src_w, view_h / src_h)
    return round(src_w * scale), round(src_h * scale)


def save_png(img: Image.Image, path: Path) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    if path.suffix.lower() in {".jpg", ".jpeg"}:
        img.convert("RGB").save(path, optimize=True, quality=92)
    else:
        img.save(path, optimize=True)


def bake_background(manifest: list) -> None:
    src = MENU_SRC / "menu_bg_custom.jpg"
    if not src.is_file():
        print("  SKIP menu_bg_custom.jpg")
        return
    img = Image.open(src)
    vw, vh = VIRTUAL_W * PIXEL_SCALE, VIRTUAL_H * PIXEL_SCALE
    bw, bh = cover_size(img.width, img.height, vw, vh)
    out = crisp_resize(img.convert("RGB"), bw, bh)
    rel = "menu_bg_custom.jpg"
    out_path = MENU_DST / rel
    save_png(out, out_path)
    manifest.append({"output": f"1x/{rel}", "size": [bw, bh], "virtualCover": [vw, vh]})
    print(f"  OK {rel}: -> {bw}x{bh}")


def bake_trimmed(name: str, dst_w: int, dst_h: int, manifest: list, src_dir: Path | None = None) -> None:
    base = src_dir or MENU_SRC
    src = base / name
    if not src.is_file():
        print(f"  SKIP {name}")
        return
    img = trim_transparent(remove_near_black(Image.open(src)))
    out = crisp_resize(img, dst_w, dst_h)
    out_path = MENU_DST / name
    save_png(out, out_path)
    manifest.append({"output": f"1x/{name}", "size": [dst_w, dst_h]})
    print(f"  OK {name}: trim {img.size} -> {dst_w}x{dst_h}")


def bake_button_sheet(manifest: list) -> float:
    src_path = MENU_SRC / "menu_buttons_sheet.png"
    if not src_path.is_file():
        print("  SKIP menu_buttons_sheet.png")
        return 1.9
    src = remove_near_black(Image.open(src_path))
    fw = src.width // BUTTON_COLS
    fh = src.height // BUTTON_ROWS
    frames: list[Image.Image] = []
    for r in range(BUTTON_ROWS):
        for c in range(BUTTON_COLS):
            cell = src.crop((c * fw, r * fh, (c + 1) * fw, (r + 1) * fh))
            frames.append(trim_transparent(cell))
    aspect = frames[0].width / max(1, frames[0].height)
    plank_w = round(VIRTUAL_W * BUTTON_W_RATIO * PIXEL_SCALE)
    plank_h = max(1, round(plank_w / aspect))
    out_dir = MENU_DST / "buttons"
    out_dir.mkdir(parents=True, exist_ok=True)
    for i, frame in enumerate(frames):
        row, col = divmod(i, BUTTON_COLS)
        out = crisp_resize(frame, plank_w, plank_h)
        name = f"btn_{row}_{col}.png"
        save_png(out, out_dir / name)
        manifest.append({"output": f"1x/buttons/{name}", "size": [plank_w, plank_h], "row": row, "col": col})
    print(f"  OK menu_buttons_sheet: {BUTTON_ROWS}x{BUTTON_COLS} -> {plank_w}x{plank_h} (aspect {aspect:.3f})")
    return aspect


def bake_title_logo(manifest: list) -> None:
    src_path = LOGO_SRC / "witcher_logo_new.png"
    if not src_path.is_file():
        print("  SKIP witcher_logo_new.png")
        return
    src = remove_near_black(Image.open(src_path))
    fw = src.width // TITLE_LOGO_COLS
    fh = src.height // TITLE_LOGO_ROWS
    frame = trim_transparent(src.crop((0, 0, fw, fh)))
    sign_w = round(VIRTUAL_W * SIGN_W_RATIO * PIXEL_SCALE)
    logo_w = round(sign_w * INNER_LOGO_W_OF_SIGN)
    logo_h = max(1, round(logo_w * frame.height / max(1, frame.width)))
    out = crisp_resize(frame, logo_w, logo_h)
    rel = "witcher_logo_frame.png"
    save_png(out, MENU_DST / rel)
    manifest.append({"output": f"1x/{rel}", "size": [logo_w, logo_h]})
    print(f"  OK witcher_logo frame: -> {logo_w}x{logo_h}")


def main() -> int:
    print(f"Меню: {MENU_SRC} -> {MENU_DST}\n")
    manifest: list = []
    sign_w = round(VIRTUAL_W * SIGN_W_RATIO * PIXEL_SCALE)

    bake_background(manifest)
    bake_button_sheet(manifest)

    sign_src = MENU_SRC / "menu_logo_sign.png"
    sign_h = sign_w
    if sign_src.is_file():
        trimmed = trim_transparent(remove_near_black(Image.open(sign_src)))
        sign_h = max(1, round(sign_w * trimmed.height / max(1, trimmed.width)))
    bake_trimmed("menu_logo_sign.png", sign_w, sign_h, manifest)

    bake_trimmed("menu_cursor.png", CURSOR_W * PIXEL_SCALE,
                  max(1, round(CURSOR_W * PIXEL_SCALE * 1.2)), manifest)
    bake_title_logo(manifest)

    meta = {
        "virtualResolution": [VIRTUAL_W, VIRTUAL_H],
        "pixelScale": PIXEL_SCALE,
        "signDrawSize": [sign_w, sign_h],
        "assets": manifest,
    }
    meta_path = MENU_DST / "manifest.json"
    meta_path.write_text(json.dumps(meta, indent=2) + "\n", encoding="utf-8")
    print(f"\nOK manifest: {meta_path.relative_to(ROOT)}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
