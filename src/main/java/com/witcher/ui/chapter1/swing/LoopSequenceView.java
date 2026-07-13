package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.loop.LoopSequenceController;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Отрисовка цепочки loop_wake → глаза → illusion_wrong. */
public final class LoopSequenceView {

  private LoopSequenceView() {
  }

  public static void draw(
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
      if (loopSequence.step() == LoopSequenceController.Step.EYES_OPEN
          && !eyesEffect.isDone()) {
        CutsceneNoiseOverlay.draw(g, sw, sh, 0.15f);
      } else {
        loopCutscenePlayer.render(g, sw, sh);
        CutsceneNoiseOverlay.draw(g, sw, sh, LoopSequenceController.NOISE_STRENGTH);
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
