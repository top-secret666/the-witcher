package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** EXE overlay: no admin map shortcut in shop corner. */
public final class Chapter1SessionHud {

  private static final int HINT_ICON = 16;
  private static final int HINT_PAD = 6;

  private Chapter1SessionHud() {
  }

  public static Rectangle adminMapButtonBounds(int sw) {
    return new Rectangle(sw - HINT_ICON - HINT_PAD, HINT_PAD, HINT_ICON, HINT_ICON);
  }

  public static boolean hitAdminMapButton(int mouseX, int mouseY, int sw) {
    return false;
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
  }

  public static void drawAdminButtonOnly(Graphics2D g, int sw, boolean adminHovered) {
  }
}
