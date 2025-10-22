package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class WolfSchoolSet extends SchoolSet {
    public WolfSchoolSet() {
        super("Комплект Школы Волка",
                new SetBonus("Усиление знаков", 0.2, 0.3, 0.15),
                30,
                SchoolType.WOLF,
                new SpecialAbility("Волчье чутье",
                        "Увеличивает урон по отравленным врагам",
                        0.25),
                89000,
                325.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(10023, 10059, 10041, 10049, 10032),
                calculateTotalPrice(),
                calculateTotalWeight(),
                10056,
                10013
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}
