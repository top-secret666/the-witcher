package main.java.com.witcher.exception;

public class InvalidPriceException extends ArmourException {
    public InvalidPriceException(int price) {
        super(String.format("\u001B[31m<<Недопустимая цена: %d крон. Цена должна быть положительной и лежать в диапазоне от 10 до 100000.>>\u001B[0m", price));
    }

//    public InvalidPriceException(String message) {
//        super("\u001B[31m" + message + "\u001B[0m");
//    }
}
