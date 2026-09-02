package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class HenGaidthSet extends NonSchoolSet {
    public HenGaidthSet() {
        super("Комплект Хен Гайдт",
                new SetBonus("Древняя сила", 0.3, 0.2, 0.2),
                35,
                "Скеллиге",
                "Древний доспех скеллигских воинов",
                new RegionBonus("Скеллиге", "Повышенная стойкость к холоду", 0.3),
                500000,
                725.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(20054, 20045, 20031, 20037, 20041),
                calculateTotalPrice(),
                calculateTotalWeight(),
                20039,
                20026
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}
