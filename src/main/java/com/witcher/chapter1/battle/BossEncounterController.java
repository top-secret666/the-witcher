package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnSceneState;

/**
 * После loop_wake: веки закрыты → открываются полностью.
 * Лес и злодей уже под веками; диалог — когда глаза открыты до конца.
 */
public final class BossEncounterController {

  private static final int MS_PER_TICK = 16;
  private static final int CLOSED_HOLD_MS = 450;
  private static final int OPEN_MS = 1400;

  private final BossEntry boss;
  private int ticks;
  private boolean dialogDismissed;

  public BossEncounterController(BossEntry boss) {
    this.boss = boss != null ? boss : BossCatalog.byId("duke");
  }

  public BossEntry boss() {
    return boss;
  }

  public int elapsedMs() {
    return ticks * MS_PER_TICK;
  }

  public void tick() {
    ticks++;
  }

  /** 0 = закрыто, 1 = полностью открыто. */
  public float eyelidOpenT() {
    int ms = elapsedMs();
    if (ms < CLOSED_HOLD_MS) {
      return 0f;
    }
    if (ms >= CLOSED_HOLD_MS + OPEN_MS) {
      return 1f;
    }
    float t = (ms - CLOSED_HOLD_MS) / (float) OPEN_MS;
    return easeOutCubic(t);
  }

  /** Веки открыты полностью — можно показать диалог. */
  public boolean eyesFullyOpen() {
    return eyelidOpenT() >= 1f;
  }

  public float portraitScale() {
    return 1f;
  }

  public boolean showDialog() {
    return eyesFullyOpen();
  }

  public VnSceneState scene() {
    if (!showDialog()) {
      return null;
    }
    return new VnSceneState(boss.name(), "…Наконец-то ты проснулся.");
  }

  public void dismissDialog() {
    dialogDismissed = true;
  }

  public boolean isReadyForSword() {
    return dialogDismissed && eyesFullyOpen();
  }

  private static float easeOutCubic(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return 1f - (float) Math.pow(1f - c, 3);
  }
}
