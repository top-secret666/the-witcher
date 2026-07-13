package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Иконка карты боя в инвентаре лавки. */
public final class BattleCardRevealView {

  private BattleCardRevealView() {
  }

  public static void drawCardIcon(Graphics2D g, int x, int y, int size, boolean hovered) {
    BufferedImage icon = Chapter1UiAssets.cardIcon();
    if (icon != null) {
      g.drawImage(ScaledImageCache.get(icon, size, size), x, y, null);
    } else {
      g.setColor(new Color(100, 30, 25));
      g.fillRoundRect(x, y, size, size, 6, 6);
      g.setColor(new Color(220, 180, 80));
      g.drawRoundRect(x, y, size - 1, size - 1, 6, 6);
    }
    if (hovered) {
      g.setColor(new Color(255, 230, 140, 160));
      g.drawRect(x - 1, y - 1, size + 2, size + 2);
    }
  }
}
