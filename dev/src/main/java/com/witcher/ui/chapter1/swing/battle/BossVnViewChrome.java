package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.UiChrome;
import main.java.com.witcher.ui.graphics.UiRect;
import main.java.com.witcher.ui.intro.VnButtonLabels;
import main.java.com.witcher.ui.intro.IntroHistoryText;
import main.java.com.witcher.ui.intro.IntroVnUi;
import main.java.com.witcher.ui.intro.view.IntroHistoryLayout;
import main.java.com.witcher.ui.intro.view.IntroHistoryTheme;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.util.List;
import java.util.function.IntConsumer;

/** Общие VN-кнопки и оверлей «История» для босс-сцен главы 1. */
public final class BossVnViewChrome {

  private BossVnViewChrome() {
  }

  public static void drawToolbar(
      Graphics2D g,
      IntroVnUi.ButtonLayout buttons,
      boolean backEnabled,
      boolean autoMode,
      int mouseX,
      int mouseY) {
    drawTextButton(g, UiRect.toAwt(buttons.backButton.x, buttons.backButton.y,
        buttons.backButton.width, buttons.backButton.height),
        VnButtonLabels.BACK, backEnabled, false,
        backEnabled && buttons.backButton.contains(mouseX, mouseY));
    drawTextButton(g, UiRect.toAwt(buttons.historyButton.x, buttons.historyButton.y,
        buttons.historyButton.width, buttons.historyButton.height),
        VnButtonLabels.HISTORY, true, false,
        buttons.historyButton.contains(mouseX, mouseY));
    drawTextButton(g, UiRect.toAwt(buttons.autoButton.x, buttons.autoButton.y,
        buttons.autoButton.width, buttons.autoButton.height),
        VnButtonLabels.AUTO, true, autoMode,
        buttons.autoButton.contains(mouseX, mouseY));
  }

  public static void drawHistoryOverlay(
      Graphics2D g,
      int sw,
      int sh,
      IntroVnUi.ButtonLayout buttons,
      boolean closeHovered,
      int historyScroll,
      IntConsumer scrollSink,
      List<String> logLines) {
    g.setColor(new Color(0, 0, 0, 158));
    g.fillRect(0, 0, sw, sh);

    Rectangle panel = toRect(buttons.historyPanel);
    DialogBoxRenderer.drawBox(g, panel.x, panel.y, panel.width, panel.height, 1f);

    Rectangle closeBounds = toRect(buttons.historyClose);
    UiChrome.drawCloseButton(g, closeBounds, closeHovered, 1f);

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    IntroHistoryLayout.Metrics m = IntroHistoryLayout.compute(
        sw, sh, panel.x, panel.y, panel.width, panel.height);

    Font titleFont = GameFonts.get().bold(m.titleSize);
    Font hintFont = GameFonts.get().italic(m.hintSize);
    Font bodyFont = GameFonts.get().plain(m.fontSize);
    g.setFont(bodyFont);
    FontMetrics fm = g.getFontMetrics();
    List<String> renderedLines = IntroHistoryText.buildRenderedLines(
        logLines, Math.round(m.textMaxW), m.fontSize);
    int maxScroll = IntroHistoryLayout.maxScroll(renderedLines.size(), m.lineH, m.contentH);
    int clampedScroll = IntroHistoryLayout.clampScroll(historyScroll, maxScroll);
    if (scrollSink != null) {
      scrollSink.accept(clampedScroll);
    }

    g.setFont(titleFont);
    g.setColor(new Color(IntroHistoryTheme.TITLE_R, IntroHistoryTheme.TITLE_G, IntroHistoryTheme.TITLE_B));
    g.drawString(VnButtonLabels.HISTORY, Math.round(m.textX), m.titleBaseline);

    g.setColor(new Color(IntroHistoryTheme.DIVIDER_R, IntroHistoryTheme.DIVIDER_G,
        IntroHistoryTheme.DIVIDER_B, IntroHistoryTheme.DIVIDER_A));
    g.drawLine(Math.round(m.textX), m.headerBottom - 4, Math.round(m.textX + m.textMaxW), m.headerBottom - 4);
    g.drawLine(Math.round(m.textX), m.footerTop, Math.round(m.textX + m.textMaxW), m.footerTop);

    Shape oldClip = g.getClip();
    g.clipRect(Math.round(m.textX), m.contentTop, Math.round(m.textMaxW), m.contentH);
    g.setFont(bodyFont);
    int y = m.contentTop + fm.getAscent() - clampedScroll;
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

  private static void drawTextButton(
      Graphics2D g,
      Rectangle r,
      String label,
      boolean enabled,
      boolean active,
      boolean hover) {
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

  public static Rectangle toRect(IntroVnUi.Rect r) {
    return UiRect.toAwt(r.x, r.y, r.width, r.height);
  }
}
