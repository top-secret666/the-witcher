package main.java.com.witcher.ui.shop.swing;

import main.java.com.witcher.ui.shop.bridge.ShopBakeKeys;
import main.java.com.witcher.ui.shop.bridge.ShopGdxBridge;

import java.awt.image.BufferedImage;

/**
 * Мост Swing ↔ LibGDX: {@code shop_card_back}, {@code icon_crown}, {@code icon_duke_seal}.
 */
public final class ShopUiAssetsFactory {

    public static final String KEY_CARD_BACK = ShopBakeKeys.CARD_BACK;
    /** Только HUD-плашка. */
    public static final String KEY_HUD_CROWN = ShopBakeKeys.HUD_CROWN;
    public static final String KEY_HUD_DUKE_SEAL = ShopBakeKeys.HUD_DUKE_SEAL;
    /** Монетка у цены товара (каталог, карточка). */
    public static final String KEY_CATALOG_COIN = ShopBakeKeys.CATALOG_COIN;
    /** Мини-иконки статов (защита, выносливость, знаки). */
    public static final String KEY_CATALOG_STAT_SHIELD = ShopBakeKeys.CATALOG_STAT_SHIELD;
    public static final String KEY_CATALOG_STAT_STAMINA = ShopBakeKeys.CATALOG_STAT_STAMINA;
    public static final String KEY_CATALOG_STAT_SIGNS = ShopBakeKeys.CATALOG_STAT_SIGNS;

    private ShopUiAssetsFactory() {
    }

    public static BufferedImage get(String key) {
        return ShopGdxBridge.getUiAsset(key);
    }
}
