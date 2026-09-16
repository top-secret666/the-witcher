<h1 align="center">THE WITCHER</h1>
<h3 align="center">Chapter 1 — Pixel Prototype</h3>

<p align="center">
  <strong>Visual novel · horror</strong><br>
  Java · Swing · pixel-art · MVP
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square" alt="Java 17" />
  <img src="https://img.shields.io/badge/Platform-Windows-0078D6?style=flat-square" alt="Windows" />
  <img src="https://img.shields.io/badge/Status-Alpha_v1.1.0-8B0000?style=flat-square" alt="Alpha" />
</p>

<p align="center">
<img width="640" height="480" alt="geralt-drink" src="https://github.com/user-attachments/assets/be2aaf6a-840e-4a8c-ba27-4b0acbca3519" />
</p>

## Download & play

### [⬇ Download Windows (.ZIP) — v1.1.0](https://github.com/top-secret666/the-witcher/releases/download/v1.1.0/The-Witcher-v1.1.0-Windows.zip)

| Step | What to do |
|:---:|:------------|
| **1** | Download the ZIP using the link above |
| **2** | Extract the archive |
| **3** | Open the `The-Witcher` folder and run **`The Witcher.exe`** |

You do **not** need to install Java separately — the runtime is bundled.  
Keep these together: `The Witcher.exe`, `app/`, `runtime/`.

If the direct link fails, the file is on [**Releases**](https://github.com/top-secret666/the-witcher/releases).

---

## About

This is my first serious pet project and an attempt to build my own engine.

## Architecture

```
View (Swing)  →  Presenter  →  Chapter1Director  →  Domain  →  Model
```

| Layer | Role |
|:-----|:-----|
| **UI** | `dev/src/.../ui/` — frame, input, assets |
| **Presenter** | Connects the screen to chapter phases |
| **Director** | Phases and transitions |
| **Domain** | Combat, loop, shop, VN |

Canonical playthrough: [`docs/chapter1_journey_checklist.md`](docs/chapter1_journey_checklist.md)

---

## Repository layout

```
the-witcher/
├── README.md
├── docs/                 design + chapter canon (presentable only)
└── dev/                  source + packaging tools
    └── src/              game code
```

Game media, build overlay, and Windows binaries are **not** in git.  
Playable build: [**GitHub Releases**](https://github.com/top-secret666/the-witcher/releases).

---

## For developers

**Requires:** JDK 17, Windows. Game assets and `dev/tools/release-overlay/` stay on your machine (gitignored).

```powershell
powershell -ExecutionPolicy Bypass -File dev\tools\package-exe.ps1
python dev\tools\make_release_zip.py --version 1.1.0
```
---

## Docs

| File | Description |
|:-----|:---------|
| [`chapter1_journey_checklist.md`](docs/chapter1_journey_checklist.md) | Chapter 1 canon |
| [`glava1_scenariy_volk.md`](docs/glava1_scenariy_volk.md) | Wolf scenario |
| [`glava1_lor.md`](docs/glava1_lor.md) | Lore notes |
| [`design/`](docs/design/) | Loop design |

---

<p align="center">
  <sub>
    Pet project · Dana Stukalova · VGTU · 2025–2026<br>
    <a href="https://github.com/top-secret666/the-witcher/releases">Releases</a>
    ·
    <a href="docs/chapter1_journey_checklist.md">Playthrough</a>
  </sub>
</p>
