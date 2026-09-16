package com.witcher.ui.shop;

import com.witcher.model.armour.*;
import com.witcher.model.sets.ArmourSet;
import com.witcher.model.sets.BeauclaireGuardSet;
import com.witcher.model.sets.NonSchoolSet;
import com.witcher.model.sets.TemerianKitSet;
import com.witcher.model.sets.TouissantSet;
import com.witcher.model.sets.WhiteTigerSet;
import com.witcher.shop.EquipSlot;
import com.witcher.shop.EquippedGear;
import com.witcher.repository.ArmourRepository;
import com.witcher.repository.SetRepository;
import com.witcher.service.ArmorCalculationService;
import com.witcher.service.ArmorGenerationService;
import com.witcher.service.ArmorManagementService;
import com.witcher.service.SetService;
import com.witcher.validation.InputValidator;

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
public final class ShopModel implements EquippedGear {

    /** Временно для теста: покупки без списания крон. */
    private static final boolean FREE_PURCHASES_FOR_TEST = false;

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
    private final List<ShopInventorySlot> pouchConsumables = new ArrayList<>();
    private final List<String> drunkPotions = new ArrayList<>();
    private final Map<EquipSlot, Armour> equipped = new EnumMap<>(EquipSlot.class);
    private ShopInventorySlot equippedWeapon;

    /** Примерка после «помочь подобрать»: ещё не списаны кроны. */
    private boolean outfitPreviewPending;
    private int outfitPreviewPrice;
    private ArmourSet outfitPreviewSet;
    private final List<Armour> outfitPreviewPieces = new ArrayList<>();
    private ShopInventorySlot outfitPreviewWeapon;

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

    /** Новая сессия лавки: генерирует товары как в {@link com.witcher.Main}. */
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
            case SETS -> new String[]{"Регионы", "4 части", "Эмблема"};
            case POTION -> new String[]{"Усиливает тело и Знаки.", "Эффект зависит от отвара.", "Пейте с умом."};
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

    /** Статы после «примерки» позиции из каталога — поверх уже купленного. */
    public ShopGearStats gearStatsWith(ShopCatalogEntry entry) {
        ShopGearStats current = equippedGearStats();
        if (entry == null) {
            return current;
        }
        if (entry.armour != null) {
            EquipSlot slot = EquipSlot.forArmour(entry.armour);
            Armour worn = slot != null ? equipped.get(slot) : null;
            if (worn != null) {
                current = current.minus(ShopGearRules.bonusFromArmour(worn));
            }
        } else if (entry.armourSet != null) {
            for (Armour worn : equipped.values()) {
                current = current.minus(ShopGearRules.bonusFromArmour(worn));
            }
        } else if (equippedWeapon != null && isWeaponCatalogName(entry.name)) {
            current = current.minus(ShopGearRules.placeholderBonus(equippedWeapon.title()));
        }
        return current.plus(bonusFromEntry(entry)).clamped();
    }

    public StatPreview statPreview(ShopCatalogEntry entry) {
        ShopGearStats base = equippedGearStats();
        ShopGearStats with = gearStatsWith(entry);
        return toPreview(base, with);
    }

    /**
     * Колбы в инвентаре: текущие статы. Жёлтый хвост — только у выбранного зелья до «Выпить».
     */
    public StatPreview inventoryStatPreview(ShopInventorySlot slot) {
        ShopGearStats current = equippedGearStats();
        ShopGearStats extra = bonusFromInventorySlot(slot);
        return toPreview(current, current.plus(extra).clamped());
    }

    private StatPreview toPreview(ShopGearStats base, ShopGearStats with) {
        return new StatPreview(new StatRow[]{
            new StatRow(with.protection(), with.protection() - base.protection(), STAT_BAR_MAX),
            new StatRow(with.stamina(), with.stamina() - base.stamina(), STAT_BAR_MAX),
            new StatRow(with.signs(), with.signs() - base.signs(), STAT_BAR_MAX)
        });
    }

    private ShopGearStats bonusFromInventorySlot(ShopInventorySlot slot) {
        if (slot != null && slot.kind() == ShopInventoryKind.POTION) {
            return ShopGearRules.placeholderBonus(slot.title());
        }
        return new ShopGearStats(0, 0, 0);
    }

    /** Короткое описание эффекта зелья — без «токсина» и веса. */
    public static String[] potionEffectLines(String name) {
        String lower = name == null ? "" : name.toLowerCase();
        if (lower.contains("кошк")) {
            return new String[]{
                "Острее зрение в темноте.",
                "Легче заметить то, что прячется в тени."
            };
        }
        if (lower.contains("отвар") || lower.contains("грифон")) {
            return new String[]{
                "Поднимает выносливость.",
                "Знаки держатся дольше и бьют крепче."
            };
        }
        if (lower.contains("гриф") || lower.contains("зелье") || lower.contains("эликсир")) {
            return new String[]{
                "Усиливает Знаки.",
                "Ведьмачьи чары на короткий срок жгут сильнее."
            };
        }
        return new String[]{
            "Усиливает ведьмачьи способности.",
            "Эффект зависит от состава отвара."
        };
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

    private static boolean isWeaponCatalogName(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        return lower.contains("меч") || lower.contains("клеймор")
            || lower.contains("кинжал") || lower.contains("арбалет");
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
        return FREE_PURCHASES_FOR_TEST || wallet >= entry.price;
    }

    private boolean trySpend(int price) {
        if (FREE_PURCHASES_FOR_TEST) {
            return true;
        }
        if (wallet < price) {
            return false;
        }
        wallet -= price;
        return true;
    }

  public boolean hasDrunkAnyPotion() {
        return !drunkPotions.isEmpty();
    }

    public List<String> inventoryItemNames() {
        return List.copyOf(purchasedLabels);
    }

    public List<ShopInventorySlot> pouchConsumables() {
        return List.copyOf(pouchConsumables);
    }

    public boolean drinkPotion(String name) {
        if (name == null) {
            return false;
        }
        for (int i = 0; i < pouchConsumables.size(); i++) {
            ShopInventorySlot slot = pouchConsumables.get(i);
            if (slot.kind() == ShopInventoryKind.POTION && name.equals(slot.title())) {
                pouchConsumables.remove(i);
                // Одно активное зелье: новое вытесняет прошлое.
                drunkPotions.clear();
                drunkPotions.add(name);
                return true;
            }
        }
        return false;
    }

    public ShopInventorySlot getEquippedWeapon() {
        return equippedWeapon;
    }

    public void equipWeapon(ShopInventorySlot weapon) {
        if (weapon == null || weapon.kind() != ShopInventoryKind.WEAPON) {
            return;
        }
        if (equippedWeapon != null && weapon.title().equals(equippedWeapon.title())) {
            return;
        }
        if (equippedWeapon != null) {
            pouchConsumables.add(equippedWeapon);
        }
        for (int i = 0; i < pouchConsumables.size(); i++) {
            ShopInventorySlot slot = pouchConsumables.get(i);
            if (slot.kind() == ShopInventoryKind.WEAPON && weapon.title().equals(slot.title())) {
                pouchConsumables.remove(i);
                break;
            }
        }
        equippedWeapon = weapon;
    }

    public void unequipWeapon() {
        if (equippedWeapon != null) {
            pouchConsumables.add(equippedWeapon);
            equippedWeapon = null;
        }
    }

    public boolean isSetPiece(Armour armour) {
        if (armour == null) {
            return false;
        }
        for (ArmourSet set : soldSets) {
            if (set.getArmorPieces().contains(armour)) {
                return true;
            }
        }
        return false;
    }

    public int inventoryItemCount() {
        return purchasedLabels.size();
    }

    public List<Armour> ownedArmour() {
        return List.copyOf(playerInventory);
    }

    @Override
    public Armour getEquipped(EquipSlot slot) {
        return equipped.get(slot);
    }

    public boolean hasEquipmentScreenAccess() {
        return !purchasedLabels.isEmpty() || !playerInventory.isEmpty() || !soldSets.isEmpty()
            || equippedWeapon != null || !drunkPotions.isEmpty();
    }

    /** Есть купленная броня, комплект или оружие — можно открыть «Надеть». */
    public boolean hasWearableGear() {
        if (!playerInventory.isEmpty() || !soldSets.isEmpty() || equippedWeapon != null) {
            return true;
        }
        for (ShopInventorySlot slot : pouchConsumables) {
            if (slot.kind() == ShopInventoryKind.WEAPON) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasAnyEquippedItem() {
        if (equippedWeapon != null) {
            return true;
        }
        for (Armour armour : equipped.values()) {
            if (armour != null) {
                return true;
            }
        }
        return false;
    }

    public boolean isEquipped(Armour armour) {
        return equipped.containsValue(armour);
    }

    public void equipArmour(Armour armour) {
        if (armour == null || !playerInventory.contains(armour)) {
            return;
        }
        EquipSlot slot = EquipSlot.forArmour(armour);
        if (slot != null) {
            equipped.put(slot, armour);
        }
    }

    /**
     * Примерка под бюджет витрины: надеть комплект + бесплатное оружие, без списания крон.
     * Подтверждение — {@link #confirmOutfitPreview()}, отказ — {@link #cancelOutfitPreview()}.
     */
    public PurchaseResult beginOutfitPreview(int budget) {
        cancelOutfitPreview();
        int spendBudget = Math.max(0, Math.min(budget, wallet));
        if (spendBudget <= 0) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
        ArmourSet bestSet = null;
        int bestSetPrice = -1;
        for (NonSchoolSet set : setService.getNonSchoolSets()) {
            if (!isShopRegionalKit(set) || soldSets.contains(set)) {
                continue;
            }
            int price = ShopPricing.setPrice(set);
            if (price <= spendBudget && price > bestSetPrice) {
                bestSetPrice = price;
                bestSet = set;
            }
        }
        if (bestSet != null) {
            soldSets.add(bestSet);
            for (Armour piece : bestSet.getArmorPieces()) {
                if (!playerInventory.contains(piece)) {
                    playerInventory.add(piece);
                }
                outfitPreviewPieces.add(piece);
                equipArmour(piece);
            }
            outfitPreviewSet = bestSet;
            outfitPreviewPrice = bestSetPrice;
            grantFreePreviewWeapon();
            outfitPreviewPending = true;
            return PurchaseResult.ok(DukeLines.dukeOutfitPurchaseOrSelf());
        }
        return beginLoosePiecesPreview(spendBudget);
    }

    private PurchaseResult beginLoosePiecesPreview(int spendBudget) {
        List<ShopCatalogEntry> chests = armorByType(Chestpiece.class);
        List<ShopCatalogEntry> trousers = armorByType(Trousers.class);
        List<ShopCatalogEntry> gloves = armorByType(Gloves.class);
        List<ShopCatalogEntry> boots = armorByType(Boots.class);
        if (chests.isEmpty() || trousers.isEmpty() || gloves.isEmpty() || boots.isEmpty()) {
            return PurchaseResult.fail(DukeLines.purchaseFailGeneric());
        }
        ShopCatalogEntry[] pick = {
            chests.get(0), trousers.get(0), gloves.get(0), boots.get(0)
        };
        int total = pick[0].price + pick[1].price + pick[2].price + pick[3].price;
        if (total > spendBudget) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
        List<List<ShopCatalogEntry>> pools = List.of(chests, trousers, gloves, boots);
        boolean upgraded;
        do {
            upgraded = false;
            for (int slot = 0; slot < 4; slot++) {
                ShopCatalogEntry cur = pick[slot];
                for (ShopCatalogEntry cand : pools.get(slot)) {
                    if (cand.price <= cur.price) {
                        continue;
                    }
                    int nextTotal = total - cur.price + cand.price;
                    if (nextTotal <= spendBudget) {
                        pick[slot] = cand;
                        total = nextTotal;
                        upgraded = true;
                        break;
                    }
                }
            }
        } while (upgraded);

        for (ShopCatalogEntry entry : pick) {
            Armour armour = entry.armour;
            if (armour == null) {
                continue;
            }
            soldArmor.add(armour);
            if (!playerInventory.contains(armour)) {
                playerInventory.add(armour);
            }
            outfitPreviewPieces.add(armour);
            equipArmour(armour);
        }
        outfitPreviewSet = null;
        outfitPreviewPrice = total;
        grantFreePreviewWeapon();
        outfitPreviewPending = true;
        return PurchaseResult.ok(DukeLines.dukeOutfitPurchaseOrSelf());
    }

    private void grantFreePreviewWeapon() {
        List<ShopCatalogEntry> weapons = staticWeaponOffers();
        if (weapons.isEmpty()) {
            return;
        }
        ShopCatalogEntry pick = weapons.get(
            java.util.concurrent.ThreadLocalRandom.current().nextInt(weapons.size()));
        String[] lines = statLinesForCategory(ShopCategory.WEAPON);
        ShopInventorySlot weapon = ShopInventorySlot.consumable(pick.name, ShopCategory.WEAPON, lines);
        outfitPreviewWeapon = weapon;
        equippedWeapon = weapon;
    }

    public boolean isOutfitPreviewPending() {
        return outfitPreviewPending;
    }

    /** Подтвердить покупку примерки (оружие остаётся бесплатным). */
    public PurchaseResult confirmOutfitPreview() {
        if (!outfitPreviewPending) {
            return PurchaseResult.fail(DukeLines.purchaseFailGeneric());
        }
        if (outfitPreviewPrice > 0 && !trySpend(outfitPreviewPrice)) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
        if (outfitPreviewSet != null) {
            recordPurchase(outfitPreviewSet.getName());
        } else {
            for (Armour piece : outfitPreviewPieces) {
                recordPurchase(piece.getName());
            }
        }
        if (outfitPreviewWeapon != null) {
            recordPurchase(outfitPreviewWeapon.title());
        }
        clearOutfitPreviewFlags();
        return PurchaseResult.ok(DukeLines.dukeOutfitKept());
    }

    /** Снять примерку и вернуть витрину (кроны не трогали). */
    public void cancelOutfitPreview() {
        if (!outfitPreviewPending && outfitPreviewPieces.isEmpty() && outfitPreviewWeapon == null) {
            return;
        }
        for (Armour piece : outfitPreviewPieces) {
            EquipSlot slot = EquipSlot.forArmour(piece);
            if (slot != null && equipped.get(slot) == piece) {
                equipped.remove(slot);
            }
            playerInventory.remove(piece);
            soldArmor.remove(piece);
        }
        if (outfitPreviewSet != null) {
            soldSets.remove(outfitPreviewSet);
        }
        if (equippedWeapon != null && equippedWeapon == outfitPreviewWeapon) {
            equippedWeapon = null;
        }
        clearOutfitPreviewFlags();
    }

    private void clearOutfitPreviewFlags() {
        outfitPreviewPending = false;
        outfitPreviewPrice = 0;
        outfitPreviewSet = null;
        outfitPreviewPieces.clear();
        outfitPreviewWeapon = null;
    }

    /** @deprecated используйте {@link #beginOutfitPreview(int)} + confirm/cancel. */
    public PurchaseResult autoOutfitForBudget(int budget) {
        PurchaseResult preview = beginOutfitPreview(budget);
        if (!preview.success()) {
            return preview;
        }
        return confirmOutfitPreview();
    }

    /** Экипирует все части купленного комплекта (кнопка по эмблеме). */
    public void equipSet(ArmourSet set) {
        if (set == null || !soldSets.contains(set)) {
            return;
        }
        for (Armour piece : set.getArmorPieces()) {
            if (playerInventory.contains(piece)) {
                EquipSlot slot = EquipSlot.forArmour(piece);
                if (slot != null) {
                    equipped.put(slot, piece);
                }
            }
        }
    }

    public boolean isSetEquipped(ArmourSet set) {
        if (set == null) {
            return false;
        }
        for (Armour piece : set.getArmorPieces()) {
            EquipSlot slot = EquipSlot.forArmour(piece);
            if (slot == null || equipped.get(slot) != piece) {
                return false;
            }
        }
        return !set.getArmorPieces().isEmpty();
    }

    public List<ArmourSet> ownedSets() {
        return List.copyOf(soldSets);
    }

    public void unequip(EquipSlot slot) {
        equipped.remove(slot);
    }

    public ShopGearStats equippedGearStats() {
        ShopGearStats stats = potionAdjustedBase();
        for (Armour armour : equipped.values()) {
            stats = stats.plus(ShopGearRules.bonusFromArmour(armour));
        }
        if (equippedWeapon != null) {
            stats = stats.plus(ShopGearRules.placeholderBonus(equippedWeapon.title()));
        }
        return stats.clamped();
    }

    /** Исходные статы + уже выпитые зелья — новая база для экипировки. */
    private ShopGearStats potionAdjustedBase() {
        ShopGearStats stats = baseGearStats();
        for (String potion : drunkPotions) {
            stats = stats.plus(ShopGearRules.placeholderBonus(potion));
        }
        return stats.clamped();
    }

    public StatPreview equippedStatPreview() {
        return toPreview(potionAdjustedBase(), equippedGearStats());
    }

    /** Краткая строка бонусов для тултипа экипировки. */
    public String armourBonusLine(Armour armour) {
        if (armour == null) {
            return "";
        }
        ShopGearStats bonus = ShopGearRules.bonusFromArmour(armour);
        return String.format("Защита +%d  ·  Выносл. %+d  ·  Знаки %+d",
            bonus.protection(), bonus.stamina(), bonus.signs());
    }

    private void recordPurchase(String label) {
        purchasedLabels.add(label);
    }

    public PurchaseResult purchase(ShopCatalogEntry entry) {
        return purchase(entry, null);
    }

    public PurchaseResult purchase(ShopCatalogEntry entry, ShopCategory shelfCategory) {
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
            return purchasePlaceholder(entry, shelfCategory);
        }
        return PurchaseResult.fail("Этого у меня уже нет на полке.");
    }

    private PurchaseResult purchaseArmor(ShopCatalogEntry entry) {
        Armour armour = entry.armour;
        if (soldArmor.contains(armour)) {
            return PurchaseResult.fail(DukeLines.purchaseFailSold());
        }
        int price = entry.price;
        if (!trySpend(price)) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
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
        if (!trySpend(price)) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
        soldSets.add(set);
        playerInventory.addAll(set.getArmorPieces());
        recordPurchase(set.getName());
        return PurchaseResult.ok(DukeLines.purchaseOk(set.getName(), price));
    }

    private PurchaseResult purchasePlaceholder(ShopCatalogEntry entry, ShopCategory shelfCategory) {
        if (!trySpend(entry.price)) {
            return PurchaseResult.fail(DukeLines.purchaseFailMoney());
        }
        if (shelfCategory == ShopCategory.POTION || shelfCategory == ShopCategory.WEAPON) {
            String[] lines = shelfCategory == ShopCategory.POTION
                ? potionEffectLines(entry.name)
                : statLinesForCategory(shelfCategory);
            ShopInventorySlot bought = ShopInventorySlot.consumable(entry.name, shelfCategory, lines);
            pouchConsumables.add(bought);
            recordPurchase(entry.name);
        } else {
            recordPurchase(entry.name);
        }
        return PurchaseResult.ok(DukeLines.purchaseOk(entry.name, entry.price));
    }

    private List<ShopCatalogEntry> armorByType(Class<? extends Armour> type) {
        List<ShopCatalogEntry> out = new ArrayList<>();
        Set<Integer> usedPrices = new HashSet<>();
        for (Armour armour : armourRepository.getAllArmor()) {
            if (!type.isInstance(armour) || soldArmor.contains(armour)) {
                continue;
            }
            int price = ShopPricing.uniqueArmorPrice(armour, usedPrices);
            out.add(ShopCatalogEntry.fromArmour(armour, price));
        }
        out.sort(Comparator.comparingInt(e -> e.price));
        return out;
    }

    private List<ShopCatalogEntry> setsCatalog() {
        List<ShopCatalogEntry> out = new ArrayList<>();
        for (NonSchoolSet set : setService.getNonSchoolSets()) {
            if (!isShopRegionalKit(set) || soldSets.contains(set)) {
                continue;
            }
            int price = ShopPricing.setPrice(set);
            out.add(ShopCatalogEntry.fromSet(set, price));
        }
        out.sort(Comparator.comparingInt(e -> e.price));
        return out;
    }

    /** Четыре региональных комплекта витрины (эмблемы + kits/). */
    private static boolean isShopRegionalKit(ArmourSet set) {
        return set instanceof WhiteTigerSet
            || set instanceof TouissantSet
            || set instanceof BeauclaireGuardSet
            || set instanceof TemerianKitSet;
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
