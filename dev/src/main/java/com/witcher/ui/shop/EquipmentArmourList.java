package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.sets.ArmourSet;
import main.java.com.witcher.shop.EquipSlot;
import main.java.com.witcher.ui.shop.ShopInventoryKind;
import main.java.com.witcher.ui.shop.ShopInventorySlot;

import java.util.ArrayList;
import java.util.List;

/**
 * Список купленной брони для экрана экипировки.
 * Комплекты — только эмблемой; части из kits/ в слотах после клика по эмблеме.
 */
public final class EquipmentArmourList {

    private EquipmentArmourList() {
    }

    public static List<Armour> filter(List<Armour> owned, EquipmentFilter filter) {
        return filter(owned, filter, null);
    }

    public static List<Armour> filter(List<Armour> owned, EquipmentFilter filter, ShopModel model) {
        List<Armour> out = new ArrayList<>();
        for (Armour armour : owned) {
            if (model != null && model.isSetPiece(armour)) {
                continue;
            }
            if (filter == null || filter == EquipmentFilter.ALL || filter.matches(armour, model)) {
                out.add(armour);
            }
        }
        return out;
    }

    /** Полный список ячеек: эмблемы комплектов + обычная броня (без кусков комплекта). */
    public static List<EquipmentGridEntry> gridEntries(ShopModel model, EquipmentFilter filter) {
        List<EquipmentGridEntry> out = new ArrayList<>();
        if (model == null) {
            return out;
        }
        EquipmentFilter f = filter == null ? EquipmentFilter.ALL : filter;
        if (f == EquipmentFilter.ALL || f == EquipmentFilter.SETS) {
            for (ArmourSet set : model.ownedSets()) {
                out.add(EquipmentGridEntry.kit(set));
            }
        }
        if (f == EquipmentFilter.SETS) {
            return out;
        }
        if (f == EquipmentFilter.WEAPON) {
            java.util.Set<String> seen = new java.util.HashSet<>();
            ShopInventorySlot equipped = model.getEquippedWeapon();
            if (equipped != null) {
                out.add(EquipmentGridEntry.weapon(equipped));
                seen.add(equipped.title());
            }
            for (ShopInventorySlot pouch : model.pouchConsumables()) {
                if (pouch.kind() == ShopInventoryKind.WEAPON && !seen.contains(pouch.title())) {
                    out.add(EquipmentGridEntry.weapon(pouch));
                }
            }
            return out;
        }
        for (Armour armour : model.ownedArmour()) {
            if (model.isSetPiece(armour)) {
                continue;
            }
            if (f == EquipmentFilter.ALL || f.matches(armour, model)) {
                out.add(EquipmentGridEntry.piece(armour));
            }
        }
        return out;
    }

    public static ShopCategory categoryFor(Armour armour) {
        EquipSlot slot = EquipSlot.forArmour(armour);
        return slot != null ? ShopCategory.forEquipSlot(slot) : ShopCategory.CHEST;
    }
}
