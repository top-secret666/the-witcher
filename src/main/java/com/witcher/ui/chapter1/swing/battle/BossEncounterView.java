package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.battle.BossEncounterController;
import main.java.com.witcher.chapter1.battle.BossEncounterScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.EyelidOverlay;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.intro.IntroVnUi;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Лес + злодей под веками. После открытия — VN как в интро (окно + Назад/История/Авто),
 * спрайт volk_duke_* по центру, эмоции по репликам. Фон не меняется.
 */
public final class BossEncounterView {

  private BossEncounterView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossEncounterController encounter,
                          int mouseX, int mouseY) {
    if (encounter == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    drawForestBackground(g, sw, sh);
    drawCenterPortrait(g, sw, sh, encounter);

    if (encounter.showDialog()) {
      drawDialogBox(g, sw, sh, encounter);
      BossVnViewChrome.drawToolbar(
          g, encounter.buttons(), encounter.backEnabled(), encounter.autoMode(), mouseX, mouseY);
    }

    if (encounter.historyOpen()) {
      BossVnViewChrome.drawHistoryOverlay(
          g, sw, sh, encounter.buttons(), encounter.historyCloseHovered(),
          encounter.historyScroll(), encounter::setHistoryScroll, encounter.buildHistoryLogLines());
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
    BufferedImage portrait = Chapter1UiAssets.volkDukeSprite(encounter.spritePathForScene());
    if (portrait == null) {
      return;
    }
    // Крупнее в диалоге — почти на весь экран.
    float baseCharScale = (sh * 0.96f) / Math.max(1, portrait.getHeight());
    float charScale = baseCharScale * encounter.portraitScale();
    int cw = Math.round(portrait.getWidth() * charScale);
    int ch = Math.round(portrait.getHeight() * charScale);
    int dialogZone = Math.round(sh * 0.15f);
    int x = (sw - cw) / 2;
    int y = sh - dialogZone - ch + Math.round(ch * 0.15f);
    drawIntroSharp(g, portrait, x, y, cw, ch);
  }

  private static void drawDialogBox(Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    BossEncounterScript.DialogEntry entry = encounter.currentEntry();
    if (entry == null) {
      return;
    }
    DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
    Color speakerColor = entry.speaker() == null
        ? DialogBoxRenderer.NARRATOR_COLOR
        : new Color((entry.speakerColorRgb() >> 16) & 0xff,
            (entry.speakerColorRgb() >> 8) & 0xff,
            entry.speakerColorRgb() & 0xff);

    String visibleText = encounter.visibleText();
    int lineY = DialogBoxRenderer.drawTypewriterText(
        g, entry.speaker(), visibleText, speakerColor, layout, 1f);

    if (!encounter.waitingForAdvance() && (encounter.tickCount() / 8) % 2 == 0) {
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      Font textFont = GameFonts.get().plain(layout.fontSize);
      g.setFont(textFont);
      FontMetrics fm = g.getFontMetrics();
      int cursorX = layout.textX + fm.stringWidth(
          DialogBoxRenderer.getLastVisibleLine(visibleText, fm, layout.textMaxW));
      g.setColor(speakerColor);
      g.fillRect(cursorX + 2, lineY - fm.getAscent() + 2,
          Math.max(2, layout.fontSize / 5), fm.getAscent());
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    if (encounter.waitingForAdvance() && !encounter.autoMode()
        && (encounter.tickCount() / 15) % 2 == 0) {
      DialogBoxRenderer.drawHint(g, "\u25B6 Enter", layout, layout.fontSize, 1f);
    } else if (encounter.waitingForAdvance() && encounter.autoMode()
        && (encounter.tickCount() / 12) % 2 == 0) {
      DialogBoxRenderer.drawHint(g, "Авто \u25B6", layout, layout.fontSize, 0.85f);
    }
  }

  /** Та же интерполяция, что у персонажей интро — максимальная чёткость painted-арта. */
  private static void drawIntroSharp(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
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
