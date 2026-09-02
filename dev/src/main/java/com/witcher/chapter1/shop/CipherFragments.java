package main.java.com.witcher.chapter1.shop;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.shop.EquipSlot;

import java.util.concurrent.ThreadLocalRandom;

/** Фрагменты шифра — по одному на слот экипировки (порядок = UI лавки). */
public final class CipherFragments {

  /** Шанс найти фрагмент при осмотре нужного слота (0..1). */
  public static final double INSPECT_FIND_CHANCE = 0.55;

  private static final String[] FRAGMENT_TEXT = {
      "K7", "M3", "R9", "V2"
  };

  private CipherFragments() {
  }

  public static EquipSlot slotForCatalogIndex(int categoryOrdinal) {
    EquipSlot[] slots = EquipSlot.values();
    if (categoryOrdinal < 0 || categoryOrdinal >= slots.length) {
      return null;
    }
    return slots[categoryOrdinal];
  }

  public static String fragmentCode(EquipSlot slot) {
    if (slot == null) {
      return "";
    }
    return FRAGMENT_TEXT[slot.ordinal()];
  }

  public static String orderedUnlockCommand(Chapter1Session session) {
    if (session == null) {
      return "";
    }
    StringBuilder sb = new StringBuilder("BREAK_LOOP(");
    boolean first = true;
    for (EquipSlot slot : EquipSlot.values()) {
      if (!session.hasFragment(slot)) {
        return "";
      }
      if (!first) {
        sb.append(", ");
      }
      sb.append(fragmentCode(slot));
      first = false;
    }
    sb.append(')');
    return sb.toString();
  }

  public static boolean tryInspectFind(Chapter1Session session, EquipSlot slot) {
    if (session == null || slot == null || session.hasFragment(slot)) {
      return false;
    }
    session.markInspected(slot);
    if (ThreadLocalRandom.current().nextDouble() >= INSPECT_FIND_CHANCE) {
      return false;
    }
    return session.tryCollectFragment(slot);
  }
}
