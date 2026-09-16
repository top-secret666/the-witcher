package com.witcher.service;

import com.witcher.model.armour.Armour;
import com.witcher.model.sets.ArmourSet;
import com.witcher.model.sets.NonSchoolSet;
import com.witcher.model.sets.SchoolSet;
import com.witcher.repository.SetRepository;

import java.util.*;

public class SetService {
    private final SetRepository setRepository;

    public SetService(SetRepository setRepository) {
        this.setRepository = setRepository;
    }

    public int calculateSetPrice(ArmourSet set) {
        return set.getArmorPieces().stream()
                .mapToInt(Armour::getPrice)
                .sum();
    }

    public List<SchoolSet> getSchoolSets() {
        return setRepository.getSchoolSets();
    }

    public List<NonSchoolSet> getNonSchoolSets() {
        return setRepository.getAllSets().stream()
                .filter(set -> set instanceof NonSchoolSet)
                .map(set -> (NonSchoolSet) set)
                .toList();
    }
}
