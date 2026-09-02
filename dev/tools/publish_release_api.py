#!/usr/bin/env python3
"""Create GitHub Release and upload Windows ZIP using git credentials."""
from __future__ import annotations

import json
import os
import subprocess
import sys
import urllib.error
import urllib.request
from pathlib import Path

REPO = "top-secret666/the-witcher"
API = f"https://api.github.com/repos/{REPO}/releases"


def get_token() -> str:
    for key in ("GITHUB_TOKEN", "GH_TOKEN"):
        value = os.environ.get(key)
        if value:
            return value
    proc = subprocess.run(
        ["git", "credential", "fill"],
        input="protocol=https\nhost=github.com\n\n",
        capture_output=True,
        text=True,
        check=True,
    )
    for line in proc.stdout.splitlines():
        if line.startswith("password="):
            return line.split("=", 1)[1]
    raise SystemExit("No GitHub token found. Run: gh auth login")


def api_request(method: str, url: str, token: str, data: dict | None = None) -> dict:
    body = None if data is None else json.dumps(data).encode("utf-8")
    req = urllib.request.Request(
        url,
        data=body,
        method=method,
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/json",
            "User-Agent": "witcher-release-uploader",
        },
    )
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))


def upload_asset(upload_url: str, token: str, zip_path: Path) -> dict:
    name = zip_path.name
    url = f"{upload_url}?name={name}"
    data = zip_path.read_bytes()
    req = urllib.request.Request(
        url,
        data=data,
        method="POST",
        headers={
            "Authorization": f"Bearer {token}",
            "Accept": "application/vnd.github+json",
            "Content-Type": "application/zip",
            "Content-Length": str(len(data)),
            "User-Agent": "witcher-release-uploader",
        },
    )
    with urllib.request.urlopen(req, timeout=600) as resp:
        return json.loads(resp.read().decode("utf-8"))


def main() -> int:
    version = sys.argv[1] if len(sys.argv) > 1 else "1.0.0"
    root = Path(__file__).resolve().parents[2]
    zip_path = root / "dist" / f"The-Witcher-v{version}-Windows.zip"
    tag = f"v{version}"

    if not zip_path.is_file():
        print(f"Missing {zip_path}", file=sys.stderr)
        return 1

    token = get_token()
    payload = {
        "tag_name": tag,
        "name": f"The Witcher Core Engine - Alpha {tag}",
        "body": (
            "Windows portable build.\n\n"
            "1. Extract the ZIP\n"
            "2. Open the `The-Witcher` folder\n"
            "3. Run `The Witcher.exe`\n\n"
            "Java is not required."
        ),
        "draft": False,
        "make_latest": True,
    }

    try:
        release = api_request("POST", API, token, payload)
    except urllib.error.HTTPError as err:
        if err.code != 422:
            raise
        release = api_request(
            "GET",
            f"https://api.github.com/repos/{REPO}/releases/tags/{tag}",
            token,
        )
        print(f"Release {tag} already exists, uploading asset…")

    upload_url = release["upload_url"].split("{", 1)[0]
    asset = upload_asset(upload_url, token, zip_path)
    print(release["html_url"])
    print(asset["browser_download_url"])
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
