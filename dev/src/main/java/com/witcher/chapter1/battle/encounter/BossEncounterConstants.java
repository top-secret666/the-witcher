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

  /**
   * После «Кто?»: чёрный экран → открытие глаз на дворе (скорость как у лесного пробуждения)
   * → слайд Весемира/Геральта.
   */
  public static final int FLASHBACK_BLACK_MS = 900;
  /** Та же скорость открытия, что CLOSED_HOLD/OPEN у леса после первого пробуждения. */
  public static final int FLASHBACK_OPEN_MS = OPEN_MS;
  /** Пауза на одном фоне перед слайдом персонажей. */
  public static final int FLASHBACK_BG_HOLD_MS = 450;
  /** Как IntroController / брифинг. */
  public static final float FLASHBACK_SLIDE_SPEED = 0.04f;
  public static final float FLASHBACK_ACTIVE_SPEED = 0.06f;
  /** После полного слайда — пауза перед печатью. */
  public static final int FLASHBACK_DIALOG_PAD_MS = 200;

  private BossEncounterConstants() {
  }
}
