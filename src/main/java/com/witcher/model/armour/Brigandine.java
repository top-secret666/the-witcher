package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public class Brigandine extends Chestpiece {
    private final int flexibilityBonus; // Бонус гибкости
    private final int stealthBonus; //Бонус скрытности
    private final ChestpieceStats stats;

    public Brigandine(String name,
                      ArmourType type,
                      ArmourCategory category,
                      ChestpieceStats stats,
                      int basePrice,
                      double weight,
                      int flexibilityBonus,
                      int stealthBonus) {
        super(name, type, category, stats, calculateAdjustedPrice(basePrice, flexibilityBonus, stealthBonus),
                weight, ChestpieceType.BRIGANDINE, stealthBonus);
        this.flexibilityBonus = flexibilityBonus;
        this.stealthBonus = stealthBonus;
        this.stats = stats;
        stats.addBrigandineEffects(flexibilityBonus, stealthBonus);
    }

    // Расчет общей эффективности
    public double calculateEffectiveness() {
        return flexibilityBonus * 1.5 + stealthBonus * 2.0;
    }

    // Корректировка цены на основе общей эффективности
    private static int calculateAdjustedPrice(int basePrice, int flexibilityBonus, int stealthBonus) {
        double effectiveness = flexibilityBonus * 1.5 + stealthBonus * 2.0;
        double priceMultiplier = 1.0;

        if (effectiveness > 30) priceMultiplier = 2.5;
        else if (effectiveness > 20) priceMultiplier = 2.0;
        else if (effectiveness > 10) priceMultiplier = 1.5;

        return (int)(basePrice * priceMultiplier);
    }

    @Override
    public double calculateProtection() {
        return getChestProtection() + (calculateEffectiveness() * 0.2);
    }

    @Override
    public String getDescription() {
        return String.format("""
            %s
            Бонус гибкости: %d
            Бонус скрытности: %d
            Общая эффективность: %.2f
            Итоговая защита: %.2f""",
                super.getDescription(),
                flexibilityBonus,
                stealthBonus,
                calculateEffectiveness(),
                calculateProtection());
    }

    public int getFlexibilityBonus() {
        return flexibilityBonus;
    }

    public int getStealthBonus() {
        return stealthBonus;
    }

    public ChestpieceStats getChestpieceStats() {
        return stats;
    }
}