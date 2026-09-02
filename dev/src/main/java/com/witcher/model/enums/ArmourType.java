package main.java.com.witcher.model.enums;

public enum ArmourType {
    LIGHT("Легкая броня", 1.0, 1.3),    // легкая броня - низкая защита, высокая подвижность
    MEDIUM("Средняя броня", 1.3, 1.0),   // средняя броня - баланс защиты и подвижности
    HEAVY("Тяжелая броня", 1.6, 0.7),   // тяжелая броня - высокая защита, низкая подвижность
    FULL_SET("Полный комплект", 2.0, 1.5);

    private final String description;
    private final double protectionMultiplier;
    private final double mobilityMultiplier;

    ArmourType(String description, double protectionMultiplier, double mobilityMultiplier) {
        this.description = description;
        this.protectionMultiplier = protectionMultiplier;
        this.mobilityMultiplier = mobilityMultiplier;
    }

    public String getDescription() {
        return description;
    }

    public double getProtectionMultiplier() {
        return protectionMultiplier;
    }

    public double getMobilityMultiplier() {
        return mobilityMultiplier;
    }
}
