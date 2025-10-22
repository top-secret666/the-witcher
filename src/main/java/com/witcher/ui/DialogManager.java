package main.java.com.witcher.ui;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.service.ArmorCalculationService;
import main.java.com.witcher.service.SetService;

import java.util.List;

public class DialogManager {
    private static final int DIALOG_DELAY = 25;
    private static final int TYPING_DELAY = 25;
    private final SetService setService;
    private final ArmorCalculationService armorCalculationService;

    public DialogManager(SetService setService, ArmorCalculationService armorCalculationService) {
        if (setService == null) {
            throw new IllegalArgumentException("SetService не может быть null");
        }
        this.setService = setService;
        this.armorCalculationService = armorCalculationService;

    }

    public void showDialogLine(String speaker, String text) {
        sleep(DIALOG_DELAY);
        System.out.print("\n" + speaker + ": ");

        for (char c : text.toCharArray()) {
            System.out.print(c);
            sleep(TYPING_DELAY);
        }
        System.out.println();
    }

    public void showDelayedText(String text) {
        sleep(DIALOG_DELAY);

        String[] lines = text.split("\n");
        for (String line : lines) {
            for (char c : line.toCharArray()) {
                System.out.print(c);
                sleep(TYPING_DELAY);
            }
            System.out.println();
        }
    }

    public void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public void sleep(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void showIntroduction() {
        clearScreen();
        showDelayedText("""
                *Ветер завывает между древних стен Каэр Морхена.
                Геральт неспешно поднимается по разрушенной лестнице...
                """);

        showDelayedText("""
                *Внезапно, прямо из стены замка вырастает ужасающая фигура в богато украшенном камзоле...*
                """);

        showDialogLine("Незнокомое существо", """
                *хриплый смех* Вас-то я и ждал, Геральт из Ривии.Здешний замок вам не кажется подозрительным?
                *незнакомец устремляет свой взгляд на Арнскрон*
                """);
        showDialogLine("Геральт", "...Ага, и вы тоже.");

        showDialogLine("Герцог", """
                 ХО-ХО-ХО-ХА... Нет, нет, я обычный торговец. Я не представился. Зовите меня Герцог.
                Приступим к делу. Броня, кирасы, шлемы, наколенники...
                Обеспечу вас всем, чего пожелаете.
                """);

        showDelayedText("\n*Из стены замка начинает вырастать настоящий торговый прилавок*\n");
    }

    public void displayMainMenu() {
        System.out.println("""
           
            ╔═══════════════  𝕷𝖆𝖛𝖐𝖆 𝕲𝖊𝖗𝖙𝖘𝖔𝖌𝖆  ═════════════════╗
           ║                                                 ║
            ║    1. Подобрать комплект брони                ║
           ║     2. Перейти в инвентарь                      ║
            ║    3. Просмотр всех комплектов                ║
           ║                                                 ║
            ║    0. Уйти                                    ║
           ║                                                 ║
           ╚═════════════════════════════════════════════════╝
           """);
        }

    public void displayInventoryMenu() {
        System.out.println("""
  
        ╔════════════════  𝕴𝖓𝖛𝖊𝖓𝖙𝖆𝖗 𝕲𝖊𝖗𝖆𝖑𝖙𝖆  ══════════════════╗
       ║                                                    ║
        ║    1. Сортировка брони                           ║
       ║     2. Поиск брони по цене                         ║
        ║    3. Надеть броню 🛡️                            ║
       ║                                                    ║
        ║    0. Вернуться в главное меню                   ║
       ║                                                    ║
        ╚══════════════════════════════════════════════════╝
       """);
    }

    public void displaySetPurchaseMenu() {
        System.out.println("""
        
        ╔══════════════  𝕶𝖔𝖒𝖕𝖑𝖊𝖐𝖙𝖞 𝖇𝖗𝖔𝖓𝖎  ═════════════════╗
         ║                                              ║
        ║    1. Эпические комплекты                    ║
         ║   2. Легендарные комплекты                   ║
        ║    0. Вернуться                              ║
         ║                                              ║
        ╚══════════════════════════════════════════════╝
        """);
    }

    public void showOutro() {
        showDialogLine("Герцог", """
                     Удачного приключения!
                     """);
        showDelayedText("""
                 *...*
                """);
    }

    public void displayArmorList(List<Armour> armorList) {
        if (armorList.isEmpty()) {
            showDialogLine("Герцог", "Извините...Я не нашел ничего подходящего...");
            return;
        }

        armorList.forEach(armor ->
                System.out.printf("%s - Цена: %d, Вес: %.2f\n",
                        armor.getName(), armor.getPrice(), armor.getWeight()));
    }

    public void displayArmorSet(List<Armour> armorSet, double totalProtection, double totalWeight) {
        System.out.println("=== Подобранный комплект ===");
        displayArmorList(armorSet);
        System.out.printf("Общая защита: %.2f\n", totalProtection);
        System.out.printf("Общий вес: %.2f\n", totalWeight);
        System.out.printf("Общая эффективность: %.2f\n", armorCalculationService.calculateTotalEffectiveness(armorSet));
    }

    public void displaySetsList() {
        System.out.println("\n=== Доступные комплекты ===");
        System.out.println("\nЭпические комплекты (Школьные):");
        setService.getSchoolSets().forEach(set -> {
            System.out.printf("\n%s (Редкость: %s)\n", set.getName(), set.getRarity().getDisplayName());
            System.out.printf("Цена: %d\n", set.calculateTotalPrice());
            System.out.printf("Вес: %.2f\n", set.calculateTotalWeight());
            System.out.printf("Особая способность: %s\n", set.getSpecialAbility().name());
            System.out.println("------------------------");
        });

        System.out.println("\nЛегендарные комплекты:");
        setService.getNonSchoolSets().forEach(set -> {
            System.out.printf("\n%s (Редкость: %s)\n", set.getName(), set.getRarity().getDisplayName());
            System.out.printf("Цена: %d\n", set.calculateTotalPrice());
            System.out.printf("Вес: %.2f\n", set.calculateTotalWeight());
            System.out.printf("Региональный бонус: %s\n", set.getRegionBonus().effect());
            System.out.println("------------------------");
        });
    }
}



