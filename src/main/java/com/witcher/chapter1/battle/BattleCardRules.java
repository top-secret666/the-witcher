package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.shop.EquipSlot;
import main.java.com.witcher.shop.EquippedGear;

/** Когда герцог выдаёт карту боя. */
public final class BattleCardRules {

  private BattleCardRules() {
  }

  /** Карта выдаётся после первой экипировки любого предмета на себя. */
  public static boolean canGrantAfterEquip(EquippedGear gear) {
    if (gear == null) {
      return false;
    }
    for (EquipSlot slot : EquipSlot.values()) {
      if (gear.getEquipped(slot) != null) {
        return true;
      }
    }
    return false;
  }
}
