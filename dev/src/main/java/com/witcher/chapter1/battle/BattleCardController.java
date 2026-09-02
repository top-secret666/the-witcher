package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.shop.EquippedGear;

/**
 * Выдача карты боя после первой экипировки; анимация — при выходе из экрана экипировки.
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

  /** @return true если карта впервые выдана (иконка ещё не в сумке) */
  public boolean tryGrantAfterEquip(Chapter1Session session, EquippedGear gear) {
    if (session == null || session.battleCardGranted() || session.battleCardIconVisible()) {
      return false;
    }
    if (!qualifiesForBattleCard(gear)) {
      return false;
    }
    session.grantBattleCard();
    session.markBattleCardRevealPending();
    return true;
  }

  private static boolean qualifiesForBattleCard(EquippedGear gear) {
    if (gear instanceof main.java.com.witcher.ui.shop.ShopModel shop) {
      return shop.hasAnyEquippedItem() || shop.hasDrunkAnyPotion();
    }
    return BattleCardRules.canGrantAfterEquip(gear);
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
