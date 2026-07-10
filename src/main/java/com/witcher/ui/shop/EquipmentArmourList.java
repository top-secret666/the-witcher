package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;

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
        ShopEquipSlot slot = ShopEquipSlot.forArmour(armour);
        return slot != null ? slot.shopCategory() : ShopCategory.CHEST;
    }
}
