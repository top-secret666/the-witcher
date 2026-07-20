package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.battle.BossQuestBriefingController;
import main.java.com.witcher.chapter1.battle.BossQuestBriefingScript;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.shop.view.ShopLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
      QuestNoticeRenderer.draw(g, sw, sh, ctrl.notice(), anim);
    }

    if (withDialog && ctrl.showDialog()) {
      drawDialogBox(g, sw, sh, ctrl);
      BossVnViewChrome.drawToolbar(g, ctrl.buttons(), ctrl.backEnabled(), ctrl.autoMode(), mouseX, mouseY);
    }
  }

  private static void drawDialogBox(Graphics2D g, int sw, int sh, BossQuestBriefingController ctrl) {
    BossQuestBriefingScript.DialogLine line = ctrl.currentLine();
    if (line == null) {
      return;
    }
    DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
    Color speakerColor = line.speaker() == null
        ? DialogBoxRenderer.NARRATOR_COLOR
        : new Color((line.speakerColorRgb() >> 16) & 0xff,
            (line.speakerColorRgb() >> 8) & 0xff,
            line.speakerColorRgb() & 0xff);

    String visibleText = ctrl.visibleText();
    int lineY = DialogBoxRenderer.drawTypewriterText(
        g, line.speaker(), visibleText, speakerColor, layout, 1f);

    if (!ctrl.waitingForAdvance() && (ctrl.tickCount() / 8) % 2 == 0) {
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      Font textFont = GameFonts.get().plain(layout.fontSize);
      g.setFont(textFont);
      FontMetrics fm = g.getFontMetrics();
      int cursorX = layout.textX + fm.stringWidth(
          DialogBoxRenderer.getLastVisibleLine(visibleText, fm, layout.textMaxW));
      g.setColor(speakerColor);
      g.fillRect(cursorX + 2, lineY - fm.getAscent() + 2,
          Math.max(2, layout.fontSize / 5), fm.getAscent());
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    if (ctrl.waitingForAdvance() && !ctrl.autoMode()
        && (ctrl.tickCount() / 15) % 2 == 0) {
      DialogBoxRenderer.drawHint(g, "\u25B6 Enter", layout, layout.fontSize, 1f);
    } else if (ctrl.waitingForAdvance() && ctrl.autoMode()
        && (ctrl.tickCount() / 12) % 2 == 0) {
      DialogBoxRenderer.drawHint(g, "Авто \u25B6", layout, layout.fontSize, 0.85f);
    }
  }
}
