package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.*;
import main.java.com.witcher.model.sets.ArmourSet;
import main.java.com.witcher.model.sets.SchoolSet;
import main.java.com.witcher.repository.ArmourRepository;
import main.java.com.witcher.repository.SetRepository;
import main.java.com.witcher.service.ArmorCalculationService;
import main.java.com.witcher.service.ArmorGenerationService;
import main.java.com.witcher.service.ArmorManagementService;
import main.java.com.witcher.service.SetService;
import main.java.com.witcher.validation.InputValidator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Доменная логика лавки: инвентарь, кошелёк, покупки.
 * Связывает Swing-витрину с сервисами из консольной версии.
 */
public final class ShopModel {

    public record PurchaseResult(boolean success, String dukeLine) {
        public static PurchaseResult ok(String line) {
            return new PurchaseResult(true, line);
        }

        public static PurchaseResult fail(String line) {
            return new PurchaseResult(false, line);
        }
    }

    private final ArmourRepository armourRepository;
    private final SetService setService;
    private final ArmorCalculationService calculationService;
    private final Set<Armour> soldArmor = new HashSet<>();
    private final Set<ArmourSet> soldSets = new HashSet<>();
    private final List<Armour> playerInventory = new ArrayList<>();

    private int wallet;
    private final boolean hideWalletAmount;

    private ShopModel(ArmourRepository armourRepository, SetService setService,
                        ArmorCalculationService calculationService, int wallet, boolean hideWalletAmount) {
        this.armourRepository = armourRepository;
        this.setService = setService;
        this.calculationService = calculationService;
        this.wallet = wallet;
        this.hideWalletAmount = hideWalletAmount;
    }

    /** Новая сессия лавки: генерирует товары как в {@link main.java.com.witcher.Main}. */
    public static ShopModel createNewSession() {
        return createNewSession(420, true);
    }

    public static ShopModel createNewSession(int startingCrowns, boolean hideWalletAmount) {
        ArmourRepository armourRepository = new ArmourRepository();
        SetRepository setRepository = new SetRepository();
        InputValidator validator = new InputValidator();
        ArmorCalculationService calculationService = new ArmorCalculationService();
        ArmorManagementService managementService = new ArmorManagementService(
            armourRepository, calculationService, validator);
        ArmorGenerationService generationService = new ArmorGenerationService(managementService);
        generationService.generateRandomInventory();

        SetService setService = new SetService(setRepository);
        return new ShopModel(armourRepository, setService, calculationService, startingCrowns, hideWalletAmount);
    }

    public String walletAmountText() {
        return hideWalletAmount ? "???" : String.valueOf(wallet);
    }

    public String walletSuffix() {
        return " крон";
    }

    public int getWallet() {
        return wallet;
    }

    public String dukeLineForCategory(ShopCategory category) {
        return switch (category) {
            case CHEST -> "Отличный выбор! Волчья сталь — как раз для таких, как вы.";
            case LEGS -> "Штаны крепкие. Ноги целее — монстров больше.";
            case GLOVES -> "Рукам тепло, клинку — верно. Берите, не пожалеете.";
            case BOOTS -> "В этих сапогах и по болоту пройдёте, и от удара отскочите.";
            case POTION -> "Хм... Зелье? Ну что ж, ваш выбор, Белый Волк...";
            case SETS -> "Ах, охотник на целые комплекты! Волчья, Кошачья, Грифонья — выбирайте.";
            case WEAPON -> "Сталь режет, серебро жжёт. Выбирайте клинок по вкусу.";
        };
    }

    public String[] statLinesForCategory(ShopCategory category) {
        List<ShopCatalogEntry> entries = getCatalog(category);
        if (!entries.isEmpty() && entries.get(0).armour != null) {
            Armour a = entries.get(0).armour;
            return new String[]{
                "Защ. " + Math.round(a.calculateProtection()),
                "Вес " + formatWeight(a.getWeight()),
                category.label
            };
        }
        return switch (category) {
            case SETS -> new String[]{"Школьные", "Легендар.", "4 части"};
            case POTION -> new String[]{"Яд", "0.5 кг", "Осторожно"};
            case WEAPON -> new String[]{"Урон 48", "Вес 9", "Сталь"};
            default -> new String[]{"—", "—", category.label};
        };
    }

    public String priceLabelForCategory(ShopCategory category) {
        return getCatalog(category).stream()
            .mapToInt(e -> e.price)
            .filter(p -> p > 0)
            .min()
            .stream()
            .mapToObj(String::valueOf)
            .findFirst()
            .orElse("···");
    }

    public List<ShopCatalogEntry> getCatalog(ShopCategory category) {
        return switch (category) {
            case CHEST -> armorByType(Chestpiece.class);
            case LEGS -> armorByType(Trousers.class);
            case GLOVES -> armorByType(Gloves.class);
            case BOOTS -> armorByType(Boots.class);
            case SETS -> setsCatalog();
            case POTION -> staticPotionOffers();
            case WEAPON -> staticWeaponOffers();
        };
    }

    public PurchaseResult purchase(ShopCatalogEntry entry) {
        if (entry == null) {
            return PurchaseResult.fail("Что-что? Не расслышал...");
        }
        if (entry.armourSet != null) {
            return purchaseSet(entry);
        }
        if (entry.armour != null) {
            return purchaseArmor(entry.armour);
        }
        if (entry.placeholder) {
            return purchasePlaceholder(entry);
        }
        return PurchaseResult.fail("Этого у меня уже нет на полке.");
    }

    private PurchaseResult purchaseArmor(Armour armour) {
        if (soldArmor.contains(armour)) {
            return PurchaseResult.fail("Уже продано. Вы опоздали, Белый Волк.");
        }
        int price = armour.getPrice();
        if (wallet < price) {
            return PurchaseResult.fail("Кошелёк пустеет быстрее, чем вы думаете. Не хватает крон.");
        }
        wallet -= price;
        soldArmor.add(armour);
        playerInventory.add(armour);
        return PurchaseResult.ok("Берите " + armour.getName() + " — " + price + " крон. Не пожалеете.");
    }

    private PurchaseResult purchaseSet(ShopCatalogEntry entry) {
        ArmourSet set = entry.armourSet;
        if (soldSets.contains(set)) {
            return PurchaseResult.fail("Комплект уже ушёл с прилавка.");
        }
        int price = entry.price;
        if (wallet < price) {
            return PurchaseResult.fail("За такой комплект нужно больше крон.");
        }
        wallet -= price;
        soldSets.add(set);
        playerInventory.addAll(set.getArmorPieces());
        return PurchaseResult.ok(set.getName() + " — отличный выбор. Охотник оценит.");
    }

    private PurchaseResult purchasePlaceholder(ShopCatalogEntry entry) {
        if (wallet < entry.price) {
            return PurchaseResult.fail("Не хватает крон.");
        }
        wallet -= entry.price;
        return PurchaseResult.ok(entry.name + " — ваш выбор. Только не разлейте по дороге.");
    }

    private List<ShopCatalogEntry> armorByType(Class<? extends Armour> type) {
        List<ShopCatalogEntry> out = new ArrayList<>();
        for (Armour armour : armourRepository.getAllArmor()) {
            if (!type.isInstance(armour) || soldArmor.contains(armour)) {
                continue;
            }
            out.add(ShopCatalogEntry.fromArmour(armour));
        }
        out.sort(Comparator.comparingInt(e -> e.price));
        return out;
    }

    private List<ShopCatalogEntry> setsCatalog() {
        List<ShopCatalogEntry> out = new ArrayList<>();
        for (SchoolSet set : setService.getSchoolSets()) {
            if (soldSets.contains(set)) {
                continue;
            }
            int price = setService.calculateSetPrice(set);
            out.add(ShopCatalogEntry.fromSet(set, price));
        }
        out.sort(Comparator.comparingInt(e -> e.price));
        return out;
    }

    private static List<ShopCatalogEntry> staticPotionOffers() {
        return List.of(
            ShopCatalogEntry.placeholder("Зелье «Чёрный гриф»", 15),
            ShopCatalogEntry.placeholder("Эликсир кошки", 22),
            ShopCatalogEntry.placeholder("Отвар грифона", 28)
        );
    }

    private static List<ShopCatalogEntry> staticWeaponOffers() {
        return List.of(
            ShopCatalogEntry.placeholder("Стальной меч", 95),
            ShopCatalogEntry.placeholder("Серебряный кинжал", 72),
            ShopCatalogEntry.placeholder("Двуручный клеймор", 140),
            ShopCatalogEntry.placeholder("Арбалет охотника", 88)
        );
    }

    private static String formatWeight(double weight) {
        if (weight == Math.floor(weight)) {
            return String.valueOf((int) weight);
        }
        return String.format("%.1f", weight);
    }
}
