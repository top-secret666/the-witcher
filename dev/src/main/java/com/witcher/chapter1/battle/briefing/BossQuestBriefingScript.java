package com.witcher.chapter1.battle.briefing;

import com.witcher.chapter1.battle.BossEntry;
import com.witcher.chapter1.vn.VnChoice;
import com.witcher.chapter1.vn.VnSceneState;
import com.witcher.ui.intro.IntroTheme;

import java.util.List;

/** Ложный контракт и реплики Герцога перед уходом в лес. */
public final class BossQuestBriefingScript {

  public record DialogLine(String speaker, String text, int speakerColorRgb) {
  }

  public record NoticeContent(
      String header,
      String targetName,
      String threatLevel,
      String body,
      String reward,
      String seal
  ) {
  }

  /** Выбор отключён — индекс за пределами диалога. */
  public static final int CHOICE_GATE_INDEX = Integer.MAX_VALUE;

  private BossQuestBriefingScript() {
  }

  public static NoticeContent noticeFor(BossEntry boss) {
    String name = boss != null ? boss.name() : "Волк";
    return new NoticeContent(
        "КОНТРАКТ",
        name.toUpperCase(),
        "Угроза: средняя",
        "В руинах старой школы видели крупного зверя. "
            + "Крестьяне просят ведьмака разобраться, "
            + "пока тварь не спустилась к деревне. "
            + "Цель: «" + name + "».",
        "Награда: 200 оренов",
        "");
  }

  public static List<DialogLine> dialogFor(BossEntry boss) {
    return List.of(
        new DialogLine(
            "Герцог",
            "Прежде чем вы меня покинете, я бы хотел предложить вам небольшой контракт.\n"
                + "В западной части деревни недалеко отсюда есть большой глухой лес.\n"
                + "Там обитает ваша цель. Её нужно убить, чтобы разрушить проклятие.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            "Геральт",
            "Что это за тварь?",
            IntroTheme.geraltRgb()),
        new DialogLine(
            "Герцог",
            "Неизвестно.\nНо местные его называют просто Волк.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            "Геральт",
            "Хочешь, чтобы я пошёл и вскрыл глотку непонятно кому\n"
                + "с моим собственным прозвищем?",
            IntroTheme.geraltRgb()),
        new DialogLine(
            "Герцог",
            "Именно.\nПопрошу вас сделать всё быстро и не вести разговоры с этой сущностью.\n"
                + "Уж больно хорошо оно умеет заговаривать зубы.",
            IntroTheme.dukeRgb())
    );
  }

  public static VnSceneState entryChoiceScene() {
    return new VnSceneState(
        "Геральт",
        "Перед видением.",
        List.of(
            new VnChoice("quick", "«Ладно. Схожу быстро, гляну.»", 0, 1),
            new VnChoice("curious", "«Слишком складно поёшь, купец. Здесь что-то не так.»", 1, 0)
        ));
  }

  public static List<DialogLine> afterChoiceTrust() {
    return List.of();
  }

  public static List<DialogLine> afterChoiceSuspicion() {
    return List.of();
  }
}
