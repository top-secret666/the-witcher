package main.java.com.witcher.repository;

import main.java.com.witcher.model.sets.*;
import java.util.List;
import java.util.ArrayList;

public class SetRepository {
    private final List<ArmourSet> sets;

    public SetRepository() {
        this.sets = initializeSets();
    }

    private List<ArmourSet> initializeSets() {
        List<ArmourSet> initialSets = new ArrayList<>();
        initialSets.add(new WolfSchoolSet());
        initialSets.add(new CatSchoolSet());
        initialSets.add(new BearSchoolSet());
        initialSets.add(new GriffinSchoolSet());
        initialSets.add(new ManticoreSchoolSet());
        initialSets.add(new BeauclaireGuardSet());
        initialSets.add(new ThousandFlowersSet());
        initialSets.add(new WhiteTigerSet());
        initialSets.add(new HenGaidthSet());
        initialSets.add(new ForgottenWolfSet());
        return initialSets;
    }

    public List<ArmourSet> getAllSets() {
        return new ArrayList<>(sets);
    }

    public List<SchoolSet> getSchoolSets() {
        List<SchoolSet> schoolSets = new ArrayList<>();
        for (ArmourSet set : sets) {
            if (set instanceof SchoolSet) {
                schoolSets.add((SchoolSet) set);
            }
        }
        return schoolSets;
    }

    public void addSet(ArmourSet set) {
        sets.add(set);
    }

}
