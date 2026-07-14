package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.graphics.PixelScaler;

import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

/** Кэш даунскейла — sharpScale не вызывается каждый кадр. */
public final class ScaledImageCache {

  private static final int MAX_ENTRIES = 48;

  private static final Map<Long, BufferedImage> CACHE = new LinkedHashMap<>(MAX_ENTRIES + 1, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<Long, BufferedImage> eldest) {
      return size() > MAX_ENTRIES;
    }
  };

  private ScaledImageCache() {
  }

  public static BufferedImage get(BufferedImage src, int w, int h) {
    if (src == null || w <= 0 || h <= 0) {
      return null;
    }
    if (src.getWidth() == w && src.getHeight() == h) {
      return src;
    }
    long key = key(src, w, h);
    return CACHE.computeIfAbsent(key, k -> PixelScaler.sharpScale(src, w, h));
  }

  private static long key(BufferedImage src, int w, int h) {
    return ((long) System.identityHashCode(src) << 32) ^ ((long) w << 16) ^ h;
  }
}
