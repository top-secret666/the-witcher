package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.cutscene.CutsceneSkipPolicy;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** Подсказка «Пробел — пропустить» внизу экрана катсцены. */
public final class CutsceneSkipHint {

  private CutsceneSkipHint() {
  }

  public static void draw(Graphics2D g, int sw, int sh, boolean visible) {
    if (!visible) {
      return;
    }
    Font font = GameFonts.get().uiPlain(10);
    FontMetrics fm = g.getFontMetrics(font);
    String text = CutsceneSkipPolicy.HINT_TEXT;
    int x = (sw - fm.stringWidth(text)) / 2;
    int y = sh - 10;
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
    g.setFont(font);
    g.setColor(new Color(8, 8, 10, 180));
    g.fillRoundRect(x - 6, y - fm.getAscent() - 2, fm.stringWidth(text) + 12, fm.getHeight() + 2, 4, 4);
    g.setColor(new Color(210, 215, 225));
    g.drawString(text, x, y);
    g.setComposite(prev);
  }
}
