package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public class Gloves extends Armour {
    private final int dexterityBonus;
    private final int gripStrength;
    private final boolean reinforcedKnuckles;

    public Gloves(String name,
                  ArmourType type,
                  ArmourCategory category,
                  int basePrice,
                  double weight,
                  int dexterityBonus,
                  int gripStrength,
                  boolean reinforcedKnuckles) {
        super(name, type, category, calculateAdjustedPrice(basePrice, dexterityBonus, gripStrength), weight, true);
        this.dexterityBonus = dexterityBonus;
        this.gripStrength = gripStrength;
        this.reinforcedKnuckles = reinforcedKnuckles;
    }

    public double calculateEffectiveness() {
        return dexterityBonus * 1.5 + gripStrength * 1.2;
    }

    private static int calculateAdjustedPrice(int basePrice, int dexterityBonus, int gripStrength) {
        double effectiveness = dexterityBonus * 1.5 + gripStrength * 1.2;
        double priceMultiplier = 1.0;

        if (effectiveness > 25) priceMultiplier = 2.2;
        else if (effectiveness > 15) priceMultiplier = 1.8;
        else if (effectiveness > 8) priceMultiplier = 1.4;

        return (int)(basePrice * priceMultiplier);
    }

    @Override
    public double calculateProtection() {
        double baseProtection = calculateEffectiveness() * 0.3;
        if (reinforcedKnuckles) {
            baseProtection *= 1.2;
        }
        return baseProtection;
    }

    @Override
    public String getDescription() {
        return String.format("%s\nБонус ловкости: %d\nСила хвата: %d\nОбщая эффективность: %.2f\n%s",
                getName(),
                dexterityBonus,
                gripStrength,
                calculateEffectiveness(),
                reinforcedKnuckles ? "Усиленные костяшки" : "Обычные костяшки");
    }

    public int getDexterityBonus() {
        return dexterityBonus;
    }

    public int getGripStrength() {
        return gripStrength;
    }

    public boolean hasReinforcedKnuckles() {
        return reinforcedKnuckles;
    }
}
