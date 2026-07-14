package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Заставка карты боя перед пробуждением. */
public final class BossMapSplashView {

  private static final int SPLASH_TICKS = 120;

  private BossMapSplashView() {
  }

  public static int splashTicks() {
    return SPLASH_TICKS;
  }

  public static boolean isComplete(int ticks) {
    return ticks >= SPLASH_TICKS;
  }

  public static void draw(Graphics2D g, int sw, int sh, int ticks) {
    g.setColor(new Color(6, 4, 3));
    g.fillRect(0, 0, sw, sh);

    float t = Math.min(1f, ticks / (float) SPLASH_TICKS);
    float openT = easeOutCubic(Math.min(1f, t * 1.35f));

    BufferedImage closed = Chapter1UiAssets.cardClosed();
    BufferedImage open = Chapter1UiAssets.bossMapOpen();
    if (closed != null && open != null) {
      BufferedImage closedScaled = ScaledImageCache.get(closed, sw, sh);
      BufferedImage openScaled = ScaledImageCache.get(open, sw, sh);
      g.drawImage(closedScaled, 0, 0, null);
      var prev = g.getComposite();
      g.setComposite(java.awt.AlphaComposite.getInstance(
          java.awt.AlphaComposite.SRC_OVER, openT));
      g.drawImage(openScaled, 0, 0, null);
      g.setComposite(prev);
    } else if (open != null) {
      g.drawImage(ScaledImageCache.get(open, sw, sh), 0, 0, null);
    }

    g.setFont(GameFonts.get().uiBold(14));
    g.setColor(new Color(240, 210, 150, Math.round(255 * easeOutCubic(t))));
    g.drawString("Карта боя", 18, sh - 28);
    g.setFont(GameFonts.get().uiPlain(9));
    g.setColor(new Color(180, 160, 130, Math.round(200 * t)));
    g.drawString("Пробуждение…", 18, sh - 12);
  }

  private static float easeOutCubic(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return 1f - (float) Math.pow(1f - c, 3);
  }
}
