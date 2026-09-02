package main.java.com.witcher.ui.graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** EXE overlay: crisp menu cursor (1x bake) at fixed virtual sizes. */
public final class MenuCursorDraw {

  public static final int LARGE_W = 12;
  public static final int SMALL_W = 7;
  private static final int HOTSPOT_X = 1;
  private static final int HOTSPOT_Y = 1;

  private static final BufferedImage CURSOR = loadCursor();

  private MenuCursorDraw() {
  }

  public static void drawLarge(Graphics2D g, int mouseX, int mouseY) {
    draw(g, mouseX, mouseY, LARGE_W);
  }

  public static void drawSmall(Graphics2D g, int mouseX, int mouseY) {
    draw(g, mouseX, mouseY, SMALL_W);
  }

  private static void draw(Graphics2D g, int mouseX, int mouseY, int cw) {
    if (CURSOR == null) {
      return;
    }
    int ch = Math.max(1, cw * CURSOR.getHeight() / CURSOR.getWidth());
    Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    PixelDraw.applyNearest(g);
    g.drawImage(CURSOR, mouseX - HOTSPOT_X, mouseY - HOTSPOT_Y, cw, ch, null);
    if (prevInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
    }
  }

  private static BufferedImage loadCursor() {
    Sprite baked = Sprite.loadOptional(MenuCursorPaths.MENU_CURSOR_1X);
    if (baked != null) {
      return baked.getImage();
    }
    Sprite fallback = Sprite.loadOptional(MenuCursorPaths.MENU_CURSOR);
    return fallback != null ? fallback.getImage() : null;
  }
}
