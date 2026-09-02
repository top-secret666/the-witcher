package main.java.com.witcher.chapter1.battle.briefing;

import main.java.com.witcher.chapter1.battle.BossVnTypingConstants;
/** Тайминги и скорости брифинга перед боссом — без логики. */
public final class BossQuestBriefingConstants {

  public static final int MS_PER_TICK = BossVnTypingConstants.MS_PER_TICK;
  public static final int TICKS_PER_CHAR = BossVnTypingConstants.TICKS_PER_CHAR;
  public static final int AUTO_DELAY_TICKS = BossVnTypingConstants.AUTO_DELAY_TICKS;
  public static final int AUTO_TICKS_PER_CHAR = BossVnTypingConstants.AUTO_TICKS_PER_CHAR;
  public static final int DISSOLVE_RAMP_MS = 1100;
  public static final int TRANSITION_TOTAL_MS = 1350;
  public static final float SLIDE_SPEED = 0.04f;
  public static final float ACTIVE_SPEED = 0.06f;
  public static final float DIALOG_REVEAL_PROGRESS = 0.52f;

  private BossQuestBriefingConstants() {
  }
}
