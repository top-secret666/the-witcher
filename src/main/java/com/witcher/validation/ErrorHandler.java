package main.java.com.witcher.validation;

import main.java.com.witcher.exception.*;
import java.util.function.Supplier;

public class ErrorHandler {
    public static <T> T handle(Supplier<T> action, String errorContext) {
        try {
            return action.get();
        } catch (InvalidPriceException e) {
            System.out.println("Герцог: " + e.getMessage());
            return null;
        } catch (InvalidPriceRangeException e) {
            System.out.println("Герцог: " + e.getMessage());
            return null;
        } catch (NoArmourFoundException e) {
            System.out.println("Герцог: " + e.getMessage());
            return null;
        } catch (InvalidInputException e) {
            System.out.println("Герцог: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.out.println("Герцог: Хм... " + errorContext);
            return null;
        }
    }
}
