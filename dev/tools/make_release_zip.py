#!/usr/bin/env python3
"""Pack the local Windows build into a release ZIP for GitHub Releases."""
from __future__ import annotations

import argparse
import shutil
import sys
import tempfile
import zipfile
from pathlib import Path


REQUIRED = ("The Witcher.exe", "app", "runtime")


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--root",
        type=Path,
        default=Path(__file__).resolve().parents[2],
        help="Repository root with The Witcher.exe, app/, runtime/",
    )
    parser.add_argument(
        "--version",
        default="1.0.0",
        help="Release version label, e.g. 1.0.0",
    )
    parser.add_argument(
        "--out-dir",
        type=Path,
        default=None,
        help="Output directory (default: <root>/dist)",
    )
    return parser.parse_args()


def ensure_build(root: Path) -> None:
    missing = [name for name in REQUIRED if not (root / name).exists()]
    if missing:
        raise SystemExit(
            "Missing build artifacts: "
            + ", ".join(missing)
            + "\nRun dev/tools/package-windows.ps1 first, then retry."
        )


def make_zip(root: Path, version: str, out_dir: Path) -> Path:
    out_dir.mkdir(parents=True, exist_ok=True)
    archive = out_dir / f"The-Witcher-v{version}-Windows.zip"
    if archive.exists():
        archive.unlink()

    staging = Path(tempfile.mkdtemp(prefix="witcher-release-"))
    try:
        bundle = staging / "The-Witcher"
        bundle.mkdir(parents=True)

        for name in REQUIRED:
            src = root / name
            dst = bundle / name
            if src.is_dir():
                shutil.copytree(src, dst)
            else:
                shutil.copy2(src, dst)

        start_bat = bundle / "START GAME.bat"
        start_bat.write_text(
            "@echo off\r\n"
            "cd /d \"%~dp0\"\r\n"
            "if not exist \"The Witcher.exe\" (\r\n"
            "  echo Oshibka: net The Witcher.exe v etoj papke.\r\n"
            "  pause\r\n"
            "  exit /b 1\r\n"
            ")\r\n"
            "if not exist \"app\\assets\" (\r\n"
            "  echo Oshibka: net papki app\\assets. Raspakujte arhiv polnostju.\r\n"
            "  pause\r\n"
            "  exit /b 1\r\n"
            ")\r\n"
            "start \"\" \"The Witcher.exe\"\r\n",
            encoding="ascii",
        )

        with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as zf:
            for path in bundle.rglob("*"):
                if path.is_file():
                    zf.write(path, path.relative_to(staging).as_posix())
    finally:
        shutil.rmtree(staging, ignore_errors=True)
    size_mb = archive.stat().st_size / (1024 * 1024)
    print(f"Created {archive} ({size_mb:.1f} MB)")
    return archive


def main() -> int:
    args = parse_args()
    root = args.root.resolve()
    out_dir = (args.out_dir or root / "dist").resolve()
    ensure_build(root)
    make_zip(root, args.version, out_dir)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
