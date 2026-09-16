package com.witcher.chapter1.battle;

import com.witcher.chapter1.battle.briefing.BossQuestBriefingConstants;

/**
 * Пиксельный dissolve / затемнение экрана (раньше в конце брифинга).
 * Теперь — после клика по боссу на карте.
 */
public final class ScreenDissolveController {

  private static final int MS_PER_TICK = BossQuestBriefingConstants.MS_PER_TICK;
  private static final int DISSOLVE_RAMP_MS = BossQuestBriefingConstants.DISSOLVE_RAMP_MS;
  private static final int TOTAL_MS = BossQuestBriefingConstants.TRANSITION_TOTAL_MS;

  private int ticks;
  private boolean active;

  public void begin() {
    active = true;
    ticks = 0;
  }

  public void tick() {
    if (active) {
      ticks++;
    }
  }

  public boolean active() {
    return active;
  }

  public float dissolveT() {
    if (!active) {
      return 0f;
    }
    float t = ticks * MS_PER_TICK / (float) DISSOLVE_RAMP_MS;
    float c = Math.max(0f, Math.min(1f, t));
    return c * c;
  }

  public boolean isComplete() {
    return active && ticks * MS_PER_TICK >= TOTAL_MS;
  }

  public void clear() {
    active = false;
    ticks = 0;
  }
}
