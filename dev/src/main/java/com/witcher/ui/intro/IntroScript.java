package main.java.com.witcher.ui.intro;

import java.util.List;

/**
 * Сценарий диалогов интро. С индекса {@link #SHOP_ANIMATION_ENTRY_INDEX}
 * идёт materialize лавки + новые реплики у прилавка.
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
            "*Ветер завывает между древних стен старого разрушенного замка.\n"
                + "Геральт неспешно поднимается по лестнице в поисках временного убежища\n"
                + "в этом заброшенном месте...*",
            IntroTheme.narratorRgb(), "none", "none", "none"),

        new DialogEntry(null,
            "*Внезапно, прямо из угла коридора появляется\n"
                + "ужасающая фигура в богато украшенном камзоле...*",
            IntroTheme.narratorRgb(), "geralt", "stranger", "right"),

        new DialogEntry("Герцог",
            "*хриплый смех*\n"
                + "Я ждал вас, господин Геральт.\n"
                + "Здешний замок вам не кажется подозрительным?\n"
                + "*Незнакомец переводит взгляд на Арнскрон.*",
            IntroTheme.strangerRgb(), "geralt", "stranger", "right"),

        new DialogEntry("Геральт",
            "Откуда ты знаешь мое имя?",
            IntroTheme.geraltRgb(), "geralt", "stranger", "left"),

        new DialogEntry("Герцог",
            "ХО-ХО-ХО-ХА...\nНет, нет, я всего лишь скромный торговец...\n"
                + "О вас наслышаны все, кто хоть что-нибудь собой представляет.\n"
                + "Простите, я не представился. Зовите меня Герцогом.\n"
                + "Приступим к делу. Броня, кирасы, шлемы, наколенники...\n"
                + "Обеспечу вас всем, чего пожелаете...",
            IntroTheme.dukeRgb(), "geralt", "duke", "right"),

        // SHOP_ANIMATION_ENTRY_INDEX — materialize GIF, диалог скрыт до конца анимации.
        new DialogEntry(null,
            "*Прилавок материализуется.*",
            IntroTheme.narratorRgb(), "geralt", "duke", "none"),

        new DialogEntry("Геральт",
            "Что это сейчас было?\nКто ты вообще такой?",
            IntroTheme.geraltRgb(), "geralt", "duke", "left"),

        new DialogEntry("Герцог",
            "На этот вопрос я не могу дать точного ответа *ухмылка*\n"
                + "Могу лишь сказать: Добро пожаловать в мою скромную лавку...",
            IntroTheme.dukeRgb(), "geralt", "duke", "right"),

        new DialogEntry("Геральт",
            "Лучше бы это была корчма...",
            IntroTheme.geraltRgb(), "geralt", "duke", "left"),

        new DialogEntry("Герцог",
            "Ах, да-да.\nЖелаете что-нибудь приобрести?",
            IntroTheme.dukeRgb(), "geralt", "duke", "right")
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
