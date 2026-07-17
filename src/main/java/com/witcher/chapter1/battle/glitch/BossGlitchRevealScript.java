package main.java.com.witcher.chapter1.battle.glitch;

import java.util.List;

/** Реплики: сначала только «...», потом угрозы на corridor. */
public final class BossGlitchRevealScript {

  public record Line(String text) {
  }

  private static final List<Line> CORRIDOR_LINES = List.of(
      new Line("Думаешь, ты можешь сбежать, Геральт?"),
      new Line("Нет. Петля держит тех, кто уже забыл своё имя."),
      new Line("Смотри.")
  );

  private BossGlitchRevealScript() {
  }

  public static List<Line> corridorLines() {
    return CORRIDOR_LINES;
  }
}
