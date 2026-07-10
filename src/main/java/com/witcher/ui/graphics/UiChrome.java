package main.java.com.witcher.ui.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import main.java.com.witcher.ui.shop.swing.ShopImageBounds;

/** Маленькие UI-элементы: стрелка «назад», крестик закрытия. */
public final class UiChrome {

  public static final int BTN_SIZE = 18;

  private static final String BASE = "/assets/sprites/lavka/";
  private static final String BAKED_UI = BASE + "1x/ui/";

  private static BufferedImage closeNormal;
  private static BufferedImage closeHover;
  private static BufferedImage backNormal;
  private static BufferedImage backHover;
  private static boolean assetsLoaded;

  private UiChrome() {
  }

  public static Rectangle closeButtonRect(int panelX, int panelY, int panelW) {
    return new Rectangle(panelX + panelW - BTN_SIZE - 6, panelY + 5, BTN_SIZE, BTN_SIZE);
  }

  public static void drawCloseButton(Graphics2D g, Rectangle r, boolean hovered, float alpha) {
    if (alpha <= 0.01f || r.width <= 0) {
      return;
    }
    ensureAssets();
    BufferedImage img = hovered ? closeHover : closeNormal;
    if (img != null) {
      drawSpriteButton(g, r, img, alpha);
      return;
    }
    drawVectorCloseButton(g, r, hovered, alpha);
  }

  /** Прогрев спрайтов кнопок на экране загрузки. */
  public static void warmup() {
    ensureAssets();
  }

  public static void drawArrowBackButton(Graphics2D g, Rectangle r, boolean hovered, float alpha) {
    if (alpha <= 0.01f || r.width <= 0) {
      return;
    }
    ensureAssets();
    BufferedImage img = hovered ? backHover : backNormal;
    if (img != null) {
      drawSpriteButton(g, r, img, alpha);
      return;
    }
    drawVectorBackButton(g, r, hovered, alpha);
  }

  private static void ensureAssets() {
    if (assetsLoaded) {
      return;
    }
    assetsLoaded = true;
    closeNormal = loadChromeBtn(BAKED_UI + "ui_btn_close.png", BASE + "ui/ui_btn_close.png");
    closeHover = loadChromeBtn(BAKED_UI + "ui_btn_close_hover.png", BASE + "ui/ui_btn_close_hover.png");
    backNormal = loadChromeBtn(BAKED_UI + "ui_btn_back.png", BASE + "ui/ui_btn_back.png");
    backHover = loadChromeBtn(BAKED_UI + "ui_btn_back_hover.png", BASE + "ui/ui_btn_back_hover.png");
  }

  private static BufferedImage loadChromeBtn(String bakedPath, String fallbackPath) {
    BufferedImage baked = loadImage(bakedPath);
    if (baked != null && baked.getWidth() == BTN_SIZE && baked.getHeight() == BTN_SIZE) {
      return baked;
    }
    BufferedImage src = loadImage(fallbackPath);
    if (src == null) {
      return baked;
    }
    Rectangle box = ShopImageBounds.compute(src);
    return PixelScaler.crispScaleRegion(src, box, BTN_SIZE, BTN_SIZE);
  }

  private static BufferedImage loadImage(String path) {
    Sprite sprite = Sprite.loadOptional(path);
    return sprite != null ? sprite.getImage() : null;
  }

  private static void drawSpriteButton(Graphics2D g, Rectangle r, BufferedImage img, float alpha) {
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    int x = r.x + (r.width - img.getWidth()) / 2;
    int y = r.y + (r.height - img.getHeight()) / 2;
    g.drawImage(img, x, y, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }

  private static void drawVectorCloseButton(Graphics2D g, Rectangle r, boolean hovered, float alpha) {
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    GameFonts.applyGameHints(g);

    Color fill = hovered ? new Color(72, 28, 24, 235) : new Color(32, 18, 12, 220);
    Color border = hovered ? new Color(220, 120, 90) : new Color(150, 90, 55);
    Color mark = hovered ? new Color(255, 210, 180) : new Color(220, 175, 140);

    g.setColor(fill);
    g.fillRoundRect(r.x, r.y, r.width, r.height, 4, 4);
    g.setColor(border);
    g.drawRoundRect(r.x, r.y, r.width, r.height, 4, 4);

    int pad = 5;
    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g.setColor(mark);
    g.drawLine(r.x + pad, r.y + pad, r.x + r.width - pad, r.y + r.height - pad);
    g.drawLine(r.x + r.width - pad, r.y + pad, r.x + pad, r.y + r.height - pad);
    g.setStroke(new BasicStroke(1f));

    g.setComposite(prev);
  }

  private static void drawVectorBackButton(Graphics2D g, Rectangle r, boolean hovered, float alpha) {
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    GameFonts.applyGameHints(g);

    Color fill = hovered ? new Color(48, 32, 14, 235) : new Color(24, 16, 8, 220);
    Color border = hovered ? new Color(210, 165, 70) : new Color(140, 100, 45);
    Color mark = hovered ? new Color(255, 235, 170) : new Color(220, 195, 140);

    g.setColor(fill);
    g.fillRoundRect(r.x, r.y, r.width, r.height, 4, 4);
    g.setColor(border);
    g.drawRoundRect(r.x, r.y, r.width, r.height, 4, 4);

    int cx = r.x + r.width / 2 + 1;
    int cy = r.y + r.height / 2;
    g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g.setColor(mark);
    g.drawLine(cx + 3, cy, cx - 4, cy - 5);
    g.drawLine(cx + 3, cy, cx - 4, cy + 5);
    g.drawLine(cx - 4, cy, cx + 1, cy);
    g.setStroke(new BasicStroke(1f));

    g.setComposite(prev);
  }
}
