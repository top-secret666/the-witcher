package main.java.com.witcher.ui.shop;

import main.java.com.witcher.ui.shop.view.ShopUiMetrics;

import java.awt.image.BufferedImage;

/** Ассеты лавки для presenter — без привязки к Swing/LibGDX. */
public interface ShopRuntimeAssets extends ShopUiMetrics {

    BufferedImage iconForCategory(ShopCategory cat);
}
