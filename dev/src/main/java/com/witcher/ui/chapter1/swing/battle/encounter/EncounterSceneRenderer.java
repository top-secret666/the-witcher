package main.java.com.witcher.ui.chapter1.swing.battle.encounter;

import main.java.com.witcher.chapter1.assets.Chapter1AssetPaths;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;

/** Fullscreen montage воспоминаний — только эпилог с осколком перед глитch-заставкой. */
public final class EncounterSceneRenderer {

  private EncounterSceneRenderer() {
  }

  /** Две картинки рядом на весь экран над полоской описания. */
  public static void drawFullBleedMontage(Graphics2D g, int sw, int sh) {
    int barH = Math.round(sh * 0.30f);
    int imageH = sh - barH;
    int gap = Math.max(2, Math.round(sw * 0.006f));
    int halfW = (sw - gap) / 2;

    drawCoverInRect(g, Chapter1AssetPaths.MEMORY_ARD_CARRAIG, 0, 0, halfW, imageH, 1f);
    drawCoverInRect(g, Chapter1AssetPaths.MEMORY_KAER_MORHEN, halfW + gap, 0, halfW, imageH, 1f);

    g.setColor(new Color(0, 0, 0, 55));
    g.fillRect(halfW, 0, gap, imageH);

    g.setColor(new Color(0, 0, 0, 45));
    g.fillRect(0, imageH - Math.round(imageH * 0.12f), sw, Math.round(imageH * 0.12f));
  }

  private static void drawCoverInRect(
      Graphics2D g,
      String imagePath,
      int rx,
      int ry,
      int rw,
      int rh,
      float alpha) {
    if (imagePath == null || rw <= 0 || rh <= 0 || alpha <= 0.01f) {
      return;
    }
    BufferedImage img = Chapter1UiAssets.encounterMemoryImage(imagePath);
    if (img == null) {
      return;
    }

    float cover = Math.max((float) rw / img.getWidth(), (float) rh / img.getHeight());
    int drawW = Math.round(img.getWidth() * cover);
    int drawH = Math.round(img.getHeight() * cover);
    int drawX = rx + (rw - drawW) / 2;
    int drawY = ry + (rh - drawH) / 2;

    Shape prevClip = g.getClip();
    Composite prevComposite = g.getComposite();
    g.clipRect(rx, ry, rw, rh);
    if (alpha < 0.999f) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }
    drawSharp(g, img, drawX, drawY, drawW, drawH);
    g.setComposite(prevComposite);
    g.setClip(prevClip);
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
