#!/usr/bin/env python3
"""Replace a release asset (delete same-named file, then upload)."""
from __future__ import annotations

import json
import subprocess
import sys
import urllib.request
from pathlib import Path

REPO = "top-secret666/the-witcher"


def get_token() -> str:
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
    raise SystemExit("No GitHub token")


def api(method: str, url: str, token: str, data: bytes | None = None, content_type: str | None = None) -> dict | None:
    headers = {
        "Authorization": f"Bearer {token}",
        "Accept": "application/vnd.github+json",
        "User-Agent": "witcher-release-uploader",
    }
    if content_type:
        headers["Content-Type"] = content_type
        headers["Content-Length"] = str(len(data or b""))
    req = urllib.request.Request(url, data=data, method=method, headers=headers)
    with urllib.request.urlopen(req, timeout=600) as resp:
        body = resp.read()
        return json.loads(body) if body else None


def main() -> int:
    version = sys.argv[1] if len(sys.argv) > 1 else "1.0.0"
    root = Path(__file__).resolve().parents[2]
    zip_path = root / "dist" / f"The-Witcher-v{version}-Windows.zip"
    tag = f"v{version}"
    if not zip_path.is_file():
        print(f"Missing {zip_path}", file=sys.stderr)
        return 1

    token = get_token()
    release = api("GET", f"https://api.github.com/repos/{REPO}/releases/tags/{tag}", token)
    for asset in release.get("assets", []):
        if asset["name"] == zip_path.name:
            api("DELETE", f"https://api.github.com/repos/{REPO}/releases/assets/{asset['id']}", token)
            print(f"Deleted old asset: {asset['name']}")

    upload_url = release["upload_url"].split("{", 1)[0] + f"?name={zip_path.name}"
    data = zip_path.read_bytes()
    asset = api("POST", upload_url, token, data=data, content_type="application/zip")
    print(release["html_url"])
    print(asset["browser_download_url"])
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
