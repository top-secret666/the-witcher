package main.java.com.witcher.ui.graphics;

/**
 * Появление UI лавки — без GIF. HUD → панель снизу → карточки по очереди → кнопка.
 */
final class ShopRevealAnimator {

    final float sceneBrighten;
    final float hudAlpha;
    final float hudSlideY;
    final float panelAlpha;
    final float panelScale;
    final float panelSlideY;
    final float panelFlash;
    final float btnAlpha;
    final float btnSlideY;
    final float[] cardAlpha;
    final float[] cardScale;
    final float[] cardSlideY;
    final boolean uiInteractive;

    private ShopRevealAnimator(float sceneBrighten, float hudAlpha, float hudSlideY,
                               float panelAlpha, float panelScale, float panelSlideY, float panelFlash,
                               float btnAlpha, float btnSlideY,
                               float[] cardAlpha, float[] cardScale, float[] cardSlideY,
                               boolean uiInteractive) {
        this.sceneBrighten = sceneBrighten;
        this.hudAlpha = hudAlpha;
        this.hudSlideY = hudSlideY;
        this.panelAlpha = panelAlpha;
        this.panelScale = panelScale;
        this.panelSlideY = panelSlideY;
        this.panelFlash = panelFlash;
        this.btnAlpha = btnAlpha;
        this.btnSlideY = btnSlideY;
        this.cardAlpha = cardAlpha;
        this.cardScale = cardScale;
        this.cardSlideY = cardSlideY;
        this.uiInteractive = uiInteractive;
    }

    static ShopRevealAnimator hidden(int cardCount) {
        return forProgress(0f, cardCount, false);
    }

    static ShopRevealAnimator complete(int cardCount) {
        return forProgress(1f, cardCount, true);
    }

    static ShopRevealAnimator forProgress(float progress, int cardCount, boolean interactiveWhenDone) {
        float t = clamp01(progress);
        boolean done = t >= 1f;

        float hudT = segment(t, 0.02f, 0.28f);
        float panelT = segment(t, 0.10f, 0.50f);
        float btnT = segment(t, 0.72f, 0.96f);

        float sceneBrighten = lerp(0.42f, 1f, easeOutCubic(Math.min(t * 1.35f, 1f)));

        float hudAlpha = easeOutCubic(hudT);
        float hudSlideY = (1f - easeOutCubic(hudT)) * -36f;

        float panelAlpha = easeOutCubic(panelT);
        float panelScale = lerp(0.62f, 1f, easeOutBack(panelT));
        float panelSlideY = (1f - easeOutCubic(panelT)) * 48f;

        float flashT = segment(panelT, 0.72f, 1f);
        float panelFlash = (1f - flashT) * 0.55f * panelAlpha;

        float btnAlpha = easeOutCubic(btnT);
        float btnSlideY = (1f - easeOutCubic(btnT)) * 20f;

        float[] cardAlpha = new float[cardCount];
        float[] cardScale = new float[cardCount];
        float[] cardSlideY = new float[cardCount];
        for (int i = 0; i < cardCount; i++) {
            float start = 0.26f + i * 0.07f;
            float end = Math.min(0.94f, start + 0.26f);
            float cardT = segment(t, start, end);
            cardAlpha[i] = easeOutCubic(cardT);
            cardScale[i] = lerp(0.35f, 1f, easeOutBack(cardT));
            cardSlideY[i] = (1f - easeOutCubic(cardT)) * 28f;
        }

        boolean uiInteractive = interactiveWhenDone && done;
        return new ShopRevealAnimator(sceneBrighten, hudAlpha, hudSlideY,
            panelAlpha, panelScale, panelSlideY, panelFlash,
            btnAlpha, btnSlideY, cardAlpha, cardScale, cardSlideY, uiInteractive);
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

    private static float easeOutBack(float t) {
        float x = clamp01(t);
        float c1 = 1.6f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(x - 1f, 3) + c1 * (float) Math.pow(x - 1f, 2);
    }
}
