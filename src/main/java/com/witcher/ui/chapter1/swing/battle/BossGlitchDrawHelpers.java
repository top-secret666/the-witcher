package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.battle.glitch.BossGlitchRevealTimeline;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

/** Общие draw-хелперы для BossGlitchRevealView (full-bleed, sheet, shake). */
public final class BossGlitchDrawHelpers {

  static final int SHAKE_PAD = 12;

  private BossGlitchDrawHelpers() {
  }

  public static void drawFullBleed(Graphics2D g, BufferedImage img, int sw, int sh, float alpha) {
    drawFullBleedShaken(g, img, sw, sh, alpha, 0, 0);
  }

  public static void drawFullBleedShaken(Graphics2D g, BufferedImage img, int sw, int sh,
                                         float alpha, int shakeX, int shakeY) {
    if (img == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }
    Composite prev = g.getComposite();
    if (alpha < 0.999f) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
    }
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.drawImage(img, shakeX - SHAKE_PAD, shakeY - SHAKE_PAD, sw + SHAKE_PAD * 2, sh + SHAKE_PAD * 2, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }

  public static void drawSheetFrameBright(Graphics2D g, int sw, int sh, int frameIndex,
                                          float alpha, int shakeX, int shakeY) {
    BufferedImage sheet = Chapter1UiAssets.bossGlitchAwakenSheet();
    if (sheet == null) {
      return;
    }
    int cols = BossGlitchRevealTimeline.SHEET_COLS;
    int rows = BossGlitchRevealTimeline.SHEET_ROWS;
    int cellW = sheet.getWidth() / cols;
    int cellH = sheet.getHeight() / rows;
    int col = frameIndex % cols;
    int row = frameIndex / cols;

    BufferedImage cell = sheet.getSubimage(col * cellW, row * cellH, cellW, cellH);
    BufferedImage argb = new BufferedImage(cellW, cellH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D cg = argb.createGraphics();
    try {
      cg.drawImage(cell, 0, 0, null);
    } finally {
      cg.dispose();
    }
    RescaleOp brighten = new RescaleOp(
        new float[] {1.4f, 1.4f, 1.4f, 1f},
        new float[] {10f, 10f, 10f, 0f},
        null);
    BufferedImage bright = brighten.filter(argb, null);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.drawImage(bright, shakeX - SHAKE_PAD, shakeY - SHAKE_PAD, sw + SHAKE_PAD * 2, sh + SHAKE_PAD * 2, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }

  public static void flashBands(Graphics2D g, int sw, int sh, float peak, long seed) {
    java.util.Random rnd = new java.util.Random(seed * 19L);
    int flashes = Math.round(2 + peak * 8);
    for (int i = 0; i < flashes; i++) {
      int y = rnd.nextInt(sh);
      int h = 2 + rnd.nextInt(Math.max(2, Math.round(sh * 0.08f * peak)));
      g.setColor(new Color(255, 255, 255, Math.round(40 + peak * 140)));
      g.fillRect(0, y, sw, h);
      if (rnd.nextBoolean()) {
        g.setColor(new Color(255, 40, 40, Math.round(50 + peak * 120)));
        g.fillRect(0, Math.min(sh - 1, y + h), sw, 1 + rnd.nextInt(3));
      }
    }
  }

  public static int dialogShakeX(long elapsedMs) {
    return Math.round((float) Math.sin(elapsedMs * 0.04) * 4f);
  }

  public static int dialogShakeY(long elapsedMs) {
    return Math.round((float) Math.sin(elapsedMs * 0.055 + 1.2) * 3f);
  }
}
