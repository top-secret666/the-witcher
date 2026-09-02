package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armour;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class ArmourSet {

    public enum Rarity {
        EPIC("Эпический", 1.5),
        LEGENDARY("Легендарный", 2.0);

        private final String displayName;
        private final double effectivenessMultiplier;

        Rarity(String displayName, double effectivenessMultiplier) {
            this.displayName = displayName;
            this.effectivenessMultiplier = effectivenessMultiplier;
        }

        public String getDisplayName() { return displayName; }
        public double getEffectivenessMultiplier() { return effectivenessMultiplier; }
    }

    private final String name;
    private final List<Armour> armorPieces;
    private final SetBonus setBonus;
    private final int requiredLevel;
    private final Rarity rarity;
    private final int basePrice;
    private final double baseWeight;

    protected ArmourSet(String name, SetBonus setBonus, int requiredLevel, Rarity rarity, int basePrice, double baseWeight) {
        this.name = name;
        this.armorPieces = new ArrayList<>();
        this.setBonus = setBonus;
        this.requiredLevel = requiredLevel;
        this.rarity = rarity;
        this.basePrice = basePrice;
        this.baseWeight = baseWeight;
    }

    public record SetBonus(
            String description,
            double damageBonus,
            double protectionBonus,
            double staminaBonus
    ){ }

    public boolean isComplete() {
        return armorPieces.size() >= 4;  // полный комплект из 4 частей
    }

    public double calculateTotalProtection() {
        double baseProtection = armorPieces.stream()
                .mapToDouble(Armour::calculateProtection)
                .sum();
        return isComplete() ? baseProtection * (1 + setBonus.protectionBonus) : baseProtection;
    }

    public String getName() { return name; }
    public List<Armour> getArmorPieces() { return Collections.unmodifiableList(armorPieces); }
    public SetBonus getSetBonus() { return setBonus; }
    public int getRequiredLevel() { return requiredLevel; }
    public Rarity getRarity() {
        return rarity;
    }

    public int calculateTotalPrice() {
        return (int)(basePrice * rarity.getEffectivenessMultiplier());
    }

    public double calculateTotalWeight() {
        return baseWeight * (1 + setBonus.protectionBonus());
    }

    protected void setArmorPieces(List<Armour> pieces) {
        this.armorPieces.clear();
        this.armorPieces.addAll(pieces);
    }

}

