package main.java.com.witcher.chapter1.battle;

/** Действие игрока в VN-бою (Command). */
public enum PlayerAction {
  ATTACK(1.0f, true, false),
  DODGE(0.6f, false, true),
  SIGN(1.1f, false, false),
  POTION(0.4f, false, true);

  public final float modifier;
  public final boolean usesDefense;
  public final boolean usesStamina;

  PlayerAction(float modifier, boolean usesDefense, boolean usesStamina) {
    this.modifier = modifier;
    this.usesDefense = usesDefense;
    this.usesStamina = usesStamina;
  }
}
