package main.java.com.witcher.ui.shop;

/** Тип предмета в панели инвентаря (не экипировка). */
public enum ShopInventoryKind {
    WALLET,
    BATTLE_CARD,
    POTION,
    WEAPON,
    ARMOUR,
    /** Эмблема купленного комплекта — экипирует все 4 части. */
    SET;

    public String actionLabel() {
        return switch (this) {
            case POTION -> "Выпить";
            case BATTLE_CARD -> "Открыть";
            default -> "Экипировка";
        };
    }

    public boolean isArmourGrid() {
        return this == ARMOUR || this == SET;
    }
}
