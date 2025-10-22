package main.java.com.witcher.ui;

import main.java.com.witcher.exception.InvalidPriceException;
import main.java.com.witcher.exception.InvalidPriceRangeException;
import main.java.com.witcher.exception.NoArmourFoundException;
import main.java.com.witcher.model.armour.Armour;
//import main.java.com.witcher.model.sets.ArmourSet;
//import main.java.com.witcher.model.sets.SchoolSet;
import main.java.com.witcher.model.sets.ArmourSet;
import main.java.com.witcher.repository.ArmourRepository;
import main.java.com.witcher.service.ArmorCalculationService;
import main.java.com.witcher.service.ArmorService;
import main.java.com.witcher.service.ArmorSortingService;
import main.java.com.witcher.service.SetService;
import main.java.com.witcher.validation.ErrorHandler;
import main.java.com.witcher.validation.InputValidator;

import java.util.*;

public class MenuHandler {
    private final ArmorService armorService;
    private final ArmorSortingService armorSortingService;
    private final ArmorCalculationService armorCalculationService;
    private final SetService setService;
    private final InputValidator validator;
    private final DialogManager dialogManager;
    private final Scanner scanner;
    private int geraltsWallet;
    private List<Armour> geraltsInventory;


    public MenuHandler(ArmorService armorService,
                       ArmorCalculationService armorCalculationService,
                       ArmorSortingService armorSortingService,
                       SetService setService,
                       InputValidator validator,
                       DialogManager dialogManager,
                       ArmourRepository armourRepository,
                       int initialWallet) {
        this.armorService = armorService;
        this.armorCalculationService = armorCalculationService;
        this.armorSortingService = armorSortingService;
        this.setService = setService;
        this.validator = validator;
        this.dialogManager = dialogManager;
        this.scanner = new Scanner(System.in);
//        this.armourRepository = armourRepository;
        this.geraltsWallet = initialWallet;
        this.geraltsInventory = new ArrayList<>();
    }

    public int getMenuChoice() {
        Integer result = ErrorHandler.handle(() -> {
            String input = scanner.nextLine();
            int choice = validator.validateIntegerInput(input);
            validator.validateMenuChoice(choice, 8);
            return choice;
        }, "(Неверный выбор меню)");

        return result != null ? result : -1;
    }
    public void handleMenuChoice(int choice) {
        switch (choice) {
            case 1 -> equipWitcher();
            case 2 -> handleInventory();
            case 3 -> viewSets();
            default -> dialogManager.showDialogLine("Герцог", "Что-что? Не расслышал...");
        }
    }

    public void equipWitcher() {
        boolean validPurchase = false;
        while (!validPurchase) {
            System.out.println("\nКошелек Геральта: " + geraltsWallet + " крон");
            dialogManager.showDialogLine("Герцог", "Сколько крон готовы потратить, Белый Волк?(min->10)(0-exit)");
            try {
                String input = scanner.nextLine();
                int targetPrice = validator.validatePriceWithExit(input);

                if (targetPrice == 0) {
                    return;
                }

                if (targetPrice > geraltsWallet) {
                    dialogManager.showDialogLine("Герцог", "У вас недостаточно крон в кошельке!");
                    continue;
                }

                validator.validatePrice(targetPrice);
                List<Armour> selectedArmor = armorService.equipWitcher(targetPrice);

                dialogManager.displayArmorSet(selectedArmor,
                        armorCalculationService.calculateTotalProtection(selectedArmor),
                        armorCalculationService.calculateTotalWeight(selectedArmor));

                System.out.println("\nЖелаете приобрести данный комплект? (y/n)");
                if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                    int totalPrice = armorCalculationService.calculateTotalPrice(selectedArmor);
                    geraltsWallet -= totalPrice;

                    geraltsInventory.addAll(selectedArmor);
                    System.out.println("\nКомплект успешно приобретен!");
                    System.out.println("Остаток в кошельке: " + geraltsWallet + " крон");
                }
                validPurchase = true;
            } catch (InvalidPriceException e) {
                dialogManager.showDialogLine("Герцог", e.getMessage());
            } catch (NoArmourFoundException e) {
                dialogManager.showDialogLine("Герцог", e.getMessage());
            } catch (Exception e) {
                dialogManager.showDialogLine("Герцог", "Извините, я не из этих мест еще плохо понимаю ваш язык...");
            }
        }
    }

    private void handleInventory() {
        while (true) {
            if (geraltsInventory.isEmpty()) {
                dialogManager.showDialogLine("Герцог", "Ваш инвентарь пуст...У вас пока нет купленной брони...(переход к экипировке)");
                equipWitcher();
                return;
            }

            System.out.println("\n╔══════════════════  Ваш текущий инвентарь  ════════════════╗");
            geraltsInventory.forEach(armor ->
                    System.out.printf("   %-50s   \n",
                            String.format("%s - Цена: %d, Вес: %.2f",
                                    armor.getName(), armor.getPrice(), armor.getWeight())));
            System.out.println("╚═══════════════════════════════════════════════════════════╝");


            dialogManager.displayInventoryMenu();
            int choice = getMenuChoice();

            switch (choice) {
                case 1 -> sortArmour();
                case 2 -> findArmorInPriceRange();
                case 3 -> equipArmor();
                case 0 -> {
                    return;
                }
                default -> dialogManager.showDialogLine("Герцог", "Что-что? Не расслышал...");
            }
        }
    }


    public void sortArmour() {
        System.out.println("""
    >>>Сортировать инвентарь по:
    1. Весу
    2. Цене
    <<<<<<<<<<<<<<<<<<
    """);

        int choice = getMenuChoice();
        List<Armour> sortedArmor = switch (choice) {
            case 1 -> {
                dialogManager.showDialogLine("Герцог", "Разложим по весу...");
                yield armorSortingService.sortByWeight(geraltsInventory);
            }
            case 2 -> {
                dialogManager.showDialogLine("Герцог", "По цене, значит...");
                yield armorSortingService.sortByPrice(geraltsInventory);
            }
            default -> {
                dialogManager.showDialogLine("Герцог", "Что-что? Не расслышал...");
                yield Collections.emptyList();
            }
        };

        if (!sortedArmor.isEmpty()) {
            System.out.println("\n=== Ваш инвентарь ===");
            dialogManager.displayArmorList(sortedArmor);
        }
    }

    public void findArmorInPriceRange() {
        try {
            dialogManager.showDialogLine("Герцог", "Какой диапазон цен вас интересует?");
            dialogManager.showDialogLine("Геральт", "Хмм...Я думаю, что готов ИСКАТЬ От:");
            int minPrice = validator.validateIntegerInput(scanner.nextLine());

            dialogManager.showDialogLine("Геральт", "До:");
            int maxPrice = validator.validateIntegerInput(scanner.nextLine());

            validator.validatePriceRange(minPrice, maxPrice);
            List<Armour> foundArmor = armorSortingService.findArmorInPriceRange(geraltsInventory, minPrice, maxPrice);

            System.out.println("\n=== Найдено в вашем инвентаре ===");
            dialogManager.displayArmorList(foundArmor);
        } catch (InvalidPriceRangeException e) {
            dialogManager.showDialogLine("Герцог", e.getMessage());
        } catch (Exception e) {
            dialogManager.showDialogLine("Герцог", "Ошибка при поиске брони");
        }
    }

    private void equipArmor() {
        if (geraltsInventory.isEmpty()) {
            dialogManager.showDialogLine("Герцог", "У вас пока нет купленной брони...(переход к экипировке)");
            equipWitcher();
            return;
        }

        // Group armor into sets (4 pieces each)
        List<List<Armour>> armorSets = new ArrayList<>();
        List<Armour> currentSet = new ArrayList<>();

        for (Armour armor : geraltsInventory) {
            currentSet.add(armor);
            if (currentSet.size() == 4) {
                armorSets.add(new ArrayList<>(currentSet));
                currentSet.clear();
            }
        }

        // Add remaining items if any
        if (!currentSet.isEmpty()) {
            armorSets.add(new ArrayList<>(currentSet));
        }

        // Display available sets
        System.out.println("\n=== Доступные комплекты ===");
        for (int i = 0; i < armorSets.size(); i++) {
            System.out.println("\n" + (i + 1) + ".");
            List<Armour> set = armorSets.get(i);
            dialogManager.displayArmorList(set);
            double setEffectiveness = armorCalculationService.calculateTotalEffectiveness(set);
            System.out.printf("Общая эффективность комплекта: %.2f\n", setEffectiveness);
        }

        dialogManager.showDialogLine("Герцог", "Какой комплект желаете надеть? (0 - отмена)");
        try {
            String input = scanner.nextLine();
            int choice = validator.validateIntegerInput(input);

            if (choice == 0) {
                return;
            }

            if (choice > 0 && choice <= armorSets.size()) {
                List<Armour> selectedSet = armorSets.get(choice - 1);
                double totalProtection = armorCalculationService.calculateTotalProtection(selectedSet);
                double totalWeight = armorCalculationService.calculateTotalWeight(selectedSet);
                double totalEffectiveness = armorCalculationService.calculateTotalEffectiveness(selectedSet);

                dialogManager.displayArmorSet(selectedSet, totalProtection, totalWeight);
                dialogManager.showDialogLine("Геральт", String.format("Хмм... Неплохо сидит. Общая эффективность: %.2f", totalEffectiveness));
            } else {
                dialogManager.showDialogLine("Герцог", "Такого комплекта нет в наличии.");
            }
        } catch (Exception e) {
            dialogManager.showDialogLine("Герцог", "Что-то пошло не так при выборе комплекта.");
        }
    }



    private void viewSets() {
        dialogManager.displaySetsList();
        dialogManager.showDialogLine("Герцог", "Желаете приобрести какой-нибудь комплект? (y/n)");
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            purchaseSet();
        }
    }

    private void purchaseSet() {
        dialogManager.displaySetPurchaseMenu();
        int choice = getMenuChoice();

        List<ArmourSet> availableSets = switch (choice) {
            case 1 -> new ArrayList<>(setService.getSchoolSets());
            case 2 -> new ArrayList<>(setService.getNonSchoolSets());
            default -> new ArrayList<>();
        };

        if (!availableSets.isEmpty()) {
            System.out.println("\nВыберите комплект для покупки (0 для отмены):");
            for (int i = 0; i < availableSets.size(); i++) {
                ArmourSet set = availableSets.get(i);
                System.out.printf("%d. %s - %d крон\n", i + 1, set.getName(), setService.calculateSetPrice(set));
            }

            try {
                String input = scanner.nextLine();
                int setChoice = validator.validateIntegerInput(input);

                if (setChoice == 0) return;

                if (setChoice > 0 && setChoice <= availableSets.size()) {
                    ArmourSet selectedSet = availableSets.get(setChoice - 1);
                    int setPrice = setService.calculateSetPrice(selectedSet);

                    if (setPrice <= geraltsWallet) {
                        geraltsWallet -= setPrice;
                        geraltsInventory.addAll(selectedSet.getArmorPieces());
                        System.out.println("В инвентарь добавлено " + selectedSet.getArmorPieces().size() + " предметов брони.");
                        dialogManager.showDialogLine("Герцог", "Отличный выбор! Комплект ваш.");

                        System.out.println("\nЖелаете перейти в инвентарь для примерки? (y/n)");
                        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
                            handleInventory();
                        }
                    } else {
                        dialogManager.showDialogLine("Герцог", "У вас недостаточно крон для этого комплекта.");
                    }
                }
            } catch (Exception e) {
                dialogManager.showDialogLine("Герцог", "Что-то пошло не так при выборе комплекта.");
            }
        }

    }
}
