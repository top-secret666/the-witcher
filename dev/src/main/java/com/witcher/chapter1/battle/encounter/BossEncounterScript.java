package com.witcher.chapter1.battle.encounter;

import com.witcher.chapter1.assets.Chapter1AssetPaths;
import com.witcher.ui.intro.IntroTheme;

import java.util.List;

/**
 * Диалог пробуждения со злодеем: реплики + эмоция спрайта.
 */
public final class BossEncounterScript {

  public enum SceneKind {
    WOLF,
    VESEMIR
  }

  public enum Expression {
    MAP,
    MAP_INTERESTED,
    REACH,
    ARMS,
    KNIFE,
    SHUSH,
    LISTEN,
    STAND
  }

  public record DialogEntry(
      String speaker,
      String text,
      int speakerColorRgb,
      Expression expression,
      SceneKind scene
  ) {
    public DialogEntry(String speaker, String text, int speakerColorRgb, Expression expression) {
      this(speaker, text, speakerColorRgb, expression, SceneKind.WOLF);
    }
  }

  private static final List<DialogEntry> ENTRIES = List.of(
      new DialogEntry("Герцог",
          "…Наконец-то ты проснулся.",
          IntroTheme.dukeRgb(), Expression.MAP),
      new DialogEntry("Герцог",
          "Лес помнит тебя лучше, чем ты — себя.\nНе делай вид, что это случайность.",
          IntroTheme.dukeRgb(), Expression.MAP_INTERESTED),
      new DialogEntry(null,
          "*Ветер шевелит кроны. Где-то в глубине — смех без рта.*",
          IntroTheme.narratorRgb(), Expression.MAP),
      new DialogEntry("Герцог",
          "Я ждал. А ты всё ещё думаешь, что уйдёшь\nиз этой петли на двух ногах.",
          IntroTheme.dukeRgb(), Expression.REACH),
      new DialogEntry("Герцог",
          "Покажи клыки, Белый Волк.\nИли я покажу свои.",
          IntroTheme.dukeRgb(), Expression.KNIFE)
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
      case MAP -> Chapter1AssetPaths.VOLK_DUKE_MAP;
      case MAP_INTERESTED -> Chapter1AssetPaths.VOLK_DUKE_MAP_INTERESTED;
      case REACH -> Chapter1AssetPaths.VOLK_DUKE_DIALOG_REACH;
      case ARMS -> Chapter1AssetPaths.VOLK_DUKE_DIALOG_ARMS;
      case KNIFE -> Chapter1AssetPaths.VOLK_DUKE_DIALOG_KNIFE;
      case SHUSH -> Chapter1AssetPaths.VOLK_DUKE_DIALOG_SHUSH;
      case LISTEN -> Chapter1AssetPaths.VOLK_DUKE_DIALOG_LISTEN;
      case STAND -> Chapter1AssetPaths.VOLK_DUKE_DIALOG_STAND;
    };
  }
}
