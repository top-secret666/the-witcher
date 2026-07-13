package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.view.Chapter1AssetPaths;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.PixelScaler;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Сцена выдачи карты боя (аналог кошелька). */
public final class BattleCardRevealView {

  private BattleCardRevealView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, float progress) {
    float dim = Math.min(0.75f, progress * 1.2f);
    g.setColor(new Color(0, 0, 0, Math.round(200 * dim)));
    g.fillRect(0, 0, sw, sh);

    float flyT = Math.min(1f, Math.max(0f, (progress - 0.15f) / 0.55f));
    float alpha = Math.min(1f, progress * 1.4f);

    int cardW = Math.round(96 + flyT * 40);
    int cardH = Math.round(128 + flyT * 52);
    int startX = sw / 2;
    int startY = sh / 2 + 40;
    int endX = 28;
    int endY = 28;
    int cx = Math.round(startX + (endX - startX) * flyT);
    int cy = Math.round(startY + (endY - startY) * flyT);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

    BufferedImage card = load(Chapter1AssetPaths.CARD_CLOSED);
    if (card != null) {
      g.drawImage(PixelScaler.sharpScale(card, cardW, cardH), cx, cy, null);
    } else {
      g.setColor(new Color(120, 40, 30));
      g.fillRoundRect(cx, cy, cardW, cardH, 8, 8);
      g.setColor(new Color(220, 180, 80));
      g.drawRoundRect(cx, cy, cardW - 1, cardH - 1, 8, 8);
    }

    if (progress > 0.08f) {
      g.setFont(GameFonts.get().uiPlain(9));
      g.setColor(new Color(240, 220, 170));
      g.drawString("Герцог: Вот карта. Выбери, с кем хочешь встретиться.", 24, sh - 48);
    }

    g.setComposite(prev);
  }

  public static void drawCardIcon(Graphics2D g, int x, int y, int size, boolean hovered) {
    BufferedImage icon = load(Chapter1AssetPaths.CARD_ICON);
    if (icon != null) {
      g.drawImage(PixelScaler.sharpScale(icon, size, size), x, y, null);
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

  private static BufferedImage load(String path) {
    Sprite sprite = Sprite.loadOptional(path);
    return sprite != null ? sprite.getImage() : null;
  }
}
