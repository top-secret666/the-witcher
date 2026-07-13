package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.BossCatalog;
import main.java.com.witcher.chapter1.battle.BossEntry;
import main.java.com.witcher.ui.chapter1.view.BossMapLayout;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.PixelScaler;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Полноэкранная карта боссов (только отрисовка). */
public final class BossMapView {

  private BossMapView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossEntry hovered, BossEntry selected) {
    g.setColor(new Color(8, 6, 5));
    g.fillRect(0, 0, sw, sh);

    BufferedImage map = Chapter1UiAssets.bossMapOpen();
    if (map != null) {
      g.drawImage(PixelScaler.sharpScale(map, sw, sh), 0, 0, null);
    } else {
      BufferedImage closed = Chapter1UiAssets.bossMapClosed();
      if (closed != null) {
        g.drawImage(PixelScaler.sharpScale(closed, sw, sh), 0, 0, null);
      }
    }

    float sx = sw / (float) Chapter1ViewConstants.VIRTUAL_W;
    float sy = sh / (float) Chapter1ViewConstants.VIRTUAL_H;
    int icon = BossMapLayout.BOSS_ICON;
    for (BossEntry boss : BossCatalog.all()) {
      boolean hot = boss.equals(hovered) || boss.equals(selected);
      int x = Math.round(boss.mapX() * sx) - icon / 2;
      int y = Math.round(boss.mapY() * sy) - icon / 2;
      BufferedImage iconImg = Chapter1UiAssets.bossMapIcon(boss.mapIconPath());
      if (iconImg != null) {
        g.drawImage(PixelScaler.sharpScale(iconImg, icon, icon), x, y, null);
      } else {
        g.setColor(hot ? new Color(200, 60, 50) : new Color(120, 40, 35));
        g.fillOval(x, y, icon, icon);
        g.setColor(new Color(220, 180, 80));
        g.drawOval(x, y, icon - 1, icon - 1);
      }
      if (hot) {
        g.setColor(new Color(255, 220, 120, 180));
        g.drawRect(x - 2, y - 2, icon + 3, icon + 3);
      }
    }

    BossEntry panelBoss = selected != null ? selected : hovered;
    if (panelBoss != null) {
      drawBossPanel(g, sw, sh, panelBoss);
    }

    g.setFont(GameFonts.get().uiPlain(8));
    g.setColor(new Color(200, 190, 170));
    g.drawString("Выберите противника — клик для боя", 12, sh - 10);
  }

  private static void drawBossPanel(Graphics2D g, int sw, int sh, BossEntry boss) {
    int pw = 150;
    int ph = 120;
    int px = sw - pw - 10;
    int py = 10;
    g.setColor(new Color(20, 14, 10, 220));
    g.fillRoundRect(px, py, pw, ph, 6, 6);
    g.setColor(new Color(180, 140, 60));
    g.drawRoundRect(px, py, pw, ph, 6, 6);

    BufferedImage portrait = Chapter1UiAssets.bossPortrait(boss.portraitPath());
    int portraitSize = 56;
    int portraitX = px + 8;
    int portraitY = py + 8;
    if (portrait != null) {
      g.drawImage(PixelScaler.sharpScale(portrait, portraitSize, portraitSize),
          portraitX, portraitY, null);
    } else {
      g.setColor(new Color(60, 40, 30));
      g.fillRect(portraitX, portraitY, portraitSize, portraitSize);
    }

    g.setFont(GameFonts.get().uiBold(9));
    g.setColor(new Color(240, 210, 150));
    g.drawString(boss.name(), px + 72, py + 20);
    g.setFont(GameFonts.get().uiPlain(8));
    g.setColor(new Color(190, 170, 140));
    g.drawString(boss.title(), px + 72, py + 34);
    g.drawString("Защита " + boss.protection(), px + 10, py + 78);
    g.drawString("Выносл. " + boss.stamina(), px + 10, py + 92);
    g.drawString("Знаки " + boss.signs(), px + 10, py + 106);
  }
}
