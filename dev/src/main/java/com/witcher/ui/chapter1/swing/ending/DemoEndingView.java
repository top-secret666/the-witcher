package com.witcher.ui.chapter1.swing.ending;

import com.witcher.chapter1.ending.DemoEndingController;
import com.witcher.chapter1.ending.DemoEndingCredits;
import com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** Экраны конца демо: Directed by → титры → спасибо. */
public final class DemoEndingView {

  private DemoEndingView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, DemoEndingController ctrl) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);
    switch (ctrl.step()) {
      case DIRECTED -> drawDirected(g, sw, sh);
      case CREDITS -> drawCredits(g, sw, sh, ctrl.creditsScrollY());
      case THANKS -> drawThanks(g, sw, sh);
      default -> { }
    }
  }

  private static void drawDirected(Graphics2D g, int sw, int sh) {
    GameFonts.applyGothicHints(g);
    g.setFont(new Font("Times New Roman", Font.PLAIN, 13));
    g.setColor(Color.WHITE);
    drawCentered(g, "Directed by", sw, sh / 2 - 18);
    g.setFont(new Font("Times New Roman", Font.BOLD, 18));
    drawCentered(g, DemoEndingCredits.DIRECTOR_NICK, sw, sh / 2 + 10);
  }

  private static void drawCredits(Graphics2D g, int sw, int sh, float scrollY) {
    GameFonts.applyGothicHints(g);
    g.setFont(GameFonts.get().uiPlain(10));
    FontMetrics fm = g.getFontMetrics();
    int lineH = fm.getHeight() + 6;
    // Лесенка снизу вверх: строки поднимаются из-за нижнего края.
    int startY = sh + 20 - Math.round(scrollY);
    g.setColor(new Color(235, 230, 220));
    for (int i = 0; i < DemoEndingCredits.lines().length; i++) {
      String line = DemoEndingCredits.lines()[i];
      int y = startY + i * lineH;
      if (y < -lineH || y > sh + lineH) {
        continue;
      }
      drawCentered(g, line, sw, y);
    }
  }

  private static void drawThanks(Graphics2D g, int sw, int sh) {
    GameFonts.applyGothicHints(g);
    int y = 28;
    g.setFont(GameFonts.get().uiBold(13));
    g.setColor(new Color(220, 190, 90));
    y = drawWrappedCentered(g, "Поздравляем! Вы прошли демо!", sw, y, sw - 40, 16) + 6;

    g.setFont(GameFonts.get().uiBold(11));
    g.setColor(new Color(235, 225, 200));
    y = drawWrappedCentered(g, "Спасибо за игру! / Thanks for playing!", sw, y, sw - 36, 14) + 8;

    g.setFont(GameFonts.get().uiPlain(8));
    g.setColor(new Color(200, 185, 155));
    y = drawWrappedCentered(g,
        "Вы прошли демо The Witcher: Core Logic Engine. "
            + "Это ранний прототип боевой системы и чистой архитектуры.",
        sw, y, sw - 32, 12) + 6;
    y = drawWrappedCentered(g,
        "Это первая и маленькая попытка сделать свою игру ;)",
        sw, y, sw - 32, 12) + 6;
    y = drawWrappedCentered(g,
        "Нашли баг или есть идея? Оставьте отзыв на GitHub — это очень поможет.",
        sw, y, sw - 32, 12) + 10;

    g.setFont(GameFonts.get().uiBold(9));
    g.setColor(new Color(220, 190, 90));
    y = drawWrappedCentered(g, "Связаться / код", sw, y, sw - 32, 12) + 4;
    g.setFont(GameFonts.get().uiPlain(8));
    g.setColor(new Color(210, 200, 180));
    y = drawWrappedCentered(g, DemoEndingCredits.GITHUB_URL, sw, y, sw - 24, 11) + 2;
    y = drawWrappedCentered(g, DemoEndingCredits.LINKEDIN_URL, sw, y, sw - 20, 10) + 2;
    y = drawWrappedCentered(g, DemoEndingCredits.TELEGRAM_LABEL, sw, y, sw - 24, 11) + 12;

    g.setFont(GameFonts.get().uiBold(9));
    g.setColor(new Color(255, 225, 150));
    drawWrappedCentered(g, "[ Нажмите ESC для выхода в главное меню ]", sw, sh - 24, sw - 24, 12);
  }

  private static void drawCentered(Graphics2D g, String line, int sw, int y) {
    if (line == null || line.isEmpty()) {
      return;
    }
    int tw = g.getFontMetrics().stringWidth(line);
    int tx = (sw - tw) / 2;
    g.drawString(line, tx, y);
  }

  private static int drawWrappedCentered(Graphics2D g, String text, int sw, int y, int maxW, int lineGap) {
    FontMetrics fm = g.getFontMetrics();
    for (String line : wrap(text, fm, maxW)) {
      drawCentered(g, line, sw, y + fm.getAscent());
      y += fm.getHeight() + lineGap;
    }
    return y;
  }

  private static String[] wrap(String text, FontMetrics fm, int maxW) {
    if (text == null || text.isBlank()) {
      return new String[] {""};
    }
    java.util.List<String> lines = new java.util.ArrayList<>();
    String[] words = text.split("\\s+");
    StringBuilder row = new StringBuilder();
    for (String word : words) {
      String candidate = row.isEmpty() ? word : row + " " + word;
      if (fm.stringWidth(candidate) <= maxW) {
        row = new StringBuilder(candidate);
      } else {
        if (!row.isEmpty()) {
          lines.add(row.toString());
        }
        row = new StringBuilder(word);
      }
    }
    if (!row.isEmpty()) {
      lines.add(row.toString());
    }
    return lines.toArray(String[]::new);
  }
}
