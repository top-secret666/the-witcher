package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.loop.WakeAwakeningTimeline;

/** Таймлайн глитч-пробуждения Волка после катсцены мечей (победа). */
public final class BossGlitchRevealTimeline {

  public static final int MS_PER_TICK = 16;

  public static final int BUILDUP_MS = 900;
  public static final int DIALOG_MS = 3600;
  public static final int BLINK_MS = 650;
  public static final int SHEET_FRAME_MS = 95;
  public static final int SHEET_COLS = 3;
  public static final int SHEET_ROWS = 3;
  public static final int SHEET_FRAMES = SHEET_COLS * SHEET_ROWS;
  public static final int SHEET_MS = SHEET_FRAME_MS * SHEET_FRAMES;
  /** Долго: большой шум → чёткость без багов → затемнение. */
  public static final int SHARPEN_MS = 2200;
  public static final int FADE_DARK_MS = 450;
  public static final int EYELID_MS = WakeAwakeningTimeline.TOTAL_MS;

  public static final int TOTAL_MS =
      BUILDUP_MS + DIALOG_MS + BLINK_MS + SHEET_MS + SHARPEN_MS + FADE_DARK_MS + EYELID_MS;

  private BossGlitchRevealTimeline() {
  }

  public enum Stage {
    GLITCH_BUILDUP,
    CORRIDOR_DIALOG,
    BLINK,
    AWAKEN_SHEET,
    SHARPEN,
    FADE_DARK,
    EYELID_OPEN,
    DONE
  }

  public static Stage stageAt(int elapsedMs) {
    if (elapsedMs >= TOTAL_MS) {
      return Stage.DONE;
    }
    int t = 0;
    t += BUILDUP_MS;
    if (elapsedMs < t) {
      return Stage.GLITCH_BUILDUP;
    }
    t += DIALOG_MS;
    if (elapsedMs < t) {
      return Stage.CORRIDOR_DIALOG;
    }
    t += BLINK_MS;
    if (elapsedMs < t) {
      return Stage.BLINK;
    }
    t += SHEET_MS;
    if (elapsedMs < t) {
      return Stage.AWAKEN_SHEET;
    }
    t += SHARPEN_MS;
    if (elapsedMs < t) {
      return Stage.SHARPEN;
    }
    t += FADE_DARK_MS;
    if (elapsedMs < t) {
      return Stage.FADE_DARK;
    }
    return Stage.EYELID_OPEN;
  }

  public static int stageElapsed(int elapsedMs, Stage stage) {
    int start = stageStartMs(stage);
    return Math.max(0, elapsedMs - start);
  }

  public static int stageStartMs(Stage stage) {
    return switch (stage) {
      case GLITCH_BUILDUP -> 0;
      case CORRIDOR_DIALOG -> BUILDUP_MS;
      case BLINK -> BUILDUP_MS + DIALOG_MS;
      case AWAKEN_SHEET -> BUILDUP_MS + DIALOG_MS + BLINK_MS;
      case SHARPEN -> BUILDUP_MS + DIALOG_MS + BLINK_MS + SHEET_MS;
      case FADE_DARK -> BUILDUP_MS + DIALOG_MS + BLINK_MS + SHEET_MS + SHARPEN_MS;
      case EYELID_OPEN -> BUILDUP_MS + DIALOG_MS + BLINK_MS + SHEET_MS + SHARPEN_MS + FADE_DARK_MS;
      case DONE -> TOTAL_MS;
    };
  }

  public static float buildupIntensity(int localMs) {
    float t = clamp(localMs / (float) BUILDUP_MS, 0f, 1f);
    return easeInCubic(t);
  }

  public static int sheetFrameIndex(int localMs) {
    int idx = localMs / SHEET_FRAME_MS;
    return Math.max(0, Math.min(SHEET_FRAMES - 1, idx));
  }

  public static float sheetNoise(int frameIndex) {
    return clamp((frameIndex + 1f) / SHEET_FRAMES, 0f, 1f);
  }

  public static float sharpenT(int localMs) {
    float t = clamp(localMs / (float) SHARPEN_MS, 0f, 1f);
    return easeOutCubic(t);
  }

  public static float fadeDarkAlpha(int localMs) {
    float t = clamp(localMs / (float) FADE_DARK_MS, 0f, 1f);
    return easeInCubic(t);
  }

  public static float blinkVisible(int localMs) {
    return (localMs / 80) % 2 == 0 ? 1f : 0f;
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
