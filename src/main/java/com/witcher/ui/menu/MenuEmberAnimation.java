package main.java.com.witcher.ui.menu;

import java.util.Random;

/**
 * Логика анимации эмберов главного меню — симуляция и визуальные параметры.
 * Одинаково для Swing и LibGDX; рендер только рисует {@link Visual}.
 */
public final class MenuEmberAnimation {

    public static final int X = 0;
    public static final int Y = 1;
    public static final int VX = 2;
    public static final int VY = 3;
    public static final int AGE = 4;
    public static final int MAX_AGE = 5;
    public static final int SIZE = 6;
    public static final int R = 7;
    public static final int G = 8;
    public static final int B = 9;

    public static final int SPAWN_INTERVAL = 3;
    public static final int MAX_PARTICLES = 40;
    public static final int PRESS_ANIMATION_TICKS = 6;

    private static final float FADE_IN_END = 0.15f;
    private static final float FADE_OUT_START = 0.7f;
    private static final float ALPHA_SCALE = 0.8f;
    private static final float PARTICLE_ALPHA = 0.78f;
    private static final float SIZE_SHRINK = 0.4f;
    private static final float GLOW_SIZE_MIN = 1.2f;
    private static final float GLOW_ALPHA_MIN = 0.3f;
    public static final float GLOW_ALPHA = 0.15f;
    public static final float GLOW_R = 1f;
    public static final float GLOW_G = 0.63f;
    public static final float GLOW_B = 0.24f;
    private static final float DRIFT_WOBBLE = 0.15f;
    private static final float DRIFT_WOBBLE_FREQ = 0.03f;
    private static final float VELOCITY_DAMPING = 0.995f;
    private static final float DEAD_Y = -10f;

    public static final class Visual {
        public final float x;
        public final float y;
        public final float size;
        public final float alpha;
        public final float colorR;
        public final float colorG;
        public final float colorB;
        public final boolean glow;
        public final float glowSize;
        public final float glowAlpha;

        public Visual(float x, float y, float size, float alpha,
                      float colorR, float colorG, float colorB,
                      boolean glow, float glowSize, float glowAlpha) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.alpha = alpha;
            this.colorR = colorR;
            this.colorG = colorG;
            this.colorB = colorB;
            this.glow = glow;
            this.glowSize = glowSize;
            this.glowAlpha = glowAlpha;
        }
    }

    private MenuEmberAnimation() {
    }

    public static float[] spawn(Random rng, float viewW, float viewH) {
        float x = rng.nextFloat() * viewW;
        float y = viewH + rng.nextFloat() * 20f;
        float vx = (rng.nextFloat() - 0.5f) * 0.3f;
        float vy = -0.4f - rng.nextFloat() * 0.6f;
        float maxAge = 120 + rng.nextInt(180);
        float size = 1f + rng.nextFloat() * 2f;
        float r = 200 + rng.nextInt(56);
        float g = 80 + rng.nextInt(80);
        float b = 10 + rng.nextInt(30);
        return new float[] {x, y, vx, vy, 0f, maxAge, size, r, g, b};
    }

    public static void tick(float[] particle) {
        particle[AGE]++;
        particle[X] += particle[VX] + (float) Math.sin(particle[AGE] * DRIFT_WOBBLE_FREQ) * DRIFT_WOBBLE;
        particle[Y] += particle[VY];
        particle[VX] *= VELOCITY_DAMPING;
    }

    public static boolean isDead(float[] particle) {
        return particle[AGE] >= particle[MAX_AGE] || particle[Y] < DEAD_Y;
    }

    public static float lifeRatio(float[] particle) {
        return particle[AGE] / particle[MAX_AGE];
    }

    public static float fadeAlpha(float life) {
        float alpha;
        if (life < FADE_IN_END) {
            alpha = life / FADE_IN_END;
        } else if (life > FADE_OUT_START) {
            alpha = (1f - life) / (1f - FADE_OUT_START);
        } else {
            alpha = 1f;
        }
        return Math.max(0f, Math.min(1f, alpha)) * ALPHA_SCALE;
    }

    public static float drawSize(float baseSize, float life) {
        return baseSize * (1f - life * SIZE_SHRINK);
    }

    public static Visual visual(float[] particle) {
        return visual(particle, 1f, 1f);
    }

    public static Visual visual(float[] particle, float scaleX, float scaleY) {
        float life = lifeRatio(particle);
        float alpha = fadeAlpha(life);
        float size = drawSize(particle[SIZE], life);
        boolean glow = size > GLOW_SIZE_MIN && alpha > GLOW_ALPHA_MIN;
        return new Visual(
            particle[X] * scaleX,
            particle[Y] * scaleY,
            size,
            alpha * PARTICLE_ALPHA,
            particle[R] / 255f,
            particle[G] / 255f,
            particle[B] / 255f,
            glow,
            size * 3f,
            alpha * GLOW_ALPHA
        );
    }
}
