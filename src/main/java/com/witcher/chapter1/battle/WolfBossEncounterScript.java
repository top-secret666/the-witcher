package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.intro.IntroTheme;

import java.util.List;

/**
 * Полный сценарий первого босса — осколок «Белый Волк».
 * Два выбора: доверие к Герцогу vs подозрение; истинный путь ведёт к глитч-катсцене.
 */
public final class WolfBossEncounterScript {

  /** Серебристо-стальной — голос собственной памяти. */
  public static final int WOLF_RGB = IntroTheme.geraltRgb();

  public enum Branch {
    NONE,
    /** Поторопиться — плохая развилка, без глитч-пробуждения. */
    HURRY,
    /** Вслушаться — истинный путь к осколку. */
    LISTEN
  }

  /** После этой реплики — выбор №1. */
  public static final int CHOICE_GATE_INDEX = 2;

  private WolfBossEncounterScript() {
  }

  public static List<BossEncounterScript.DialogEntry> introLines() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Герцог",
            "А, этот… Даже не тратьте на него время.\n"
                + "Мелкая формальность — Белый Волк. Минуем и идём дальше,\n"
                + "в замке заждались.",
            IntroTheme.dukeRgb(),
            BossEncounterScript.Expression.MAP),
        new BossEncounterScript.DialogEntry(
            null,
            "*Сквозь туман проступает силуэт в потёртой шкурянке.\n"
                + "Медальон на груди дрожит — ты не помнишь, когда надел его.*",
            IntroTheme.narratorRgb(),
            BossEncounterScript.Expression.MAP),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Ты меня не помнишь?",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED)
    );
  }

  public static VnSceneState firstChoiceScene() {
    return new VnSceneState(
        "Геральт",
        "Герцог торопит. Волк смотрит так, будто видел меня до петли.",
        List.of(
            new VnChoice("hurry", "Поторопиться, как советует Герцог", 0, 1),
            new VnChoice("listen", "Остановиться и вслушаться", 1, 0)
        ));
  }

  public static List<BossEncounterScript.DialogEntry> continuation(Branch branch) {
    return switch (branch) {
      case HURRY -> hurryLines();
      case LISTEN -> listenLines();
      case NONE -> List.of();
    };
  }

  private static List<BossEncounterScript.DialogEntry> hurryLines() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Герцог",
            "Вот и правильно. Нечего слушать всякий сброд из чужих снов.",
            IntroTheme.dukeRgb(),
            BossEncounterScript.Expression.INTERESTED),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Как скажешь…",
            WOLF_RGB,
            BossEncounterScript.Expression.MAP),
        new BossEncounterScript.DialogEntry(
            null,
            "*Фигура тускнеет, будто ей нечего удерживать.\n"
                + "Лес снова пахнет смолой — как в лавке Герцога.*",
            IntroTheme.narratorRgb(),
            BossEncounterScript.Expression.MAP),
        new BossEncounterScript.DialogEntry(
            "Герцог",
            "Видите? Просто шум в голове уставшего наёмника.\n"
                + "Идём. У меня для вас новые перчатки.",
            IntroTheme.dukeRgb(),
            BossEncounterScript.Expression.LUNGE)
    );
  }

  private static List<BossEncounterScript.DialogEntry> listenLines() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Каэр Морхен. Снег на стенах, от которых пахнет старым железом.\n"
                + "Весемир кричал, что лошадь нельзя поить ледяной водой —\n"
                + "а ты всё равно улыбался, потому что знал: она переживёт.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Испытание травами. Когда мир выгорел в глазах —\n"
                + "ты не просил пощады. Ты просто стиснул зубы\n"
                + "и стал тем, кого зовут Геральт из Ривии.",
            WOLF_RGB,
            BossEncounterScript.Expression.LUNGE),
        new BossEncounterScript.DialogEntry(
            "Герцог",
            "Довольно. Сказки для детей и пьяных в трактирах.\n"
                + "Держитесь темы, Белый Волк — или исчезните окончательно.",
            IntroTheme.dukeRgb(),
            BossEncounterScript.Expression.ATTACK),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Ты помнишь, как держать сталь. Не против меня.\n"
                + "Против того, кто прячет тебя в этой лавке.",
            WOLF_RGB,
            BossEncounterScript.Expression.ATTACK),
        new BossEncounterScript.DialogEntry(
            null,
            "*Медальон вспыхивает холодом. На миг кажется,\n"
                + "что весь лес — не место, а чья-то чужая мысль.*",
            IntroTheme.narratorRgb(),
            BossEncounterScript.Expression.MAP)
    );
  }
}
