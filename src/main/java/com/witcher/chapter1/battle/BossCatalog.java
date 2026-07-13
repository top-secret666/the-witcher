package main.java.com.witcher.chapter1.battle;

import java.util.List;

/** Список боссов на карте (пока только герцог). */
public final class BossCatalog {

  private static final String BATTLE_SPRITES = "/assets/sprites/chapter1/battle/";

  private static final List<BossEntry> BOSSES = List.of(
      new BossEntry(
          "duke",
          "Герцог",
          "Хозяин иллюзии",
          9, 8, 6,
          BATTLE_SPRITES + "boss_duke_map.png",
          BATTLE_SPRITES + "boss_duke_portrait.png",
          280, 150
      )
  );

  private BossCatalog() {
  }

  public static List<BossEntry> all() {
    return BOSSES;
  }

  public static BossEntry byId(String id) {
    if (id == null) {
      return null;
    }
    for (BossEntry boss : BOSSES) {
      if (boss.id().equals(id)) {
        return boss;
      }
    }
    return null;
  }
}
