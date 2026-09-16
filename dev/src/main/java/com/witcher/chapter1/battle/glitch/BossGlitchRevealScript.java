package com.witcher.chapter1.battle.glitch;

import java.util.List;

/** Реплики глитч-пробуждения на истинной ветке Волка. */
public final class BossGlitchRevealScript {

  public record Line(String text) {
  }

  private static final List<Line> CORRIDOR_LINES = List.of(
      new Line("...."),
      new Line("Думаешь можешь сбежать Геральт?"),
      new Line("Ты никогда не вспомнишь кто ты на самом деле."),
      new Line("Ты застрянешь в этой петле навсегда...")
  );

  private BossGlitchRevealScript() {
  }

  public static List<Line> corridorLines() {
    return CORRIDOR_LINES;
  }
}
