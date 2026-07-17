package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.battle.BossEncounterController;
import main.java.com.witcher.chapter1.battle.BossEncounterScript;
import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.chapter1.swing.EyelidOverlay;
import main.java.com.witcher.ui.chapter1.swing.ScaledImageCache;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.UiChrome;
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
import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Лес + злодей под веками. После открытия — VN как в интро (окно + Назад/История/Авто),
 * спрайт volk_duke_* по центру, эмоции по репликам. Фон не меняется.
 */
public final class BossEncounterView {

  private BossEncounterView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, BossEncounterController encounter,
                          int mouseX, int mouseY) {
    if (encounter == null) {
      g.setColor(Color.BLACK);
      g.fillRect(0, 0, sw, sh);
      return;
    }

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    drawForestBackground(g, sw, sh);
    drawCenterPortrait(g, sw, sh, encounter);

    if (encounter.showDialog()) {
      drawDialogBox(g, sw, sh, encounter);
      drawVnButtons(g, encounter, mouseX, mouseY);
    }

    if (encounter.historyOpen()) {
      drawHistoryOverlay(g, sw, sh, encounter);
    }

    EyelidOverlay.renderBlack(g, sw, sh, encounter.eyelidOpenT());
  }

  private static void drawForestBackground(Graphics2D g, int sw, int sh) {
    BufferedImage forest = Chapter1UiAssets.bossWakeForest();
    if (forest == null) {
      return;
    }
    BufferedImage bg = ScaledImageCache.get(forest, sw, sh);
    if (bg != null) {
      g.drawImage(bg, 0, 0, null);
    }
  }

  private static void drawCenterPortrait(
      Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    BufferedImage portrait = Chapter1UiAssets.volkDukeSprite(encounter.spritePathForScene());
    if (portrait == null) {
      return;
    }
    // Крупнее в диалоге — почти на весь экран.
    float baseCharScale = (sh * 0.96f) / Math.max(1, portrait.getHeight());
    float charScale = baseCharScale * encounter.portraitScale();
    int cw = Math.round(portrait.getWidth() * charScale);
    int ch = Math.round(portrait.getHeight() * charScale);
    int dialogZone = Math.round(sh * 0.15f);
    int x = (sw - cw) / 2;
    int y = sh - dialogZone - ch + Math.round(ch * 0.15f);
    drawIntroSharp(g, portrait, x, y, cw, ch);
  }

  private static void drawDialogBox(Graphics2D g, int sw, int sh, BossEncounterController encounter) {
    BossEncounterScript.DialogEntry entry = encounter.currentEntry();
    if (entry == null) {
      return;
    }
    DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
    Color speakerColor = entry.speaker() == null
        ? DialogBoxRenderer.NARRATOR_COLOR
        : new Color((entry.speakerColorRgb() >> 16) & 0xff,
            (entry.speakerColorRgb() >> 8) & 0xff,
            entry.speakerColorRgb() & 0xff);

    String visibleText = encounter.visibleText();
    int lineY = DialogBoxRenderer.drawTypewriterText(
        g, entry.speaker(), visibleText, speakerColor, layout, 1f);

    if (!encounter.waitingForAdvance() && (encounter.tickCount() / 8) % 2 == 0) {
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

    if (encounter.waitingForAdvance() && !encounter.autoMode()
        && (encounter.tickCount() / 15) % 2 == 0) {
      DialogBoxRenderer.drawHint(g, "\u25B6 Enter", layout, layout.fontSize, 1f);
    } else if (encounter.waitingForAdvance() && encounter.autoMode()
        && (encounter.tickCount() / 12) % 2 == 0) {
      DialogBoxRenderer.drawHint(g, "Авто \u25B6", layout, layout.fontSize, 0.85f);
    }
  }

  private static void drawVnButtons(Graphics2D g, BossEncounterController encounter,
                                    int mouseX, int mouseY) {
    IntroVnUi.ButtonLayout b = encounter.buttons();
    drawVnTextButton(g, toRect(b.backButton), "Назад",
        encounter.backEnabled(), false,
        encounter.backEnabled() && b.backButton.contains(mouseX, mouseY));
    drawVnTextButton(g, toRect(b.historyButton), "История",
        true, false, b.historyButton.contains(mouseX, mouseY));
    drawVnTextButton(g, toRect(b.autoButton), "Авто",
        true, encounter.autoMode(), b.autoButton.contains(mouseX, mouseY));
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
                                         BossEncounterController encounter) {
    g.setColor(new Color(0, 0, 0, 158));
    g.fillRect(0, 0, sw, sh);

    IntroVnUi.ButtonLayout b = encounter.buttons();
    Rectangle panel = toRect(b.historyPanel);
    DialogBoxRenderer.drawBox(g, panel.x, panel.y, panel.width, panel.height, 1f);

    Rectangle closeBounds = toRect(b.historyClose);
    UiChrome.drawCloseButton(g, closeBounds, encounter.historyCloseHovered(), 1f);

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    IntroHistoryLayout.Metrics m = IntroHistoryLayout.compute(
        sw, sh, panel.x, panel.y, panel.width, panel.height);

    Font titleFont = GameFonts.get().bold(m.titleSize);
    Font hintFont = GameFonts.get().italic(m.hintSize);
    Font bodyFont = GameFonts.get().plain(m.fontSize);
    g.setFont(bodyFont);
    FontMetrics fm = g.getFontMetrics();
    List<String> renderedLines = IntroHistoryText.buildRenderedLines(
        encounter.buildHistoryLogLines(), Math.round(m.textMaxW), m.fontSize);
    int maxScroll = IntroHistoryLayout.maxScroll(renderedLines.size(), m.lineH, m.contentH);
    int historyScroll = IntroHistoryLayout.clampScroll(encounter.historyScroll(), maxScroll);
    encounter.setHistoryScroll(historyScroll);

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

  /** Та же интерполяция, что у персонажей интро — максимальная чёткость painted-арта. */
  private static void drawIntroSharp(Graphics2D g, BufferedImage img, int x, int y, int w, int h) {
    Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    Object prevRender = g.getRenderingHint(RenderingHints.KEY_RENDERING);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g.drawImage(img, x, y, w, h, null);
    if (prevInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
    } else {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }
    if (prevRender != null) {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRender);
    } else {
      g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    }
  }
}
