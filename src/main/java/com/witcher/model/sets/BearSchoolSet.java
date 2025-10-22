package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class BearSchoolSet extends SchoolSet {
    public BearSchoolSet() {
        super("Комплект Школы Медведя",
                new SetBonus("Стойкость медведя", 0.35, 0.45, 0.2),
                30,
                SchoolType.BEAR,
                new SpecialAbility("Медвежья мощь", "Увеличивает сопротивление урону на 45%", 0.45),
                66000,
                125.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(10050, 10045, 10030, 10065, 10075),
                calculateTotalPrice(),
                calculateTotalWeight(),
                10052,
                10013
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}


