package main.java.com.witcher.ui.graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Курсор меню / VN: 1x bake для UI, полный ассет как в {@code IntroScreen} для диалогов. */
public final class MenuCursorDraw {

  public static final int LARGE_W = 12;
  public static final int SMALL_W = 7;
  /** Как {@link main.java.com.witcher.ui.graphics.IntroScreen#drawCursor}. */
  public static final int INTRO_W = 16;
  private static final int HOTSPOT_X = 1;
  private static final int HOTSPOT_Y = 1;
  private static final int INTRO_HOTSPOT = 4;

  private static final BufferedImage CURSOR_1X = loadCursor1x();
  private static final BufferedImage CURSOR_INTRO = loadCursorIntro();

  private MenuCursorDraw() {
  }

  public static void drawLarge(Graphics2D g, int mouseX, int mouseY) {
    draw1x(g, mouseX, mouseY, LARGE_W);
  }

  public static void drawSmall(Graphics2D g, int mouseX, int mouseY) {
    draw1x(g, mouseX, mouseY, SMALL_W);
  }

  /** Диалоги интро / брифинг / Волк / Весемир — 1:1 с IntroScreen. */
  public static void drawIntro(Graphics2D g, int mouseX, int mouseY) {
    BufferedImage cursor = CURSOR_INTRO != null ? CURSOR_INTRO : CURSOR_1X;
    if (cursor == null) {
      return;
    }
    int cw = INTRO_W;
    int ch = Math.max(1, cw * cursor.getHeight() / cursor.getWidth());
    Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    PixelDraw.applyNearest(g);
    g.drawImage(cursor, mouseX - INTRO_HOTSPOT, mouseY - INTRO_HOTSPOT, cw, ch, null);
    if (prevInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
    }
  }

  private static void draw1x(Graphics2D g, int mouseX, int mouseY, int cw) {
    if (CURSOR_1X == null) {
      return;
    }
    int ch = Math.max(1, cw * CURSOR_1X.getHeight() / CURSOR_1X.getWidth());
    Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    PixelDraw.applyNearest(g);
    g.drawImage(CURSOR_1X, mouseX - HOTSPOT_X, mouseY - HOTSPOT_Y, cw, ch, null);
    if (prevInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
    }
  }

  private static BufferedImage loadCursor1x() {
    Sprite baked = Sprite.loadOptional(MenuCursorPaths.MENU_CURSOR_1X);
    if (baked != null) {
      return baked.getImage();
    }
    Sprite fallback = Sprite.loadOptional(MenuCursorPaths.MENU_CURSOR);
    return fallback != null ? fallback.getImage() : null;
  }

  private static BufferedImage loadCursorIntro() {
    Sprite s = Sprite.loadOptional(MenuCursorPaths.MENU_CURSOR);
    return s != null ? s.getImage() : null;
  }
}
