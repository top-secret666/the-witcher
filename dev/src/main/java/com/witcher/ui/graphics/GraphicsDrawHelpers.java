package main.java.com.witcher.ui.graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Общие подсказки Java2D для отрисовки спрайтов. */
public final class GraphicsDrawHelpers {

  private GraphicsDrawHelpers() {
  }

  public static void drawBicubic(Graphics2D g, BufferedImage image, int x, int y, int w, int h) {
    if (image == null || w <= 0 || h <= 0) {
      return;
    }
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.drawImage(image, x, y, w, h, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
  }
}
