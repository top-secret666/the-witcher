package main.java.com.witcher.chapter1.loop;

import main.java.com.witcher.chapter1.cutscene.CutsceneSkipPolicy;

/** Состояние пробуждения век (без отрисовки) — общее для Swing и движка. */
public final class EyesAwakeningController {

  public enum Mode {
    IDLE, AWAKENING, DONE
  }

  private Mode mode = Mode.IDLE;
  private int ticks;

  public void reset(Mode startMode) {
    mode = startMode == Mode.AWAKENING ? Mode.AWAKENING : Mode.IDLE;
    ticks = 0;
  }

  public Mode mode() {
    return mode;
  }

  public boolean isDone() {
    return mode == Mode.DONE;
  }

  public int elapsedMs() {
    return ticks * WakeAwakeningTimeline.MS_PER_TICK;
  }

  public float eyelidOpenT() {
    return WakeAwakeningTimeline.eyelidOpenT(elapsedMs());
  }

  public float sharpness() {
    return WakeAwakeningTimeline.sharpness(elapsedMs());
  }

  public float noiseStrength() {
    return WakeAwakeningTimeline.noiseStrength(elapsedMs());
  }

  public void tick() {
    if (mode != Mode.AWAKENING) {
      return;
    }
    ticks++;
    if (WakeAwakeningTimeline.isComplete(elapsedMs())) {
      mode = Mode.DONE;
    }
  }

  public boolean canSkip() {
    return mode == Mode.AWAKENING && CutsceneSkipPolicy.canSkip(elapsedMs());
  }

  /** Пропуск заставки пробуждения (пробел). */
  public void skip() {
    if (!canSkip()) {
      return;
    }
    mode = Mode.DONE;
  }
}
