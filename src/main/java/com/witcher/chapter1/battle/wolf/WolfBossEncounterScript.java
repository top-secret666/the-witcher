package main.java.com.witcher.chapter1.battle.wolf;

import main.java.com.witcher.chapter1.assets.Chapter1AssetPaths;
import main.java.com.witcher.chapter1.battle.encounter.BossEncounterScript;
import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.intro.IntroTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Лес: только Волк. Два выбора здесь (настроение задаётся в лавке), финальный — в {@link WolfBossFinaleScript}.
 */
public final class WolfBossEncounterScript {

  public static final int WOLF_RGB = IntroTheme.geraltRgb();

  public enum MemoryBranch {
    NONE,
    /** «Старые истории» — доверие лавке. */
    DISMISS,
    /** «Было по-настоящему» — подозрение. */
    ACKNOWLEDGE
  }

  /** После этой реплики — выбор №2 (последняя строка блока воспоминаний). */
  public static final int CHOICE_GATE_INDEX = 7;

  private WolfBossEncounterScript() {
  }

  public static List<BossEncounterScript.DialogEntry> introLines(Chapter1Session session) {
    List<BossEncounterScript.DialogEntry> lines = new ArrayList<>();
    lines.add(new BossEncounterScript.DialogEntry(
        null,
        "*Сквозь туман проступает силуэт в потёртой шкурянке.\n"
            + "Медальон на груди холодит кожу — ты не помнишь, когда надел его.*",
        IntroTheme.narratorRgb(),
        BossEncounterScript.Expression.MAP));
    lines.add(openingLine(session));
    lines.addAll(memoryLines());
    return lines;
  }

  private static BossEncounterScript.DialogEntry openingLine(Chapter1Session session) {
    boolean curious = session != null
        && session.wolfEntryMood() == Chapter1Session.WolfEntryMood.CURIOUS;
    String text = curious
        ? "Решил посмотреть, значит. Надо же — впервые за долгое время\n"
            + "не отмахнулся сразу."
        : "Быстро, значит. Ты всегда умел называть бегство делом —\n"
            + "«нет времени», «не сейчас». Другого раза не будет.\n"
            + "Их не бывает у таких, как мы.";
    return new BossEncounterScript.DialogEntry(
        "Волк", text, WOLF_RGB, BossEncounterScript.Expression.INTERESTED);
  }

  /** Шесть кликов воспоминания — фон меняется по backgroundImage на реплике. */
  private static List<BossEncounterScript.DialogEntry> memoryLines() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Ладно, к делу, пока меня не заела ностальгия.\n"
                + "Помнишь дорогу на Ард Каррайг...",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED,
            Chapter1AssetPaths.WOLF_PORTRAIT,
            false),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "...снег по колено, Плотва скользит и материт тебя всем,\n"
                + "чем умеет материть лошадь...",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED,
            Chapter1AssetPaths.MEMORY_ARD_CARRAIG,
            false),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "...а ты ей отвечаешь вслух. Взрослый мужик, разговаривающий\n"
                + "с лошадью о смысле жизни...",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED,
            Chapter1AssetPaths.MEMORY_ARD_CARRAIG,
            true),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "А потом Каэр Морхен, Весемир орёт на тебя за ледяную воду\n"
                + "для лошади...",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED,
            Chapter1AssetPaths.MEMORY_KAER_MORHEN,
            false),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "...и ты стоишь и лыбишься, как идиот, потому что после\n"
                + "стольких лет чужое ворчание всё ещё звучит как дом.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED,
            null,
            false),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Здесь так не бывает. Здесь никто на тебя не орёт по-настоящему...\n"
                + "а ты как будто и не замечаешь.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED,
            Chapter1AssetPaths.WOLF_PORTRAIT,
            false)
    );
  }

  public static VnSceneState memoryChoiceScene() {
    return new VnSceneState(
        "Геральт",
        "Волк говорит о дороге, которой здесь нет.",
        List.of(
            new VnChoice(
                "dismiss",
                "«Это просто старые истории. Здесь и сейчас важнее.»",
                0, 1),
            new VnChoice(
                "acknowledge",
                "«Это было. По-настоящему, не здесь.»",
                1, 0)
        ));
  }

  public static List<BossEncounterScript.DialogEntry> continuation(MemoryBranch branch) {
    return switch (branch) {
      case DISMISS -> dismissLines();
      case ACKNOWLEDGE -> acknowledgeLines();
      case NONE -> List.of();
    };
  }

  private static List<BossEncounterScript.DialogEntry> dismissLines() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Ладно. Раз ты здесь — не там — придётся напомнить руками,\n"
                + "раз слова не доходят.\n\n"
                + "Последний вопрос, прежде чем начнём: ты ещё помнишь,\n"
                + "каково это — держать что-то настоящее в руках?",
            WOLF_RGB,
            BossEncounterScript.Expression.LUNGE,
            Chapter1AssetPaths.WOLF_PORTRAIT,
            false)
    );
  }

  private static List<BossEncounterScript.DialogEntry> acknowledgeLines() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Хорошо. Тогда вспомним по-старому.\n\n"
                + "Последний вопрос, прежде чем начнём: ты ещё помнишь,\n"
                + "каково это — держать что-то настоящее в руках,\n"
                + "или он уже выел это из тебя без остатка?",
            WOLF_RGB,
            BossEncounterScript.Expression.ATTACK,
            Chapter1AssetPaths.WOLF_PORTRAIT,
            false)
    );
  }
}
