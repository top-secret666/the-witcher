package main.java.com.witcher.ui.graphics;

import java.awt.image.BufferedImage;

/**
 * Мост Swing ↔ LibGDX: {@code shop_card_back}, {@code icon_crown}, {@code icon_duke_seal}.
 */
public final class ShopUiAssetsFactory {

    private static final String BRIDGE = "main.java.com.witcher.gdx.bridge.HybridShopUiAssets";

    public static final String KEY_CARD_BACK = "shop_card_back@166x249";
    public static final String KEY_CROWN_18 = "icon_crown@18";
    public static final String KEY_CROWN_10 = "icon_crown@10";
    public static final String KEY_DUKE_SEAL = "icon_duke_seal@32";

    private ShopUiAssetsFactory() {
    }

    public static BufferedImage get(String key) {
        try {
            Object result = Class.forName(BRIDGE).getMethod("get", String.class).invoke(null, key);
            if (result instanceof BufferedImage image && image.getWidth() > 0 && image.getHeight() > 0) {
                return image;
            }
        } catch (Throwable ignored) {
            // GDX bridge недоступен
        }
        return null;
    }
}
