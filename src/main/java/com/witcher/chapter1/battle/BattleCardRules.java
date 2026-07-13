package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.ui.shop.ShopEquipSlot;
import main.java.com.witcher.ui.shop.ShopModel;

/** Когда герцог выдаёт карту боя. */
public final class BattleCardRules {

  private BattleCardRules() {
  }

  public static boolean isFullyEquipped(ShopModel model) {
    if (model == null) {
      return false;
    }
    for (ShopEquipSlot slot : ShopEquipSlot.values()) {
      if (model.getEquipped(slot) == null) {
        return false;
      }
    }
    return true;
  }
}
