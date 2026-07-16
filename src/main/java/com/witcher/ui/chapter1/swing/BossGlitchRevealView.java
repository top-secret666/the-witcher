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

/** Глитч-пробуждение Волка после победы: баг → коридор → моргание → лист → резкость → веки. */
public final class BossGlitchRevealView {

  private BossGlitchRevealView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl) {
    if (ctrl == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }

    int shakeX = dialogShakeX(ctrl);
    int shakeY = dialogShakeY(ctrl);
    g.translate(shakeX, shakeY);

    Stage stage = ctrl.stage();
    int local = ctrl.stageElapsedMs();
    long seed = ctrl.elapsedMs();

    switch (stage) {
      case GLITCH_BUILDUP -> drawBuildup(g, sw, sh, local, seed);
      case CORRIDOR_DIALOG -> drawCorridorDialog(g, sw, sh, ctrl, local, seed);
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

    g.translate(-shakeX, -shakeY);
  }

  private static void drawBuildup(Graphics2D g, int sw, int sh, int localMs, long seed) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    float intensity = BossGlitchRevealTimeline.buildupIntensity(localMs);
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh);
    PixelBugOverlay.draw(g, sw, sh, intensity, seed);
    if (intensity > 0.55f) {
      float reveal = (intensity - 0.55f) / 0.45f;
      drawCover(g, Chapter1UiAssets.bossBloodCorridor(), sw, sh, reveal, 1f);
    }
  }

  private static void drawCorridorDialog(
      Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl,
      int localMs, long seed) {
    int variant = ctrl.dialogBgVariant();
    BufferedImage bg = switch (variant) {
      case 1 -> Chapter1UiAssets.bossWakeForest();
      case 2 -> Chapter1UiAssets.wolfShardReveal();
      default -> Chapter1UiAssets.bossBloodCorridor();
    };
    drawCover(g, bg, sw, sh, 1f, 1f);
    float glitch = variant == 0 ? 0.12f : (variant == 1 ? 0.22f : 0.05f);
    if (glitch > 0.01f) {
      PixelBugOverlay.draw(g, sw, sh, glitch, seed);
    }
    drawThreatDialog(g, sw, sh, ctrl.visibleDialogText());
  }

  private static void drawBlink(Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl,
                                int localMs, long seed) {
    int frame = BossGlitchRevealTimeline.sheetFrameIndex(localMs / 2);
    drawSheetFrame(g, sw, sh, frame, 1f);
    float visible = BossGlitchRevealTimeline.blinkVisible(localMs);
    if (visible > 0.5f) {
      drawCover(g, Chapter1UiAssets.bossBloodCorridor(), sw, sh, 0.85f, 1f);
    } else {
      g.setColor(new Color(0, 0, 0, 200));
      g.fillRect(0, 0, sw, sh);
    }
    PixelBugOverlay.draw(g, sw, sh, 0.25f + frame * 0.06f, seed);
    drawThreatDialog(g, sw, sh, ctrl.visibleDialogText());
  }

  private static void drawAwakenSheet(Graphics2D g, int sw, int sh, int localMs, long seed) {
    int frame = BossGlitchRevealTimeline.sheetFrameIndex(localMs);
    drawSheetFrame(g, sw, sh, frame, 1f);
    PixelBugOverlay.draw(g, sw, sh, BossGlitchRevealTimeline.sheetNoise(frame), seed);
    GlitchOverlayRenderer.drawHeavyForced(g, sw, sh);
  }

  private static void drawSharpen(Graphics2D g, int sw, int sh, int localMs, long seed) {
    float sharp = BossGlitchRevealTimeline.sharpenT(localMs);
    BufferedImage img = Chapter1UiAssets.wolfShardReveal();
    drawCoverSharp(g, img, sw, sh, sharp);
    CutsceneNoiseOverlay.draw(g, sw, sh, (1f - sharp) * 0.85f);
    if (sharp < 0.95f) {
      PixelBugOverlay.draw(g, sw, sh, (1f - sharp) * 0.35f, seed);
    }
  }

  private static void drawFadeDark(Graphics2D g, int sw, int sh, int localMs) {
    drawCoverSharp(g, Chapter1UiAssets.wolfShardReveal(), sw, sh, 1f);
    float alpha = BossGlitchRevealTimeline.fadeDarkAlpha(localMs);
    g.setColor(new Color(0, 0, 0, Math.round(255 * alpha)));
    g.fillRect(0, 0, sw, sh);
  }

  private static void drawEyelidOpen(Graphics2D g, int sw, int sh, BossGlitchRevealController ctrl) {
    // wolf_shard_awaken — на весь экран под веками (не портрет).
    drawCoverSharp(g, Chapter1UiAssets.wolfShardAwaken(), sw, sh, 1f);
    EyelidOverlay.renderBlack(g, sw, sh, ctrl.eyelidOpenT());
  }

  private static void drawSheetFrame(Graphics2D g, int sw, int sh, int frameIndex, float alpha) {
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
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.drawImage(sheet,
        0, 0, sw, sh,
        col * cellW, row * cellH, col * cellW + cellW, row * cellH + cellH,
        null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }

  private static void drawCover(Graphics2D g, BufferedImage img, int sw, int sh, float alpha, float sharp) {
    if (img == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    drawCoverSharp(g, img, sw, sh, sharp);
    g.setComposite(prev);
  }

  /** Stretch to full virtual screen — edge to edge, no letterbox. */
  private static void drawCoverSharp(Graphics2D g, BufferedImage img, int sw, int sh, float sharp) {
    if (img == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    Object render = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    if (sharp >= 0.65f) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    } else {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }
    g.drawImage(img, 0, 0, sw, sh, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    if (render != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, render);
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
