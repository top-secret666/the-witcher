package main.java.com.witcher.gdx.bridge;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.shop.ArmourIconRegistry;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEntryIcons;

import java.awt.image.BufferedImage;

/**
 * GDX-иконки с откатом на Swing для каждого товара — если GPU-запекание пустое/прозрачное.
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
        BufferedImage icon = gdx != null ? gdx.iconForEntry(entry, category) : null;
        if (GdxIconBaker.isUsable(icon)) {
            return icon;
        }
        return swing.iconForEntry(entry, category);
    }

    @Override
    public BufferedImage iconForArmour(Armour armour, ShopCategory category, int size) {
        BufferedImage icon = gdx != null ? gdx.iconForArmour(armour, category, size) : null;
        if (GdxIconBaker.isUsable(icon)) {
            return icon;
        }
        return swing.iconForArmour(armour, category, size);
    }
}
