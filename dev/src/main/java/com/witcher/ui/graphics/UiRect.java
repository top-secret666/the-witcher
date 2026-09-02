package main.java.com.witcher.ui.graphics;

import java.awt.Rectangle;

/** Конвертация float-rect UI в AWT Rectangle. */
public final class UiRect {

  private UiRect() {
  }

  public static Rectangle toAwt(float x, float y, float width, float height) {
    return new Rectangle(Math.round(x), Math.round(y), Math.round(width), Math.round(height));
  }
}
