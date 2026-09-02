package main.java.com.witcher.ui.chapter1.presenter.wolf;

import main.java.com.witcher.chapter1.Chapter1Director;
import main.java.com.witcher.chapter1.Chapter1Phase;
import main.java.com.witcher.chapter1.battle.BossEntry;
import main.java.com.witcher.chapter1.battle.briefing.BossQuestBriefingController;
import main.java.com.witcher.chapter1.battle.encounter.BossEncounterController;
import main.java.com.witcher.chapter1.battle.glitch.BossGlitchRevealController;
import main.java.com.witcher.chapter1.battle.wolf.WolfBossFinaleController;
import main.java.com.witcher.chapter1.ending.WolfEndingType;
import main.java.com.witcher.chapter1.loop.LoopRules;
import main.java.com.witcher.chapter1.view.Chapter1Layout;
import main.java.com.witcher.chapter1.vn.VnSceneState;
import main.java.com.witcher.ui.chapter1.view.VnChoiceLayout;

import java.util.List;

/** Фазы первого босса (Волк): брифинг → лес → финал → глитч → итог. */
public final class WolfBossPhaseHandler {

  public interface Host {
    Chapter1Director director();

    BossEntry selectedBoss();

    List<VnChoiceLayout.ChoiceRect> choiceRects();

    void setChoiceRects(List<VnChoiceLayout.ChoiceRect> rects);

    void refreshChoiceRects();

    void notifyPhaseEntered();

    BossGlitchRevealController bossGlitchReveal();

    void setBattleVictory(boolean value);

    /** Старт VFX мечей на шаге CLASH. */
    void beginFinaleSwordClash();

    /** Тик VFX; {@code true} если закончился (или пропущен). */
    boolean tickFinaleSwordClash();

    /** Пропуск VFX по клику/Space. */
    void skipFinaleSwordClash();

    boolean isFinaleSwordClashPlaying();
  }

  private final Host host;

  private BossQuestBriefingController questBriefing;
  private BossEncounterController encounter;
  private WolfBossFinaleController wolfFinale;
  private WolfEndingType wolfEndingType = WolfEndingType.BAD_LOOP;

  public WolfBossPhaseHandler(Host host) {
    this.host = host;
  }

  public BossQuestBriefingController questBriefing() {
    return questBriefing;
  }

  public BossEncounterController encounter() {
    return encounter;
  }

  public WolfBossFinaleController wolfFinale() {
    return wolfFinale;
  }

  public WolfEndingType wolfEndingType() {
    return wolfEndingType;
  }

  public VnSceneState activeChoiceScene(Chapter1Phase phase) {
    if (phase == Chapter1Phase.BOSS_QUEST_BRIEFING
        && questBriefing != null && questBriefing.waitingForChoice()) {
      return questBriefing.choiceScene();
    }
    if (phase == Chapter1Phase.BOSS_ENCOUNTER
        && encounter != null && encounter.waitingForChoice()) {
      return encounter.choiceScene();
    }
    if (phase == Chapter1Phase.BOSS_FINALE && wolfFinale != null) {
      return wolfFinale.scene();
    }
    return null;
  }

  public void onPhaseEntered(Chapter1Phase phase) {
    switch (phase) {
      case BOSS_QUEST_BRIEFING ->
          questBriefing = new BossQuestBriefingController(host.selectedBoss());
      case BOSS_ENCOUNTER ->
          encounter = new BossEncounterController(host.selectedBoss(), host.director().session());
      case BOSS_GLITCH_REVEAL -> {
        host.bossGlitchReveal().reset();
        host.setBattleVictory(true);
      }
      case BOSS_FINALE -> {
        wolfFinale = new WolfBossFinaleController(host.director().session());
        host.refreshChoiceRects();
      }
      case WOLF_ENDING -> encounter = null;
      default -> { }
    }
  }

  public void updateBriefing(int mouseX, int mouseY, boolean clicked, int wheelNotches) {
    if (questBriefing == null) {
      questBriefing = new BossQuestBriefingController(host.selectedBoss());
    }
    questBriefing.setLayoutSize(Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
    questBriefing.tick();

    if (questBriefing.inTransition()) {
      if (questBriefing.isComplete()) {
        questBriefing = null;
        host.director().beginLoopSequence(false);
        host.notifyPhaseEntered();
      }
      return;
    }

    if (questBriefing.waitingForChoice()) {
      host.refreshChoiceRects();
      if (clicked) {
        int index = VnChoiceLayout.hitIndex(host.choiceRects(), mouseX, mouseY);
        if (index >= 0) {
          applyBriefingChoice(index);
        }
      }
      return;
    }

    questBriefing.updateDialog(mouseX, mouseY, clicked, wheelNotches, false);
    if (questBriefing.waitingForChoice()) {
      host.refreshChoiceRects();
    }
  }

  public void applyBriefingChoice(int index) {
    if (questBriefing == null) {
      return;
    }
    questBriefing.choose(index, host.director().session());
    host.setChoiceRects(List.of());
  }

  public void updateEncounter(int mouseX, int mouseY, boolean clicked, int wheelNotches) {
    if (encounter == null) {
      encounter = new BossEncounterController(host.selectedBoss(), host.director().session());
    }
    encounter.setLayoutSize(Chapter1Layout.VIRTUAL_W, Chapter1Layout.VIRTUAL_H);
    encounter.tick();

    if (encounter.waitingForChoice()) {
      host.refreshChoiceRects();
      if (clicked) {
        int index = VnChoiceLayout.hitIndex(host.choiceRects(), mouseX, mouseY);
        if (index >= 0) {
          applyEncounterChoice(index);
        }
      }
      return;
    }

    encounter.updateDialog(mouseX, mouseY, clicked, wheelNotches, false);

    if (encounter.waitingForChoice()) {
      host.refreshChoiceRects();
      return;
    }

    if (encounter.isDialogComplete()) {
      host.director().enterBossFinale();
      host.notifyPhaseEntered();
    }
  }

  public void applyEncounterChoice(int index) {
    if (encounter == null) {
      return;
    }
    encounter.choose(index, host.director().session());
    host.setChoiceRects(List.of());
  }

  public void updateGlitchReveal() {
    host.bossGlitchReveal().tick();
    if (host.bossGlitchReveal().isComplete()) {
      finishGlitchReveal();
    }
  }

  public void skipGlitchReveal() {
    host.bossGlitchReveal().skip();
    finishGlitchReveal();
  }

  private void finishGlitchReveal() {
    if (host.director().phase() != Chapter1Phase.BOSS_GLITCH_REVEAL) {
      return;
    }
    host.director().enterWolfEnding();
    host.notifyPhaseEntered();
  }

  public void updateFinale(int mouseX, int mouseY, boolean clicked) {
    if (wolfFinale == null) {
      wolfFinale = new WolfBossFinaleController(host.director().session());
      host.refreshChoiceRects();
      return;
    }
    if (wolfFinale.step() == WolfBossFinaleController.Step.CLASH) {
      if (clicked) {
        host.skipFinaleSwordClash();
      }
      if (host.tickFinaleSwordClash()) {
        advanceFinale();
      }
      return;
    }
    if (!clicked) {
      return;
    }
    if (wolfFinale.scene().waitingForChoice()) {
      int index = VnChoiceLayout.hitIndex(host.choiceRects(), mouseX, mouseY);
      if (index >= 0) {
        applyFinaleChoice(index);
      }
      return;
    }
    advanceFinale();
  }

  public void applyFinaleChoice(int index) {
    if (wolfFinale == null) {
      return;
    }
    wolfFinale.choose(index);
    host.beginFinaleSwordClash();
    host.refreshChoiceRects();
  }

  public void advanceFinale() {
    if (wolfFinale == null) {
      return;
    }
    if (wolfFinale.step() == WolfBossFinaleController.Step.CLASH
        && host.isFinaleSwordClashPlaying()) {
      return;
    }
    if (wolfFinale.isDone()) {
      finishFinale();
      return;
    }
    wolfFinale.advance();
    host.refreshChoiceRects();
    if (wolfFinale.isDone()) {
      finishFinale();
    }
  }

  private void finishFinale() {
    if (wolfFinale == null) {
      return;
    }
    wolfEndingType = wolfFinale.trueEnding()
        ? WolfEndingType.TRUE_SHARD
        : WolfEndingType.BAD_LOOP;
    LoopRules.onWolfOutcome(host.director().session(), wolfFinale.trueEnding());
    wolfFinale = null;
    host.setChoiceRects(List.of());
    if (wolfEndingType == WolfEndingType.TRUE_SHARD) {
      host.director().enterBossGlitchReveal();
    } else {
      host.director().enterWolfEnding();
    }
    host.notifyPhaseEntered();
  }

  public void updateEnding(boolean clicked) {
    if (clicked) {
      host.director().enterShop();
      host.notifyPhaseEntered();
    }
  }
}
