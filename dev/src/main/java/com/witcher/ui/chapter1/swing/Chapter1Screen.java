package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Director;
import main.java.com.witcher.ui.chapter1.presenter.Chapter1Input;
import main.java.com.witcher.ui.chapter1.presenter.Chapter1Presenter;
import main.java.com.witcher.ui.chapter1.view.Chapter1View;
import main.java.com.witcher.ui.graphics.GameWindow;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.swing.ShopScreen;

import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

/**
 * Фасад главы 1 для {@link GameWindow}.
 * Логика — {@link Chapter1Presenter}, отрисовка — {@link Chapter1SwingView}.
 */
public final class Chapter1Screen {

  private final Chapter1Presenter presenter;
  private final Chapter1View view;

  public Chapter1Screen() {
    this(Chapter1Director.loadOrNew(), ShopModel.createNewSession());
  }

  public Chapter1Screen(Chapter1Director director, ShopModel shopModel) {
    presenter = new Chapter1Presenter(director, shopModel);
    view = new Chapter1SwingView();
  }

  public Chapter1Director director() {
    return presenter.director();
  }

  public ShopModel shopModel() {
    return presenter.shopModel();
  }

  public ShopScreen shopScreen() {
    return presenter.shopScreen();
  }

  public void beginAfterIntro() {
    presenter.beginAfterIntro();
  }

  public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
    presenter.update(new Chapter1Input(mouseX, mouseY, clicked, escPressed, wheelNotches));
  }

  public void keyPressed(KeyEvent e) {
    presenter.keyPressed(e);
  }

  public void keyTyped(KeyEvent e) {
    presenter.keyTyped(e);
  }

  public void render(BufferedImage screen, int mouseX, int mouseY) {
    view.render(screen, mouseX, mouseY, presenter);
  }

  public void renderTextOverlay(Graphics2D g, int mouseX, int mouseY) {
    view.renderTextOverlay(g, mouseX, mouseY, presenter);
  }

  public boolean isExitRequested() {
    return presenter.isExitRequested();
  }

  public void clearExitRequest() {
    presenter.clearExitRequest();
  }

  public boolean isChapterComplete() {
    return presenter.isChapterComplete();
  }
}
