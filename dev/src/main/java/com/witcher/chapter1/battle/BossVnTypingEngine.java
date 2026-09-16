package com.witcher.chapter1.battle;

import com.witcher.ui.settings.GameSettings;

/** Общий тик typewriter-логики для босс-VN (брифинг и лес). */
public final class BossVnTypingEngine {

  public enum TickResult {
    CONTINUE,
    ADVANCE_LINE
  }

  private BossVnTypingEngine() {
  }

  /** Тик с таймингами из {@link GameSettings}. */
  public static TickResult tickWithSettings(
      BossVnTypingState state,
      String fullText,
      int totalChars,
      boolean advance,
      boolean autoMode) {
    GameSettings s = GameSettings.get();
    if (!s.typewriterEnabled()
        && !state.waitingForAdvance()
        && state.charIndex() < totalChars) {
      state.setCharIndex(totalChars);
      state.setWaitingForAdvance(true);
      state.clearAutoWait();
      return TickResult.CONTINUE;
    }
    return tick(
        state, fullText, totalChars, advance, autoMode,
        s.ticksPerChar(), s.autoTicksPerChar(), s.autoDelayTicks());
  }

  public static TickResult tick(
      BossVnTypingState state,
      int totalChars,
      boolean advance,
      boolean autoMode,
      int ticksPerChar,
      int autoTicksPerChar,
      int autoDelayTicks) {
    return tick(state, null, totalChars, advance, autoMode, ticksPerChar, autoTicksPerChar, autoDelayTicks);
  }

  public static TickResult tick(
      BossVnTypingState state,
      String fullText,
      int totalChars,
      boolean advance,
      boolean autoMode,
      int ticksPerChar,
      int autoTicksPerChar,
      int autoDelayTicks) {
    if (state.waitingForAdvance()) {
      if (advance) {
        return TickResult.ADVANCE_LINE;
      }
      if (autoMode) {
        state.incrementAutoWaitTicks();
        if (state.autoWaitTicks() >= autoDelayTicks) {
          return TickResult.ADVANCE_LINE;
        }
      }
      return TickResult.CONTINUE;
    }

    if (autoMode) {
      state.incrementTypeTickCounter();
      if (state.typeTickCounter() >= autoTicksPerChar) {
        state.setTypeTickCounter(0);
        advanceChars(state, fullText, totalChars);
      }
      return TickResult.CONTINUE;
    }

    if (advance && state.charIndex() < totalChars) {
      state.setCharIndex(totalChars);
      state.setWaitingForAdvance(true);
      return TickResult.CONTINUE;
    }

    state.incrementTypeTickCounter();
    if (state.typeTickCounter() >= ticksPerChar) {
      state.setTypeTickCounter(0);
      advanceChars(state, fullText, totalChars);
    }
    return TickResult.CONTINUE;
  }

  /** Один шаг + сразу все подряд идущие {@code \n}, чтобы пустые строки не тормозили печать. */
  private static void advanceChars(BossVnTypingState state, String fullText, int totalChars) {
    int next = state.charIndex() + 1;
    if (fullText != null && next > 0 && next <= fullText.length()) {
      int just = next - 1;
      if (just < fullText.length() && fullText.charAt(just) == '\n') {
        while (next < totalChars && next < fullText.length() && fullText.charAt(next) == '\n') {
          next++;
        }
      }
    }
    state.setCharIndex(next);
    if (next >= totalChars) {
      state.setCharIndex(totalChars);
      state.setWaitingForAdvance(true);
      state.clearAutoWait();
    }
  }
}
