package main.java.com.witcher.chapter1.shop;

import main.java.com.witcher.chapter1.Chapter1Session;
import main.java.com.witcher.shop.EquipSlot;

/** Результат осмотра предмета в лавке (без покупки). */
public record InspectAction(
    boolean fragmentFound,
    String fragmentCode,
    EquipSlot slot,
    String dukeLine
) {

  public static InspectAction inspect(Chapter1Session session, EquipSlot slot, String itemName) {
    if (session == null || slot == null) {
      return new InspectAction(false, "", null, "");
    }
    boolean found = CipherFragments.tryInspectFind(session, slot);
    String code = found ? CipherFragments.fragmentCode(slot) : "";
    String line = found
        ? "Хм… в подкладке что-то мерцает. Символы: " + code
        : "Вы внимательно осматриваете «" + itemName + "». Ничего необычного.";
    if (found) {
      session.addSuspicion(1);
    }
    return new InspectAction(found, code, slot, line);
  }
}
