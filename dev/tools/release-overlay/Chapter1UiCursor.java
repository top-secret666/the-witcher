package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.graphics.MenuCursorDraw;
import main.java.com.witcher.ui.graphics.MenuCursorPaths;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Курсор главы 1 (системный скрыт, рисуем поверх пост-обработки и кнопки паузы).
 * UI лавки — крупный 28px; VN — {@link MenuCursorDraw#drawIntro}.
 */
public final class Chapter1UiCursor {

  private static final int SHOP_W = 28;
  private static final int SHOP_HOTSPOT = 4;
  private static final BufferedImage SHOP_CURSOR = loadShopCursor();

  private Chapter1UiCursor() {
  }

  /** Лавка / карта / HUD — тот же размер, что раньше в ShopSwingView. */
  public static void draw(Graphics2D g, int mouseX, int mouseY) {
    if (SHOP_CURSOR != null) {
      int ch = Math.max(1, SHOP_W * SHOP_CURSOR.getHeight() / SHOP_CURSOR.getWidth());
      Object prev = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      g.drawImage(SHOP_CURSOR, mouseX - SHOP_HOTSPOT, mouseY - SHOP_HOTSPOT, SHOP_W, ch, null);
      if (prev != null) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prev);
      }
      return;
    }
    MenuCursorDraw.drawLarge(g, mouseX, mouseY);
  }

  public static void drawDialog(Graphics2D g, int mouseX, int mouseY) {
    MenuCursorDraw.drawIntro(g, mouseX, mouseY);
  }

  private static BufferedImage loadShopCursor() {
    Sprite s = Sprite.loadOptional(MenuCursorPaths.MENU_CURSOR);
    return s != null ? s.getImage() : null;
  }
}

