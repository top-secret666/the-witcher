package main.java.com.witcher.chapter1;

/**
 * Канонический cinematic-путь главы 1 (для Swing и будущего порта на движок).
 *
 * <p>Выдача карты боя — состояние лавки {@code BATTLE_CARD_REVEAL}, не фаза директора.
 *
 * <p>Проблески мечей — VFX внутри {@link Chapter1Phase#BOSS_FINALE} (шаг CLASH),
 * не отдельная фаза директора.
 */
public final class Chapter1CanonicalJourney {

  /** Порядок фаз директора по канону (без legacy). */
  public static final Chapter1Phase[] DIRECTOR_PHASES = {
      Chapter1Phase.SHOP,
      Chapter1Phase.BOSS_MAP,
      Chapter1Phase.BOSS_QUEST_BRIEFING,
      Chapter1Phase.LOOP_SEQUENCE,
      Chapter1Phase.BOSS_ENCOUNTER,
      Chapter1Phase.BOSS_FINALE,
      Chapter1Phase.BOSS_GLITCH_REVEAL,
      Chapter1Phase.WOLF_ENDING,
      Chapter1Phase.SHOP
  };

  private Chapter1CanonicalJourney() {
  }

  /** Фазы вне канона (debug / meta / заготовки). */
  public static boolean isLegacyOrSide(Chapter1Phase phase) {
    return phase == Chapter1Phase.BATTLE_RESULT
        || phase == Chapter1Phase.VN_BATTLE
        || phase == Chapter1Phase.LOOP_HOLD
        || phase == Chapter1Phase.HACK
        || phase == Chapter1Phase.ENDING
        || phase == Chapter1Phase.VN_DIALOG
        || phase == Chapter1Phase.CUTSCENE;
  }
}
