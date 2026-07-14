package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.BossEncounterController;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** VN-появление врага: лес, злодей по центру, диалог. */
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

    float bgAlpha = Math.min(1f, encounter.eyelidOpenT() * 1.1f);
    if (bgAlpha > 0.02f) {
      drawForestBackground(g, sw, sh, bgAlpha);
    }

    float portraitA = encounter.portraitAlpha();
    if (portraitA > 0.02f) {
      drawCenterPortrait(g, sw, sh, encounter, portraitA);
    }

    if (encounter.showDialog() && encounter.scene() != null) {
      DialogBoxRenderer.drawCompactFramedSpeakerText(
          g, sw, sh,
          encounter.scene().speaker(),
          encounter.scene().body(),
          new Color(218, 165, 32),
          portraitA);
    }

    EyelidOverlay.render(g, sw, sh, 1f - encounter.eyelidOpenT());
  }

  private static void drawForestBackground(Graphics2D g, int sw, int sh, float alpha) {
    BufferedImage forest = Chapter1UiAssets.bossWakeForest();
    if (forest == null) {
      return;
    }
    BufferedImage bg = ScaledImageCache.get(forest, sw, sh);
    var prev = g.getComposite();
    g.setComposite(java.awt.AlphaComposite.getInstance(
        java.awt.AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
    g.drawImage(bg, 0, 0, null);
    g.setComposite(prev);
  }

  private static void drawCenterPortrait(
      Graphics2D g, int sw, int sh, BossEncounterController encounter, float alpha) {
    BufferedImage portrait = Chapter1UiAssets.bossPortrait(encounter.boss().portraitPath());
    if (portrait == null) {
      return;
    }
    float scale = encounter.portraitScale();
    int pw = Math.round(sw * 0.36f * scale);
    int ph = Math.round(sh * 0.78f * scale);
    BufferedImage scaled = ScaledImageCache.get(portrait, pw, ph);
    int x = (sw - pw) / 2;
    int y = sh - ph - 42;
    var prev = g.getComposite();
    g.setComposite(java.awt.AlphaComposite.getInstance(
        java.awt.AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
    g.drawImage(scaled, x, y, null);
    g.setComposite(prev);
  }
}
