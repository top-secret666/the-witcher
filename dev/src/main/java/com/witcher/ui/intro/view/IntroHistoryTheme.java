package main.java.com.witcher.ui.intro.view;

/**
 * Окно истории интро — tools/swing_to_gdx_history_panel.py
 * Источник: IntroScreen.drawHistoryOverlay (Swing).
 */
public final class IntroHistoryTheme {

    public static final float DIM_ALPHA = 0.85f;
    public static final float FONT_SIZE_RATIO = 0.034f;
    public static final int FONT_SIZE_MIN = 13;
    public static final int TITLE_SIZE_DELTA = 1;
    public static final int HINT_SIZE_DELTA = -1;
    public static final int HINT_SIZE_MIN = 10;
    public static final int LINE_GAP = 4;
    public static final float PAD_RATIO = 0.022f;
    public static final int PAD_MIN = 10;
    public static final int HEADER_GAP = 8;
    public static final int FOOTER_GAP = 6;
    public static final int TITLE_R = 255;
    public static final int TITLE_G = 195;
    public static final int TITLE_B = 110;
    public static final int TITLE_A = 255;
    public static final int DIVIDER_R = 255;
    public static final int DIVIDER_G = 215;
    public static final int DIVIDER_B = 0;
    public static final int DIVIDER_A = 160;
    public static final int SPEAKER_R = 180;
    public static final int SPEAKER_G = 150;
    public static final int SPEAKER_B = 90;
    public static final int SPEAKER_A = 255;
    public static final int BODY_R = 210;
    public static final int BODY_G = 195;
    public static final int BODY_B = 155;
    public static final int BODY_A = 255;
    public static final int HINT_R = 150;
    public static final int HINT_G = 130;
    public static final int HINT_B = 95;
    public static final int HINT_A = 200;
    public static final int CLOSE_BTN_SIZE = 18;
    public static final int CLOSE_BTN_MARGIN_X = 6;
    public static final int CLOSE_BTN_MARGIN_Y = 5;
    public static final float PANEL_W_RATIO = 0.82f;
    public static final float PANEL_H_RATIO = 0.72f;
    public static final int PANEL_MIN_W = 280;
    public static final int PANEL_MIN_H = 200;

    public static int fontSize(int viewH) {
        return Math.max(FONT_SIZE_MIN, Math.round(viewH * FONT_SIZE_RATIO));
    }

    public static int pad(int viewW) {
        return Math.max(PAD_MIN, Math.round(viewW * PAD_RATIO));
    }

    private IntroHistoryTheme() {
    }
}
