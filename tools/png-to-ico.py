"""Convert PNG to multi-size ICO for jpackage on Windows."""
from __future__ import annotations

import sys
from pathlib import Path

from PIL import Image


def main() -> int:
    if len(sys.argv) != 3:
        print("Usage: png-to-ico.py <input.png> <output.ico>", file=sys.stderr)
        return 1

    src = Path(sys.argv[1])
    dst = Path(sys.argv[2])
    if not src.is_file():
        print(f"Missing input: {src}", file=sys.stderr)
        return 1

    img = Image.open(src).convert("RGBA")
    sizes = [(256, 256), (128, 128), (64, 64), (48, 48), (32, 32), (16, 16)]
    frames = [img.resize(size, Image.Resampling.NEAREST) for size in sizes]
    dst.parent.mkdir(parents=True, exist_ok=True)
    frames[0].save(dst, format="ICO", sizes=sizes)
    print(f"Wrote {dst}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
