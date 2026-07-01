package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.sets.ArmourSet;

/** Строка списка внутри категории. */
public final class ShopCatalogEntry {
    public final String name;
    public final int price;
    public final Armour armour;
    public final ArmourSet armourSet;
    public final boolean placeholder;
    public final Rectangle bounds = new Rectangle();

    private ShopCatalogEntry(String name, int price, Armour armour, ArmourSet armourSet, boolean placeholder) {
        this.name = name;
        this.price = price;
        this.armour = armour;
        this.armourSet = armourSet;
        this.placeholder = placeholder;
    }

    public static ShopCatalogEntry fromArmour(Armour armour) {
        return new ShopCatalogEntry(armour.getName(), armour.getPrice(), armour, null, false);
    }

    public static ShopCatalogEntry fromSet(ArmourSet set, int price) {
        return new ShopCatalogEntry(set.getName(), price, null, set, false);
    }

    public static ShopCatalogEntry placeholder(String name, int price) {
        return new ShopCatalogEntry(name, price, null, null, true);
    }

    public String priceLabel() {
        return price > 0 ? String.valueOf(price) : "···";
    }
}
