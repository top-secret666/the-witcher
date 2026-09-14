# ЛАБОРАТОРНАЯ РАБОТА № 2
## Модульное тестирование, использование JUnit

**Проект (тема диплома):** THE WITCHER — интерактивная глава 1 «Петля герцога»  
**Стек:** Java 17, JUnit 5 (код тестов в отчёте; в `src/test` пока **не** внедряем)  
**Дата:** 14.09.2026

---

## Цель

Познакомиться с модульным тестированием — наиболее ранним видом тестирования, которое выполняется на стадии кодирования самим разработчиком — и написать первые простые JUnit-тесты для методов нашего проекта.

В этой лабораторной тесты оформлены **как учебный код в markdown**. В репозиторий (`dev/src/test/...`) их пока не кладём: сначала разбираем классы эквивалентности и граничные значения, потом отдельно подключим JUnit к сборке.

---

# 1. Краткие теоретические сведения

**Модульное тестирование** (unit testing, юнит-тестирование) — вид тестирования, направленный на оценку корректности исходного кода. Модульные тесты покрывают **атомарные** участки: отдельные методы и взаимодействие небольших объектов. Их пишут рядом с кодом, без запуска всего окна игры.

**Преимущества модульных тестов**

- повышают качество архитектуры (метод, который нельзя вызвать без Swing, — сигнал, что логику стоит отделить);
- стимулируют писать простые методы;
- поощряют изменения: рефакторинг не ломает поведение «втихую»;
- упрощают интеграцию;
- помогают документировать код («как этим методом пользоваться»);
- минимизируют зависимости (тест не должен поднимать лавку целиком);
- создаются на бесплатных фреймворках, есть для многих языков.

В работе используем **JUnit 5** — фреймворк модульного тестирования для Java (`org.junit.jupiter.api`). Типичные инструменты: `@Test`, `assertEquals`, `assertTrue` / `assertFalse`, `assertThrows`, `assertDoesNotThrow`.

Модульные тесты **не** заменяют чек-лист и тест-кейсы из лабораторной № 1: юнит проверяет метод в изоляции, системный тест — путь игрока по экранам.

---

# 2. Что тестируем в нашем проекте

В методичке на первом этапе берут готовый `calculator-1.0.jar`, на втором — один метод с **двумя параметрами**, классы эквивалентности и границы.

У нас аналог калькулятора — домен главы 1 **без Swing**. Для первых тестов выбраны методы, которые:

- уже есть в коде;
- детерминированы (без `ThreadLocalRandom`, без окна);
- имеют понятный оракул (исключение / `true` / `false`).

| Этап | Метод | Зачем |
|---|---|---|
| 1. Самые простые | `InputValidator.validatePrice(int)` | один параметр, как `isPositive` в таблице вариантов |
| 1. Игровая логика | `Chapter1Session.prisonBlocksVictory()` | порог «Плен ≥ 6» из дизайн-дока |
| 2. Обязательный (2 параметра) | `InputValidator.validatePriceRange(int min, int max)` | 4 класса эквивалентности + 5 границ, как на рисунке 1 |

Константы из кода:

```text
InputValidator.MIN_PRICE = 10
InputValidator.MAX_PRICE = 1_000_000
Chapter1Session.PRISON_COUNTER_THRESHOLD = 6
```

Правила (как в исходниках):

```text
validatePrice(price)
  бросает InvalidPriceException, если price < 10 или price > 1_000_000

validatePriceRange(min, max)
  бросает InvalidPriceRangeException, если
    min < 10  ИЛИ  max > 1_000_000  ИЛИ  min >= max

prisonBlocksVictory()
  true, если prison >= 6
```

`addPrison(amount)` увеличивает плен **только при amount > 0** — это тоже граничный факт.

---

# 3. Этап 1. Первые несложные тесты

Идея JUnit: один тестовый метод = одна проверка (или тесно связанная группа). Имя метода читается как спецификация.

Ниже — учебный класс. Пакет тестовый, прод-код импортируем как есть (`main.java.com.witcher...` — так сейчас устроен проект).

```java
package com.witcher.lab2;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.exception.InvalidPriceException;
import main.java.com.witcher.validation.InputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FirstUnitTests {

    private InputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InputValidator();
    }

    // --- validatePrice: один параметр ---

    @Test
    void validatePrice_typicalValid_doesNotThrow() {
        assertDoesNotThrow(() -> validator.validatePrice(420));
    }

    @Test
    void validatePrice_tooSmall_throwsInvalidPrice() {
        assertThrows(InvalidPriceException.class, () -> validator.validatePrice(9));
    }

    @Test
    void validatePrice_tooLarge_throwsInvalidPrice() {
        assertThrows(InvalidPriceException.class,
                () -> validator.validatePrice(1_000_001));
    }

    // --- Плен: игровая граница из лабораторной № 1 ---

    @Test
    void prison_belowThreshold_doesNotBlockVictory() {
        Chapter1Session session = Chapter1Session.newGame();
        session.addPrison(5);
        assertFalse(session.prisonBlocksVictory());
    }

    @Test
    void prison_atThreshold_blocksVictory() {
        Chapter1Session session = Chapter1Session.newGame();
        session.addPrison(6);
        assertTrue(session.prisonBlocksVictory());
    }

    @Test
    void addPrison_zeroOrNegative_doesNotChangePrison() {
        Chapter1Session session = Chapter1Session.newGame();
        session.addPrison(0);
        session.addPrison(-3);
        assertFalse(session.prisonBlocksVictory());
        assertEquals(0, session.prison());
    }
}
```

**Что здесь показано**

| Аннотация / проверка | Смысл |
|---|---|
| `@BeforeEach` | новый валидатор перед каждым тестом — тесты независимы |
| `assertDoesNotThrow` | корректная цена 420 (стартовый кошелёк лавки) проходит |
| `assertThrows` | некорректный ввод даёт **конкретный** тип исключения |
| `assertTrue` / `assertFalse` | булев оракул порога плена |

420 — не случайное число: столько крон выдаёт лавка. 5 и 6 — граница «плен блокирует победу». Это уже классы эквивалентности, только в простом виде.

---

# 4. Этап 2. Классы эквивалентности и граничные значения

Метод варианта (аналог `sum` / `div` из таблицы 1 методички):

```java
public void validatePriceRange(int minPrice, int maxPrice)
```

Два параметра. По методичке для такого метода нужны **четыре** теста на классы эквивалентности и **пять** — на границы.

## 4.1. Классы эквивалентности

**Класс эквивалентности** — набор входных данных, на которых метод ведёт себя одинаково. Достаточно **одного представителя** класса: если 50…200 валидны, нет смысла гонять 51…201, 52…202 и т.д.

Условия валидности независимы, поэтому плоскость `(minPrice, maxPrice)` делим на четыре области — как четыре четверти на рисунке 1 методички, только оси не «знак числа», а «выполняется ли своё ограничение».

```text
                    maxPrice
                         ^
                         |
      EC3                |              EC1
      min валиден        |              min валиден
      max > MAX          |              max валиден
                         |              min < max
    ---------------------+------------------------→ minPrice
                         | 10
      EC2                |
      min < MIN          |              EC4
      max валиден        |              min и max «в диапазоне»,
                         |              но min >= max
```

Рисунок 1 (адаптация). Четыре класса эквивалентности для `validatePriceRange`.

| Класс | Условие | Представитель | Ожидание |
|---|---|---|---|
| EC1 | min ≥ 10, max ≤ 1_000_000, min < max | `(50, 200)` | исключения нет |
| EC2 | min < 10, max в диапазоне | `(5, 200)` | `InvalidPriceRangeException` |
| EC3 | min в диапазоне, max > 1_000_000 | `(50, 2_000_000)` | `InvalidPriceRangeException` |
| EC4 | оба в диапазоне, но min ≥ max | `(300, 100)` | `InvalidPriceRangeException` |

Четыре представителя → четыре тестовых метода. Внутри класса поведение считается одинаковым.

## 4.2. Граничные значения

**Граничное значение** — точка на стыке двух классов. Ошибки чаще сидят на равенстве (`<` vs `<=`), поэтому проверяем **саму границу и соседей**.

Для нашего метода три границы:

1. нижняя по `min`: `MIN_PRICE = 10`;
2. верхняя по `max`: `MAX_PRICE = 1_000_000`;
3. отношение параметров: `min < max` (на диагонали `min == max` уже отказ).

Пять тестовых методов (как требует методичка):

| № | Граница | Вход | Класс | Ожидание |
|---|---|---|---|---|
| B1 | min = 10 (на границе, валид) | `(10, 100)` | EC1 | нет исключения |
| B2 | min = 9 (шаг за границу) | `(9, 100)` | EC2 | исключение |
| B3 | max = 1_000_000 (на границе, валид) | `(10, 1_000_000)` | EC1 | нет исключения |
| B4 | max = 1_000_001 (шаг за границу) | `(10, 1_000_001)` | EC3 | исключение |
| B5 | min = max (грань min < max) | `(100, 100)` | EC4 | исключение |

Сосед справа от B5, уже валидный: `(100, 101)` — это тот же EC1, отдельный тест не обязателен, но полезен при живом прогоне.

## 4.3. Код тестов этапа 2

```java
package com.witcher.lab2;

import main.java.com.witcher.exception.InvalidPriceRangeException;
import main.java.com.witcher.validation.InputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidatePriceRangeTest {

    private InputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new InputValidator();
    }

    @Nested
    @DisplayName("Классы эквивалентности (4 теста)")
    class EquivalenceClasses {

        @Test
        void ec1_validRange_doesNotThrow() {
            assertDoesNotThrow(() -> validator.validatePriceRange(50, 200));
        }

        @Test
        void ec2_minBelowMinPrice_throws() {
            assertThrows(InvalidPriceRangeException.class,
                    () -> validator.validatePriceRange(5, 200));
        }

        @Test
        void ec3_maxAboveMaxPrice_throws() {
            assertThrows(InvalidPriceRangeException.class,
                    () -> validator.validatePriceRange(50, 2_000_000));
        }

        @Test
        void ec4_minNotLessThanMax_throws() {
            assertThrows(InvalidPriceRangeException.class,
                    () -> validator.validatePriceRange(300, 100));
        }
    }

    @Nested
    @DisplayName("Граничные значения (5 тестов)")
    class Boundaries {

        @Test
        void b1_minEqualsMinPrice_valid() {
            assertDoesNotThrow(() -> validator.validatePriceRange(10, 100));
        }

        @Test
        void b2_minJustBelowMinPrice_invalid() {
            assertThrows(InvalidPriceRangeException.class,
                    () -> validator.validatePriceRange(9, 100));
        }

        @Test
        void b3_maxEqualsMaxPrice_valid() {
            assertDoesNotThrow(() -> validator.validatePriceRange(10, 1_000_000));
        }

        @Test
        void b4_maxJustAboveMaxPrice_invalid() {
            assertThrows(InvalidPriceRangeException.class,
                    () -> validator.validatePriceRange(10, 1_000_001));
        }

        @Test
        void b5_minEqualsMax_invalid() {
            assertThrows(InvalidPriceRangeException.class,
                    () -> validator.validatePriceRange(100, 100));
        }
    }
}
```

`@Nested` — только группировка в отчёте JUnit, на логику не влияет. Можно писать девять плоских `@Test`.

---

# 5. Сводная таблица прогона (для отчёта)

Статус при живом прогоне: `[ ]` не гоняли · `[x]` прошёл · `[!]` упал.

| ID | Метод | Вход | Ожидание | Статус |
|---|---|---|---|---|
| P1 | `validatePrice` | 420 | нет исключения | `[ ]` |
| P2 | `validatePrice` | 9 | `InvalidPriceException` | `[ ]` |
| P3 | `validatePrice` | 1_000_001 | `InvalidPriceException` | `[ ]` |
| PR1 | `prisonBlocksVictory` | плен = 5 | `false` | `[ ]` |
| PR2 | `prisonBlocksVictory` | плен = 6 | `true` | `[ ]` |
| PR3 | `addPrison` | 0 и −3 | плен остаётся 0 | `[ ]` |
| EC1 | `validatePriceRange` | 50, 200 | нет исключения | `[ ]` |
| EC2 | `validatePriceRange` | 5, 200 | `InvalidPriceRangeException` | `[ ]` |
| EC3 | `validatePriceRange` | 50, 2_000_000 | `InvalidPriceRangeException` | `[ ]` |
| EC4 | `validatePriceRange` | 300, 100 | `InvalidPriceRangeException` | `[ ]` |
| B1 | `validatePriceRange` | 10, 100 | нет исключения | `[ ]` |
| B2 | `validatePriceRange` | 9, 100 | исключение | `[ ]` |
| B3 | `validatePriceRange` | 10, 1_000_000 | нет исключения | `[ ]` |
| B4 | `validatePriceRange` | 10, 1_000_001 | исключение | `[ ]` |
| B5 | `validatePriceRange` | 100, 100 | исключение | `[ ]` |

Когда тесты внедрим в проект, эту таблицу заполним по факту зелёного прогона.

---

# 6. Что сознательно не тестируем на этом шаге

- Swing-экраны, анимации, `ShopPresenter` — это не модуль, а система (лабораторная № 1).
- `BattleResolver.resolveRound` — внутри `ThreadLocalRandom` (бросок 1d6). Для юнита сначала нужно вынести кубик; иначе тест нестабилен.
- `ShopModel.canPurchase` — завязан на флаг `FREE_PURCHASES_FOR_TEST`. Пока он `true`, класс «не хватает крон» **недостижим**. Это как раз пример, зачем юниты: флаг сразу виден, когда пишешь тест на `trySpend`.

Следующие простые кандидаты (когда будем внедрять): `BattleResolver.detectCounter(defense, stamina)` — тоже два числа и три исхода (`TRUE_DAMAGE` / `STAMINA_DRAIN` / `STANDARD`) с границей `def > sta + 2`.

---

# Выводы

Модульный тест проверяет атомарный кусок кода (метод), а не сценарий игрока. Его пишет разработчик на этапе кодирования, обычно фреймворком вроде JUnit.

**Класс эквивалентности** — множество входов с одинаковым поведением. Для `validatePriceRange` все пары вроде `(50, 200)`, `(80, 900)`, `(12, 40)` лежат в одном классе EC1: обе границы соблюдены и min < max. Достаточно одного представителя. Пары `(5, 200)` и `(0, 80)` — другой класс (EC2): min меньше 10, метод всегда бросает исключение.

**Граничное значение** — вход на стыке классов. У `MIN_PRICE` граница равна 10: `(10, 100)` ещё валиден, `(9, 100)` уже нет. У отношения min и max граница — равенство: `(100, 100)` невалиден, потому что в коде стоит `min >= max`, а не только `min > max`. Именно такие «на единицу левее / правее» места обычно содержат дефект.

Юнит-тест фиксирует это ожиданием: `assertDoesNotThrow` на валидном представителе и `assertThrows` на невалидном. Так тест становится спецификацией метода и страховкой при рефакторинге лавки.

---

# Контрольные вопросы

**1. Что такое классы эквивалентности?**  
Наборы входных данных, на которых программа ведёт себя одинаково. Из каждого класса берут одного представителя. Пример из варианта: все диапазоны с `min ≥ 10`, `max ≤ 1_000_000`, `min < max` — один класс; представитель `(50, 200)`.

**2. Что такое модульные тесты?**  
Тесты, которые проверяют корректность небольших частей исходного кода (методов, классов) в изоляции, на стадии разработки, как правило с помощью фреймворка (у нас JUnit).

**3. Что проверяют модульные тесты?**  
Что атомарный участок делает то, что задумано: верный результат, верное исключение, верное изменение состояния объекта. Не проверяют «удобно ли кликать по кирасе» — это уже UI / системные тесты.

**4. Какие элементы подвергаются проверкам в рамках модульного тестирования?**  
Отдельные методы и взаимодействие близких объектов (например `Chapter1Session.addPrison` + `prisonBlocksVictory`). Не экраны, не сеть, не вся глава целиком.

---

## Источники

1. Методические указания к лабораторной работе № 2 «Модульное тестирование, использование JUnit».
2. Куликов С. С. *Тестирование программного обеспечения. Базовый курс* — классы эквивалентности и граничные значения.
3. Исходный код: `InputValidator`, `Chapter1Session`, `InvalidPriceException`, `InvalidPriceRangeException`.
4. Лабораторная № 1 — порог плена ≥ 6, кошелёк 420 крон.
