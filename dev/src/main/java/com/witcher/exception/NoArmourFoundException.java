package main.java.com.witcher.exception;

public class NoArmourFoundException extends ArmourException {
    public NoArmourFoundException(String message) {
        super("Броня не найдена:" + message);
    }

    public NoArmourFoundException(int price) {
        super(String.format(
                "\u001BНе удалось найти подходящий комплект брони за %d крон.\u001B" +
                        "\u001BПопробуйте указать другую сумму\u001B.",
                price
        ));
    }
}
