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

        new DialogEntry("Незнакомое существо",
            "*хриплый смех* Вас-то я и ждал, Геральт из Ривии.\nЗдешний замок вам не кажется подозрительным?\n*незнакомец устремляет свой взгляд на Арнскрон*",
            IntroTheme.strangerRgb(), "geralt", "stranger", "right"),

        new DialogEntry("Геральт",
            "...Ага, и вы тоже.",
            IntroTheme.geraltRgb(), "geralt", "stranger", "left"),

        new DialogEntry("Герцог",
            "Я вас ждал, господин из Ривии.\nПростите манеры — зовите меня Герцог. Я скромный торговец.\nБроня, зелья, клинки — чего пожелаете, обеспечу.",
            IntroTheme.dukeRgb(), "geralt", "duke", "right"),

        new DialogEntry(null,
            "*Из стены замка начинает вырастать\nнастоящий торговый прилавок...*",
            IntroTheme.narratorRgb(), "geralt", "duke", "none"),

        new DialogEntry("Геральт",
            "...Сгенерировал?",
            IntroTheme.geraltRgb(), "geralt", "duke", "left"),

        new DialogEntry("Герцог",
            "Ха-ха… Это называется ассортимент.\nВыбирайте с умом — покупатель всегда прав.",
            IntroTheme.dukeRgb(), "geralt", "duke", "right"),

        new DialogEntry("Геральт",
            "Хмм. Я бы попросил лучше спирт.",
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
