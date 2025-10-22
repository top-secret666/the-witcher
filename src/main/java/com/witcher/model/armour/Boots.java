package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public class Boots extends Armour {
    private final int speedBonus;
    private final int balanceBonus;
    private final boolean reinforcedSoles;

    public Boots(String name,
                 ArmourType type,
                 ArmourCategory category,
                 int basePrice,
                 double weight,
                 int speedBonus,
                 int balanceBonus,
                 boolean reinforcedSoles) {
        super(name, type, category, calculateAdjustedPrice(basePrice, speedBonus, balanceBonus, reinforcedSoles),
                weight, true);
        this.speedBonus = speedBonus;
        this.balanceBonus = balanceBonus;
        this.reinforcedSoles = reinforcedSoles;
    }

    private static int calculateAdjustedPrice(int basePrice, int speedBonus, int balanceBonus, boolean reinforcedSoles) {
        double effectiveness = speedBonus * 1.5 + balanceBonus * 1.2;
        double priceMultiplier = 1.0;

        if (effectiveness > 25) priceMultiplier = 2.2;
        else if (effectiveness > 15) priceMultiplier = 1.8;
        else if (effectiveness > 8) priceMultiplier = 1.4;

        if (reinforcedSoles) priceMultiplier += 0.3;

        return (int)(basePrice * priceMultiplier);
    }

    public double calculateEffectiveness() {
        double baseEffectiveness = speedBonus * 1.5 + balanceBonus * 1.2;
        return reinforcedSoles ? baseEffectiveness * 1.25 : baseEffectiveness;
    }

    @Override
    public double calculateProtection() {
        return calculateEffectiveness() * 0.4;
    }

    @Override
    public String getDescription() {
        return String.format("%s\nБонус скорости: %d\nБонус равновесия: %d\nОбщая эффективность: %.2f\n%s",
                getName(),
                speedBonus,
                balanceBonus,
                calculateEffectiveness(),
                reinforcedSoles ? "Усиленная подошва" : "Обычная подошва");
    }

    public int getSpeedBonus() {
        return speedBonus;
    }

    public int getBalanceBonus() {
        return balanceBonus;
    }

    public boolean hasReinforcedSoles() {
        return reinforcedSoles;
    }
}
