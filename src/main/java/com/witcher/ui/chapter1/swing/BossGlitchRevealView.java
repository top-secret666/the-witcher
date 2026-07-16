package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.BossGlitchRevealController;
import main.java.com.witcher.chapter1.battle.BossGlitchRevealTimeline;
import main.java.com.witcher.chapter1.battle.BossGlitchRevealTimeline.Stage;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.Random;

/**
 * Порядок слоёв (без наложений «картинка на картинку» не по сценарию):
 * чёрный → ТВ-баги → ТОЛЬКО heavy → «...» → код-баги → corridor+диалог+тряска
 * → быстрый цикл 3 лесов + sheet → БАЦ чёрный → shard из шума → пропадает.
 */
public final class BossGlitchRevealView {

  private static final int SHAKE_PAD = 12;

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
      case CODE_FILL -> drawCodeFill(g, sw, sh, local, seed);
      case CODE_FADE -> drawCodeFade(g, sw, sh, local, seed);
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
    PixelBugOverlay.drawTvStatic(g, sw, sh, intensity, seed);
  }

  private static void drawHeavyReveal(Graphics2D g, int sw, int sh, int localMs, long seed) {
    // ТОЛЬКО heavy — без corridor под ним.
    float heavyA = BossGlitchRevealTimeline.rise01(localMs, BossGlitchRevealTimeline.HEAVY_REVEAL_MS);
    float staticA = BossGlitchRevealTimeline.fall01(localMs, BossGlitchRevealTimeline.HEAVY_REVEAL_MS);
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh, Math.max(0.35f, heavyA));
    if (staticA > 0.05f) {
      PixelBugOverlay.drawTvStatic(g, sw, sh, staticA * 0.85f, seed);
    }
  }

  private static void drawDotsDialog(Graphics2D g, int sw, int sh) {
    // Только heavy + три красные точки. Без тряски.
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh, 0.85f);
    drawThreeRedDots(g, sw, sh);
  }

  private static void drawCodeFill(Graphics2D g, int sw, int sh, int localMs, long seed) {
    // После «...» снова только баги (пиксели + 0/1/буквы) — без heavy и без corridor.
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    float intensity = BossGlitchRevealTimeline.rise01(localMs, BossGlitchRevealTimeline.CODE_FILL_MS);
    PixelBugOverlay.drawDigitalRain(g, sw, sh, intensity, seed, 0.72f);
  }

  private static void drawCodeFade(Graphics2D g, int sw, int sh, int localMs, long seed) {
    // Рассеивание: помехи слабеют, но остаются. Corridor ещё нет.
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    float residual = 0.22f + BossGlitchRevealTimeline.fall01(localMs, BossGlitchRevealTimeline.CODE_FADE_MS) * 0.65f;
    PixelBugOverlay.drawDigitalRain(g, sw, sh, residual, seed, 0.5f);
  }

  private static void drawCorridorDialog(
      Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl, long seed) {
    int shakeX = dialogShakeX(ctrl.elapsedMs());
    int shakeY = dialogShakeY(ctrl.elapsedMs());
    // Только corridor — без heavy сверху.
    drawFullBleedShaken(g, Chapter1UiAssets.bossBloodCorridor(), sw, sh, 1f, shakeX, shakeY);
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
    drawFullBleedShaken(g, bg, sw, sh, 1f, shakeX, shakeY);

    int frame = BossGlitchRevealTimeline.sheetFrameIndex(
        Math.min(localMs, BossGlitchRevealTimeline.SHEET_MS - 1));
    drawSheetFrameBright(g, sw, sh, frame, 1f, shakeX, shakeY);

    PixelBugOverlay.drawTvStatic(g, sw, sh, 0.15f + peak * 0.85f, seed);
    if (peak > 0.45f) {
      // Вспышки + неровности на пике глюка — без второго фонового ассета.
      flashBands(g, sw, sh, peak, seed);
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
      drawFullBleed(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, sharp);
    }
    float noise = sharp >= 0.97f ? 0f : (1f - sharp) * 0.9f + 0.05f;
    if (noise > 0.02f) {
      CutsceneNoiseOverlay.draw(g, sw, sh, Math.min(1f, noise));
    }
  }

  private static void drawShardOut(Graphics2D g, int sw, int sh, int localMs) {
    float a = BossGlitchRevealTimeline.shardOutAlpha(localMs);
    drawFullBleed(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, a);
    if (a < 0.99f) {
      g.setColor(new Color(0, 0, 0, Math.round(255 * (1f - a))));
      g.fillRect(0, 0, sw, sh);
    }
  }

  private static void drawThreeRedDots(Graphics2D g, int sw, int sh) {
    int dialogH = Math.round(sh * 0.22f);
    int y0 = sh - dialogH;
    g.setColor(new Color(0, 0, 0, 210));
    g.fillRect(0, y0, sw, dialogH);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int fontSize = Math.max(16, Math.round(sh * 0.06f));
    g.setFont(GameFonts.get().bold(fontSize));
    FontMetrics fm = g.getFontMetrics();
    String dots = "...";
    int tx = (sw - fm.stringWidth(dots)) / 2;
    int ty = y0 + (dialogH + fm.getAscent() - fm.getDescent()) / 2;
    g.setColor(new Color(200, 18, 28));
    g.drawString(dots, tx, ty);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  private static void flashBands(Graphics2D g, int sw, int sh, float peak, long seed) {
    Random rnd = new Random(seed * 19L);
    int flashes = Math.round(2 + peak * 8);
    for (int i = 0; i < flashes; i++) {
      int y = rnd.nextInt(sh);
      int h = 2 + rnd.nextInt(Math.max(2, Math.round(sh * 0.08f * peak)));
      g.setColor(new Color(255, 255, 255, Math.round(40 + peak * 140)));
      g.fillRect(0, y, sw, h);
      if (rnd.nextBoolean()) {
        g.setColor(new Color(255, 40, 40, Math.round(50 + peak * 120)));
        g.fillRect(0, Math.min(sh - 1, y + h), sw, 1 + rnd.nextInt(3));
      }
    }
  }

  private static void drawSheetFrameBright(Graphics2D g, int sw, int sh, int frameIndex,
                                           float alpha, int shakeX, int shakeY) {
    BufferedImage sheet = Chapter1UiAssets.bossGlitchAwakenSheet();
    if (sheet == null) {
      return;
    }
    int cols = BossGlitchRevealTimeline.SHEET_COLS;
    int rows = BossGlitchRevealTimeline.SHEET_ROWS;
    int cellW = sheet.getWidth() / cols;
    int cellH = sheet.getHeight() / rows;
    int col = frameIndex % cols;
    int row = frameIndex / cols;

    BufferedImage cell = sheet.getSubimage(col * cellW, row * cellH, cellW, cellH);
    BufferedImage argb = new BufferedImage(cellW, cellH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D cg = argb.createGraphics();
    try {
      cg.drawImage(cell, 0, 0, null);
    } finally {
      cg.dispose();
    }
    RescaleOp brighten = new RescaleOp(
        new float[] {1.4f, 1.4f, 1.4f, 1f},
        new float[] {10f, 10f, 10f, 0f},
        null);
    BufferedImage bright = brighten.filter(argb, null);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.drawImage(bright, shakeX - SHAKE_PAD, shakeY - SHAKE_PAD, sw + SHAKE_PAD * 2, sh + SHAKE_PAD * 2, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }

  private static void drawFullBleedShaken(Graphics2D g, BufferedImage img, int sw, int sh,
                                          float alpha, int shakeX, int shakeY) {
    if (img == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }
    Composite prev = g.getComposite();
    if (alpha < 0.999f) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
    }
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.drawImage(img, shakeX - SHAKE_PAD, shakeY - SHAKE_PAD, sw + SHAKE_PAD * 2, sh + SHAKE_PAD * 2, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }

  private static void drawFullBleed(Graphics2D g, BufferedImage img, int sw, int sh, float alpha) {
    drawFullBleedShaken(g, img, sw, sh, alpha, 0, 0);
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
    g.setColor(new Color(190, 18, 28));
    int x = Math.round(sw * 0.06f);
    int y = y0 + Math.round(dialogH * 0.42f);
    for (String line : wrap(text, fm, Math.round(sw * 0.88f))) {
      g.drawString(line, x, y);
      y += fm.getHeight() + 2;
    }
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  private static java.util.List<String> wrap(String text, FontMetrics fm, int maxW) {
    java.util.List<String> out = new java.util.ArrayList<>();
    for (String paragraph : text.split("\n", -1)) {
      if (paragraph.isBlank()) {
        out.add("");
        continue;
      }
      String[] words = paragraph.split("\\s+");
      StringBuilder line = new StringBuilder();
      for (String word : words) {
        String trial = line.isEmpty() ? word : line + " " + word;
        if (fm.stringWidth(trial) > maxW && !line.isEmpty()) {
          out.add(line.toString());
          line = new StringBuilder(word);
        } else {
          line = new StringBuilder(trial);
        }
      }
      if (!line.isEmpty()) {
        out.add(line.toString());
      }
    }
    return out;
  }

  private static int dialogShakeX(long elapsedMs) {
    return Math.round((float) Math.sin(elapsedMs * 0.04) * 4f);
  }

  private static int dialogShakeY(long elapsedMs) {
    return Math.round((float) Math.sin(elapsedMs * 0.055 + 1.2) * 3f);
  }
}
