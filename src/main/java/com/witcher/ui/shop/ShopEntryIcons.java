package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;

import java.awt.image.BufferedImage;

/** Иконки товаров каталога для presenter. */
public interface ShopEntryIcons {

    BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category);

    BufferedImage iconForArmour(Armour armour, ShopCategory category, int size);

    BufferedImage iconForName(String name, ShopCategory category, int size);
}
