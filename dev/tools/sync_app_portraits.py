#!/usr/bin/env python3
"""Copy duke portrait edits from packaged app/assets back into dev resources."""
from __future__ import annotations

import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
APP = ROOT / "app" / "assets" / "sprites"
DEV = ROOT / "dev" / "src" / "main" / "resources" / "assets" / "sprites"

PAIRS = [
    (APP / "lavka" / "duke_portrait_shop.png", DEV / "lavka" / "duke_portrait_shop.png"),
    (APP / "lavka" / "duke_portrait_fun_shop.png", DEV / "lavka" / "duke_portrait_fun_shop.png"),
    (APP / "lavka" / "1x" / "duke_portrait_shop.png", DEV / "lavka" / "1x" / "duke_portrait_shop.png"),
    (APP / "lavka" / "1x" / "duke_portrait_fun_shop.png", DEV / "lavka" / "1x" / "duke_portrait_fun_shop.png"),
    (APP / "screen saver" / "duke_portrait.png", DEV / "screen saver" / "duke_portrait.png"),
    (APP / "screen saver" / "duke_portrait_fun.png", DEV / "screen saver" / "duke_portrait_fun.png"),
    (APP / "chapter1" / "battle" / "boss_duke_portrait.png", DEV / "chapter1" / "battle" / "boss_duke_portrait.png"),
]


def main() -> int:
    copied = 0
    for src, dst in PAIRS:
        if not src.is_file():
            continue
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        copied += 1
        print(f"Synced {src.relative_to(ROOT)} -> {dst.relative_to(ROOT)}")
    if copied == 0:
        print("No app portrait overrides found; using dev resources as-is.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
