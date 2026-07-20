package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.shop.BossMemoryFragments;
import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;

/**
 * Финальная VN после энкоунтера: прелюдия → выбор №2 → исход.
 */
public final class WolfBossFinaleController {

  public enum Step {
    PRELUDE, WOLF, CHOICE, RESOLVE, DONE
  }

  private final Chapter1Session session;
  private Step step = Step.PRELUDE;
  private VnSceneState scene = WolfBossFinaleScript.prelude();
  private boolean trueEnding;

  public WolfBossFinaleController(Chapter1Session session) {
    this.session = session != null ? session : Chapter1Session.newGame();
  }

  public VnSceneState scene() {
    return scene;
  }

  public Step step() {
    return step;
  }

  public boolean isDone() {
    return step == Step.DONE;
  }

  public boolean trueEnding() {
    return trueEnding;
  }

  public void advance() {
    if (step == Step.DONE || scene.waitingForChoice()) {
      return;
    }
    switch (step) {
      case PRELUDE -> {
        step = Step.WOLF;
        scene = WolfBossFinaleScript.wolfPrompt();
      }
      case WOLF -> {
        step = Step.CHOICE;
        scene = WolfBossFinaleScript.finalChoice();
      }
      case RESOLVE -> step = Step.DONE;
      default -> { }
    }
  }

  public void choose(int index) {
    if (step != Step.CHOICE || !scene.waitingForChoice()) {
      return;
    }
    scene.select(index);
    VnChoice choice = scene.selectedChoice();
    if (choice != null) {
      if (choice.suspicionDelta() > 0) {
        session.addSuspicion(choice.suspicionDelta());
      }
      if (choice.trustDelta() > 0) {
        session.addTrust(choice.trustDelta());
      }
    }
    trueEnding = session.suspicionDominates();
    step = Step.RESOLVE;
    if (trueEnding) {
      BossMemoryFragments.grantWolfShard(session);
      scene = WolfBossFinaleScript.trueEndingLine(BossMemoryFragments.wolfFragmentCode());
    } else {
      scene = WolfBossFinaleScript.badEndingLine();
    }
  }
}
