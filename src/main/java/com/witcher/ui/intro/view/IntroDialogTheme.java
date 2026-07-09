package main.java.com.witcher.ui.intro.view;

/**
 * Цвета и параметры диалогового окна — tools/swing_to_gdx_dialog_box.py
 * Источник: DialogBoxRenderer.java (Swing).
 */
public final class IntroDialogTheme {

    public static final int BOX_BG_R = 10;
    public static final int BOX_BG_G = 8;
    public static final int BOX_BG_B = 4;
    public static final int BOX_BG_A = 220;
    public static final int BOX_BORDER_R = 140;
    public static final int BOX_BORDER_G = 100;
    public static final int BOX_BORDER_B = 35;
    public static final int BOX_BORDER_A = 255;
    public static final int SPEECH_R = 220;
    public static final int SPEECH_G = 190;
    public static final int SPEECH_B = 100;
    public static final int SPEECH_A = 255;
    public static final int HINT_R = 180;
    public static final int HINT_G = 160;
    public static final int HINT_B = 120;
    public static final int HINT_A = 180;
    public static final int NARRATOR_R = 160;
    public static final int NARRATOR_G = 145;
    public static final int NARRATOR_B = 120;
    public static final int NARRATOR_A = 255;
    public static final int GERALT_R = 160;
    public static final int GERALT_G = 205;
    public static final int GERALT_B = 235;
    public static final int GERALT_A = 255;
    public static final int DUKE_R = 218;
    public static final int DUKE_G = 165;
    public static final int DUKE_B = 32;
    public static final int DUKE_A = 255;
    public static final int GRADIENT_TOP_R = 20;
    public static final int GRADIENT_TOP_G = 16;
    public static final int GRADIENT_TOP_B = 8;
    public static final int GRADIENT_TOP_A = 80;
    public static final int GRADIENT_BOTTOM_R = 5;
    public static final int GRADIENT_BOTTOM_G = 4;
    public static final int GRADIENT_BOTTOM_B = 2;
    public static final int GRADIENT_BOTTOM_A = 0;
    public static final int FRAME_OUTER_R = 235;
    public static final int FRAME_OUTER_G = 200;
    public static final int FRAME_OUTER_B = 110;
    public static final int FRAME_OUTER_A = 255;
    public static final int FRAME_INNER_R = 218;
    public static final int FRAME_INNER_G = 165;
    public static final int FRAME_INNER_B = 32;
    public static final int FRAME_INNER_A = 255;
    public static final int FRAME_DARK_R = 60;
    public static final int FRAME_DARK_G = 45;
    public static final int FRAME_DARK_B = 15;
    public static final int FRAME_DARK_A = 200;
    public static final int CORNER_GOLD_R = 255;
    public static final int CORNER_GOLD_G = 215;
    public static final int CORNER_GOLD_B = 0;
    public static final int CORNER_GOLD_A = 220;
    public static final int INNER_STROKE_R = 255;
    public static final int INNER_STROKE_G = 245;
    public static final int INNER_STROKE_B = 160;
    public static final int INNER_STROKE_A = 255;
    public static final int SHADOW_R = 0;
    public static final int SHADOW_G = 0;
    public static final int SHADOW_B = 0;
    public static final int SHADOW_A = 140;
    public static final int OUTLINE_R = 12;
    public static final int OUTLINE_G = 8;
    public static final int OUTLINE_B = 4;
    public static final int OUTLINE_A = 220;

    public static final float BOX_FILL_ALPHA_MUL = 0.9f;
    public static final int CORNER_SIZE = 12;
    public static final int FRAME_OUTER_OFFSET = 2;
    public static final int FRAME_INNER_THICKNESS = 2;
    public static final int FRAME_DARK_INSET = 4;
    public static final float INNER_STROKE_ALPHA_1 = 0.65f;
    public static final float INNER_STROKE_ALPHA_2 = 0.45f;
    public static final float INNER_STROKE_ALPHA_3 = 0.25f;
    public static final int SPEAKER_NAME_PAD_H = 6;
    public static final int SPEAKER_NAME_PAD_V = 2;
    public static final int SPEAKER_NAME_BOX_PAD = 12;
    public static final int SPEAKER_NAME_OFFSET_Y = 2;
    public static final int SPEAKER_NAME_LIFT_EXTRA = 18;
    public static final float DIALOG_HEIGHT_RATIO = 0.3f;
    public static final float DIALOG_BOTTOM_MARGIN_RATIO = 0.02f;
    public static final float DIALOG_FONT_SIZE_RATIO = 0.04f;
    public static final float DIALOG_PAD_RATIO = 0.02f;
    public static final float DIALOG_MIN_HEIGHT = 52f;
    public static final float DIALOG_MIN_WIDTH = 200f;

    public static int packRgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static int speechRgb() {
        return packRgb(SPEECH_R, SPEECH_G, SPEECH_B);
    }

    public static int narratorRgb() {
        return packRgb(NARRATOR_R, NARRATOR_G, NARRATOR_B);
    }

    private IntroDialogTheme() {
    }
}
