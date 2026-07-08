package main.java.com.witcher.ui.intro.view;

/**
 * Геометрия диалогового окна интро — без Graphics2D.
 */
public final class IntroDialogLayout {

    public static final class Layout {
        public final int boxX;
        public final int boxY;
        public final int boxW;
        public final int boxH;
        public final int pad;
        public final int textX;
        public final int textY;
        public final int textMaxW;
        public final int fontSize;

        Layout(int sw, int sh, float heightRatio, float widthRatio) {
            boxH = Math.max(52, (int) (sh * heightRatio));
            boxW = Math.max(200, (int) (sw * widthRatio));
            boxX = (sw - boxW) / 2;
            boxY = sh - boxH - (int) (sh * 0.02f);
            if (heightRatio <= 0.11f) {
                fontSize = Math.max(13, (int) (sh * 0.040f));
                pad = Math.max(6, (int) (sw * 0.018f));
            } else {
                fontSize = Math.max(12, (int) (sh * 0.040f));
                pad = (int) (sw * 0.02f);
            }
            textX = boxX + pad;
            textY = boxY + pad;
            textMaxW = boxW - pad * 2;
        }
    }

    private IntroDialogLayout() {
    }

    public static Layout computeLayout(int sw, int sh) {
        return new Layout(sw, sh, 0.30f, 1.0f);
    }

    public static Layout computeLayout(int sw, int sh, float heightRatio, float widthRatio) {
        return new Layout(sw, sh, heightRatio, widthRatio);
    }
}
