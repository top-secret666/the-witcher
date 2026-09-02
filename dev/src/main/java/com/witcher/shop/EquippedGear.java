package main.java.com.witcher.shop;

import main.java.com.witcher.model.armour.Armour;

/** Чтение экипировки игрока (лавка, бой, прогресс главы). */
public interface EquippedGear {

  Armour getEquipped(EquipSlot slot);

  /** Надета хотя бы одна часть брони или оружие. */
  default boolean hasAnyEquippedItem() {
    for (EquipSlot slot : EquipSlot.values()) {
      if (getEquipped(slot) != null) {
        return true;
      }
    }
    return false;
  }
}
