package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Веки от первого лица поверх катсцены (как в фильмах): только шторки, без радужки/зрачков.
 */
public final class EyesBlinkEffect {

  public enum Mode {
    IDLE, OPENING, BLINKING, DONE
  }

  private static final int OPEN_TICKS = 70;
  private static final int BLINK_CLOSE_TICKS = 12;
  private static final int BLINK_OPEN_TICKS = 28;

  private static final Color LID = new Color(6, 4, 3);
  private static final Color LID_RIM = new Color(28, 16, 12);
  private static final Color LASH = new Color(18, 10, 8, 200);

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
    float openT = switch (mode) {
      case OPENING -> easeOutCubic(ticks / (float) OPEN_TICKS);
      case BLINKING -> {
        if (ticks < BLINK_CLOSE_TICKS) {
          yield 1f - easeInCubic(ticks / (float) BLINK_CLOSE_TICKS);
        }
        yield easeOutCubic((ticks - BLINK_CLOSE_TICKS) / (float) BLINK_OPEN_TICKS);
      }
      default -> 1f;
    };
    openT = Math.max(0f, Math.min(1f, openT));
    drawFirstPersonLids(g, sw, sh, openT);
  }

  /**
   * Верхняя и нижняя шторки смыкаются к центру экрана.
   * openT=0 — полностью закрыто, openT=1 — открыто.
   */
  private void drawFirstPersonLids(Graphics2D g, int sw, int sh, float openT) {
    float closed = 1f - openT;
    if (closed <= 0.01f) {
      return;
    }

    int maxCover = Math.round(sh * 0.52f);
    int lidH = Math.max(1, Math.round(maxCover * closed));

    // Основная масса век
    g.setColor(LID);
    g.fillRect(0, 0, sw, lidH);
    g.fillRect(0, sh - lidH, sw, lidH);

    // Пиксельные «складки» — чуть светлее полосы у края
    g.setColor(LID_RIM);
    int rim = Math.max(2, lidH / 10);
    g.fillRect(0, Math.max(0, lidH - rim), sw, rim);
    g.fillRect(0, sh - lidH, sw, rim);

    // Линия ресниц по стыку века с миром
    g.setColor(LASH);
    for (int x = 0; x < sw; x += 3) {
      int jitter = (x * 17 + ticks) % 3;
      g.fillRect(x, lidH - 1 - jitter, 2, 1);
      g.fillRect(x + 1, sh - lidH + jitter, 2, 1);
    }

    // Боковые уголки — слегка сильнее закрытие (типичный FP blink)
    int cornerW = Math.round(sw * 0.12f * closed);
    if (cornerW > 0) {
      g.setColor(LID);
      for (int i = 0; i < cornerW; i++) {
        int extra = Math.round((cornerW - i) * 0.35f);
        g.fillRect(i, lidH, 1, extra);
        g.fillRect(sw - 1 - i, lidH, 1, extra);
        g.fillRect(i, sh - lidH - extra, 1, extra);
        g.fillRect(sw - 1 - i, sh - lidH - extra, 1, extra);
      }
    }
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
