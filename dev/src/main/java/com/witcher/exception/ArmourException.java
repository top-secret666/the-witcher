package com.witcher.exception;

public class ArmourException extends RuntimeException {
    public ArmourException(String message) {
        super("(Ошибка брони:)" + message);
    }
}
