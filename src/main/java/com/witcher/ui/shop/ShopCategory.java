package main.java.com.witcher.ui.shop;

/** Категории витрины лавки (карточки на главном экране). */
public enum ShopCategory {
    CHEST("Кираса", 0),
    LEGS("Штаны", 1),
    GLOVES("Перчатки", 2),
    BOOTS("Сапоги", 3),
    POTION("Зелье", 4),
    SETS("Комплекты", -1),
    WEAPON("Оружие", -2);

    public final String label;
    /** Индекс в {@code ShopAssetCache.itemIcons}, -1 — отдельная иконка. */
    public final int iconIndex;

    ShopCategory(String label, int iconIndex) {
        this.label = label;
        this.iconIndex = iconIndex;
    }
}
