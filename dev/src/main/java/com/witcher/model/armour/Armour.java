package main.java.com.witcher.model.armour;

import main.java.com.witcher.model.enums.ArmourCategory;
import main.java.com.witcher.model.enums.ArmourType;
import java.util.Objects;

public abstract class Armour {
    private final String name;
    private final ArmourType type;
    private final ArmourCategory category;
    private final int price;
    private final double weight;
    private final boolean isPaired;
    private int requiredLevel;

    protected Armour (String name,
                      ArmourType type,
                      ArmourCategory category,
                      int price,
                      double weight,
                      boolean isPaired) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.price = price;
        this.weight = weight;
        this.isPaired = isPaired;
    }

    public String getName() {
        return name;
    }

    public ArmourType getType() {
        return type;
    }

    public ArmourCategory getCategory() {
        return category;
    }

    public int getPrice() {
        return price;
    }

    public double getWeight() {
        return weight;
    }

    public boolean isPaired() {
        return isPaired;
    }

    public abstract String getDescription();
    public abstract double calculateProtection();

    public int getRequiredLevel() {
        return requiredLevel;
    }

    @Override
    public String toString() {
        return String.format("Название: %s, Тип: %s, Категория: %s, Цена: %d, Парный предмет: %s",
                name, type, category, price, isPaired ? "Да" : "Нет");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Armour armor)) return false;
        return price == armor.price &&
                isPaired == armor.isPaired &&
                Double.compare(armor.weight, weight) == 0 &&
                Objects.equals(name, armor.name) &&
                type == armor.type &&
                category == armor.category;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, category, price, isPaired);
    }
}
