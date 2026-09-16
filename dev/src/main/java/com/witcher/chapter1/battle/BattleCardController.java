package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.shop.EquippedGear;

/**
 * Выдача карты боя: после любой покупки, либо «подарок» Герцога, если ничего не купили.
 */
public final class BattleCardController {

  public static final int REVEAL_TOTAL_TICKS = 120;

  private int revealTicks;
  private boolean revealing;

  public boolean isRevealing() {
    return revealing;
  }

  public int revealTicks() {
    return revealTicks;
  }

  public float revealProgress() {
    return Math.min(1f, revealTicks / (float) REVEAL_TOTAL_TICKS);
  }

  /** После любой успешной покупки. */
  public boolean tryGrantAfterPurchase(Chapter1Session session) {
    return grantIfNeeded(session);
  }

  /**
   * Если клиент ничего не купил, но уже видел кошелёк и ушёл с витрины —
   * Герцог всё равно суёт карту («бесплатное направление»).
   */
  public boolean tryGrantAsBrowseConsolation(Chapter1Session session) {
    return grantIfNeeded(session);
  }

  /** Хук экипировки / зелья — тоже выдаёт карту, если ещё не выдана. */
  public boolean tryGrantAfterEquip(Chapter1Session session, EquippedGear gear) {
    return grantIfNeeded(session);
  }

  private boolean grantIfNeeded(Chapter1Session session) {
    if (session == null || session.battleCardGranted() || session.battleCardIconVisible()) {
      return false;
    }
    session.grantBattleCard();
    session.markBattleCardRevealPending();
    return true;
  }

  public void tickReveal() {
    if (!revealing) {
      return;
    }
    revealTicks++;
    if (revealTicks >= REVEAL_TOTAL_TICKS) {
      revealing = false;
    }
  }

  public void finishReveal(Chapter1Session session) {
    if (session != null) {
      session.showBattleCardIcon();
    }
    revealing = false;
    revealTicks = REVEAL_TOTAL_TICKS;
  }

  public boolean canOpenMap(Chapter1Session session) {
    return session != null && session.battleCardIconVisible() && !revealing;
  }
}
