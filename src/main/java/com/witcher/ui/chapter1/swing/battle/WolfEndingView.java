package main.java.com.witcher.ui.chapter1.swing.battle;

import main.java.com.witcher.chapter1.ending.WolfEndingType;
import main.java.com.witcher.chapter1.shop.BossMemoryFragments;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Экран итога первого босса — плохая петля или истинный осколок. */
public final class WolfEndingView {

  private WolfEndingView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, WolfEndingType type) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

    if (type == WolfEndingType.TRUE_SHARD) {
      drawTrueShard(g, sw, sh);
    } else {
      drawBadLoop(g, sw, sh);
    }

    g.setFont(GameFonts.get().uiPlain(9));
    g.setColor(new Color(140, 130, 110));
    String hint = "Клик — вернуться в лавку";
    int hw = g.getFontMetrics().stringWidth(hint);
    g.drawString(hint, (sw - hw) / 2, sh - Math.round(sh * 0.08f));

    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
  }

  private static void drawTrueShard(Graphics2D g, int sw, int sh) {
    g.setFont(GameFonts.get().uiBold(16));
    g.setColor(new Color(180, 210, 240));
    drawCentered(g, "ОСКОЛОК ПРОБУЖДЁН", sw, sh / 2 - 48);

    g.setFont(GameFonts.get().uiPlain(10));
    g.setColor(new Color(200, 185, 150));
    drawCentered(g, "Воспоминание: фрагмент " + BossMemoryFragments.wolfFragmentCode(), sw, sh / 2 - 20);
    drawCentered(g, "Петля треснула. Иллюзия больше не бесспорна.", sw, sh / 2 + 4);

    g.setFont(GameFonts.get().uiBold(12));
    g.setColor(new Color(220, 190, 90));
    drawCentered(g, "…ПРОДОЛЖЕНИЕ СЛЕДУЕТ…", sw, sh / 2 + 36);
  }

  private static void drawBadLoop(Graphics2D g, int sw, int sh) {
    g.setFont(GameFonts.get().uiBold(16));
    g.setColor(new Color(150, 70, 65));
    drawCentered(g, "ПЕТЛЯ", sw, sh / 2 - 28);

    g.setFont(GameFonts.get().uiPlain(10));
    g.setColor(new Color(170, 150, 130));
    drawCentered(g, "Вы поверили хозяину лавки.", sw, sh / 2 + 2);
    drawCentered(g, "Утро начинается снова — с прилавка и улыбки.", sw, sh / 2 + 20);
  }

  private static void drawCentered(Graphics2D g, String line, int sw, int y) {
    int tw = g.getFontMetrics().stringWidth(line);
    g.drawString(line, (sw - tw) / 2, y);
  }
}
