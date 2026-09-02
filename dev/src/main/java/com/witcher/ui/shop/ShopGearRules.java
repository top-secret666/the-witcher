package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.Boots;
import main.java.com.witcher.model.armour.Chestpiece;
import main.java.com.witcher.model.armour.Gloves;
import main.java.com.witcher.model.armour.Trousers;
import main.java.com.witcher.model.sets.ArmourSet;
import main.java.com.witcher.model.sets.SchoolSet;

/** Пересчёт доменной брони в три шкалы витрины. */
public final class ShopGearRules {

    static final int STAT_BAR_MAX = 50;

    private ShopGearRules() {
    }

    static ShopGearStats bonusFromArmour(Armour armour) {
        int prot = clamp(Math.round((float) armour.calculateProtection() * 0.5f), 1, 16);
        double weight = armour.getWeight();
        int stamina = -clamp(Math.round((float) weight * 0.28f), 0, 9);
        int signs = -clamp(Math.round((float) weight * 0.12f), 0, 5);

        if (armour instanceof Boots boots) {
            stamina += boots.getSpeedBonus() + boots.getBalanceBonus() / 2;
        } else if (armour instanceof Gloves gloves) {
            stamina += Math.max(1, gloves.getDexterityBonus() / 3);
            signs += 1;
        } else if (armour instanceof Trousers trousers) {
            stamina += Math.max(1, trousers.getMovementBonus() / 3);
        } else if (armour instanceof Chestpiece) {
            stamina -= 1;
            signs -= 1;
        }
        return new ShopGearStats(prot, stamina, signs);
    }

    static ShopGearStats bonusFromSet(ArmourSet set) {
        if (set instanceof SchoolSet school) {
            return switch (school.getSchoolType()) {
                case WOLF -> new ShopGearStats(11, -3, 5);
                case CAT -> new ShopGearStats(5, 7, 2);
                case GRIFFIN -> new ShopGearStats(7, -2, 9);
                case BEAR -> new ShopGearStats(15, -7, -2);
                case MANTICORE -> new ShopGearStats(9, 1, 6);
            };
        }
        return new ShopGearStats(9, -2, 3);
    }

    public static ShopGearStats placeholderBonus(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("меч") || lower.contains("клеймор")) {
            return new ShopGearStats(0, -2, 0);
        }
        if (lower.contains("кинжал") || lower.contains("арбалет")) {
            return new ShopGearStats(0, -1, 0);
        }
        if (lower.contains("зелье") || lower.contains("эликсир")) {
            return new ShopGearStats(0, 0, 4);
        }
        if (lower.contains("отвар")) {
            return new ShopGearStats(0, 1, 5);
        }
        return new ShopGearStats(0, 0, 0);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
