package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armor;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.ArrayList;
import java.util.List;

public class BeauclaireGuardSet extends NonSchoolSet {
    public BeauclaireGuardSet() {
        super("Комплект стражи Боклера",
                new SetBonus("Доблесть Боклера", 0.2, 0.3, 0.15),
                38,
                "Боклер",
                "Церемониальные доспехи стражи Боклера",
                new RegionBonus("Боклер", "Уважение местных жителей", 0.25),
                100999,
                425.0);

        Armor fullSet = new Armor(
                getName() + " (полный комплект)",
                ArmourType.FULL_SET,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(20034, 20055, 20020, 20037, 20049),
                calculateTotalPrice(),
                calculateTotalWeight(),
                20049,
                20019) {
        };

        List<Armour> singlePiece = new ArrayList<>();
        singlePiece.add(fullSet);
        super.setArmorPieces(singlePiece);
    }
}

