<h1 align="center">THE WITCHER</h1>
<h3 align="center">Chapter 1 — Pixel Prototype</h3>

<p align="center">
  <strong>Визуальная новелла · хоррор </strong><br>
  Java · Swing · pixel-art · MVP
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square" alt="Java 17" />
  <img src="https://img.shields.io/badge/Platform-Windows-0078D6?style=flat-square" alt="Windows" />
  <img src="https://img.shields.io/badge/Status-Alpha_v1.1.0-8B0000?style=flat-square" alt="Alpha" />
</p>

<p align="center">
<img width="1024" height="1024" alt="image (2)" src="https://github.com/user-attachments/assets/365cae5f-282a-4b30-8ad3-1ddf1f9756cc" />
</p>

## Скачать и играть

### [⬇ Скачать Windows-сборку (Releases → v1.1.0)](https://github.com/top-secret666/the-witcher/releases/tag/v1.1.0)

На странице релиза скачай файл **`The-Witcher-v1.1.0-Windows.zip`**.

| Шаг | Что сделать |
|:---:|:------------|
| **1** | Открой ссылку выше |
| **2** | Скачай **`The-Witcher-v1.1.0-Windows.zip`** |
| **3** | Распакуй → папка `The-Witcher` → запусти **`The Witcher.exe`** |

Java отдельно ставить **не нужно** — рантайм внутри сборки.  
Держи рядом: `The Witcher.exe`, `app/`, `runtime/`.

Все версии: [**Releases**](https://github.com/top-secret666/the-witcher/releases).

---

## О проекте

Первый серьёзный pet-project и попытка написать свой движок.

## Архитектура

```
View (Swing)  →  Presenter  →  Chapter1Director  →  Domain  →  Model
```

| Слой | Роль |
|:-----|:-----|
| **UI** | `dev/src/.../ui/` — кадр, ввод, ассеты |
| **Presenter** | Связка экрана с фазами главы |
| **Director** | Фазы и переходы |
| **Domain** | Бой, петля, лавка, VN |

Канон прохождения: [`docs/chapter1_journey_checklist.md`](docs/chapter1_journey_checklist.md)

---

## Структура репозитория

```
the-witcher/
├── README.md
├── docs/                 дизайн, диалоги, чеклист
│   └── media/            картинки для README
└── dev/                  исходники + tools (сборка exe)
    ├── src/              игра
    └── tools/            package-exe, release zip/upload
```

Бинарники **не** в git — только через [**GitHub Releases**](https://github.com/top-secret666/the-witcher/releases).

---

## Для разработчиков

**Нужно:** JDK 17, Windows

```powershell
powershell -ExecutionPolicy Bypass -File dev\tools\package-exe.ps1
python dev\tools\make_release_zip.py --version 1.1.0
python dev\tools\publish_release_api.py 1.1.0
```
---

## Документы

| Файл | Описание |
|:-----|:---------|
| [`chapter1_journey_checklist.md`](docs/chapter1_journey_checklist.md) | Канон главы 1 |
| [`glava1_scenariy_volk.md`](docs/glava1_scenariy_volk.md) | Сценарий Волка |
| [`dialogues.md`](docs/dialogues.md) | Черновик диалогов |
| [`design/`](docs/design/) | Дизайн петли |

---

<p align="center">
  <sub>
    Pet project · Dana Stukalova · VGTU · 2025–2026<br>
    <a href="https://github.com/top-secret666/the-witcher/releases">Releases</a>
    ·
    <a href="docs/chapter1_journey_checklist.md">Playthrough</a>
  </sub>
</p>
