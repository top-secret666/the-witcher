package main.java.com.witcher.chapter1.battle;

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

  /** После этой реплики — выбор №2. */
  public static final int CHOICE_GATE_INDEX = 2;

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
    lines.add(memoryBlock());
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

  private static BossEncounterScript.DialogEntry memoryBlock() {
    return new BossEncounterScript.DialogEntry(
        "Волк",
        "Помнишь ту дорогу — снег по колено, Плотва\n"
            + "оскальзывается на льду и смотрит так, будто это ты\n"
            + "придумал зиму. А ты ей отвечаешь вслух. Может, и понимала.\n"
            + "Больше, чем иные люди.\n\n"
            + "А потом Каэр Морхен. Весемир ругает за ледяную воду\n"
            + "для лошади — а ты стоишь и улыбаешься, потому что\n"
            + "после стольких лет чьё-то ворчание всё ещё звучит как дом.\n\n"
            + "Здесь такого не бывает. Здесь всё слишком гладко,\n"
            + "чтобы быть правдой — а ты как будто и не замечаешь.",
        WOLF_RGB,
        BossEncounterScript.Expression.INTERESTED);
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
            BossEncounterScript.Expression.LUNGE)
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
            BossEncounterScript.Expression.ATTACK)
    );
  }
}
