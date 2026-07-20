package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.battle.BossQuestBriefingScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.shop.view.ShopLayout;
import main.java.com.witcher.ui.shop.view.ShopViewConstants;

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

/** Текст контракта поверх центральной {@code shop_catalog_panel}. */
public final class QuestNoticeRenderer {

  private QuestNoticeRenderer() {
  }

  public static void draw(
      Graphics2D g,
      ShopLayout layout,
      BossQuestBriefingScript.NoticeContent notice,
      float alpha) {
    if (notice == null || layout == null || alpha <= 0.01f) {
      return;
    }

    int panelX = layout.panelX;
    int panelY = layout.panelY;
    int panelW = layout.panelW;
    int panelH = layout.panelH;
    int insetX = ShopViewConstants.catalogFrameInsetX(panelW);
    int insetTop = ShopViewConstants.catalogListTopInset(panelH);
    int innerX = panelX + insetX;
    int innerY = panelY + insetTop;
    int innerW = ShopViewConstants.catalogRowContentW(panelW);
    int innerH = panelH - insetTop - Math.round(panelH * 0.14f);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));

    BufferedImage asset = Chapter1UiAssets.bossQuestNotice();
    if (asset != null) {
      BufferedImage scaled = ScaledImageCache.get(asset, innerW, innerH);
      if (scaled != null) {
        g.drawImage(scaled, innerX, innerY, null);
      }
    }

    drawNoticeText(g, innerX, innerY, innerW, innerH, notice);
    g.setComposite(prev);
  }

  private static void drawNoticeText(
      Graphics2D g,
      int x,
      int y,
      int w,
      int h,
      BossQuestBriefingScript.NoticeContent notice) {
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int pad = Math.max(8, w / 18);
    int tx = x + pad;
    int maxW = w - pad * 2;
    int cy = y + pad + 6;

    Font headerFont = GameFonts.get().bold(Math.max(11, h / 24));
    Font titleFont = GameFonts.get().bold(Math.max(13, h / 18));
    Font bodyFont = GameFonts.get().plain(Math.max(9, h / 30));
    Font sealFont = GameFonts.get().italic(Math.max(8, h / 34));

    g.setFont(headerFont);
    g.setColor(new Color(210, 185, 120));
    cy = drawWrapped(g, notice.header(), tx, cy, maxW, headerFont) + 4;

    g.setFont(titleFont);
    g.setColor(new Color(235, 220, 170));
    cy = drawWrapped(g, notice.targetName(), tx, cy, maxW, titleFont) + 4;

    g.setFont(bodyFont);
    g.setColor(new Color(195, 175, 135));
    cy = drawWrapped(g, notice.threatLevel(), tx, cy, maxW, bodyFont) + 6;
    cy = drawWrapped(g, notice.body(), tx, cy, maxW, bodyFont) + 8;
    cy = drawWrapped(g, notice.reward(), tx, cy, maxW, bodyFont) + 10;

    g.setFont(sealFont);
    g.setColor(new Color(165, 140, 95));
    drawWrapped(g, notice.seal(), tx, y + h - pad - 14, maxW, sealFont);
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
