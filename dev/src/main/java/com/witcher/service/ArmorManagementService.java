package main.java.com.witcher.service;

import main.java.com.witcher.model.armour.Armour;
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

    public void addArmor(Armour armor) {
        validator.validateName(armor.getName());
        validator.validateWeight(armor.getWeight());
        armorRepository.add(armor);
    }
}
