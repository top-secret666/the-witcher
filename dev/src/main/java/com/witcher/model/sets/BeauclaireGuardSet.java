package main.java.com.witcher.model.sets;

public class BeauclaireGuardSet extends NonSchoolSet {
    public BeauclaireGuardSet() {
        super("Комплект капитана стражи Боклера",
                new SetBonus("Доблесть Боклера", 0.2, 0.3, 0.15),
                38,
                "Боклер",
                "Церемониальные доспехи стражи Боклера",
                new RegionBonus("Боклер", "Уважение местных жителей", 0.25),
                100999,
                425.0);
        setArmorPieces(KitArmourPieces.of(getName(), 98, 15.0));
    }
}
