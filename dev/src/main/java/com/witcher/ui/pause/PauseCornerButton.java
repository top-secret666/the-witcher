package com.witcher.ui.pause;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Маленькая кнопка паузы в углу экрана. */
public final class PauseCornerButton {

  public enum Corner {
    TOP_LEFT,
    TOP_RIGHT
  }

  public static final int SIZE = 18;
  public static final int MARGIN = 8;

  private static final Color BG = new Color(28, 18, 8, 230);
  private static final Color BG_HOT = new Color(48, 32, 12, 240);
  private static final Color BORDER = new Color(160, 120, 50);
  private static final Color BORDER_HOT = new Color(230, 185, 80);
  private static final Color BAR = new Color(230, 200, 120);

  private PauseCornerButton() {
  }

  /** По умолчанию — левый верхний угол. */
  public static Rectangle bounds(int sw, int sh) {
    return bounds(sw, sh, Corner.TOP_LEFT, MARGIN);
  }

  public static Rectangle bounds(int sw, int sh, Corner corner) {
    return bounds(sw, sh, corner, MARGIN);
  }

  public static Rectangle bounds(int sw, int sh, Corner corner, int topY) {
    int x = corner == Corner.TOP_RIGHT ? sw - MARGIN - SIZE : MARGIN;
    return new Rectangle(x, Math.max(0, topY), SIZE, SIZE);
  }

  public static boolean hit(int sw, int sh, int mouseX, int mouseY) {
    return hit(sw, sh, mouseX, mouseY, Corner.TOP_LEFT, MARGIN);
  }

  public static boolean hit(int sw, int sh, int mouseX, int mouseY, Corner corner) {
    return hit(sw, sh, mouseX, mouseY, corner, MARGIN);
  }

  public static boolean hit(int sw, int sh, int mouseX, int mouseY, Corner corner, int topY) {
    return bounds(sw, sh, corner, topY).contains(mouseX, mouseY);
  }

  public static void draw(Graphics2D g, int sw, int sh, int mouseX, int mouseY) {
    draw(g, sw, sh, mouseX, mouseY, Corner.TOP_LEFT, MARGIN);
  }

  public static void draw(Graphics2D g, int sw, int sh, int mouseX, int mouseY, Corner corner) {
    draw(g, sw, sh, mouseX, mouseY, corner, MARGIN);
  }

  public static void draw(Graphics2D g, int sw, int sh, int mouseX, int mouseY,
                          Corner corner, int topY) {
    Rectangle r = bounds(sw, sh, corner, topY);
    boolean hot = r.contains(mouseX, mouseY);
    g.setColor(hot ? BG_HOT : BG);
    g.fillRoundRect(r.x, r.y, r.width, r.height, 4, 4);
    g.setColor(hot ? BORDER_HOT : BORDER);
    g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 4, 4);

    int barW = 3;
    int barH = 9;
    int gap = 3;
    int total = barW * 2 + gap;
    int bx = r.x + (r.width - total) / 2;
    int by = r.y + (r.height - barH) / 2;
    g.setColor(BAR);
    g.fillRect(bx, by, barW, barH);
    g.fillRect(bx + barW + gap, by, barW, barH);
  }
}
