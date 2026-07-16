package main.java.com.witcher.ui.chapter1.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/** Пиксельные помехи: ТВ-статика (красно-бело-чёрные) и цифровой «дождь» 0/1/букв. */
public final class PixelBugOverlay {

  private static final char[] CODE_CHARS = "01ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();

  private static BufferedImage[] baked;
  private static int cachedW;
  private static int cachedH;
  private static int animFrame;

  private PixelBugOverlay() {
  }

  /** Старый общий режим (для прочих мест). */
  public static void draw(Graphics2D g, int sw, int sh, float intensity, long seedMs) {
    drawTvStatic(g, sw, sh, intensity, seedMs);
  }

  /** Красно-бело-чёрные ТВ-помехи. intensity 0..1. */
  public static void drawTvStatic(Graphics2D g, int sw, int sh, float intensity, long seedMs) {
    drawTvStatic(g, sw, sh, intensity, seedMs, false);
  }

  /**
   * ТВ-помехи. {@code opaqueCurtain} — первый занавес: на пике ничего не видно под шумом.
   */
  public static void drawTvStatic(Graphics2D g, int sw, int sh, float intensity, long seedMs,
                                  boolean opaqueCurtain) {
    if (g == null || intensity <= 0.01f || sw <= 0 || sh <= 0) {
      return;
    }
    ensure(sw, sh);
    float clamped = Math.max(0f, Math.min(1f, intensity));
    Composite prev = g.getComposite();

    if (opaqueCurtain && clamped > 0.35f) {
      float veil = (clamped - 0.35f) / 0.65f;
      g.setColor(new Color(8, 0, 0, Math.round(120 + veil * 135)));
      g.fillRect(0, 0, sw, sh);
    }

    float bakeA = opaqueCurtain ? (0.4f + clamped * 0.6f) : (0.25f + clamped * 0.7f);
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bakeA));
    g.drawImage(baked[animFrame % baked.length], 0, 0, null);
    if (opaqueCurtain && clamped > 0.55f) {
      g.drawImage(baked[(animFrame + 2) % baked.length], 0, 0, null);
    }
    g.setComposite(prev);
    animFrame++;

    Random rnd = new Random(seedMs * 131L + animFrame * 17L);
    float blockMul = opaqueCurtain ? 1.85f : 1f;
    int blocks = Math.round((10 + clamped * 90) * blockMul);
    for (int i = 0; i < blocks; i++) {
      int bw = 2 + rnd.nextInt(Math.max(2, Math.round(sw * 0.1f * clamped)));
      int bh = 1 + rnd.nextInt(Math.max(1, Math.round(sh * 0.05f * clamped)));
      int bx = rnd.nextInt(Math.max(1, sw - bw));
      int by = rnd.nextInt(Math.max(1, sh - bh));
      Color c = switch (rnd.nextInt(5)) {
        case 0, 1 -> new Color(200 + rnd.nextInt(55), 20 + rnd.nextInt(40), 20 + rnd.nextInt(30),
            Math.round(100 + clamped * 155));
        case 2 -> new Color(240, 240, 245, Math.round(90 + clamped * 150));
        default -> new Color(0, 0, 0, Math.round(120 + clamped * 135));
      };
      g.setColor(c);
      g.fillRect(bx, by, bw, bh);
    }

    int bands = Math.round((3 + clamped * 16) * (opaqueCurtain ? 1.6f : 1f));
    for (int i = 0; i < bands; i++) {
      int by = rnd.nextInt(sh);
      int bh = 1 + rnd.nextInt(Math.max(1, Math.round(2 + clamped * 10)));
      boolean red = rnd.nextFloat() < 0.7f;
      g.setColor(red
          ? new Color(255, 30, 30, Math.round(70 + clamped * 160))
          : new Color(230, 230, 235, Math.round(50 + clamped * 120)));
      g.fillRect(0, by, sw, bh);
    }

    int shift = Math.round(clamped * (opaqueCurtain ? 8 : 5));
    if (shift > 0) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
          (opaqueCurtain ? 0.35f : 0.2f) * clamped));
      g.setColor(new Color(255, 0, 0, opaqueCurtain ? 180 : 140));
      g.fillRect(shift, 0, sw, sh);
      g.setComposite(prev);
    }
  }

  private static final String[] VIRUS_WORDS = {
      "ВЫХОД", "СМЕРТЬ", "ПЛЕН", "ПЕТЛЯ", "ВОЛК", "ГЛАЗ", "КРОВЬ", "ТИШИНА",
      "ЛОЖЬ", "СТРАХ", "ИМЯ", "СОН", "РЁВ", "КОСТИ", "ТЕНЬ", "ГОЛОД"
  };

  /**
   * Вирусный занавес: слова (ВЫХОД/СМЕРТЬ/ПЛЕН…) + буквы/цифры, на пике экран глухой.
   */
  public static void drawVirusWordCurtain(Graphics2D g, int sw, int sh, float intensity, long seedMs) {
    if (g == null || intensity <= 0.01f) {
      return;
    }
    float clamped = Math.max(0f, Math.min(1f, intensity));
    drawDigitalRain(g, sw, sh, clamped, seedMs, 0.55f + clamped * 0.55f);

    if (clamped > 0.45f) {
      float veil = (clamped - 0.45f) / 0.55f;
      g.setColor(new Color(10, 0, 0, Math.round(40 + veil * 160)));
      g.fillRect(0, 0, sw, sh);
    }

    Random rnd = new Random(seedMs * 53L + animFrame * 11L);
    int wordCount = Math.round(8 + clamped * 70);
    int baseSize = Math.max(10, Math.round(sh * 0.028f));
    for (int i = 0; i < wordCount; i++) {
      String word = VIRUS_WORDS[rnd.nextInt(VIRUS_WORDS.length)];
      float scale = 0.7f + rnd.nextFloat() * (0.8f + clamped * 2.2f);
      int size = Math.max(9, Math.round(baseSize * scale));
      g.setFont(new Font(Font.MONOSPACED, Font.BOLD, size));
      FontMetrics fm = g.getFontMetrics();
      int tw = fm.stringWidth(word);
      int x = rnd.nextInt(Math.max(1, sw + tw)) - tw / 2;
      int y = rnd.nextInt(Math.max(1, sh));
      boolean red = rnd.nextFloat() < 0.75f;
      int alpha = Math.round(70 + clamped * 185);
      g.setColor(red
          ? new Color(220, 25, 30, alpha)
          : new Color(230, 230, 235, alpha));
      g.drawString(word, x, y);
    }
  }

  /**
   * Цифры/буквы/нули-единицы поверх статики.
   * densityScale &lt; 1 — слабее прошлого «полного забития», но всё равно почти ничего не видно.
   */
  public static void drawDigitalRain(Graphics2D g, int sw, int sh, float intensity, long seedMs,
                                     float densityScale) {
    if (g == null || intensity <= 0.01f) {
      return;
    }
    float clamped = Math.max(0f, Math.min(1f, intensity));
    float dens = Math.max(0.15f, Math.min(1f, densityScale));
    drawTvStatic(g, sw, sh, clamped * 0.85f, seedMs);

    Random rnd = new Random(seedMs * 97L + animFrame * 31L);
    int fontSize = Math.max(8, Math.round(sh * 0.035f));
    Font font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    int stepX = Math.max(fm.charWidth('0') + 2, 8);
    int stepY = Math.max(fm.getHeight(), 10);
    int cols = Math.max(1, sw / stepX);
    int rows = Math.max(1, sh / stepY);
    int count = Math.round(cols * rows * (0.18f + clamped * 0.55f) * dens);

    Composite prev = g.getComposite();
    for (int i = 0; i < count; i++) {
      int cx = rnd.nextInt(cols) * stepX;
      int cy = rnd.nextInt(rows) * stepY + fm.getAscent();
      char ch = CODE_CHARS[rnd.nextInt(CODE_CHARS.length)];
      boolean red = rnd.nextFloat() < 0.65f;
      int alpha = Math.round(90 + clamped * 150);
      g.setColor(red
          ? new Color(220, 40, 40, alpha)
          : new Color(220, 220, 225, alpha));
      g.drawString(String.valueOf(ch), cx, cy);
    }
    g.setComposite(prev);
  }

  private static void ensure(int sw, int sh) {
    if (baked != null && cachedW == sw && cachedH == sh) {
      return;
    }
    cachedW = sw;
    cachedH = sh;
    animFrame = 0;
    baked = new BufferedImage[6];
    for (int i = 0; i < baked.length; i++) {
      baked[i] = bakeRedStatic(sw, sh, 900 + i * 113);
    }
  }

  private static BufferedImage bakeRedStatic(int sw, int sh, int seed) {
    BufferedImage img = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
    int[] px = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    Random r = new Random(seed);
    int count = Math.max(1, Math.round(sw * sh * 0.006f));
    for (int i = 0; i < count; i++) {
      int idx = r.nextInt(px.length);
      int pick = r.nextInt(4);
      int alpha = 50 + r.nextInt(180);
      int rgb = switch (pick) {
        case 0 -> 0xE02020;
        case 1 -> 0xF0F0F4;
        default -> 0x000000;
      };
      px[idx] = (alpha << 24) | rgb;
    }
    return img;
  }
}
