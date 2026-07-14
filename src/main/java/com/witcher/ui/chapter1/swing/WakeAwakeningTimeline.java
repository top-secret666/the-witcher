package main.java.com.witcher.ui.chapter1.swing;

/**
 * Таймлайн пробуждения: чёрный экран → быстрый peek → медленное открытие →
 * пару морганий → снова закрытие. Управляет веками, резкостью и шумом GIF.
 */
public final class WakeAwakeningTimeline {

  public static final int TOTAL_MS = 5200;
  public static final int MS_PER_TICK = 16;

  private static final int T_BLACK = 700;
  private static final int T_QUICK_OPEN = 850;
  private static final int T_QUICK_CLOSE = 1000;
  private static final int T_SLOW_OPEN = 3000;
  private static final int T_BLINK1 = 3400;
  private static final int T_BLINK2 = 3800;
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
      return easeOutCubic(t) * 0.38f;
    }
    if (ms < T_QUICK_CLOSE) {
      float t = (ms - T_QUICK_OPEN) / (float) (T_QUICK_CLOSE - T_QUICK_OPEN);
      return 0.38f * (1f - easeInCubic(t));
    }
    if (ms < T_SLOW_OPEN) {
      float t = (ms - T_QUICK_CLOSE) / (float) (T_SLOW_OPEN - T_QUICK_CLOSE);
      return easeOutCubic(t);
    }
    if (ms < T_BLINK1) {
      return blinkPulse(ms - T_SLOW_OPEN, T_BLINK1 - T_SLOW_OPEN);
    }
    if (ms < T_BLINK2) {
      return blinkPulse(ms - T_BLINK1, T_BLINK2 - T_BLINK1);
    }
    if (ms < T_SLOW_CLOSE) {
      float t = (ms - T_BLINK2) / (float) (T_SLOW_CLOSE - T_BLINK2);
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
    } else if (ms < T_BLINK2) {
      clarity = 0.72f + 0.28f * easeOutCubic((ms - T_SLOW_OPEN) / (float) (T_BLINK2 - T_SLOW_OPEN));
    } else if (ms < T_SLOW_CLOSE) {
      clarity = 1f - easeInCubic((ms - T_BLINK2) / (float) (T_SLOW_CLOSE - T_BLINK2)) * 0.55f;
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
    if (t < 0.42f) {
      return 1f - easeInCubic(t / 0.42f) * 0.72f;
    }
    return easeOutCubic((t - 0.42f) / 0.58f);
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
