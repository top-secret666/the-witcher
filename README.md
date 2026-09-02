# The Witcher — Chapter 1 Prototype

Pixel-art Java prototype: shop loop, wolf boss, VN dialogues.

## Download (Windows)

### [Download ready-to-play build (.ZIP)](https://github.com/top-secret666/the-witcher/releases/download/v1.0.0/The-Witcher-v1.0.0-Windows.zip)

_Unpack the archive and run `The Witcher.exe`. Java is bundled — no install needed._  
Keep `The Witcher.exe`, `app/`, and `runtime/` in the same folder.

## Run from source (developers)

Sources live under **`dev/`**. Build scripts (`.ps1`, `.bat`) are local-only and not tracked in git.

1. Install **JDK 17**.
2. Build and publish a local Windows bundle:
   ```powershell
   powershell -ExecutionPolicy Bypass -File dev\tools\package-windows.ps1
   ```
3. Create a release ZIP:
   ```powershell
   python dev\tools\make_release_zip.py --version 1.0.0
   ```
4. Upload `dist/The-Witcher-v1.0.0-Windows.zip` to [GitHub Releases](https://github.com/top-secret666/the-witcher/releases).

> **Do not commit** `The Witcher.exe`, `app/`, or `runtime/` to git — ship binaries via Releases only.

## Docs

- `docs/chapter1_journey_checklist.md` — canonical playthrough path
- `docs/design/` — chapter design notes
