package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.chapter1.view.VnChoiceLayout;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
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
        g, sw, sh, scene.speaker(), scene.body(), new Color(218, 165, 32), 1f);
  }

  public static void drawOverlay(Graphics2D g, int sw, int sh, VnSceneState scene) {
    g.setColor(new Color(0, 0, 0, 140));
    g.fillRect(0, 0, sw, sh);
    if (scene == null) {
      return;
    }
    DialogBoxRenderer.drawCompactFramedSpeakerText(
        g, sw, sh, scene.speaker(), scene.body(), new Color(218, 165, 32), 1f);
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
    for (VnChoiceLayout.ChoiceRect rect : choiceRects) {
      g.setColor(new Color(255, 220, 140));
      String label = (rect.index() + 1) + ". " + choices.get(rect.index()).label();
      g.drawString(label, (int) rect.x(), (int) (rect.y() + 12));
    }
  }
}
