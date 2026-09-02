package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.graphics.PixelScaler;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Рендер кадра катсцены с прогрессивной резкостью (эффект пробуждения). */
public final class WakeVisionRenderer {

  private static BufferedImage lastSrc;
  private static int lastBucket = -1;
  private static BufferedImage lastSoft;

  private WakeVisionRenderer() {
  }

  public static void drawFrame(Graphics2D g, BufferedImage frame, int dx, int dy, float sharpness) {
    if (g == null || frame == null) {
      return;
    }
    if (sharpness >= 0.97f) {
      g.drawImage(frame, dx, dy, null);
      return;
    }
    int bucket = Math.round(sharpness * 8f);
    BufferedImage soft = cachedSoft(frame, bucket, sharpness);
    g.drawImage(soft, dx, dy, null);

    if (sharpness < 0.55f) {
      float fog = (0.55f - sharpness) * 0.45f;
      var prev = g.getComposite();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fog));
      g.setColor(new Color(4, 3, 2));
      g.fillRect(dx, dy, frame.getWidth(), frame.getHeight());
      g.setComposite(prev);
    }
  }

  private static BufferedImage cachedSoft(BufferedImage frame, int bucket, float sharpness) {
    if (frame == lastSrc && bucket == lastBucket && lastSoft != null) {
      return lastSoft;
    }
    lastSrc = frame;
    lastBucket = bucket;
    int w = frame.getWidth();
    int h = frame.getHeight();
    float blur = 1f - Math.max(0f, Math.min(1f, sharpness));
    int down = Math.max(2, Math.round(2 + blur * 5f));
    int dw = Math.max(1, w / down);
    int dh = Math.max(1, h / down);
    lastSoft = PixelScaler.smoothScale(PixelScaler.smoothScale(frame, dw, dh), w, h);
    return lastSoft;
  }
}
