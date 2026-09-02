package main.java.com.witcher.model.enums;

public enum ArmourCategory {
    COMMON("Обычная", 1.0, 0) ,     // обычная броня - базовые характеристики, без эффектов
    MAGICAL("Магическая", 1.2, 1) ,  // магическая броня - улучшенные характеристики, 1 эффект
    MASTERWORK("Мастерская", 1.4, 2), // мастерская броня - высокие характеристики, 2 эффекта
    RELIC("Реликтовая", 1.6, 3),  // реликтовая броня - очень высокие характеристики, 3 эффекта
    SET_ITEM("Комплектная", 1.8, 3); // комплектная броня - максимальные характеристики, 3 эффекта


    private final String description;
    private final double qualityMultiplier;
    private final int maxEffectsCount;

    ArmourCategory(String description, double qualityMultiplier, int maxEffectsCount) {
        this.description = description;
        this.qualityMultiplier = qualityMultiplier;
        this.maxEffectsCount = maxEffectsCount;
    }

    public String getDescription() {
        return description;
    }

    public double getQualityMultiplier() {
        return qualityMultiplier;
    }

    public int getMaxEffectsCount() {
        return maxEffectsCount;
    }
}
