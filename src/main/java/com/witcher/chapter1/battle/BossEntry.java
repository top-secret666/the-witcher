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
    String portraitPath,
    int mapX,
    int mapY
) {
}
