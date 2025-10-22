package main.java.com.witcher.ui;

import main.java.com.witcher.service.ArmorCalculationService;
import main.java.com.witcher.service.ArmorService;
import main.java.com.witcher.service.ArmorSortingService;
import main.java.com.witcher.service.SetService;
import main.java.com.witcher.validation.InputValidator;
import main.java.com.witcher.repository.ArmourRepository;

import java.util.Scanner;

import static java.awt.SystemColor.text;


public class ConsoleUI {
    private final DialogManager dialogManager;
    private final MenuHandler menuHandler;
    private final int geraltsWallet;

    public ConsoleUI(ArmorService armorService, ArmorCalculationService armorCalculationService, ArmorSortingService armorSortingService, SetService setService, InputValidator validator, ArmourRepository armourRepository, int initialWallet) {
        this.dialogManager = new DialogManager(setService, armorCalculationService);
        this.menuHandler = new MenuHandler(armorService, armorCalculationService, armorSortingService, setService, validator, dialogManager, armourRepository, initialWallet);
        this.geraltsWallet = initialWallet;
    }

    public void start() {
        System.out.println("\nНажмите Enter чтобы пропустить диалог или любую другую клавишу для продолжения...");
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine();

        if (!input.isEmpty()) {
            dialogManager.showIntroduction();
            showShopEntrance();
        }

        while (true) {
            dialogManager.displayMainMenu();
            int choice = menuHandler.getMenuChoice();
            if (choice == 0) {
                dialogManager.showOutro();
                break;
            }
            menuHandler.handleMenuChoice(choice);
        }
    }

//    private void showProgressBar() {
//        String bar = "";
//        for (int i = 0; i <= 20; i++) {
//            bar += "█";
//            System.out.print("\rЗагрузка: [" + bar + "] " + i * 5 + "%");
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//        System.out.println();
//    }


private void showShopEntrance() {
    String[] frames = {
            "╔═══[†]═══╗",
            "╔═══[‡]═══╗",
            "╔═══[†]═══╗",
            "╔═══[‡]═══╗"
    };
    for (int i = 0; i < 12; i++) {
        System.out.print("\r" + frames[i % frames.length] + " Входим в магазин герцога...");
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
}