package main.java.com.witcher.chapter1.battle.briefing;

import main.java.com.witcher.chapter1.battle.BossEntry;
import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.intro.IntroTheme;

import java.util.List;

/** Ложный контракт и реплики Герцога перед уходом в лес — только здесь, до пробуждения. */
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

  /** После этой реплики — выбор №1. */
  public static final int CHOICE_GATE_INDEX = 2;

  private BossQuestBriefingScript() {
  }

  public static NoticeContent noticeFor(BossEntry boss) {
    String name = boss != null ? boss.name() : "Белый Волк";
    return new NoticeContent(
        "ЗАКАЗ НА МОНСТРА",
        name.toUpperCase(),
        "Угроза: высокая",
        "Жители хутора у Старого Брода жалуются: в чаще бродит тень\n"
            + "в человеческом обличии. Скот пропадает, дети боятся\n"
            + "выходить за ограду. Свидетели называют зверя «" + name + "».\n"
            + "Требуется устранить угрозу. Не верьте слухам — действуйте.",
        "Вознаграждение: 250 крон\n+ право на трофеи",
        "Печать наместника Белого Ручья");
  }

  public static List<DialogLine> dialogFor(BossEntry boss) {
    String name = boss != null ? boss.name() : "Белый Волк";
    return List.of(
        new DialogLine(
            "Герцог",
            "Вот свежий лист с доски. «" + name + "» — бывший ученик,\n"
                + "бывшая школа. Формальность, не более.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            "Герцог",
            "Хотите — подожду. Вы взглянете на лес и вернётесь.\n"
                + "Лес никуда не денется. Я — тоже.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            null,
            "*Бумага пахнет типографской краской и сухим пергаментом.\n"
                + "Не смолой. Не кровью. Не лесом.*",
            IntroTheme.narratorRgb()),
        new DialogLine(
            "Герцог",
            "Как скажете. Я никуда не тороплюсь.",
            IntroTheme.dukeRgb())
    );
  }

  public static VnSceneState entryChoiceScene() {
    return new VnSceneState(
        "Геральт",
        "Герцог ждёт ответа.",
        List.of(
            new VnChoice("quick", "«Хорошо, я быстро.»", 0, 1),
            new VnChoice("curious", "«Посмотрим, что он скажет.»", 1, 0)
        ));
  }
}
