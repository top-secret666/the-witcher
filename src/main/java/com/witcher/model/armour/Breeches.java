package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public class Breeches extends Trousers {
    private final int agilityBonus;
    private final TrousersStats stats;

    public Breeches(String name,
                    ArmourType type,
                    ArmourCategory category,
                    TrousersStats stats,
                    int basePrice,
                    double weight,
                    int movementBonus,
                    int agilityBonus) {
        super(name, type, category, stats, calculateAdjustedPrice(basePrice, movementBonus, agilityBonus),
                weight, movementBonus, TrouserType.BREECHES);
        this.agilityBonus = agilityBonus;
        this.stats = stats;
        stats.addBreechesEffects(agilityBonus);
    }


    // Расчет общей эффективности
    public double calculateEffectiveness() {
        return getMovementBonus() * 1.5 + agilityBonus * 2.0;
    }

    private static int calculateAdjustedPrice(int basePrice, int movementBonus, int agilityBonus) {
        double effectiveness = movementBonus * 1.5 + agilityBonus * 2.0;
        double priceMultiplier = 1.0;

        if (effectiveness > 25) priceMultiplier = 2.5;
        else if (effectiveness > 15) priceMultiplier = 2.0;
        else if (effectiveness > 8) priceMultiplier = 1.5;

        return (int)(basePrice * priceMultiplier);
    }

    @Override
    public double calculateProtection() {
        return calculateEffectiveness() * 0.3;
    }

    @Override
    public String getDescription() {
        return String.format("%s\nБонус ловкости: %d\nОбщая эффективность: %.2f",
                super.getDescription(),
                agilityBonus,
                calculateEffectiveness());
    }

    public int getAgilityBonus() {
        return agilityBonus;
    }

    public TrousersStats getTrousersStats() {
        return stats;
    }
}
