package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.loop.LoopSequenceController;
import main.java.com.witcher.chapter1.loop.LoopSequenceKind;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Переход карта → босс: либо loop_wake + веки, либо процедурная ходьба сквозь чащу.
 */
public final class LoopSequenceView {

  private LoopSequenceView() {
  }

  public static void draw(
      BufferedImage screen,
      int sw,
      int sh,
      LoopSequenceController loopSequence,
      EyesBlinkEffect eyesEffect,
      CutscenePlayer loopCutscenePlayer,
      ForestWalkScene forestWalk) {
    if (loopSequence.kind() == LoopSequenceKind.FOREST_WALK) {
      drawForestWalk(screen, sw, sh, loopSequence, forestWalk);
      return;
    }
    drawEyelidWake(screen, sw, sh, loopSequence, eyesEffect, loopCutscenePlayer);
  }

  private static void drawForestWalk(
      BufferedImage screen,
      int sw,
      int sh,
      LoopSequenceController loopSequence,
      ForestWalkScene forestWalk) {
    Graphics2D g = screen.createGraphics();
    try {
      forestWalk.render(g, sw, sh, loopSequence.forestWalkElapsedMs());
    } finally {
      g.dispose();
    }
  }

  private static void drawEyelidWake(
      BufferedImage screen,
      int sw,
      int sh,
      LoopSequenceController loopSequence,
      EyesBlinkEffect eyesEffect,
      CutscenePlayer loopCutscenePlayer) {
    Graphics2D g = screen.createGraphics();
    try {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);

      float openT = eyesEffect.eyelidOpenT();
      if (openT > 0.03f) {
        float sharpness = eyesEffect.sharpness();
        loopCutscenePlayer.renderWake(g, sharpness);
        CutsceneNoiseOverlay.draw(g, sw, sh, eyesEffect.noiseStrength());
      }

      if (loopSequence.showEyes()) {
        eyesEffect.render(g, sw, sh);
      }
    } finally {
      g.dispose();
    }
  }

  public static int virtualW() {
    return Chapter1ViewConstants.VIRTUAL_W;
  }

  public static int virtualH() {
    return Chapter1ViewConstants.VIRTUAL_H;
  }
}
