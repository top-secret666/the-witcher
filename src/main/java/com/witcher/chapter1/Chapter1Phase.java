package main.java.com.witcher.chapter1;

/**
 * Фазы главы 1 — переключает {@link Chapter1Director}.
 *
 * <p>Канонический cinematic-путь (глава 1 «бой»):
 * {@code SHOP → BOSS_MAP → BOSS_QUEST_BRIEFING → LOOP_SEQUENCE → BOSS_ENCOUNTER → ...}.
 *
 * <p>Выдача карты боя — это состояние лавки ({@code BATTLE_CARD_REVEAL} в ShopPresenter),
 * а не отдельная фаза директора.
 *
 * <p>{@code VN_BATTLE}, {@code HACK}, {@code ENDING}, {@code LOOP_HOLD} — смежные/legacy пути,
 * не входят в канонический cinematic-конвейер.
 */
public enum Chapter1Phase {
  CUTSCENE,
  SHOP,
  BOSS_MAP,
  /** Ложный контракт в лавке перед уходом на босса. */
  BOSS_QUEST_BRIEFING,
  LOOP_SEQUENCE,
  /** Зарезервировано под illusion_wrong / удержание после петли; сейчас не стартует из канона. */
  LOOP_HOLD,
  BOSS_ENCOUNTER,
  /** Глитч-пробуждение осколка Волка (истинная ветка). */
  BOSS_GLITCH_REVEAL,
  /** Финальный выбор после боя / глитча. */
  BOSS_FINALE,
  /** Итог первого босса: плохая петля или осколок. */
  WOLF_ENDING,
  BATTLE_RESULT,
  VN_BATTLE,
  VN_DIALOG,
  HACK,
  ENDING
}
