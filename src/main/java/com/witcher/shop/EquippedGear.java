package main.java.com.witcher.shop;

import main.java.com.witcher.model.armour.Armour;

/** Чтение экипировки игрока (лавка, бой, прогресс главы). */
public interface EquippedGear {

  Armour getEquipped(EquipSlot slot);
}
