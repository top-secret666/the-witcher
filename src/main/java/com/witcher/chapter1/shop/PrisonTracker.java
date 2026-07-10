package main.java.com.witcher.chapter1.shop;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.loop.LoopRules;

/** Счётчик «Плен» — покупка и экипировка усиливают зеркального герцога. */
public final class PrisonTracker {

  public static final int PURCHASE_COST = 1;
  public static final int EQUIP_COST = 1;

  private PrisonTracker() {
  }

  public static void onPurchase(Chapter1Session session) {
    apply(session, PURCHASE_COST);
  }

  public static void onEquip(Chapter1Session session) {
    apply(session, EQUIP_COST);
  }

  private static void apply(Chapter1Session session, int amount) {
    if (session == null || amount <= 0) {
      return;
    }
    session.addPrison(amount);
    LoopRules.persist(session);
  }
}
