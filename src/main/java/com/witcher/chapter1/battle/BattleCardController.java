package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.ui.shop.ShopModel;

/**
 * Выдача карты боя после полной экипировки.
 * Анимация reveal — в {@code ui.chapter1.swing.BattleCardRevealView}.
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

  /** @return true если началась новая сцена выдачи карты */
  public boolean tryGrantAfterEquip(Chapter1Session session, ShopModel shop) {
    if (session == null || session.battleCardGranted() || !BattleCardRules.isFullyEquipped(shop)) {
      return false;
    }
    session.grantBattleCard();
    revealing = true;
    revealTicks = 0;
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
