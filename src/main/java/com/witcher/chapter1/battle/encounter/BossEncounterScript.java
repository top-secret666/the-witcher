package main.java.com.witcher.chapter1.battle.encounter;

import main.java.com.witcher.chapter1.assets.Chapter1AssetPaths;
import main.java.com.witcher.ui.intro.IntroTheme;

import java.util.List;

/**
 * Диалог пробуждения со злодеем: реплики + эмоция спрайта
 * (stand / reach / lunge / knife).
 */
public final class BossEncounterScript {

  public enum Expression {
    MAP,
    INTERESTED,
    LUNGE,
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
          IntroTheme.dukeRgb(), Expression.LUNGE),
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

  public static String spritePathFor(Expression expression) {
    return switch (expression) {
      case ATTACK -> Chapter1AssetPaths.VOLK_DUKE_MAP_ATTACK;
      case INTERESTED -> Chapter1AssetPaths.VOLK_DUKE_MAP_INTERESTED;
      case LUNGE -> Chapter1AssetPaths.VOLK_DUKE_MAP_LUNGE;
      case MAP -> Chapter1AssetPaths.VOLK_DUKE_MAP;
    };
  }
}
