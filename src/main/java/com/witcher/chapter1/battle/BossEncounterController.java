package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnSceneState;

/** VN-появление босса после пробуждения: лес + злодей по центру. */
public final class BossEncounterController {

  private static final int MS_PER_TICK = 16;
  private static final int LID_OPEN_MS = 1500;
  private static final int PORTRAIT_DELAY_MS = 600;

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
    if (ms >= LID_OPEN_MS) {
      return 1f;
    }
    return easeOutCubic(ms / (float) LID_OPEN_MS);
  }

  public float portraitAlpha() {
    int ms = elapsedMs();
    if (ms < PORTRAIT_DELAY_MS) {
      return 0f;
    }
    return easeOutCubic((ms - PORTRAIT_DELAY_MS) / (float) (LID_OPEN_MS - PORTRAIT_DELAY_MS + 500));
  }

  /** Лёгкий zoom-in злодея из центра. */
  public float portraitScale() {
    float alpha = portraitAlpha();
    return 0.88f + 0.12f * alpha;
  }

  public boolean showDialog() {
    return portraitAlpha() > 0.6f;
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
    return dialogDismissed && eyelidOpenT() >= 0.98f;
  }

  private static float easeOutCubic(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return 1f - (float) Math.pow(1f - c, 3);
  }
}
