package main.java.com.witcher.ui.chapter1.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/** Процедурное «багование» экрана: блоки, полосы, хроматика — intensity 0..1. */
public final class PixelBugOverlay {

  private static BufferedImage[] baked;
  private static int cachedW;
  private static int cachedH;
  private static int animFrame;

  private PixelBugOverlay() {
  }

  public static void draw(Graphics2D g, int sw, int sh, float intensity, long seedMs) {
    if (g == null || intensity <= 0.01f || sw <= 0 || sh <= 0) {
      return;
    }
    ensure(sw, sh);
    float clamped = Math.max(0f, Math.min(1f, intensity));

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f + clamped * 0.55f));
    g.drawImage(baked[animFrame % baked.length], 0, 0, null);
    g.setComposite(prev);
    animFrame++;

    Random rnd = new Random(seedMs * 131L + animFrame * 17L);
    int blocks = Math.round(6 + clamped * 48);
    for (int i = 0; i < blocks; i++) {
      int bw = 4 + rnd.nextInt(Math.max(4, Math.round(sw * 0.08f * clamped)));
      int bh = 2 + rnd.nextInt(Math.max(2, Math.round(sh * 0.04f * clamped)));
      int bx = rnd.nextInt(Math.max(1, sw - bw));
      int by = rnd.nextInt(Math.max(1, sh - bh));
      int tone = rnd.nextInt(220);
      int alpha = Math.round(80 + clamped * 175);
      g.setColor(new Color(tone, tone, tone, alpha));
      g.fillRect(bx, by, bw, bh);
    }

    int bands = Math.round(2 + clamped * 10);
    for (int i = 0; i < bands; i++) {
      int by = rnd.nextInt(sh);
      int bh = 1 + rnd.nextInt(Math.max(1, Math.round(3 + clamped * 8)));
      Color c = switch (rnd.nextInt(3)) {
        case 0 -> new Color(255, 40, 40, Math.round(60 + clamped * 140));
        case 1 -> new Color(40, 255, 120, Math.round(50 + clamped * 120));
        default -> new Color(60, 120, 255, Math.round(50 + clamped * 120));
      };
      g.setColor(c);
      g.fillRect(0, by, sw, bh);
    }

    int shift = Math.round(clamped * 6);
    if (shift > 0) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f * clamped));
      g.setColor(new Color(255, 0, 0, 120));
      g.fillRect(shift, 0, sw, sh);
      g.setColor(new Color(0, 255, 255, 90));
      g.fillRect(-shift, 0, sw, sh);
      g.setComposite(prev);
    }
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
      baked[i] = bake(sw, sh, 900 + i * 113);
    }
  }

  private static BufferedImage bake(int sw, int sh, int seed) {
    BufferedImage img = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
    int[] px = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    Random r = new Random(seed);
    int count = Math.max(1, Math.round(sw * sh * 0.004f));
    for (int i = 0; i < count; i++) {
      int idx = r.nextInt(px.length);
      int tone = 40 + r.nextInt(200);
      int alpha = 40 + r.nextInt(180);
      px[idx] = (alpha << 24) | (tone << 16) | (tone << 8) | tone;
    }
    return img;
  }
}
