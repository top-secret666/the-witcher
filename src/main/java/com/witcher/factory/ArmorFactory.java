package main.java.com.witcher.factory;

import main.java.com.witcher.model.armour.*;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Random;
import java.util.Set;


public class ArmorFactory {
    private static final Random random = new Random();
    private static final Set<String> usedNames = new HashSet<>();
    private static final Properties armorNames = new Properties();
    private static final int MAX_NAME_VARIANTS = 20;
    private static final String RARE_PREFIX = "Редкий ";

    static {
        try (InputStream in = openArmorNamesStream()) {
            if (in == null) {
                throw new FileNotFoundException("armor_names.properties not found");
            }
            armorNames.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load armor names", e);
        }
    }

    private static InputStream openArmorNamesStream() throws IOException {
        String assetsRoot = System.getProperty("witcher.assets", "").replace('\\', '/').trim();
        if (assetsRoot.endsWith("/")) {
            assetsRoot = assetsRoot.substring(0, assetsRoot.length() - 1);
        }

        String[] candidates = {
            "armor_names.properties",
            "../armor_names.properties",
            "../../armor_names.properties",
            "src/main/resources/armor_names.properties",
        };

        if (!assetsRoot.isEmpty()) {
            File parent = new File(assetsRoot).getParentFile();
            if (parent != null) {
                File inResources = new File(parent, "armor_names.properties");
                if (inResources.isFile()) {
                    return new FileInputStream(inResources);
                }
            }
            File inAssets = new File(assetsRoot, "armor_names.properties");
            if (inAssets.isFile()) {
                return new FileInputStream(inAssets);
            }
        }

        for (String path : candidates) {
            File file = new File(path);
            if (file.isFile()) {
                return new FileInputStream(file);
            }
        }

        InputStream classpath = ArmorFactory.class.getResourceAsStream("/armor_names.properties");
        if (classpath != null) {
            return classpath;
        }
        return null;
    }


    private static String[] getNames(String key) {
        return armorNames.getProperty(key).split(",");
    }

    private static String uniqueFullName(String propertyKey, String suffix) {
        return allocateName(getNames(propertyKey), suffix, true);
    }

    private static String uniquePrefixedName(String prefix, String propertyKey) {
        return allocateName(getNames(propertyKey), prefix, false);
    }

    private static String allocateName(String[] nameArray, String affix, boolean suffix) {
        if (nameArray.length == 0) {
            throw new IllegalStateException("No armor names configured");
        }
        ArrayList<String> pool = new ArrayList<>(Arrays.asList(nameArray));
        Collections.shuffle(pool, random);
        for (String part : pool) {
            String full = suffix ? part + affix : affix + part;
            if (usedNames.add(full)) {
                return full;
            }
        }
        for (String part : nameArray) {
            for (int n = 2; n < MAX_NAME_VARIANTS; n++) {
                String variant = suffix ? part + " " + n + affix : affix + part + " " + n;
                if (usedNames.add(variant)) {
                    return variant;
                }
            }
        }
        String fallback = RARE_PREFIX + (usedNames.size() + 1)
            + (suffix ? affix : "");
        usedNames.add(fallback);
        return fallback;
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
        String name = uniqueFullName("brigandine.names", " Бригантина");
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
        String name = uniqueFullName("cuirass.names", " Кирасса");
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
        String name = uniquePrefixedName("Доспех ", "armor.names");
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
        String name = uniquePrefixedName("Нагрудник ", "breastplate.names");
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
        String name = uniqueFullName("gloves.names", " Перчатки");
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
        String name = uniqueFullName("boots.names", " Сапоги");
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
        String name = uniqueFullName("breeches.names", " Бриджи");
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
        String name = uniqueFullName("pants.names", " Брюки");
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
