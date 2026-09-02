package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class ThousandFlowersSet extends NonSchoolSet {
    public ThousandFlowersSet() {
        super("Комплект Тысячи Цветов",
                new SetBonus("Благословение природы", 0.2, 0.2, 0.3),
                36,
                "Туссент",
                "Мистический доспех друидов",
                new RegionBonus("Туссент", "Регенерация здоровья", 0.25),
                788000,
                925.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(20034, 20027, 20050, 20045, 20030),
                calculateTotalPrice(),
                calculateTotalWeight(),
                20034,
                20018
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}
