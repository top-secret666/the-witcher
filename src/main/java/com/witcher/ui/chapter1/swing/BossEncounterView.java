package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.BossEncounterController;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Лес + злодей всегда под веками; веки открываются полностью и открывают сцену.
 * Диалог — только когда глаза открыты до конца.
 */
public final class BossEncounterView {

  private BossEncounterView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    if (encounter == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    // Сцена уже есть под веками — открытие глаз её раскрывает.
    drawForestBackground(g, sw, sh);
    drawCenterPortrait(g, sw, sh, encounter);

    if (encounter.showDialog() && encounter.scene() != null) {
      DialogBoxRenderer.drawCompactFramedSpeakerText(
          g, sw, sh,
          encounter.scene().speaker(),
          encounter.scene().body(),
          new Color(218, 165, 32),
          1f);
    }

    EyelidOverlay.renderBlack(g, sw, sh, encounter.eyelidOpenT());
  }

  private static void drawForestBackground(Graphics2D g, int sw, int sh) {
    BufferedImage forest = Chapter1UiAssets.bossWakeForest();
    if (forest == null) {
      return;
    }
    BufferedImage bg = ScaledImageCache.get(forest, sw, sh);
    if (bg != null) {
      g.drawImage(bg, 0, 0, null);
    }
  }

  private static void drawCenterPortrait(
      Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    BufferedImage portrait = Chapter1UiAssets.bossPortrait(encounter.boss().portraitPath());
    if (portrait == null) {
      return;
    }
    int basePw = Math.round(sw * 0.36f);
    int basePh = Math.round(sh * 0.78f);
    BufferedImage scaled = ScaledImageCache.get(portrait, basePw, basePh);
    if (scaled == null) {
      return;
    }
    float scale = encounter.portraitScale();
    int pw = Math.round(basePw * scale);
    int ph = Math.round(basePh * scale);
    int x = (sw - pw) / 2;
    int y = sh - ph - 42;
    g.drawImage(scaled, x, y, pw, ph, null);
  }
}
