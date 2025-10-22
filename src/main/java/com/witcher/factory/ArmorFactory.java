package main.java.com.witcher.factory;

import main.java.com.witcher.model.armour.*;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;


public class ArmorFactory {
    private static final Random random = new Random();
    private static final Set<String> usedNames = new HashSet<>();
    private static final Properties armorNames = new Properties();

    static {
        try {
            File file = new File("src/main/resources/armor_names.properties");
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
                armorNames.load(reader);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load armor names", e);
        }
    }


    private static String[] getNames(String key) {
        return armorNames.getProperty(key).split(",");
    }

    private static String getUniqueName(String propertyKey) {
        String[] nameArray = getNames(propertyKey);
        return nameArray[random.nextInt(nameArray.length)];
    }

    public static void clearUsedNames() {
        usedNames.clear();
    }

    private static int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    private static double randomDouble(double min, double max) {
        return min + (max - min) * random.nextDouble();
    }

    public static Brigandine createRandomBrigandine() {
        String name = getUniqueName("brigandine.names") + " Бригантина";
        ArmourType type = ArmourType.values()[random.nextInt(ArmourType.values().length)];
        ArmourCategory category = ArmourCategory.values()[random.nextInt(ArmourCategory.values().length)];
        int basePrice = randomInt(100, 500);
        double weight = randomDouble(5.0, 15.0);
        int flexibilityBonus = randomInt(1, 20);
        int stealthBonus = randomInt(1, 10);

        ChestpieceStats stats = new ChestpieceStats(0, 0, flexibilityBonus, stealthBonus, 0);

        if (!stats.getSpecialEffects().isEmpty()) {
            basePrice = (int)(basePrice * (1 + stats.getSpecialEffects().size() * 0.5));
        }

        return new Brigandine(name, type, category, stats, basePrice, weight, flexibilityBonus, stealthBonus);
    }


    public static Cuirass createRandomCuirass() {
        String name = getUniqueName("cuirass.names") + " Кирасса";
        ArmourType type = ArmourType.values()[random.nextInt(ArmourType.values().length)];
        ArmourCategory category = ArmourCategory.values()[random.nextInt(ArmourCategory.values().length)];
        int basePrice = randomInt(150, 1200);
        double weight = randomDouble(6.0, 18.0);
        int balanceBonus = randomInt(1, 20);
        int counterAttackChance = randomInt(5, 25);
        int chestProtection = randomInt(10, 25);

        ChestpieceStats stats = new ChestpieceStats(0, counterAttackChance, 0, 0, chestProtection);

        if (!stats.getSpecialEffects().isEmpty()) {
            basePrice = (int)(basePrice * (1 + stats.getSpecialEffects().size() * 0.5));
        }
        if (counterAttackChance > 15) {
            basePrice = (int)(basePrice * 1.24);


        }

        return new Cuirass(name, type, category, stats, basePrice, weight,
                balanceBonus, counterAttackChance, chestProtection);
    }

    public static Armor createRandomArmor() {
        String name = "Доспех " + getUniqueName("armor.names");
        ArmourType type = ArmourType.values()[random.nextInt(ArmourType.values().length)];
        ArmourCategory category = ArmourCategory.values()[random.nextInt(ArmourCategory.values().length)];
        int basePrice = randomInt(250, 2000);
        double weight = randomDouble(8.0, 25.0);
        int strengthRequirement = randomInt(8, 20);
        int armorDurabilityBonus = randomInt(5, 25);
        int chestProtection = randomInt(20, 40);

        ChestpieceStats stats = new ChestpieceStats(strengthRequirement, 0, armorDurabilityBonus, 0, chestProtection);

        if (!stats.getSpecialEffects().isEmpty()) {
            basePrice = (int)(basePrice * (1 + stats.getSpecialEffects().size() * 0.7));
        }
        if (chestProtection > 30) {
            basePrice = (int)(basePrice * 1.3);
        }

        return new Armor(name, type, category, stats, basePrice, weight, strengthRequirement, armorDurabilityBonus);
    }


    public static Breastplate createRandomBreastplate() {
        String name = "Нагрудник " + getUniqueName("breastplate.names");
        ArmourType type = ArmourType.values()[random.nextInt(ArmourType.values().length)];
        ArmourCategory category = ArmourCategory.values()[random.nextInt(ArmourCategory.values().length)];
        int basePrice = randomInt(200, 800);
        double weight = randomDouble(7.0, 20.0);
        int magicResistance = randomInt(5, 20);
        int staminaBonus = randomInt(5, 25);
        int chestProtection = randomInt(15, 35);

        ChestpieceStats stats = new ChestpieceStats(magicResistance, 0, 0, staminaBonus, chestProtection);

        // Increase price based on special effects
        if (!stats.getSpecialEffects().isEmpty()) {
            basePrice = (int)(basePrice * (1 + stats.getSpecialEffects().size() * 0.57));
        }

        return new Breastplate(name, type, category, stats, basePrice, weight,
                magicResistance, staminaBonus, chestProtection);
    }


    public static Gloves createRandomGloves() {
        String name = getUniqueName("gloves.names") + " Перчатки";
        ArmourType type = ArmourType.values()[random.nextInt(ArmourType.values().length)];
        ArmourCategory category = ArmourCategory.values()[random.nextInt(ArmourCategory.values().length)];
        int basePrice = randomInt(50, 300);
        double weight = randomDouble(0.5, 3.0);
        int dexterityBonus = randomInt(1, 12);
        int gripStrength = randomInt(1, 10);
        boolean reinforcedKnuckles = random.nextBoolean();

        return new Gloves(name, type, category, basePrice, weight,
                dexterityBonus, gripStrength, reinforcedKnuckles);
    }

    public static Boots createRandomBoots() {
        String name = getUniqueName("boots.names") + " Сапоги";
        ArmourType type = ArmourType.values()[random.nextInt(ArmourType.values().length)];
        ArmourCategory category = ArmourCategory.values()[random.nextInt(ArmourCategory.values().length)];
        int basePrice = randomInt(80, 400);
        double weight = randomDouble(1.0, 4.0);
        int speedBonus = randomInt(1, 12);
        int balanceBonus = randomInt(1, 10);
        boolean reinforcedSoles = random.nextBoolean();

        return new Boots(name, type, category, basePrice, weight, speedBonus, balanceBonus, reinforcedSoles);
    }

    public static Breeches createRandomBreeches() {
        String name = getUniqueName("breeches.names") + " Бриджи";
        ArmourType type = ArmourType.values()[random.nextInt(ArmourType.values().length)];
        ArmourCategory category = ArmourCategory.values()[random.nextInt(ArmourCategory.values().length)];
        int basePrice = randomInt(80, 700);
        double weight = randomDouble(2.0, 6.0);
        int movementBonus = randomInt(1, 20);
        int agilityBonus = randomInt(1, 28);

        TrousersStats stats = new TrousersStats(0, agilityBonus, movementBonus);

        if (!stats.getSpecialCombatEffect().isEmpty()) {
            basePrice = (int)(basePrice * (1 + stats.getSpecialCombatEffect().size() * 0.6));
        }

        return new Breeches(name, type, category, stats, basePrice, weight, movementBonus, agilityBonus);
    }

    public static Pants createRandomPants() {
        String name = getUniqueName("pants.names") + " Брюки";
        ArmourType type = ArmourType.values()[random.nextInt(ArmourType.values().length)];
        ArmourCategory category = ArmourCategory.values()[random.nextInt(ArmourCategory.values().length)];
        int basePrice = randomInt(80, 400);
        double weight = randomDouble(1.0, 5.0);
        int movementBonus = randomInt(1, 12);
        int durabilityBonus = randomInt(1, 10);

        TrousersStats stats = new TrousersStats(durabilityBonus, 0, movementBonus);

        if (!stats.getSpecialCombatEffect().isEmpty()) {
            basePrice = (int)(basePrice * (1 + stats.getSpecialCombatEffect().size() * 0.57));
        }

        return new Pants(name, type, category, stats, basePrice, weight, movementBonus, durabilityBonus);
    }
}
