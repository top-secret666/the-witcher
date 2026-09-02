package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public abstract class Chestpiece extends Armour {
    private final ChestpieceType chestpieceType;
    private final int chestProtection;    // специальная защита для груди
    private final ChestpieceStats chestpieceStats;

    protected Chestpiece(String name,
                         ArmourType type,
                         ArmourCategory category,
                         ChestpieceStats stats,
                         int price,
                         double weight,
                         ChestpieceType chestpieceType,
                         int chestProtection) {
        super(name, type, category, price, weight, false);
        this.chestpieceType = chestpieceType;
        this.chestProtection = chestProtection;
        this.chestpieceStats = stats;
        stats.addChestpieceEffects(chestProtection);
    }

    public enum ChestpieceType {
        BRIGANDINE,    // бригантина
        ARMOR,         // доспех
        CUIRASS,       // кираса
        BREASTPLATE    // нагрудник
    }

    public ChestpieceType getChestpieceType() {
        return chestpieceType;
    }

    public int getChestProtection() {
        return chestProtection;
    }

    @Override
    public String getDescription() {
        return String.format("%s - Тип: %s, Защита груди: %d",
                getName(), chestpieceType, chestProtection);
    }

    public ChestpieceStats getChestpieceStats() {
        return chestpieceStats;
    }

    public double calculateEffectiveness() {
        if (getCategory() == ArmourCategory.SET_ITEM) {
            return 1000.0 * 2.75;
        }
        return 0.0;
    }

}
