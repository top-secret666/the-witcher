package main.java.com.witcher.chapter1.battle.wolf;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.shop.BossMemoryFragments;
import main.java.com.witcher.chapter1.vn.VnChoice;
import main.java.com.witcher.chapter1.vn.VnSceneState;

/** Финальная VN: выбор №3 → «Тогда покажи» → исход. */
public final class WolfBossFinaleController {

  public enum Step {
    CHOICE, CLASH, RESOLVE, DONE
  }

  private final Chapter1Session session;
  private Step step = Step.CHOICE;
  private VnSceneState scene = WolfBossFinaleScript.finalChoice();
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
    if (step == Step.CLASH) {
      step = Step.RESOLVE;
      scene = trueEnding
          ? WolfBossFinaleScript.trueEndingLine(BossMemoryFragments.wolfFragmentCode())
          : WolfBossFinaleScript.badEndingLine();
      return;
    }
    if (step == Step.RESOLVE) {
      step = Step.DONE;
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
    step = Step.CLASH;
    scene = WolfBossFinaleScript.wolfClashLine();
    if (trueEnding) {
      BossMemoryFragments.grantWolfShard(session);
    }
  }
}
