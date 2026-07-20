package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.battle.BossQuestBriefingScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Лист заказа по центру экрана + текст пером (Forum). */
public final class QuestNoticeRenderer {

  /** Разметка пергамента на виртуальном кадре 480×360. */
  public record Layout(int x, int y, int w, int h, int textX, int textY, int textMaxW, int textMaxH) {
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

    int padX = Math.round(w * 0.11f);
    int padTop = Math.round(h * 0.11f);
    int padBottom = Math.round(h * 0.24f);
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
      float alpha) {
    if (notice == null || alpha <= 0.01f) {
      return;
    }

    Layout layout = layout(sw, sh);
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));

    BufferedImage asset = Chapter1UiAssets.bossQuestNotice();
    if (asset != null) {
      BufferedImage scaled = ScaledImageCache.get(asset, layout.w, layout.h);
      if (scaled != null) {
        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(scaled, layout.x, layout.y, null);
        if (interp != null) {
          g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
        }
      }
    }

    drawNoticeText(g, layout, notice);
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

    Font headerFont = QuestNoticeFonts.header(Math.max(11, layout.h / 22));
    Font titleFont = QuestNoticeFonts.title(Math.max(13, layout.h / 16));
    Font bodyFont = QuestNoticeFonts.body(Math.max(9, layout.h / 32));
    Font sealFont = QuestNoticeFonts.seal(Math.max(8, layout.h / 36));

    g.setFont(headerFont);
    g.setColor(new Color(58, 32, 14));
    cy = drawWrapped(g, notice.header(), layout.textX, cy, maxW, headerFont) + 5;
    if (cy > bottomLimit) {
      return;
    }

    g.setFont(titleFont);
    g.setColor(new Color(42, 22, 8));
    cy = drawWrapped(g, notice.targetName(), layout.textX, cy, maxW, titleFont) + 6;
    if (cy > bottomLimit) {
      return;
    }

    g.setFont(bodyFont);
    g.setColor(new Color(52, 34, 18));
    cy = drawWrapped(g, notice.threatLevel(), layout.textX, cy, maxW, bodyFont) + 5;
    cy = drawWrapped(g, notice.body(), layout.textX, cy, maxW, bodyFont) + 7;
    cy = drawWrapped(g, notice.reward(), layout.textX, cy, maxW, bodyFont);

    g.setFont(sealFont);
    g.setColor(new Color(68, 44, 22, 210));
    int sealY = layout.y + layout.h - Math.round(layout.h * 0.17f);
    drawWrapped(g, notice.seal(), layout.textX, sealY, Math.round(maxW * 0.72f), sealFont);
  }

  private static int drawWrapped(Graphics2D g, String text, int x, int y, int maxW, Font font) {
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    for (String line : wrap(text, fm, maxW)) {
      g.drawString(line, x, y);
      y += fm.getHeight() + 2;
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
