package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Color;
import java.awt.Graphics2D;

/** Пиксельная анимация век — открытие / моргание поверх катсцены. */
public final class EyesBlinkEffect {

  public enum Mode {
    IDLE, OPENING, BLINKING, DONE
  }

  private static final int OPEN_TICKS = 90;
  private static final int BLINK_CLOSE_TICKS = 14;
  private static final int BLINK_OPEN_TICKS = 32;

  private static final Color LID_DARK = new Color(8, 5, 4);
  private static final Color LASH = new Color(22, 14, 10, 140);
  private static final Color SCLERA = new Color(210, 198, 175);
  private static final Color IRIS = new Color(36, 72, 54);
  private static final Color IRIS_GLOW = new Color(130, 210, 160, 120);
  private static final Color PUPIL = new Color(8, 8, 10);
  private static final Color HIGHLIGHT = new Color(255, 255, 255);

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
      case OPENING -> easeOutCubic(ticks / (float) OPEN_TICKS);
      case BLINKING -> {
        if (ticks < BLINK_CLOSE_TICKS) {
          yield 1f - easeInCubic(ticks / (float) BLINK_CLOSE_TICKS);
        }
        yield easeOutCubic((ticks - BLINK_CLOSE_TICKS) / (float) BLINK_OPEN_TICKS);
      }
      default -> 0f;
    };
    t = Math.max(0f, Math.min(1f, t));

    drawIrisHint(g, sw, sh, t);
    drawPixelEyelids(g, sw, sh, t);
  }

  private void drawPixelEyelids(Graphics2D g, int sw, int sh, float openT) {
    float closed = 1f - openT;
    int lidH = Math.max(0, Math.round(sh * 0.54f * closed));
    if (lidH <= 0) {
      return;
    }

    g.setColor(LID_DARK);
    g.fillRect(0, 0, sw, lidH);
    g.fillRect(0, sh - lidH, sw, lidH);

    g.setColor(LASH);
    for (int i = 0; i < 24; i++) {
      int x = (i * 37 + ticks * 3) % sw;
      g.fillRect(x, lidH - 2, 2, 1);
      g.fillRect((x + 11) % sw, sh - lidH + 1, 2, 1);
    }
  }

  private void drawIrisHint(Graphics2D g, int sw, int sh, float openT) {
    if (openT < 0.12f) {
      return;
    }
    float eyeT = Math.min(1f, (openT - 0.12f) / 0.88f);
    int cx = sw / 2;
    int cy = sh / 2;
    int gap = Math.round(sw * 0.085f);
    int eyeW = Math.max(8, Math.round(sw * 0.13f * eyeT));
    int eyeH = Math.max(4, Math.round(sh * 0.065f * eyeT));

    for (int side = -1; side <= 1; side += 2) {
      drawSingleEye(g, cx + side * gap, cy, eyeW, eyeH, eyeT);
    }
  }

  private void drawSingleEye(Graphics2D g, int cx, int cy, int eyeW, int eyeH, float eyeT) {
    int pxW = Math.max(eyeW - (eyeW % 2), 8);
    int pxH = Math.max(eyeH - (eyeH % 2), 4);
    int left = cx - pxW / 2;
    int top = cy - pxH / 2;
    int alpha = Math.round(220 * eyeT);

    g.setColor(withAlpha(SCLERA, alpha));
    g.fillRect(left, top, pxW, pxH);

    int irisW = Math.max(4, pxW * 2 / 3);
    int irisH = Math.max(3, pxH * 2 / 3);
    int irisX = cx - irisW / 2;
    int irisY = cy - irisH / 2;
    g.setColor(withAlpha(IRIS, Math.round(240 * eyeT)));
    g.fillRect(irisX, irisY, irisW, irisH);

    g.setColor(withAlpha(IRIS_GLOW, Math.round(100 * eyeT)));
    g.fillRect(irisX - 1, irisY - 1, irisW + 2, irisH + 2);

    int pupil = Math.max(2, irisW / 3);
    g.setColor(PUPIL);
    g.fillRect(cx - pupil / 2, cy - pupil / 2, pupil, pupil);

    g.setColor(withAlpha(HIGHLIGHT, Math.round(200 * eyeT)));
    g.fillRect(cx - pupil / 2 - 2, cy - pupil / 2 - 1, 2, 2);
  }

  private static Color withAlpha(Color base, int alpha) {
    return new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.max(0, Math.min(255, alpha)));
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
