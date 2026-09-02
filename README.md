# The Witcher — Core Logic Engine ⚔️ (Java)

```text
┌──────────────────────────────────────────────────────────────────┐
│  QUEST: THE WITCHER                                               │
│  LOOT : OOP • GAME LOGIC • CLEAN STRUCTURE                        │
└──────────────────────────────────────────────────────────────────┘
```

Educational Java project focused on **game logic** built with clean **object-oriented design**.

##  Goal

Demonstrate OOP in a real-ish domain:
- modeling entities
- interactions and rules
- game state transitions

##  Tech

```text
Language: Java
Focus   : OOP (encapsulation, inheritance, polymorphism)
```

## What’s implemented

- Core entities and relationships
- Modular logic for interactions
- Rule-driven flow for game state

## How to run

**Requirements:** JDK 17 (only for building; the packaged `.exe` bundles a runtime)

### Windows — portable exe (recommended)

```powershell
powershell -ExecutionPolicy Bypass -File tools\package-windows.ps1
```

Launch:

```text
release\The Witcher\The Witcher.exe
```

### Windows — from source (dev)

```powershell
powershell -ExecutionPolicy Bypass -File tools\compile-swing.ps1
```

Then run the main class `main.java.com.witcher.ui.graphics.GameWindow` with classpath `out\swing-run` + `lib\gdx\*.jar`.

Local `.bat` scripts are kept on disk for convenience but are not tracked in git.

### macOS / Linux

```bash
chmod +x run.sh setup-gdx-libs.sh
./setup-gdx-libs.sh   # первый раз — скачает LibGDX + natives под вашу ОС
./run.sh              # собрать и запустить
```

На Apple Silicon подтянутся `natives-macos-arm64`, на Intel Mac — `natives-macos`.

### Main class

`main.java.com.witcher.ui.graphics.GameWindow`

### Troubleshooting

| Проблема | Что сделать |
|----------|-------------|
| `JDK 17 not found` | Установите JDK 17 и задайте `JAVA_HOME` |
| `lib/gdx is empty` | Запустите `setup-gdx-libs.ps1` (Windows) или `./setup-gdx-libs.sh` (Mac/Linux) |
| На Mac не работает `run.bat` | Используйте `./run.sh` — `.bat` только для Windows |

##  Repo quality checklist (recommended)

- Remove `.idea/` and `*.iml` from git tracking
- Add IDE files to `.gitignore`
- Add a short “Architecture” section (2–3 bullets) once you pick a structure

