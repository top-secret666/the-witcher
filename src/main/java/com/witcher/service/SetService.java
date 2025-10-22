package main.java.com.witcher.service;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.sets.ArmourSet;
import main.java.com.witcher.model.sets.NonSchoolSet;
import main.java.com.witcher.model.sets.SchoolSet;
import main.java.com.witcher.repository.SetRepository;
//import main.java.com.witcher.validation.InputValidator;


import java.util.*;
import java.util.stream.Collectors;

public class SetService {
    private final SetRepository setRepository;
//    private final InputValidator validator;
//    private final ArmorCalculationService armorCalculationService;
//    private final ArmorManagementService armorManagementService;

    public SetService(SetRepository setRepository) {
        this.setRepository = setRepository;
//        this.armorCalculationService = armorCalculationService;
//        this.validator = validator;
//        this.armorManagementService = armorManagementService;
    }


//    public List<ArmourSet> getSortedSetsByProtection() {
//        return setRepository.getAllSets().stream()
//                .sorted(Comparator.comparingDouble(this::calculateSetProtection).reversed())
//                .collect(Collectors.toList());
//    }

//    public double calculateSetProtection(ArmourSet set) {
//        double baseProtection = set.getArmorPieces().stream()
//                .mapToDouble(Armour::calculateProtection)
//                .sum();
//        return set.isComplete() ? baseProtection * (1 + set.getSetBonus().protectionBonus()) : baseProtection;
//    }

    public int calculateSetPrice(ArmourSet set) {
        return set.getArmorPieces().stream()
                .mapToInt(Armour::getPrice)
                .sum();
    }

//    public Map<SchoolSet.SchoolType, Double> calculateSchoolSetEfficiencies() {
//        return Arrays.stream(SchoolSet.SchoolType.values())
//                .collect(Collectors.toMap(
//                        Function.identity(),
//                        type -> {
//                            Optional<SchoolSet> schoolSet = findSchoolSetByType(type);
//                            return schoolSet.map(this::calculateSetEfficiency).orElse(0.0);
//                        }
//                ));
//    }

//    private Optional<SchoolSet> findSchoolSetByType(SchoolSet.SchoolType type) {
//        return setRepository.getAllSets().stream()
//                .filter(set -> set instanceof SchoolSet)
//                .map(set -> (SchoolSet) set)
//                .filter(set -> set.getSchoolType() == type)
//                .findFirst();
//    }


//    public void addSet(ArmourSet set) {
//        validator.validateSet(set);
//
//        boolean isCompatible = set.getArmorPieces().stream()
//                .allMatch(armor -> armorManagementService.isArmorCompatibleWithSet(armor, set));
//
//        if (isCompatible) {
//            setRepository.addSet(set);
//        }
//    }

//    public double calculateSetEfficiency(ArmourSet set) {
//        double protection = calculateSetProtection(set);
//        double weight = armorCalculationService.calculateTotalWeight(set.getArmorPieces());
//        return protection / weight;
//    }

    public List<SchoolSet> getSchoolSets() {
        return setRepository.getSchoolSets();
    }

    public List<NonSchoolSet> getNonSchoolSets() {
        return setRepository.getAllSets().stream()
                .filter(set -> set instanceof NonSchoolSet)
                .map(set -> (NonSchoolSet) set)
                .collect(Collectors.toList());
    }

}