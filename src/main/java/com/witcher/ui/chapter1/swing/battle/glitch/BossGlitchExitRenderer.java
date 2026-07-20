package main.java.com.witcher.ui.chapter1.swing.battle.glitch;

import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Random;

/** Отрисовка «ВЫХОД» в диалоге и разброса по экрану (второй занавес). */
public final class BossGlitchExitRenderer {

  private static final Color RED_TEXT = new Color(200, 18, 28);
  private static final String EXIT_WORD = "ВЫХОД";

  private BossGlitchExitRenderer() {
  }

  public static void drawDialogLine(Graphics2D g, int sw, int sh, String text, float scale) {
    int dialogH = Math.round(sh * 0.22f);
    int y0 = sh - dialogH;
    g.setColor(new Color(0, 0, 0, 210));
    g.fillRect(0, y0, sw, dialogH);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int fontSize = Math.max(16, Math.round(sh * 0.055f * Math.max(0.5f, scale)));
    g.setFont(GameFonts.get().bold(fontSize));
    FontMetrics fm = g.getFontMetrics();
    int tx = (sw - fm.stringWidth(text)) / 2;
    int ty = y0 + (dialogH + fm.getAscent() - fm.getDescent()) / 2;
    g.setColor(RED_TEXT);
    g.drawString(text, tx, ty);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  /** Одно «ВЫХОД» в диалоге: обычный / больше / ещё больше (замена, не стек). */
  public static void drawExitDialogSingle(Graphics2D g, int sw, int sh, int step) {
    int dialogH = Math.round(sh * 0.22f);
    int y0 = sh - dialogH;
    g.setColor(new Color(0, 0, 0, 210));
    g.fillRect(0, y0, sw, dialogH);

    float scale = switch (step) {
      case 1 -> 1.55f;
      case 2 -> 2.15f;
      default -> 1f;
    };
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int baseSize = Math.max(16, Math.round(sh * 0.055f));
    int fontSize = Math.max(14, Math.round(baseSize * scale));
    g.setFont(GameFonts.get().bold(fontSize));
    FontMetrics fm = g.getFontMetrics();
    int tx = (sw - fm.stringWidth(EXIT_WORD)) / 2;
    int ty = y0 + (dialogH + fm.getAscent() - fm.getDescent()) / 2;
    g.setColor(RED_TEXT);
    g.drawString(EXIT_WORD, tx, ty);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  /** Много мелких «ВЫХОД» случайно по экрану, без наложений. */
  public static void drawScatteredExitWords(Graphics2D g, int sw, int sh, float fillT) {
    if (fillT <= 0.01f) {
      return;
    }
    float fill = Math.max(0f, Math.min(1f, fillT));
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int fontSize = Math.max(9, Math.round(sh * 0.026f));
    g.setFont(GameFonts.get().bold(fontSize));
    FontMetrics fm = g.getFontMetrics();
    int tw = fm.stringWidth(EXIT_WORD);
    int th = fm.getHeight();
    int minGap = Math.max(4, Math.round(fontSize * 0.35f));

    float areaPerWord = (tw + minGap * 2f) * (th + minGap * 2f);
    int capacity = Math.max(40, Math.round((sw * sh) / areaPerWord * 0.85f));
    int target = Math.max(1, Math.round(capacity * (0.04f + fill * 0.96f)));

    Random rnd = new Random(9041L);
    int[] xs = new int[capacity];
    int[] ys = new int[capacity];
    int placed = 0;
    int attempts = capacity * 40;
    for (int a = 0; a < attempts && placed < capacity; a++) {
      int x = rnd.nextInt(Math.max(1, sw - tw));
      int y = th + rnd.nextInt(Math.max(1, sh - th));
      boolean ok = true;
      for (int i = 0; i < placed; i++) {
        int dx = Math.abs(xs[i] - x);
        int dy = Math.abs(ys[i] - y);
        if (dx < tw + minGap && dy < th + minGap) {
          ok = false;
          break;
        }
      }
      if (!ok) {
        continue;
      }
      xs[placed] = x;
      ys[placed] = y;
      placed++;
    }

    int drawCount = Math.min(target, placed);
    g.setColor(RED_TEXT);
    for (int i = 0; i < drawCount; i++) {
      g.drawString(EXIT_WORD, xs[i], ys[i]);
    }
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }
}
