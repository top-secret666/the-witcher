package main.java.com.witcher.chapter1.loop;

import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.chapter1.loop.WakeAwakeningTimeline;

/**
 * loop_wake: GIF + шум + веки пробуждения — режим {@link LoopSequenceKind#EYELID_WAKE}.
 * Процедурная ходьба сквозь чащу — {@link LoopSequenceKind#FOREST_WALK}.
 */
public final class LoopSequenceController {

  public static final int LOOP_WAKE_TICKS = WakeAwakeningTimeline.totalTicks();

  public enum Step {
    IDLE,
    LOOP_WAKE,
    HOLD
  }

  private Step step = Step.IDLE;
  private int stepTicks;
  private LoopSequenceKind kind = LoopSequenceKind.EYELID_WAKE;
  private int forestElapsedMs;
  private boolean forestSkipped;

  public Step step() {
    return step;
  }

  public LoopSequenceKind kind() {
    return kind;
  }

  public boolean isActive() {
    return step != Step.IDLE && step != Step.HOLD;
  }

  public boolean isHolding() {
    return step == Step.HOLD;
  }

  public void start(boolean eyesPrelude, LoopSequenceKind sequenceKind) {
    kind = sequenceKind != null ? sequenceKind : LoopSequenceKind.EYELID_WAKE;
    step = Step.LOOP_WAKE;
    stepTicks = 0;
    forestElapsedMs = 0;
    forestSkipped = false;
  }

  /** @deprecated используйте {@link #start(boolean, LoopSequenceKind)} */
  @Deprecated
  public void start(boolean eyesPrelude) {
    start(eyesPrelude, LoopSequenceKind.EYELID_WAKE);
  }

  public void tick() {
    if (step == Step.IDLE || step == Step.HOLD) {
      return;
    }
    stepTicks++;
    if (kind == LoopSequenceKind.FOREST_WALK && !forestSkipped) {
      forestElapsedMs += ForestWalkTimeline.MS_PER_TICK;
    }
  }

  public int forestWalkElapsedMs() {
    return forestSkipped ? ForestWalkTimeline.TOTAL_MS : forestElapsedMs;
  }

  public boolean forestWalkComplete() {
    return kind == LoopSequenceKind.FOREST_WALK
        && forestWalkElapsedMs() >= ForestWalkTimeline.TOTAL_MS;
  }

  public void skipForestWalk() {
    if (kind == LoopSequenceKind.FOREST_WALK) {
      forestSkipped = true;
      forestElapsedMs = ForestWalkTimeline.TOTAL_MS;
    }
  }

  public CutsceneId currentCutscene() {
    return kind == LoopSequenceKind.EYELID_WAKE && step == Step.LOOP_WAKE
        ? CutsceneId.LOOP_WAKE
        : null;
  }

  public boolean showEyes() {
    return kind == LoopSequenceKind.EYELID_WAKE && step == Step.LOOP_WAKE;
  }

  public void enterHold() {
    step = Step.HOLD;
    stepTicks = 0;
  }

  public enum EyesPhase {
    IDLE, AWAKENING
  }

  public EyesPhase eyesPhase() {
    return showEyes() ? EyesPhase.AWAKENING : EyesPhase.IDLE;
  }
}
