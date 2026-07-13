package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/** Лёгкий пиксельный шум поверх GIF-катсцены. */
public final class CutsceneNoiseOverlay {

  private static final Random RNG = new Random(31);

  private CutsceneNoiseOverlay() {
  }

  public static void draw(Graphics2D g, int sw, int sh, float strength) {
    if (g == null || strength <= 0f) {
      return;
    }
    int count = Math.round(sw * sh * 0.0025f * strength);
    for (int i = 0; i < count; i++) {
      int x = RNG.nextInt(sw);
      int y = RNG.nextInt(sh);
      int tone = 90 + RNG.nextInt(80);
      int alpha = Math.round(28 + 40 * strength);
      g.setColor(new Color(tone, tone, tone, alpha));
      g.fillRect(x, y, 1, 1);
    }
    if (RNG.nextFloat() < 0.12f * strength) {
      int bandY = RNG.nextInt(sh);
      g.setColor(new Color(200, 60, 60, Math.round(35 * strength)));
      g.fillRect(0, bandY, sw, 1);
    }
  }
}
