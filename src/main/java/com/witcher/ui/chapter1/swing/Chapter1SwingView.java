package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Phase;
import main.java.com.witcher.ui.chapter1.presenter.Chapter1Presenter;
import main.java.com.witcher.ui.chapter1.view.Chapter1View;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/** Отрисовка кадра главы 1 (аналог {@link main.java.com.witcher.ui.shop.swing.ShopSwingView}). */
public final class Chapter1SwingView implements Chapter1View {

  @Override
  public void render(BufferedImage screen, int mouseX, int mouseY, Chapter1Presenter presenter) {
    int sw = screen.getWidth();
    int sh = screen.getHeight();

    switch (presenter.director().phase()) {
      case CUTSCENE -> {
        Graphics2D g = screen.createGraphics();
        try {
          g.setColor(Color.BLACK);
          g.fillRect(0, 0, sw, sh);
          presenter.cutscenePlayer().render(g, sw, sh);
        } finally {
          g.dispose();
        }
      }
      case LOOP_SEQUENCE, LOOP_HOLD -> LoopSequenceView.draw(
          screen, sw, sh,
          presenter.loopSequence(),
          presenter.eyesEffect(),
          presenter.loopCutscenePlayer());
      case SHOP -> {
        presenter.shopScreen().render(screen, mouseX, mouseY);
        if (presenter.isDukeDialogActive()) {
          Graphics2D g = screen.createGraphics();
          try {
            VnSceneRenderer.drawOverlay(g, sw, sh, presenter.activeScene());
          } finally {
            g.dispose();
          }
        }
      }
      case HACK -> presenter.shopScreen().render(screen, mouseX, mouseY);
      case CARD_REVEAL -> {
        presenter.shopScreen().render(screen, mouseX, mouseY);
        Graphics2D g = screen.createGraphics();
        try {
          BattleCardRevealView.draw(g, sw, sh, presenter.battleCard().revealProgress());
        } finally {
          g.dispose();
        }
      }
      case BOSS_MAP -> {
        Graphics2D g = screen.createGraphics();
        try {
          BossMapView.draw(g, sw, sh, presenter.hoveredBoss(), presenter.selectedBoss());
        } finally {
          g.dispose();
        }
      }
      case VN_BATTLE, VN_DIALOG, ENDING -> {
        Graphics2D g = screen.createGraphics();
        try {
          VnSceneRenderer.drawScene(g, sw, sh, presenter.activeScene());
        } finally {
          g.dispose();
        }
      }
    }

    if (presenter.director().phase() != Chapter1Phase.CUTSCENE
        && presenter.director().phase() != Chapter1Phase.LOOP_SEQUENCE
        && presenter.director().phase() != Chapter1Phase.LOOP_HOLD) {
      Graphics2D overlay = screen.createGraphics();
      try {
        GlitchOverlayRenderer.draw(overlay, sw, sh, presenter.director().session());
        if (presenter.director().phase() == Chapter1Phase.HACK) {
          presenter.doorLoopPlayer().render(overlay, sw, sh);
          HackTerminalView.draw(overlay, sw, sh, presenter.hack(), presenter.hackShakeTick());
        }
        if (presenter.director().phase() == Chapter1Phase.SHOP
            || presenter.director().phase() == Chapter1Phase.HACK) {
          Chapter1SessionHud.draw(overlay, sw, presenter.director().session());
          if (presenter.director().phase() == Chapter1Phase.SHOP
              && presenter.battleCard().canOpenMap(presenter.director().session())) {
            BattleCardRevealView.drawCardIcon(
                overlay,
                Chapter1ViewConstants.CARD_ICON_X,
                Chapter1ViewConstants.CARD_ICON_Y,
                Chapter1ViewConstants.CARD_ICON_SIZE,
                presenter.cardIconHovered());
          }
        }
      } finally {
        overlay.dispose();
      }
    }
  }

  @Override
  public void renderTextOverlay(Graphics2D g, int mouseX, int mouseY, Chapter1Presenter presenter) {
    if (presenter.director().phase() == Chapter1Phase.SHOP
        || presenter.director().phase() == Chapter1Phase.HACK) {
      presenter.shopScreen().renderTextOverlay(g, mouseX, mouseY);
    }
    if (presenter.hasActiveChoices()) {
      VnSceneRenderer.drawChoices(g, presenter.activeScene(), presenter.choiceRects());
    }
  }
}
