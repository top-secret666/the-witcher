package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnSceneState;

/** После loop_wake: веки закрыты → полное открытие → злодей по центру и диалог. */
public final class BossEncounterController {

  private static final int MS_PER_TICK = 16;
  private static final int CLOSED_HOLD_MS = 700;
  private static final int OPEN_MS = 1300;

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

  public boolean sceneRevealed() {
    return eyelidOpenT() >= 0.99f;
  }

  public float portraitAlpha() {
    return sceneRevealed() ? 1f : 0f;
  }

  public float portraitScale() {
    return 1f;
  }

  public boolean showDialog() {
    return sceneRevealed();
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
    return dialogDismissed && sceneRevealed();
  }

  private static float easeOutCubic(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return 1f - (float) Math.pow(1f - c, 3);
  }
}
