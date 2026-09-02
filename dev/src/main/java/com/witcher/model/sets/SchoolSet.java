package main.java.com.witcher.model.sets;

import main.java.com.witcher.model.enums.ArmourType;

public abstract class SchoolSet extends ArmourSet {
    private final SchoolType schoolType;
    private final String schoolDescription;
    private final SpecialAbility specialAbility;

    public enum SchoolType {
        WOLF("Школа Волка", "Баланс атаки и защиты", ArmourType.MEDIUM),
        GRIFFIN("Школа Грифона", "Усиление знаков", ArmourType.MEDIUM),
        CAT("Школа Кота", "Скорость и ловкость", ArmourType.LIGHT),
        BEAR("Школа Медведя", "Тяжелая броня и защита", ArmourType.HEAVY),
        MANTICORE("Школа Мантикоры", "Алхимия и яды", ArmourType.MEDIUM);

        private final String name;
        private final String specialty;
        private final ArmourType preferredArmorType;

        SchoolType(String name, String specialty, ArmourType preferredArmorType) {
            this.name = name;
            this.specialty = specialty;
            this.preferredArmorType = preferredArmorType;
        }

        public String getName() { return name; }
        public ArmourType getPreferredArmorType() { return preferredArmorType; }
        public String getSpecialty() { return specialty; }

    }

    public record SpecialAbility(
            String name,
            String description,
            double effectPower
    ) {}

    protected SchoolSet(String name, SetBonus setBonus, int requiredLevel,
                        SchoolType schoolType, SpecialAbility specialAbility,
                        int basePrice, double baseWeight) {
        super(name, setBonus, requiredLevel, Rarity.EPIC, basePrice, baseWeight);
        this.schoolType = schoolType;
        this.schoolDescription = schoolType.getSpecialty();
        this.specialAbility = specialAbility;
    }


    public SchoolType getSchoolType() {
        return schoolType;
    }

    public String getSchoolDescription() {
        return schoolDescription;
    }

    public SpecialAbility getSpecialAbility() {
        return specialAbility;
    }




}
