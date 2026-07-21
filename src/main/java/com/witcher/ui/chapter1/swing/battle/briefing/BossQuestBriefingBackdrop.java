package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.chapter1.battle.briefing.BossQuestBriefingController;
import main.java.com.witcher.ui.shop.swing.ShopAssetCache;
import main.java.com.witcher.ui.shop.view.ShopLayout;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Лавка без витрины: фон + intro-портреты; лист заказа рисует {@link QuestNoticeRenderer}. */
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

    if (ctrl != null && ctrl.showDialog()) {
      BriefingCharacterRenderer.draw(g, sw, sh, ctrl);
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

  private static void drawFocusVignette(Graphics2D g, int sw, int sh, ShopLayout layout,
                                        QuestNoticeAnimator paper) {
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.40f));
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.32f));
    int pad = 10;
    g.fillRoundRect(paper.x - pad, paper.y - pad,
        paper.w + pad * 2, paper.h + pad * 2, 8, 8);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.26f));
    GradientPaint left = new GradientPaint(0, 0, new Color(0, 0, 0, 210),
        paper.x - 12, 0, new Color(0, 0, 0, 0));
    g.setPaint(left);
    g.fillRect(0, 0, Math.max(0, paper.x - 6), layout.dialogTop);
    GradientPaint right = new GradientPaint(paper.x + paper.w + 12, 0,
        new Color(0, 0, 0, 0), sw, 0, new Color(0, 0, 0, 210));
    g.setPaint(right);
    g.fillRect(paper.x + paper.w + 6, 0, sw - paper.x - paper.w - 6, layout.dialogTop);
    g.setComposite(prev);
  }
}
