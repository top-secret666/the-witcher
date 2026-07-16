package main.java.com.witcher.chapter1.battle;

import java.util.List;

/** Угрожающие реплики на фоне «кровавого коридора». */
public final class BossGlitchRevealScript {

  public record Line(String text, int bgVariant) {
  }

  private static final List<Line> LINES = List.of(
      new Line("Думаешь, ты можешь сбежать, Геральт?", 0),
      new Line("Нет. Петля держит тех, кто уже забыл своё имя.", 1),
      new Line("Смотри.", 2)
  );

  private BossGlitchRevealScript() {
  }

  public static List<Line> lines() {
    return LINES;
  }

  public static int lineCount() {
    return LINES.size();
  }
}
