package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.PixelScaler;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Иконка карты боя на сумке / в углу HUD. */
public final class BattleCardRevealView {

  private BattleCardRevealView() {
  }

  public static void drawCardIcon(Graphics2D g, int x, int y, int size, boolean hovered) {
    BufferedImage icon = Chapter1UiAssets.cardIcon();
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

  /**
   * Карта появляется в центре и улетает в слот сумки (как кошелёк).
   *
   * @param ticks      кадр анимации
   * @param appearEnd  конец фазы появления
   * @param flyEnd     конец полёта к сумке
   * @param fadeEnd    конец затухания в сумке
   */
  public static void drawFlyingCard(
      Graphics2D g,
      int sw,
      int sh,
      int bagCenterX,
      int bagCenterY,
      int ticks,
      int appearEnd,
      int flyEnd,
      int fadeEnd) {
    if (ticks > fadeEnd) {
      return;
    }

    float appearT = smoothstep(ticks / (float) appearEnd);
    float maxSize = 88f;
    float minSize = 16f;

    int centerX = sw / 2;
    int centerY = sh / 2 + 20;

    float px;
    float py;
    float pw;
    float alpha;

    if (ticks <= appearEnd) {
      pw = maxSize * (0.28f + appearT * 0.72f);
      px = centerX - pw / 2f;
      py = centerY - pw / 2f;
      alpha = Math.min(1f, appearT * 1.1f);
    } else {
      float posT = smoothstep((ticks - appearEnd) / (float) (flyEnd - appearEnd));
      float sizeT = posT * posT * (3f - 2f * posT);
      pw = maxSize + (minSize - maxSize) * sizeT;
      float cx = centerX + (bagCenterX - centerX) * posT;
      float cy = centerY + (bagCenterY - centerY) * posT;
      px = cx - pw / 2f;
      py = cy - pw / 2f;
      alpha = 1f;
      if (ticks > flyEnd) {
        float fadeT = smoothstep((ticks - flyEnd) / (float) (fadeEnd - flyEnd));
        pw = minSize;
        px = bagCenterX - pw / 2f;
        py = bagCenterY - pw / 2f;
        alpha = Math.max(0f, 1f - fadeT);
      }
    }

    if (alpha <= 0.02f) {
      return;
    }

    int ipw = Math.round(pw);
    int ipx = Math.round(px);
    int ipy = Math.round(py);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

    BufferedImage card = Chapter1UiAssets.cardClosed();
    if (card != null) {
      g.drawImage(PixelScaler.sharpScale(card, ipw, Math.round(ipw * 1.33f)), ipx, ipy, null);
    } else {
      int cardH = Math.round(ipw * 1.33f);
      g.setColor(new Color(120, 40, 30));
      g.fillRoundRect(ipx, ipy, ipw, cardH, 8, 8);
      g.setColor(new Color(220, 180, 80));
      g.drawRoundRect(ipx, ipy, ipw - 1, cardH - 1, 8, 8);
    }

    g.setComposite(prev);
  }

  public static void drawDukeHint(Graphics2D g, int sh) {
    g.setFont(GameFonts.get().uiPlain(9));
    g.setColor(new Color(240, 220, 170));
    g.drawString("Герцог: Вот карта. Выбери, с кем хочешь встретиться.", 24, sh - 48);
  }

  private static float smoothstep(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return c * c * (3f - 2f * c);
  }
}
