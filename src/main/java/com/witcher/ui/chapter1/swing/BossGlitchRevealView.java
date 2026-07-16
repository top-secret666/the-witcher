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
 * Тряска экрана есть, но фон рисуется с запасом (pad), чтобы по краям не было дыр.
 */
public final class BossGlitchRevealView {

  /** Запас вокруг экрана — тряска не открывает чёрные края. */
  private static final int SHAKE_PAD = 10;

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
    int shakeX = screenShakeX(ctrl);
    int shakeY = screenShakeY(ctrl);

    switch (stage) {
      case GLITCH_BUILDUP -> drawBuildup(g, sw, sh, local, seed, shakeX, shakeY);
      case CORRIDOR_DIALOG -> drawCorridorDialog(g, sw, sh, ctrl, seed, shakeX, shakeY);
      case BLINK -> drawBlink(g, sw, sh, ctrl, local, seed, shakeX, shakeY);
      case AWAKEN_SHEET -> drawAwakenSheet(g, sw, sh, local, seed, shakeX, shakeY);
      case SHARPEN -> drawSharpen(g, sw, sh, local, seed);
      case FADE_DARK -> drawFadeDark(g, sw, sh, local);
      case EYELID_OPEN -> drawEyelidOpen(g, sw, sh, ctrl);
      default -> {
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
      }
    }
  }

  private static void drawBuildup(Graphics2D g, int sw, int sh, int localMs, long seed,
                                  int shakeX, int shakeY) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    float intensity = BossGlitchRevealTimeline.buildupIntensity(localMs);
    if (intensity > 0.55f) {
      float reveal = (intensity - 0.55f) / 0.45f;
      drawFullBleedShaken(g, Chapter1UiAssets.bossBloodCorridor(), sw, sh, reveal, shakeX, shakeY);
    }
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh);
    PixelBugOverlay.draw(g, sw, sh, intensity, seed);
  }

  private static void drawCorridorDialog(
      Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl, long seed,
      int shakeX, int shakeY) {
    int variant = ctrl.dialogBgVariant();
    BufferedImage bg = switch (variant) {
      case 1 -> Chapter1UiAssets.wolfForestEyes();
      case 2 -> Chapter1UiAssets.wolfShardReveal();
      default -> Chapter1UiAssets.bossBloodCorridor();
    };
    drawFullBleedShaken(g, bg, sw, sh, 1f, shakeX, shakeY);
    float glitch = variant == 0 ? 0.12f : (variant == 1 ? 0.22f : 0.05f);
    if (glitch > 0.01f) {
      PixelBugOverlay.draw(g, sw, sh, glitch, seed);
    }
    drawThreatDialog(g, sw, sh, ctrl.visibleDialogText());
  }

  private static void drawBlink(Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl,
                                int localMs, long seed, int shakeX, int shakeY) {
    int frame = BossGlitchRevealTimeline.sheetFrameIndex(localMs / 2);
    float visible = BossGlitchRevealTimeline.blinkVisible(localMs);
    if (visible > 0.5f) {
      drawFullBleedShaken(g, Chapter1UiAssets.bossBloodCorridor(), sw, sh, 1f, shakeX, shakeY);
    } else {
      drawSheetFrameBright(g, sw, sh, frame, 1f, shakeX, shakeY);
    }
    PixelBugOverlay.draw(g, sw, sh, 0.25f + frame * 0.06f, seed);
    drawThreatDialog(g, sw, sh, ctrl.visibleDialogText());
  }

  private static void drawAwakenSheet(Graphics2D g, int sw, int sh, int localMs, long seed,
                                      int shakeX, int shakeY) {
    int frame = BossGlitchRevealTimeline.sheetFrameIndex(localMs);
    drawSheetFrameBright(g, sw, sh, frame, 1f, shakeX, shakeY);
    // Без glitch_overlay_heavy — только лёгкие пиксельные баги по кадрам.
    float noise = BossGlitchRevealTimeline.sheetNoise(frame) * 0.75f;
    PixelBugOverlay.draw(g, sw, sh, noise, seed);
  }

  private static void drawSharpen(Graphics2D g, int sw, int sh, int localMs, long seed) {
    float sharp = BossGlitchRevealTimeline.sharpenT(localMs);
    // Как при пробуждении GIF: картинка из «мыла» в резкость + пиксельный шум сходит.
    BufferedImage scaled = ScaledImageCache.get(Chapter1UiAssets.wolfShardReveal(), sw, sh);
    if (scaled == null) {
      drawFullBleed(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, 1f);
    } else {
      WakeVisionRenderer.drawFrame(g, scaled, 0, 0, sharp);
    }
    // Большой шум в начале → 0 на максимальной чёткости. Без glitch_overlay_heavy / PixelBug.
    float noise = sharp >= 0.97f ? 0f : (1f - sharp) * 0.92f + 0.06f;
    if (noise > 0.02f) {
      CutsceneNoiseOverlay.draw(g, sw, sh, Math.min(1f, noise));
    }
  }

  private static void drawFadeDark(Graphics2D g, int sw, int sh, int localMs) {
    drawFullBleed(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, 1f);
    float alpha = BossGlitchRevealTimeline.fadeDarkAlpha(localMs);
    g.setColor(new Color(0, 0, 0, Math.round(255 * alpha)));
    g.fillRect(0, 0, sw, sh);
  }

  private static void drawEyelidOpen(Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl) {
    drawFullBleed(g, Chapter1UiAssets.wolfShardAwaken(), sw, sh, 1f);
    EyelidOverlay.renderBlack(g, sw, sh, ctrl.eyelidOpenT());
  }

  private static void drawSheetFrameBright(Graphics2D g, int sw, int sh, int frameIndex,
                                           float alpha, int shakeX, int shakeY) {
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

    // Копируем в ARGB — иначе RescaleOp падает на RGB/indexed листах.
    BufferedImage cell = sheet.getSubimage(col * cellW, row * cellH, cellW, cellH);
    BufferedImage argb = new BufferedImage(cellW, cellH, BufferedImage.TYPE_INT_ARGB);
    Graphics2D cg = argb.createGraphics();
    try {
      cg.drawImage(cell, 0, 0, null);
    } finally {
      cg.dispose();
    }
    RescaleOp brighten = new RescaleOp(
        new float[] {1.45f, 1.45f, 1.45f, 1f},
        new float[] {12f, 12f, 12f, 0f},
        null);
    BufferedImage bright = brighten.filter(argb, null);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    // oversized + shake — без чёрных краёв
    g.drawImage(bright,
        shakeX - SHAKE_PAD, shakeY - SHAKE_PAD,
        sw + SHAKE_PAD * 2, sh + SHAKE_PAD * 2,
        null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }

  /** Фон на весь экран с запасом и тряской — края никогда не «дырявые». */
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
    Object render = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(img,
        shakeX - SHAKE_PAD, shakeY - SHAKE_PAD,
        sw + SHAKE_PAD * 2, sh + SHAKE_PAD * 2,
        null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    if (render != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, render);
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

  private static int screenShakeX(BossGlitchRevealController ctrl) {
    Stage stage = ctrl.stage();
    if (stage != Stage.CORRIDOR_DIALOG && stage != Stage.BLINK
        && stage != Stage.GLITCH_BUILDUP && stage != Stage.AWAKEN_SHEET) {
      return 0;
    }
    float amp = stage == Stage.AWAKEN_SHEET ? 4f : 3f;
    return Math.round((float) Math.sin(ctrl.elapsedMs() * 0.035) * amp);
  }

  private static int screenShakeY(BossGlitchRevealController ctrl) {
    Stage stage = ctrl.stage();
    if (stage != Stage.CORRIDOR_DIALOG && stage != Stage.BLINK
        && stage != Stage.GLITCH_BUILDUP && stage != Stage.AWAKEN_SHEET) {
      return 0;
    }
    float amp = stage == Stage.AWAKEN_SHEET ? 3f : 2f;
    return Math.round((float) Math.sin(ctrl.elapsedMs() * 0.05 + 1.2) * amp);
  }
}
