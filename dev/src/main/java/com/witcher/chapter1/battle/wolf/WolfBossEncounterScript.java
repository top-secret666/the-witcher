package main.java.com.witcher.chapter1.battle.wolf;

import main.java.com.witcher.chapter1.battle.encounter.BossEncounterScript;
import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.intro.IntroTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Лес: только Волк (volk-спрайты). Картинки воспоминаний — только в эпилоге с осколком.
 */
public final class WolfBossEncounterScript {

  public static final int WOLF_RGB = IntroTheme.geraltRgb();

  public enum MemoryBranch {
    NONE,
    DISMISS,
    ACKNOWLEDGE
  }

  /** После этой реплики — выбор №2. */
  public static final int CHOICE_GATE_INDEX = 7;

  private WolfBossEncounterScript() {
  }

  public static List<BossEncounterScript.DialogEntry> introLines(Chapter1Session session) {
    List<BossEncounterScript.DialogEntry> lines = new ArrayList<>();
    lines.add(new BossEncounterScript.DialogEntry(
        null,
        "*Туман висит между деревьями, как мокрая ткань.\n"
            + "Где-то далеко скрипит ветка.\n"
            + "Медальон на груди холодит кожу — ты не помнишь, когда надел его.*",
        IntroTheme.narratorRgb(),
        BossEncounterScript.Expression.MAP));
    lines.add(new BossEncounterScript.DialogEntry(
        null,
        "*Из белой мглы выходит человек в потёртой шкурянке.\n"
            + "Лицо знакомое не чертами, а усталостью.\n"
            + "Так узнают старый шрам под перчаткой.*",
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
        ? "Остановился.\nНадо же.\nОбычно на этом месте ты уже ищешь,\n"
            + "кому бы поверить вместо себя."
        : "Быстро, значит.\nТы всегда умел уходить от важных разговоров\n"
            + "с видом человека, который просто занят.";
    return new BossEncounterScript.DialogEntry(
        "Волк", text, WOLF_RGB, BossEncounterScript.Expression.INTERESTED);
  }

  /** Только текст — без смены фона (воспоминания-картинки в финале). */
  private static List<BossEncounterScript.DialogEntry> memoryLines() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Помнишь дорогу на Ард Каррайг?\n"
                + "Снег по колено.\nПлотва скользит на льду\n"
                + "и смотрит так, будто это ты лично придумал зиму.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED),
        new BossEncounterScript.DialogEntry(
            "Геральт",
            "Плотва часто так смотрела.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Вот.\nПомнишь.\nЭто не доказательство.\n"
                + "Это хуже.\nЭто больно узнавать.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Ты говорил с ней вслух.\nС лошадью.\n"
                + "О дороге, о погоде, о людях, которых стоило бы убить,\n"
                + "но за которых никто не заплатил.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED),
        new BossEncounterScript.DialogEntry(
            "Геральт",
            "Она слушала лучше людей.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED),
        new BossEncounterScript.DialogEntry(
            "Волк",
            "И осуждала честнее.\n\n"
                + "А потом Каэр Морхен.\nВесемир орёт, что ты опять напоил\n"
                + "Плотву ледяной водой.\nТы стоишь, мокрый, злой, голодный,\n"
                + "и улыбаешься как последний идиот.\n\n"
                + "Здесь такого нет.\nЗдесь всё слишком аккуратное.\n"
                + "Торговец улыбается ровно тогда, когда надо.\n"
                + "Карта появляется, когда надо.\n"
                + "А настоящее обычно неудобное.",
            WOLF_RGB,
            BossEncounterScript.Expression.INTERESTED)
    );
  }

  public static VnSceneState memoryChoiceScene() {
    return new VnSceneState(
        "Геральт",
        "Волк говорит о дороге, которой здесь нет.",
        List.of(
            new VnChoice(
                "dismiss",
                "«Это старые истории. Они ничего не меняют.»",
                0, 1),
            new VnChoice(
                "acknowledge",
                "«Это было. Я помню.»",
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
            "Конечно.\nСтарые истории.\nОчень удобно.\n"
                + "Так можно похоронить кого угодно.\n"
                + "Даже себя, если подобрать достаточно сухие слова.\n\n"
                + "Последний вопрос.\nТы ещё помнишь, каково это —\n"
                + "держать в руках что-то настоящее?\n"
                + "Не купленное. Не выданное. Не подсунутое.",
            WOLF_RGB,
            BossEncounterScript.Expression.LUNGE)
    );
  }

  private static List<BossEncounterScript.DialogEntry> acknowledgeLines() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Волк",
            "Тише.\nНе говори это так быстро.\n"
                + "Память не собака. Её нельзя позвать и сразу погладить.\n"
                + "Она сначала кусает.\n\n"
                + "Последний вопрос.\nТы ещё помнишь, каково это —\n"
                + "держать в руках что-то настоящее?\n"
                + "Не купленное. Не выданное. Не подсунутое. Своё.",
            WOLF_RGB,
            BossEncounterScript.Expression.ATTACK)
    );
  }
}
