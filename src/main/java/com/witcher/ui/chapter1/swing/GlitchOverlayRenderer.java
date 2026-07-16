package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.ui.chapter1.view.Chapter1AssetPaths;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Оверлей глюка по уровню подозрения + forced-режимы для катсцен. */
public final class GlitchOverlayRenderer {

  private static BufferedImage heavy;
  private static BufferedImage medium;

  private GlitchOverlayRenderer() {
  }

  public static void draw(Graphics2D g, int sw, int sh, Chapter1Session session) {
    if (session == null || g == null) {
      return;
    }
    BufferedImage overlay = switch (session.glitchLevel()) {
      case HEAVY -> cachedHeavy();
      case MEDIUM -> cachedMedium();
      case NONE, LIGHT -> null;
    };
    if (overlay == null) {
      return;
    }
    var prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
    g.drawImage(overlay, 0, 0, sw, sh, null);
    g.setComposite(prev);
  }

  public static void drawHeavyForced(Graphics2D g, int sw, int sh) {
    drawOverlay(g, sw, sh, cachedHeavy(), 0.65f);
  }

  public static void drawMediumForced(Graphics2D g, int sw, int sh) {
    drawOverlay(g, sw, sh, cachedMedium(), 0.7f);
  }

  public static void drawMediumForced(Graphics2D g, int sw, int sh, float alpha) {
    drawOverlay(g, sw, sh, cachedMedium(), alpha);
  }

  public static void drawHeavyForced(Graphics2D g, int sw, int sh, float alpha) {
    drawOverlay(g, sw, sh, cachedHeavy(), alpha);
  }

  private static void drawOverlay(Graphics2D g, int sw, int sh, BufferedImage overlay, float alpha) {
    if (g == null || overlay == null || alpha <= 0.01f) {
      return;
    }
    float a = Math.max(0f, Math.min(1f, alpha));
    var prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
    g.drawImage(overlay, 0, 0, sw, sh, null);
    g.setComposite(prev);
  }

  private static BufferedImage cachedHeavy() {
    if (heavy == null) {
      var sprite = Sprite.loadOptional(Chapter1AssetPaths.GLITCH_HEAVY);
      heavy = sprite != null ? WitcherGlitchPalette.apply(sprite.getImage()) : null;
    }
    return heavy;
  }

  private static BufferedImage cachedMedium() {
    if (medium == null) {
      var sprite = Sprite.loadOptional(Chapter1AssetPaths.GLITCH_MEDIUM);
      medium = sprite != null ? WitcherGlitchPalette.apply(sprite.getImage()) : null;
    }
    return medium;
  }
}
