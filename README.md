![The-Witcher-3-The-Witcher-gif-Pixel-Gif-6434293](https://github.com/user-attachments/assets/e81d2ffd-a08c-44b2-ab65-f323ce33c989)


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

**Requirements:** JDK 17

### Windows

```bat
run.bat
```

При первом запуске скрипт сам скачает LibGDX в `lib/gdx/` и соберёт проект в `out/swing-run/`.

Отдельно, если нужно только скачать библиотеки или собрать без запуска:

```bat
setup-gdx-libs.ps1
compile-swing-hybrid.bat
```

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

