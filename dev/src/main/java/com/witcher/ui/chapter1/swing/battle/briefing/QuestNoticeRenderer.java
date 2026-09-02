package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.chapter1.battle.briefing.BossQuestBriefingScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.GraphicsDrawHelpers;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Горизонтальный лист заказа по центру экрана + текст Philosopher внутри пергамента. */
public final class QuestNoticeRenderer {

  /** Разметка пергамента на виртуальном кадре 480×360. */
  public record Layout(int x, int y, int w, int h, int textX, int textY, int textMaxW, int textMaxH) {
    public int centerX() {
      return x + w / 2;
    }
  }

  private enum Align { CENTER, LEFT }

  private record TextBlock(String text, Font font, Color color, Align align, int gapAfter) {
  }

  private record DrawLine(String text, Font font, Color color, Align align) {
  }

  private QuestNoticeRenderer() {
  }

  public static Layout layout(int sw, int sh) {
    return QuestNoticeLayout.compute(sw, sh).toRendererLayout();
  }

  public static void draw(
      Graphics2D g,
      int sw,
      int sh,
      BossQuestBriefingScript.NoticeContent notice,
      QuestNoticeAnimator anim) {
    drawPaper(g, sw, sh, anim);
    drawTextOverlay(g, sw, sh, notice, anim);
  }

  /** Пергамент в кадре сцены (под CRT-постобработкой). */
  public static void drawPaper(Graphics2D g, int sw, int sh, QuestNoticeAnimator anim) {
    if (anim == null || anim.paperAlpha <= 0.01f) {
      return;
    }

    Composite prev = g.getComposite();
    AffineTransform saved = g.getTransform();
    int cx = anim.x + anim.w / 2;
    int cy = anim.y + anim.h / 2;
    AffineTransform flip = new AffineTransform(saved);
    flip.translate(cx, cy);
    flip.scale(anim.flipScaleX, 1.0);
    flip.translate(-cx, -cy);
    g.setTransform(flip);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, anim.paperAlpha)));
    BufferedImage asset = Chapter1UiAssets.bossQuestNotice();
    if (asset != null && anim.w > 0 && anim.h > 0) {
      BufferedImage scaled = ScaledImageCache.get(asset, anim.w, anim.h);
      if (scaled != null) {
        GraphicsDrawHelpers.drawBicubic(g, scaled, anim.x, anim.y, anim.w, anim.h);
      }
    }
    g.setTransform(saved);
    g.setComposite(prev);
  }

  /** Чёткий текст поверх пост-обработки (как речь в лавке). */
  public static void drawTextOverlay(
      Graphics2D g,
      int sw,
      int sh,
      BossQuestBriefingScript.NoticeContent notice,
      QuestNoticeAnimator anim) {
    if (notice == null || anim == null || anim.textAlpha <= 0.02f) {
      return;
    }

    Layout textLayout = layoutForAnim(anim, layout(sw, sh));
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, anim.textAlpha)));
    GameFonts.applyUiOverlayHints(g);
    paintNoticeText(g, textLayout, notice);
    g.setComposite(prev);
  }

  private static Layout layoutForAnim(QuestNoticeAnimator anim, Layout target) {
    if (anim == null || (Math.abs(anim.w - target.w()) < 2 && Math.abs(anim.h - target.h()) < 2)) {
      return target;
    }
    float sx = anim.w / (float) Math.max(1, target.w());
    float sy = anim.h / (float) Math.max(1, target.h());
    int padLeft = target.textX() - target.x();
    int padTop = target.textY() - target.y();
    return new Layout(
        anim.x, anim.y, anim.w, anim.h,
        anim.x + Math.round(padLeft * sx),
        anim.y + Math.round(padTop * sy),
        Math.round(target.textMaxW() * sx),
        Math.round(target.textMaxH() * sy));
  }

  private static void paintNoticeText(
      Graphics2D g,
      Layout layout,
      BossQuestBriefingScript.NoticeContent notice) {
    Color inkDark = QuestNoticeInkColors.HEADER;
    Color inkBody = QuestNoticeInkColors.BODY;
    int maxW = layout.textMaxW;
    int bottomLimit = layout.textY + layout.textMaxH;

    float scale = 1f;
    List<TextBlock> blocks;
    List<DrawLine> lines;
    int totalHeight;
    do {
      blocks = buildBlocks(notice, layout, scale, inkDark, inkBody);
      lines = flattenBlocks(blocks, maxW, g);
      totalHeight = measureLines(lines, g);
      if (totalHeight <= layout.textMaxH || scale <= 0.72f) {
        break;
      }
      scale -= 0.05f;
    } while (scale > 0.72f);

    int startY = layout.textY + Math.max(0, (layout.textMaxH - totalHeight) / 3);
    int lineY = startY;

    Shape prevClip = g.getClip();
    g.clipRect(layout.textX, layout.textY, maxW, layout.textMaxH);

    for (DrawLine line : lines) {
      if (lineY > bottomLimit) {
        break;
      }
      g.setFont(line.font());
      FontMetrics fm = g.getFontMetrics();
      int baseline = lineY + fm.getAscent();
      if (baseline > bottomLimit) {
        break;
      }
      int x = line.align() == Align.CENTER
          ? layout.centerX() - fm.stringWidth(line.text()) / 2
          : layout.textX;
      drawCrispInk(g, line.text(), x, baseline, line.color());
      lineY += lineStep(fm);
    }

    g.setClip(prevClip);
  }

  private static List<TextBlock> buildBlocks(
      BossQuestBriefingScript.NoticeContent notice,
      Layout layout,
      float scale,
      Color inkDark,
      Color inkBody) {
    int scaleRef = Math.max(1, Math.round(Math.max(layout.h, layout.w / 3) * scale));
    Font headerFont = QuestNoticeFonts.header(Math.max(11, Math.round(scaleRef * 0.084f)));
    Font titleFont = QuestNoticeFonts.title(Math.max(13, Math.round(scaleRef * 0.098f)));
    Font bodyFont = QuestNoticeFonts.body(Math.max(10, Math.round(scaleRef * 0.070f)));

    return List.of(
        new TextBlock(notice.header(), headerFont, inkDark, Align.CENTER, 1),
        new TextBlock(notice.targetName(), titleFont, inkDark, Align.CENTER, 3),
        new TextBlock(notice.threatLevel(), bodyFont, inkBody, Align.CENTER, 2),
        new TextBlock(notice.body(), bodyFont, inkBody, Align.LEFT, 2),
        new TextBlock(notice.reward(), bodyFont, QuestNoticeInkColors.REWARD, Align.LEFT, 0));
  }

  private static List<DrawLine> flattenBlocks(List<TextBlock> blocks, int maxW, Graphics2D g) {
    List<DrawLine> out = new ArrayList<>();
    for (TextBlock block : blocks) {
      if (block.text() == null || block.text().isBlank()) {
        continue;
      }
      g.setFont(block.font());
      FontMetrics fm = g.getFontMetrics();
      for (String wrapped : DialogBoxRenderer.wrapLine(block.text(), fm, maxW)) {
        out.add(new DrawLine(wrapped, block.font(), block.color(), block.align()));
      }
      for (int i = 0; i < block.gapAfter(); i++) {
        out.add(new DrawLine("", block.font(), block.color(), block.align()));
      }
    }
    return out;
  }

  private static int measureLines(List<DrawLine> lines, Graphics2D g) {
    int total = 0;
    FontMetrics lastFm = null;
    for (DrawLine line : lines) {
      if (line.text().isEmpty()) {
        if (lastFm != null) {
          total += Math.max(2, lineStep(lastFm) / 3);
        }
        continue;
      }
      g.setFont(line.font());
      lastFm = g.getFontMetrics();
      total += lineStep(lastFm);
    }
    return total;
  }

  private static int lineStep(FontMetrics fm) {
    return fm.getAscent() + Math.max(1, fm.getDescent() / 2);
  }

  private static void drawCrispInk(Graphics2D g, String text, int x, int y, Color ink) {
    int tx = (x + 1) & ~1;
    int ty = (y + 1) & ~1;
    g.setColor(new Color(255, 248, 235, 70));
    g.drawString(text, tx, ty - 1);
    g.setColor(new Color(0, 0, 0, 45));
    g.drawString(text, tx + 1, ty + 1);
    g.setColor(ink);
    g.drawString(text, tx, ty);
  }
}
