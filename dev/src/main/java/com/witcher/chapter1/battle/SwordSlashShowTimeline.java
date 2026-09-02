package main.java.com.witcher.chapter1.battle;

/**
 * Расписание спрайтовых проблесков мечей на чёрном фоне (общее для Swing/движка).
 * Предпочтительно — лист RUSH 5×12 (60 кадров).
 * Fallback — листы A/B, если rush не загрузился.
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

  public static final int RUSH_COLS = 5;
  public static final int RUSH_ROWS = 12;
  public static final int RUSH_FRAMES = RUSH_COLS * RUSH_ROWS;
  /** Чуть медленнее 10мс — читаемее на пиксель-арте. */
  public static final int RUSH_FRAME_MS = 14;
  /** Короткая пауза на чёрном перед первым кадром. */
  public static final long RUSH_START_MS = 80;
  /** Пауза на чёрном после последнего кадра (хвост катсцены). */
  public static final long RUSH_TAIL_BLACK_MS = 160;

  private static final Clip RUSH_CLIP =
      new Clip(SheetId.RUSH, RUSH_START_MS, RUSH_FRAME_MS, RUSH_FRAMES, false);

  /** Fallback A/B: вспышки совпадают с {@link SwordClashTimeline} (220 / 480 / 760). */
  private static final Clip[] FALLBACK_CLIPS = {
      new Clip(SheetId.B, 200, 40, 8, false),
      new Clip(SheetId.A, 440, 42, 20, false),
      new Clip(SheetId.B, 740, 40, 8, true)
  };

  private static final Clip[] RUSH_CLIPS = {RUSH_CLIP};

  private static volatile boolean preferRush = true;

  private SwordSlashShowTimeline() {
  }

  public static void setPreferRush(boolean prefer) {
    preferRush = prefer;
  }

  public static boolean prefersRush() {
    return preferRush;
  }

  public static long rushSheetEndMs() {
    return RUSH_CLIP.endMs();
  }

  /** Конец визуала листа + хвост чёрного. */
  public static long rushVisualEndMs() {
    return RUSH_CLIP.endMs() + RUSH_TAIL_BLACK_MS;
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
    return null;
  }
}
