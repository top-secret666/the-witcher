package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.view.Chapter1AssetPaths;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Оверлей глюка по уровню подозрения. */
public final class GlitchOverlayRenderer {

  private static BufferedImage light;
  private static BufferedImage medium;
  private static BufferedImage heavy;

  private GlitchOverlayRenderer() {
  }

  public static void draw(Graphics2D g, int sw, int sh, Chapter1Session session) {
    if (session == null || g == null) {
      return;
    }
    BufferedImage overlay = switch (session.glitchLevel()) {
      case LIGHT -> cachedLight();
      case MEDIUM -> cachedMedium();
      case HEAVY -> cachedHeavy();
      case NONE -> null;
    };
    if (overlay == null) {
      return;
    }
    var prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
    g.drawImage(overlay, 0, 0, sw, sh, null);
    g.setComposite(prev);
  }

  private static BufferedImage cachedLight() {
    if (light == null) {
      var sprite = Sprite.loadOptional(Chapter1AssetPaths.GLITCH_LIGHT);
      light = sprite != null ? sprite.getImage() : null;
    }
    return light;
  }

  private static BufferedImage cachedMedium() {
    if (medium == null) {
      var sprite = Sprite.loadOptional(Chapter1AssetPaths.GLITCH_MEDIUM);
      medium = sprite != null ? sprite.getImage() : null;
    }
    return medium;
  }

  private static BufferedImage cachedHeavy() {
    if (heavy == null) {
      var sprite = Sprite.loadOptional(Chapter1AssetPaths.GLITCH_HEAVY);
      heavy = sprite != null ? sprite.getImage() : null;
    }
    return heavy;
  }
}
