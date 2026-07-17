package main.java.com.witcher.chapter1.battle.glitch;

import java.util.List;

/** Логика глитч-пробуждения после диалога энкоунтера. */
public final class BossGlitchRevealController {

  private static final int TICKS_PER_CHAR = 2;
  private static final int LINE_PAUSE_TICKS = 40;

  private int ticks;
  private int dialogLine;
  private int charIndex;
  private int typeTickCounter;
  private int linePauseTicks;
  private boolean skipped;

  public void reset() {
    ticks = 0;
    dialogLine = 0;
    charIndex = 0;
    typeTickCounter = 0;
    linePauseTicks = 0;
    skipped = false;
  }

  public void tick() {
    if (isComplete()) {
      return;
    }
    ticks++;
    if (stage() == BossGlitchRevealTimeline.Stage.CORRIDOR_DIALOG) {
      tickDialog();
    }
  }

  public void skip() {
    skipped = true;
  }

  public boolean isComplete() {
    return skipped || elapsedMs() >= BossGlitchRevealTimeline.TOTAL_MS;
  }

  public int elapsedMs() {
    return ticks * BossGlitchRevealTimeline.MS_PER_TICK;
  }

  public BossGlitchRevealTimeline.Stage stage() {
    return BossGlitchRevealTimeline.stageAt(elapsedMs());
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

  private void tickDialog() {
    List<BossGlitchRevealScript.Line> lines = BossGlitchRevealScript.corridorLines();
    if (dialogLine >= lines.size()) {
      return;
    }
    if (linePauseTicks > 0) {
      linePauseTicks--;
      if (linePauseTicks == 0 && dialogLine + 1 < lines.size()) {
        dialogLine++;
        charIndex = 0;
        typeTickCounter = 0;
      }
      return;
    }
    String full = lines.get(dialogLine).text();
    if (charIndex >= full.length()) {
      linePauseTicks = LINE_PAUSE_TICKS;
      return;
    }
    typeTickCounter++;
    if (typeTickCounter >= TICKS_PER_CHAR) {
      typeTickCounter = 0;
      charIndex++;
    }
  }
}
