package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.loop.EyesAwakeningController;

import java.awt.Graphics2D;

/** Swing-обёртка над {@link EyesAwakeningController}: state в домене, веки в EyelidOverlay. */
public final class EyesBlinkEffect {

  public enum Mode {
    IDLE, AWAKENING, DONE;

    EyesAwakeningController.Mode toDomain() {
      return switch (this) {
        case IDLE -> EyesAwakeningController.Mode.IDLE;
        case AWAKENING -> EyesAwakeningController.Mode.AWAKENING;
        case DONE -> EyesAwakeningController.Mode.DONE;
      };
    }

    static Mode fromDomain(EyesAwakeningController.Mode mode) {
      return switch (mode) {
        case IDLE -> IDLE;
        case AWAKENING -> AWAKENING;
        case DONE -> DONE;
      };
    }
  }

  private final EyesAwakeningController controller = new EyesAwakeningController();

  public void reset(Mode startMode) {
    controller.reset(startMode == null ? EyesAwakeningController.Mode.IDLE : startMode.toDomain());
  }

  public Mode mode() {
    return Mode.fromDomain(controller.mode());
  }

  public boolean isDone() {
    return controller.isDone();
  }

  public int elapsedMs() {
    return controller.elapsedMs();
  }

  public float eyelidOpenT() {
    return controller.eyelidOpenT();
  }

  public float sharpness() {
    return controller.sharpness();
  }

  public float noiseStrength() {
    return controller.noiseStrength();
  }

  public void tick() {
    controller.tick();
  }

  /** Пропуск анимации пробуждения. */
  public boolean canSkip() {
    return controller.canSkip();
  }

  public void skip() {
    controller.skip();
  }

  public void render(Graphics2D g, int sw, int sh) {
    if (controller.mode() != EyesAwakeningController.Mode.AWAKENING) {
      return;
    }
    EyelidOverlay.renderBlack(g, sw, sh, eyelidOpenT());
  }
}
