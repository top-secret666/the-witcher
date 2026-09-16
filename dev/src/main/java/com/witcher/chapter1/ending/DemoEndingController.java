package com.witcher.chapter1.ending;

/** Конец демо: Directed by → лесенка титров (авто) → «Спасибо за игру». */
public final class DemoEndingController {

  public enum Step {
    DIRECTED,
    CREDITS,
    THANKS,
    DONE
  }

  /** ~1.5 сек при 30 fps. */
  private static final int DIRECTED_TICKS = 45;
  /** Ещё быстрее лесенка. */
  private static final int CREDITS_TICKS = 130;
  private static final float CREDITS_SCROLL_PX_PER_TICK = 4.6f;

  private Step step = Step.DIRECTED;
  private int ticks;
  private float creditsScrollY;

  public Step step() {
    return step;
  }

  public int ticks() {
    return ticks;
  }

  public float creditsScrollY() {
    return creditsScrollY;
  }

  public boolean musicActive() {
    return step == Step.DIRECTED || step == Step.CREDITS;
  }

  public boolean acceptsExitKey() {
    return step == Step.THANKS;
  }

  public boolean isDone() {
    return step == Step.DONE;
  }

  public void tick() {
    if (step == Step.DONE) {
      return;
    }
    ticks++;
    if (step == Step.CREDITS) {
      creditsScrollY += CREDITS_SCROLL_PX_PER_TICK;
    }
    switch (step) {
      case DIRECTED -> {
        if (ticks >= DIRECTED_TICKS) {
          enterCredits();
        }
      }
      case CREDITS -> {
        if (ticks >= CREDITS_TICKS) {
          enterThanks();
        }
      }
      default -> { }
    }
  }

  /** Щелчок не ускоряет Directed by — только лесенку / спасибо. */
  public void skipToNext() {
    switch (step) {
      case CREDITS -> enterThanks();
      default -> { }
    }
  }

  public void finishToMenu() {
    step = Step.DONE;
  }

  private void enterCredits() {
    step = Step.CREDITS;
    ticks = 0;
    creditsScrollY = 0f;
  }

  private void enterThanks() {
    step = Step.THANKS;
    ticks = 0;
  }
}
