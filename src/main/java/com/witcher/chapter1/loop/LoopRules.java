package main.java.com.witcher.chapter1.loop;

import main.java.com.witcher.chapter1.Chapter1Save;
import main.java.com.witcher.chapter1.Chapter1Session;

/** Правила сброса и сохранения витка петли. */
public final class LoopRules {

  private LoopRules() {
  }

  /** После поражения в бою: новый виток, мета-прогресс частично сохраняется. */
  public static void onBattleDefeat(Chapter1Session session) {
    if (session == null) {
      return;
    }
    session.advanceLoopAfterDefeat();
    Chapter1Save.save(session);
  }

  /** Ложный побег: петля продолжается, иллюзия «подкручивается». */
  public static void onFalseEscape(Chapter1Session session) {
    if (session == null) {
      return;
    }
    session.applyFalseEscape();
    Chapter1Save.save(session);
  }

  public static void persist(Chapter1Session session) {
    Chapter1Save.save(session);
  }
}
