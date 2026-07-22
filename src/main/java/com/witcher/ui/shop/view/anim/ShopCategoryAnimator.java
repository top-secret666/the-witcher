package main.java.com.witcher.ui.shop.view.anim;

import main.java.com.witcher.ui.intro.IntroEasing;

/**
 * Анимация открытия категории: карточка уезжает влево и растёт, справа выезжает список.
 */
public final class ShopCategoryAnimator {

    public final float progress;
    public final int cardX;
    public final int cardY;
    public final int cardW;
    public final int cardH;
    public final float gridCardsAlpha;
    public final float counterAlpha;
    public final float detailPanelAlpha;
    public final float detailPanelSlideX;
    public final boolean listInteractive;

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

    public static ShopCategoryAnimator opening(float t, int fromX, int fromY, int fromW, int fromH,
                                               int toX, int toY, int toW, int toH) {
        float p = clamp01(t);
        float move = IntroEasing.easeInOutCubic(p);
        int cardX = Math.round(lerp(fromX, toX, move));
        int cardY = Math.round(lerp(fromY, toY, move));
        float scaleT = IntroEasing.easeInOutCubic(p);
        int cardW = Math.round(lerp(fromW, toW, scaleT));
        int cardH = Math.round(lerp(fromH, toH, scaleT));

        float gridFade = 1f - IntroEasing.easeOutCubic(Math.min(p * 1.35f, 1f));
        float counterT = segment(p, 0.06f, 0.72f);
        float counterAlpha = IntroEasing.easeOutCubic(counterT);
        float detailT = segment(p, 0.18f, 0.88f);
        float detailAlpha = IntroEasing.easeOutCubic(detailT);
        float detailSlide = (1f - IntroEasing.easeOutCubic(detailT)) * 36f;

        return new ShopCategoryAnimator(p, cardX, cardY, cardW, cardH,
            gridFade, counterAlpha, detailAlpha, detailSlide, p >= 1f);
    }

    public static ShopCategoryAnimator open(int toX, int toY, int toW, int toH) {
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
}
