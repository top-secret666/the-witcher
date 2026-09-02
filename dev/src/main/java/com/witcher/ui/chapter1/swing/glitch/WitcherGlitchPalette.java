package main.java.com.witcher.ui.chapter1.swing.glitch;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Палитра глитч/horror главы 1: мокрый камень, холодная сталь, кровь.
 * Кислотный neon (lime/cyan) глушится; глаза/улыбка (светлые блики) сохраняются и усиливаются.
 */
public final class WitcherGlitchPalette {

  private static final int[] ANCHORS = {
      0x000000, // void
      0x080A0E, // wet stone
      0x141820, // steel shadow
      0x2A3444, // cold steel
      0x4A5668, // mist gray
      0x6E7888, // pale steel
      0x6A1418, // dried blood
      0xA82028, // blood
      0xC8C8D4, // soft highlight
      0xF2F2F8  // eye / smile white
  };

  private WitcherGlitchPalette() {
  }

  public static BufferedImage apply(BufferedImage src) {
    if (src == null) {
      return null;
    }
    BufferedImage argb = toArgb(src);
    int w = argb.getWidth();
    int h = argb.getHeight();
    BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
    int[] px = ((DataBufferInt) argb.getRaster().getDataBuffer()).getData();
    int[] dst = ((DataBufferInt) out.getRaster().getDataBuffer()).getData();

    for (int i = 0; i < px.length; i++) {
      int c = px[i];
      int a = (c >>> 24) & 0xff;
      if (a < 8) {
        dst[i] = c;
        continue;
      }
      int r = (c >>> 16) & 0xff;
      int g = (c >>> 8) & 0xff;
      int b = c & 0xff;

      int max = Math.max(r, Math.max(g, b));
      int min = Math.min(r, Math.min(g, b));
      float lum = (r + g + b) / 3f;
      float sat = max == 0 ? 0f : (max - min) / (float) max;

      // Глаза / улыбка: яркие малонасыщенные — оставляем и чуть белим.
      if (lum >= 165f && sat <= 0.38f) {
        int boost = clamp((int) (lum + (255 - lum) * 0.45f));
        dst[i] = (a << 24) | (boost << 16) | (boost << 8) | Math.min(255, boost + 4);
        continue;
      }

      // Кислотный зелёный / лайм — в холодную сталь.
      if (g > r + 18 && g > b + 12 && sat > 0.28f) {
        int steel = clamp((int) (lum * 0.55f));
        r = steel;
        g = clamp(steel + 4);
        b = clamp(steel + 14);
        sat = 0.2f;
      }

      // Кислотный cyan — в приглушённый стальной синий.
      if (b > r + 22 && g > r + 10 && sat > 0.28f) {
        int steel = clamp((int) (lum * 0.5f));
        r = clamp(steel - 4);
        g = clamp(steel + 6);
        b = clamp(steel + 18);
      }

      // Общий холодный сдвиг + десатурация неона.
      float desat = sat > 0.55f ? 0.55f : 1f;
      int avg = (r + g + b) / 3;
      int nr = clamp((int) (r * desat + avg * (1f - desat) * 0.35f + b * 0.04f));
      int ng = clamp((int) (g * desat * 0.9f + avg * (1f - desat) * 0.4f));
      int nb = clamp((int) (b * 1.06f + avg * 0.05f));

      int mapped = nearestAnchor(nr, ng, nb);
      int mr = (mapped >>> 16) & 0xff;
      int mg = (mapped >>> 8) & 0xff;
      int mb = mapped & 0xff;
      // Сильнее якорь (80%) — меньше кислоты от оригинала.
      nr = (mr * 8 + nr * 2) / 10;
      ng = (mg * 8 + ng * 2) / 10;
      nb = (mb * 8 + nb * 2) / 10;
      dst[i] = (a << 24) | (nr << 16) | (ng << 8) | nb;
    }
    return out;
  }

  private static int nearestAnchor(int r, int g, int b) {
    int best = ANCHORS[0];
    int bestDist = Integer.MAX_VALUE;
    for (int anchor : ANCHORS) {
      int ar = (anchor >>> 16) & 0xff;
      int ag = (anchor >>> 8) & 0xff;
      int ab = anchor & 0xff;
      int dr = r - ar;
      int dg = g - ag;
      int db = b - ab;
      int dist = dr * dr + dg * dg + db * db;
      if (dist < bestDist) {
        bestDist = dist;
        best = anchor;
      }
    }
    return best;
  }

  private static BufferedImage toArgb(BufferedImage src) {
    if (src.getType() == BufferedImage.TYPE_INT_ARGB) {
      return src;
    }
    BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = out.createGraphics();
    try {
      g.drawImage(src, 0, 0, null);
    } finally {
      g.dispose();
    }
    return out;
  }

  private static int clamp(int v) {
    return Math.max(0, Math.min(255, v));
  }
}
