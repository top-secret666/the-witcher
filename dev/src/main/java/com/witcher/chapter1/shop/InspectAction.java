package com.witcher.chapter1.shop;

import com.witcher.chapter1.Chapter1Session;
import com.witcher.shop.EquipSlot;
import com.witcher.ui.shop.DukeLines;

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
    // Осмотр-фрагмент шифра убран — обычная реплика осмотра.
    String line = DukeLines.rowInspect(itemName != null ? itemName : "предмет", 0);
    return new InspectAction(false, "", slot, line);
  }
}
