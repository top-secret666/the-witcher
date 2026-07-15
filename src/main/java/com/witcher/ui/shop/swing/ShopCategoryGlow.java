package main.java.com.witcher.ui.shop.swing;

import main.java.com.witcher.ui.shop.ShopCategory;

import java.awt.Color;

/**
 * Цвета мягкого свечения при покупке (палитра лавки).
 * Описание в инвентаре/экипировке берёт тот же оттенок.
 */
public final class ShopCategoryGlow {

    public record Tint(Color outer, Color inner) {
    }

    private static final Tint GOLD = new Tint(new Color(255, 210, 80), new Color(255, 235, 150));
    private static final Tint CHEST = new Tint(new Color(168, 110, 58), new Color(210, 160, 95));
    private static final Tint LEGS = new Tint(new Color(72, 118, 168), new Color(130, 175, 220));
    private static final Tint BOOTS = new Tint(new Color(78, 138, 72), new Color(150, 210, 100));
    private static final Tint GLOVES = new Tint(new Color(148, 72, 58), new Color(188, 115, 85));
    private static final Tint SETS = new Tint(new Color(148, 78, 190), new Color(210, 150, 255));
    private static final Tint POTION = new Tint(new Color(90, 150, 120), new Color(150, 220, 170));
    private static final Tint WEAPON = new Tint(new Color(180, 150, 70), new Color(230, 205, 120));

    private ShopCategoryGlow() {
    }

    public static Tint forCategory(ShopCategory category) {
        if (category == null) {
            return GOLD;
        }
        return switch (category) {
            case CHEST -> CHEST;
            case LEGS -> LEGS;
            case BOOTS -> BOOTS;
            case GLOVES -> GLOVES;
            case SETS -> SETS;
            case POTION -> POTION;
            case WEAPON -> WEAPON;
            default -> GOLD;
        };
    }

    /** Цвет текста описания — тот же оттенок, что свечение при полёте в сумку. */
    public static Color descriptionColor(ShopCategory category) {
        return forCategory(category).inner();
    }

    public static Color borderColor(ShopCategory category) {
        return forCategory(category).outer();
    }
}
