package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Session;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Катсцена боя: столкновения клинков, вспышки, тряска. */
public final class SwordCutsceneView {

  private SwordCutsceneView() {
  }

  public static void draw(
      BufferedImage screen,
      int sw,
      int sh,
      SwordGlintOverlay overlay,
      int shakeX,
      int shakeY,
      Chapter1Session session,
      boolean freezeGlitch) {
    Graphics2D g = screen.createGraphics();
    try {
      g.translate(shakeX, shakeY);
      overlay.render(g, sw, sh);
      g.translate(-shakeX, -shakeY);
      if (freezeGlitch) {
        GlitchOverlayRenderer.drawHeavyForced(g, sw, sh);
      }
    } finally {
      g.dispose();
    }
  }
}
