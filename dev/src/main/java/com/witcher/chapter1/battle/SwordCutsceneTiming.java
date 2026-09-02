package main.java.com.witcher.chapter1.battle;

/** Длительности катсцены мечей — общее для Swing и движка. */
public final class SwordCutsceneTiming {

  /**
   * Полная длина: чёрный старт + rush-лист + хвост.
   * Согласовано с {@link SwordSlashShowTimeline#rushVisualEndMs()}.
   */
  public static final int TOTAL_MS = (int) SwordSlashShowTimeline.rushVisualEndMs();

  /** Доля пути, после которой при победе можно заморозить кадр (legacy / опционально). */
  public static final float FREEZE_PROGRESS = 0.78f;

  private SwordCutsceneTiming() {
  }

  public static long freezeAfterMs() {
    return Math.round(TOTAL_MS * FREEZE_PROGRESS);
  }
}
