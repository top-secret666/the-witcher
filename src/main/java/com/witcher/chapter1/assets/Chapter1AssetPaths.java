package main.java.com.witcher.chapter1.assets;

/** Пути к ассетам главы 1 (строки) — общее для Swing и движка. */
public final class Chapter1AssetPaths {

  public static final String SPRITES = "/assets/sprites/chapter1/";
  public static final String GLITCH_HEAVY = SPRITES + "glitch_overlay_heavy.png";
  public static final String HACK_FRAME = SPRITES + "hack_terminal_frame.png";
  public static final String HACK_TIMER = SPRITES + "hack_timer_bar.png";
  public static final String HACK_HIDDEN_HINT = SPRITES + "hack_hidden_hint.png";
  public static final String BOOT_BG = SPRITES + "escape_boot_bg.png";

  public static final String BATTLE = SPRITES + "battle/";
  public static final String CARD_ICON = BATTLE + "card_icon.png";
  public static final String CARD_CLOSED = BATTLE + "card_closed.png";
  public static final String CARD_MAP_OPEN = BATTLE + "card_map_open.png";
  public static final String BOSS_DUKE_MAP = BATTLE + "boss_duke_map.png";
  public static final String BOSS_DUKE_PORTRAIT = BATTLE + "boss_duke_portrait.png";
  public static final String BOSS_WAKE_FOREST = BATTLE + "boss_wake_forest.png";

  private Chapter1AssetPaths() {
  }
}
