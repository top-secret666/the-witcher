package main.java.com.witcher.exception;

public class InvalidPriceRangeException extends ArmourException {
    public InvalidPriceRangeException(int minPrice, int maxPrice) {
        super(String.format(
                "\u001B[31mНедопустимый ценовой диапазон: от %d до %d крон.\n" +
                        "Минимальная цена (min=10) должна быть меньше максимальной (max=1000000) и обе должны быть положительными.\u001B[0m",
                minPrice, maxPrice
        ));
    }
}
