package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.Chapter1Phase;
import main.java.com.witcher.ui.chapter1.presenter.Chapter1Presenter;
import main.java.com.witcher.ui.chapter1.swing.battle.BattleResultView;
import main.java.com.witcher.ui.chapter1.swing.battle.map.BossMapView;
import main.java.com.witcher.ui.chapter1.swing.battle.briefing.BossQuestBriefingView;
import main.java.com.witcher.ui.chapter1.swing.battle.encounter.BossEncounterView;
import main.java.com.witcher.ui.chapter1.swing.battle.glitch.BossGlitchRevealView;
import main.java.com.witcher.ui.chapter1.swing.battle.wolf.WolfEndingView;
import main.java.com.witcher.ui.chapter1.swing.glitch.GlitchOverlayRenderer;
import main.java.com.witcher.ui.chapter1.view.Chapter1View;

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
      case BOSS_MAP -> {
        Graphics2D g = screen.createGraphics();
        try {
          BossMapView.draw(g, sw, sh, presenter.hoveredBoss(), presenter.selectedBoss(),
              presenter.bossMapBackHovered());
        } finally {
          g.dispose();
        }
      }
      case BOSS_QUEST_BRIEFING -> {
        Graphics2D g = screen.createGraphics();
        try {
          var shopLayout = presenter.shopScreen().presenter().createLayout();
          BossQuestBriefingView.draw(g, sw, sh, presenter.questBriefing(), shopLayout, mouseX, mouseY);
        } finally {
          g.dispose();
        }
      }
      case BOSS_ENCOUNTER -> {
        Graphics2D g = screen.createGraphics();
        try {
          BossEncounterView.draw(g, sw, sh, presenter.encounter(), mouseX, mouseY);
        } finally {
          g.dispose();
        }
      }
      case BOSS_GLITCH_REVEAL -> {
        Graphics2D g = screen.createGraphics();
        try {
          BossGlitchRevealView.draw(g, sw, sh, presenter.bossGlitchReveal());
        } finally {
          g.dispose();
        }
      }
      case BOSS_FINALE -> {
        Graphics2D g = screen.createGraphics();
        try {
          g.setColor(Color.BLACK);
          g.fillRect(0, 0, sw, sh);
          VnSceneRenderer.drawScene(g, sw, sh, presenter.activeScene());
        } finally {
          g.dispose();
        }
      }
      case WOLF_ENDING -> {
        Graphics2D g = screen.createGraphics();
        try {
          WolfEndingView.draw(g, sw, sh, presenter.wolfEndingType());
        } finally {
          g.dispose();
        }
      }
      case BATTLE_RESULT -> {
        Graphics2D g = screen.createGraphics();
        try {
          BattleResultView.draw(g, sw, sh, presenter.battleVictory());
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

    if (shouldSkipSessionOverlay(presenter.director().phase())) {
      return;
    }
    Graphics2D overlay = screen.createGraphics();
    try {
      GlitchOverlayRenderer.draw(overlay, sw, sh, presenter.director().session());
      if (presenter.director().phase() == Chapter1Phase.HACK) {
        presenter.doorLoopPlayer().render(overlay, sw, sh);
        HackTerminalView.draw(overlay, sw, sh, presenter.hack(), presenter.hackShakeTick());
      }
      if (presenter.director().phase() == Chapter1Phase.SHOP
          || presenter.director().phase() == Chapter1Phase.HACK) {
        boolean adminHovered = Chapter1SessionHud.hitAdminMapButton(mouseX, mouseY, sw);
        Chapter1SessionHud.draw(overlay, sw, presenter.director().session(), adminHovered);
      }
    } finally {
      overlay.dispose();
    }
  }

  private static boolean shouldSkipSessionOverlay(Chapter1Phase phase) {
    return phase == Chapter1Phase.CUTSCENE
        || phase == Chapter1Phase.LOOP_SEQUENCE
        || phase == Chapter1Phase.LOOP_HOLD
        || phase == Chapter1Phase.BOSS_MAP
        || phase == Chapter1Phase.BOSS_QUEST_BRIEFING
        || phase == Chapter1Phase.BOSS_ENCOUNTER
        || phase == Chapter1Phase.BOSS_GLITCH_REVEAL
        || phase == Chapter1Phase.BOSS_FINALE
        || phase == Chapter1Phase.WOLF_ENDING
        || phase == Chapter1Phase.BATTLE_RESULT;
  }

  @Override
  public void renderTextOverlay(Graphics2D g, int mouseX, int mouseY, Chapter1Presenter presenter) {
    Chapter1Phase phase = presenter.director().phase();
    int sw = g.getClipBounds() != null ? g.getClipBounds().width : 480;
    int sh = g.getClipBounds() != null ? g.getClipBounds().height : 360;
    // Не рисуем речь лавки поверх VN — отсюда «живой.осов?» при зависании покупки.
    if ((phase == Chapter1Phase.SHOP || phase == Chapter1Phase.HACK)
        && !presenter.isDukeDialogActive()) {
      presenter.shopScreen().renderTextOverlay(g, mouseX, mouseY);
    }
    if (presenter.hasActiveChoices()) {
      if (phase == Chapter1Phase.BOSS_QUEST_BRIEFING || phase == Chapter1Phase.BOSS_ENCOUNTER) {
        VnSceneRenderer.drawOverlay(g, sw, sh, presenter.activeScene());
      }
      VnSceneRenderer.drawChoices(g, presenter.activeScene(), presenter.choiceRects());
    }
    if (phase == Chapter1Phase.BOSS_MAP
        || phase == Chapter1Phase.BOSS_QUEST_BRIEFING
        || phase == Chapter1Phase.BOSS_ENCOUNTER
        || phase == Chapter1Phase.BOSS_FINALE
        || phase == Chapter1Phase.WOLF_ENDING
        || phase == Chapter1Phase.BATTLE_RESULT
        || phase == Chapter1Phase.VN_BATTLE
        || phase == Chapter1Phase.VN_DIALOG
        || phase == Chapter1Phase.ENDING
        || (phase == Chapter1Phase.SHOP && presenter.isDukeDialogActive())) {
      Chapter1UiCursor.draw(g, mouseX, mouseY);
    }
  }
}
