package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.Boots;
import main.java.com.witcher.model.armour.ChestpieceStats;
import main.java.com.witcher.model.armour.Cuirass;
import main.java.com.witcher.model.armour.Gloves;
import main.java.com.witcher.model.armour.Pants;
import main.java.com.witcher.model.armour.TrousersStats;
import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;

import java.util.List;

/** Четыре слота комплекта (кираса / штаны / перчатки / сапоги) для лавки. */
final class KitArmourPieces {

    private KitArmourPieces() {
    }

    static List<Armour> of(String setDisplayName, int piecePrice, double pieceWeight) {
        String kit = setDisplayName;
        return List.of(
            new Cuirass(
                "Кираса: " + kit,
                ArmourType.HEAVY,
                ArmourCategory.SET_ITEM,
                new ChestpieceStats(22, 18, 20, 16, 24),
                piecePrice,
                pieceWeight,
                14,
                12,
                24),
            new Pants(
                "Штаны: " + kit,
                ArmourType.HEAVY,
                ArmourCategory.SET_ITEM,
                new TrousersStats(16, 14, 12),
                Math.max(40, piecePrice - 20),
                pieceWeight * 0.7,
                12,
                14),
            new Gloves(
                "Перчатки: " + kit,
                ArmourType.MEDIUM,
                ArmourCategory.SET_ITEM,
                Math.max(35, piecePrice - 30),
                pieceWeight * 0.35,
                14,
                12,
                true),
            new Boots(
                "Сапоги: " + kit,
                ArmourType.MEDIUM,
                ArmourCategory.SET_ITEM,
                Math.max(35, piecePrice - 25),
                pieceWeight * 0.4,
                12,
                11,
                true)
        );
    }
}
