package main.java.com.witcher.ui.menu.view;

/**
 * Цвета меню — общие для Swing и LibGDX.
 */
public final class MenuTheme {

    /** Тёплая тёмная тень подписи (не чёрная). */
    public static final float SHADOW_R = 0.18f;
    public static final float SHADOW_G = 0.12f;
    public static final float SHADOW_B = 0.06f;
    public static final float SHADOW_A = 0.42f;

    public static final int SWING_SHADOW_R = 46;
    public static final int SWING_SHADOW_G = 31;
    public static final int SWING_SHADOW_B = 16;
    public static final int SWING_SHADOW_ALPHA = 108;

    public static final float LABEL_R = 245 / 255f;
    public static final float LABEL_G = 220 / 255f;
    public static final float LABEL_B = 120 / 255f;

    public static final float LABEL_PRESSED_R = 200 / 255f;
    public static final float LABEL_PRESSED_G = 170 / 255f;
    public static final float LABEL_PRESSED_B = 90 / 255f;

    public static final float SHADOW_OFFSET_X = 1f;
    public static final float SHADOW_OFFSET_Y = 1f;

    private MenuTheme() {
    }

    public static float labelR(int buttonState) {
        return buttonState == 2 ? LABEL_PRESSED_R : LABEL_R;
    }

    public static float labelG(int buttonState) {
        return buttonState == 2 ? LABEL_PRESSED_G : LABEL_G;
    }

    public static float labelB(int buttonState) {
        return buttonState == 2 ? LABEL_PRESSED_B : LABEL_B;
    }
}
