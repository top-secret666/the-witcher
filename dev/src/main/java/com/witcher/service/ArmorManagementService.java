package com.witcher.service;

import com.witcher.model.armour.Armour;
import com.witcher.repository.ArmourRepository;
import com.witcher.validation.InputValidator;

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
