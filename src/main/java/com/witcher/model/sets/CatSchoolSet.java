package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class CatSchoolSet extends SchoolSet {
    public CatSchoolSet() {
        super("Комплект Школы Кота",
                new SetBonus("Скорость клинка", 0.3, 0.15, 0.25),
                30,
                SchoolType.CAT,
                new SpecialAbility("Кошачья ловкость",
                        "Увеличивает критический урон на 30%",
                        0.3),
                50000,
                115.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(10050, 10037, 10038, 10026, 10045),
                calculateTotalPrice(),
                calculateTotalWeight(),
                10032,
                10018
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);

    }
}
