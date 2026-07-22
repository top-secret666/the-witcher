package main.java.com.witcher.repository;

import main.java.com.witcher.model.armour.Armour;
import java.util.List;
import java.util.ArrayList;

public class ArmourRepository {
    private final List<Armour> inventory = new ArrayList<>();

    public List<Armour> findAll() {
        return new ArrayList<>(inventory);
    }

    public int getSize() {
        return inventory.size();
    }

    public void add(Armour armor) {
        inventory.add(armor);
    }

    public List<Armour> getAllArmor() {
        return findAll();
    }
}
