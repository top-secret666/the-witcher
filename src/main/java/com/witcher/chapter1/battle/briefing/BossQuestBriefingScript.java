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
        "Жители хутора у Старого Брода жалуются: в чаще бродит тень "
            + "в человеческом обличии. Скот пропадает, дети боятся "
            + "выходить за ограду. Свидетели называют зверя «" + name + "». "
            + "Требуется устранить угрозу. Не верьте слухам — действуйте.",
        "Вознаграждение: 250 крон, право на трофеи",
        "");
  }

  public static List<DialogLine> dialogFor(BossEntry boss) {
    String name = boss != null ? boss.name() : "Белый Волк";
    return List.of(
        new DialogLine(
            "Герцог",
            "А, этот.\nС вашего позволения, я бы не придавал заказу лишнего веса.\n"
                + "«" + name + "» — бывший ученик. Бывшая школа.\n"
                + "Старая боль, которую кто-то решил оформить как контракт.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            "Герцог",
            "Я хочу, чтобы вы взглянули.\nЕсли там чудовище — поступите профессионально.\n"
                + "Если там пустой слух — вернётесь ко мне,\n"
                + "и мы подберём что-нибудь... практичнее воспоминаний.",
            IntroTheme.dukeRgb()),
        new DialogLine(
            null,
            "*Бумага пахнет типографской краской и сухим пергаментом.\n"
                + "Не смолой. Не кровью. Не лесом.*",
            IntroTheme.narratorRgb()),
        new DialogLine(
            "Герцог",
            "Как скажете.\nПодожду.\nВ конце концов, терпение — лучший друг хорошей торговли.",
            IntroTheme.dukeRgb())
    );
  }

  public static VnSceneState entryChoiceScene() {
    return new VnSceneState(
        "Геральт",
        "Герцог ждёт ответа.",
        List.of(
            new VnChoice("quick", "«Хорошо. Быстро посмотрю.»", 0, 1),
            new VnChoice("curious", "«Нет. Слишком гладко звучит.»", 1, 0)
        ));
  }
}
