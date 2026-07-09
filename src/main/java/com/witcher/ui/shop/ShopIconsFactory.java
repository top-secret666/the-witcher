package main.java.com.witcher.ui.shop;

/**
 * Фабрика иконок товаров: при наличии GDX-bridge в classpath — GPU-запекание,
 * иначе чистый Swing {@link ArmourIconRegistry}.
 */
public final class ShopIconsFactory {

    private static final String HYBRID_CLASS = "main.java.com.witcher.gdx.bridge.HybridShopIcons";

    private ShopIconsFactory() {
    }

    public static void warmupHybridIcons() {
        try {
            Class<?> hybrid = Class.forName(HYBRID_CLASS);
            hybrid.getMethod("warmup").invoke(null);
        } catch (Throwable error) {
            System.out.println("[ShopIcons] GDX bridge nedostupen, Swing icons");
        }
    }

    public static ShopEntryIcons create(int iconSize) {
        try {
            Class<?> hybrid = Class.forName(HYBRID_CLASS);
            Object icons = hybrid.getMethod("create", int.class).invoke(null, iconSize);
            if (icons instanceof ShopEntryIcons entryIcons) {
                return entryIcons;
            }
        } catch (Throwable error) {
            System.out.println("[ShopIcons] GDX fallback: " + error.getMessage());
        }
        return ArmourIconRegistry.get(iconSize);
    }
}
