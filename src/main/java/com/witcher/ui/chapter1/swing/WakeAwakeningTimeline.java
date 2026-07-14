package main.java.com.witcher.ui.chapter1.swing;

/**
 * Таймлайн пробуждения: чёрный экран → быстрый peek → открытие → моргание → закрытие.
 */
public final class WakeAwakeningTimeline {

  public static final int TOTAL_MS = 3200;
  public static final int MS_PER_TICK = 16;

  private static final int T_BLACK = 350;
  private static final int T_QUICK_OPEN = 520;
  private static final int T_QUICK_CLOSE = 680;
  private static final int T_SLOW_OPEN = 2000;
  private static final int T_BLINK = 2400;
  private static final int T_SLOW_CLOSE = TOTAL_MS;

  private WakeAwakeningTimeline() {
  }

  public static int totalTicks() {
    return TOTAL_MS / MS_PER_TICK + 1;
  }

  public static float eyelidOpenT(int ms) {
    ms = Math.max(0, Math.min(ms, TOTAL_MS));
    if (ms < T_BLACK) {
      return 0f;
    }
    if (ms < T_QUICK_OPEN) {
      float t = (ms - T_BLACK) / (float) (T_QUICK_OPEN - T_BLACK);
      return easeOutCubic(t) * 0.32f;
    }
    if (ms < T_QUICK_CLOSE) {
      float t = (ms - T_QUICK_OPEN) / (float) (T_QUICK_CLOSE - T_QUICK_OPEN);
      return 0.32f * (1f - easeInCubic(t));
    }
    if (ms < T_SLOW_OPEN) {
      float t = (ms - T_QUICK_CLOSE) / (float) (T_SLOW_OPEN - T_QUICK_CLOSE);
      return easeOutCubic(t);
    }
    if (ms < T_BLINK) {
      return blinkPulse(ms - T_SLOW_OPEN, T_BLINK - T_SLOW_OPEN);
    }
    if (ms < T_SLOW_CLOSE) {
      float t = (ms - T_BLINK) / (float) (T_SLOW_CLOSE - T_BLINK);
      return 1f - easeInCubic(t);
    }
    return 0f;
  }

  /** 0 = сильное размытие, 1 = полная резкость. */
  public static float sharpness(int ms) {
    float open = eyelidOpenT(ms);
    if (open < 0.04f || ms < T_QUICK_CLOSE) {
      return 0f;
    }
    float clarity;
    if (ms < T_SLOW_OPEN) {
      clarity = easeOutCubic((ms - T_QUICK_CLOSE) / (float) (T_SLOW_OPEN - T_QUICK_CLOSE));
    } else if (ms < T_BLINK) {
      clarity = 0.78f + 0.22f * easeOutCubic((ms - T_SLOW_OPEN) / (float) (T_BLINK - T_SLOW_OPEN));
    } else if (ms < T_SLOW_CLOSE) {
      clarity = 1f - easeInCubic((ms - T_BLINK) / (float) (T_SLOW_CLOSE - T_BLINK)) * 0.5f;
    } else {
      clarity = 0f;
    }
    return Math.max(0f, Math.min(1f, clarity * Math.min(1f, open * 1.2f)));
  }

  /** Сильный шум в начале пробуждения, слабеет по мере прояснения. */
  public static float noiseStrength(int ms) {
    float sharp = sharpness(ms);
    float open = eyelidOpenT(ms);
    if (open < 0.04f) {
      return 0.35f;
    }
    return Math.max(0.06f, (1f - sharp) * 0.88f + 0.08f);
  }

  public static boolean isComplete(int ms) {
    return ms >= TOTAL_MS;
  }

  private static float blinkPulse(int offset, int duration) {
    float t = offset / (float) duration;
    if (t < 0.45f) {
      return 1f - easeInCubic(t / 0.45f) * 0.68f;
    }
    return easeOutCubic((t - 0.45f) / 0.55f);
  }

  private static float easeOutCubic(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return 1f - (float) Math.pow(1f - c, 3);
  }

  private static float easeInCubic(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return c * c * c;
  }
}
