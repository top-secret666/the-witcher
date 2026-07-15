package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.BossCatalog;
import main.java.com.witcher.chapter1.battle.BossEntry;
import main.java.com.witcher.ui.chapter1.view.BossMapLayout;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.UiChrome;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Полноэкранная карта боссов (только отрисовка). */
public final class BossMapView {

  private static final int PORTRAIT_W = 58;
  private static final int PORTRAIT_H = 72;
  private static final Color PANEL_BG = new Color(18, 12, 8, 230);
  private static final Color PANEL_BORDER = new Color(200, 160, 70);
  private static final Color SCANLINE = new Color(0, 0, 0, 55);
  private static final Color NAME_COLOR = new Color(245, 240, 230);
  private static final Color TITLE_COLOR = new Color(200, 175, 140);
  private static final Color STAT_COLOR = new Color(235, 225, 210);

  private BossMapView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossEntry hovered, BossEntry selected,
                          boolean backHovered) {
    g.setColor(new Color(8, 6, 5));
    g.fillRect(0, 0, sw, sh);

    BufferedImage map = Chapter1UiAssets.bossMapOpen();
    if (map != null) {
      g.drawImage(ScaledImageCache.get(map, sw, sh), 0, 0, null);
    } else {
      BufferedImage closed = Chapter1UiAssets.bossMapClosed();
      if (closed != null) {
        g.drawImage(ScaledImageCache.get(closed, sw, sh), 0, 0, null);
      }
    }

    float sx = sw / (float) Chapter1ViewConstants.VIRTUAL_W;
    float sy = sh / (float) Chapter1ViewConstants.VIRTUAL_H;
    int icon = BossMapLayout.BOSS_ICON;
    for (BossEntry boss : BossCatalog.all()) {
      boolean hot = boss.equals(hovered) || boss.equals(selected);
      int size = hot ? icon + 4 : icon;
      int x = Math.round(boss.mapX() * sx) - size / 2;
      int y = Math.round(boss.mapY() * sy) - size / 2;
      BufferedImage iconImg = Chapter1UiAssets.bossMapIcon(boss.activeMapIconPath(hot));
      if (iconImg != null) {
        drawCrisp(g, iconImg, x, y, size, size);
      } else {
        g.setColor(hot ? new Color(200, 60, 50) : new Color(120, 40, 35));
        g.fillOval(x, y, size, size);
        g.setColor(new Color(220, 180, 80));
        g.drawOval(x, y, size - 1, size - 1);
      }
    }

    BossEntry panelBoss = selected != null ? selected : hovered;
    if (panelBoss != null) {
      drawBossPanel(g, sw, sh, panelBoss);
    }

    Rectangle back = BossMapLayout.backButton(sw, sh);
    UiChrome.drawArrowBackButton(g, back, backHovered, 1f);
  }

  private static void drawBossPanel(Graphics2D g, int sw, int sh, BossEntry boss) {
    int pad = 8;
    int textColX = pad + PORTRAIT_W + 10;
    Font nameFont = GameFonts.get().uiBold(11);
    Font titleFont = GameFonts.get().uiPlain(8);
    Font statFont = GameFonts.get().uiPlain(9);
    FontMetrics nameFm = g.getFontMetrics(nameFont);
    FontMetrics titleFm = g.getFontMetrics(titleFont);
    FontMetrics statFm = g.getFontMetrics(statFont);

    String name = boss.name() != null ? boss.name() : "";
    String title = boss.title() != null ? boss.title() : "";
    String[] stats = {
        "Защита " + boss.protection(),
        "Выносл. " + boss.stamina(),
        "Знаки " + boss.signs()
    };

    int textW = Math.max(nameFm.stringWidth(name), titleFm.stringWidth(title));
    for (String s : stats) {
      textW = Math.max(textW, statFm.stringWidth(s));
    }
    int pw = Math.max(150, textColX + textW + pad);
    int headerH = Math.max(PORTRAIT_H, nameFm.getHeight() + titleFm.getHeight() + 6);
    int statsTop = pad + headerH + 6;
    int ph = statsTop + stats.length * (statFm.getHeight() + 2) + pad;
    int px = sw - pw - 10;
    int py = 10;

    g.setColor(PANEL_BG);
    g.fillRect(px, py, pw, ph);
    for (int ly = 1; ly < ph; ly += 2) {
      g.setColor(SCANLINE);
      g.drawLine(px, py + ly, px + pw - 1, py + ly);
    }
    g.setColor(PANEL_BORDER);
    g.drawRect(px, py, pw - 1, ph - 1);

    BufferedImage portrait = Chapter1UiAssets.bossPortrait(boss.portraitPath());
    int portraitX = px + pad;
    int portraitY = py + pad;
    if (portrait != null) {
      drawCrisp(g, portrait, portraitX, portraitY, PORTRAIT_W, PORTRAIT_H);
    } else {
      g.setColor(new Color(60, 40, 30));
      g.fillRect(portraitX, portraitY, PORTRAIT_W, PORTRAIT_H);
    }

    int textX = px + textColX;
    g.setFont(nameFont);
    g.setColor(NAME_COLOR);
    g.drawString(name, textX, py + pad + nameFm.getAscent());
    g.setFont(titleFont);
    g.setColor(TITLE_COLOR);
    g.drawString(title, textX, py + pad + nameFm.getHeight() + titleFm.getAscent());

    g.setFont(statFont);
    g.setColor(STAT_COLOR);
    int sy = py + statsTop + statFm.getAscent();
    for (String s : stats) {
      g.drawString(s, px + pad, sy);
      sy += statFm.getHeight() + 2;
    }
  }

  /** Иконка/портрет без размытия — nearest-neighbor, целые координаты. */
  private static void drawCrisp(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
    if (img == null || w <= 0 || h <= 0) {
      return;
    }
    Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    Object prevRender = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    g.drawImage(img, x, y, w, h, null);
    if (prevInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
    }
    if (prevRender != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRender);
    }
    if (prevAa != null) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
    }
  }
}
