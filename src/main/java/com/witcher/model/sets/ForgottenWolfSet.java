package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class ForgottenWolfSet extends NonSchoolSet {
    public ForgottenWolfSet() {
        super("Комплект Забытого Волка",
                new SetBonus("Древняя мудрость", 0.3, 0.3, 0.3),
                45,
                "Каэр Морхен",
                "Легендарный доспех первых ведьмаков",
                new RegionBonus("Каэр Морхен", "Усиление ведьмачьих чувств", 0.4),
                366000,
                365.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(20038, 20055, 20028, 20045, 20051),
                calculateTotalPrice(),
                calculateTotalWeight(),
                20032,
                20023
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}

