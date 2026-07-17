package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.loop.LoopSequenceController;
import main.java.com.witcher.ui.chapter1.swing.glitch.CutsceneNoiseOverlay;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** loop_wake: GIF с пробуждением (резкость + шум) и веки от первого лица. */
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
