package main.java.com.witcher.gdx;

import main.java.com.witcher.ui.shop.view.ShopViewConstants;

/**
 * Алиас констант лавки для LibGDX-модуля.
 * Канонические значения — в {@link ShopViewConstants}.
 *
 * @deprecated Используйте {@link ShopViewConstants} напрямую.
 */
@Deprecated
public final class LavkaLayout {

    public static final int VIRTUAL_W = ShopViewConstants.VIRTUAL_W;
    public static final int VIRTUAL_H = ShopViewConstants.VIRTUAL_H;
    public static final int PANEL_W = 380;
    public static final int CARD_W = 54;
    public static final int CARD_H = 81;
    public static final int CARD_GAP = 6;
    public static final int GRID_COLS = ShopViewConstants.GRID_COLS;
    public static final int CARD_ART = 32;
    public static final int HUD_H = ShopViewConstants.HUD_H;
    public static final int PIXEL_SCALE = 2;

    private LavkaLayout() {
    }
}
