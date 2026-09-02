package main.java.com.witcher.ui.chapter1.view;

import main.java.com.witcher.ui.chapter1.presenter.Chapter1Presenter;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Контракт отрисовки главы 1 — реализация Swing: {@link main.java.com.witcher.ui.chapter1.swing.Chapter1SwingView}.
 */
public interface Chapter1View {

  void render(BufferedImage screen, int mouseX, int mouseY, Chapter1Presenter presenter);

  void renderTextOverlay(Graphics2D g, int mouseX, int mouseY, Chapter1Presenter presenter);
}
