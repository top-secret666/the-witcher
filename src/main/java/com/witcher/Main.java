package main.java.com.witcher;

import main.java.com.witcher.model.armour.*;
import main.java.com.witcher.repository.ArmourRepository;
import main.java.com.witcher.repository.SetRepository;
import main.java.com.witcher.service.*;
import main.java.com.witcher.ui.ConsoleUI;
import main.java.com.witcher.validation.InputValidator;
import main.java.com.witcher.ui.util.AnimationUtils;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

//ДОБАВИТЬ ДОПОЛНИТЕЛЬНЫЙ ЭНАМ ДЛЯ КОМПЛЕКТОВ ТИПО УЛУЧШЕННЫЙ МАСТЕРСКИЙ И Т.Д.
public class Main {
    public static void main(String[] args) {
        AnimationUtils.showProgressBar();
       AnimationUtils.animateWitcherLogoFade();

        try {
            // Step 1: Initialize system
            System.out.println("=== Инициализация системы ===");
            // Add wallet initialization
            AnimationUtils.displayCharacterSetup();
            System.out.println("\nКошелек[?]:");
            Scanner scanner = new Scanner(System.in);
            int geraltsWallet = scanner.nextInt();

            System.out.println("\nУровень[?]:");
            int geraltsLevel = scanner.nextInt();

            System.out.println("Кошелек Геральта: " + geraltsWallet + " крон");
            System.out.println("Уровень Геральта: " + geraltsLevel + "\n");

            ArmourRepository armourRepository = initializeRepository();
            SetRepository setRepository = new SetRepository();
            InputValidator inputValidator = new InputValidator();

            // Step 2: Initialize services
            System.out.println("Инициализация сервисов...");
            ArmorCalculationService armorCalculationService = new ArmorCalculationService();
            ArmorManagementService armorManagementService = new ArmorManagementService(armourRepository, armorCalculationService, inputValidator);
            ArmorService armorService = new ArmorService(armourRepository, armorCalculationService, inputValidator);
            ArmorSortingService armorSortingService = new ArmorSortingService(inputValidator);
            SetService setService = new SetService(setRepository);
            ArmorGenerationService generationService = new ArmorGenerationService(armorManagementService);
            System.out.println("Сервисы успешно инициализированы");


            // Step 3: Generate initial inventory
            System.out.println("\n=== Генерация товаров ===");
            generateInitialInventory(generationService, armourRepository);

            // Step 4: Start UI
            System.out.println("\n=== Запуск пользовательского интерфейса ===");
            ConsoleUI consoleUI = new ConsoleUI(armorService, armorCalculationService, armorSortingService, setService, inputValidator, armourRepository, geraltsWallet);
            consoleUI.start();

        } catch (Exception e) {
            System.out.println("\nКритическая ошибка: " + e.getMessage());
            System.out.println("Программа будет завершена");
            System.exit(1);
        }
    }

    private static ArmourRepository initializeRepository() {
        System.out.println("Инициализация хранилища...");
        ArmourRepository repository = new ArmourRepository();
        System.out.println("Хранилище успешно создано");
        return repository;
    }

//    ИЗБАВИТЬСЯ ОТ ДУБЛИРОВАНИЯ Эффекты: [BLEEDING_RESIST, BLEEDING_RESIST, MAGIC_RESISTANCE] КРАСИВЫЙ ВЫВОД?
//    ПЕРЕПИСАТЬ БОНУСЫ И СООТВЕТСТВЕННО ПОКАЗАТЕЛИ РАНДОМА
private static void generateInitialInventory(ArmorGenerationService generationService, ArmourRepository repository) {
    System.out.println("Начало генерации предметов...");
    generationService.generateRandomInventory();
    int itemsGenerated = repository.getSize();
    System.out.println("Генерация завершена успешно!");
    System.out.println("Создано предметов: " + itemsGenerated);

    System.out.println("\nСгенерированные предметы:");
    repository.getAllArmor().forEach(armor -> {
        String bonusStats = "";
        String effectsInfo = "";
        String typeInfo = String.format("| Тип: %s | Категория: %s", armor.getType(), armor.getCategory());

        if (armor instanceof Brigandine brigandine) {
            bonusStats = String.format("| Гибкость: %d | Скрытность: %d",
                    brigandine.getFlexibilityBonus(), brigandine.getStealthBonus());
            effectsInfo = "Эффекты: " + brigandine.getChestpieceStats().getSpecialEffects();
        } else if (armor instanceof Cuirass cuirass) {
            bonusStats = String.format("| Баланс: %d | Шанс контратаки: %d | Защита груди: %d",
                    cuirass.getBalanceBonus(), cuirass.getCounterAttackChance(), cuirass.getChestProtection());
            effectsInfo = "Эффекты: " + cuirass.getChestpieceStats().getSpecialEffects();
        } else if (armor instanceof Armor armorPiece) {
            bonusStats = String.format("| Требуемая сила: %d | Бонус прочности: %d | Защита груди: %d",
                    armorPiece.getStrengthRequirement(), armorPiece.getArmorDurabilityBonus(), armorPiece.getChestProtection());
            effectsInfo = "Эффекты: " + armorPiece.getChestpieceStats().getSpecialEffects();
        } else if (armor instanceof Breastplate breastplate) {
            bonusStats = String.format("| Сопр.магии: %d | Бонус выносливости: %d | Защита груди: %d",
                    breastplate.getMagicResistance(), breastplate.getStaminaBonus(), breastplate.getChestProtection());
            effectsInfo = "Эффекты: " + breastplate.getChestpieceStats().getSpecialEffects();
        } else if (armor instanceof Gloves gloves) {
            bonusStats = String.format("| Ловкость: %d | Сила хвата: %d | %s",
                    gloves.getDexterityBonus(),
                    gloves.getGripStrength(),
                    gloves.hasReinforcedKnuckles() ? "Усиленные костяшки" : "Обычные костяшки");
        } else if (armor instanceof Boots boots) {
            bonusStats = String.format("| Скорость: %d | Баланс: %d",
                    boots.getSpeedBonus(), boots.getBalanceBonus());
        } else if (armor instanceof Breeches breeches) {
            bonusStats = String.format("| Подвижность: %d | Бонус ловкости: %d",
                    breeches.getMovementBonus(), breeches.getAgilityBonus());
            effectsInfo = "Боевые эффекты: " + breeches.getTrousersStats().getSpecialCombatEffect();
        } else if (armor instanceof Pants pants) {
            bonusStats = String.format("| Подвижность: %d | Прочность: %d",
                    pants.getMovementBonus(), pants.getDurabilityBonus());
            effectsInfo = "Боевые эффекты: " + pants.getTrousersStats().getSpecialCombatEffect();
        }

        System.out.printf("Название: %-20s | Цена: %-6d | Вес: %.2f %s %s | %s%n",
                new String(armor.getName().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8),
                armor.getPrice(),
                armor.getWeight(),
                typeInfo,
                bonusStats,
                effectsInfo
        );
    });
}



}