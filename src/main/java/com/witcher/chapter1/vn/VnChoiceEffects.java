package main.java.com.witcher.chapter1.vn;

import main.java.com.witcher.chapter1.Chapter1Session;

/** Применение дельт доверия/подозрения из VN-выбора к сессии. */
public final class VnChoiceEffects {

  private VnChoiceEffects() {
  }

  public static void apply(Chapter1Session session, VnChoice choice) {
    if (session == null || choice == null) {
      return;
    }
    if (choice.suspicionDelta() > 0) {
      session.addSuspicion(choice.suspicionDelta());
    }
    if (choice.trustDelta() > 0) {
      session.addTrust(choice.trustDelta());
    }
  }
}
