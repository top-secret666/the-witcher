package main.java.com.witcher.chapter1;

/** Фазы главы 1 — переключает {@link Chapter1Director}. */
public enum Chapter1Phase {
  /** Полноэкранная катсцена (Netflix + пост-обработка). */
  CUTSCENE,
  /** Лавка герцога (пиксель-UI). */
  SHOP,
  /** Сцена выдачи карты боя (как кошелёк). */
  CARD_REVEAL,
  /** Карта боссов на весь экран. */
  BOSS_MAP,
  /** Цепочка loop_wake → глаза → illusion_wrong. */
  LOOP_SEQUENCE,
  /** Пауза после illusion_wrong — ничего не происходит. */
  LOOP_HOLD,
  /** VN-сцена боя: выбор тактики, без RPG-HUD. */
  VN_BATTLE,
  /** VN-диалог (герцог, финал, подсказки). */
  VN_DIALOG,
  /** Терминал взлома шифра. */
  HACK,
  /** Финальная развилка побега. */
  ENDING
}
