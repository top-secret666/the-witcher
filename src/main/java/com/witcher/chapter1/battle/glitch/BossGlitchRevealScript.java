package main.java.com.witcher.chapter1.battle.glitch;

import java.util.List;

/** Реплики глитч-пробуждения на истинной ветке Волка. */
public final class BossGlitchRevealScript {

  public record Line(String text) {
  }

  private static final List<Line> CORRIDOR_LINES = List.of(
      new Line("Думаешь, ты можешь сбежать, Геральт из Ривии?"),
      new Line("Петля держит тех, кто забыл, зачем медальон когда-то зазвенел."),
      new Line("Смотри. Вспомни, кого ты оставил в снегу у стен Каэр Морхена."),
      new Line("Это не лавка. Это клетка с зеркалами.")
  );

  private BossGlitchRevealScript() {
  }

  public static List<Line> corridorLines() {
    return CORRIDOR_LINES;
  }
}
