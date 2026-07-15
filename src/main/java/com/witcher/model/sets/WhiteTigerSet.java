package main.java.com.witcher.model.sets;

public class WhiteTigerSet extends NonSchoolSet {
    public WhiteTigerSet() {
        super("Комплект Белого Тигра Запада",
                new SetBonus("Ярость тигра", 0.35, 0.15, 0.25),
                42,
                "Зерикания",
                "Легендарный доспех зериканских воинов",
                new RegionBonus("Зерикания", "Стойкость к жаре", 0.3),
                888000,
                665.0);
        setArmorPieces(KitArmourPieces.of(getName(), 110, 16.5));
    }
}
