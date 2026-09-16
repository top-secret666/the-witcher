package com.witcher.ui.chapter1.swing.battle.map;

import com.witcher.chapter1.battle.BossCatalog;
import com.witcher.chapter1.battle.BossEntry;
import com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import com.witcher.ui.chapter1.swing.ScaledImageCache;
import com.witcher.ui.chapter1.view.BossMapLayout;
import com.witcher.ui.chapter1.view.Chapter1ViewConstants;
import com.witcher.ui.graphics.DialogBoxRenderer;
import com.witcher.ui.graphics.GameFonts;
import com.witcher.ui.graphics.UiChrome;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;

/** Полноэкранная карта боссов (только отрисовка). */
public final class BossMapView {

  private static final int PORTRAIT_W = 58;
  private static final int PORTRAIT_H = 72;
  private static final Color PANEL_BG = new Color(18, 12, 8, 230);
  private static final Color PANEL_BORDER = new Color(200, 160, 70);
  private static final Color SCANLINE = new Color(0, 0, 0, 55);
  private static final Color NAME_COLOR = new Color(245, 240, 230);
  private static final Color DESC_COLOR = new Color(235, 225, 210);

  private BossMapView() {
  }

  /** Тиков на половину цикла мигания (~0.4 с при 30 fps). */
  private static final int ATTENTION_HALF_CYCLE = 12;

  public static void draw(Graphics2D g, int sw, int sh, BossEntry hovered, BossEntry selected,
                          boolean backHovered, boolean attentionBlink, int attentionTick) {
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
    boolean pulseHot = attentionBlink && attentionHoverPhase(attentionTick);
    for (BossEntry boss : BossCatalog.all()) {
      boolean hot = boss.equals(hovered) || boss.equals(selected) || pulseHot;
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
      drawBossPanelBackground(g, sw, panelBoss);
      drawBossPanelText(g, sw, panelBoss);
    }

    Rectangle back = BossMapLayout.backButton(sw, sh);
    UiChrome.drawArrowBackButton(g, back, backHovered, 1f);
  }

  /**
   * Раньше текст шёл в text-overlay (чёткий поверх CRT).
   * Теперь пусто: имя/описание рисуются в {@link #draw} и расплываются с картой.
   */
  public static void drawTextOverlay(Graphics2D g, int sw, int sh, BossEntry boss) {
    // no-op — текст в scene buffer
  }

  private static void drawBossPanelText(Graphics2D g, int sw, BossEntry boss) {
    BossPanelLayout layout = layoutPanel(g, sw, boss);
    GameFonts.applyGothicHints(g);

    int textX = layout.px + layout.textColX;
    g.setFont(layout.nameFont);
    GameFonts.drawOutlined(g, layout.name, textX,
        layout.py + layout.pad + layout.nameFm.getAscent(), NAME_COLOR);

    g.setFont(layout.descFont);
    int sy = layout.py + layout.descTop + layout.descFm.getAscent();
    for (String line : layout.descLines) {
      GameFonts.drawOutlined(g, line, textX, sy, DESC_COLOR);
      sy += layout.descFm.getHeight() + 2;
    }
  }

  private static void drawBossPanelBackground(Graphics2D g, int sw, BossEntry boss) {
    BossPanelLayout layout = layoutPanel(g, sw, boss);
    int px = layout.px;
    int py = layout.py;
    int pw = layout.pw;
    int ph = layout.ph;

    g.setColor(PANEL_BG);
    g.fillRect(px, py, pw, ph);
    for (int ly = 1; ly < ph; ly += 2) {
      g.setColor(SCANLINE);
      g.drawLine(px, py + ly, px + pw - 1, py + ly);
    }
    g.setColor(PANEL_BORDER);
    g.drawRect(px, py, pw - 1, ph - 1);

    BufferedImage portrait = Chapter1UiAssets.bossPortrait(boss.portraitPath());
    int portraitX = px + layout.pad;
    int portraitY = py + layout.pad;
    if (portrait != null) {
      drawCrisp(g, portrait, portraitX, portraitY, PORTRAIT_W, PORTRAIT_H);
    } else {
      g.setColor(new Color(60, 40, 30));
      g.fillRect(portraitX, portraitY, PORTRAIT_W, PORTRAIT_H);
    }
  }

  private static BossPanelLayout layoutPanel(Graphics2D g, int sw, BossEntry boss) {
    int pad = 8;
    int textColX = pad + PORTRAIT_W + 10;
    Font nameFont = GameFonts.get().uiBold(12);
    Font descFont = GameFonts.get().uiPlain(9);
    FontMetrics nameFm = g.getFontMetrics(nameFont);
    FontMetrics descFm = g.getFontMetrics(descFont);

    String name = boss.name() != null ? boss.name() : "";
    String description = boss.description() != null ? boss.description() : "";
    // Описание сразу под «Волк», в колонке справа от портрета.
    int descMaxW = Math.max(120, Math.min(200, sw / 2 - textColX));
    List<String> descLines = DialogBoxRenderer.wrapLine(description, descFm, descMaxW);

    int textBlockH = nameFm.getHeight() + 4
        + Math.max(1, descLines.size()) * (descFm.getHeight() + 2);
    int pw = Math.max(pad + PORTRAIT_W + 10 + Math.max(nameFm.stringWidth(name), descMaxW) + pad, 150);
    int ph = pad + Math.max(PORTRAIT_H, textBlockH) + pad;
    int descTop = pad + nameFm.getHeight() + 4;
    int px = sw - pw - 10;
    int py = 10;
    return new BossPanelLayout(px, py, pw, ph, pad, textColX, descTop,
        name, descLines, nameFont, descFont, nameFm, descFm);
  }

  private record BossPanelLayout(
      int px, int py, int pw, int ph, int pad, int textColX, int descTop,
      String name, List<String> descLines,
      Font nameFont, Font descFont,
      FontMetrics nameFm, FontMetrics descFm) {
  }

  /** true → показывать hover-вариант медальона. */
  private static boolean attentionHoverPhase(int tick) {
    return ((Math.floorDiv(Math.max(0, tick), ATTENTION_HALF_CYCLE)) % 2) == 0;
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
