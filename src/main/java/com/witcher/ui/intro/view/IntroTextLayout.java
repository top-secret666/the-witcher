package main.java.com.witcher.ui.intro.view;

import main.java.com.witcher.ui.intro.IntroVnUi;

/** Baseline текста интро — общие формулы для Swing и LibGDX. */
public final class IntroTextLayout {

    private IntroTextLayout() {
    }

    public static float dialogFontScale(int fontSize, float baseFontSize) {
        return fontSize / baseFontSize;
    }

    public static float dialogLineBaselineSwingY(float lineSwingY, float capHeight) {
        return lineSwingY - capHeight * 0.35f;
    }

    public static float vnLabelBaselineSwingY(IntroVnUi.Rect button, float capHeight) {
        return button.y + button.height * 0.5f - capHeight * 0.5f;
    }

    public static float vnFontSize(int viewH) {
        return Math.max(IntroLayout.VN_FONT_MIN, viewH * IntroLayout.VN_FONT_SIZE_RATIO);
    }
}
