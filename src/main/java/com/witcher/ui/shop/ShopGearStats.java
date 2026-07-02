package main.java.com.witcher.ui.shop;

/** Три шкалы на обороте карточки (как в RPG-меню). */
public record ShopGearStats(int protection, int stamina, int signs) {

    public static ShopGearStats geraltBase() {
        return new ShopGearStats(18, 32, 24);
    }

    public ShopGearStats plus(ShopGearStats other) {
        return new ShopGearStats(
            protection + other.protection,
            stamina + other.stamina,
            signs + other.signs);
    }

    public ShopGearStats clamped() {
        return new ShopGearStats(
            clamp(protection, 0, 99),
            clamp(stamina, 0, 99),
            clamp(signs, 0, 99));
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
