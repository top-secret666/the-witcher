package com.witcher.ui.chapter1.swing.battle.briefing;

import com.witcher.chapter1.battle.briefing.BossQuestBriefingController;
import com.witcher.ui.shop.swing.ShopAssetCache;
import com.witcher.ui.shop.view.ShopLayout;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Лавка без витрины: фон + виньетка; портреты и лист — в {@link BossQuestBriefingView}. */
public final class BossQuestBriefingBackdrop {

  private BossQuestBriefingBackdrop() {
  }

  public static void draw(Graphics2D g, int sw, int sh, ShopLayout layout,
                          BossQuestBriefingController ctrl) {
    ShopAssetCache assets = ShopAssetCache.get();

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    drawScaledCenter(g, assets.merchantBackground(), sw, sh, 0.72f);

    BossQuestBriefingController.NoticeFrame frame = ctrl != null
        ? ctrl.noticeFrame()
        : new BossQuestBriefingController.NoticeFrame(
            QuestNoticeRenderer.layout(sw, sh),
            QuestNoticeAnimator.opening(1f, QuestNoticeRenderer.layout(sw, sh)));
    QuestNoticeAnimator paperAnim = frame.anim();
    drawFocusVignette(g, sw, sh, layout, paperAnim);
    // Портреты рисует BossQuestBriefingView поверх листа заказа.
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

  private static void drawFocusVignette(Graphics2D g, int sw, int sh, ShopLayout layout,
                                        QuestNoticeAnimator paper) {
    Composite prev = g.getComposite();
    // Полное затемнение без «окна» вокруг пергамента — иначе светлая прямоугольная рамка.
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.42f));
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, layout.dialogTop);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.22f));
    GradientPaint leftGrad = new GradientPaint(0, 0, new Color(0, 0, 0, 200),
        Math.max(1, sw / 5), 0, new Color(0, 0, 0, 0));
    g.setPaint(leftGrad);
    g.fillRect(0, 0, Math.max(0, sw / 5), layout.dialogTop);
    GradientPaint rightGrad = new GradientPaint(sw - Math.max(1, sw / 5), 0,
        new Color(0, 0, 0, 0), sw, 0, new Color(0, 0, 0, 200));
    g.setPaint(rightGrad);
    g.fillRect(sw - Math.max(0, sw / 5), 0, Math.max(0, sw / 5), layout.dialogTop);
    g.setComposite(prev);
  }
}
