<p align="center">
  <img src="docs/media/gameplay.gif" alt="The Witcher — Chapter 1 gameplay preview" width="720" />
</p>

<p align="center">
  <em>↑ Сюда вставь свою GIF: положи файл <code>docs/media/gameplay.gif</code> и закоммить</em>
</p>

<br>

<h1 align="center">⚔️ THE WITCHER</h1>
<h3 align="center">Chapter 1 — Pixel Prototype</h3>

<p align="center">
  <strong>Лавка герцога · петля времени · первый босс — Волк</strong><br>
  Java · Swing · pixel-art · MVP-архитектура
</p>

<p align="center">
  <a href="https://github.com/top-secret666/the-witcher/releases/download/v1.0.0/The-Witcher-v1.0.0-Windows.zip">
    <img src="https://img.shields.io/badge/⬇_СКАЧАТЬ_ИГРУ-Windows_ZIP-8B0000?style=for-the-badge&labelColor=1a1208&color=C9A227" alt="Download Windows ZIP" />
  </a>
  &nbsp;
  <a href="https://github.com/top-secret666/the-witcher/releases">
    <img src="https://img.shields.io/badge/Все_релизы-GitHub_Releases-2d2d2d?style=for-the-badge&labelColor=1a1208&color=4a4a4a" alt="All releases" />
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-17-orange?style=flat-square" alt="Java 17" />
  <img src="https://img.shields.io/badge/Platform-Windows-0078D6?style=flat-square" alt="Windows" />
  <img src="https://img.shields.io/badge/Status-Alpha_v1.0.0-8B0000?style=flat-square" alt="Alpha" />
  <img src="https://img.shields.io/badge/License-Educational-grey?style=flat-square" alt="Educational" />
</p>

---

## 🕹️ Скачать и запустить

### [👉 СКАЧАТЬ ГОТОВУЮ ИГРУ ДЛЯ WINDOWS (.ZIP)](https://github.com/top-secret666/the-witcher/releases/download/v1.0.0/The-Witcher-v1.0.0-Windows.zip)

| Шаг | Действие |
|:---:|:---|
| **1** | Скачай архив по ссылке выше |
| **2** | Распакуй ZIP в любую папку |
| **3** | Запусти **`The Witcher.exe`** внутри папки `The-Witcher` |

> **Java ставить не нужно** — в сборку уже вложен runtime.  
> Не переноси `The Witcher.exe` отдельно от папок `app/` и `runtime/`.

<details>
<summary><strong>Если ссылка не открывается</strong></summary>

Перейди на страницу [**Releases**](https://github.com/top-secret666/the-witcher/releases) и скачай  
`The-Witcher-v1.0.0-Windows.zip` вручную.

</details>

---

## 📜 О проекте

> *«Снова лавка. Снова герцог. Снова ты — без памяти и без выхода… пока не разберёшься с Волком.»*

Pet-project в духе **The Witcher**: интерактивная **глава 1** с магазином брони, визуальными новеллами, терминалом-хаком и боем с первым боссом.

**Что внутри прототипа:**

- 🏪 **Лавка** — покупка экипировки, анимации, каталог, кошелёк
- 🔁 **Петля** — сброс витка, пробуждение, мета-прогресс
- 🐺 **Босс «Волк»** — брифинг → лес → финал → глитч-итог
- 💬 **VN-диалоги** — реплики герцога, выборы, кат-сцены
- ⌨️ **Терминал** — скрытая механика доступа к карте боссов

---

## 🏗️ Архитектура

```
View (Swing)  →  Presenter  →  Chapter1Director  →  Domain  →  Model
```

| Слой | Роль |
|:-----|:-----|
| **UI** | `dev/src/.../ui/` — отрисовка, ввод, ассеты |
| **Presenter** | Связка экрана и логики главы |
| **Director** | Фазы главы, переходы, save/load |
| **Domain** | Бой, петля, магазин, VN-правила |

Канонический путь прохождения: [`docs/chapter1_journey_checklist.md`](docs/chapter1_journey_checklist.md)

---

## 📁 Структура репозитория

```
the-witcher/
├── README.md              ← ты здесь
├── docs/                  ← дизайн, диалоги, чеклист главы
│   └── media/
│       └── gameplay.gif   ← превью для README (добавь сама)
└── dev/                   ← исходный код и Gradle
    ├── src/
    ├── core/
    └── tools/
```

> **В git нет `.exe` и сборок** — только исходники.  
> Бинарники распространяются через [**GitHub Releases**](https://github.com/top-secret666/the-witcher/releases): так принято в gamedev, и git не раздувается.

---

## 🛠️ Для разработчиков

**Требования:** JDK 17, Windows (для сборки клиента)

```powershell
# Собрать локальную Windows-сборку
powershell -ExecutionPolicy Bypass -File dev\tools\package-windows.ps1

# Упаковать ZIP для нового релиза
python dev\tools\make_release_zip.py --version 1.0.0
```

Загрузи `dist/The-Witcher-v1.0.0-Windows.zip` в [Releases](https://github.com/top-secret666/the-witcher/releases/new) — **не коммить** `The Witcher.exe`, `app/`, `runtime/` в репозиторий.

---

## 📚 Документация

| Файл | Описание |
|:-----|:---------|
| [`chapter1_journey_checklist.md`](docs/chapter1_journey_checklist.md) | Канон прохождения главы 1 |
| [`glava1_scenariy_volk.md`](docs/glava1_scenariy_volk.md) | Сценарий босса Волк |
| [`dialogues.md`](docs/dialogues.md) | Диалоги |
| [`design/`](docs/design/) | Заметки по дизайну петли |

---

<p align="center">
  <sub>
    Pet-project · Стукалова Дана · VGTU · 2025–2026<br>
    <a href="https://github.com/top-secret666/the-witcher/releases">Releases</a>
    ·
    <a href="docs/chapter1_journey_checklist.md">Playthrough guide</a>
  </sub>
</p>
