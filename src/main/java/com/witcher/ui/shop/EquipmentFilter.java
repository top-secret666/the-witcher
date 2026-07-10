package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;

/** Фильтры инвентаря экипировки (иконки как в инвентаре Ведьмака 3). */
public enum EquipmentFilter {
    ALL("Всё", -1),
    CHEST("Кираса", 0),
    LEGS("Штаны", 1),
    GLOVES("Перчатки", 2),
    BOOTS("Сапоги", 3);

    public final String sectionLabel;
    /** {@link ShopAssetCache#equipSlotPlaceholder(int)} или -1 для «всё». */
    public final int iconIndex;

    EquipmentFilter(String sectionLabel, int iconIndex) {
        this.sectionLabel = sectionLabel;
        this.iconIndex = iconIndex;
    }

    public boolean matches(Armour armour) {
        if (this == ALL) {
            return true;
        }
        ShopEquipSlot slot = ShopEquipSlot.forArmour(armour);
        return slot != null && slot.iconIndex == iconIndex;
    }
}
