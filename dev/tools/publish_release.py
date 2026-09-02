#!/usr/bin/env python3
"""Upload dist/The-Witcher-vX-Windows.zip to GitHub Releases via gh CLI."""
from __future__ import annotations

import argparse
import shutil
import subprocess
import sys
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--version", default="1.0.0")
    parser.add_argument(
        "--root",
        type=Path,
        default=Path(__file__).resolve().parents[2],
    )
    args = parser.parse_args()

    root = args.root.resolve()
    tag = f"v{args.version}"
    zip_path = root / "dist" / f"The-Witcher-v{args.version}-Windows.zip"
    if not zip_path.is_file():
        print(f"Missing {zip_path}. Build the ZIP first.", file=sys.stderr)
        return 1

    gh = shutil.which("gh")
    if not gh:
        print("Install GitHub CLI (gh) and run: gh auth login", file=sys.stderr)
        return 1

    cmd = [
        gh,
        "release",
        "create",
        tag,
        str(zip_path),
        "--title",
        f"The Witcher Core Engine - Alpha {tag}",
        "--notes",
        "Windows portable build. Extract the ZIP and run The Witcher.exe "
        "inside the The-Witcher folder (or START GAME.bat). Java not required.",
    ]
    subprocess.run(cmd, check=True)
    print(f"https://github.com/top-secret666/the-witcher/releases/tag/{tag}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
