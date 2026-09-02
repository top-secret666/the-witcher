#!/usr/bin/env python3
"""Make near-black portrait backgrounds transparent and trim huge shop portraits."""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[2]
ASSETS = ROOT / "dev" / "src" / "main" / "resources" / "assets" / "sprites"

PORTRAITS = [
    ASSETS / "lavka" / "duke_portrait_shop.png",
    ASSETS / "lavka" / "duke_portrait_fun_shop.png",
    ASSETS / "lavka" / "1x" / "duke_portrait_shop.png",
    ASSETS / "lavka" / "1x" / "duke_portrait_fun_shop.png",
    ASSETS / "screen saver" / "duke_portrait.png",
    ASSETS / "screen saver" / "duke_portrait_fun.png",
    ASSETS / "chapter1" / "battle" / "boss_duke_portrait.png",
]

MAX_HEIGHT = 720
BLACK_THRESHOLD = 28


def process(path: Path) -> None:
    if not path.is_file():
        return
    img = Image.open(path).convert("RGBA")
    if img.height > MAX_HEIGHT:
        scale = MAX_HEIGHT / img.height
        img = img.resize((max(1, int(img.width * scale)), MAX_HEIGHT), Image.Resampling.LANCZOS)
    pixels = img.load()
    w, h = img.size
    for y in range(h):
        for x in range(w):
            r, g, b, a = pixels[x, y]
            if a < 8:
                continue
            if r <= BLACK_THRESHOLD and g <= BLACK_THRESHOLD and b <= BLACK_THRESHOLD:
                pixels[x, y] = (0, 0, 0, 0)
    path.parent.mkdir(parents=True, exist_ok=True)
    img.save(path, format="PNG", optimize=True)
    print(f"Fixed alpha: {path.relative_to(ROOT)}")


def main() -> int:
    for portrait in PORTRAITS:
        process(portrait)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
