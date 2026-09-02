package main.java.com.witcher.chapter1;

/**
 * Фазы главы 1 — переключает {@link Chapter1Director}.
 *
 * <p>Канонический cinematic-путь:
 * {@code SHOP → BOSS_MAP → BOSS_QUEST_BRIEFING → LOOP_SEQUENCE → BOSS_ENCOUNTER
 * → BOSS_FINALE → [BOSS_GLITCH_REVEAL] → WOLF_ENDING → SHOP}.
 *
 * <p>Выдача карты боя — состояние лавки ({@code BATTLE_CARD_REVEAL} в ShopPresenter),
 * а не отдельная фаза директора. Проблески мечей — VFX внутри {@link #BOSS_FINALE}.
 *
 * <p>См. {@link Chapter1CanonicalJourney}.
 */
public enum Chapter1Phase {
  /** Катсцены GIF / meta (не шаг канона лавка→волк). */
  CUTSCENE,
  SHOP,
  BOSS_MAP,
  /** Ложный контракт в лавке перед уходом на босса. */
  BOSS_QUEST_BRIEFING,
  LOOP_SEQUENCE,
  /**
   * Зарезервировано под illusion_wrong / удержание после петли.
   * Из канона не стартует ({@link Chapter1Director#enterLoopHold} — legacy).
   */
  LOOP_HOLD,
  BOSS_ENCOUNTER,
  /** Глитч-пробуждение осколка Волка (только истинная ветка). */
  BOSS_GLITCH_REVEAL,
  /** Финальный выбор + VFX мечей (CLASH) после боя. */
  BOSS_FINALE,
  /** Итог первого босса: плохая петля или осколок (канонический result-экран). */
  WOLF_ENDING,
  /**
   * Legacy-экран «ПОБЕДА/ПОРАЖЕНИЕ» после старого sword→result пайплайна.
   * Канон использует {@link #WOLF_ENDING}; вход только через {@link Chapter1Director#enterBattleResult}.
   */
  BATTLE_RESULT,
  /**
   * Legacy VN-бой. Не часть канона; debug-вход {@code Ctrl+B} в лавке.
   * Исход → {@link Chapter1Director#onBattleDefeat} / возврат в лавку.
   */
  VN_BATTLE,
  VN_DIALOG,
  /** Meta: терминал хака (не связан с мечами / Волком). */
  HACK,
  /** Meta: диалог побега. */
  ENDING
}
