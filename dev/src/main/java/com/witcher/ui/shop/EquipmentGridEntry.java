package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.sets.ArmourSet;

/** Ячейка сетки экипировки: броня, комплект или оружие. */
public record EquipmentGridEntry(Armour armour, ArmourSet armourSet, ShopInventorySlot weapon) {

    public static EquipmentGridEntry piece(Armour armour) {
        return new EquipmentGridEntry(armour, null, null);
    }

    public static EquipmentGridEntry kit(ArmourSet set) {
        return new EquipmentGridEntry(null, set, null);
    }

    public static EquipmentGridEntry weapon(ShopInventorySlot slot) {
        return new EquipmentGridEntry(null, null, slot);
    }

    public boolean isKit() {
        return armourSet != null;
    }

    public boolean isWeapon() {
        return weapon != null;
    }

    public String title() {
        if (weapon != null) {
            return weapon.title();
        }
        if (armourSet != null) {
            return armourSet.getName();
        }
        return armour != null ? armour.getName() : "";
    }
}
