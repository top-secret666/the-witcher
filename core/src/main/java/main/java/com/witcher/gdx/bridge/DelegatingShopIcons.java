package main.java.com.witcher.gdx.bridge;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.shop.ArmourIconRegistry;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEntryIcons;

import java.awt.image.BufferedImage;

/**
 * Оригиналы из {@code icons/items} с откатом на GDX-запекание, если файла нет.
 */
public final class DelegatingShopIcons implements ShopEntryIcons {

    private final GdxBakedArmourIconRegistry gdx;
    private final ArmourIconRegistry swing;

    public DelegatingShopIcons(int iconSize) {
        this.gdx = GdxIconBakeSession.isReady() ? GdxBakedArmourIconRegistry.get(iconSize) : null;
        this.swing = ArmourIconRegistry.get(iconSize);
    }

    @Override
    public BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category) {
        BufferedImage icon = swing.iconForEntry(entry, category);
        if (icon != null) {
            return icon;
        }
        icon = gdx != null ? gdx.iconForEntry(entry, category) : null;
        return GdxIconBaker.isUsable(icon) ? icon : null;
    }

    @Override
    public BufferedImage iconForArmour(Armour armour, ShopCategory category, int size) {
        BufferedImage icon = swing.iconForArmour(armour, category, size);
        if (icon != null) {
            return icon;
        }
        icon = gdx != null ? gdx.iconForArmour(armour, category, size) : null;
        return GdxIconBaker.isUsable(icon) ? icon : null;
    }
}
