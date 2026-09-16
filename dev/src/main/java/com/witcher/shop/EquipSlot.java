package com.witcher.shop;

import com.witcher.model.armour.Armour;
import com.witcher.model.armour.Boots;
import com.witcher.model.armour.Chestpiece;
import com.witcher.model.armour.Gloves;
import com.witcher.model.armour.Trousers;

/** Слоты брони Геральта (лавка и прогресс главы 1). Оружие — отдельно в UI экипировки. */
public enum EquipSlot {
  CHEST(0, "Кираса"),
  LEGS(1, "Штаны"),
  GLOVES(2, "Руки"),
  BOOTS(3, "Сапоги");

  public final int iconIndex;
  public final String label;

  EquipSlot(int iconIndex, String label) {
    this.iconIndex = iconIndex;
    this.label = label;
  }

  public static EquipSlot forArmour(Armour armour) {
    if (armour instanceof Chestpiece) {
      return CHEST;
    }
    if (armour instanceof Trousers) {
      return LEGS;
    }
    if (armour instanceof Gloves) {
      return GLOVES;
    }
    if (armour instanceof Boots) {
      return BOOTS;
    }
    return null;
  }
}
