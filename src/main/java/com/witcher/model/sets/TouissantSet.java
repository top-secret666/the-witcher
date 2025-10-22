package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class TouissantSet extends NonSchoolSet {
    public TouissantSet() {
        super("Туссентский комплект",
                new SetBonus("Рыцарская честь", 0.25, 0.25, 0.2),
                40,
                "Туссент",
                "Доспех туссентского рыцарства",
                new RegionBonus("Туссент", "Повышенная защита от вампиров", 0.35),
                996700,
                725.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(20048, 20038, 20020, 20031, 20053),
                calculateTotalPrice(),
                calculateTotalWeight(),
                20035,
                20010
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}
