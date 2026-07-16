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

/**
 * Глитч-пробуждение Волка после победы.
 * Фон (corridor / sheet / shard) всегда статичен на весь экран — трясётся только UI/оверлей.
 */
public final class BossGlitchRevealView {

  private static final RescaleOp SHEET_BRIGHTEN =
      new RescaleOp(new float[] {1.45f, 1.45f, 1.45f, 1f}, new float[] {12f, 12f, 12f, 0f}, null);

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
      case GLITCH_BUILDUP -> drawBuildup(g, sw, sh, local, seed);
      case CORRIDOR_DIALOG -> drawCorridorDialog(g, sw, sh, ctrl, seed);
      case BLINK -> drawBlink(g, sw, sh, ctrl, local, seed);
      case AWAKEN_SHEET -> drawAwakenSheet(g, sw, sh, local, seed);
      case SHARPEN -> drawSharpen(g, sw, sh, local, seed);
      case FADE_DARK -> drawFadeDark(g, sw, sh, local);
      case EYELID_OPEN -> drawEyelidOpen(g, sw, sh, ctrl);
      default -> {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
      }
    }
  }

  private static void drawBuildup(Graphics2D g, int sw, int sh, int localMs, long seed) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    float intensity = BossGlitchRevealTimeline.buildupIntensity(localMs);
    // Картинка статична; «тряска экрана» — только через оверлей багов.
    if (intensity > 0.55f) {
      float reveal = (intensity - 0.55f) / 0.45f;
      drawFullScreen(g, Chapter1UiAssets.bossBloodCorridor(), sw, sh, reveal);
    }
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh);
    PixelBugOverlay.draw(g, sw, sh, intensity, seed);
  }

  private static void drawCorridorDialog(
      Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl, long seed) {
    int variant = ctrl.dialogBgVariant();
    BufferedImage bg = switch (variant) {
      case 1 -> Chapter1UiAssets.bossWakeForest();
      case 2 -> Chapter1UiAssets.wolfShardReveal();
      default -> Chapter1UiAssets.bossBloodCorridor();
    };
    // Фон — статичный на весь экран, без translate.
    drawFullScreen(g, bg, sw, sh, 1f);
    float glitch = variant == 0 ? 0.12f : (variant == 1 ? 0.22f : 0.05f);
    if (glitch > 0.01f) {
      PixelBugOverlay.draw(g, sw, sh, glitch, seed);
    }
    // Трясётся только диалоговое окно / текст.
    int shakeX = dialogShakeX(ctrl);
    int shakeY = dialogShakeY(ctrl);
    g.translate(shakeX, shakeY);
    drawThreatDialog(g, sw, sh, ctrl.visibleDialogText());
    g.translate(-shakeX, -shakeY);
  }

  private static void drawBlink(Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl,
                                int localMs, long seed) {
    int frame = BossGlitchRevealTimeline.sheetFrameIndex(localMs / 2);
    float visible = BossGlitchRevealTimeline.blinkVisible(localMs);
    if (visible > 0.5f) {
      drawFullScreen(g, Chapter1UiAssets.bossBloodCorridor(), sw, sh, 1f);
    } else {
      drawSheetFrameBright(g, sw, sh, frame, 1f);
    }
    PixelBugOverlay.draw(g, sw, sh, 0.25f + frame * 0.06f, seed);
    int shakeX = dialogShakeX(ctrl);
    int shakeY = dialogShakeY(ctrl);
    g.translate(shakeX, shakeY);
    drawThreatDialog(g, sw, sh, ctrl.visibleDialogText());
    g.translate(-shakeX, -shakeY);
  }

  private static void drawAwakenSheet(Graphics2D g, int sw, int sh, int localMs, long seed) {
    int frame = BossGlitchRevealTimeline.sheetFrameIndex(localMs);
    // Поярче кадры 3×3 — глаза/улыбка читаются сильнее.
    drawSheetFrameBright(g, sw, sh, frame, 1f);
    float noise = BossGlitchRevealTimeline.sheetNoise(frame) * 0.75f;
    PixelBugOverlay.draw(g, sw, sh, noise, seed);
    if (noise > 0.35f) {
      GlitchOverlayRenderer.drawHeavyForced(g, sw, sh);
    }
  }

  private static void drawSharpen(Graphics2D g, int sw, int sh, int localMs, long seed) {
    float sharp = BossGlitchRevealTimeline.sharpenT(localMs);
    // Фон всегда чёткий на весь экран; шум/баги — слоем сверху, сходят к нулю.
    drawFullScreen(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, 1f);

    float muddy = 1f - sharp;
    // Сначала огромный пиксельный шум + баги, потом всё чище.
    float noise = muddy * muddy * 1.15f; // сильнее в начале
    float bugs = muddy * 0.95f;
    if (noise > 0.02f) {
      CutsceneNoiseOverlay.draw(g, sw, sh, Math.min(1f, noise));
    }
    if (bugs > 0.02f) {
      PixelBugOverlay.draw(g, sw, sh, Math.min(1f, bugs), seed);
    }
    if (muddy > 0.45f) {
      GlitchOverlayRenderer.drawHeavyForced(g, sw, sh);
    }
    // На максимальной чёткости — ни багов, ни шума.
  }

  private static void drawFadeDark(Graphics2D g, int sw, int sh, int localMs) {
    // Максимально чётко, без багов — потом всё пропадает в темноту.
    drawFullScreen(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, 1f);
    float alpha = BossGlitchRevealTimeline.fadeDarkAlpha(localMs);
    g.setColor(new Color(0, 0, 0, Math.round(255 * alpha)));
    g.fillRect(0, 0, sw, sh);
  }

  private static void drawEyelidOpen(Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl) {
    drawFullScreen(g, Chapter1UiAssets.wolfShardAwaken(), sw, sh, 1f);
    EyelidOverlay.renderBlack(g, sw, sh, ctrl.eyelidOpenT());
  }

  private static void drawSheetFrameBright(Graphics2D g, int sw, int sh, int frameIndex, float alpha) {
    BufferedImage sheet = Chapter1UiAssets.bossGlitchAwakenSheet();
    if (sheet == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }
    int cols = BossGlitchRevealTimeline.SHEET_COLS;
    int rows = BossGlitchRevealTimeline.SHEET_ROWS;
    int cellW = sheet.getWidth() / cols;
    int cellH = sheet.getHeight() / rows;
    int col = frameIndex % cols;
    int row = frameIndex / cols;

    BufferedImage cell = sheet.getSubimage(col * cellW, row * cellH, cellW, cellH);
    BufferedImage bright = new BufferedImage(cellW, cellH, BufferedImage.TYPE_INT_ARGB);
    SHEET_BRIGHTEN.filter(cell, bright);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.drawImage(bright, 0, 0, sw, sh, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }

  private static void drawFullScreen(Graphics2D g, BufferedImage img, int sw, int sh, float alpha) {
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
    Object render = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(img, 0, 0, sw, sh, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    if (render != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, render);
    }
    g.setComposite(prev);
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
    Font font = GameFonts.get().plain(fontSize);
    g.setFont(font);
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

  private static int dialogShakeX(BossGlitchRevealController ctrl) {
    if (ctrl.stage() != Stage.CORRIDOR_DIALOG && ctrl.stage() != Stage.BLINK) {
      return 0;
    }
    return Math.round((float) Math.sin(ctrl.elapsedMs() * 0.035) * 3f);
  }

  private static int dialogShakeY(BossGlitchRevealController ctrl) {
    if (ctrl.stage() != Stage.CORRIDOR_DIALOG && ctrl.stage() != Stage.BLINK) {
      return 0;
    }
    return Math.round((float) Math.sin(ctrl.elapsedMs() * 0.05 + 1.2) * 2f);
  }
}
