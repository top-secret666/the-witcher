package main.java.com.witcher.chapter1.cutscene;

/** Когда можно пропустить катсцену и какой текст подсказки показывать. */
public final class CutsceneSkipPolicy {

  public static final int SKIP_DELAY_MS = 1500;
  public static final String HINT_TEXT = "Пробел — пропустить";

  private CutsceneSkipPolicy() {
  }

  public static boolean canSkip(int elapsedMs) {
    return elapsedMs >= SKIP_DELAY_MS;
  }
}
