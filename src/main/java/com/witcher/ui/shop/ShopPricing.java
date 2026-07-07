package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.Boots;
import main.java.com.witcher.model.armour.Chestpiece;
import main.java.com.witcher.model.armour.Gloves;
import main.java.com.witcher.model.armour.Trousers;
import main.java.com.witcher.model.sets.ArmourSet;
import main.java.com.witcher.model.sets.SchoolSet;

/**
 * Витринные цены лавки — отдельно от «сырых» цен в доменной модели.
 * Рассчитаны под стартовый кошелёк ~420 крон.
 */
public final class ShopPricing {

    private ShopPricing() {
    }

    public static int armorPrice(Armour armour) {
        int raw = Math.max(10, armour.getPrice());
        double weight = armour.getWeight();
        int tier = 0;
        if (armour instanceof Chestpiece) {
            tier += 2;
        } else if (armour instanceof Trousers) {
            tier += 1;
        } else if (armour instanceof Boots || armour instanceof Gloves) {
            tier -= 1;
        }
        double norm = Math.log1p(raw) / Math.log1p(2000);
        int scaled = 48 + (int) (norm * 118) + tier * 7 + (int) (weight * 1.2);
        int jitter = Math.floorMod(armour.getName().hashCode(), 13) - 6;
        return clamp(scaled + jitter, 48, 198);
    }

    /** Витринная цена, уникальная внутри одной категории каталога. */
    public static int uniqueArmorPrice(Armour armour, java.util.Set<Integer> usedPrices) {
        int base = armorPrice(armour);
        if (usedPrices.add(base)) {
            return base;
        }
        for (int delta = 1; delta <= 75; delta++) {
            if (base + delta <= 198 && usedPrices.add(base + delta)) {
                return base + delta;
            }
            if (base - delta >= 48 && usedPrices.add(base - delta)) {
                return base - delta;
            }
        }
        for (int p = 48; p <= 198; p++) {
            if (usedPrices.add(p)) {
                return p;
            }
        }
        return base;
    }

    public static int setPrice(ArmourSet set) {
        if (set instanceof SchoolSet school) {
            return switch (school.getSchoolType()) {
                case CAT -> 360;
                case WOLF -> 385;
                case MANTICORE -> 395;
                case GRIFFIN -> 410;
                case BEAR -> 420;
            };
        }
        int sum = set.getArmorPieces().stream().mapToInt(ShopPricing::armorPrice).sum();
        return clamp(sum - 40, 320, 450);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
