package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public class Cuirass extends Chestpiece {
    private final int balanceBonus;  //Бонус равновесия
    private final int counterAttackChance; // Шанс контратаки
    private final ChestpieceStats stats;

    public Cuirass(String name,
                   ArmourType type,
                   ArmourCategory category,
                   ChestpieceStats stats,
                   int basePrice,
                   double weight,
                   int balanceBonus,
                   int counterAttackChance,
                   int chestProtection) {
        super(name, type, category, stats, calculateAdjustedPrice(basePrice, balanceBonus, counterAttackChance),
                weight, ChestpieceType.CUIRASS, chestProtection);
        this.balanceBonus = balanceBonus;
        this.counterAttackChance = counterAttackChance;
        this.stats = stats;
        stats.addCuirassEffects(counterAttackChance);
    }

    // Расчет общей эффективности
    public double calculateCombatEfficiency() {
        return balanceBonus * 1.5 + counterAttackChance * 2.0;
    }

    // Корректировка цены на основе общей эффективности
    private static int calculateAdjustedPrice(int basePrice, int balanceBonus, int counterAttackChance) {
        double efficiency = balanceBonus * 1.5 + counterAttackChance * 2.0;
        double priceMultiplier = 1.0;

        if (efficiency > 40) priceMultiplier = 3.0;
        else if (efficiency > 30) priceMultiplier = 2.5;
        else if (efficiency > 20) priceMultiplier = 2.0;

        return (int)(basePrice * priceMultiplier);
    }

    @Override
    public double calculateProtection() {
        return getChestProtection() + (calculateCombatEfficiency() * 0.25);
    }

    @Override
    public String getDescription() {
        return String.format("""
            %s
            Бонус равновесия: %d
            Шанс контратаки: %d%%
            Боевая эффективность: %.2f
            Итоговая защита: %.2f""",
                super.getDescription(),
                balanceBonus,
                counterAttackChance,
                calculateCombatEfficiency(),
                calculateProtection());
    }

    public int getBalanceBonus() {
        return balanceBonus;
    }

    public int getCounterAttackChance() {
        return counterAttackChance;
    }

    public ChestpieceStats getChestpieceStats() {
        return stats;
    }
}
