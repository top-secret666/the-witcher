package main.java.com.witcher.model.sets;

public class TemerianKitSet extends NonSchoolSet {
    public TemerianKitSet() {
        super("Темерский комплект",
                new SetBonus("Верность Темерии", 0.22, 0.22, 0.18),
                36,
                "Темерия",
                "Доспех темеранской гвардии",
                new RegionBonus("Темерия", "Стойкость в строевом бою", 0.28),
                185000,
                480.0);
        setArmorPieces(KitArmourPieces.of(getName(), 95, 14.0));
    }
}
