package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Graphics2D;

/**
 * Таймлайн век поверх loop_wake: закрыто → открытие → пауза → моргание → открыто.
 * Форма век — {@link EyelidOverlay} (кривые Безье).
 */
public final class EyesBlinkEffect {

  public enum Mode {
    IDLE, OPENING, BLINKING, DONE
  }

  /** ~60 FPS. */
  private static final int MS_PER_TICK = 16;

  // Таймлайн открытия (мс)
  private static final int CLOSED_HOLD_MS = 600;
  private static final int OPEN_DURATION_MS = 1200;
  private static final int OPEN_HOLD_MS = 600;
  private static final int MICRO_BLINK_MS = 200;
  private static final int END_HOLD_MS = 400;

  private static final int BLINK_CLOSE_MS = 100;
  private static final int BLINK_OPEN_MS = 120;

  private Mode mode = Mode.IDLE;
  private int ticks;

  public void reset(Mode startMode) {
    mode = startMode != null ? startMode : Mode.IDLE;
    ticks = 0;
  }

  public Mode mode() {
    return mode;
  }

  public boolean isDone() {
    return mode == Mode.DONE;
  }

  public void startBlink() {
    mode = Mode.BLINKING;
    ticks = 0;
  }

  public void tick() {
    if (mode == Mode.IDLE || mode == Mode.DONE) {
      return;
    }
    ticks++;
    if (mode == Mode.OPENING && openingElapsedMs() >= totalOpeningMs()) {
      mode = Mode.DONE;
    } else if (mode == Mode.BLINKING && blinkElapsedMs() >= BLINK_CLOSE_MS + BLINK_OPEN_MS) {
      mode = Mode.DONE;
    }
  }

  public void render(Graphics2D g, int sw, int sh) {
    if (mode == Mode.IDLE || mode == Mode.DONE) {
      return;
    }
    float openT = switch (mode) {
      case OPENING -> openingOpenT(openingElapsedMs());
      case BLINKING -> blinkOpenT(blinkElapsedMs());
      default -> 1f;
    };
    EyelidOverlay.render(g, sw, sh, openT);
  }

  private int openingElapsedMs() {
    return ticks * MS_PER_TICK;
  }

  private int blinkElapsedMs() {
    return ticks * MS_PER_TICK;
  }

  private static int totalOpeningMs() {
    return CLOSED_HOLD_MS + OPEN_DURATION_MS + OPEN_HOLD_MS + MICRO_BLINK_MS + END_HOLD_MS;
  }

  /**
   * 0.0–0.6с закрыто → 0.6–1.8с ease-out → 1.8–2.4с открыто → 2.4–2.6с моргание → 2.6–3.0с открыто.
   */
  private static float openingOpenT(int ms) {
    int t0 = 0;
    int t1 = t0 + CLOSED_HOLD_MS;
    int t2 = t1 + OPEN_DURATION_MS;
    int t3 = t2 + OPEN_HOLD_MS;
    int t4 = t3 + MICRO_BLINK_MS;
    int t5 = t4 + END_HOLD_MS;

    if (ms < t1) {
      return 0f;
    }
    if (ms < t2) {
      return easeOutCubic((ms - t1) / (float) OPEN_DURATION_MS);
    }
    if (ms < t3) {
      return 1f;
    }
    if (ms < t4) {
      float blinkT = (ms - t3) / (float) MICRO_BLINK_MS;
      if (blinkT < 0.5f) {
        return 1f - easeInCubic(blinkT * 2f) * 0.75f;
      }
      return easeOutCubic((blinkT - 0.5f) * 2f);
    }
    if (ms < t5) {
      return 1f;
    }
    return 1f;
  }

  private static float blinkOpenT(int ms) {
    if (ms < BLINK_CLOSE_MS) {
      return 1f - easeInCubic(ms / (float) BLINK_CLOSE_MS);
    }
    return easeOutCubic((ms - BLINK_CLOSE_MS) / (float) BLINK_OPEN_MS);
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
