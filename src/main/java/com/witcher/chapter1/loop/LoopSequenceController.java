package main.java.com.witcher.chapter1.loop;

import main.java.com.witcher.chapter1.cutscene.CutsceneId;

/**
 * Шаги: loop_wake (GIF + шум + веки) → короткое моргание поверх GIF → illusion_wrong → стоп.
 */
public final class LoopSequenceController {

  public static final int LOOP_WAKE_TICKS = 130;
  public static final float NOISE_STRENGTH = 0.55f;

  public enum Step {
    IDLE,
    LOOP_WAKE,
    EYES_BLINK,
    ILLUSION_WRONG,
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

  /** eyesPrelude больше не отдельный чёрный экран — всегда сразу loop_wake с веками. */
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
    return switch (step) {
      case LOOP_WAKE, EYES_BLINK -> CutsceneId.LOOP_WAKE;
      case ILLUSION_WRONG -> CutsceneId.ILLUSION_WRONG;
      default -> null;
    };
  }

  /** Веки поверх GIF на старте открытия и при финальном моргании. */
  public boolean showEyes() {
    return step == Step.LOOP_WAKE || step == Step.EYES_BLINK;
  }

  public EyesPhase eyesPhase() {
    return switch (step) {
      case LOOP_WAKE -> EyesPhase.OPENING;
      case EYES_BLINK -> EyesPhase.BLINKING;
      default -> EyesPhase.IDLE;
    };
  }

  public void advanceFromEyesBlink() {
    if (step == Step.EYES_BLINK) {
      step = Step.ILLUSION_WRONG;
      stepTicks = 0;
    }
  }

  public boolean shouldStartBlink() {
    return step == Step.LOOP_WAKE && stepTicks >= LOOP_WAKE_TICKS;
  }

  public boolean shouldFinishIllusion() {
    return step == Step.ILLUSION_WRONG && stepTicks >= 1;
  }

  public void enterBlink() {
    step = Step.EYES_BLINK;
    stepTicks = 0;
  }

  public void enterHold() {
    step = Step.HOLD;
    stepTicks = 0;
  }

  public enum EyesPhase {
    IDLE, OPENING, BLINKING
  }
}
