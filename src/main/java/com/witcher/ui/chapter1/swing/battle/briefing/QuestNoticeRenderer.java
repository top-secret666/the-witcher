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

/** Лист заказа по центру экрана + текст Philosopher. */
public final class QuestNoticeRenderer {

  /** Разметка пергамента на виртуальном кадре 480×360. */
  public record Layout(int x, int y, int w, int h, int textX, int textY, int textMaxW, int textMaxH) {
    public int centerX() {
      return x + w / 2;
    }
  }

  private QuestNoticeRenderer() {
  }

  public static Layout layout(int sw, int sh) {
    BufferedImage src = Chapter1UiAssets.bossQuestNotice();
    float aspect = src != null && src.getHeight() > 0
        ? src.getWidth() / (float) src.getHeight()
        : 0.72f;

    DialogBoxRenderer.Layout dialog = DialogBoxRenderer.computeLayout(sw, sh);
    int reserveBottom = sh - dialog.boxY + Math.round(sh * 0.07f);

    int maxW = Math.round(sw * 0.74f);
    int maxH = sh - reserveBottom - Math.round(sh * 0.02f);
    int h = Math.min(Math.round(sh * 0.62f), maxH);
    int w = Math.round(h * aspect * 1.12f);
    if (w > maxW) {
      w = maxW;
      h = Math.round(w / (aspect * 1.12f));
    }

    int x = (sw - w) / 2;
    int y = Math.max(Math.round(sh * 0.015f), reserveBottom > 0 ? 0 : Math.round(sh * 0.02f));
    if (y + h > sh - reserveBottom) {
      y = Math.max(Math.round(sh * 0.01f), sh - reserveBottom - h);
    }

    int padX = Math.round(w * 0.10f);
    int padTop = Math.round(h * 0.11f);
    int padBottom = Math.round(h * 0.22f);
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
      float widthScale = 1.12f;
      int drawW = Math.round(anim.w * widthScale);
      int drawX = anim.x - (drawW - anim.w) / 2;
      BufferedImage scaled = ScaledImageCache.get(asset, drawW, anim.h);
      if (scaled != null) {
        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(scaled, drawX, anim.y, null);
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
    int padBottom = target.y() + target.h() - (target.textY() + target.textMaxH());
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

    int maxW = layout.textMaxW;
    int cy = layout.textY;
    int bottomLimit = layout.textY + layout.textMaxH;
    int centerX = layout.centerX();

    Font headerFont = QuestNoticeFonts.header(Math.max(14, Math.round(layout.h * 0.068f)));
    Font titleFont = QuestNoticeFonts.title(Math.max(18, Math.round(layout.h * 0.082f)));
    Font bodyFont = QuestNoticeFonts.body(Math.max(12, Math.round(layout.h * 0.044f)));
    Font sealFont = QuestNoticeFonts.seal(Math.max(10, Math.round(layout.h * 0.038f)));

    Color inkDark = new Color(28, 14, 4);
    Color inkBody = new Color(32, 18, 6);

    g.setFont(headerFont);
    g.setColor(inkDark);
    cy = drawCenteredInk(g, notice.header(), centerX, cy, maxW, headerFont, 4) + 6;
    if (cy > bottomLimit) {
      return;
    }

    g.setFont(titleFont);
    g.setColor(inkDark);
    cy = drawCenteredInk(g, notice.targetName(), centerX, cy, maxW, titleFont, 5) + 8;
    if (cy > bottomLimit) {
      return;
    }

    g.setFont(bodyFont);
    g.setColor(inkBody);
    cy = drawWrappedInk(g, notice.threatLevel(), layout.textX, cy, maxW, bodyFont, 3) + 6;
    cy = drawWrappedInk(g, notice.body(), layout.textX, cy, maxW, bodyFont, 3) + 8;
    cy = drawWrappedInk(g, notice.reward(), layout.textX, cy, maxW, bodyFont, 3);

    g.setFont(sealFont);
    g.setColor(new Color(48, 28, 12, 230));
    int sealY = layout.y + layout.h - Math.round(layout.h * 0.15f);
    drawWrappedInk(g, notice.seal(), layout.textX, sealY, Math.round(maxW * 0.72f), sealFont, 2);
  }

  private static void drawInkString(Graphics2D g, String text, int x, int y, Color ink) {
    g.setColor(new Color(255, 248, 235, 140));
    g.drawString(text, x - 1, y);
    g.drawString(text, x + 1, y);
    g.drawString(text, x, y - 1);
    g.drawString(text, x, y + 1);
    g.setColor(new Color(0, 0, 0, 45));
    g.drawString(text, x + 1, y + 1);
    g.setColor(ink);
    g.drawString(text, x, y);
  }

  private static int drawCenteredInk(Graphics2D g, String text, int centerX, int y,
                                       int maxW, Font font, int lineGap) {
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    for (String line : wrap(text, fm, maxW)) {
      int lw = fm.stringWidth(line);
      drawInkString(g, line, centerX - lw / 2, y, g.getColor());
      y += fm.getHeight() + lineGap;
    }
    return y;
  }

  private static int drawWrappedInk(Graphics2D g, String text, int x, int y, int maxW, Font font, int lineGap) {
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    Color ink = g.getColor();
    for (String line : wrap(text, fm, maxW)) {
      drawInkString(g, line, x, y, ink);
      y += fm.getHeight() + lineGap;
    }
    return y;
  }

  private static List<String> wrap(String text, FontMetrics fm, int maxW) {
    List<String> out = new ArrayList<>();
    if (text == null || text.isBlank()) {
      return out;
    }
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
}
