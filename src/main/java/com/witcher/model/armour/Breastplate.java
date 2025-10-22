package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public class Breastplate extends Chestpiece {
    private final int magicResistance;
    private final int staminaBonus;
    private final ChestpieceStats stats;

    public Breastplate(String name,
                       ArmourType type,
                       ArmourCategory category,
                       ChestpieceStats stats,
                       int basePrice,
                       double weight,
                       int magicResistance,
                       int staminaBonus,
                       int chestProtection) {
        super(name, type, category, stats, calculateAdjustedPrice(basePrice, magicResistance, staminaBonus),
                weight, ChestpieceType.BREASTPLATE, chestProtection);
        this.magicResistance = magicResistance;
        this.staminaBonus = staminaBonus;
        this.stats = stats;
        stats.addBreastplateEffects(chestProtection);
    }

    public double calculateEffectiveness() {
        return magicResistance * 2.0 + staminaBonus * 1.5;
    }

    private static int calculateAdjustedPrice(int basePrice, int magicResistance, int staminaBonus) {
        double effectiveness = magicResistance * 3.0 + staminaBonus * 2.5;
        double priceMultiplier = 1.0;

        if (effectiveness > 40) priceMultiplier = 3.0;
        else if (effectiveness > 30) priceMultiplier = 2.5;
        else if (effectiveness > 20) priceMultiplier = 2.0;

        return (int)(basePrice * priceMultiplier);
    }

    @Override
    public double calculateProtection() {
        return getChestProtection() + (calculateEffectiveness() * 0.25);
    }

    @Override
    public String getDescription() {
        return String.format("""
            %s
            Сопротивление магии: %d
            Бонус выносливости: %d
            Общая эффективность: %.2f
            Итоговая защита: %.2f""",
                super.getDescription(),
                magicResistance,
                staminaBonus,
                calculateEffectiveness(),
                calculateProtection());
    }

    public int getMagicResistance() {
        return magicResistance;
    }

    public int getStaminaBonus() {
        return staminaBonus;
    }

    public ChestpieceStats getChestpieceStats() {
        return stats;
    }
}
