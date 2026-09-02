package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.chapter1.view.VnChoiceLayout;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

/** Отрисовка VN-сцен главы 1 (бой, диалог герцога, финал). */
public final class VnSceneRenderer {

  private VnSceneRenderer() {
  }

  public static void drawScene(Graphics2D g, int sw, int sh, VnSceneState scene) {
    g.setColor(new Color(12, 8, 6));
    g.fillRect(0, 0, sw, sh);
    if (scene == null) {
      return;
    }
    DialogBoxRenderer.drawCompactFramedSpeakerText(
        g, sw, sh, scene.speaker(), scene.body(), DialogBoxRenderer.DUKE_COLOR, 1f);
  }

  /** Эпилог с осколком: воспоминания на весь экран + минимальная полоска диалога. */
  public static void drawShardEpilogueScene(Graphics2D g, int sw, int sh, VnSceneState scene) {
    if (scene == null) {
      return;
    }
    DialogBoxRenderer.drawShardEpilogueBar(
        g, sw, sh, scene.speaker(), scene.body(), 1f);
  }

  public static void drawOverlay(Graphics2D g, int sw, int sh, VnSceneState scene) {
    g.setColor(new Color(0, 0, 0, 140));
    g.fillRect(0, 0, sw, sh);
    if (scene == null) {
      return;
    }
    DialogBoxRenderer.drawCompactFramedSpeakerText(
        g, sw, sh, scene.speaker(), scene.body(), DialogBoxRenderer.DUKE_COLOR, 1f);
  }

  public static void drawChoices(
      Graphics2D g,
      VnSceneState scene,
      List<VnChoiceLayout.ChoiceRect> choiceRects) {
    if (scene == null || !scene.waitingForChoice()) {
      return;
    }
    var choices = scene.choices();
    g.setFont(GameFonts.get().uiBold(10));
    FontMetrics fm = g.getFontMetrics();
    for (VnChoiceLayout.ChoiceRect rect : choiceRects) {
      g.setColor(new Color(255, 220, 140));
      String label = (rect.index() + 1) + ". " + choices.get(rect.index()).label();
      int tw = fm.stringWidth(label);
      int tx = Math.round(rect.x() + (rect.width() - tw) / 2f);
      int ty = Math.round(rect.y() + (rect.height() + fm.getAscent() - fm.getDescent()) / 2f);
      g.drawString(label, tx, ty);
    }
  }
}
