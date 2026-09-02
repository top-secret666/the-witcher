package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.graphics.MenuCursorDraw;

import java.awt.Graphics2D;

/** EXE overlay: 28px UI cursor, 16px VN dialog cursor. */
public final class Chapter1UiCursor {

  private Chapter1UiCursor() {
  }

  public static void draw(Graphics2D g, int mouseX, int mouseY) {
    MenuCursorDraw.drawLarge(g, mouseX, mouseY);
  }

  public static void drawSmall(Graphics2D g, int mouseX, int mouseY) {
    MenuCursorDraw.drawSmall(g, mouseX, mouseY);
  }
}
