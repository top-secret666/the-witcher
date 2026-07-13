package main.java.com.witcher.ui.shop;

/** Один слот иконки в панели инвентаря лавки. */
public record ShopInventorySlot(
    ShopInventoryKind kind,
    String title,
    String[] detailLines,
    ShopCategory iconCategory
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
        ShopCategory.POTION);
  }

  public static ShopInventorySlot battleCard() {
    return new ShopInventorySlot(
        ShopInventoryKind.BATTLE_CARD,
        "Карта контрактов",
        new String[]{
            "Метки целей на Арнскроне.",
            "Откройте карту, чтобы выбрать встречу."
        },
        ShopCategory.WEAPON);
  }

  public static ShopInventorySlot consumable(String name, ShopCategory category, String[] detailLines) {
    ShopInventoryKind kind = category == ShopCategory.WEAPON
        ? ShopInventoryKind.WEAPON
        : ShopInventoryKind.POTION;
    return new ShopInventorySlot(kind, name, detailLines, category);
  }
}
