package main.java.com.witcher.ui.chapter1.swing.battle.encounter;

import main.java.com.witcher.chapter1.assets.Chapter1AssetPaths;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Fullscreen montage воспоминаний — только эпилог с осколком перед глитч-заставкой. */
public final class EncounterSceneRenderer {

  private EncounterSceneRenderer() {
  }

  public static void drawFullBleed(Graphics2D g, int sw, int sh, String imagePath, float alpha) {
    if (imagePath == null || alpha <= 0.01f) {
      return;
    }
    BufferedImage img = Chapter1UiAssets.encounterMemoryImage(imagePath);
    if (img == null) {
      return;
    }
    float cover = Math.max((float) sw / img.getWidth(), (float) sh / img.getHeight());
    int drawW = Math.round(img.getWidth() * cover);
    int drawH = Math.round(img.getHeight() * cover);
    int drawX = (sw - drawW) / 2;
    int drawY = (sh - drawH) / 2;

    Composite prev = g.getComposite();
    if (alpha < 0.999f) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }
    drawSharp(g, img, drawX, drawY, drawW, drawH);
    g.setComposite(prev);
  }

  public static void drawFullBleedMontage(Graphics2D g, int sw, int sh) {
    drawFullBleed(g, sw, sh, Chapter1AssetPaths.MEMORY_ARD_CARRAIG, 0.55f);
    drawFullBleed(g, sw, sh, Chapter1AssetPaths.MEMORY_KAER_MORHEN, 0.92f);
    g.setColor(new Color(0, 0, 0, 80));
    g.fillRect(0, 0, sw, sh);
  }

  private static void drawSharp(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
    Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    Object prevRender = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(img, x, y, w, h, null);
    if (prevInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
    } else {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }
    if (prevRender != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRender);
    } else {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }
  }
}
