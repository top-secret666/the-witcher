package main.java.com.witcher.chapter1.assets;

/** Пути к ассетам главы 1 (строки) — общее для Swing и движка. */
public final class Chapter1AssetPaths {

  public static final String SPRITES = "/assets/sprites/chapter1/";
  public static final String GLITCH_HEAVY = SPRITES + "glitch_overlay_heavy.png";
  public static final String GLITCH_MEDIUM = SPRITES + "glitch_overlay_medium.png";
  public static final String HACK_FRAME = SPRITES + "hack_terminal_frame.png";
  public static final String HACK_TIMER = SPRITES + "hack_timer_bar.png";
  public static final String HACK_HIDDEN_HINT = SPRITES + "hack_hidden_hint.png";
  public static final String BOOT_BG = SPRITES + "escape_boot_bg.png";

  public static final String BATTLE = SPRITES + "battle/";
  public static final String UI = SPRITES + "ui/";
  public static final String CARD_ICON = BATTLE + "card_icon.png";
  public static final String CARD_CLOSED = BATTLE + "card_closed.png";
  public static final String CARD_MAP_OPEN = BATTLE + "card_map_open.png";
  public static final String BOSS_DUKE_MAP = BATTLE + "boss_duke_map.png";
  public static final String BOSS_DUKE_MAP_HOVER = BATTLE + "boss_duke_map_hover.png";
  public static final String BOSS_DUKE_PORTRAIT = BATTLE + "boss_duke_portrait.png";
  public static final String VOLK_DUKE_MAP = BATTLE + "volk_duke_dialog_stand.png";
  public static final String VOLK_DUKE_MAP_ATTACK = BATTLE + "volk_duke_dialog_knife.png";
  public static final String VOLK_DUKE_MAP_INTERESTED = BATTLE + "volk_duke_dialog_reach.png";
  /** Бросок/хватание — агрессивный жест в диалоге. */
  public static final String VOLK_DUKE_MAP_LUNGE = BATTLE + "volk_duke_dialog_lunge.png";
  /** Кровавый коридор — глитч-диалог после победы в катсцене мечей. */
  public static final String BOSS_BLOOD_CORRIDOR = BATTLE + "boss_blood_corridor.png";
  /** 3×3: глаза/улыбка в глитче. */
  public static final String BOSS_GLITCH_AWAKEN_SHEET = BATTLE + "boss_glitch_awaken_sheet.png";
  /** Финальный кадр осколка Волка (резкость нарастает). */
  public static final String WOLF_SHARD_REVEAL = BATTLE + "wolf_shard_reveal.png";
  /** Спрайт Волка после открытия век. */
  public static final String WOLF_SHARD_AWAKEN = BATTLE + "wolf_shard_awaken.png";
  /** Туманный лес с множеством глаз — фон в глитч-диалоге. */
  public static final String WOLF_FOREST_EYES = BATTLE + "wolf_forest_eyes.png";
  /** Холодный туманный лес (без глаз) — цикл фонов под листом. */
  public static final String WOLF_MIST_FOREST = BATTLE + "wolf_mist_forest.png";
  public static final String BOSS_WAKE_FOREST = BATTLE + "boss_wake_forest.png";
  /** Лист заказа с доски (контракт на босса) — брифинг в лавке. */
  public static final String BOSS_QUEST_NOTICE = BATTLE + "boss_quest_notice.png";
  /** Портрет Волка над диалогом (лес). */
  public static final String WOLF_PORTRAIT = BATTLE + "wolf_portrait.png";
  /** Воспоминание: дорога на Ард Каррайг. */
  public static final String MEMORY_ARD_CARRAIG = BATTLE + "memory_ard_carraig.png";
  /** Воспоминание: Каэр Морхен. */
  public static final String MEMORY_KAER_MORHEN = BATTLE + "memory_kaer_morhen.png";

  public static final String HALO_WALLET = UI + "halo_wallet.png";
  public static final String HALO_POTION = UI + "halo_potion.png";
  public static final String HALO_MAP = UI + "halo_map.png";
  public static final String HALO_WEAPON = UI + "halo_weapon.png";

  private Chapter1AssetPaths() {
  }
}
