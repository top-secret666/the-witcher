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
      g.setColor(hovered ? new Color(120, 40, 32) : new Color(100, 30, 25));
      g.fillRoundRect(x, y, size, size, 6, 6);
    }
  }
}
