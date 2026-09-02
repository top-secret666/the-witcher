package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Graphics2D;

/** Экран итога боя после катсцены с бликами. */
public final class BattleResultView {

  private BattleResultView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, boolean victory) {
    g.setColor(new Color(4, 3, 2));
    g.fillRect(0, 0, sw, sh);

    g.setFont(GameFonts.get().uiBold(18));
    g.setColor(victory ? new Color(220, 190, 80) : new Color(180, 70, 60));
    String title = victory ? "ПОБЕДА" : "ПОРАЖЕНИЕ";
    int tw = g.getFontMetrics().stringWidth(title);
    g.drawString(title, (sw - tw) / 2, sh / 2 - 8);

    g.setFont(GameFonts.get().uiPlain(9));
    g.setColor(new Color(170, 150, 120));
    String hint = "Клик — вернуться в лавку";
    int hw = g.getFontMetrics().stringWidth(hint);
    g.drawString(hint, (sw - hw) / 2, sh / 2 + 18);
  }
}
