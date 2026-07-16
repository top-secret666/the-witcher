package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/**
 * Единая палитра глитч/horror-сцен главы 1 под стиль игры:
 * чёрный «мокрый камень», холодный стальной серо-синий, кровь, RGB-помехи.
 * Накладывается при загрузке ассетов — без правок исходных PNG на диске.
 */
public final class WitcherGlitchPalette {

  // Целевые якоря палитры (как в глитч-кадрах + Каэр Морхен).
  private static final int[] ANCHORS = {
      0x000000, // void
      0x0A0C10, // wet stone
      0x1A2230, // steel shadow
      0x3A4658, // cold steel
      0x6A7888, // mist gray
      0x8A1A1A, // blood
      0xC82828, // bright blood
      0x1A8A3A, // glitch green
      0x28C8C8, // glitch cyan
      0xE8E8F0  // eye white / bloom
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

      // Сначала лёгкий «холодный» сдвиг, потом snap к якорям.
      int nr = clamp((int) (r * 0.92f + b * 0.05f));
      int ng = clamp((int) (g * 0.88f + b * 0.08f));
      int nb = clamp((int) (b * 1.05f + r * 0.02f));

      int mapped = nearestAnchor(nr, ng, nb);
      // Смешиваем 70% якорь + 30% холодный оригинал — не теряем детали.
      int mr = ((mapped >>> 16) & 0xff);
      int mg = ((mapped >>> 8) & 0xff);
      int mb = mapped & 0xff;
      nr = (mr * 7 + nr * 3) / 10;
      ng = (mg * 7 + ng * 3) / 10;
      nb = (mb * 7 + nb * 3) / 10;
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
