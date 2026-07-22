package main.java.com.witcher.exception;

import main.java.com.witcher.validation.InputValidator;

public class InvalidPriceRangeException extends ArmourException {
    public InvalidPriceRangeException(int minPrice, int maxPrice) {
        super(String.format(
                "\u001B[31mНедопустимый ценовой диапазон: от %d до %d крон.\n" +
                        "Минимальная цена (min=%d) должна быть меньше максимальной (max=%d) и обе должны быть положительными.\u001B[0m",
                minPrice, maxPrice, InputValidator.MIN_PRICE, InputValidator.MAX_PRICE
        ));
    }
}
