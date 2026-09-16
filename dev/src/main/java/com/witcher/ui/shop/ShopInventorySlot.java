package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.sets.ArmourSet;

/** Один слот иконки в панели инвентаря лавки. */
public record ShopInventorySlot(
    ShopInventoryKind kind,
    String title,
    String[] detailLines,
    ShopCategory iconCategory,
    Armour armour,
    ArmourSet armourSet
) {

  public static ShopInventorySlot wallet(ShopModel model) {
    String amount = model.walletAmountText() + model.walletSuffix();
    return new ShopInventorySlot(
        ShopInventoryKind.WALLET,
        "Кошелёк",
        new String[]{
            "Золотой мешок с гонораром.",
            amount + " — плата за Арнскрон."
        },
        ShopCategory.POTION,
        null,
        null);
  }

  public static ShopInventorySlot battleCard() {
    return new ShopInventorySlot(
        ShopInventoryKind.BATTLE_CARD,
        "Карта контрактов",
        new String[]{
            "Метки целей на Арнскроне.",
            "Откройте карту, чтобы выбрать встречу."
        },
        ShopCategory.WEAPON,
        null,
        null);
  }

  public static ShopInventorySlot consumable(String name, ShopCategory category, String[] detailLines) {
    ShopInventoryKind kind = category == ShopCategory.WEAPON
        ? ShopInventoryKind.WEAPON
        : ShopInventoryKind.POTION;
    return new ShopInventorySlot(kind, name, detailLines, category, null, null);
  }

  public static ShopInventorySlot armour(Armour piece, boolean equipped) {
    ShopCategory cat = EquipmentArmourList.categoryFor(piece);
    return new ShopInventorySlot(
        ShopInventoryKind.ARMOUR,
        piece.getName(),
        new String[]{
            cat.label,
            equipped ? "Надето." : "Не надето. Нажмите «Надеть»."
        },
        cat,
        piece,
        null);
  }

  public static ShopInventorySlot set(ArmourSet set, boolean equipped) {
    return new ShopInventorySlot(
        ShopInventoryKind.SET,
        set.getName(),
        new String[]{
            "Комплект",
            equipped
                ? "Надето целиком."
                : "Не надето. «Надеть» — все четыре части."
        },
        ShopCategory.SETS,
        null,
        set);
  }

  public static ShopInventorySlot weapon(String name, String[] detailLines, boolean equipped) {
    java.util.ArrayList<String> lines = new java.util.ArrayList<>();
    if (detailLines != null) {
      for (String line : detailLines) {
        if (line == null || line.isBlank()) {
          continue;
        }
        if (line.startsWith("Надето") || line.startsWith("Не надето")) {
          continue;
        }
        lines.add(line);
      }
    }
    if (lines.isEmpty()) {
      lines.add("Оружие");
    }
    lines.add(equipped ? "Надето." : "Не надето. Нажмите «Надеть».");
    return new ShopInventorySlot(
        ShopInventoryKind.WEAPON,
        name,
        lines.toArray(new String[0]),
        ShopCategory.WEAPON,
        null,
        null);
  }

  public String actionLabel() {
    return kind.actionLabel();
  }
}
