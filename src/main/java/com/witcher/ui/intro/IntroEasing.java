package main.java.com.witcher.ui.intro;

/**
 * Функции сглаживания анимаций интро.
 */
public final class IntroEasing {

    private IntroEasing() {
    }

    public static float easeOutBack(float t) {
        if (t >= 1f) {
            return 1f;
        }
        if (t <= 0f) {
            return 0f;
        }
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    public static float easeInOutCubic(float t) {
        if (t >= 1f) {
            return 1f;
        }
        if (t <= 0f) {
            return 0f;
        }
        if (t < 0.5f) {
            return 4f * t * t * t;
        }
        return 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    public static float easeOut(float t) {
        return 1f - (1f - t) * (1f - t);
    }

    public static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }
}
