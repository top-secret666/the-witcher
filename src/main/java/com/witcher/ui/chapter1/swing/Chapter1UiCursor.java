package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Курсор главы 1 (системный скрыт, рисуем поверх пост-обработки). */
public final class Chapter1UiCursor {

  private static final BufferedImage MENU_CURSOR = loadMenuCursor();

  private Chapter1UiCursor() {
  }

  public static void draw(Graphics2D g, int mouseX, int mouseY) {
    if (MENU_CURSOR == null) {
      return;
    }
    int cw = 28;
    int ch = Math.max(1, cw * MENU_CURSOR.getHeight() / MENU_CURSOR.getWidth());
    Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.drawImage(MENU_CURSOR, mouseX - 4, mouseY - 4, cw, ch, null);
    if (prevInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
    }
  }

  private static BufferedImage loadMenuCursor() {
    Sprite s = Sprite.loadOptional("/assets/sprites/menu/menu_cursor.png");
    return s != null ? s.getImage() : null;
  }
}
