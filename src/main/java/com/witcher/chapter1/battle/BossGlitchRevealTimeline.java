package main.java.com.witcher.chapter1.battle;

/** Таймлайн глитч-пробуждения Волка — строгий порядок слоёв. */
public final class BossGlitchRevealTimeline {

  public static final int MS_PER_TICK = 16;

  /** Чёрный → ТВ-помехи (без букв) до заполнения. */
  public static final int STATIC_FILL_MS = 1100;
  /** Спад помех: heavy уже под ними. */
  public static final int HEAVY_REVEAL_MS = 700;
  /** Три красные точки на heavy, без тряски. */
  public static final int DOTS_MS = 1100;
  /** Занавес: баги+буквы поверх heavy, «...» → растущий «ВЫХОД». */
  public static final int EXIT_CURTAIN_MS = 1700;
  /** Быстрый спад занавеса → corridor. */
  public static final int EXIT_DROP_MS = 280;
  /** corridor + тряска + диалог. */
  public static final int CORRIDOR_DIALOG_MS = 3800;
  public static final int SHEET_FRAME_MS = 85;
  public static final int SHEET_COLS = 3;
  public static final int SHEET_ROWS = 3;
  public static final int SHEET_FRAMES = SHEET_COLS * SHEET_ROWS;
  public static final int SHEET_MS = SHEET_FRAME_MS * SHEET_FRAMES;
  public static final int CYCLE_MS = SHEET_MS + 700;
  public static final int BG_CYCLE_MS = 180;
  public static final int BLACK_BANG_MS = 450;
  public static final int SHARD_EMERGE_MS = 2000;
  public static final int SHARD_OUT_MS = 420;

  /** Доля EXIT_CURTAIN, пока ещё «...», потом «ВЫХОД». */
  public static final float EXIT_WORD_START = 0.22f;

  public static final int TOTAL_MS =
      STATIC_FILL_MS + HEAVY_REVEAL_MS + DOTS_MS + EXIT_CURTAIN_MS + EXIT_DROP_MS
          + CORRIDOR_DIALOG_MS + CYCLE_MS + BLACK_BANG_MS + SHARD_EMERGE_MS + SHARD_OUT_MS;

  private BossGlitchRevealTimeline() {
  }

  public enum Stage {
    STATIC_FILL,
    HEAVY_REVEAL,
    DOTS_DIALOG,
    EXIT_CURTAIN,
    EXIT_DROP,
    CORRIDOR_DIALOG,
    CYCLE_SHEET,
    BLACK_BANG,
    SHARD_EMERGE,
    SHARD_OUT,
    DONE
  }

  public static Stage stageAt(int elapsedMs) {
    if (elapsedMs >= TOTAL_MS) {
      return Stage.DONE;
    }
    int t = 0;
    if (elapsedMs < (t += STATIC_FILL_MS)) {
      return Stage.STATIC_FILL;
    }
    if (elapsedMs < (t += HEAVY_REVEAL_MS)) {
      return Stage.HEAVY_REVEAL;
    }
    if (elapsedMs < (t += DOTS_MS)) {
      return Stage.DOTS_DIALOG;
    }
    if (elapsedMs < (t += EXIT_CURTAIN_MS)) {
      return Stage.EXIT_CURTAIN;
    }
    if (elapsedMs < (t += EXIT_DROP_MS)) {
      return Stage.EXIT_DROP;
    }
    if (elapsedMs < (t += CORRIDOR_DIALOG_MS)) {
      return Stage.CORRIDOR_DIALOG;
    }
    if (elapsedMs < (t += CYCLE_MS)) {
      return Stage.CYCLE_SHEET;
    }
    if (elapsedMs < (t += BLACK_BANG_MS)) {
      return Stage.BLACK_BANG;
    }
    if (elapsedMs < (t += SHARD_EMERGE_MS)) {
      return Stage.SHARD_EMERGE;
    }
    return Stage.SHARD_OUT;
  }

  public static int stageElapsed(int elapsedMs, Stage stage) {
    return Math.max(0, elapsedMs - stageStartMs(stage));
  }

  public static int stageStartMs(Stage stage) {
    return switch (stage) {
      case STATIC_FILL -> 0;
      case HEAVY_REVEAL -> STATIC_FILL_MS;
      case DOTS_DIALOG -> STATIC_FILL_MS + HEAVY_REVEAL_MS;
      case EXIT_CURTAIN -> STATIC_FILL_MS + HEAVY_REVEAL_MS + DOTS_MS;
      case EXIT_DROP -> STATIC_FILL_MS + HEAVY_REVEAL_MS + DOTS_MS + EXIT_CURTAIN_MS;
      case CORRIDOR_DIALOG -> STATIC_FILL_MS + HEAVY_REVEAL_MS + DOTS_MS + EXIT_CURTAIN_MS
          + EXIT_DROP_MS;
      case CYCLE_SHEET -> STATIC_FILL_MS + HEAVY_REVEAL_MS + DOTS_MS + EXIT_CURTAIN_MS
          + EXIT_DROP_MS + CORRIDOR_DIALOG_MS;
      case BLACK_BANG -> STATIC_FILL_MS + HEAVY_REVEAL_MS + DOTS_MS + EXIT_CURTAIN_MS
          + EXIT_DROP_MS + CORRIDOR_DIALOG_MS + CYCLE_MS;
      case SHARD_EMERGE -> STATIC_FILL_MS + HEAVY_REVEAL_MS + DOTS_MS + EXIT_CURTAIN_MS
          + EXIT_DROP_MS + CORRIDOR_DIALOG_MS + CYCLE_MS + BLACK_BANG_MS;
      case SHARD_OUT -> STATIC_FILL_MS + HEAVY_REVEAL_MS + DOTS_MS + EXIT_CURTAIN_MS
          + EXIT_DROP_MS + CORRIDOR_DIALOG_MS + CYCLE_MS + BLACK_BANG_MS + SHARD_EMERGE_MS;
      case DONE -> TOTAL_MS;
    };
  }

  public static float rise01(int localMs, int duration) {
    return easeInCubic(clamp(localMs / (float) Math.max(1, duration), 0f, 1f));
  }

  public static float fall01(int localMs, int duration) {
    return 1f - easeOutCubic(clamp(localMs / (float) Math.max(1, duration), 0f, 1f));
  }

  /** Быстрый почти линейный спад занавеса. */
  public static float exitDropT(int localMs) {
    float t = clamp(localMs / (float) Math.max(1, EXIT_DROP_MS), 0f, 1f);
    return 1f - easeInCubic(t);
  }

  /** 0 = ещё «...», &gt;0 = рост «ВЫХОД» (0..1). */
  public static float exitWordGrowT(int localMs) {
    int start = Math.round(EXIT_CURTAIN_MS * EXIT_WORD_START);
    if (localMs < start) {
      return 0f;
    }
    return easeInCubic(clamp((localMs - start) / (float) Math.max(1, EXIT_CURTAIN_MS - start), 0f, 1f));
  }

  public static int sheetFrameIndex(int localMs) {
    int idx = localMs / SHEET_FRAME_MS;
    return Math.max(0, Math.min(SHEET_FRAMES - 1, idx));
  }

  public static int bgCycleIndex(int localMs) {
    return (localMs / BG_CYCLE_MS) % 3;
  }

  public static float cycleGlitchPeak(int localMs) {
    float t = clamp(localMs / (float) CYCLE_MS, 0f, 1f);
    if (t < 0.72f) {
      return 0.12f + t * 0.2f;
    }
    return easeInCubic((t - 0.72f) / 0.28f);
  }

  public static float sharpenT(int localMs) {
    return easeOutCubic(clamp(localMs / (float) SHARD_EMERGE_MS, 0f, 1f));
  }

  public static float shardOutAlpha(int localMs) {
    float t = clamp(localMs / (float) SHARD_OUT_MS, 0f, 1f);
    if (t < 0.35f) {
      return 1f;
    }
    return 1f - easeInCubic((t - 0.35f) / 0.65f);
  }

  private static float easeInCubic(float t) {
    return t * t * t;
  }

  private static float easeOutCubic(float t) {
    return 1f - (float) Math.pow(1f - t, 3);
  }

  private static float clamp(float v, float min, float max) {
    return Math.max(min, Math.min(max, v));
  }
}
