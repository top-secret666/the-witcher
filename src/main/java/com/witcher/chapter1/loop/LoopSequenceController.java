package main.java.com.witcher.chapter1.loop;

import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.chapter1.loop.WakeAwakeningTimeline;

/**
 * loop_wake: GIF + шум + веки пробуждения. illusion_wrong пока отключён.
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

  public Step step() {
    return step;
  }

  public boolean isActive() {
    return step != Step.IDLE && step != Step.HOLD;
  }

  public boolean isHolding() {
    return step == Step.HOLD;
  }

  public void start(boolean eyesPrelude) {
    step = Step.LOOP_WAKE;
    stepTicks = 0;
  }

  public void tick() {
    if (step == Step.IDLE || step == Step.HOLD) {
      return;
    }
    stepTicks++;
  }

  public CutsceneId currentCutscene() {
    return step == Step.LOOP_WAKE ? CutsceneId.LOOP_WAKE : null;
  }

  public boolean showEyes() {
    return step == Step.LOOP_WAKE;
  }

  public void enterHold() {
    step = Step.HOLD;
    stepTicks = 0;
  }

  public enum EyesPhase {
    IDLE, AWAKENING
  }

  public EyesPhase eyesPhase() {
    return step == Step.LOOP_WAKE ? EyesPhase.AWAKENING : EyesPhase.IDLE;
  }
}
