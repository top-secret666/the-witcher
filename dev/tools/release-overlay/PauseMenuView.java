package main.java.com.witcher.ui.pause;

import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

/** Оверлей паузы поверх игрового кадра. */
public final class PauseMenuView {

  private static final Color DIM = new Color(0, 0, 0, 150);
  private static final Color PANEL_BG = new Color(18, 12, 8, 235);
  private static final Color PANEL_BORDER = new Color(200, 160, 70);
  private static final Color TITLE = new Color(255, 220, 140);
  private static final Color BODY = new Color(210, 190, 155);
  private static final Color BTN_IDLE = new Color(32, 22, 12, 230);
  private static final Color BTN_HOT = new Color(55, 38, 14, 240);
  private static final Color BTN_BORDER_IDLE = new Color(140, 100, 45);
  private static final Color BTN_BORDER_HOT = new Color(230, 185, 80);
  private static final Color BTN_TEXT_IDLE = new Color(220, 195, 140);
  private static final Color BTN_TEXT_HOT = new Color(255, 240, 190);

  private PauseMenuView() {
  }

  public static void draw(Graphics2D g, int sw, int sh, PauseMenuController menu) {
    menu.layout(sw, sh);
    g.setColor(DIM);
    g.fillRect(0, 0, sw, sh);

    Rectangle panel = menu.panelBounds(sw, sh);
    g.setColor(PANEL_BG);
    g.fillRoundRect(panel.x, panel.y, panel.width, panel.height, 8, 8);
    g.setColor(PANEL_BORDER);
    g.drawRoundRect(panel.x, panel.y, panel.width - 1, panel.height - 1, 8, 8);

    Object prevAa = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    Font titleFont = GameFonts.get().uiBold(14);
    g.setFont(titleFont);
    FontMetrics tfm = g.getFontMetrics();
    String title = menu.title();
    int titleX = panel.x + (panel.width - tfm.stringWidth(title)) / 2;
    int titleY = panel.y + 16 + tfm.getAscent();
    g.setColor(TITLE);
    g.drawString(title, titleX, titleY);

    if (menu.mode() == PauseMenuController.Mode.CONFIRM_MAIN) {
      Font bodyFont = GameFonts.get().uiPlain(11);
      g.setFont(bodyFont);
      FontMetrics bfm = g.getFontMetrics();
      g.setColor(BODY);
      String[] lines = menu.confirmBody().split("\n");
      int bodyY = titleY + 10 + bfm.getAscent();
      for (String line : lines) {
        int x = panel.x + (panel.width - bfm.stringWidth(line)) / 2;
        g.drawString(line, x, bodyY);
        bodyY += bfm.getHeight();
      }
    }

    Font btnFont = GameFonts.get().uiBold(11);
    g.setFont(btnFont);
    FontMetrics bfm = g.getFontMetrics();
    for (int i = 0; i < menu.buttonCount(); i++) {
      Rectangle r = menu.button(i);
      boolean hot = menu.selected(i);
      g.setColor(hot ? BTN_HOT : BTN_IDLE);
      g.fillRoundRect(r.x, r.y, r.width, r.height, 5, 5);
      g.setColor(hot ? BTN_BORDER_HOT : BTN_BORDER_IDLE);
      g.drawRoundRect(r.x, r.y, r.width - 1, r.height - 1, 5, 5);
      String label = menu.label(i);
      g.setColor(hot ? BTN_TEXT_HOT : BTN_TEXT_IDLE);
      g.drawString(label, r.x + (r.width - bfm.stringWidth(label)) / 2,
          r.y + (r.height + bfm.getAscent() - bfm.getDescent()) / 2);
    }
    if (prevAa != null) {
      g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, prevAa);
    }
  }
}
