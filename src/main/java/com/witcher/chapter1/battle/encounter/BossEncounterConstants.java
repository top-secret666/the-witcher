package main.java.com.witcher.chapter1.battle.encounter;

import main.java.com.witcher.chapter1.battle.BossVnTypingConstants;
/** Тайминги лесного VN-энкаунтера — без логики. */
public final class BossEncounterConstants {

  public static final int MS_PER_TICK = BossVnTypingConstants.MS_PER_TICK;
  public static final int CLOSED_HOLD_MS = 900;
  public static final int OPEN_MS = 1600;
  public static final int TICKS_PER_CHAR = BossVnTypingConstants.TICKS_PER_CHAR;
  public static final int AUTO_DELAY_TICKS = BossVnTypingConstants.AUTO_DELAY_TICKS;
  public static final int AUTO_TICKS_PER_CHAR = BossVnTypingConstants.AUTO_TICKS_PER_CHAR;

  private BossEncounterConstants() {
  }
}
