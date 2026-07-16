package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Graphics2D;

/** Отладочный HUD счётчиков главы 1 (угол экрана). */
public final class Chapter1SessionHud {

  private Chapter1SessionHud() {
  }

  public static void draw(Graphics2D g, int sw, Chapter1Session session) {
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
}
