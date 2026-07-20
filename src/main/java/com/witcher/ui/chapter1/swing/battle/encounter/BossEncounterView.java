package main.java.com.witcher.ui.chapter1.swing.battle.encounter;

import main.java.com.witcher.chapter1.battle.encounter.BossEncounterController;
import main.java.com.witcher.chapter1.battle.encounter.BossEncounterScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.battle.BossVnDialogBoxRenderer;
import main.java.com.witcher.ui.chapter1.swing.battle.BossVnViewChrome;
import main.java.com.witcher.ui.chapter1.swing.EyelidOverlay;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;

import java.awt.Color;
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
    if (encounter.useSceneImage()) {
      EncounterSceneRenderer.drawSceneImage(g, sw, sh, encounter);
    } else {
      drawCenterPortrait(g, sw, sh, encounter);
    }

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
    BossVnDialogBoxRenderer.draw(
        g, sw, sh,
        entry.speaker(), entry.speakerColorRgb(), encounter.visibleText(),
        encounter.tickCount(), encounter.waitingForAdvance(), encounter.autoMode());
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
