package com.witcher.ui.chapter1.swing.battle;

import com.witcher.ui.graphics.DialogBoxRenderer;
import com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Общая отрисовка typewriter-окна для босс-VN (брифинг и лес). */
public final class BossVnDialogBoxRenderer {

  private BossVnDialogBoxRenderer() {
  }

  public static void draw(
      Graphics2D g,
      int sw,
      int sh,
      String speaker,
      int speakerColorRgb,
      String visibleText,
      int tickCount,
      boolean waitingForAdvance,
      boolean autoMode) {
    draw(g, sw, sh, speaker, speakerColorRgb, visibleText, tickCount, waitingForAdvance, autoMode, true);
  }

  public static void draw(
      Graphics2D g,
      int sw,
      int sh,
      String speaker,
      int speakerColorRgb,
      String visibleText,
      int tickCount,
      boolean waitingForAdvance,
      boolean autoMode,
      boolean drawBackgroundBox) {
    DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
    Color speakerColor = speaker == null
        ? DialogBoxRenderer.NARRATOR_COLOR
        : new Color((speakerColorRgb >> 16) & 0xff,
            (speakerColorRgb >> 8) & 0xff,
            speakerColorRgb & 0xff);

    int baselineY = DialogBoxRenderer.drawTypewriterText(
        g, speaker, visibleText, speakerColor, layout, 1f, drawBackgroundBox);

    if (!waitingForAdvance && (tickCount / 8) % 2 == 0) {
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
      Font textFont = GameFonts.get().plain(layout.fontSize);
      g.setFont(textFont);
      FontMetrics fm = g.getFontMetrics();
      int cursorX = layout.textX + fm.stringWidth(
          DialogBoxRenderer.getLastVisibleLine(visibleText, fm, layout.textMaxW));
      g.setColor(speakerColor);
      int caretTop = DialogBoxRenderer.typewriterCaretTop(baselineY, fm);
      int caretH = DialogBoxRenderer.typewriterCaretHeight(fm, layout.fontSize);
      g.fillRect(cursorX + 2, caretTop, Math.max(2, layout.fontSize / 5), caretH);
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    if (waitingForAdvance && !autoMode && (tickCount / 15) % 2 == 0) {
      DialogBoxRenderer.drawHint(g, "Enter", layout, layout.fontSize, 1f);
    } else if (waitingForAdvance && autoMode && (tickCount / 12) % 2 == 0) {
      DialogBoxRenderer.drawHint(g, "Авто", layout, layout.fontSize, 0.85f);
    }
  }
}
