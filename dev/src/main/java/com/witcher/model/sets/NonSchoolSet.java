package main.java.com.witcher.model.sets;


import main.java.com.witcher.model.armour.Armour;

import java.util.List;

public abstract class NonSchoolSet extends ArmourSet {
    private final String origin;
    private final String lore;
    private final RegionBonus regionBonus;

    public record RegionBonus(
            String region,
            String effect,
            double bonusValue
    ) {}



    protected NonSchoolSet(String name, SetBonus setBonus, int requiredLevel,
                           String origin, String lore, RegionBonus regionBonus,
                           int basePrice, double baseWeight) {
        super(name, setBonus, requiredLevel, Rarity.LEGENDARY, basePrice, baseWeight);
        this.origin = origin;
        this.lore = lore;
        this.regionBonus = regionBonus;
    }


    public String getOrigin() {
        return origin;
    }

    public String getLore() {
        return lore;
    }

    public RegionBonus getRegionBonus() {
        return regionBonus;
    }
    protected void setArmorPieces(List<Armour> pieces) {
        super.setArmorPieces(pieces);
    }
}
