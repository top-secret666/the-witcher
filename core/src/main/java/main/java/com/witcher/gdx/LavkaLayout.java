package main.java.com.witcher.gdx;

/**
 * Общие константы лавки для Swing ({@code ShopScreen}) и LibGDX
 * ({@code gdx.screens.ShopScreen}).
 * При смене раскладки — править здесь и в {@code tools/bake_lavka_assets.py}.
 */
public final class LavkaLayout {

    public static final int VIRTUAL_W = 1200;
    public static final int VIRTUAL_H = 720;
    public static final int PANEL_W = 380;
    public static final int CARD_W = 54;
    public static final int CARD_H = 81;
    public static final int CARD_GAP = 6;
    public static final int GRID_COLS = 5;
    public static final int CARD_ART = 32;
    public static final int HUD_H = 72;
    public static final int PIXEL_SCALE = 2;

    private LavkaLayout() {
    }
}
