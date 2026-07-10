package main.java.com.witcher.chapter1.vn;

import java.util.List;

/** Тексты финальной VN-сцены после взлома терминала. */
public final class EndingVnScript {

  private EndingVnScript() {
  }

  public static VnSceneState intro() {
    return new VnSceneState(
        "Герцог",
        "Дверь дрожит. Ты вспомнил слишком много, ведьмак. Скажи — ты всё ещё мой гость?");
  }

  public static VnSceneState finalChoice() {
    return new VnSceneState(
        "Геральт",
        "Последний вопрос. Кому я верю?",
        List.of(
            new VnChoice("suspicion", "Ему лгут. Я выбиваю дверь.", 2, 0),
            new VnChoice("trust", "Он прав — я остаюсь.", 0, 2),
            new VnChoice("neutral", "Молчу и слушаю петлю.", 1, 1)
        ));
  }

  public static VnSceneState resolveLine(boolean trueEscape) {
    if (trueEscape) {
      return new VnSceneState(
          "Рассказчик",
          "Петля трескается. За дверью — не лавка, а утро, которого ты ещё не видел.");
    }
    return new VnSceneState(
        "Рассказчик",
        "Ты выходишь… и снова слышишь звон монет. Герцог улыбается, будто ничего не было.");
  }
}
