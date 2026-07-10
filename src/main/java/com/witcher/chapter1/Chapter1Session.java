package main.java.com.witcher.chapter1;

import main.java.com.witcher.ui.shop.ShopEquipSlot;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

/**
 * Счётчики и прогресс главы 1 «Петля герцога».
 * Без AWT — только доменная логика.
 */
public final class Chapter1Session {

  public static final int SUSPICION_TERMINAL_THRESHOLD = 3;
  public static final int FRAGMENTS_FOR_TERMINAL = 4;
  public static final int PRISON_COUNTER_THRESHOLD = 6;

  private int loop = 1;
  private int prison;
  private int suspicion;
  private int trust;
  private final boolean[] cipherFragments = new boolean[ShopEquipSlot.values().length];
  private final Set<ShopEquipSlot> inspectedSlots = EnumSet.noneOf(ShopEquipSlot.class);
  private boolean terminalAccessGranted;
  private boolean cipherSolved;
  private boolean escapeAttemptUnlocked;
  private int hackAttemptsThisLoop;

  public static Chapter1Session newGame() {
    return new Chapter1Session();
  }

  public int loop() {
    return loop;
  }

  public int prison() {
    return prison;
  }

  public int suspicion() {
    return suspicion;
  }

  public int trust() {
    return trust;
  }

  public int hackAttemptsThisLoop() {
    return hackAttemptsThisLoop;
  }

  public boolean terminalAccessGranted() {
    return terminalAccessGranted;
  }

  public boolean cipherSolved() {
    return cipherSolved;
  }

  public boolean escapeAttemptUnlocked() {
    return escapeAttemptUnlocked;
  }

  public void addPrison(int amount) {
    if (amount > 0) {
      prison += amount;
    }
  }

  public void addSuspicion(int amount) {
    if (amount > 0) {
      suspicion += amount;
      refreshTerminalAccess();
    }
  }

  public void addTrust(int amount) {
    if (amount > 0) {
      trust += amount;
    }
  }

  public void markInspected(ShopEquipSlot slot) {
    if (slot != null) {
      inspectedSlots.add(slot);
    }
  }

  public boolean wasInspected(ShopEquipSlot slot) {
    return slot != null && inspectedSlots.contains(slot);
  }

  /** Шанс найти фрагмент при осмотре — вызывается снаружи после проверки слота. */
  public boolean tryCollectFragment(ShopEquipSlot slot) {
    if (slot == null || cipherFragments[slot.ordinal()]) {
      return false;
    }
    cipherFragments[slot.ordinal()] = true;
    refreshTerminalAccess();
    return true;
  }

  public boolean hasFragment(ShopEquipSlot slot) {
    return slot != null && cipherFragments[slot.ordinal()];
  }

  public int fragmentCount() {
    int count = 0;
    for (boolean found : cipherFragments) {
      if (found) {
        count++;
      }
    }
    return count;
  }

  public boolean prisonBlocksVictory() {
    return prison >= PRISON_COUNTER_THRESHOLD;
  }

  public boolean suspicionDominates() {
    return suspicion > trust;
  }

  public void markCipherSolved() {
    cipherSolved = true;
    escapeAttemptUnlocked = true;
  }

  public void registerHackAttempt() {
    hackAttemptsThisLoop++;
  }

  /**
   * Сброс витка после поражения в бою.
   * Броня и кошелёк — в {@code ShopModel}; здесь только мета-прогресс главы.
   */
  public void advanceLoopAfterDefeat() {
    loop++;
    hackAttemptsThisLoop = 0;
    // Часть подозрения сохраняется — игрок «помнит», что что-то не так.
    suspicion = Math.max(suspicion, (int) Math.ceil(suspicion * 0.75));
  }

  /**
   * Ложный побег: иллюзия продолжается, но прогресс не обнуляется полностью.
   */
  public void applyFalseEscape() {
    loop++;
    hackAttemptsThisLoop = 0;
    cipherSolved = false;
    escapeAttemptUnlocked = false;
    trust = Math.max(trust, suspicion);
    suspicion = Math.max(1, suspicion / 2);
    refreshTerminalAccess();
  }

  public GlitchLevel glitchLevel() {
    if (suspicion >= 6) {
      return GlitchLevel.HEAVY;
    }
    if (suspicion >= 3) {
      return GlitchLevel.MEDIUM;
    }
    if (suspicion >= 1) {
      return GlitchLevel.LIGHT;
    }
    return GlitchLevel.NONE;
  }

  private void refreshTerminalAccess() {
    terminalAccessGranted = suspicion >= SUSPICION_TERMINAL_THRESHOLD
        && fragmentCount() >= FRAGMENTS_FOR_TERMINAL;
  }

  /** Снимок для {@link Chapter1Save}. */
  Chapter1Snapshot snapshot() {
    return new Chapter1Snapshot(
        loop, prison, suspicion, trust,
        Arrays.copyOf(cipherFragments, cipherFragments.length),
        inspectedSlots.toArray(new ShopEquipSlot[0]),
        terminalAccessGranted, cipherSolved, escapeAttemptUnlocked, hackAttemptsThisLoop);
  }

  void restore(Chapter1Snapshot snap) {
    if (snap == null) {
      return;
    }
    loop = snap.loop();
    prison = snap.prison();
    suspicion = snap.suspicion();
    trust = snap.trust();
    for (int i = 0; i < cipherFragments.length && i < snap.cipherFragments().length; i++) {
      cipherFragments[i] = snap.cipherFragments()[i];
    }
    inspectedSlots.clear();
    for (ShopEquipSlot slot : snap.inspectedSlots()) {
      if (slot != null) {
        inspectedSlots.add(slot);
      }
    }
    terminalAccessGranted = snap.terminalAccessGranted();
    cipherSolved = snap.cipherSolved();
    escapeAttemptUnlocked = snap.escapeAttemptUnlocked();
    hackAttemptsThisLoop = snap.hackAttemptsThisLoop();
    refreshTerminalAccess();
  }

  public enum GlitchLevel {
    NONE, LIGHT, MEDIUM, HEAVY
  }

  record Chapter1Snapshot(
      int loop,
      int prison,
      int suspicion,
      int trust,
      boolean[] cipherFragments,
      ShopEquipSlot[] inspectedSlots,
      boolean terminalAccessGranted,
      boolean cipherSolved,
      boolean escapeAttemptUnlocked,
      int hackAttemptsThisLoop
  ) {
  }
}
