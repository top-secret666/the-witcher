package main.java.com.witcher.chapter1.battle;

import main.java.com.witcher.chapter1.assets.Chapter1AssetPaths;

import java.util.List;

/** Список боссов на карте (пока только герцог). */
public final class BossCatalog {

  private static final List<BossEntry> BOSSES = List.of(
      new BossEntry(
          "duke",
          "Герцог",
          "Хозяин иллюзий",
          9, 8, 6,
          Chapter1AssetPaths.BOSS_DUKE_MAP,
          Chapter1AssetPaths.BOSS_DUKE_MAP_HOVER,
          Chapter1AssetPaths.BOSS_DUKE_PORTRAIT,
          80, 168
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
