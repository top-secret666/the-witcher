package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class ManticoreSchoolSet extends SchoolSet {
    public ManticoreSchoolSet() {
        super("Комплект Школы Мантикоры",
                new SetBonus("Алхимическое мастерство", 0.2, 0.2, 0.2),
                30,
                SchoolType.MANTICORE,
                new SpecialAbility("Мастер ядов",
                        "Усиливает эффекты всех зелий на 30%",
                        0.3),
                40000,
                156.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(10027, 10031, 10046, 10052, 10029),
                calculateTotalPrice(),
                calculateTotalWeight(),
                10022,
                10045
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}

