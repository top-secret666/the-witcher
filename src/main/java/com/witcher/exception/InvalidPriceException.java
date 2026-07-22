package main.java.com.witcher.exception;

import main.java.com.witcher.validation.InputValidator;

public class InvalidPriceException extends ArmourException {
    public InvalidPriceException(int price) {
        super(String.format(
                "\u001B[31m<<Недопустимая цена: %d крон. Цена должна быть положительной и лежать в диапазоне от %d до %d.>>\u001B[0m",
                price, InputValidator.MIN_PRICE, InputValidator.MAX_PRICE));
    }
}
