package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

public abstract class Trousers extends Armour { // усиленные колени
    private final int movementBonus;
    private final TrouserType trouserType;
    private final TrousersStats trousersStats;

    protected Trousers(String name,
                    ArmourType type,
                    ArmourCategory category,
                    TrousersStats stats,
                    int price,
                    double weight,
                    int movementBonus,
                    TrouserType trouserType) {
        super(name, type, category, price, weight, false);
        this.movementBonus = movementBonus;
        this.trouserType = trouserType;
        stats.addTrousersEffects(movementBonus);
        this.trousersStats = stats;

    }

    public enum TrouserType {
        BREECHES,   // бриджи
        PANTS,      // брюки
        LEGGINGS    // штаны
    }

    @Override
    public double calculateProtection() {
        return (movementBonus * 0.5);
    }

    @Override
    public String getDescription() {
        return String.format("%s\nТип: %s\nБонус подвижности: %d",
                getName(),
                trouserType,
                movementBonus);
    }

    public int getMovementBonus() {
        return movementBonus;
    }

    public TrouserType getTrouserType() {
        return trouserType;
    }

    public TrousersStats getTrousersStats() {
        return trousersStats;
    }
}