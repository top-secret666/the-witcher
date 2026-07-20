package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.battle.BossQuestBriefingScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;
import main.java.com.witcher.ui.graphics.GameFonts;

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

/** Лист заказа с доски — PNG-ассет или процедурный пергамент. */
public final class QuestNoticeRenderer {

  private QuestNoticeRenderer() {
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
    int panelW = Math.round(sw * 0.42f);
    int panelH = Math.round(sh * 0.72f);
    int x = Math.round(sw * 0.06f);
    int y = Math.round(sh * 0.08f);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));

    BufferedImage asset = Chapter1UiAssets.bossQuestNotice();
    if (asset != null) {
      BufferedImage scaled = ScaledImageCache.get(asset, panelW, panelH);
      if (scaled != null) {
        g.drawImage(scaled, x, y, null);
      }
    } else {
      drawProceduralParchment(g, x, y, panelW, panelH);
    }
    drawNoticeText(g, x, y, panelW, panelH, notice);
    g.setComposite(prev);
  }

  private static void drawProceduralParchment(Graphics2D g, int x, int y, int w, int h) {
    g.setColor(new Color(214, 196, 158));
    g.fillRoundRect(x, y, w, h, 10, 10);
    g.setColor(new Color(120, 88, 48));
    g.drawRoundRect(x, y, w, h, 10, 10);
    g.setColor(new Color(0, 0, 0, 35));
    g.drawRoundRect(x + 6, y + 6, w - 12, h - 12, 8, 8);
    for (int i = 0; i < 6; i++) {
      g.setColor(new Color(160, 130, 90, 40 + i * 8));
      g.drawLine(x + 12, y + 18 + i * 9, x + w - 12, y + 20 + i * 9);
    }
  }

  private static void drawNoticeText(
      Graphics2D g,
      int x,
      int y,
      int w,
      int h,
      BossQuestBriefingScript.NoticeContent notice) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int pad = Math.max(10, w / 14);
    int tx = x + pad;
    int maxW = w - pad * 2;
    int cy = y + pad + 8;

    Font headerFont = GameFonts.get().bold(Math.max(11, h / 22));
    Font titleFont = GameFonts.get().bold(Math.max(13, h / 16));
    Font bodyFont = GameFonts.get().plain(Math.max(9, h / 28));
    Font sealFont = GameFonts.get().italic(Math.max(8, h / 32));

    g.setFont(headerFont);
    g.setColor(new Color(90, 30, 20));
    cy = drawWrapped(g, notice.header(), tx, cy, maxW, headerFont) + 6;

    g.setFont(titleFont);
    g.setColor(new Color(40, 20, 10));
    cy = drawWrapped(g, notice.targetName(), tx, cy, maxW, titleFont) + 4;

    g.setFont(bodyFont);
    g.setColor(new Color(70, 45, 25));
    cy = drawWrapped(g, notice.threatLevel(), tx, cy, maxW, bodyFont) + 8;
    cy = drawWrapped(g, notice.body(), tx, cy, maxW, bodyFont) + 10;
    cy = drawWrapped(g, notice.reward(), tx, cy, maxW, bodyFont) + 12;

    g.setFont(sealFont);
    g.setColor(new Color(110, 70, 35));
    drawWrapped(g, notice.seal(), tx, y + h - pad - 16, maxW, sealFont);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  private static int drawWrapped(Graphics2D g, String text, int x, int y, int maxW, Font font) {
    g.setFont(font);
    FontMetrics fm = g.getFontMetrics();
    for (String line : wrap(text, fm, maxW)) {
      g.drawString(line, x, y);
      y += fm.getHeight() + 1;
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
