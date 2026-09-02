package main.java.com.witcher.chapter1.shop;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.shop.EquipSlot;

/**
 * Кусочки шифра из воспоминаний боссов (не из товаров лавки).
 * Волк — первый осколок ({@link EquipSlot#CHEST} / K7).
 */
public final class BossMemoryFragments {

  /** Первый фрагмент — воспоминание о Каэр Морхене после истинной развилки Волка. */
  public static final EquipSlot WOLF_FRAGMENT_SLOT = EquipSlot.CHEST;

  private BossMemoryFragments() {
  }

  public static String wolfFragmentCode() {
    return CipherFragments.fragmentCode(WOLF_FRAGMENT_SLOT);
  }

  public static boolean grantWolfShard(Chapter1Session session) {
    if (session == null || session.hasFragment(WOLF_FRAGMENT_SLOT)) {
      return false;
    }
    session.tryCollectFragment(WOLF_FRAGMENT_SLOT);
    session.markWolfBossResolved(true);
    return true;
  }
}
