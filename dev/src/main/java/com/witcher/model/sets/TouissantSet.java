package main.java.com.witcher.model.sets;

public class TouissantSet extends NonSchoolSet {
    public TouissantSet() {
        super("Комплект туссентского рыцаря",
                new SetBonus("Рыцарская честь", 0.25, 0.25, 0.2),
                40,
                "Туссент",
                "Доспех туссентского рыцарства",
                new RegionBonus("Туссент", "Повышенная защита от вампиров", 0.35),
                996700,
                725.0);
        setArmorPieces(KitArmourPieces.of(getName(), 105, 17.0));
    }
}
