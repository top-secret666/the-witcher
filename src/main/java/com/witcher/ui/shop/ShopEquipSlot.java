package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.Boots;
import main.java.com.witcher.model.armour.Chestpiece;
import main.java.com.witcher.model.armour.Gloves;
import main.java.com.witcher.model.armour.Trousers;

/** Слоты экипировки Геральта в лавке. */
public enum ShopEquipSlot {
    CHEST(0, "Нагрудник"),
    LEGS(1, "Штаны"),
    GLOVES(2, "Перчатки"),
    BOOTS(3, "Сапоги");

    public final int iconIndex;
    public final String label;

    ShopEquipSlot(int iconIndex, String label) {
        this.iconIndex = iconIndex;
        this.label = label;
    }

    public static ShopEquipSlot forArmour(Armour armour) {
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
