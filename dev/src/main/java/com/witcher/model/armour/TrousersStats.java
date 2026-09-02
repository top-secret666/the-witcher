package main.java.com.witcher.model.armour;

import java.util.ArrayList;
import java.util.List;

public class TrousersStats {
    private final List<CombatEffect> specialCombatEffect;

    public enum CombatEffect {
        FIRE_RESISTANCE,    // сопротивление огню
        STEALTH_BOOST,      // улучшение скрытности
        DODGE_BOOST,        // увеличение уклонения
        ACROBATICS_MASTER,  // мастерство акробатики
        SPRINT_BOOST        // ускорение спринта
    }

    public TrousersStats(int durabilityBonus, int agilityBonus, int movementBonus) {
        this.specialCombatEffect = new ArrayList<>();
        calculateAndAddEffects(durabilityBonus, agilityBonus, movementBonus);
    }

    private void calculateAndAddEffects(int durabilityBonus, int agilityBonus, int movementBonus) {
        double totalEffectiveness = agilityBonus * 1.5 + movementBonus * 1.2 + durabilityBonus * 0.8;

        if (totalEffectiveness >= 40) {
            specialCombatEffect.add(CombatEffect.DODGE_BOOST);
            specialCombatEffect.add(CombatEffect.ACROBATICS_MASTER);
        } else if (totalEffectiveness >= 30) {
            specialCombatEffect.add(CombatEffect.STEALTH_BOOST);
            specialCombatEffect.add(CombatEffect.SPRINT_BOOST);
        } else if (totalEffectiveness >= 20) {
            specialCombatEffect.add(CombatEffect.FIRE_RESISTANCE);
        }
    }

    public void addPantsEffects(int durabilityBonus) {
        if (durabilityBonus >= 20) {
            specialCombatEffect.add(CombatEffect.FIRE_RESISTANCE);
            specialCombatEffect.add(CombatEffect.SPRINT_BOOST);
        } else if (durabilityBonus >= 15) {
            specialCombatEffect.add(CombatEffect.FIRE_RESISTANCE);
        }
    }

    public void addTrousersEffects(int movementBonus) {
        if (movementBonus >= 25) {
            specialCombatEffect.add(CombatEffect.SPRINT_BOOST);
            specialCombatEffect.add(CombatEffect.DODGE_BOOST);
        } else if (movementBonus >= 20) {
            specialCombatEffect.add(CombatEffect.SPRINT_BOOST);
        } else if (movementBonus >= 15) {
            specialCombatEffect.add(CombatEffect.DODGE_BOOST);
        }
    }

    public void addBreechesEffects(int agilityBonus) {
        if (agilityBonus >= 25) {
            specialCombatEffect.add(CombatEffect.DODGE_BOOST);
            specialCombatEffect.add(CombatEffect.ACROBATICS_MASTER);
        } else if (agilityBonus >= 20) {
            specialCombatEffect.add(CombatEffect.STEALTH_BOOST);
        }
    }

    public List<CombatEffect> getSpecialCombatEffect() {
        return new ArrayList<>(specialCombatEffect);
    }
}
