package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public class Pants extends Trousers {
    private final int movementBonus;
    private final int durabilityBonus;
    private final TrousersStats stats;

    public Pants(String name,
                 ArmourType type,
                 ArmourCategory category,
                 TrousersStats stats,
                 int basePrice,
                 double weight,
                 int movementBonus,
                 int durabilityBonus) {
        super(name, type, category, stats, calculateAdjustedPrice(basePrice, movementBonus, durabilityBonus),
                weight, movementBonus, TrouserType.PANTS);
        this.movementBonus = movementBonus;
        this.durabilityBonus = durabilityBonus;
        this.stats = stats;
        stats.addPantsEffects(durabilityBonus);
    }


    // Расчет общей эффективности
    public double calculateEffectiveness() {
        return movementBonus * 1.5 + durabilityBonus * 1.8;
    }

    // Корректировка цены на основе общей эффективности
    private static int calculateAdjustedPrice(int basePrice, int movementBonus, int durabilityBonus) {
        double effectiveness = movementBonus * 1.5 + durabilityBonus * 1.8;
        double priceMultiplier = 1.0;

        if (effectiveness > 25) priceMultiplier = 2.2;
        else if (effectiveness > 15) priceMultiplier = 1.8;
        else if (effectiveness > 8) priceMultiplier = 1.4;

        return (int)(basePrice * priceMultiplier);
    }

    @Override
    public double calculateProtection() {
        return calculateEffectiveness() * 0.25;
    }

    @Override
    public String getDescription() {
        return String.format("%s\nБонус движения: %d\nБонус прочности: %d\nОбщая эффективность: %.2f",
                super.getDescription(),
                movementBonus,
                durabilityBonus,
                calculateEffectiveness());
    }

    public int getMovementBonus() {
        return movementBonus;
    }

    public int getDurabilityBonus() {
        return durabilityBonus;
    }

    public TrousersStats getTrousersStats() {
        return stats;
    }
}

