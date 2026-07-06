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
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    private final List<String> purchasedLabels = new ArrayList<>();
    private final Map<ShopEquipSlot, Armour> equipped = new EnumMap<>(ShopEquipSlot.class);

    private int wallet;
    private boolean hideWalletAmount;

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

    /** Кошелёк ещё скрыт (???), сцена с мешком не проиграна. */
    public boolean needsWalletReveal() {
        return hideWalletAmount;
    }

    /** После анимации мешка — показываем реальную сумму. */
    public void revealWallet() {
        hideWalletAmount = false;
    }

    public String dukeLineForCategory(ShopCategory category) {
        return DukeLines.forCategory(category);
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
            case POTION -> new String[]{"Токсин", "0.5 кг", "Осторожно"};
            case WEAPON -> new String[]{"Урон 42", "Вес 8", "Сталь"};
            default -> new String[]{"—", "—", category.label};
        };
    }

    public record StatRow(int value, int delta, int max) {
    }

    public record StatPreview(StatRow[] rows) {
    }

    private static final int STAT_BAR_MAX = ShopGearRules.STAT_BAR_MAX;

    /** Базовые статы Геральта без выбранной экипировки в слоте. */
    public ShopGearStats baseGearStats() {
        return ShopGearStats.geraltBase();
    }

    /** Статы после «примерки» позиции из каталога. */
    public ShopGearStats gearStatsWith(ShopCatalogEntry entry) {
        if (entry == null) {
            return baseGearStats();
        }
        ShopGearStats bonus = bonusFromEntry(entry);
        return baseGearStats().plus(bonus).clamped();
    }

    public StatPreview statPreview(ShopCatalogEntry entry) {
        ShopGearStats base = baseGearStats();
        ShopGearStats with = gearStatsWith(entry);
        return new StatPreview(new StatRow[]{
            new StatRow(with.protection(), with.protection() - base.protection(), STAT_BAR_MAX),
            new StatRow(with.stamina(), with.stamina() - base.stamina(), STAT_BAR_MAX),
            new StatRow(with.signs(), with.signs() - base.signs(), STAT_BAR_MAX)
        });
    }

    private ShopGearStats bonusFromEntry(ShopCatalogEntry entry) {
        if (entry.armour != null) {
            return ShopGearRules.bonusFromArmour(entry.armour);
        }
        if (entry.armourSet != null) {
            return ShopGearRules.bonusFromSet(entry.armourSet);
        }
        return ShopGearRules.placeholderBonus(entry.name);
    }

    public String priceLabelForCategory(ShopCategory category) {
        var min = getCatalog(category).stream()
            .mapToInt(e -> e.price)
            .filter(p -> p > 0)
            .min();
        return min.isPresent() ? String.valueOf(min.getAsInt()) : "···";
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

    public boolean canPurchase(ShopCatalogEntry entry) {
        if (entry == null) {
            return false;
        }
        if (hideWalletAmount) {
            return true;
        }
        if (entry.armour != null && soldArmor.contains(entry.armour)) {
            return false;
        }
        if (entry.armourSet != null && soldSets.contains(entry.armourSet)) {
            return false;
        }
        return wallet >= entry.price;
    }

    public List<String> inventoryItemNames() {
        return List.copyOf(purchasedLabels);
    }

    public int inventoryItemCount() {
        return purchasedLabels.size();
    }

    public List<Armour> ownedArmour() {
        return List.copyOf(playerInventory);
    }

    public Armour getEquipped(ShopEquipSlot slot) {
        return equipped.get(slot);
    }

    public boolean isEquipped(Armour armour) {
        return equipped.containsValue(armour);
    }

    public void equipArmour(Armour armour) {
        if (armour == null || !playerInventory.contains(armour)) {
            return;
        }
        ShopEquipSlot slot = ShopEquipSlot.forArmour(armour);
        if (slot != null) {
            equipped.put(slot, armour);
        }
    }

    public void unequip(ShopEquipSlot slot) {
        equipped.remove(slot);
    }

    public ShopGearStats equippedGearStats() {
        ShopGearStats stats = baseGearStats();
        for (Armour armour : equipped.values()) {
            stats = stats.plus(ShopGearRules.bonusFromArmour(armour));
        }
        return stats.clamped();
    }

    public StatPreview equippedStatPreview() {
        ShopGearStats base = baseGearStats();
        ShopGearStats with = equippedGearStats();
        return new StatPreview(new StatRow[]{
            new StatRow(with.protection(), with.protection() - base.protection(), STAT_BAR_MAX),
            new StatRow(with.stamina(), with.stamina() - base.stamina(), STAT_BAR_MAX),
            new StatRow(with.signs(), with.signs() - base.signs(), STAT_BAR_MAX)
        });
    }

    private void recordPurchase(String label) {
        purchasedLabels.add(label);
    }

    public PurchaseResult purchase(ShopCatalogEntry entry) {
        if (entry == null) {
            return PurchaseResult.fail(DukeLines.purchaseFailGeneric());
        }
        if (entry.armourSet != null) {
            return purchaseSet(entry);
        }
        if (entry.armour != null) {
            return purchaseArmor(entry);
        }
        if (entry.placeholder) {
            return purchasePlaceholder(entry);
        }
        return PurchaseResult.fail("Этого у меня уже нет на полке.");
    }

    private PurchaseResult purchaseArmor(ShopCatalogEntry entry) {
        Armour armour = entry.armour;
        if (soldArmor.contains(armour)) {
            return PurchaseResult.fail(DukeLines.purchaseFailSold());
        }
        int price = entry.price;
        if (wallet < price) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
        wallet -= price;
        soldArmor.add(armour);
        playerInventory.add(armour);
        recordPurchase(armour.getName());
        return PurchaseResult.ok(DukeLines.purchaseOk(armour.getName(), price));
    }

    private PurchaseResult purchaseSet(ShopCatalogEntry entry) {
        ArmourSet set = entry.armourSet;
        if (soldSets.contains(set)) {
            return PurchaseResult.fail(DukeLines.purchaseFailSold());
        }
        int price = entry.price;
        if (wallet < price) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
        wallet -= price;
        soldSets.add(set);
        playerInventory.addAll(set.getArmorPieces());
        recordPurchase(set.getName());
        return PurchaseResult.ok(DukeLines.purchaseOk(set.getName(), price));
    }

    private PurchaseResult purchasePlaceholder(ShopCatalogEntry entry) {
        if (wallet < entry.price) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
        wallet -= entry.price;
        recordPurchase(entry.name);
        return PurchaseResult.ok(DukeLines.purchaseOk(entry.name, entry.price));
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
            int price = ShopPricing.setPrice(set);
            out.add(ShopCatalogEntry.fromSet(set, price));
        }
        out.sort(Comparator.comparingInt(e -> e.price));
        return out;
    }

    private static List<ShopCatalogEntry> staticPotionOffers() {
        return List.of(
            ShopCatalogEntry.placeholder("Зелье «Чёрный гриф»", 18),
            ShopCatalogEntry.placeholder("Эликсир кошки", 26),
            ShopCatalogEntry.placeholder("Отвар грифона", 34)
        );
    }

    private static List<ShopCatalogEntry> staticWeaponOffers() {
        return List.of(
            ShopCatalogEntry.placeholder("Стальной меч", 118),
            ShopCatalogEntry.placeholder("Серебряный кинжал", 88),
            ShopCatalogEntry.placeholder("Двуручный клеймор", 165),
            ShopCatalogEntry.placeholder("Арбалет охотника", 102)
        );
    }

    private static String formatWeight(double weight) {
        if (weight == Math.floor(weight)) {
            return String.valueOf((int) weight);
        }
        return String.format("%.1f", weight);
    }
}
