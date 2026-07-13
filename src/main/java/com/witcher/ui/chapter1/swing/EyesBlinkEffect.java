package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/** Пиксельная анимация век — открытие / моргание поверх катсцены. */
public final class EyesBlinkEffect {

  public enum Mode {
    IDLE, OPENING, BLINKING, DONE
  }

  private static final int OPEN_TICKS = 72;
  private static final int BLINK_CLOSE_TICKS = 18;
  private static final int BLINK_OPEN_TICKS = 36;

  private Mode mode = Mode.IDLE;
  private int ticks;
  private final Random rng = new Random(17);

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
    switch (mode) {
      case OPENING -> {
        if (ticks >= OPEN_TICKS) {
          mode = Mode.DONE;
        }
      }
      case BLINKING -> {
        if (ticks >= BLINK_CLOSE_TICKS + BLINK_OPEN_TICKS) {
          mode = Mode.DONE;
        }
      }
      default -> { }
    }
  }

  public void render(Graphics2D g, int sw, int sh) {
    if (mode == Mode.IDLE || mode == Mode.DONE) {
      return;
    }
    float t = switch (mode) {
      case OPENING -> easeOut(ticks / (float) OPEN_TICKS);
      case BLINKING -> {
        if (ticks < BLINK_CLOSE_TICKS) {
          yield 1f - ticks / (float) BLINK_CLOSE_TICKS;
        }
        yield (ticks - BLINK_CLOSE_TICKS) / (float) BLINK_OPEN_TICKS;
      }
      default -> 0f;
    };
    t = Math.max(0f, Math.min(1f, t));

    int lidH = Math.round(sh * (0.52f * (1f - t) + 0.02f));
    if (lidH <= 0) {
      return;
    }

    g.setColor(new Color(6, 4, 3));
    g.fillRect(0, 0, sw, lidH);
    g.fillRect(0, sh - lidH, sw, lidH);

    drawIrisHint(g, sw, sh, t);
    drawPixelNoiseLashes(g, sw, sh, lidH);
  }

  private void drawIrisHint(Graphics2D g, int sw, int sh, float openT) {
    if (openT < 0.35f) {
      return;
    }
    int cx = sw / 2;
    int cy = sh / 2;
    int eyeW = Math.round(sw * 0.11f * openT);
    int eyeH = Math.round(sh * 0.055f * openT);
    int gap = Math.round(sw * 0.08f);
    g.setColor(new Color(28, 48, 36, Math.round(180 * openT)));
    g.fillOval(cx - gap - eyeW, cy - eyeH / 2, eyeW * 2, eyeH);
    g.fillOval(cx + gap - eyeW, cy - eyeH / 2, eyeW * 2, eyeH);
    g.setColor(new Color(120, 170, 140, Math.round(200 * openT)));
    int pupil = Math.max(2, eyeW / 4);
    g.fillRect(cx - gap - pupil / 2, cy - pupil / 2, pupil, pupil);
    g.fillRect(cx + gap - pupil / 2, cy - pupil / 2, pupil, pupil);
  }

  private void drawPixelNoiseLashes(Graphics2D g, int sw, int sh, int lidH) {
    g.setColor(new Color(18, 12, 8, 120));
    for (int i = 0; i < 40; i++) {
      int x = rng.nextInt(sw);
      int y = lidH - 2 + rng.nextInt(4);
      g.fillRect(x, y, 2, 1);
      y = sh - lidH + rng.nextInt(4);
      g.fillRect(x, y, 2, 1);
    }
  }

  private static float easeOut(float t) {
    return 1f - (1f - t) * (1f - t);
  }
}
