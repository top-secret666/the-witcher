package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.shop.EquipSlot;

/** Фильтры инвентаря экипировки (иконки как в инвентаре Ведьмака 3). */
public enum EquipmentFilter {
    ALL("Всё", -1),
    CHEST("Кираса", 0),
    LEGS("Штаны", 1),
    GLOVES("Перчатки", 2),
    BOOTS("Сапоги", 3),
    SETS("Комплекты", -2),
    WEAPON("Оружие", -3);

    /** Спец. коды иконок (не слот экипировки). */
    public static final int ICON_ALL = -1;
    public static final int ICON_SETS = -2;
    public static final int ICON_WEAPON = -3;

    public final String sectionLabel;
    /** {@link ShopAssetCache#equipSlotPlaceholder(int)} или спец. код &lt; 0. */
    public final int iconIndex;

    EquipmentFilter(String sectionLabel, int iconIndex) {
        this.sectionLabel = sectionLabel;
        this.iconIndex = iconIndex;
    }

    public boolean matches(Armour armour) {
        return matches(armour, null);
    }

    public boolean matches(Armour armour, ShopModel model) {
        if (this == ALL) {
            return true;
        }
        if (this == WEAPON) {
            return false;
        }
        if (this == SETS) {
            return model != null && model.isSetPiece(armour);
        }
        EquipSlot slot = EquipSlot.forArmour(armour);
        return slot != null && slot.iconIndex == iconIndex;
    }

    /** Фильтры экрана экипировки без оружия (оружие только в инвентаре «особые»). */
    public static EquipmentFilter[] armourFilters() {
        return new EquipmentFilter[]{ALL, CHEST, LEGS, GLOVES, BOOTS, SETS};
    }
}
