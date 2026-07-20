package main.java.com.witcher.ui.chapter1.swing.battle.encounter;

import main.java.com.witcher.chapter1.assets.Chapter1AssetPaths;
import main.java.com.witcher.chapter1.battle.encounter.BossEncounterController;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Портрет / воспоминание над диалоговым окном — кросс-фейд и лёгкий Ken Burns. */
public final class EncounterSceneRenderer {

  private static final float KEN_BURNS_ZOOM = 0.022f;
  private static final float KEN_BURNS_PAN_X = 0.014f;
  private static final float KEN_BURNS_PAN_Y = 0.009f;

  private EncounterSceneRenderer() {
  }

  public static void drawSceneImage(Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    if (encounter == null || !encounter.useSceneImage()) {
      return;
    }

    String fromPath = encounter.sceneImageFrom();
    String toPath = encounter.sceneImageTo();
    float fadeT = encounter.sceneCrossfadeT();
    boolean kenBurns = encounter.sceneKenBurnsDrift();
    float kenPhase = encounter.sceneKenBurnsPhase();

    int dialogZone = Math.round(sh * 0.15f);
    int areaH = sh - dialogZone;
    int areaW = sw;
    int areaX = 0;
    int areaY = 0;

    BufferedImage fromImg = Chapter1UiAssets.encounterSceneImage(fromPath);
    BufferedImage toImg = Chapter1UiAssets.encounterSceneImage(toPath);

    if (fadeT < 1f && fromImg != null && toImg != null && fromImg != toImg) {
      drawFitted(g, fromImg, areaX, areaY, areaW, areaH, kenBurns, kenPhase, 1f - fadeT);
      drawFitted(g, toImg, areaX, areaY, areaW, areaH, kenBurns, kenPhase, fadeT);
    } else {
      BufferedImage img = toImg != null ? toImg : fromImg;
      if (img != null) {
        drawFitted(g, img, areaX, areaY, areaW, areaH, kenBurns, kenPhase, 1f);
      }
    }
  }

  private static void drawFitted(
      Graphics2D g,
      BufferedImage img,
      int areaX,
      int areaY,
      int areaW,
      int areaH,
      boolean kenBurns,
      float kenPhase,
      float alpha) {
    if (img == null || alpha <= 0.01f) {
      return;
    }

    float phase = kenBurns ? Math.min(1f, kenPhase) : 0f;
    float scaleBoost = 1f + phase * KEN_BURNS_ZOOM;
    float fit = Math.min((float) areaW / img.getWidth(), (float) areaH / img.getHeight());
    int drawW = Math.round(img.getWidth() * fit * scaleBoost);
    int drawH = Math.round(img.getHeight() * fit * scaleBoost);
    int drawX = areaX + (areaW - drawW) / 2 + Math.round(phase * areaW * KEN_BURNS_PAN_X);
    int drawY = areaY + (areaH - drawH) / 2 + Math.round(phase * areaH * KEN_BURNS_PAN_Y);

    Composite prev = g.getComposite();
    if (alpha < 0.999f) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }
    drawSharp(g, img, drawX, drawY, drawW, drawH);
    g.setComposite(prev);
  }

  public static void drawFullBleed(
      Graphics2D g, int sw, int sh, String imagePath, float alpha) {
    if (imagePath == null || alpha <= 0.01f) {
      return;
    }
    BufferedImage img = Chapter1UiAssets.encounterSceneImage(imagePath);
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

  /** Два кадра воспоминаний на весь экран (финал с осколком). */
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
