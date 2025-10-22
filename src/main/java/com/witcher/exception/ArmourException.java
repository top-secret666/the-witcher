package main.java.com.witcher.exception;

public class ArmourException extends RuntimeException {
    public ArmourException(String message) {
        super("(Ошибка брони:)" + message);
    }

//    public ArmourException(String message, Throwable cause) {
//        super("(Ошибка брони:)" + message, cause);
//    }
}
