package main.java.com.witcher.service;

import main.java.com.witcher.model.armour.*;


import java.util.List;

public class ArmorCalculationService {

    public int calculateTotalPrice(List<Armour> armorSet) {
        return armorSet.stream()
                .mapToInt(armor -> armor.isPaired() ? armor.getPrice() * 2 : armor.getPrice())
                .sum();
    }

    public double calculateTotalProtection(List<Armour> armorSet) {
        return armorSet.stream()
                .mapToDouble(Armour::calculateProtection)
                .sum();
    }

    public double calculateTotalWeight(List<Armour> armorSet) {
        return armorSet.stream()
                .mapToDouble(armor -> armor.isPaired() ? armor.getWeight() * 2 : armor.getWeight())
                .sum();
    }

//    public double calculateArmorRating(Armour armor) {
//        double baseRating = armor.calculateProtection();
//        double bonusRating = 0;
//
//        // Добавляем рейтинг от бонусов в зависимости от типа брони
//        if (armor instanceof Chestpiece) {
//            Chestpiece chestpiece = (Chestpiece) armor;
//            bonusRating += chestpiece.getChestProtection() * 0.5;
//        } else if (armor instanceof Gloves) {
//            Gloves gloves = (Gloves) armor;
//            bonusRating += gloves.getDexterityBonus() * 0.3;
//        } else if (armor instanceof Boots) {
//            Boots boots = (Boots) armor;
//            bonusRating += boots.getSpeedBonus() * 0.3;
//        } else if (armor instanceof Trousers) {
//            Trousers trousers = (Trousers) armor;
//            bonusRating += trousers.getMovementBonus() * 0.4;
//        }
//
//        return baseRating + bonusRating;
//    }

    public double calculateTotalEffectiveness(List<Armour> armorSet) {
        return armorSet.stream()
                .mapToDouble(armor -> {
                    if (armor instanceof Brigandine brigandine) {
                        return brigandine.calculateEffectiveness();
                    }
                    if (armor instanceof Cuirass cuirass) {
                        return cuirass.getBalanceBonus() * 1.5 + cuirass.getCounterAttackChance() * 2.0;
                    }
                    if (armor instanceof Armor basicArmor) {
                        return basicArmor.getStrengthRequirement() * 1.2 + basicArmor.getArmorDurabilityBonus() * 1.8;
                    }
                    if (armor instanceof Breastplate breastplate) {
                        return breastplate.getMagicResistance() * 2.0 + breastplate.getStaminaBonus() * 1.5;
                    }
                    if (armor instanceof Gloves gloves) {
                        return gloves.getDexterityBonus() * 2.0 + gloves.getGripStrength() * 1.5;
                    }
                    if (armor instanceof Boots boots) {
                        return boots.getSpeedBonus() * 2.0 + boots.getBalanceBonus() * 1.5;
                    }
                    if (armor instanceof Breeches breeches) {
                        return breeches.getMovementBonus() * 1.8 + breeches.getAgilityBonus() * 1.7;
                    }
                    if (armor instanceof Pants pants) {
                        return pants.getMovementBonus() * 1.5 + pants.getDurabilityBonus() * 1.6;
                    }
                    return 0.0;
                })
                .sum();
    }




}
