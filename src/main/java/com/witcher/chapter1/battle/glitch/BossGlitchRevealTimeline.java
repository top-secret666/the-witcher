package main.java.com.witcher.chapter1.battle.glitch;

import main.java.com.witcher.ui.intro.IntroEasing;

/** Таймлайн глитч-пробуждения Волка — строгий порядок слоёв. */
public final class BossGlitchRevealTimeline {

  public static final int MS_PER_TICK = 16;

  /** Чёрный → плотные ТВ-помехи (без букв) до полной непрозрачности. */
  public static final int STATIC_FILL_MS = 1200;
  /** Спад первого занавеса: heavy уже под шумом, картинка проявляется. */
  public static final int HEAVY_REVEAL_MS = 1950;
  /** Только «...». */
  public static final int DOTS_MS = 900;
  /**
   * Второй занавес: «ВЫХОД» обычный → больше → ещё больше → мелкий разброс по экрану → шум.
   */
  public static final int EXIT_CURTAIN_MS = 2300;
  /** Быстрый спад второго занавеса → corridor + диалог. */
  public static final int EXIT_DROP_MS = 240;
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

  /**
   * Доли EXIT_CURTAIN:
   * 0 → normal, 1 → больше, 2 → ещё больше (одно слово), 3 → мелкий разброс по экрану, 4 → шум-занавес.
   */
  public static final float EXIT_SIZE_BIG_AT = 0.10f;
  public static final float EXIT_SIZE_HUGE_AT = 0.20f;
  /** Мелкий разброс «ВЫХОД» постепенно заполняет экран. */
  public static final float EXIT_SCATTER_AT = 0.30f;
  /** После заполнения — быстрый старт шумового занавеса. */
  public static final float EXIT_NOISE_AT = 0.68f;

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
    return 1f - IntroEasing.easeOutCubic(clamp(localMs / (float) Math.max(1, duration), 0f, 1f));
  }

  /**
   * Спад первого занавеса: чуть быстрее, но всё ещё мягкий.
   */
  public static float heavyStaticFall(int localMs) {
    float t = clamp(localMs / (float) Math.max(1, HEAVY_REVEAL_MS), 0f, 1f);
    if (t < 0.28f) {
      return 1f;
    }
    float u = (t - 0.28f) / 0.72f;
    return 1f - easeInCubic(u);
  }

  /** Быстрый спад второго занавеса. */
  public static float exitDropT(int localMs) {
    float t = clamp(localMs / (float) Math.max(1, EXIT_DROP_MS), 0f, 1f);
    return 1f - easeInCubic(t);
  }

  public static float exitBuildT(int localMs) {
    return clamp(localMs / (float) Math.max(1, EXIT_CURTAIN_MS), 0f, 1f);
  }

  /**
   * 0/1/2 = одно «ВЫХОД» в диалоге (разный размер), 3 = мелкий разброс по экрану, 4 = шум-занавес.
   */
  public static int exitBuildStep(int localMs) {
    float t = exitBuildT(localMs);
    if (t < EXIT_SIZE_BIG_AT) {
      return 0;
    }
    if (t < EXIT_SIZE_HUGE_AT) {
      return 1;
    }
    if (t < EXIT_SCATTER_AT) {
      return 2;
    }
    if (t < EXIT_NOISE_AT) {
      return 3;
    }
    return 4;
  }

  /** 0..1 густота мелких «ВЫХОД»: постепенно от почти пусто до полного заполнения. */
  public static float exitScatterFillT(int localMs) {
    float t = exitBuildT(localMs);
    if (t < EXIT_SCATTER_AT) {
      return 0f;
    }
    if (t >= EXIT_NOISE_AT) {
      return 1f;
    }
    float u = clamp(
        (t - EXIT_SCATTER_AT) / Math.max(0.001f, EXIT_NOISE_AT - EXIT_SCATTER_AT),
        0f, 1f);
    // Линейно/мягко — видно, как экран постепенно забивается словами.
    return IntroEasing.easeOutCubic(u);
  }

  /** 0..1 плотность шумового занавеса: старт резкий (быстро закрывает экран). */
  public static float virusSpreadT(int localMs) {
    float t = exitBuildT(localMs);
    if (t < EXIT_NOISE_AT) {
      return 0f;
    }
    float u = clamp(
        (t - EXIT_NOISE_AT) / Math.max(0.001f, 1f - EXIT_NOISE_AT),
        0f, 1f);
    // Быстрый набор в начале занавеса.
    return IntroEasing.easeOutCubic(Math.min(1f, u * 1.55f));
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
    return IntroEasing.easeOutCubic(clamp(localMs / (float) SHARD_EMERGE_MS, 0f, 1f));
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

  private static float clamp(float v, float min, float max) {
    return Math.max(min, Math.min(max, v));
  }
}
