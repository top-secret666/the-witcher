package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.vn.VnSceneState;

/** VN-появление босса: чёрный экран → пауза → полное открытие → злодей и диалог. */
public final class BossEncounterController {

  private static final int MS_PER_TICK = 16;
  private static final int CLOSED_HOLD_MS = 2000;
  private static final int OPEN_MS = 1100;
  private static final int PORTRAIT_FADE_MS = 700;

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

  public float portraitAlpha() {
    int ms = elapsedMs();
    int revealStart = CLOSED_HOLD_MS + OPEN_MS;
    if (ms < revealStart) {
      return 0f;
    }
    if (ms >= revealStart + PORTRAIT_FADE_MS) {
      return 1f;
    }
    return easeOutCubic((ms - revealStart) / (float) PORTRAIT_FADE_MS);
  }

  public float portraitScale() {
    float alpha = portraitAlpha();
    return 0.92f + 0.08f * alpha;
  }

  public boolean showDialog() {
    return portraitAlpha() > 0.85f;
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
    return dialogDismissed && portraitAlpha() >= 0.98f;
  }

  private static float easeOutCubic(float t) {
    float c = Math.max(0f, Math.min(1f, t));
    return 1f - (float) Math.pow(1f - c, 3);
  }
}
