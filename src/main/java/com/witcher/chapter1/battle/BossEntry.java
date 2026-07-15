package main.java.com.witcher.chapter1.battle;

/** Один босс на карте герцога. */
public record BossEntry(
    String id,
    String name,
    String title,
    int protection,
    int stamina,
    int signs,
    String mapIconPath,
    String mapHoverIconPath,
    String portraitPath,
    int mapX,
    int mapY
) {

  /** Иконка на карте: hover/selected → hover-арт, иначе обычная. */
  public String activeMapIconPath(boolean hot) {
    if (hot && mapHoverIconPath != null && !mapHoverIconPath.isBlank()) {
      return mapHoverIconPath;
    }
    return mapIconPath;
  }
}
