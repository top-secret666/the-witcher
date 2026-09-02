<h1 align="center">THE WITCHER</h1>
<h3 align="center">Chapter 1 — Pixel Prototype</h3>

<p align="center">
  <strong>Duke's shop · time loop · first boss — the Wolf</strong><br>
  Java · Swing · pixel-art · MVP architecture
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square" alt="Java 17" />
  <img src="https://img.shields.io/badge/Platform-Windows-0078D6?style=flat-square" alt="Windows" />
  <img src="https://img.shields.io/badge/Status-Alpha_v1.0.0-8B0000?style=flat-square" alt="Alpha" />
</p>

---

## Download & play

### [Download Windows build (.ZIP)](https://github.com/top-secret666/the-witcher/releases/latest/download/The-Witcher-v1.0.0-Windows.zip)

> If the link returns 404, open [**Releases**](https://github.com/top-secret666/the-witcher/releases) and download `The-Witcher-v1.0.0-Windows.zip` manually.

| Step | What to do |
|:----:|:-----------|
| **1** | Download the ZIP from the link above |
| **2** | Extract the archive |
| **3** | Open the `The-Witcher` folder and run **`The Witcher.exe`** (or `START GAME.bat`) |

No Java install required — the build bundles its own runtime.  
Keep `The Witcher.exe`, `app/`, and `runtime/` in the same folder.

If the direct link fails, grab the file from [**Releases**](https://github.com/top-secret666/the-witcher/releases).

---

## About

> *"The shop again. The duke again. You again — with no memory and no way out… until you face the Wolf."*

A **The Witcher**–inspired pet project: interactive **chapter 1** with an armour shop, visual-novel scenes, a hack terminal, and the first boss fight.

**What's in this prototype:**

- **Shop** — gear purchases, animations, catalogue, wallet
- **Loop** — cycle reset, awakening, meta progress
- **Wolf boss** — briefing → forest → finale → glitch ending
- **VN dialogues** — duke lines, choices, cutscenes
- **Terminal** — hidden path to the boss map

---

## Architecture

```
View (Swing)  →  Presenter  →  Chapter1Director  →  Domain  →  Model
```

| Layer | Role |
|:------|:-----|
| **UI** | `dev/src/.../ui/` — rendering, input, assets |
| **Presenter** | Binds screen logic to chapter flow |
| **Director** | Phases, transitions, save/load |
| **Domain** | Combat, loop, shop, VN rules |

Canonical playthrough: [`docs/chapter1_journey_checklist.md`](docs/chapter1_journey_checklist.md)

---

## Repository layout

```
the-witcher/
├── README.md
├── docs/              design notes, dialogues, playthrough checklist
│   └── media/         README artwork
└── dev/               source code & Gradle (LibGDX legacy)
```

Binaries are **not** stored in git — ship builds via [**GitHub Releases**](https://github.com/top-secret666/the-witcher/releases) only.

---

## For developers

**Requirements:** JDK 17, Windows (to build the client)

```powershell
powershell -ExecutionPolicy Bypass -File dev\tools\package-windows.ps1
python dev\tools\make_release_zip.py --version 1.0.0
python dev\tools\publish_release.py --version 1.0.0
```

Upload `dist/The-Witcher-v1.0.0-Windows.zip` to [Releases](https://github.com/top-secret666/the-witcher/releases/new).  
Do **not** commit `The Witcher.exe`, `app/`, or `runtime/`.

---

## Docs

| File | Description |
|:-----|:------------|
| [`chapter1_journey_checklist.md`](docs/chapter1_journey_checklist.md) | Chapter 1 canonical path |
| [`glava1_scenariy_volk.md`](docs/glava1_scenariy_volk.md) | Wolf boss script |
| [`dialogues.md`](docs/dialogues.md) | Dialogue draft |
| [`design/`](docs/design/) | Loop design notes |

---

<p align="center">
  <sub>
    Pet project · Dana Stukalova · VGTU · 2025–2026<br>
    <a href="https://github.com/top-secret666/the-witcher/releases">Releases</a>
    ·
    <a href="docs/chapter1_journey_checklist.md">Playthrough guide</a>
  </sub>
</p>
