package main.java.com.witcher.ui.chapter1.swing.glitch;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/** Пиксельный шум поверх GIF — заранее запечённые кадры, без fillRect на каждый пиксель. */
public final class CutsceneNoiseOverlay {

  private static final int FRAME_COUNT = 8;

  private static BufferedImage[] frames;
  private static int cachedW;
  private static int cachedH;
  private static int frameIndex;

  private CutsceneNoiseOverlay() {
  }

  public static void draw(Graphics2D g, int sw, int sh, float strength) {
    if (g == null || strength <= 0f || sw <= 0 || sh <= 0) {
      return;
    }
    ensureFrames(sw, sh);
    if (frames == null || frames.length == 0) {
      return;
    }
    var prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, strength * 0.55f));
    g.drawImage(frames[frameIndex], 0, 0, null);
    g.setComposite(prev);
    frameIndex = (frameIndex + 1) % frames.length;
  }

  private static void ensureFrames(int sw, int sh) {
    if (frames != null && cachedW == sw && cachedH == sh) {
      return;
    }
    cachedW = sw;
    cachedH = sh;
    frameIndex = 0;
    frames = new BufferedImage[FRAME_COUNT];
    for (int f = 0; f < FRAME_COUNT; f++) {
      frames[f] = bakeNoiseFrame(sw, sh, 31 + f * 97);
    }
  }

  private static BufferedImage bakeNoiseFrame(int sw, int sh, int seed) {
    BufferedImage img = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
    int[] pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    Random r = new Random(seed);
    int density = Math.max(1, Math.round(sw * sh * 0.0025f));
    for (int i = 0; i < density; i++) {
      int idx = r.nextInt(pixels.length);
      int tone = 90 + r.nextInt(80);
      int alpha = 28 + r.nextInt(40);
      pixels[idx] = (alpha << 24) | (tone << 16) | (tone << 8) | tone;
    }
    if (r.nextFloat() < 0.35f) {
      int bandY = r.nextInt(sh);
      int row = bandY * sw;
      int red = (35 << 24) | (200 << 16) | (60 << 8) | 60;
      for (int x = 0; x < sw; x++) {
        pixels[row + x] = red;
      }
    }
    return img;
  }
}
