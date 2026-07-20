package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.ui.intro.IntroTheme;

import java.util.List;

/** Ложный контракт с доски и реплики Герцога перед уходом в лес. */
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
        "Вознаграждение: 250 крон + право на трофеи",
        "Печать наместника Белого Ручья");
  }

  public static List<DialogLine> dialogFor(BossEntry boss) {
    String name = boss != null ? boss.name() : "Белый Волк";
    return List.of(
        new DialogLine(
            "Герцог",
            "Вот, свежий лист с доски. Всё по форме — как в Темерии любят:\n"
                + "печать, сумма, срок. Красиво же?",
            IntroTheme.dukeRgb()),
        new DialogLine(
            "Герцог",
            "«" + name + "». Зверь, что терроризирует лес у деревни.\n"
                + "Местные уже на взводе — им нужен герой, а нам — тишина.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            "Герцог",
            "Не слушайте их баек. Они видят в каждом шорохе чудовище,\n"
                + "а в каждом чужаке — ведьмака. Вы же профессионал.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            null,
            "*Бумага пахнет типографской краской и сухим пергаментом.\n"
                + "Не смолой. Не кровью. Не лесом.*",
            IntroTheme.narratorRgb()),
        new DialogLine(
            "Герцог",
            "Прочтите ещё раз, если сомневаетесь. Там всё честно написано:\n"
                + "кто плохой, кто хороший, и сколько вам заплатят.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            "Герцог",
            "Ну что ж… Думаю, вы готовы, Белый Волк.\n"
                + "Пора в путь. Лес не будет ждать.",
            IntroTheme.dukeRgb())
    );
  }
}
