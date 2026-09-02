package main.java.com.witcher.chapter1.battle;

/** Общий тик typewriter-логики для босс-VN (брифинг и лес). */
public final class BossVnTypingEngine {

  public enum TickResult {
    CONTINUE,
    ADVANCE_LINE
  }

  private BossVnTypingEngine() {
  }

  public static TickResult tick(
      BossVnTypingState state,
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
        int next = state.charIndex() + 1;
        state.setCharIndex(next);
        if (next >= totalChars) {
          state.setCharIndex(totalChars);
          state.setWaitingForAdvance(true);
          state.clearAutoWait();
        }
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
      int next = state.charIndex() + 1;
      state.setCharIndex(next);
      if (next >= totalChars) {
        state.setCharIndex(totalChars);
        state.setWaitingForAdvance(true);
      }
    }
    return TickResult.CONTINUE;
  }
}
