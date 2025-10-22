package main.java.com.witcher.validation;

import main.java.com.witcher.exception.ExcessBudgetException;
import main.java.com.witcher.exception.InvalidPriceException;
import main.java.com.witcher.exception.InvalidPriceRangeException;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.sets.ArmourSet;

import java.util.List;
import java.util.Objects;
import java.util.Scanner;

public class InputValidator {
    public static final int MIN_PRICE = 10;
    private static final int MAX_PRICE = 1000000;
    private List<Armour> currentSet;

    public void validatePrice(int price) throws InvalidPriceException {
        if (price < MIN_PRICE || price > MAX_PRICE) {
            throw new InvalidPriceException(price);
        }
    }

    public void validatePriceRange(int minPrice, int maxPrice) throws InvalidPriceRangeException {
        if (minPrice < MIN_PRICE || maxPrice > MAX_PRICE || minPrice >= maxPrice) {
            throw new InvalidPriceRangeException(minPrice, maxPrice);
        }
    }

    public void validateWeight(double weight) {
        if (weight <= 0 || weight > 100) {
            throw new IllegalArgumentException(
                    String.format("Недопустимый вес: %.2f. Вес должен быть больше 0 и меньше 100.", weight)
            );
        }
    }

    public void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("...");
        }
    }


    public void validateSet(ArmourSet set) {
        Objects.requireNonNull(set, "Комплект брони не может быть пустым");
        Objects.requireNonNull(set.getArmorPieces(), "Список элементов брони не может быть пустым");

        if (set.getArmorPieces().isEmpty()) {
            throw new IllegalArgumentException("Комплект брони должен содержать хотя бы один элемент");
        }

        set.getArmorPieces().forEach(piece ->
                Objects.requireNonNull(piece, "Каждый элемент брони должен быть определен")
        );
    }

    public void validateMenuChoice(int choice, int maxChoice) {
        if (choice < 0 || choice > maxChoice) {
            throw new IllegalArgumentException("Выберите существующий пункт меню");
        }
    }

    public int validateIntegerInput(String input) {
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Введите целое число");
        }
    }

    public void validateRemainingBudget(int remainingBudget, int minPrice) throws ExcessBudgetException {
        if (remainingBudget >= minPrice) {
            throw new ExcessBudgetException(remainingBudget, currentSet);
        }
    }

    public String validateYesNoInput(Scanner scanner) {
        String input;
        do {
            input = scanner.nextLine().trim().toLowerCase();
            if (!input.equals("y") && !input.equals("n")) {
                System.out.println("Герцог: Прошу ответить 'y' или 'n'");
            }
        } while (!input.equals("y") && !input.equals("n"));
        return input;
    }

    public int validatePriceWithExit(String input) throws InvalidPriceException {
        int price = validateIntegerInput(input);

        if (price == 0) {
            return 0;
        }

        validatePrice(price);
        return price;
    }
}
