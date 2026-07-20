package main.java.com.witcher.chapter1.battle.encounter;

/** Состояние кросс-фейда фона над диалогом лесного VN. */
final class EncounterSceneBackground {

  static final int CROSSFADE_MS = BossEncounterConstants.SCENE_CROSSFADE_MS;

  private String displayedPath;
  private String fadeFromPath;
  private String fadeToPath;
  private int fadeElapsedMs;
  private int lineStartTick;
  private boolean kenBurnsDrift;

  void onEntryShown(int tick, BossEncounterScript.DialogEntry entry) {
    lineStartTick = tick;
    kenBurnsDrift = entry != null && entry.kenBurnsDrift();
    String next = entry != null ? entry.backgroundImage() : null;
    if (next == null) {
      return;
    }
    if (next.equals(displayedPath) && fadeToPath == null) {
      return;
    }
    if (displayedPath == null && fadeElapsedMs <= 0) {
      displayedPath = next;
      return;
    }
    fadeFromPath = displayedPath != null ? displayedPath : next;
    fadeToPath = next;
    fadeElapsedMs = 0;
  }

  void tick(int tick, int msPerTick) {
    if (fadeToPath == null) {
      return;
    }
    fadeElapsedMs += msPerTick;
    if (fadeElapsedMs >= CROSSFADE_MS) {
      displayedPath = fadeToPath;
      fadeFromPath = null;
      fadeToPath = null;
      fadeElapsedMs = 0;
    }
  }

  boolean hasSceneImage() {
    return displayedPath != null || fadeToPath != null;
  }

  String imageFrom() {
    if (fadeToPath != null) {
      return fadeFromPath != null ? fadeFromPath : fadeToPath;
    }
    return displayedPath;
  }

  String imageTo() {
    return fadeToPath != null ? fadeToPath : displayedPath;
  }

  float crossfadeT() {
    if (fadeToPath == null) {
      return 1f;
    }
    return Math.min(1f, fadeElapsedMs / (float) CROSSFADE_MS);
  }

  boolean kenBurnsDrift() {
    return kenBurnsDrift;
  }

  float kenBurnsPhase(int tick, int msPerTick) {
    int lineMs = Math.max(0, (tick - lineStartTick) * msPerTick);
    return lineMs / 45000f;
  }

  void reset() {
    displayedPath = null;
    fadeFromPath = null;
    fadeToPath = null;
    fadeElapsedMs = 0;
    lineStartTick = 0;
    kenBurnsDrift = false;
  }
}
