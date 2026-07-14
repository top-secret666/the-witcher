package main.java.com.witcher.chapter1.battle;

/**
 * Расписание спрайтовых проблесков мечей на чёрном фоне (общее для Swing/движка).
 * Лист A — длинный мах (24 кадра), лист B — острые вспышки/скрещивания (8 кадров).
 */
public final class SwordSlashShowTimeline {

  public enum SheetId {
    A, B
  }

  public record ActiveFrame(SheetId sheet, int index, boolean flip) {
  }

  private record Clip(SheetId sheet, long startMs, int frameMs, int frames, boolean flip) {
    long endMs() {
      return startMs + (long) frameMs * frames;
    }
  }

  /** Три акцента под тайминг ударов ~600 / 1400 / 2200 мс. */
  private static final Clip[] CLIPS = {
      new Clip(SheetId.B, 520, 42, 8, false),
      new Clip(SheetId.A, 1050, 45, 24, false),
      new Clip(SheetId.B, 2280, 42, 8, true)
  };

  private SwordSlashShowTimeline() {
  }

  public static ActiveFrame frameAt(long elapsedMs) {
    if (elapsedMs < 0) {
      return null;
    }
    for (Clip clip : CLIPS) {
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
