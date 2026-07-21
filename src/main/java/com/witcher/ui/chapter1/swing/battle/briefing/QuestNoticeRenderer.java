package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.chapter1.battle.briefing.BossQuestBriefingScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

  private record TextBlock(String text, Font font, Color color, int gapAfter) {
  }

  private QuestNoticeRenderer() {
  }

  public static Layout layout(int sw, int sh) {
    BufferedImage src = Chapter1UiAssets.bossQuestNotice();
    float aspect = src != null && src.getHeight() > 0
        ? src.getWidth() / (float) src.getHeight()
        : 1.5f;

    DialogBoxRenderer.Layout dialog = DialogBoxRenderer.computeLayout(sw, sh);
    int reserveBottom = sh - dialog.boxY + Math.round(sh * 0.05f);

    int maxW = Math.round(sw * 0.92f);
    int maxH = sh - reserveBottom - Math.round(sh * 0.03f);
    int preferredH = Math.round(sh * 0.42f);

    int w = maxW;
    int h = Math.round(w / aspect);
    if (h > Math.min(maxH, preferredH)) {
      h = Math.min(maxH, preferredH);
      w = Math.round(h * aspect);
    }

    int x = (sw - w) / 2;
    int y = Math.max(Math.round(sh * 0.05f), (sh - reserveBottom - h) / 2);
    if (y + h > sh - reserveBottom) {
      y = Math.max(Math.round(sh * 0.02f), sh - reserveBottom - h);
    }

    int padX = Math.round(w * 0.12f);
    int padTop = Math.round(h * 0.14f);
    int padBottom = Math.round(h * 0.16f);
    int textX = x + padX;
    int textY = y + padTop;
    int textMaxW = w - padX * 2;
    int textMaxH = h - padTop - padBottom;

    return new Layout(x, y, w, h, textX, textY, textMaxW, textMaxH);
  }

  public static void draw(
      Graphics2D g,
      int sw,
      int sh,
      BossQuestBriefingScript.NoticeContent notice,
      QuestNoticeAnimator anim) {
    if (notice == null || anim == null || anim.paperAlpha <= 0.01f) {
      return;
    }

    Layout target = layout(sw, sh);
    Layout textLayout = layoutForAnim(anim, target);
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
        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(scaled, anim.x, anim.y, null);
        if (interp != null) {
          g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
        }
      }
    }
    g.setTransform(saved);

    if (anim.textAlpha > 0.02f) {
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, anim.textAlpha)));
      drawNoticeText(g, textLayout, notice);
    }

    g.setComposite(prev);
  }

  /** Текстовая зона следует за текущим размером/позицией листа (анимация открытия). */
  private static Layout layoutForAnim(QuestNoticeAnimator anim, Layout target) {
    if (anim == null || (Math.abs(anim.w - target.w()) < 2 && Math.abs(anim.h - target.h()) < 2)) {
      return target;
    }
    float sx = anim.w / (float) Math.max(1, target.w());
    float sy = anim.h / (float) Math.max(1, target.h());
    int padXRel = target.textX() - target.x();
    int padTop = target.textY() - target.y();
    return new Layout(
        anim.x, anim.y, anim.w, anim.h,
        anim.x + Math.round(padXRel * sx),
        anim.y + Math.round(padTop * sy),
        Math.round(target.textMaxW() * sx),
        Math.round(target.textMaxH() * sy));
  }

  private static void drawNoticeText(
      Graphics2D g,
      Layout layout,
      BossQuestBriefingScript.NoticeContent notice) {
    QuestNoticeFonts.applyInkHints(g);

    int centerX = layout.centerX();
    int maxW = layout.textMaxW;
    Color inkDark = new Color(28, 14, 4);
    Color inkBody = new Color(32, 18, 6);

    float scale = 1f;
    List<TextBlock> blocks;
    int totalHeight;
    do {
      blocks = buildBlocks(notice, layout, scale, inkDark, inkBody);
      totalHeight = measureBlocks(blocks, maxW, g);
      if (totalHeight <= layout.textMaxH || scale <= 0.78f) {
        break;
      }
      scale -= 0.06f;
    } while (scale > 0.78f);

    int startY = layout.textY + Math.max(0, (layout.textMaxH - totalHeight) / 2);
    int cy = startY;
    int bottomLimit = layout.textY + layout.textMaxH;

    for (TextBlock block : blocks) {
      if (cy > bottomLimit) {
        break;
      }
      cy = drawCenteredInk(g, block.text(), centerX, cy, maxW, block.font(), block.color());
      cy += block.gapAfter();
    }
  }

  private static List<TextBlock> buildBlocks(
      BossQuestBriefingScript.NoticeContent notice,
      Layout layout,
      float scale,
      Color inkDark,
      Color inkBody) {
    int scaleRef = Math.max(1, Math.round(Math.max(layout.h, layout.w / 3) * scale));
    Font headerFont = QuestNoticeFonts.header(Math.max(10, Math.round(scaleRef * 0.088f)));
    Font titleFont = QuestNoticeFonts.title(Math.max(12, Math.round(scaleRef * 0.105f)));
    Font bodyFont = QuestNoticeFonts.body(Math.max(8, Math.round(scaleRef * 0.068f)));

    return List.of(
        new TextBlock(notice.header(), headerFont, inkDark, 2),
        new TextBlock(notice.targetName(), titleFont, inkDark, 4),
        new TextBlock(notice.threatLevel(), bodyFont, inkBody, 2),
        new TextBlock(notice.body(), bodyFont, inkBody, 3),
        new TextBlock(notice.reward(), bodyFont, new Color(40, 22, 8), 0));
  }

  private static int measureBlocks(List<TextBlock> blocks, int maxW, Graphics2D g) {
    int total = 0;
    for (TextBlock block : blocks) {
      if (block.text() == null || block.text().isBlank()) {
        continue;
      }
      g.setFont(block.font());
      FontMetrics fm = g.getFontMetrics();
      int lineStep = compactLineStep(fm);
      total += countLines(block.text(), fm, maxW) * lineStep;
      total += block.gapAfter();
    }
    return total;
  }

  private static int countLines(String text, FontMetrics fm, int maxW) {
    int lines = 0;
    for (String paragraph : text.split("\n", -1)) {
      if (paragraph.isBlank()) {
        continue;
      }
      lines += wrap(paragraph, fm, maxW).size();
    }
    return Math.max(1, lines);
  }

  private static int drawCenteredInk(
      Graphics2D g, String text, int centerX, int y, int maxW, Font font, Color ink) {
    if (text == null || text.isBlank()) {
      return y;
    }
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    int lineStep = compactLineStep(fm);
    y += fm.getAscent();
    for (String paragraph : text.split("\n", -1)) {
      if (paragraph.isBlank()) {
        continue;
      }
      for (String line : wrap(paragraph, fm, maxW)) {
        int lw = fm.stringWidth(line);
        drawInkString(g, line, centerX - lw / 2, y, ink);
        y += lineStep;
      }
    }
    return y - fm.getAscent() + lineStep;
  }

  private static int compactLineStep(FontMetrics fm) {
    return fm.getAscent() + Math.max(1, fm.getDescent() / 2);
  }

  private static void drawInkString(Graphics2D g, String text, int x, int y, Color ink) {
    g.setColor(new Color(255, 248, 235, 100));
    g.drawString(text, x - 1, y);
    g.drawString(text, x + 1, y);
    g.setColor(new Color(0, 0, 0, 35));
    g.drawString(text, x + 1, y + 1);
    g.setColor(ink);
    g.drawString(text, x, y);
  }

  private static List<String> wrap(String text, FontMetrics fm, int maxW) {
    List<String> out = new ArrayList<>();
    if (text == null || text.isBlank()) {
      return out;
    }
    String[] words = text.split("\\s+");
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
    return out;
  }
}
