package main.java.com.witcher.service;
//РАЗДЕЛИТЬ НА КЛАССЫ!!!!!!
//СОРТИРОВКА БИТВА!!!!
import main.java.com.witcher.exception.ExcessBudgetException;
import main.java.com.witcher.exception.InvalidPriceException;
import main.java.com.witcher.exception.NoArmourFoundException;
import main.java.com.witcher.model.armour.*;
import main.java.com.witcher.repository.ArmourRepository;
import main.java.com.witcher.validation.InputValidator;

import java.util.*;
import java.util.stream.Collectors;

public class ArmorService {
    private final ArmourRepository armorRepository;
    private final ArmorCalculationService armorCalculationService;
    private final InputValidator validator;
    private static final int minArmorSetPrice = 1000;

    public ArmorService(ArmourRepository armorRepository, ArmorCalculationService armorCalculationService, InputValidator validator) {
        this.armorCalculationService = armorCalculationService;
        this.armorRepository = armorRepository;
        this.validator = validator;
    }

    public List<Armour> equipWitcher(int targetPrice) throws InvalidPriceException {
        validator.validatePrice(targetPrice);
        List<Armour> allSelectedArmor;

        List<Armour> currentSet = findArmorSet(targetPrice);
        if (currentSet.isEmpty()) {
            throw new NoArmourFoundException(targetPrice);
        }

        int setPrice = armorCalculationService.calculateTotalPrice(currentSet);
        allSelectedArmor = new ArrayList<>(currentSet);


        if (setPrice < targetPrice) {
            int remainingBudget = targetPrice - setPrice;
            try {
                validator.validateRemainingBudget(remainingBudget, minArmorSetPrice);
            } catch (ExcessBudgetException e) {
                System.out.println(e.getMessage());
                Scanner scanner = new Scanner(System.in);
                if (validator.validateYesNoInput(scanner).equals("y")) {
                    List<Armour> additionalSet = findArmorSetQuietly(remainingBudget, currentSet);
                    if (!additionalSet.isEmpty()) {
                        allSelectedArmor.addAll(additionalSet);
                    } else {
                        System.out.println("\nГерцог: На оставшуюся сумму не удалось подобрать полный комплект.");
                    }
                }
            }
        }

        return allSelectedArmor;
    }



    private List<Armour> findArmorSetQuietly(int targetPrice, List<Armour> excludeArmor) {
        try {
            return findArmorSetInternal(targetPrice, false, excludeArmor);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }



    private List<Armour> findArmorSet(int targetPrice) {
        return findArmorSetInternal(targetPrice, true, null);
    }

    private List<Armour> findArmorSetInternal(int targetPrice, boolean showWarnings, List<Armour> excludeArmor) {
        List<Armour> selectedArmor = new ArrayList<>();
        List<Armour> bestSet = new ArrayList<>();
        Map<Class<?>, List<Armour>> armorByType = groupArmorByBaseType();

        // Remove already used armor
        if (excludeArmor != null) {
            for (Armour armor : excludeArmor) {
                armorByType.values().forEach(list -> list.remove(armor));
            }
        }


        int[] closestPrice = {Integer.MAX_VALUE};
        int[] bestTotalPrice = {0};

        findClosestArmorSet(targetPrice, new ArrayList<>(armorByType.keySet()),
                armorByType, selectedArmor, bestSet, 0,
                closestPrice, bestTotalPrice);

        if (bestTotalPrice[0] > targetPrice) {
            if (showWarnings) {
                System.out.println("\n=== Предупреждение! ===");
                System.out.println("Минимальная стоимость комплекта превышает запрошенную цену.");
                System.out.println("Запрошено: " + targetPrice + " крон");
                System.out.println("Минимальная необходимая сумма: " + bestTotalPrice[0] + " крон");
                System.out.println("Не хватает: " + (bestTotalPrice[0] - targetPrice) + " крон");
            }
            throw new NoArmourFoundException("\u001B*Кашляет*...извините,но у меня аллергия к нищим...Может, добавите крон?\u001B");
        }
        return bestSet;
    }

    private void findClosestArmorSet(int targetPrice,
                                     List<Class<?>> armorTypes,
                                     Map<Class<?>, List<Armour>> armorByType,
                                     List<Armour> currentSet,
                                     List<Armour> bestSet,
                                     int currentTypeIndex,
                                     int[] closestPrice,
                                     int[] bestTotalPrice) {
        if (currentTypeIndex >= armorTypes.size()) {
            if (isValidArmorSet(currentSet)) {
                int totalPrice = armorCalculationService.calculateTotalPrice(currentSet);
                if (Math.abs(targetPrice - totalPrice) < Math.abs(targetPrice - closestPrice[0])) {
                    closestPrice[0] = totalPrice;
                    bestTotalPrice[0] = totalPrice;
                    bestSet.clear();
                    bestSet.addAll(currentSet);
                }
            }
            return;
        }

        Class<?> currentType = armorTypes.get(currentTypeIndex);
        List<Armour> availableArmor = armorByType.get(currentType).stream()
                .sorted(Comparator.comparing(Armour::getPrice))
                .toList();

        for (Armour armor : availableArmor) {
            currentSet.add(armor);
            findClosestArmorSet(targetPrice, armorTypes, armorByType,
                    currentSet, bestSet, currentTypeIndex + 1,
                    closestPrice, bestTotalPrice);
            currentSet.remove(armor);
        }
    }

    private Map<Class<?>, List<Armour>> groupArmorByBaseType() {
        return armorRepository.findAll().stream()
                .collect(Collectors.groupingBy(armor -> {
                    if (armor instanceof Chestpiece) return Chestpiece.class;
                    if (armor instanceof Trousers) return Trousers.class;
                    if (armor instanceof Gloves) return Gloves.class;
                    if (armor instanceof Boots) return Boots.class;
                    return armor.getClass();
                }));
    }

    private boolean isValidArmorSet(List<Armour> armorSet) {
        boolean hasChestpiece = armorSet.stream().anyMatch(a -> a instanceof Chestpiece);
        boolean hasTrousers = armorSet.stream().anyMatch(a -> a instanceof Trousers);
        boolean hasGloves = armorSet.stream().anyMatch(a -> a instanceof Gloves);
        boolean hasBoots = armorSet.stream().anyMatch(a -> a instanceof Boots);

        return hasChestpiece && hasTrousers && hasGloves && hasBoots;
    }


}
