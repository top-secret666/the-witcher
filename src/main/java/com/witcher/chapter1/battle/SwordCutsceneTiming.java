package main.java.com.witcher.chapter1.battle;

/** Длительности катсцены мечей — общее для Swing и движка. */
public final class SwordCutsceneTiming {

  public static final int TOTAL_MS = 3500;
  /** Доля пути, после которой при победе замораживается кадр + глитч. */
  public static final float FREEZE_PROGRESS = 0.72f;

  private SwordCutsceneTiming() {
  }

  public static long freezeAfterMs() {
    return Math.round(TOTAL_MS * FREEZE_PROGRESS);
  }
}
