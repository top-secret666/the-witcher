package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.sets.ArmourSet;

/** Ячейка сетки экипировки: обычная броня или эмблема комплекта. */
public record EquipmentGridEntry(Armour armour, ArmourSet armourSet) {

    public static EquipmentGridEntry piece(Armour armour) {
        return new EquipmentGridEntry(armour, null);
    }

    public static EquipmentGridEntry kit(ArmourSet set) {
        return new EquipmentGridEntry(null, set);
    }

    public boolean isKit() {
        return armourSet != null;
    }

    public String title() {
        if (armourSet != null) {
            return armourSet.getName();
        }
        return armour != null ? armour.getName() : "";
    }
}
