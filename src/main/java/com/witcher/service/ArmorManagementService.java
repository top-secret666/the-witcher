package main.java.com.witcher.service;

import main.java.com.witcher.model.armour.Armour;
//import main.java.com.witcher.model.sets.ArmourSet;
//import main.java.com.witcher.model.sets.SchoolSet;
import main.java.com.witcher.repository.ArmourRepository;
import main.java.com.witcher.validation.InputValidator;

public class ArmorManagementService extends ArmorService {

    private final ArmourRepository armorRepository;
    private final InputValidator validator;

    public ArmorManagementService(ArmourRepository armorRepository, ArmorCalculationService calculationService, InputValidator validator) {
        super(armorRepository, calculationService, validator);
        this.armorRepository = armorRepository;
        this.validator = validator;
    }
//    private boolean canAddArmor(List<Armour> currentSet, Armour newArmor) {
//        return currentSet.stream()
//                .noneMatch(armor -> armor.getClass().equals(newArmor.getClass()));
//    }

    public void addArmor(Armour armor) {
        validator.validateName(armor.getName());
        validator.validateWeight(armor.getWeight());
        armorRepository.add(armor);
    }

//    public List<Armour> getArmorByType(ArmourType type) {
//        return armorInventory.stream()
//                .filter(armor -> armor.getType() == type)
//                .collect(Collectors.toList());
//    }
//
//    public List<Armour> getArmorByCategory(ArmourCategory category) {
//        return armorInventory.stream()
//                .filter(armor -> armor.getCategory() == category)
//                .collect(Collectors.toList());
//    }

//    public List<Armour> getCurrentInventory() {
//        return new ArrayList<>(armorInventory);
//    }

//    public boolean isArmorCompatibleWithSet(Armour armor, ArmourSet set) {
//        if (set instanceof SchoolSet schoolSet) {
//            return armor.getType() == schoolSet.getSchoolType().getPreferredArmorType();
//        }
//
//        return set.getArmorPieces().stream()
//                .anyMatch(setPiece -> setPiece.getCategory() == armor.getCategory());
//    }
}
