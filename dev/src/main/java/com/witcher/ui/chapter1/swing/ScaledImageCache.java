package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.graphics.PixelScaler;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Кэш даунскейла без пирамиды halfQuality (она жрёт heap на крупных PNG).
 * Один проход drawImage → цель.
 */
public final class ScaledImageCache {

  private static final int MAX_ENTRIES = 32;
  private static final int MAX_DST_EDGE = 960;

  private static final Map<Long, BufferedImage> CACHE = new LinkedHashMap<>(MAX_ENTRIES + 1, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, BufferedImage> eldest) {
      return size() > MAX_ENTRIES;
    }
  };

  private ScaledImageCache() {
  }

  public static synchronized BufferedImage get(BufferedImage src, int w, int h) {
    if (src == null || w <= 0 || h <= 0) {
      return null;
    }
    int dw = Math.min(w, MAX_DST_EDGE);
    int dh = Math.min(h, MAX_DST_EDGE);
    if (src.getWidth() == dw && src.getHeight() == dh) {
      return src;
    }
    long key = key(src, dw, dh);
    BufferedImage cached = CACHE.get(key);
    if (cached != null) {
      return cached;
    }
    BufferedImage scaled = scaleOnce(src, dw, dh);
    CACHE.put(key, scaled);
    return scaled;
  }

  private static BufferedImage scaleOnce(BufferedImage src, int dstW, int dstH) {
    try {
      BufferedImage out = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_ARGB);
      Graphics2D g = out.createGraphics();
      try {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.drawImage(src, 0, 0, dstW, dstH, null);
      } finally {
        g.dispose();
      }
      return out;
    } catch (OutOfMemoryError oom) {
      CACHE.clear();
      System.gc();
      return PixelScaler.smoothScale(src, dstW, dstH);
    }
  }

  private static long key(BufferedImage src, int w, int h) {
    return ((long) System.identityHashCode(src) << 32) ^ (((long) w & 0xffffL) << 16) ^ (h & 0xffffL);
  }
}
