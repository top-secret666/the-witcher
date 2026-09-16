package com.witcher.chapter1.battle.wolf;

import com.witcher.chapter1.battle.encounter.BossEncounterScript;
import com.witcher.chapter1.Chapter1Session;
import com.witcher.chapter1.vn.VnChoice;
import com.witcher.chapter1.vn.VnSceneState;
import com.witcher.ui.intro.IntroTheme;

import java.util.ArrayList;
import java.util.List;

/**
 * Лес: диалог с Волком → вспышка памяти (Весемир) → финал.
 * Выбор памяти отключён (одна концовка); вернём в Unity.
 */
public final class WolfBossEncounterScript {

  public static final int WOLF_RGB = IntroTheme.geraltRgb();

  public enum MemoryBranch {
    NONE,
    DISMISS,
    ACKNOWLEDGE
  }

  /** Индекс «...» Волка — с этой реплики музыка леса обрывается. */
  public static final int WOLF_MUSIC_CUT_INDEX = 11;

  /** Индекс «Кто?» — после него вспышка с Весемиром. */
  public static final int CHOICE_GATE_INDEX = 15;

  private WolfBossEncounterScript() {
  }

  public static List<BossEncounterScript.DialogEntry> introLines(Chapter1Session session) {
    List<BossEncounterScript.DialogEntry> lines = new ArrayList<>();
    lines.add(new BossEncounterScript.DialogEntry(
        null,
        "*Морозный туман забивает лёгкие. Настоящий, колючий холод.*",
        IntroTheme.narratorRgb(),
        BossEncounterScript.Expression.MAP_INTERESTED));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "Наконец очнулся?",
        WOLF_RGB,
        BossEncounterScript.Expression.MAP));
    lines.add(new BossEncounterScript.DialogEntry(
        "Геральт",
        "Что... ты... такое?",
        IntroTheme.geraltRgb(),
        BossEncounterScript.Expression.MAP));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "Ты знаешь, кто я. Я это ты...",
        WOLF_RGB,
        BossEncounterScript.Expression.REACH));
    lines.add(new BossEncounterScript.DialogEntry(
        "Геральт",
        "Знаешь, верится с трудом.\n"
            + "Как по мне, ты больше смахиваешь на говорящий скелет.",
        IntroTheme.geraltRgb(),
        BossEncounterScript.Expression.REACH));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "Но ты же сам не любишь судить по обложке, ведьмак...\n"
            + "Ты убивал далеко не каждую тварь, что у тебя заказывали,\n"
            + "а иногда мог даже отпустить...",
        WOLF_RGB,
        BossEncounterScript.Expression.REACH));
    lines.add(new BossEncounterScript.DialogEntry(
        "Геральт",
        "Я отпускал только невинных...\n"
            + "Чем же ты сможешь доказать то, что ты не являешься проклятием,\n"
            + "которое просто заговаривает мне зубы...",
        IntroTheme.geraltRgb(),
        BossEncounterScript.Expression.REACH));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "Доказать? Посмотри вокруг.\n"
            + "Где эта деревня, где испуганные крестьяне?\n"
            + "В этой глуши нет ни одной живой души, кроме нас с тобой.\n\n"
            + "Настоящее проклятие сидит в своей лавке и считает монеты,\n"
            + "пока ты делаешь всю грязную работу.",
        WOLF_RGB,
        BossEncounterScript.Expression.ARMS));
    lines.add(new BossEncounterScript.DialogEntry(
        "Геральт",
        "...Зараза...ты говоришь одними загадками...\n\n"
            + "Почему ты одет как ведьмак?",
        IntroTheme.geraltRgb(),
        BossEncounterScript.Expression.MAP_INTERESTED));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "Потому что я это ты...",
        WOLF_RGB,
        BossEncounterScript.Expression.REACH));
    lines.add(new BossEncounterScript.DialogEntry(
        "Геральт",
        "Или ты просто снял одежду с мертвеца...\n"
            + "Почему ты так яростно пытаешься доказать, что ты часть меня?",
        IntroTheme.geraltRgb(),
        BossEncounterScript.Expression.KNIFE));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "...",
        WOLF_RGB,
        BossEncounterScript.Expression.KNIFE));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "....",
        WOLF_RGB,
        BossEncounterScript.Expression.SHUSH));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "....",
        WOLF_RGB,
        BossEncounterScript.Expression.LISTEN));
    lines.add(new BossEncounterScript.DialogEntry(
        "Волк",
        "Он знает... Он идёт...\n"
            + "У нас осталось не так много времени,\n"
            + "но я успею показать тебе то, что ты должен знать...",
        WOLF_RGB,
        BossEncounterScript.Expression.STAND));
    lines.add(new BossEncounterScript.DialogEntry(
        "Геральт",
        "Кто?",
        IntroTheme.geraltRgb(),
        BossEncounterScript.Expression.STAND));
    return lines;
  }

  public static VnSceneState memoryChoiceScene() {
    return new VnSceneState(
        "Геральт",
        "Отношение к памяти.",
        List.of(
            new VnChoice(
                "dismiss",
                "«Это старые байки. Они мертвы, и ворошить их нет смысла.»",
                0, 1),
            new VnChoice(
                "acknowledge",
                "«Это было со мной. Я помню каждый вдох.»",
                1, 0)
        ));
  }

  public static List<BossEncounterScript.DialogEntry> continuation(MemoryBranch branch) {
    return vesemirFlashback();
  }

  private static List<BossEncounterScript.DialogEntry> vesemirFlashback() {
    return List.of(
        new BossEncounterScript.DialogEntry(
            "Весемир",
            "Не распускай колени! Смягчай шаг!\n"
                + "Бедрами, бедрами работай! Сила идет из бедер, а не из плеча!",
            IntroTheme.strangerRgb(),
            BossEncounterScript.Expression.STAND,
            BossEncounterScript.SceneKind.VESEMIR),
        new BossEncounterScript.DialogEntry(
            "Геральт",
            "Я... я стараюсь...",
            IntroTheme.geraltRgb(),
            BossEncounterScript.Expression.STAND,
            BossEncounterScript.SceneKind.VESEMIR),
        new BossEncounterScript.DialogEntry(
            "Весемир",
            "Ты не стараешься, ты машешь клинком, словно цепом для обмолота ржи!\n"
                + "Расслабься! Вложи силу в удар, но расслабь мышцы сразу после него.\n"
                + "Иначе ты быстро выдохнешься, у тебя задрожат пальцы,\n"
                + "и ты не сможешь сделать финт.",
            IntroTheme.strangerRgb(),
            BossEncounterScript.Expression.STAND,
            BossEncounterScript.SceneKind.VESEMIR),
        new BossEncounterScript.DialogEntry(
            "Геральт",
            "Но они и так дрожат...\nМеч слишком тяжелый...",
            IntroTheme.geraltRgb(),
            BossEncounterScript.Expression.STAND,
            BossEncounterScript.SceneKind.VESEMIR)
    );
  }
}
