package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.BossEncounterController;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** VN-появление врага: веки открываются, портрет, диалог. */
public final class BossEncounterView {

  private BossEncounterView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    if (encounter == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }

    g.setColor(new Color(10, 8, 6));
    g.fillRect(0, 0, sw, sh);

    BufferedImage map = Chapter1UiAssets.bossMapOpen();
    if (map != null) {
      BufferedImage bg = ScaledImageCache.get(map, sw, sh);
      g.drawImage(bg, 0, 0, null);
      var prev = g.getComposite();
      g.setColor(new Color(0, 0, 0, 120));
      g.fillRect(0, 0, sw, sh);
      g.setComposite(prev);
    }

    float portraitA = encounter.portraitAlpha();
    if (portraitA > 0.02f) {
      drawPortrait(g, sw, sh, encounter, portraitA);
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

  private static void drawPortrait(
      Graphics2D g, int sw, int sh, BossEncounterController encounter, float alpha) {
    BufferedImage portrait = Chapter1UiAssets.bossPortrait(encounter.boss().portraitPath());
    if (portrait == null) {
      return;
    }
    int pw = Math.round(sw * 0.42f);
    int ph = Math.round(sh * 0.72f);
    BufferedImage scaled = ScaledImageCache.get(portrait, pw, ph);
    int x = sw - pw - 16 + Math.round(encounter.portraitSlideX());
    int y = sh - ph - 36;
    var prev = g.getComposite();
    g.setComposite(java.awt.AlphaComposite.getInstance(
        java.awt.AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
    g.drawImage(scaled, x, y, null);
    g.setComposite(prev);

    g.setFont(GameFonts.get().uiBold(10));
    g.setColor(new Color(240, 210, 150, Math.round(255 * alpha)));
    g.drawString(encounter.boss().name(), x + 8, y + ph + 14);
    g.setFont(GameFonts.get().uiPlain(8));
    g.setColor(new Color(190, 170, 140, Math.round(220 * alpha)));
    g.drawString(encounter.boss().title(), x + 8, y + ph + 28);
  }
}
