package main.java.com.witcher.ui.graphics;

/**
 * Появление UI лавки после приветственного диалога — без GIF, только lerp + easing.
 * HUD сверху → панель → карточки по очереди → кнопка «Купить».
 */
final class ShopRevealAnimator {

    final float hudAlpha;
    final float hudSlideY;
    final float panelAlpha;
    final float panelScale;
    final float btnAlpha;
    final float[] cardAlpha;
    final float[] cardScale;
    final boolean uiInteractive;

    private ShopRevealAnimator(float hudAlpha, float hudSlideY, float panelAlpha, float panelScale,
                               float btnAlpha, float[] cardAlpha, float[] cardScale, boolean uiInteractive) {
        this.hudAlpha = hudAlpha;
        this.hudSlideY = hudSlideY;
        this.panelAlpha = panelAlpha;
        this.panelScale = panelScale;
        this.btnAlpha = btnAlpha;
        this.cardAlpha = cardAlpha;
        this.cardScale = cardScale;
        this.uiInteractive = uiInteractive;
    }

    /** До конца диалога — только фон и персонажи. */
    static ShopRevealAnimator hidden(int cardCount) {
        return forProgress(0f, cardCount, false);
    }

    /** Полностью открытая лавка. */
    static ShopRevealAnimator complete(int cardCount) {
        return forProgress(1f, cardCount, true);
    }

    static ShopRevealAnimator forProgress(float progress, int cardCount, boolean interactiveWhenDone) {
        float t = clamp01(progress);
        boolean done = t >= 1f;

        float hudT = segment(t, 0f, 0.22f);
        float panelT = segment(t, 0.12f, 0.42f);
        float btnT = segment(t, 0.78f, 0.98f);

        float hudAlpha = easeOutCubic(hudT);
        float hudSlideY = (1f - easeOutCubic(hudT)) * -14f;

        float panelAlpha = easeOutCubic(panelT);
        float panelScale = lerp(0.9f, 1f, easeOutBack(panelT));

        float btnAlpha = easeOutCubic(btnT);

        float[] cardAlpha = new float[cardCount];
        float[] cardScale = new float[cardCount];
        for (int i = 0; i < cardCount; i++) {
            float start = 0.32f + i * 0.055f;
            float end = Math.min(0.92f, start + 0.22f);
            float cardT = segment(t, start, end);
            cardAlpha[i] = easeOutCubic(cardT);
            cardScale[i] = lerp(0.72f, 1f, easeOutBack(cardT));
        }

        boolean uiInteractive = interactiveWhenDone && done;
        return new ShopRevealAnimator(hudAlpha, hudSlideY, panelAlpha, panelScale, btnAlpha,
            cardAlpha, cardScale, uiInteractive);
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

    /** Лёгкий «отскок» — аккуратно, без мультяшности. */
    private static float easeOutBack(float t) {
        float x = clamp01(t);
        float c1 = 1.4f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(x - 1f, 3) + c1 * (float) Math.pow(x - 1f, 2);
    }
}
