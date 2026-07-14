package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Graphics2D;

/** Анимация век и тайминг пробуждения loop_wake. */
public final class EyesBlinkEffect {

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

  public void render(Graphics2D g, int sw, int sh) {
    if (mode != Mode.AWAKENING) {
      return;
    }
    EyelidOverlay.renderBlack(g, sw, sh, eyelidOpenT());
  }
}
