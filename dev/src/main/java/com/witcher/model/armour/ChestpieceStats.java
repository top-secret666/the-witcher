package main.java.com.witcher.model.armour;

import java.util.*;


public class ChestpieceStats {
    private final List<Effect> specialEffects;

    public enum Effect {
        STAMINA_BOOST,     // увеличение выносливости
        CRITICAL_STRIKE,   // шанс критического удара
        MAGIC_RESISTANCE,  // сопротивление магии
        POISON_IMMUNITY,   // иммунитет к яду
        BLEEDING_RESIST    // сопротивление кровотечению
    }

    public ChestpieceStats(int magicResistance, int counterAttackChance, int armorDurabilityBonus, int flexibilityBonus, int chestProtection) {
        this.specialEffects = new ArrayList<>();
        calculateAndAddEffects(magicResistance, counterAttackChance, armorDurabilityBonus, flexibilityBonus, chestProtection);
    }

    protected void calculateAndAddEffects(int magicResistance, int counterAttackChance, int armorDurabilityBonus, int flexibilityBonus, int chestProtection) {
        double totalEffectiveness = flexibilityBonus * 1.5 + armorDurabilityBonus * 1.2 + magicResistance * 1.4 + counterAttackChance * 2 + chestProtection;

        if (totalEffectiveness >= 40) {
            specialEffects.add(Effect.STAMINA_BOOST);
            specialEffects.add(Effect.CRITICAL_STRIKE);
        } else if (totalEffectiveness >= 30) {
            specialEffects.add(Effect.MAGIC_RESISTANCE);
            specialEffects.add(Effect.BLEEDING_RESIST);
        } else if (totalEffectiveness >= 20) {
            specialEffects.add(Effect.POISON_IMMUNITY);
        }
    }
    public List<Effect> getSpecialEffects() {
        return new ArrayList<>(specialEffects);
    }

    public void addChestpieceEffects(int chestProtection) {
        if (chestProtection >= 20) {
            specialEffects.add(Effect.BLEEDING_RESIST);
        }
    }

    public void addCuirassEffects(int counterAttackChance) {
        if (counterAttackChance >= 20) {
            specialEffects.add(Effect.CRITICAL_STRIKE);
        }
    }

    public void addBrigandineEffects(int flexibilityBonus, int stealthBonus) {
        double totalEffectiveness = flexibilityBonus * 1.5 + stealthBonus * 2.0;

        if (totalEffectiveness >= 40) {
            specialEffects.add(Effect.STAMINA_BOOST);
            specialEffects.add(Effect.CRITICAL_STRIKE);
        } else if (totalEffectiveness >= 30) {
            specialEffects.add(Effect.MAGIC_RESISTANCE);
        } else if (totalEffectiveness >= 20) {
            specialEffects.add(Effect.POISON_IMMUNITY);
        }
    }

    public void addBreastplateEffects(int magicResistance) {
        if (magicResistance >= 20) {
            specialEffects.add(Effect.MAGIC_RESISTANCE);
            specialEffects.add(Effect.STAMINA_BOOST);
        } else if (magicResistance >= 15) {
            specialEffects.add(Effect.MAGIC_RESISTANCE);
        } else if (magicResistance >= 10) {
            specialEffects.add(Effect.STAMINA_BOOST);
        }
    }

    public void addArmorEffects(int armorDurabilityBonus) {
        if (armorDurabilityBonus >= 25) {
            specialEffects.add(Effect.POISON_IMMUNITY);
            specialEffects.add(Effect.BLEEDING_RESIST);
        } else if (armorDurabilityBonus >= 20) {
            specialEffects.add(Effect.BLEEDING_RESIST);
        } else if (armorDurabilityBonus >= 15) {
            specialEffects.add(Effect.POISON_IMMUNITY);
        }
    }

}
