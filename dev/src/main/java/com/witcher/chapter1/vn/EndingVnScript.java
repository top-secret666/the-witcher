package main.java.com.witcher.chapter1.vn;

import java.util.List;

/** Тексты финальной VN-сцены после взлома терминала. */
public final class EndingVnScript {

  private EndingVnScript() {
  }

  public static VnSceneState intro() {
    return new VnSceneState(
        "Герцог",
        "Дверь дрожит.\nЗабавно, правда?\n"
            + "Люди всегда думают, что выход должен быть дверью.");
  }

  public static VnSceneState finalChoice() {
    return new VnSceneState(
        "Геральт",
        "Последний вопрос.\nКому я верю?",
        List.of(
            new VnChoice("suspicion", "«Ему лгут. Я выбиваю дверь.»", 2, 0),
            new VnChoice("trust", "«Он прав. Я остаюсь.»", 0, 2),
            new VnChoice("neutral", "«Молчу и слушаю петлю.»", 1, 1)
        ));
  }

  public static VnSceneState resolveLine(boolean trueEscape) {
    if (trueEscape) {
      return new VnSceneState(
          "Рассказчик",
          "Петля трескается.\nЗа дверью — не лавка.\n"
              + "Не замок. Не ещё один аккуратно расставленный прилавок.\n"
              + "Утро. Холодное, серое, настоящее.");
    }
    return new VnSceneState(
        "Рассказчик",
        "Вы открываете дверь.\nЗа ней звенят монеты.\n"
            + "Герцог улыбается, будто ничего не было.");
  }
}
