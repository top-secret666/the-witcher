package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.ui.shop.swing.ShopAssetCache;
import main.java.com.witcher.ui.shop.view.ShopLayout;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/** Лавка без витрины: фон + портреты; лист заказа рисует {@link QuestNoticeRenderer}. */
public final class BossQuestBriefingBackdrop {

  private BossQuestBriefingBackdrop() {
  }

  public static void draw(Graphics2D g, int sw, int sh, ShopLayout layout) {
    ShopAssetCache assets = ShopAssetCache.get();

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    drawScaledCenter(g, assets.merchantBackground(), sw, sh, 0.72f);
    drawFocusVignette(g, sw, sh, layout);

    drawPortrait(g, sw, layout.dialogTop, assets.geraltPortrait(), true);
    drawPortrait(g, sw, layout.dialogTop, assets.dukePortrait(), false);

    if (assets.hudBarImage() != null) {
      Composite prev = g.getComposite();
      g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
      g.drawImage(assets.hudBarImage(), layout.hudX, layout.hudY, layout.hudW, layout.hudH, null);
      g.setComposite(prev);
    }
  }

  private static void drawScaledCenter(Graphics2D g, BufferedImage img, int sw, int sh, float alpha) {
    if (img == null) {
      return;
    }
    int x = (sw - img.getWidth()) / 2;
    int y = (sh - img.getHeight()) / 2;
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    g.drawImage(img, x, y, null);
    g.setComposite(prev);
  }

  private static void drawFocusVignette(Graphics2D g, int sw, int sh, ShopLayout layout) {
    QuestNoticeRenderer.Layout paper = QuestNoticeRenderer.layout(sw, sh);

    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f));
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.32f));
    int pad = 10;
    g.fillRoundRect(paper.x() - pad, paper.y() - pad,
        paper.w() + pad * 2, paper.h() + pad * 2, 8, 8);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.26f));
    GradientPaint left = new GradientPaint(0, 0, new Color(0, 0, 0, 210),
        paper.x() - 12, 0, new Color(0, 0, 0, 0));
    g.setPaint(left);
    g.fillRect(0, 0, Math.max(0, paper.x() - 6), layout.dialogTop);
    GradientPaint right = new GradientPaint(paper.x() + paper.w() + 12, 0,
        new Color(0, 0, 0, 0), sw, 0, new Color(0, 0, 0, 210));
    g.setPaint(right);
    g.fillRect(paper.x() + paper.w() + 6, 0, sw - paper.x() - paper.w() - 6, layout.dialogTop);
    g.setComposite(prev);
  }

  private static void drawPortrait(Graphics2D g, int sw, int dialogTop,
                                   BufferedImage sprite, boolean left) {
    if (sprite == null) {
      return;
    }
    int cw = sprite.getWidth();
    int ch = sprite.getHeight();
    int baseY = dialogTop - ch + Math.round(ch * 0.12f);
    int cx = left ? -Math.round(cw * 0.12f) : sw - cw + Math.round(cw * 0.12f);
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
    g.drawImage(sprite, cx, baseY, null);
    if (interp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
    }
    g.setComposite(prev);
  }
}
