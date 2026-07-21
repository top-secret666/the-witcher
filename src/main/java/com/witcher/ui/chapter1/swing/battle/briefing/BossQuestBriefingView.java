package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.chapter1.battle.briefing.BossQuestBriefingController;
import main.java.com.witcher.chapter1.battle.briefing.BossQuestBriefingScript;
import main.java.com.witcher.ui.chapter1.swing.battle.BossVnDialogBoxRenderer;
import main.java.com.witcher.ui.chapter1.swing.battle.BossVnViewChrome;
import main.java.com.witcher.ui.shop.view.ShopLayout;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Брифинг в лавке: панель заказа + диалог + чёрный pixel-dissolve. */
public final class BossQuestBriefingView {

  private BossQuestBriefingView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossQuestBriefingController ctrl,
                          ShopLayout layout, int mouseX, int mouseY) {
    if (ctrl == null || layout == null) {
      return;
    }

    if (ctrl.inTransition()) {
      BufferedImage snap = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
      Graphics2D cg = snap.createGraphics();
      try {
        drawScene(cg, sw, sh, ctrl, layout, mouseX, mouseY, false);
      } finally {
        cg.dispose();
      }
      BossBriefingDissolveRenderer.draw(g, sw, sh, snap, ctrl.dissolveT());
    } else {
      drawScene(g, sw, sh, ctrl, layout, mouseX, mouseY, true);
    }

    if (ctrl.historyOpen()) {
      BossVnViewChrome.drawHistoryOverlay(
          g, sw, sh, ctrl.buttons(), ctrl.historyCloseHovered(),
          ctrl.historyScroll(), ctrl::setHistoryScroll, ctrl.buildHistoryLogLines());
    }
  }

  private static void drawScene(Graphics2D g, int sw, int sh, BossQuestBriefingController ctrl,
                                ShopLayout layout, int mouseX, int mouseY, boolean withDialog) {
    BossQuestBriefingBackdrop.draw(g, sw, sh, layout, ctrl);

    if (ctrl.showNotice()) {
      QuestNoticeRenderer.Layout target = QuestNoticeRenderer.layout(sw, sh);
      QuestNoticeAnimator anim = QuestNoticeAnimator.opening(ctrl.noticeAnimProgress(), target);
      QuestNoticeRenderer.drawPaper(g, sw, sh, anim);
    }

    // Диалог и текст заказа — в renderTextOverlay (поверх CRT).
  }

  /** Диалог брифинга — чёткий UI поверх пост-обработки. */
  public static void drawTextOverlay(Graphics2D g, int sw, int sh, BossQuestBriefingController ctrl,
                                     int mouseX, int mouseY) {
    if (ctrl == null) {
      return;
    }
    if (ctrl.showNotice()) {
      QuestNoticeRenderer.Layout target = QuestNoticeRenderer.layout(sw, sh);
      QuestNoticeAnimator anim = QuestNoticeAnimator.opening(ctrl.noticeAnimProgress(), target);
      QuestNoticeRenderer.drawTextOverlay(g, sw, sh, ctrl.notice(), anim);
    }
    if (ctrl.showDialog()) {
      drawDialogBox(g, sw, sh, ctrl);
      BossVnViewChrome.drawToolbar(g, ctrl.buttons(), ctrl.backEnabled(), ctrl.autoMode(), mouseX, mouseY);
    }
  }

  private static void drawDialogBox(Graphics2D g, int sw, int sh, BossQuestBriefingController ctrl) {
    BossQuestBriefingScript.DialogLine line = ctrl.currentLine();
    if (line == null) {
      return;
    }
    BossVnDialogBoxRenderer.draw(
        g, sw, sh,
        line.speaker(), line.speakerColorRgb(), ctrl.visibleText(),
        ctrl.tickCount(), ctrl.waitingForAdvance(), ctrl.autoMode());
  }
}
