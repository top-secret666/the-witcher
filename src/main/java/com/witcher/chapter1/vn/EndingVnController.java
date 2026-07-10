package main.java.com.witcher.chapter1.vn;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.chapter1.ending.EscapeEnding;
import main.java.com.witcher.chapter1.ending.EscapeResolver;

/**
 * Финальная VN после успешного взлома: диалог → выбор → исход.
 */
public final class EndingVnController {

  private enum Step {
    INTRO, CHOICE, RESOLVE, DONE
  }

  private final Chapter1Session session;
  private Step step = Step.INTRO;
  private VnSceneState scene = EndingVnScript.intro();
  private EscapeEnding resolvedEnding = EscapeEnding.LOCKED;

  public EndingVnController(Chapter1Session session) {
    this.session = session;
  }

  public VnSceneState scene() {
    return scene;
  }

  public boolean isDone() {
    return step == Step.DONE;
  }

  public EscapeEnding resolvedEnding() {
    return resolvedEnding;
  }

  public void advance() {
    if (step == Step.DONE || scene.waitingForChoice()) {
      return;
    }
    switch (step) {
      case INTRO -> {
        step = Step.CHOICE;
        scene = EndingVnScript.finalChoice();
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
    resolvedEnding = EscapeResolver.resolve(session);
    step = Step.RESOLVE;
    scene = EndingVnScript.resolveLine(resolvedEnding == EscapeEnding.TRUE_ESCAPE);
  }
}
