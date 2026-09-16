package com.witcher.ui.chapter1.swing.battle.encounter;

import com.witcher.chapter1.battle.encounter.BossEncounterController;
import com.witcher.chapter1.battle.encounter.BossEncounterScript;
import com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import com.witcher.ui.chapter1.swing.battle.BossVnDialogBoxRenderer;
import com.witcher.ui.chapter1.swing.battle.BossVnViewChrome;
import com.witcher.ui.chapter1.swing.EyelidOverlay;
import com.witcher.ui.chapter1.swing.ScaledImageCache;
import com.witcher.ui.graphics.DialogBoxRenderer;
import com.witcher.ui.intro.view.IntroCharacterLayout;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Лес + Волк, либо вспышка Каэр Морхена (Весемир / Геральт по бокам).
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

    if (encounter.isVesemirScene()
        || (encounter.flashbackActive() && !encounter.flashbackReady())) {
      drawVesemirScene(g, sw, sh, encounter);
    } else {
      drawForestBackground(g, sw, sh);
      drawCenterPortrait(g, sw, sh, encounter);
    }
    // Плашка диалога до CRT — как в интро, без яркой «полосы» поверх пост-обработки.
    if (encounter.showDialog()) {
      DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
      DialogBoxRenderer.drawBox(g, layout.boxX, layout.boxY, layout.boxW, layout.boxH, 1f);
    }
    EyelidOverlay.renderBlack(g, sw, sh, encounter.eyelidOverlayOpenT());
  }

  /** Чёткий VN UI поверх CRT. */
  public static void drawTextOverlay(Graphics2D g, int sw, int sh, BossEncounterController encounter,
                                     int mouseX, int mouseY) {
    if (encounter == null) {
      return;
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
  }

  private static void drawVesemirScene(
      Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    if (!encounter.vesemirBgVisible()) {
      return;
    }
    drawCoverBackground(g, sw, sh, Chapter1UiAssets.vesemirFlashbackBg());

    BossEncounterScript.DialogEntry entry = encounter.activeEntry();
    boolean vesemirSpeak = entry != null && "Весемир".equals(entry.speaker())
        && encounter.showDialog();
    boolean geraltSpeak = entry != null && "Геральт".equals(entry.speaker())
        && encounter.showDialog();

    BufferedImage vesemir = vesemirSpeak
        ? Chapter1UiAssets.vesemirSpeak()
        : Chapter1UiAssets.vesemirIdle();
    BufferedImage geralt = geraltSpeak
        ? Chapter1UiAssets.youngGeraltSpeak()
        : Chapter1UiAssets.youngGeraltIdle();

    // Как интро: Геральт слева, Весемир справа; слайд + activeAnim при речи.
    drawSideCharacter(g, sw, sh, geralt, encounter.geraltSlide(), true,
        geraltSpeak, encounter.leftActiveAnim(), encounter.tickCount());
    drawSideCharacter(g, sw, sh, vesemir, encounter.vesemirSlide(), false,
        vesemirSpeak, encounter.rightActiveAnim(), encounter.tickCount());
  }

  private static void drawSideCharacter(
      Graphics2D g, int sw, int sh, BufferedImage sprite, float slide,
      boolean left, boolean active, float activeAnim, int tick) {
    if (sprite == null || slide <= 0.001f) {
      return;
    }
    IntroCharacterLayout.Rect rect = IntroCharacterLayout.computeCharacterRect(
        sw, sh, sprite.getWidth(), sprite.getHeight(),
        slide, left, active, activeAnim, tick, false, false);
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    Object render = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(sprite, rect.x, rect.y, rect.width, rect.height, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    if (render != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, render);
    }
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

  private static void drawCoverBackground(Graphics2D g, int sw, int sh, BufferedImage src) {
    if (src == null) {
      return;
    }
    float cover = Math.max((float) sw / src.getWidth(), (float) sh / src.getHeight());
    int dw = Math.round(src.getWidth() * cover);
    int dh = Math.round(src.getHeight() * cover);
    int dx = (sw - dw) / 2;
    int dy = (sh - dh) / 2;
    drawIntroSharp(g, src, dx, dy, dw, dh);
  }

  private static void drawCenterPortrait(
      Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    BufferedImage portrait = Chapter1UiAssets.volkDukeSprite(encounter.spritePathForScene());
    if (portrait == null) {
      return;
    }
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
        encounter.tickCount(), encounter.waitingForAdvance(), encounter.autoMode(),
        false);
  }

  private static void drawIntroSharp(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
    Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    Object prevRender = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    // Nearest — без bicubic-мыла и без заливки чёрным под альфой.
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    g.drawImage(img, x, y, w, h, null);
    if (prevInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
    }
    if (prevRender != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRender);
    }
  }
}
