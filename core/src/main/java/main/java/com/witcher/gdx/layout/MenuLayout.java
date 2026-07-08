package main.java.com.witcher.gdx.layout;

/**
 * Раскладка главного меню — сгенерировано tools/swing_to_gdx_layout.py.
 * Координаты в пространстве Swing: 480×360, Y сверху вниз.
 * Рендер через main.java.com.witcher.gdx.graphics.SwingCoords.
 */
public final class MenuLayout {

    public static final float DESIGN_W = 480f;
    public static final float DESIGN_H = 360f;

    public static final float LOGO_Y_RATIO = 0.035f;
    public static final float SIGN_W_RATIO = 0.45f;
    public static final float TITLE_LOGO_W_RATIO = 0.31f;
    public static final float INNER_LOGO_W_OF_SIGN = 0.7f;
    public static final float INNER_LOGO_OFFSET_Y_OF_SIGN = 0.05f;
    public static final float LOGO_MARGIN_BOTTOM = 16f;
    public static final float CONTENT_MARGIN_BOTTOM = 16f;
    public static final float BUTTON_GAP_OF_AVAILABLE = 0.04f;
    public static final float BUTTON_W_RATIO = 0.62f;
    public static final float HELP_Y_FROM_BOTTOM = 8f;
    public static final float CURSOR_W = 18f;
    public static final float CURSOR_HOTSPOT_X = 4f;
    public static final float CURSOR_HOTSPOT_Y = 4f;
    public static final float TEXT_ANCHOR_Y = 0.54f;
    public static final float TEXT_FONT_MIN = 16f;
    public static final float TEXT_FONT_HEIGHT_RATIO = 0.36f;
    public static final float[] TEXT_ANCHOR_X = { 0.43f, 0.47f, 0.47f };

    private MenuLayout() {
    }

    public static float logoY(float viewH) {
        return viewH * LOGO_Y_RATIO;
    }

    public static float signW(float viewW) {
        return viewW * SIGN_W_RATIO;
    }
}
