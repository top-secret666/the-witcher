package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.battle.BossQuestBriefingController;
import main.java.com.witcher.chapter1.battle.BossQuestBriefingScript;
import main.java.com.witcher.ui.chapter1.swing.WakeVisionRenderer;
import main.java.com.witcher.ui.chapter1.swing.glitch.CutsceneNoiseOverlay;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.UiChrome;
import main.java.com.witcher.ui.intro.IntroHistoryText;
import main.java.com.witcher.ui.intro.IntroVnUi;
import main.java.com.witcher.ui.intro.view.IntroHistoryLayout;
import main.java.com.witcher.ui.intro.view.IntroHistoryTheme;
import main.java.com.witcher.ui.shop.view.ShopLayout;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.List;

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
      drawDissolve(g, sw, sh, snap, ctrl.dissolveT());
    } else {
      drawScene(g, sw, sh, ctrl, layout, mouseX, mouseY, true);
    }

    if (ctrl.historyOpen()) {
      drawHistoryOverlay(g, sw, sh, ctrl);
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
      drawVnButtons(g, ctrl, mouseX, mouseY);
    }
  }

  /** Обратный wolf_shard_reveal: резкость → пиксельный шум → чёрный. */
  private static void drawDissolve(Graphics2D g, int sw, int sh, BufferedImage snap, float dissolveT) {
    float clarity = Math.max(0f, 1f - dissolveT);
    WakeVisionRenderer.drawFrame(g, snap, 0, 0, clarity);

    float noise = dissolveT <= 0.01f ? 0f : (1f - clarity) * 0.95f + 0.08f;
    if (noise > 0.02f) {
      CutsceneNoiseOverlay.draw(g, sw, sh, Math.min(1f, noise));
    }

    if (clarity < 0.48f) {
      float fog = (0.48f - clarity) * 0.62f;
      var prev = g.getComposite();
      g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, fog));
      g.setColor(new Color(0, 0, 0));
      g.fillRect(0, 0, sw, sh);
      g.setComposite(prev);
    }

    if (dissolveT > 0.68f) {
      float extra = (dissolveT - 0.68f) / 0.32f;
      g.setColor(new Color(0, 0, 0, Math.round(255 * Math.min(1f, extra))));
      g.fillRect(0, 0, sw, sh);
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

  private static void drawVnButtons(Graphics2D g, BossQuestBriefingController ctrl,
                                    int mouseX, int mouseY) {
    IntroVnUi.ButtonLayout b = ctrl.buttons();
    drawVnTextButton(g, toRect(b.backButton), "Назад",
        ctrl.backEnabled(), false,
        ctrl.backEnabled() && b.backButton.contains(mouseX, mouseY));
    drawVnTextButton(g, toRect(b.historyButton), "История",
        true, false, b.historyButton.contains(mouseX, mouseY));
    drawVnTextButton(g, toRect(b.autoButton), "Авто",
        true, ctrl.autoMode(), b.autoButton.contains(mouseX, mouseY));
  }

  private static void drawVnTextButton(Graphics2D g, Rectangle r, String label, boolean enabled,
                                       boolean active, boolean hover) {
    int alpha255 = enabled ? 255 : 130;
    Color textColor;
    if (!enabled) {
      textColor = new Color(95, 80, 58, alpha255);
    } else if (active) {
      textColor = new Color(255, 225, 130, alpha255);
    } else if (hover) {
      textColor = new Color(255, 235, 170, alpha255);
    } else {
      textColor = new Color(205, 180, 115, alpha255);
    }

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    int fontSize = Math.max(10, r.height - 8);
    g.setFont(active ? GameFonts.get().bold(fontSize) : GameFonts.get().plain(fontSize));
    FontMetrics fm = g.getFontMetrics();
    int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
    int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;

    if (hover && enabled) {
      g.setColor(new Color(0, 0, 0, 100));
      g.drawString(label, tx + 1, ty + 1);
    }
    g.setColor(textColor);
    g.drawString(label, tx, ty);

    if ((hover || active) && enabled) {
      int ulY = ty + 2;
      g.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), 180));
      g.drawLine(tx, ulY, tx + fm.stringWidth(label), ulY);
    }
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  private static void drawHistoryOverlay(Graphics2D g, int sw, int sh,
                                         BossQuestBriefingController ctrl) {
    g.setColor(new Color(0, 0, 0, 158));
    g.fillRect(0, 0, sw, sh);

    IntroVnUi.ButtonLayout b = ctrl.buttons();
    Rectangle panel = toRect(b.historyPanel);
    DialogBoxRenderer.drawBox(g, panel.x, panel.y, panel.width, panel.height, 1f);

    Rectangle closeBounds = toRect(b.historyClose);
    UiChrome.drawCloseButton(g, closeBounds, ctrl.historyCloseHovered(), 1f);

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    IntroHistoryLayout.Metrics m = IntroHistoryLayout.compute(
        sw, sh, panel.x, panel.y, panel.width, panel.height);

    Font titleFont = GameFonts.get().bold(m.titleSize);
    Font hintFont = GameFonts.get().italic(m.hintSize);
    Font bodyFont = GameFonts.get().plain(m.fontSize);
    g.setFont(bodyFont);
    FontMetrics fm = g.getFontMetrics();
    List<String> renderedLines = IntroHistoryText.buildRenderedLines(
        ctrl.buildHistoryLogLines(), Math.round(m.textMaxW), m.fontSize);
    int maxScroll = IntroHistoryLayout.maxScroll(renderedLines.size(), m.lineH, m.contentH);
    int historyScroll = IntroHistoryLayout.clampScroll(ctrl.historyScroll(), maxScroll);
    ctrl.setHistoryScroll(historyScroll);

    g.setFont(titleFont);
    g.setColor(new Color(IntroHistoryTheme.TITLE_R, IntroHistoryTheme.TITLE_G, IntroHistoryTheme.TITLE_B));
    g.drawString("История", Math.round(m.textX), m.titleBaseline);

    g.setColor(new Color(IntroHistoryTheme.DIVIDER_R, IntroHistoryTheme.DIVIDER_G,
        IntroHistoryTheme.DIVIDER_B, IntroHistoryTheme.DIVIDER_A));
    g.drawLine(Math.round(m.textX), m.headerBottom - 4, Math.round(m.textX + m.textMaxW), m.headerBottom - 4);
    g.drawLine(Math.round(m.textX), m.footerTop, Math.round(m.textX + m.textMaxW), m.footerTop);

    Shape oldClip = g.getClip();
    g.clipRect(Math.round(m.textX), m.contentTop, Math.round(m.textMaxW), m.contentH);
    g.setFont(bodyFont);
    int y = m.contentTop + fm.getAscent() - historyScroll;
    for (String line : renderedLines) {
      if (y > m.contentBottom) {
        break;
      }
      if (y + fm.getDescent() >= m.contentTop) {
        g.setColor(new Color(220, 200, 160));
        g.drawString(line, Math.round(m.textX), y);
      }
      y += m.lineH;
    }
    g.setClip(oldClip);

    g.setFont(hintFont);
    g.setColor(new Color(IntroHistoryTheme.HINT_R, IntroHistoryTheme.HINT_G, IntroHistoryTheme.HINT_B));
    g.drawString("Колесо — прокрутка", Math.round(m.textX), m.hintBaseline);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  private static Rectangle toRect(IntroVnUi.Rect r) {
    return new Rectangle(Math.round(r.x), Math.round(r.y), Math.round(r.width), Math.round(r.height));
  }
}
