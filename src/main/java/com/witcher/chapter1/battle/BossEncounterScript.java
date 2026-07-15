package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.ui.intro.IntroTheme;

import java.util.List;

/**
 * Диалог пробуждения со злодеем: реплики + эмоция спрайта (map / interested / attack).
 */
public final class BossEncounterScript {

  public enum Expression {
    MAP,
    INTERESTED,
    ATTACK
  }

  public record DialogEntry(
      String speaker,
      String text,
      int speakerColorRgb,
      Expression expression
  ) {
  }

  private static final List<DialogEntry> ENTRIES = List.of(
      new DialogEntry("Герцог",
          "…Наконец-то ты проснулся.",
          IntroTheme.dukeRgb(), Expression.MAP),
      new DialogEntry("Герцог",
          "Лес помнит тебя лучше, чем ты — себя.\nНе делай вид, что это случайность.",
          IntroTheme.dukeRgb(), Expression.INTERESTED),
      new DialogEntry(null,
          "*Ветер шевелит кроны. Где-то в глубине — смех без рта.*",
          IntroTheme.narratorRgb(), Expression.MAP),
      new DialogEntry("Герцог",
          "Я ждал. А ты всё ещё думаешь, что уйдёшь\nиз этой петли на двух ногах.",
          IntroTheme.dukeRgb(), Expression.INTERESTED),
      new DialogEntry("Герцог",
          "Покажи клыки, Белый Волк.\nИли я покажу свои.",
          IntroTheme.dukeRgb(), Expression.ATTACK)
  );

  private BossEncounterScript() {
  }

  public static List<DialogEntry> entries() {
    return ENTRIES;
  }

  public static int entryCount() {
    return ENTRIES.size();
  }
}
