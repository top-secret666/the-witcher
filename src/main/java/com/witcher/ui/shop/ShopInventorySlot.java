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

  public static ShopInventorySlot armour(Armour piece) {
    ShopCategory cat = EquipmentArmourList.categoryFor(piece);
    return new ShopInventorySlot(
        ShopInventoryKind.ARMOUR,
        piece.getName(),
        new String[]{
            cat.label,
            "Купленный предмет. Откройте экипировку, чтобы надеть."
        },
        cat,
        piece,
        null);
  }

  public static ShopInventorySlot set(ArmourSet set) {
    return new ShopInventorySlot(
        ShopInventoryKind.SET,
        set.getName(),
        new String[]{
            "Комплект",
            "Эмблема набора. Экипировка надевает все четыре части."
        },
        ShopCategory.SETS,
        null,
        set);
  }

  public String actionLabel() {
    return kind.actionLabel();
  }
}
