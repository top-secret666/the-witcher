package com.witcher.chapter1.battle.wolf;

import com.witcher.chapter1.Chapter1Session;
import com.witcher.chapter1.shop.BossMemoryFragments;
import com.witcher.chapter1.vn.VnSceneState;

/**
 * Финальная VN: сразу эпилог истинной ветки (без пустого экрана «доставай меч»).
 */
public final class WolfBossFinaleController {

  public enum Step {
    CHOICE, CLASH, RESOLVE, DONE
  }

  /** ~1 с при пустом эпилоге — сразу к глитчу/титрам. */
  private static final int TRUE_RESOLVE_AUTO_TICKS = 30;

  private final Chapter1Session session;
  private Step step = Step.RESOLVE;
  private VnSceneState scene;
  private final boolean trueEnding = true;
  private int stepTicks;

  public WolfBossFinaleController(Chapter1Session session) {
    this.session = session != null ? session : Chapter1Session.newGame();
    BossMemoryFragments.grantWolfShard(this.session);
    this.scene = WolfBossFinaleScript.trueEndingLine(BossMemoryFragments.wolfFragmentCode());
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

  public void tick() {
    if (step == Step.DONE || step == Step.CHOICE) {
      return;
    }
    stepTicks++;
  }

  /** Автопереход: RESOLVE → DONE. */
  public boolean shouldAutoAdvance() {
    if (step == Step.RESOLVE) {
      return stepTicks >= TRUE_RESOLVE_AUTO_TICKS;
    }
    return false;
  }

  /** @deprecated use {@link #shouldAutoAdvance()} */
  @Deprecated
  public boolean shouldAutoFinishResolve() {
    return step == Step.RESOLVE && shouldAutoAdvance();
  }

  public void advance() {
    if (step == Step.DONE || scene.waitingForChoice()) {
      return;
    }
    if (step == Step.CLASH) {
      step = Step.RESOLVE;
      stepTicks = 0;
      scene = WolfBossFinaleScript.trueEndingLine(BossMemoryFragments.wolfFragmentCode());
      return;
    }
    if (step == Step.RESOLVE) {
      step = Step.DONE;
    }
  }

  /** Выбор реплик отключён — одна концовка. */
  public void choose(int index) {
    // no-op
  }
}
