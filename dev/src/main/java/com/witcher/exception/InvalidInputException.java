package main.java.com.witcher.exception;

public class InvalidInputException extends ArmourException {
    public InvalidInputException(String message) {
        super("(Некорректный ввод:)" + message);
    }
}
