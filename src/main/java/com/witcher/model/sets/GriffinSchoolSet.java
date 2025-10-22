package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class GriffinSchoolSet extends SchoolSet {
    public GriffinSchoolSet() {
        super("Комплект Школы Грифона",
                new SetBonus("Мощь знаков", 0.15, 0.25, 0.2),
                30,
                SchoolType.GRIFFIN,
                new SpecialAbility("Магическая аура",
                        "Усиливает все знаки на 25%",
                        0.25),
                80000,
                225.0);
        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(10040, 10023, 10038, 10028, 10057),
                calculateTotalPrice(),
                calculateTotalWeight(),
                10052,
                10035
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}

