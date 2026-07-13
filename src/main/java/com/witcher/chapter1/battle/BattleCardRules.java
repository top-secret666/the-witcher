package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.shop.EquipSlot;
import main.java.com.witcher.shop.EquippedGear;

/** Когда герцог выдаёт карту боя. */
public final class BattleCardRules {

  private BattleCardRules() {
  }

  public static boolean isFullyEquipped(EquippedGear gear) {
    if (gear == null) {
      return false;
    }
    for (EquipSlot slot : EquipSlot.values()) {
      if (gear.getEquipped(slot) == null) {
        return false;
      }
    }
    return true;
  }
}
