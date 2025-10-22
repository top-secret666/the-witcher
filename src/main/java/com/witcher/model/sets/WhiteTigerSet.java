package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class WhiteTigerSet extends NonSchoolSet {
    public WhiteTigerSet() {
        super("Комплект Белого Тигра",
                new SetBonus("Ярость тигра", 0.35, 0.15, 0.25),
                42,
                "Зерикания",
                "Легендарный доспех зериканских воинов",
                new RegionBonus("Зерикания", "Стойкость к жаре", 0.3),
                888000,
                665.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(20040, 20035, 20030, 20025, 20045),
                calculateTotalPrice(),
                calculateTotalWeight(),
                20042,
                20015
        );

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}
