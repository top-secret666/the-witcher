package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** Отладочный HUD счётчиков главы 1 + админ-кнопка в уголке. */
public final class Chapter1SessionHud {

  private static final int HINT_ICON = 16;
  private static final int HINT_PAD = 6;

  private Chapter1SessionHud() {
  }

  /** Хитбокс админ-кнопки (правый верхний угол) — переход на карту боссов. */
  public static Rectangle adminMapButtonBounds(int sw) {
    return new Rectangle(sw - HINT_ICON - HINT_PAD, HINT_PAD, HINT_ICON, HINT_ICON);
  }

  public static boolean hitAdminMapButton(int mouseX, int mouseY, int sw) {
    return adminMapButtonBounds(sw).contains(mouseX, mouseY);
  }

  public static void draw(Graphics2D g, int sw, Chapter1Session session) {
    draw(g, sw, session, false);
  }

  public static void draw(Graphics2D g, int sw, Chapter1Session session, boolean adminHovered) {
    if (session == null || g == null) {
      return;
    }
    g.setFont(GameFonts.get().uiPlain(8));
    g.setColor(new Color(180, 200, 220, 200));
    int y = 10;
    int line = 11;
    g.drawString("Виток " + session.loop(), 6, y);
    y += line;
    g.drawString("Плен " + session.prison(), 6, y);
    y += line;
    g.drawString("Подозр. " + session.suspicion() + " / Дов. " + session.trust(), 6, y);
    y += line;
    g.drawString("Фрагм. " + session.fragmentCount() + "/4", 6, y);
    if (session.terminalAccessGranted()) {
      y += line;
      g.setColor(new Color(120, 255, 160, 220));
      g.drawString("~ терминал", 6, y);
    }

    drawAdminButtonOnly(g, sw, adminHovered);
  }

  /** Только кнопка перехода на карту боссов — без боковых счётчиков (для лавки). */
  public static void drawAdminButtonOnly(Graphics2D g, int sw, boolean adminHovered) {
    if (g == null) {
      return;
    }
    BufferedImage hint = Chapter1UiAssets.hiddenHint();
    Rectangle r = adminMapButtonBounds(sw);
    if (hint != null) {
      if (adminHovered) {
        g.setColor(new Color(255, 220, 120, 90));
        g.fillRect(r.x - 2, r.y - 2, r.width + 4, r.height + 4);
      }
      g.drawImage(hint, r.x, r.y, r.width, r.height, null);
    } else {
      g.setColor(adminHovered ? new Color(255, 200, 80, 220) : new Color(200, 80, 80, 200));
      g.fillRect(r.x, r.y, r.width, r.height);
    }
  }
}
