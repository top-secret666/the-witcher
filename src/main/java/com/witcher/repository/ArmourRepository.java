package main.java.com.witcher.repository;

import main.java.com.witcher.model.armour.Armour;
import java.util.List;
import java.util.ArrayList;

public class ArmourRepository {
    private final List<Armour> inventory = new ArrayList<>();

//    public void save(Armour armour) {
//        inventory.add(armour);
//    }

    public List<Armour> findAll() {
        return new ArrayList<>(inventory);
    }

//    public Optional<Armour> findByName(String name) {
//        return inventory.stream()
//                .filter(armour -> armour.getName().equals(name))
//                .findFirst();
//    }
//
//    public void delete(Armour armour) {
//        inventory.remove(armour);
//    }
//
//    public void deleteByName(String name) {
//        inventory.removeIf(armour -> armour.getName().equals(name));
//    }
//
////    public void clear() {
////        inventory.clear();
////    }

    public int getSize() {
        return inventory.size();
    }

//    public boolean exists(String name) {
//        return inventory.stream()
//                .anyMatch(armour -> armour.getName().equals(name));
//    }
//
//    public List<Armour> getInventory() {
//        return new ArrayList<>(inventory);
//    }

    public void add(Armour armor) {
        inventory.add(armor);
    }

    public List<Armour> getAllArmor() {
        return findAll();
    }
}



