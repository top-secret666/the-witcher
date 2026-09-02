package main.java.com.witcher.ui.chapter1.swing.battle.glitch;

import main.java.com.witcher.chapter1.battle.glitch.BossGlitchRevealController;
import main.java.com.witcher.chapter1.battle.glitch.BossGlitchRevealTimeline;
import main.java.com.witcher.chapter1.battle.glitch.BossGlitchRevealTimeline.Stage;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;
import main.java.com.witcher.ui.chapter1.swing.WakeVisionRenderer;
import main.java.com.witcher.ui.chapter1.swing.glitch.CutsceneNoiseOverlay;
import main.java.com.witcher.ui.chapter1.swing.glitch.GlitchOverlayRenderer;
import main.java.com.witcher.ui.chapter1.swing.glitch.PixelBugOverlay;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Порядок: чёрный → плотный ТВ-шум → спад → «...» → «ВЫХОД» (одно слово, растёт)
 * → мелкий разброс «ВЫХОД» по экрану → шумовой занавес → corridor…
 */
public final class BossGlitchRevealView {

  private static final Color RED_TEXT = new Color(200, 18, 28);

  private BossGlitchRevealView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl) {
    if (ctrl == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }

    Stage stage = ctrl.stage();
    int local = ctrl.stageElapsedMs();
    long seed = ctrl.elapsedMs();

    switch (stage) {
      case STATIC_FILL -> drawStaticFill(g, sw, sh, local, seed);
      case HEAVY_REVEAL -> drawHeavyReveal(g, sw, sh, local, seed);
      case DOTS_DIALOG -> drawDotsDialog(g, sw, sh);
      case EXIT_CURTAIN -> drawExitCurtain(g, sw, sh, local, seed);
      case EXIT_DROP -> drawExitDrop(g, sw, sh, local, seed);
      case CORRIDOR_DIALOG -> drawCorridorDialog(g, sw, sh, ctrl, seed);
      case CYCLE_SHEET -> drawCycleSheet(g, sw, sh, local, seed);
      case BLACK_BANG -> {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
      }
      case SHARD_EMERGE -> drawShardEmerge(g, sw, sh, local, seed);
      case SHARD_OUT -> drawShardOut(g, sw, sh, local);
      default -> {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
      }
    }
  }

  private static void drawStaticFill(Graphics2D g, int sw, int sh, int localMs, long seed) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    float intensity = BossGlitchRevealTimeline.rise01(localMs, BossGlitchRevealTimeline.STATIC_FILL_MS);
    if (intensity > 0.55f) {
      GlitchOverlayRenderer.drawHeavyForced(g, sw, sh, 1f);
    }
    PixelBugOverlay.drawTvStatic(g, sw, sh, Math.min(1f, intensity * 1.15f), seed, true);
  }

  private static void drawHeavyReveal(Graphics2D g, int sw, int sh, int localMs, long seed) {
    float staticA = BossGlitchRevealTimeline.heavyStaticFall(localMs);
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh, 1f);
    if (staticA > 0.03f) {
      PixelBugOverlay.drawTvStatic(g, sw, sh, staticA, seed, true);
    }
  }

  private static void drawDotsDialog(Graphics2D g, int sw, int sh) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh, 1f);
    BossGlitchExitRenderer.drawDialogLine(g, sw, sh, "...", 1f);
  }

  private static void drawExitCurtain(Graphics2D g, int sw, int sh, int localMs, long seed) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh, 1f);

    int step = BossGlitchRevealTimeline.exitBuildStep(localMs);
    float virus = BossGlitchRevealTimeline.virusSpreadT(localMs);

    if (step <= 2) {
      BossGlitchExitRenderer.drawExitDialogSingle(g, sw, sh, step);
    } else {
      BossGlitchExitRenderer.drawScatteredExitWords(
          g, sw, sh, BossGlitchRevealTimeline.exitScatterFillT(localMs));
    }
    if (virus > 0.01f) {
      PixelBugOverlay.drawVirusWordCurtain(g, sw, sh, virus, seed);
    }
  }

  private static void drawExitDrop(Graphics2D g, int sw, int sh, int localMs, long seed) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh, 1f);
    float curtain = BossGlitchRevealTimeline.exitDropT(localMs);
    if (curtain > 0.03f) {
      PixelBugOverlay.drawVirusWordCurtain(g, sw, sh, curtain, seed);
    }
  }

  private static void drawCorridorDialog(
      Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl, long seed) {
    int shakeX = BossGlitchDrawHelpers.dialogShakeX(ctrl.elapsedMs());
    int shakeY = BossGlitchDrawHelpers.dialogShakeY(ctrl.elapsedMs());
    BossGlitchDrawHelpers.drawFullBleedShaken(
        g, Chapter1UiAssets.bossBloodCorridor(), sw, sh, 1f, shakeX, shakeY);
    PixelBugOverlay.drawTvStatic(g, sw, sh, 0.08f, seed);
    drawThreatDialog(g, sw, sh, ctrl.visibleDialogText());
  }

  private static void drawCycleSheet(Graphics2D g, int sw, int sh, int localMs, long seed) {
    float peak = BossGlitchRevealTimeline.cycleGlitchPeak(localMs);
    int shakeX = Math.round((float) Math.sin(localMs * 0.09) * (3f + peak * 14f));
    int shakeY = Math.round((float) Math.sin(localMs * 0.11 + 1.1) * (2f + peak * 10f));

    BufferedImage bg = switch (BossGlitchRevealTimeline.bgCycleIndex(localMs)) {
      case 1 -> Chapter1UiAssets.bossWakeForest();
      case 2 -> Chapter1UiAssets.wolfMistForest();
      default -> Chapter1UiAssets.wolfForestEyes();
    };
    BossGlitchDrawHelpers.drawFullBleedShaken(g, bg, sw, sh, 1f, shakeX, shakeY);

    int frame = BossGlitchRevealTimeline.sheetFrameIndex(
        Math.min(localMs, BossGlitchRevealTimeline.SHEET_MS - 1));
    BossGlitchDrawHelpers.drawSheetFrameBright(g, sw, sh, frame, 1f, shakeX, shakeY);

    PixelBugOverlay.drawTvStatic(g, sw, sh, 0.15f + peak * 0.85f, seed);
    if (peak > 0.45f) {
      BossGlitchDrawHelpers.flashBands(g, sw, sh, peak, seed);
    }
  }

  private static void drawShardEmerge(Graphics2D g, int sw, int sh, int localMs, long seed) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    float sharp = BossGlitchRevealTimeline.sharpenT(localMs);
    BufferedImage scaled = ScaledImageCache.get(Chapter1UiAssets.wolfShardReveal(), sw, sh);
    if (scaled != null) {
      WakeVisionRenderer.drawFrame(g, scaled, 0, 0, sharp);
    } else {
      BossGlitchDrawHelpers.drawFullBleed(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, sharp);
    }
    float noise = sharp >= 0.97f ? 0f : (1f - sharp) * 0.9f + 0.05f;
    if (noise > 0.02f) {
      CutsceneNoiseOverlay.draw(g, sw, sh, Math.min(1f, noise));
    }
  }

  private static void drawShardOut(Graphics2D g, int sw, int sh, int localMs) {
    float a = BossGlitchRevealTimeline.shardOutAlpha(localMs);
    BossGlitchDrawHelpers.drawFullBleed(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, a);
    if (a < 0.99f) {
      g.setColor(new Color(0, 0, 0, Math.round(255 * (1f - a))));
      g.fillRect(0, 0, sw, sh);
    }
  }

  private static void drawThreatDialog(Graphics2D g, int sw, int sh, String text) {
    if (text == null || text.isBlank()) {
      return;
    }
    int dialogH = Math.round(sh * 0.24f);
    int y0 = sh - dialogH;
    g.setColor(new Color(0, 0, 0, 215));
    g.fillRect(0, y0, sw, dialogH);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int fontSize = Math.max(12, Math.round(sh * 0.042f));
    g.setFont(GameFonts.get().plain(fontSize));
    FontMetrics fm = g.getFontMetrics();
    g.setColor(RED_TEXT);
    int x = Math.round(sw * 0.06f);
    int y = y0 + Math.round(dialogH * 0.42f);
    for (String line : BossGlitchTextWrap.wrap(text, fm, Math.round(sw * 0.88f))) {
      g.drawString(line, x, y);
      y += fm.getHeight() + 2;
    }
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }
}
