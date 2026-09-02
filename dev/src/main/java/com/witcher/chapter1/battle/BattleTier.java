package main.java.com.witcher.chapter1.battle;

/** Уровень сложности боя — растёт с номером витка. */
public enum BattleTier {
  LOW(1.0f, true),
  MID(1.35f, false),
  HIGH(1.75f, true);

  public final float dukeAttackMultiplier;
  /** Можно ли «убить» герцога на этом тире (Mid — только оглушить). */
  public final boolean canBeDefeated;

  BattleTier(float dukeAttackMultiplier, boolean canBeDefeated) {
    this.dukeAttackMultiplier = dukeAttackMultiplier;
    this.canBeDefeated = canBeDefeated;
  }

  public static BattleTier forLoop(int loop) {
    if (loop <= 1) {
      return LOW;
    }
    if (loop <= 3) {
      return MID;
    }
    return HIGH;
  }
}
