package main.java.com.witcher.chapter1.vn;

import main.java.com.witcher.chapter1.Chapter1Session;

/** Диалоги герцога в лавке (подозрение / доверие). */
public final class DukeShopDialog {

  private DukeShopDialog() {
  }

  public static boolean shouldShowLoopReturn(Chapter1Session session) {
    return session != null && session.loop() > 1 && session.suspicion() == 0 && session.trust() == 0;
  }

  public static VnSceneState loopReturnGreeting(Chapter1Session session) {
    // До выбора босса на карте — только реплика, без вариантов ответа.
    return new VnSceneState(
        "Герцог",
        "Снова здесь?\nКак приятно видеть постоянного клиента.\n"
            + "Хотя, признаюсь, обычно они помнят, что уже заходили.");
  }

  public static VnSceneState prisonPressure(Chapter1Session session) {
    return null;
  }

  public static boolean shouldShowPrisonPressure(Chapter1Session session) {
    // Реплика «смотрите не на те вещи» отключена.
    return false;
  }
}
