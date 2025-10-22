package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public class Armor extends Chestpiece {
    private final int strengthRequirement;  //Требуемая сила
    private final int armorDurabilityBonus; //Бонус прочности
    private final ChestpieceStats stats;

    public Armor(String name,
                 ArmourType type,
                 ArmourCategory category,
                 ChestpieceStats stats,
                 int basePrice,
                 double weight,
                 int strengthRequirement,
                 int armorDurabilityBonus) {
        super(name, type, category, stats, calculateAdjustedPrice(basePrice, strengthRequirement, armorDurabilityBonus),
                weight, ChestpieceType.ARMOR, armorDurabilityBonus);
        this.strengthRequirement = strengthRequirement;
        this.armorDurabilityBonus = armorDurabilityBonus;
        this.stats = stats;
        stats.addArmorEffects(armorDurabilityBonus);
    }

    // Расчет общей эффективности брони
    public double calculateEffectiveness() {
        return strengthRequirement * 1.2 + armorDurabilityBonus * 1.8;
    }

    // Корректировка цены на основе общей эффективности
    private static int calculateAdjustedPrice(int basePrice, int strengthRequirement, int armorDurabilityBonus) {
        double effectiveness = strengthRequirement * 1.2 + armorDurabilityBonus * 1.8;
        double priceMultiplier = 1.0;

        if (effectiveness > 40) priceMultiplier = 3.0;
        else if (effectiveness > 30) priceMultiplier = 2.5;
        else if (effectiveness > 20) priceMultiplier = 2.0;

        return (int)(basePrice * priceMultiplier);
    }

    @Override
    public double calculateProtection() {
        return getChestProtection() + (calculateEffectiveness() * 0.3);
    }

    @Override
    public String getDescription() {
        return String.format("""
            %s
            Требуемая сила: %d
            Бонус прочности: %d
            Общая эффективность: %.2f
            Итоговая защита: %.2f""",
                super.getDescription(),
                strengthRequirement,
                armorDurabilityBonus,
                calculateEffectiveness(),
                calculateProtection());
    }

    public int getStrengthRequirement() {
        return strengthRequirement;
    }

    public int getArmorDurabilityBonus() {
        return armorDurabilityBonus;
    }

    public ChestpieceStats getChestpieceStats() {
        return stats;
    }
}
