package main.java.com.witcher.ui.shop;

import java.awt.image.BufferedImage;

/** Иконки товаров каталога для presenter. */
public interface ShopEntryIcons {

    BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category);
}
