package main.java.com.witcher.chapter1.battle.glitch;

import main.java.com.witcher.chapter1.cutscene.CutsceneSkipPolicy;

import java.util.List;

/** Логика глитч-пробуждения после диалога энкоунтера. */
public final class BossGlitchRevealController {

  /** Пауза после полной строки — чтобы успеть прочитать. */
  private static final int LINE_PAUSE_TICKS = 55;

  private int ticks;
  private int dialogLine;
  private int charIndex;
  private int typeTickCounter;
  private int linePauseTicks;
  private boolean skipped;
  private boolean dialogFinished;

  public void reset() {
    ticks = 0;
    dialogLine = 0;
    charIndex = 0;
    typeTickCounter = 0;
    linePauseTicks = 0;
    skipped = false;
    dialogFinished = false;
  }

  public void tick() {
    if (isComplete()) {
      return;
    }

    BossGlitchRevealTimeline.Stage planned = BossGlitchRevealTimeline.stageAt(elapsedMs());
    boolean corridorActive = planned == BossGlitchRevealTimeline.Stage.CORRIDOR_DIALOG
        || (planned.ordinal() > BossGlitchRevealTimeline.Stage.CORRIDOR_DIALOG.ordinal()
            && !dialogFinished);

    if (corridorActive) {
      tickDialog();
    }

    // Улыбка / sheet — только после полной печати и паузы на чтение.
    if (planned.ordinal() > BossGlitchRevealTimeline.Stage.CORRIDOR_DIALOG.ordinal()
        && !dialogFinished) {
      return;
    }

    ticks++;
  }

  public boolean canSkip() {
    return !skipped && CutsceneSkipPolicy.canSkip(elapsedMs());
  }

  public void skip() {
    if (!canSkip()) {
      return;
    }
    skipped = true;
  }

  public boolean isComplete() {
    return skipped || elapsedMs() >= BossGlitchRevealTimeline.TOTAL_MS;
  }

  public int elapsedMs() {
    return ticks * BossGlitchRevealTimeline.MS_PER_TICK;
  }

  public BossGlitchRevealTimeline.Stage stage() {
    BossGlitchRevealTimeline.Stage s = BossGlitchRevealTimeline.stageAt(elapsedMs());
    if (s.ordinal() > BossGlitchRevealTimeline.Stage.CORRIDOR_DIALOG.ordinal()
        && !dialogFinished) {
      return BossGlitchRevealTimeline.Stage.CORRIDOR_DIALOG;
    }
    return s;
  }

  public int stageElapsedMs() {
    return BossGlitchRevealTimeline.stageElapsed(elapsedMs(), stage());
  }

  public String visibleDialogText() {
    List<BossGlitchRevealScript.Line> lines = BossGlitchRevealScript.corridorLines();
    if (dialogLine < 0 || dialogLine >= lines.size()) {
      return "";
    }
    String full = lines.get(dialogLine).text();
    int end = Math.min(charIndex, full.length());
    return full.substring(0, end);
  }

  public boolean dialogFinished() {
    return dialogFinished;
  }

  /**
   * Финальная катсцена: текст появляется сразу целиком (без печати).
   * Настройки скорости не применяются.
   */
  private void tickDialog() {
    if (dialogFinished) {
      return;
    }
    List<BossGlitchRevealScript.Line> lines = BossGlitchRevealScript.corridorLines();
    if (lines.isEmpty()) {
      dialogFinished = true;
      return;
    }
    if (linePauseTicks > 0) {
      linePauseTicks--;
      if (linePauseTicks == 0) {
        if (dialogLine + 1 < lines.size()) {
          dialogLine++;
          charIndex = 0;
          typeTickCounter = 0;
        } else {
          dialogFinished = true;
        }
      }
      return;
    }
    String full = lines.get(dialogLine).text();
    charIndex = full.length();
    linePauseTicks = LINE_PAUSE_TICKS;
  }
}
