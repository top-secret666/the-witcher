package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.chapter1.battle.briefing.BossQuestBriefingScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;

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

/** Лист заказа по центру экрана + текст пером (Forum). */
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

    int h = Math.round(sh * 0.74f);
    int w = Math.round(h * aspect);
    int maxW = Math.round(sw * 0.56f);
    if (w > maxW) {
      w = maxW;
      h = Math.round(w / aspect);
    }

    int x = (sw - w) / 2;
    int y = Math.round(sh * 0.04f);

    int padX = Math.round(w * 0.12f);
    int padTop = Math.round(h * 0.13f);
    int padBottom = Math.round(h * 0.26f);
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
      drawNoticeText(g, target, notice);
    }

    g.setComposite(prev);
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

    Font headerFont = QuestNoticeFonts.header(Math.max(11, layout.h / 24));
    Font titleFont = QuestNoticeFonts.title(Math.max(13, layout.h / 18));
    Font bodyFont = QuestNoticeFonts.body(Math.max(9, layout.h / 34));
    Font sealFont = QuestNoticeFonts.seal(Math.max(8, layout.h / 38));

    g.setFont(headerFont);
    g.setColor(new Color(58, 32, 14));
    cy = drawCentered(g, notice.header(), centerX, cy, maxW, headerFont, 3) + 8;
    if (cy > bottomLimit) {
      return;
    }

    g.setFont(titleFont);
    g.setColor(new Color(42, 22, 8));
    cy = drawCentered(g, notice.targetName(), centerX, cy, maxW, titleFont, 4) + 10;
    if (cy > bottomLimit) {
      return;
    }

    g.setFont(bodyFont);
    g.setColor(new Color(52, 34, 18));
    cy = drawWrapped(g, notice.threatLevel(), layout.textX, cy, maxW, bodyFont, 3) + 8;
    cy = drawWrapped(g, notice.body(), layout.textX, cy, maxW, bodyFont, 4) + 10;
    cy = drawWrapped(g, notice.reward(), layout.textX, cy, maxW, bodyFont, 3);

    g.setFont(sealFont);
    g.setColor(new Color(68, 44, 22, 210));
    int sealY = layout.y + layout.h - Math.round(layout.h * 0.16f);
    drawWrapped(g, notice.seal(), layout.textX, sealY, Math.round(maxW * 0.68f), sealFont, 2);
  }

  private static int drawCentered(Graphics2D g, String text, int centerX, int y,
                                  int maxW, Font font, int lineGap) {
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    for (String line : wrap(text, fm, maxW)) {
      int lw = fm.stringWidth(line);
      g.drawString(line, centerX - lw / 2, y);
      y += fm.getHeight() + lineGap;
    }
    return y;
  }

  private static int drawWrapped(Graphics2D g, String text, int x, int y, int maxW, Font font, int lineGap) {
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    for (String line : wrap(text, fm, maxW)) {
      g.drawString(line, x, y);
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
