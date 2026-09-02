package main.java.com.witcher.exception;

import main.java.com.witcher.model.armour.Armour;

import java.util.List;

public class ExcessBudgetException extends Exception {
    private final int remainingBudget;
    private final List<Armour> currentSet;

    public ExcessBudgetException(int remainingBudget, List<Armour> currentSet) {
        super("Хм... У вас остается приличная сумма. Желаете подобрать дополнительную броню? (y/n)");
        this.remainingBudget = remainingBudget;
        this.currentSet = currentSet;
    }

    public int getRemainingBudget() {
        return remainingBudget;
    }

    public List<Armour> getCurrentSet() {
        return currentSet;
    }
}
