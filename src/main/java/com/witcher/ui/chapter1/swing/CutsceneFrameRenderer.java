package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Только отрисовка кадра катсцены — без playback-логики. */
public final class CutsceneFrameRenderer {

  private CutsceneFrameRenderer() {
  }

  public static void paintFrame(Graphics2D g, BufferedImage frame, int dx, int dy) {
    if (g == null || frame == null) {
      return;
    }
    g.drawImage(frame, dx, dy, null);
  }

  public static void paintWakeFrame(
      Graphics2D g, BufferedImage frame, int dx, int dy, float sharpness) {
    if (g == null || frame == null) {
      return;
    }
    WakeVisionRenderer.drawFrame(g, frame, dx, dy, sharpness);
  }
}
