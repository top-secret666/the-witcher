package main.java.com.witcher.ui.graphics;

/**
 * Анимация открытия категории: карточка уезжает влево и растёт, справа выезжает список.
 */
final class ShopCategoryAnimator {

    final float progress;
    final int cardX;
    final int cardY;
    final int cardW;
    final int cardH;
    final float gridCardsAlpha;
    final float counterAlpha;
    final float detailPanelAlpha;
    final float detailPanelSlideX;
    final boolean listInteractive;

    private ShopCategoryAnimator(float progress, int cardX, int cardY, int cardW, int cardH,
                                 float gridCardsAlpha, float counterAlpha,
                                 float detailPanelAlpha, float detailPanelSlideX,
                                 boolean listInteractive) {
        this.progress = progress;
        this.cardX = cardX;
        this.cardY = cardY;
        this.cardW = cardW;
        this.cardH = cardH;
        this.gridCardsAlpha = gridCardsAlpha;
        this.counterAlpha = counterAlpha;
        this.detailPanelAlpha = detailPanelAlpha;
        this.detailPanelSlideX = detailPanelSlideX;
        this.listInteractive = listInteractive;
    }

    static ShopCategoryAnimator opening(float t, int fromX, int fromY, int fromW, int fromH,
                                        int toX, int toY, int toW, int toH) {
        float p = clamp01(t);
        float move = easeInOutCubic(p);
        int cardX = Math.round(lerp(fromX, toX, move));
        int cardY = Math.round(lerp(fromY, toY, move));
        float scaleT = easeInOutCubic(p);
        int cardW = Math.round(lerp(fromW, toW, scaleT));
        int cardH = Math.round(lerp(fromH, toH, scaleT));

        float gridFade = 1f - easeOutCubic(Math.min(p * 1.35f, 1f));
        float counterT = segment(p, 0.06f, 0.72f);
        float counterAlpha = easeOutCubic(counterT);
        float detailT = segment(p, 0.18f, 0.88f);
        float detailAlpha = easeOutCubic(detailT);
        float detailSlide = (1f - easeOutCubic(detailT)) * 36f;

        return new ShopCategoryAnimator(p, cardX, cardY, cardW, cardH,
            gridFade, counterAlpha, detailAlpha, detailSlide, p >= 1f);
    }

    static ShopCategoryAnimator open(int toX, int toY, int toW, int toH) {
        return new ShopCategoryAnimator(1f, toX, toY, toW, toH,
            0f, 1f, 1f, 0f, true);
    }

    private static float segment(float t, float start, float end) {
        if (t <= start) {
            return 0f;
        }
        if (t >= end) {
            return 1f;
        }
        return (t - start) / (end - start);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * clamp01(t);
    }

    private static float easeOutCubic(float t) {
        float x = clamp01(t);
        return 1f - (float) Math.pow(1f - x, 3);
    }

    private static float easeInOutCubic(float t) {
        float x = clamp01(t);
        if (x < 0.5f) {
            return 4f * x * x * x;
        }
        return 1f - (float) Math.pow(-2f * x + 2f, 3) / 2f;
    }

    private static float easeOutBack(float t) {
        float x = clamp01(t);
        float c1 = 1.45f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(x - 1f, 3) + c1 * (float) Math.pow(x - 1f, 2);
    }
}
