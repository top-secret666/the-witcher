package main.java.com.witcher.chapter1.loop;

/** Таймлайн процедурного перехода «сквозь чащу» (общий для Swing/движка). */
public final class ForestWalkTimeline {

  public static final int MS_PER_TICK = 16;
  /** Идём в темноте веток, камера качается. */
  public static final int WALK_MS = 1400;
  /** «Вдох» перед раздвижением веток. */
  public static final int PAUSE_MS = 180;
  /** Ветки расходятся в стороны. */
  public static final int PART_MS = 900;
  /** Держим открытый туманный лес перед сценой босса. */
  public static final int HOLD_MS = 700;
  public static final int TOTAL_MS = WALK_MS + PAUSE_MS + PART_MS + HOLD_MS;

  private ForestWalkTimeline() {
  }

  public static int totalTicks() {
    return TOTAL_MS / MS_PER_TICK + 1;
  }

  public static boolean isComplete(int elapsedMs) {
    return elapsedMs >= TOTAL_MS;
  }

  public static long partStartMs() {
    return WALK_MS + PAUSE_MS;
  }

  public static float easeInBack(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    float c1 = 1.70158f;
    float c3 = c1 + 1f;
    return c3 * c * c * c - c1 * c * c;
  }

  public static float easeOutQuad(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return 1f - (1f - c) * (1f - c);
  }
}
