package main.java.com.witcher.chapter1.loop;

import main.java.com.witcher.chapter1.Chapter1Save;
import main.java.com.witcher.chapter1.Chapter1Session;

/**
 * Правила сброса и сохранения витка петли.
 * Канонический исход Волка: {@link #onWolfOutcome}.
 * Legacy VN-бой: {@link #onBattleDefeat}.
 */
public final class LoopRules {

  private LoopRules() {
  }

  /** После поражения в legacy VN-бое: новый виток, мета частично сохраняется. */
  public static void onBattleDefeat(Chapter1Session session) {
    if (!saveIfPresent(session)) {
      return;
    }
    session.advanceLoopAfterDefeat();
    Chapter1Save.save(session);
  }

  /** Ложный побег: петля продолжается, иллюзия «подкручивается». */
  public static void onFalseEscape(Chapter1Session session) {
    if (!saveIfPresent(session)) {
      return;
    }
    session.applyFalseEscape();
    Chapter1Save.save(session);
  }

  /**
   * Канон: исход финала Волка → session + save.
   *
   * @param trueShard {@code true} — осколок без сброса петли; {@code false} — плохая петля
   */
  public static void onWolfOutcome(Chapter1Session session, boolean trueShard) {
    if (trueShard) {
      onWolfTrueShard(session);
    } else {
      onWolfBadLoop(session);
    }
  }

  /** Петля сомкнулась после плохой развилки Волка. */
  public static void onWolfBadLoop(Chapter1Session session) {
    if (!saveIfPresent(session)) {
      return;
    }
    session.markWolfBossResolved(false);
    session.advanceLoopAfterDefeat();
    Chapter1Save.save(session);
  }

  /** После истинного осколка Волка — сохранить прогресс без сброса петли. */
  public static void onWolfTrueShard(Chapter1Session session) {
    if (!saveIfPresent(session)) {
      return;
    }
    Chapter1Save.save(session);
  }

  public static void persist(Chapter1Session session) {
    Chapter1Save.save(session);
  }

  private static boolean saveIfPresent(Chapter1Session session) {
    return session != null;
  }
}
