package main.java.com.witcher.service;

import main.java.com.witcher.exception.InvalidPriceRangeException;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.validation.InputValidator;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArmorSortingService {
    private final InputValidator validator;
    public ArmorSortingService(InputValidator validator) {
        this.validator = validator;
    }

    public List<Armour> sortByWeight(List<Armour> inventory) {
        return inventory.stream()
                .sorted(Comparator.comparing(Armour::getWeight))
                .collect(Collectors.toList());
    }

    public List<Armour> sortByPrice(List<Armour> inventory) {
        return inventory.stream()
                .sorted(Comparator.comparing(Armour::getPrice))
                .collect(Collectors.toList());
    }


    public List<Armour> findArmorInPriceRange(List<Armour> inventory, int minPrice, int maxPrice) throws InvalidPriceRangeException {
        validator.validatePriceRange(minPrice, maxPrice);
        return inventory.stream()
                .filter(armor -> armor.getPrice() >= minPrice && armor.getPrice() <= maxPrice)
                .collect(Collectors.toList());
    }

}
