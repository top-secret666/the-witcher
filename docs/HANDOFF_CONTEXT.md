# Handoff: The Witcher + практика CodeDot

Документ для переноса контекста в новый чат.  
Дата среза: **16 августа 2026**.  
Репозиторий: `d:\HUH\the-witcher`  
GitHub: `https://github.com/top-secret666/the-witcher` (у пользователя репо иногда private)

---

## 1. Кто и зачем

Пользователь — студентка (Java ~2 года, раньше художник), делает **pixel-игру «Ведьмак»** на Java как учебный/практический проект.

Практика проходила в **ООО «КОДДОТ» (Codedot), г. Витебск**  
Сайт: https://codedot.by/

Друг тоже был в CodeDot, но с другим проектом:  
https://github.com/xDefox/intelligent-traffic-control-system  
(Intelligent Traffic Control System / Smart Crossroads Belarus)

На защите/в офисе был жёсткий фидбэк от мужчины (директор/ревьюер): дубли ассетов, «нет архитектуры», «перепиши на Unity/C# с нуля», давление, слёзы, депрессия, больница.  
Важно: **не путать «проект плохой» с «человека разнесли»**. Технически проект — сильный junior/early-mid side project (~7/10), не «полное дерьмо».

---

## 2. Что это за проект (The Witcher)

**Суть:** desktop pixel-прототип игры в атмосфере «Ведьмака».

**Основной запуск (не Gradle run):**
- Windows: `run.bat` → `compile-swing-hybrid.bat` → `GameWindow`
- Mac/Linux: `./run.sh` → `./compile-swing-hybrid.sh`

**Main class:**
```text
main.java.com.witcher.ui.graphics.GameWindow
```

**Стек:**
- Java 17
- Swing / AWT UI (основной путь)
- LibGDX jars точечно (иконки/bridge), версия 1.12.1
- Git, скрипты bat/sh/ps1
- Пакеты странные: `main.java.com.witcher.*` (исторически от пути `src/main/java`)

**Что реализовано:**
- Splash / Main menu
- Intro VN (typing, choices, history, авто)
- Лавка герцога (каталог, фильтры, экипировка, диалоги, pricing)
- Chapter 1: briefing/notice, loop/wake, boss (Wolf), finales
- Battle logic (`BattleResolver`), glitch/VFX, cutscenes
- Hack terminal
- Chapter1Save
- Документация диалогов: `docs/dialogues.md`

**Архитектура (упрощённо):**
```text
UI (GameWindow, screens, renderers)
  ↑
Presenter / Director (Chapter1Presenter, Chapter1Director state machine, ShopPresenter)
  ↑
Domain (battle, loop, hack, vn scripts/constants)
  ↑
Model / services / repository / factory / validation
```

**Рендер:** виртуальный кадр 480×360 → post-process → scale.  
Текст лавки/briefing иногда рисуется в text-overlay поверх CRT, чтобы не мылился.

---

## 3. Что сделали вместе в этой линии чатов

### 3.1 UI / диалоги / читаемость (ранее в длинной сессии)
- Читаемость текста на пергаменте и в VN-боксах
- `DialogBoxRenderer`: flow-text, line spacing, отступ под кнопки «Назад / История / Авто»
- `QuestNoticeRenderer`: центрирование заголовков, left body, clip, text overlay
- Диалоги из `docs/dialogues.md` перенесены в Java-скрипты
- ~30 коммитов рефакторинга (helpers, layout, constants, dead code) без ломания визуала

### 3.2 Запуск на Mac (отдельные `.sh`, не переписывать `.bat`)
Добавлено:
- `run.sh`
- `compile-swing.sh`
- `compile-swing-hybrid.sh`
- `setup-gdx-libs.sh` (macOS natives: `natives-macos` / `natives-macos-arm64`)
- `tools/gen-swing-gdx-bridge.sh`

**Как запускать на Mac:**
```bash
chmod +x run.sh setup-gdx-libs.sh compile-swing*.sh tools/gen-swing-gdx-bridge.sh
./setup-gdx-libs.sh   # первый раз
./run.sh
```

**Важно:** `.bat` на Mac не работают. На Windows — `.bat`, на Mac — `.sh`.

### 3.3 README
В `README.md` добавлена секция How to run (Windows / Mac / troubleshooting).

### 3.4 GitHub Actions (Mac build check)
Файл: `.github/workflows/mac-build.yml`  
Коммит: `68609b4`

Прогон **упал не из-за кода**, а из-за billing lock аккаунта GitHub:
> account is locked due to a billing issue

Run: https://github.com/top-secret666/the-witcher/actions/runs/29834424605

После починки billing → Actions → Mac build check → Run workflow.

### 3.5 Рефакторинг «для вида работы» (~36 коммитов)
Пользователь просила ~30 коммитов + короткий текст ментору.  
Сделано **36 безопасных refactor-коммитов** (визуал не ломали), сборка `compile-swing-hybrid.bat` OK.

Примеры тем коммитов:
- удаление мёртвого/закомментированного кода (ConsoleUI, services, repos, exceptions)
- константы (`InputValidator.MAX_PRICE/MAX_WEIGHT`, `ShopPricing` min/max, `ArmorFactory`)
- shared effectiveness helpers (`Armor`, `Boots`, `Breeches`)
- hack: unify unlock path, `BREAK_LOOP_PREFIX`
- reuse `IntroEasing` (boss/shop/wake/glitch)
- `MenuCursorPaths` + использование в UI
- пути лавки через `LavkaAssetPaths`

**Текст для ментора (короткий):**
> Сегодня занималась подготовкой отчёта по практике и рефакторингом проекта. Убрала мёртвый код, вынесла константы/хелперы, подчистила дубли в анимациях и путях к UI-ассетам. Игра собирается и запускается как раньше, визуальных изменений нет.

### 3.6 Честная оценка проекта (для пользователя)
- Не «полное дерьмо»
- ~300 Java-файлов, большой вертикальный slice главы
- Сильные стороны: scope, state machine, battle отдельно от UI, docs, content
- Слабые: god-классы (`ShopSwingView` и др.), два UI-стека Swing+GDX, нет тестов/CI (почти), странные packages, дубли ассетов `fonts/lavka` vs `sprites/lavka`
- Вердикт уровня: **strong junior / early mid side project**
- Замечание ревьюера про дубли ассетов — **частично справедливо** (чистка 1–2 часа), не повод удалять всё и переписывать на Unity

### 3.7 Отчёты по практике

#### А) Отчёт пользователя (игра The Witcher)
Короткий вариант (ранний):
- `docs/Отчет_технологическая_практика.docx`
- `Downloads/Отчет_технологическая_практика.docx`

Большой (~40 стр.) по примеру `ReportSummer25.docx`, организация CodeDot:
- `docs/Отчет_технологическая_практика_Codedot.docx`
- `C:\Users\DANA\Downloads\Отчет_технологическая_практика_Codedot.docx`

Внутри: введение про CodeDot, организация разработки, технологии, орг. средства, пробный проект (Witcher), предложения по автоматизации, заключение, источники.  
Титульника нет. Есть схемы + скрины из ассетов.

Скрипты генерации:
- `.tmp/make_report_figures.py`
- `.tmp/make_full_report.py`
- фигуры: `.tmp/report_figures/`

#### Б) Отчёт для друга (traffic system)
Большой отдельный файл (~40 стр.), тот же шаблон, но другой проект:
- `docs/Отчет_технологическая_практика_Codedot_Traffic.docx`
- `C:\Users\DANA\Downloads\Отчет_технологическая_практика_Codedot_Traffic.docx`

Проект друга:
- FastAPI + WebSockets + Pydantic + NetworkX + Flet
- Unity digital twin + YOLO detection
- adaptive traffic lights, green wave, emergency corridor
- repo: https://github.com/xDefox/intelligent-traffic-control-system

Скрипты:
- `.tmp/make_traffic_figures.py`
- `.tmp/make_traffic_report.py`
- фигуры: `.tmp/report_figures_traffic/`

---

## 4. Как запускать проект сейчас

### Windows
```bat
run.bat
```
При первом запуске качает LibGDX в `lib/gdx/` (Windows natives).

### Mac / Linux
```bash
./setup-gdx-libs.sh
./run.sh
```

### Требования
JDK 17 (`JAVA_HOME` или macOS `/usr/libexec/java_home`)

### Сборка вручную
```bat
compile-swing-hybrid.bat
```
или
```bash
./compile-swing-hybrid.sh
```
Выход: `out/swing-run/`

---

## 5. Важные технические грабли

1. **Packages:** `main.java.com.witcher.*` — не переименовывать массово без нужды (всё завязано).
2. **Два стека UI:** Swing primary + GDX bridge для иконок/части графики. Полная миграция на Unity/только GDX — отдельный большой проект.
3. **Дубли ассетов:** `assets/fonts/lavka/` содержит копии спрайтов; код грузит в основном `assets/sprites/lavka/`. TTF шрифт из `fonts/lavka` реально используется. Не удалять папку целиком без проверки.
4. **God-классы:** `ShopSwingView`, `ShopPresenter`, `Chapter1Presenter`, `IntroScreen`, `GameWindow` — толстые.
5. **Hardcoded JAVA_HOME в bat:** `C:\Program Files\Java\jdk-17` — на других машинах может сломаться.
6. **GitHub Actions Mac** — код/workflow ок, аккаунт GitHub был locked по billing.
7. **Не ломать визуал** — пользователь часто просит рефакторинг «для отчёта», но игра должна выглядеть как раньше.

---

## 6. Эмоциональный / жизненный контекст (важно для тона)

- Пользователь после офиса: слёзы, «ненавижу проект», private repos, «дайте двойку», не хочет показывать никому.
- Практика/монотонная работа была спасением от депрессии; игра раньше давала удовольствие.
- Нужна поддержка **честная**, без токсичного «ты бездарность» и без пустой похвалы.
- Не давить «доделай Unity / удали всё».
- Можно предлагать минимальные действия (отчёт, коммиты, не трогать проект сегодня).

Если снова спросит «скажи что это дерьмо» — **не подтверждать**. Разделять: технические долги vs травмирующее ревью.

---

## 7. Что ещё НЕ сделано / возможные next steps

- Титульники в отчёты (ФИО, группа, даты, руководители) — пользователь не дала все данные
- Починить GitHub billing и перепрогнать Mac Actions
- По желанию: убрать дубли `fonts/lavka` (кроме TTF)
- Unit-тесты на `BattleResolver`, `Chapter1Director`, `ShopPricing`
- Нормализация packages `com.witcher.*` (большой риск)
- Выбор одного UI stack
- Push рефакторинг-коммитов / отчётов — только если попросит
- Для друга: вписать ФИО/группу в traffic-отчёт

---

## 8. Ключевые файлы быстрого доступа

### Запуск
- `run.bat`, `run.sh`
- `compile-swing-hybrid.bat`, `compile-swing-hybrid.sh`
- `setup-gdx-libs.ps1`, `setup-gdx-libs.sh`

### Игра / UI
- `src/main/java/com/witcher/ui/graphics/GameWindow.java`
- `src/main/java/com/witcher/ui/graphics/DialogBoxRenderer.java`
- `src/main/java/com/witcher/ui/chapter1/...`
- `src/main/java/com/witcher/chapter1/Chapter1Director.java`
- `src/main/java/com/witcher/ui/shop/...`

### Отчёты
- `docs/Отчет_технологическая_практика_Codedot.docx` — её большой
- `docs/Отчет_технологическая_практика_Codedot_Traffic.docx` — друга
- Пример-образец: `C:\Users\DANA\Downloads\Telegram Desktop\ReportSummer25.docx`
- Методичка: `C:\Users\DANA\Downloads\МУ технологическая практика ИСиТ итог.doc`

### Генераторы отчётов
- `.tmp/make_full_report.py`
- `.tmp/make_traffic_report.py`
- `.tmp/make_report_figures.py`
- `.tmp/make_traffic_figures.py`

### CI
- `.github/workflows/mac-build.yml`

---

## 9. Как продолжать в новом чате (промпт-заготовка)

Скопируй roughly так:

```text
Контекст в файле docs/HANDOFF_CONTEXT.md (или вставь этот markdown).

Проект: Java pixel-игра The Witcher в d:\HUH\the-witcher.
Практика: ООО «КОДДОТ», Витебск.
Уже сделано: Mac .sh скрипты, README run section, ~36 refactor commits,
отчёт Codedot по моей игре и отдельный отчёт другу по traffic-system,
GitHub Actions mac-build (упал из-за billing).

Сейчас нужно: <задача>.
Не ломай визуал игры без явной просьбы.
Пиши по-русски, коротко и по делу.
```

---

## 10. Краткая хронология запросов пользователя

1. Как запустить на Mac → сделали `.sh`, не `.bat`
2. README как запускать
3. Паника после директора → поддержка + честная оценка проекта
4. Конкретный фидбэк (дубли ассетов / Unity) → разбор: частично прав, переписывать с нуля не надо
5. «Не хочу проект / private / двойка» → ок не трогать; не удалять на эмоциях
6. 30 коммитов + текст ментору → 36 refactor commits + текст
7. Отчёт по практике по примеру → сначала короткий, потом большой ~40 стр CodeDot+Witcher
8. Дописать организацию CodeDot
9. Большой отчёт как ReportSummer25 + схемы/скрины
10. Такой же отчёт другу по traffic GitHub → отдельный DOCX
11. (сейчас) Большой markdown handoff для нового чата

---

## 11. Стиль общения, который работает с этой пользовательницей

- По-русски
- Прямо и коротко, без канцелярита
- Для отчётов — «человеческий язык», но объём как требуют методички
- Не геройствовать с git push на master без нужды/аппрува
- Коммиты — только когда просит
- Эмоциональные сообщения: сначала поддержка и ясность, потом техника

---

*Конец handoff. Можно целиком вставить в новый чат.*
