package main.java.com.witcher.chapter1.battle;

/**
 * Состояние typewriter-строки для босс-VN.
 * Общий тикер — {@link BossVnTypingEngine}.
 */
public final class BossVnTypingState {

  private int charIndex;
  private int typeTickCounter;
  private boolean waitingForAdvance;
  private int autoWaitTicks;

  public void reset() {
    charIndex = 0;
    typeTickCounter = 0;
    waitingForAdvance = false;
    autoWaitTicks = 0;
  }

  public int charIndex() {
    return charIndex;
  }

  public boolean waitingForAdvance() {
    return waitingForAdvance;
  }

  public int autoWaitTicks() {
    return autoWaitTicks;
  }

  public void clearAutoWait() {
    autoWaitTicks = 0;
  }

  public void clearWaitingForAdvance() {
    waitingForAdvance = false;
  }

  public String visibleText(String fullText) {
    if (fullText == null) {
      return "";
    }
    int end = Math.min(charIndex, fullText.length());
    return fullText.substring(0, end);
  }

  void setCharIndex(int value) {
    charIndex = value;
  }

  void setTypeTickCounter(int value) {
    typeTickCounter = value;
  }

  void setWaitingForAdvance(boolean value) {
    waitingForAdvance = value;
  }

  void incrementTypeTickCounter() {
    typeTickCounter++;
  }

  void incrementAutoWaitTicks() {
    autoWaitTicks++;
  }

  int typeTickCounter() {
    return typeTickCounter;
  }
}
