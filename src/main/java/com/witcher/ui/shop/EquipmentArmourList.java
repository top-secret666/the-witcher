package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.Boots;
import main.java.com.witcher.model.armour.Chestpiece;
import main.java.com.witcher.model.armour.Gloves;
import main.java.com.witcher.model.armour.Trousers;

import java.util.ArrayList;
import java.util.List;

/** Список купленной брони с фильтрацией по слоту. */
public final class EquipmentArmourList {

    private EquipmentArmourList() {
    }

    public static List<Armour> filter(List<Armour> owned, EquipmentFilter filter) {
        if (filter == null || filter == EquipmentFilter.ALL) {
            return owned;
        }
        List<Armour> out = new ArrayList<>();
        for (Armour armour : owned) {
            if (filter.matches(armour)) {
                out.add(armour);
            }
        }
        return out;
    }

    public static ShopCategory categoryFor(Armour armour) {
        if (armour instanceof Chestpiece) {
            return ShopCategory.CHEST;
        }
        if (armour instanceof Trousers) {
            return ShopCategory.LEGS;
        }
        if (armour instanceof Gloves) {
            return ShopCategory.GLOVES;
        }
        if (armour instanceof Boots) {
            return ShopCategory.BOOTS;
        }
        return ShopCategory.CHEST;
    }
}
