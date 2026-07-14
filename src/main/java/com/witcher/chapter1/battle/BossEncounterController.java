package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnSceneState;

/** VN-появление босса после пробуждения. */
public final class BossEncounterController {

  private static final int MS_PER_TICK = 16;
  private static final int LID_OPEN_MS = 1400;
  private static final int PORTRAIT_DELAY_MS = 500;

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
    return easeOutCubic((ms - PORTRAIT_DELAY_MS) / (float) (LID_OPEN_MS - PORTRAIT_DELAY_MS + 400));
  }

  public float portraitSlideX() {
    float alpha = portraitAlpha();
    return (1f - alpha) * 48f;
  }

  public boolean showDialog() {
    return portraitAlpha() > 0.55f;
  }

  public VnSceneState scene() {
    if (!showDialog()) {
      return null;
    }
    return new VnSceneState(
        boss.name(),
        "Ты наконец открыл глаза… но лес помнит каждый твой шаг.");
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
