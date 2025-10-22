package main.java.com.witcher.service;

import main.java.com.witcher.factory.ArmorFactory;
import main.java.com.witcher.model.armour.Armour;

import java.util.Random;
import java.util.function.Supplier;

public class ArmorGenerationService {
    private final ArmorManagementService armorManagementService;

    public ArmorGenerationService(ArmorManagementService armorManagementService) {
        this.armorManagementService = armorManagementService;
    }

    private int getRandomCount(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min + 1) + min;
    }

    public void generateRandomInventory() {
        System.out.println("Начало генерации инвентаря...");

        int armorCount = getRandomCount(1, 5);
        int brigandineCount = getRandomCount(2, 6);
        int cuirassCount = getRandomCount(1, 4);
        int breastplateCount = getRandomCount(2, 5);
        int glovesCount = getRandomCount(3, 7);
        int bootsCount = getRandomCount(3, 7);
        int breechesCount = getRandomCount(2, 6);
        int pantsCount = getRandomCount(2, 6);

        System.out.println("Планируется создать предметов:");
        System.out.printf("Броня: %d, Бригандины: %d, Кирассы: %d, Нагрудники: %d%n",
                armorCount, brigandineCount, cuirassCount, breastplateCount);
        System.out.printf("Перчатки: %d, Ботинки: %d, Бриджи: %d, Штаны: %d%n",
                glovesCount, bootsCount, breechesCount, pantsCount);

        // Generate each type with completion tracking
        ArmorFactory.clearUsedNames();
        generateItems("Броня", armorCount, ArmorFactory::createRandomArmor);

        ArmorFactory.clearUsedNames();
        generateItems("Бригандина", brigandineCount, ArmorFactory::createRandomBrigandine);

        ArmorFactory.clearUsedNames();
        generateItems("Кирасса", cuirassCount, ArmorFactory::createRandomCuirass);

        ArmorFactory.clearUsedNames();
        generateItems("Нагрудник", breastplateCount, ArmorFactory::createRandomBreastplate);

        ArmorFactory.clearUsedNames();
        generateItems("Перчатки", glovesCount, ArmorFactory::createRandomGloves);

        ArmorFactory.clearUsedNames();
        generateItems("Ботинки", bootsCount, ArmorFactory::createRandomBoots);

        ArmorFactory.clearUsedNames();
        generateItems("Бриджи", breechesCount, ArmorFactory::createRandomBreeches);

        ArmorFactory.clearUsedNames();
        generateItems("Штаны", pantsCount, ArmorFactory::createRandomPants);


        System.out.println("Генерация инвентаря завершена");
    }

    private void generateItems(String type, int count, Supplier<Armour> factory) {
        System.out.printf("Создание %s (%d шт.)...%n", type, count);
        for (int i = 0; i < count; i++) {
            armorManagementService.addArmor(factory.get());
            System.out.printf("%s #%d создан%n", type, i + 1);
        }
    }
}