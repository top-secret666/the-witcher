package main.java.com.witcher.chapter1.battle;

/**
 * Расписание спрайтовых проблесков мечей на чёрном фоне (общее для Swing/движка).
 * Предпочтительно — лист RUSH 5×12 (60 кадров), быстро.
 * Fallback — листы A/B (старые акценты).
 */
public final class SwordSlashShowTimeline {

  public enum SheetId {
    RUSH, A, B
  }

  public record ActiveFrame(SheetId sheet, int index, boolean flip) {
  }

  private record Clip(SheetId sheet, long startMs, int frameMs, int frames, boolean flip) {
    long endMs() {
      return startMs + (long) frameMs * frames;
    }
  }

  /** Быстрая раскадровка дуэли: 60 кадров. */
  public static final int RUSH_COLS = 5;
  public static final int RUSH_ROWS = 12;
  public static final int RUSH_FRAMES = RUSH_COLS * RUSH_ROWS;
  public static final int RUSH_FRAME_MS = 10;
  public static final long RUSH_START_MS = 40;

  private static final Clip RUSH_CLIP =
      new Clip(SheetId.RUSH, RUSH_START_MS, RUSH_FRAME_MS, RUSH_FRAMES, false);

  /** Старый fallback, если rush-лист не загрузился. */
  private static final Clip[] FALLBACK_CLIPS = {
      new Clip(SheetId.B, 520, 42, 8, false),
      new Clip(SheetId.A, 1050, 45, 24, false),
      new Clip(SheetId.B, 2280, 42, 8, true)
  };

  private static final Clip[] RUSH_CLIPS = {RUSH_CLIP};

  private static volatile boolean preferRush = true;

  private SwordSlashShowTimeline() {
  }

  /** Включает rush-расписание, если лист доступен. */
  public static void setPreferRush(boolean prefer) {
    preferRush = prefer;
  }

  public static boolean prefersRush() {
    return preferRush;
  }

  public static long rushEndMs() {
    return RUSH_CLIP.endMs();
  }

  public static ActiveFrame frameAt(long elapsedMs) {
    if (elapsedMs < 0) {
      return null;
    }
    Clip[] clips = preferRush ? RUSH_CLIPS : FALLBACK_CLIPS;
    for (Clip clip : clips) {
      if (elapsedMs < clip.startMs || elapsedMs >= clip.endMs()) {
        continue;
      }
      int idx = (int) ((elapsedMs - clip.startMs) / clip.frameMs);
      idx = Math.max(0, Math.min(clip.frames - 1, idx));
      return new ActiveFrame(clip.sheet, idx, clip.flip);
    }
    // После rush держим последний кадр до конца катсцены.
    if (preferRush && elapsedMs >= RUSH_CLIP.endMs()) {
      return new ActiveFrame(SheetId.RUSH, RUSH_FRAMES - 1, false);
    }
    return null;
  }
}
