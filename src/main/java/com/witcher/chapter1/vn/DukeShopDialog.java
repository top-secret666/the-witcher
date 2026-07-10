package main.java.com.witcher.chapter1.vn;

import main.java.com.witcher.chapter1.Chapter1Session;

import java.util.List;

/** Диалоги герцога в лавке (подозрение / доверие). */
public final class DukeShopDialog {

  private DukeShopDialog() {
  }

  public static boolean shouldShowLoopReturn(Chapter1Session session) {
    return session != null && session.loop() > 1 && session.suspicion() == 0 && session.trust() == 0;
  }

  public static VnSceneState loopReturnGreeting(Chapter1Session session) {
    return new VnSceneState(
        "Герцог",
        "Снова здесь? Отличный выбор брони ждёт тебя, друг.",
        List.of(
            new VnChoice("polite", "Спасибо. Покажите каталог.", 0, 1),
            new VnChoice("doubt", "Мы уже это проходили…", 1, 0)
        ));
  }

  public static VnSceneState prisonPressure(Chapter1Session session) {
    return new VnSceneState(
        "Герцог",
        "Ты слишком долго копаешься в вещах. Может, хватит вопросов?",
        List.of(
            new VnChoice("push", "Где выход?", 2, 0),
            new VnChoice("comply", "Ладно. Я куплю ещё.", 0, 1)
        ));
  }

  public static boolean shouldShowPrisonPressure(Chapter1Session session) {
    return session != null
        && session.prison() >= 3
        && session.prison() < Chapter1Session.PRISON_COUNTER_THRESHOLD;
  }
}
