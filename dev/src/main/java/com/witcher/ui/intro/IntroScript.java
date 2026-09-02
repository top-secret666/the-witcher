package main.java.com.witcher.ui.intro;

import java.util.List;

/**
 * Сценарий диалогов интро — 9 реплик визуальной новеллы.
 */
public final class IntroScript {

    public static final int SHOP_ANIMATION_ENTRY_INDEX = 5;

    public record DialogEntry(
        String speaker,
        String text,
        int speakerColorRgb,
        String leftChar,
        String rightChar,
        String activeSide
    ) {}

    private static final List<DialogEntry> ENTRIES = List.of(
        new DialogEntry(null,
            "*Ветер завывает между древних стен Каэр Морхена.\nГеральт неспешно поднимается по разрушенной лестнице...",
            IntroTheme.narratorRgb(), "none", "none", "none"),

        new DialogEntry(null,
            "*Внезапно, прямо из стены замка вырастает\nужасающая фигура в богато украшенном камзоле...*",
            IntroTheme.narratorRgb(), "geralt", "stranger", "right"),

        new DialogEntry("Герцог",
            "*хрипло смеётся*\nВас-то я и ждал, Геральт из Ривии.\n"
                + "Любопытно... человек с вашей славой всё же переступил этот порог.\n"
                + "Не находите этот замок... подозрительным?\n"
                + "*Незнакомец переводит взгляд на Арнскрон.*",
            IntroTheme.strangerRgb(), "geralt", "stranger", "right"),

        new DialogEntry("Геральт",
            "Ага.\nИ ты тоже.",
            IntroTheme.geraltRgb(), "geralt", "stranger", "left"),

        new DialogEntry("Герцог",
            "Хо-хо-хо...\nПростите мои манеры.\n"
                + "Зовите меня Герцог. Всего лишь скромный торговец.\n"
                + "Броня. Клинки. Зелья. Всё, что может понадобиться ведьмаку...\n"
                + "уже ждёт своего хозяина.",
            IntroTheme.dukeRgb(), "geralt", "duke", "right"),

        new DialogEntry(null,
            "*Из стены начинает медленно вырастать\nтяжёлый дубовый прилавок...*",
            IntroTheme.narratorRgb(), "geralt", "duke", "none"),

        new DialogEntry("Геральт",
            "...Отрендерил?",
            IntroTheme.geraltRgb(), "geralt", "duke", "left"),

        new DialogEntry("Герцог",
            "Забавное слово.\nНет, господин из Ривии. Я ничего не создаю.\n"
                + "Я лишь открываю доступ к тому, что уже существует...\n"
                + "где-то по ту сторону.\n"
                + "Люди вашего времени, кажется, называют это генерацией.\n"
                + "Я предпочитаю считать это... хорошей торговлей.",
            IntroTheme.dukeRgb(), "geralt", "duke", "right"),

        new DialogEntry("Геральт",
            "Удобная философия.\n\n"
                + "Обычно торговцы сначала спрашивают, что нужно покупателю.\n\n"
                + "Тогда я начну со спирта.",
            IntroTheme.geraltRgb(), "geralt", "duke", "left")
    );

    private IntroScript() {
    }

    public static List<DialogEntry> entries() {
        return ENTRIES;
    }

    public static int entryCount() {
        return ENTRIES.size();
    }
}
