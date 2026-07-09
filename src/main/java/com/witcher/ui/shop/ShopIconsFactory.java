package main.java.com.witcher.ui.shop;

import main.java.com.witcher.ui.shop.bridge.ShopGdxBridge;

/**
 * Фабрика иконок товаров: при наличии GDX-bridge в classpath — GPU-запекание,
 * иначе чистый Swing {@link ArmourIconRegistry}.
 */
public final class ShopIconsFactory {

    private ShopIconsFactory() {
    }

    public static void warmupHybridIcons() {
        ShopGdxBridge.warmupIcons();
    }

    public static ShopEntryIcons create(int iconSize) {
        return ShopGdxBridge.createIcons(iconSize);
    }
}
