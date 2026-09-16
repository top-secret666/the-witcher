package main.java.com.witcher.ui.shop.presenter;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.sets.ArmourSet;
import main.java.com.witcher.chapter1.shop.Chapter1ShopBridge;
import main.java.com.witcher.ui.shop.EquipmentArmourList;
import main.java.com.witcher.ui.shop.EquipmentFilter;
import main.java.com.witcher.ui.shop.EquipmentGridEntry;
import main.java.com.witcher.ui.shop.DukeLines;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEntryIcons;
import main.java.com.witcher.shop.EquipSlot;
import main.java.com.witcher.ui.shop.ShopInventoryKind;
import main.java.com.witcher.ui.shop.ShopInventorySlot;
import main.java.com.witcher.ui.shop.ShopGearRules;
import main.java.com.witcher.ui.shop.ShopGearStats;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.ShopRuntimeAssets;
import main.java.com.witcher.ui.shop.view.ShopLayout;
import main.java.com.witcher.ui.shop.view.ShopUiMetrics;
import main.java.com.witcher.ui.shop.view.ShopViewConstants;
import main.java.com.witcher.ui.shop.view.ShopShowcaseItem;
import main.java.com.witcher.ui.shop.view.anim.ShopCategoryAnimator;
import main.java.com.witcher.ui.shop.view.anim.ShopRevealAnimator;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** MVP presenter для экрана лавки — вся логика ввода, состояний и анимаций. */
public final class ShopPresenter {

    private static final ShopCategory[] GRID_CATEGORIES = {
        ShopCategory.CHEST, ShopCategory.LEGS, ShopCategory.GLOVES,
        ShopCategory.BOOTS, ShopCategory.POTION, ShopCategory.SETS, ShopCategory.WEAPON
    };

    private static final String WELCOME_LINE = DukeLines.WELCOME;

    private static final String IDLE_LINE = DukeLines.IDLE;

    /** После стольких открытий категорий без покупок — предложение подобрать броню. */
    private static final int ARMOR_HELP_AFTER_EMPTY_CATEGORY_OPENS = 3;

    private final ShopModel model;
    private final ShopSessionState ui = new ShopSessionState();
    private final ShopRuntimeAssets assets;
    private final ShopUiMetrics metrics;
    private final ShopEntryIcons armourIcons;
    private final Chapter1ShopBridge chapterBridge;
    private Runnable battleCardRevealComplete;
    /** Esc закрыл инвентарь/категорию/reveal — не открывать паузу в этом кадре. */
    private boolean escConsumedByUi;

    public ShopPresenter(ShopModel model, ShopRuntimeAssets assets, ShopEntryIcons armourIcons) {
        this(model, assets, armourIcons, null);
    }

    public ShopPresenter(ShopModel model, ShopRuntimeAssets assets, ShopEntryIcons armourIcons,
                         Chapter1ShopBridge chapterBridge) {
        this.model = model;
        this.assets = assets;
        this.metrics = assets;
        this.armourIcons = armourIcons;
        this.chapterBridge = chapterBridge;
        initShowcaseFromModel();
        ui.currentDialog = WELCOME_LINE;
    }

    public void update(ShopInput input) {
        ui.tick++;
        escConsumedByUi = false;

        if (input.escPressed()) {
            if (ui.battleConfirmActive) {
                declineBattleConfirm();
                escConsumedByUi = true;
                return;
            }
            if (ui.outfitConfirmActive) {
                declineOutfitPurchase();
                escConsumedByUi = true;
                return;
            }
            if (ui.equipmentOpen) {
                ui.equipmentOpen = false;
                ui.inventoryOpen = true;
                ui.equipmentAttention = false;
                refreshToBattleAttention();
                promoteBattleCardIconAttention(inventorySlots());
                escConsumedByUi = true;
                return;
            }
            if (ui.inventoryOpen) {
                closeInventory();
                escConsumedByUi = true;
                return;
            }
            // Кошелёк / карта / покупка: Esc не ускоряет анимацию reveal.
            if (ui.state == ShopScreenState.WALLET_REVEAL
                || ui.state == ShopScreenState.BATTLE_CARD_REVEAL
                || ui.state == ShopScreenState.PURCHASE_REVEAL) {
                // Не return до tickTimedScenes — иначе Esc «замораживает» fly-in.
                tickTimedScenes();
                escConsumedByUi = true;
                return;
            }
            if (ui.state == ShopScreenState.CATEGORY || ui.state == ShopScreenState.CATEGORY_OPENING) {
                beginCategoryClose();
                escConsumedByUi = true;
                return;
            }
            if (ui.state == ShopScreenState.CATEGORY_CLOSING) {
                escConsumedByUi = true;
                return;
            }
            ui.pauseRequested = true;
            return;
        }

        ShopScreenState stateBeforeTick = ui.state;
        tickTimedScenes();
        updateAshParticles();

        // Если reveal только что сам доиграл, клик этого кадра не уходит в каталог/покупку.
        boolean revealJustFinished = input.clicked()
            && (stateBeforeTick == ShopScreenState.PURCHASE_REVEAL
                || stateBeforeTick == ShopScreenState.BATTLE_CARD_REVEAL
                || stateBeforeTick == ShopScreenState.WALLET_REVEAL)
            && ui.state != stateBeforeTick;
        if (revealJustFinished) {
            return;
        }

        ShopRevealAnimator reveal = revealAnimator();
        boolean showcaseInteractive = reveal.uiInteractive && ui.state == ShopScreenState.IDLE;

        ShopLayout layout = createLayout();

        // Reveal кошелька/карты нельзя пропустить кликом — анимация доигрывает.
        if (ui.state == ShopScreenState.WALLET_REVEAL) {
            return;
        }
        if (ui.state == ShopScreenState.BATTLE_CARD_REVEAL) {
            return;
        }

        if (ui.state == ShopScreenState.PURCHASE_REVEAL && input.clicked()) {
            if (ui.purchaseRevealCount >= 2) {
                finishPurchaseReveal();
            }
            return;
        }

        if (ui.armorHelpActive) {
            updateArmorHelpInput(input.mouseX(), input.mouseY(), input.clicked());
            return;
        }
        if (ui.outfitConfirmActive) {
            updateOutfitConfirmInput(input.mouseX(), input.mouseY(), input.clicked());
            return;
        }
        if (ui.battleConfirmActive) {
            updateBattleConfirmInput(input.mouseX(), input.mouseY(), input.clicked());
            return;
        }

        if ((ui.state == ShopScreenState.CATEGORY_OPENING || ui.state == ShopScreenState.CATEGORY_CLOSING)
            && input.clicked()) {
            skipCategoryAnimation();
            return;
        }

        boolean bagUnlocked = !model.needsWalletReveal()
            && ui.state != ShopScreenState.WALLET_REVEAL
            && ui.state != ShopScreenState.BATTLE_CARD_REVEAL
            && ui.state != ShopScreenState.PURCHASE_REVEAL;
        if (bagUnlocked) {
            updateInventoryInput(input.mouseX(), input.mouseY(), input.clicked(), input.wheelNotches());
        } else {
            ui.inventoryBagBounds.setBounds(0, 0, 0, 0);
            ui.inventoryBagHovered = false;
        }

        if (ui.inventoryOpen || ui.equipmentOpen) {
            ui.hoveredIndex = -1;
            ui.hoveredRowIndex = -1;
            ui.categoryBuyHovered = false;
            ui.categoryBackHovered = false;
            ui.inventoryBagHovered = false;
            return;
        }

        ui.hoveredIndex = -1;
        ui.hoveredRowIndex = -1;
        ui.categoryBuyHovered = false;
        ui.categoryBackHovered = false;

        if (showcaseInteractive) {
            for (int i = 0; i < ui.showcaseItems.size(); i++) {
                if (ui.showcaseItems.get(i).bounds.contains(input.mouseX(), input.mouseY())) {
                    ui.hoveredIndex = i;
                    break;
                }
            }
        }

        if (ui.state == ShopScreenState.CATEGORY || ui.state == ShopScreenState.CATEGORY_OPENING
            || ui.state == ShopScreenState.CATEGORY_CLOSING) {
            ShopCategoryAnimator catAnim = categoryAnimator(layout);
            if (input.wheelNotches() != 0 && catAnim.listInteractive) {
                Rectangle panel = layout.detailListPanelSlot(metrics.detailPanelW(), metrics.detailPanelH());
                scrollCatalogBy(panel.y, input.wheelNotches());
            }
            if (catAnim.listInteractive) {
                for (int i = 0; i < ui.catalogEntries.size(); i++) {
                    if (ui.catalogEntries.get(i).bounds.contains(input.mouseX(), input.mouseY())) {
                        ui.hoveredRowIndex = i;
                        break;
                    }
                }
            }
        }

        if (showcaseInteractive && input.clicked() && ui.hoveredIndex >= 0) {
            ui.selectedIndex = ui.hoveredIndex;
            ShopShowcaseItem item = ui.showcaseItems.get(ui.hoveredIndex);
            ui.currentDialog = item.dukeLine;
            ui.categoryClosing = false;
            ui.categoryTicks = 0;
            Point slot = layout.cardSlot(ui.hoveredIndex);
            ui.categoryFromRect.setBounds(slot.x, slot.y, layout.cardW, layout.cardH);
            buildCatalogRows(item);
            if (model.inventoryItemCount() == 0 && !ui.armorHelpActive) {
                ui.armorHelpEmptyCategoryCount++;
            }
            // Деньги/кошелёк появляются после клика по карточке категории, не на общей заставке.
            if (model.needsWalletReveal()) {
                beginWalletReveal(true);
            } else {
                ui.state = ShopScreenState.CATEGORY_OPENING;
            }
        }

        if (ui.state == ShopScreenState.CATEGORY && input.clicked()) {
            ShopCategoryAnimator cat = categoryAnimator(layout);
            if (ui.categoryBackBounds.contains(input.mouseX(), input.mouseY()) && cat.listInteractive) {
                beginCategoryClose();
            } else if (ui.categoryBuyBounds.contains(input.mouseX(), input.mouseY()) && isBuyButtonEnabled()) {
                tryPurchaseSelected();
            } else if (ui.hoveredRowIndex >= 0) {
                ui.selectedRowIndex = ui.hoveredRowIndex;
                ShopCatalogEntry row = ui.catalogEntries.get(ui.hoveredRowIndex);
                Rectangle panel = layout.detailListPanelSlot(metrics.detailPanelW(), metrics.detailPanelH());
                ensureRowVisible(panel.y, ui.selectedRowIndex);
                if (chapterBridge != null && ui.selectedIndex >= 0 && ui.selectedIndex < ui.showcaseItems.size()) {
                    ShopCategory showcaseCategory = ui.showcaseItems.get(ui.selectedIndex).category;
                    ui.currentDialog = chapterBridge.inspectCatalogRow(row, showcaseCategory);
                } else {
                    ui.currentDialog = DukeLines.rowInspect(row.name, row.price);
                }
            }
        }

        if (ui.state == ShopScreenState.CATEGORY && ui.categoryBuyBounds.width > 0) {
            ui.categoryBuyHovered = ui.categoryBuyBounds.contains(input.mouseX(), input.mouseY());
        }
        if (ui.state == ShopScreenState.CATEGORY && ui.categoryBackBounds.width > 0) {
            ui.categoryBackHovered = ui.categoryBackBounds.contains(input.mouseX(), input.mouseY());
        }

        if (bagUnlocked) {
            ui.inventoryBagHovered = ui.inventoryBagBounds.contains(input.mouseX(), input.mouseY());
            if (ui.inventoryAttention && ui.inventoryBagHovered) {
                ui.inventoryAttention = false;
            }
        }
    }

    private void clearInventoryAttentionOnHover(int mouseX, int mouseY, List<ShopInventorySlot> slots) {
        if (!ui.inventoryOpen) {
            return;
        }
        if (ui.equipmentAttention
            && ui.inventoryGearButtonBounds.width > 0
            && ui.inventoryGearButtonBounds.contains(mouseX, mouseY)) {
            // Только гасим «Надеть» — карта замигает после выхода из экрана экипировки.
            ui.equipmentAttention = false;
            return;
        }
        if (ui.potionDrinkAttention
            && ui.inventoryEquipButtonBounds.width > 0
            && ui.inventoryEquipButtonBounds.contains(mouseX, mouseY)) {
            ui.potionDrinkAttention = false;
            return;
        }
        if (ui.battleCardOpenAttention
            && ui.inventoryEquipButtonBounds.width > 0
            && ui.inventoryEquipButtonBounds.contains(mouseX, mouseY)) {
            ui.battleCardOpenAttention = false;
            return;
        }
        if (ui.battleCardAttention
            && ui.inventoryHoveredIndex >= 0
            && ui.inventoryHoveredIndex < slots.size()
            && slots.get(ui.inventoryHoveredIndex).kind() == ShopInventoryKind.BATTLE_CARD) {
            // Кликнули/навели на иконку карты → дальше мигает только «Открыть».
            ui.battleCardAttention = false;
            ui.battleCardOpenAttention = true;
            ui.inventoryFocusedIndex = ui.inventoryHoveredIndex;
        }
    }

    private void focusBattleCardSlot(List<ShopInventorySlot> slots) {
        for (int i = 0; i < slots.size(); i++) {
            if (slots.get(i).kind() == ShopInventoryKind.BATTLE_CARD) {
                ui.inventoryFocusedIndex = i;
                return;
            }
        }
    }

    public ShopSessionState ui() {
        return ui;
    }

    public ShopModel model() {
        return model;
    }

    public ShopRuntimeAssets runtimeAssets() {
        return assets;
    }

    public ShopEntryIcons armourIcons() {
        return armourIcons;
    }

    public List<ShopInventorySlot> inventorySlots() {
        List<ShopInventorySlot> slots = new ArrayList<>();
        if (!model.needsWalletReveal()) {
            slots.add(ShopInventorySlot.wallet(model));
        }
        if (chapterBridge != null && chapterBridge.battleCardInInventory()) {
            slots.add(ShopInventorySlot.battleCard());
        }
        List<ShopInventorySlot> potions = new ArrayList<>();
        List<ShopInventorySlot> weapons = new ArrayList<>();
        for (ShopInventorySlot pouch : model.pouchConsumables()) {
            if (pouch.kind() == ShopInventoryKind.WEAPON) {
                weapons.add(ShopInventorySlot.weapon(pouch.title(), pouch.detailLines(), false));
            } else {
                potions.add(pouch);
            }
        }
        slots.addAll(potions);
        slots.addAll(weapons);
        ShopInventorySlot wornWeapon = model.getEquippedWeapon();
        if (wornWeapon != null) {
            slots.add(ShopInventorySlot.weapon(wornWeapon.title(), wornWeapon.detailLines(), true));
        }
        java.util.Set<Armour> kitPieces = new java.util.HashSet<>();
        for (ArmourSet set : model.ownedSets()) {
            slots.add(ShopInventorySlot.set(set, model.isSetEquipped(set)));
            kitPieces.addAll(set.getArmorPieces());
        }
        for (Armour armour : model.ownedArmour()) {
            if (!kitPieces.contains(armour)) {
                slots.add(ShopInventorySlot.armour(armour, model.isEquipped(armour)));
            }
        }
        return slots;
    }

    public ShopLayout createLayout() {
        return new ShopLayout(VIRTUAL_W, VIRTUAL_H, ui.showcaseItems.size(),
            metrics.hudX(), metrics.hudW(), metrics.hudH(), metrics.panelW(),
            metrics.panelHeaderH(), metrics.topRowCols(), metrics.bottomRowCols());
    }

    public ShopRevealAnimator revealAnimator() {
        return switch (ui.state) {
            case REVEAL -> ShopRevealAnimator.forProgress(
                ui.revealTicks / (float) REVEAL_DURATION_TICKS, ui.showcaseItems.size(), true);
            case WALLET_REVEAL, PURCHASE_REVEAL, BATTLE_CARD_REVEAL -> ShopRevealAnimator.complete(ui.showcaseItems.size());
            default -> ShopRevealAnimator.complete(ui.showcaseItems.size());
        };
    }

    public ShopCategoryAnimator categoryAnimator(ShopLayout layout) {
        Rectangle to = layout.leftCategoryCardSlot();
        float t = categoryAnimProgress();
        if (t >= 1f && !ui.categoryClosing) {
            return ShopCategoryAnimator.open(to.x, to.y, to.width, to.height);
        }
        return ShopCategoryAnimator.opening(t,
            ui.categoryFromRect.x, ui.categoryFromRect.y, ui.categoryFromRect.width, ui.categoryFromRect.height,
            to.x, to.y, to.width, to.height);
    }

    public String walletHudAmountText() {
        if (ui.state != ShopScreenState.WALLET_REVEAL) {
            return model.walletAmountText();
        }
        int countStart = WALLET_APPEAR_TICKS + WALLET_FLY_TICKS + WALLET_BAG_CLOSE_TICKS;
        if (ui.walletRevealTicks < countStart) {
            return "???";
        }
        if (WALLET_COUNT_TICKS <= 0) {
            return model.walletAmountText();
        }
        float t = Math.min(1f, (ui.walletRevealTicks - countStart) / (float) WALLET_COUNT_TICKS);
        t = t * t * (3f - 2f * t);
        return String.valueOf(Math.round(model.getWallet() * t));
    }

    public boolean isCategoryMode() {
        return ui.state == ShopScreenState.CATEGORY_OPENING
            || ui.state == ShopScreenState.CATEGORY
            || ui.state == ShopScreenState.CATEGORY_CLOSING;
    }

    public boolean isInventoryOpen() {
        return ui.inventoryOpen;
    }

    public boolean isEquipmentOpen() {
        return ui.equipmentOpen;
    }

    public boolean isChapterEventIdle() {
        return ui.state == ShopScreenState.IDLE || ui.state == ShopScreenState.CATEGORY;
    }

    /**
     * Только таймеры reveal (покупка / кошелёк / карта / категория).
     * Нужен, когда глава показывает VN поверх лавки и не вызывает полный {@link #update}.
     */
    public void tickTimedScenes() {
        if (ui.state == ShopScreenState.REVEAL) {
            ui.revealTicks++;
            if (ui.revealTicks >= REVEAL_DURATION_TICKS) {
                ui.state = ShopScreenState.IDLE;
                // Приветствие остаётся — игрок успевает дочитать.
                ui.currentDialog = WELCOME_LINE;
            }
        }

        if (ui.state == ShopScreenState.WALLET_REVEAL) {
            ui.walletRevealTicks++;
            if (ui.walletRevealTicks >= WALLET_REVEAL_TOTAL) {
                finishWalletReveal();
            }
        }

        if (ui.state == ShopScreenState.BATTLE_CARD_REVEAL) {
            ui.battleCardRevealTicks++;
            if (ui.battleCardRevealTicks >= BATTLE_CARD_REVEAL_TOTAL) {
                finishBattleCardReveal();
            }
        }

        if (ui.state == ShopScreenState.PURCHASE_REVEAL) {
            ui.purchaseRevealTicks++;
            // Завершаем сразу по концу fly+fade, без мёртвой паузы и без клика.
            if (ui.purchaseRevealTicks >= PURCHASE_REVEAL_TOTAL) {
                finishPurchaseReveal();
            }
        }

        if (ui.state == ShopScreenState.CATEGORY_OPENING || ui.state == ShopScreenState.CATEGORY_CLOSING) {
            ui.categoryTicks++;
            if (ui.categoryClosing) {
                if (ui.categoryTicks >= CATEGORY_OPEN_DURATION_TICKS) {
                    finishCategoryClose();
                }
            } else if (ui.categoryTicks >= CATEGORY_OPEN_DURATION_TICKS) {
                ui.state = ShopScreenState.CATEGORY;
            }
        }
    }

    public ShopCatalogEntry selectedCatalogEntry() {
        if (ui.selectedRowIndex >= 0 && ui.selectedRowIndex < ui.catalogEntries.size()) {
            return ui.catalogEntries.get(ui.selectedRowIndex);
        }
        return null;
    }

    public String selectedCatalogPrice() {
        if (ui.selectedRowIndex >= 0 && ui.selectedRowIndex < ui.catalogEntries.size()) {
            return ui.catalogEntries.get(ui.selectedRowIndex).priceLabel();
        }
        if (ui.selectedIndex >= 0 && ui.selectedIndex < ui.showcaseItems.size()) {
            return ui.showcaseItems.get(ui.selectedIndex).priceLabel;
        }
        return "···";
    }

    public int catalogRowStep() {
        return metrics.rowH() + 4;
    }

    public int catalogListTop(int panelY) {
        return panelY + ShopViewConstants.catalogListTopInset(metrics.detailPanelH());
    }

    public int catalogListBottom(int panelY) {
        return ShopViewConstants.catalogListBottomY(panelY, metrics.detailPanelH(), metrics.btnH());
    }

    public int categoryStatLegendY(int panelY) {
        return ShopViewConstants.categoryStatLegendY(panelY, metrics.detailPanelH(), metrics.btnH());
    }

    public int catalogRowContentW() {
        return ShopViewConstants.catalogRowContentW(metrics.detailPanelW());
    }

    public int catalogRowX(int panelX) {
        return ShopViewConstants.catalogRowX(panelX, metrics.detailPanelW());
    }

    public int categoryBuyButtonY(int panelY) {
        return ShopViewConstants.categoryBuyButtonY(panelY, metrics.detailPanelH(), metrics.btnH());
    }

    public int maxCatalogScroll(int panelY) {
        int visible = catalogListBottom(panelY) - catalogListTop(panelY);
        int content = ui.catalogEntries.size() * catalogRowStep() - 4;
        return Math.max(0, content - visible);
    }

    public boolean isBuyButtonEnabled() {
        if (ui.selectedRowIndex < 0 || ui.selectedRowIndex >= ui.catalogEntries.size()) {
            return false;
        }
        return model.canPurchase(ui.catalogEntries.get(ui.selectedRowIndex));
    }

    public int[] catalogStatDeltas(ShopCatalogEntry row) {
        ShopModel.StatPreview preview = model.statPreview(row);
        if (preview.rows().length < 3) {
            return new int[]{0, 0, 0};
        }
        return new int[]{
            preview.rows()[0].delta(),
            preview.rows()[1].delta(),
            preview.rows()[2].delta()
        };
    }

    public BufferedImage itemArtForEntry(ShopCatalogEntry entry, ShopShowcaseItem categoryItem) {
        if (categoryItem == null) {
            return null;
        }
        if (entry != null) {
            BufferedImage icon = armourIcons.iconForEntry(entry, categoryItem.category);
            if (icon != null) {
                return icon;
            }
        }
        return categoryItem.cardArt != null ? categoryItem.cardArt : categoryItem.icon;
    }

    public boolean exitRequested() {
        return ui.exitRequested;
    }

    public void clearExitRequest() {
        ui.exitRequested = false;
    }

    public boolean pauseRequested() {
        return ui.pauseRequested;
    }

    public void clearPauseRequest() {
        ui.pauseRequested = false;
    }

    public boolean escConsumedByUi() {
        return escConsumedByUi;
    }

    /** Кнопка «В БОЙ»: только после надетой брони/оружия или выпитого зелья; карта ещё не выдана. */
    public boolean canShowToBattleButton() {
        boolean ready = model.hasAnyEquippedItem() || model.hasDrunkAnyPotion();
        return ready
            && (chapterBridge == null || !chapterBridge.battleCardInInventory());
    }

    private void refreshToBattleAttention() {
        // Мигает только после надетой брони/оружия; одно зелье — кнопка есть, но без пульса.
        ui.toBattleAttention = canShowToBattleButton() && model.hasAnyEquippedItem();
    }

    private void beginBattleConfirm() {
        if (!canShowToBattleButton() || chapterBridge == null) {
            return;
        }
        if (!model.hasAnyEquippedItem() && !model.hasDrunkAnyPotion()) {
            ui.currentDialog = DukeLines.equipBeforeBattle();
            ui.dialogSpeaker = "Герцог";
            return;
        }
        ui.battleConfirmActive = true;
        ui.toBattleAttention = false;
        ui.armorHelpHovered = -1;
        ui.currentDialog = DukeLines.battleConfirm();
        ui.dialogSpeaker = "Герцог";
    }

    private void updateBattleConfirmInput(int mx, int my, boolean clicked) {
        layoutYesNoChoices();
        ui.armorHelpHovered = main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.hitIndex(
            ui.armorHelpChoiceBounds, mx, my);
        if (!clicked || ui.armorHelpHovered < 0) {
            return;
        }
        if (ui.armorHelpHovered == 0) {
            confirmBattleDepart();
        } else {
            declineBattleConfirm();
        }
    }

    private void confirmBattleDepart() {
        ui.battleConfirmActive = false;
        ui.armorHelpChoiceBounds.clear();
        ui.armorHelpHovered = -1;
        ui.equipmentOpen = false;
        ui.inventoryOpen = false;
        if (chapterBridge != null) {
            chapterBridge.requestQuestBriefing();
        }
    }

    private void declineBattleConfirm() {
        ui.battleConfirmActive = false;
        ui.armorHelpChoiceBounds.clear();
        ui.armorHelpHovered = -1;
        ui.currentDialog = IDLE_LINE;
        ui.dialogSpeaker = "Герцог";
        refreshToBattleAttention();
    }

    public Point inventoryBagSlot() {
        int bagX = INVENTORY_BAG_MARGIN;
        int bagY = INVENTORY_BAG_MARGIN;
        ui.inventoryBagBounds.setBounds(bagX, bagY, INVENTORY_BAG_SIZE, INVENTORY_BAG_SIZE);
        return new Point(bagX, bagY);
    }

    private void initShowcaseFromModel() {
        ui.showcaseItems.clear();
        for (ShopCategory cat : GRID_CATEGORIES) {
            ShopShowcaseItem.Kind kind = cat == ShopCategory.SETS ? ShopShowcaseItem.Kind.SET_CATALOG : ShopShowcaseItem.Kind.PIECE;
            BufferedImage icon = assets.iconForCategory(cat);
            ui.showcaseItems.add(new ShopShowcaseItem(
                kind, cat,
                model.priceLabelForCategory(cat),
                model.dukeLineForCategory(cat),
                model.statLinesForCategory(cat),
                icon, icon));
        }
    }

    private void refreshShowcasePrices() {
        for (ShopShowcaseItem item : ui.showcaseItems) {
            item.priceLabel = model.priceLabelForCategory(item.category);
        }
    }

    private void buildCatalogRows(ShopShowcaseItem category) {
        ui.catalogEntries.clear();
        ui.hoveredRowIndex = -1;
        ui.catalogScrollOffset = 0;
        ui.catalogEntries.addAll(model.getCatalog(category.category));
        ui.selectedRowIndex = ui.catalogEntries.isEmpty() ? -1 : 0;
    }

    private void scrollCatalogBy(int panelY, int wheelNotches) {
        ui.catalogScrollOffset += wheelNotches * catalogRowStep();
        ui.catalogScrollOffset = Math.max(0, Math.min(maxCatalogScroll(panelY), ui.catalogScrollOffset));
    }

    private void ensureRowVisible(int panelY, int rowIndex) {
        if (rowIndex < 0) {
            return;
        }
        int listTop = catalogListTop(panelY);
        int listBottom = catalogListBottom(panelY);
        int rowY = listTop + rowIndex * catalogRowStep() - ui.catalogScrollOffset;
        int rowBottom = rowY + metrics.rowH();
        if (rowY < listTop) {
            ui.catalogScrollOffset -= listTop - rowY;
        } else if (rowBottom > listBottom) {
            ui.catalogScrollOffset += rowBottom - listBottom;
        }
        ui.catalogScrollOffset = Math.max(0, Math.min(maxCatalogScroll(panelY), ui.catalogScrollOffset));
    }

    private void updateInventoryInput(int mouseX, int mouseY, boolean clicked, int wheelNotches) {
        inventoryBagSlot();
        if (ui.equipmentOpen) {
            updateEquipmentInput(mouseX, mouseY, clicked);
            return;
        }
        List<ShopInventorySlot> slots = inventorySlots();
        if (ui.inventoryOpen) {
            if (wheelNotches != 0) {
                scrollInventoryGrids(mouseX, mouseY, wheelNotches, slots);
            }
            ui.inventoryHoveredIndex = -1;
            for (int i = 0; i < ui.inventorySlotBounds.size(); i++) {
                Rectangle bounds = ui.inventorySlotBounds.get(i);
                if (bounds.width > 0 && bounds.height > 0 && bounds.contains(mouseX, mouseY)) {
                    ui.inventoryHoveredIndex = i;
                    break;
                }
            }
            ui.inventoryPouchIconHovered = ui.inventoryHoveredIndex >= 0;
            ui.inventoryCloseHovered = ui.inventoryCloseBounds.contains(mouseX, mouseY);
            ui.toBattleHovered = canShowToBattleButton()
                && ui.toBattleButtonBounds.width > 0
                && ui.toBattleButtonBounds.contains(mouseX, mouseY);
            clearInventoryAttentionOnHover(mouseX, mouseY, slots);
            if (clicked) {
                if (ui.inventoryCloseBounds.contains(mouseX, mouseY)) {
                    closeInventory();
                } else if (ui.toBattleHovered) {
                    beginBattleConfirm();
                } else if (ui.inventoryEquipButtonBounds.width > 0
                    && ui.inventoryEquipButtonBounds.contains(mouseX, mouseY)) {
                    handleInventoryAction(slots);
                } else if (ui.inventoryGearButtonBounds.width > 0
                    && ui.inventoryGearButtonBounds.contains(mouseX, mouseY)) {
                    // Только переход в экипировку — надевает игрок сам, не автоматом.
                    openEquipmentFromInventory();
                } else if (ui.inventoryHoveredIndex >= 0) {
                    ui.inventoryFocusedIndex = ui.inventoryHoveredIndex;
                    if (ui.battleCardAttention
                        && slots.get(ui.inventoryHoveredIndex).kind() == ShopInventoryKind.BATTLE_CARD) {
                        ui.battleCardAttention = false;
                        ui.battleCardOpenAttention = true;
                    }
                } else if (!ui.inventoryPanelBounds.contains(mouseX, mouseY)) {
                    closeInventory();
                }
            }
        } else if (clicked && ui.inventoryBagBounds.contains(mouseX, mouseY)) {
            openInventoryAttentionCleared(slots);
        }
    }

    private void openInventoryAttentionCleared(List<ShopInventorySlot> slots) {
        ui.inventoryOpen = true;
        ui.inventoryAttention = false;
        ui.inventorySpecialScroll = 0;
        ui.inventoryArmourScroll = 0;
        boolean hasCard = ui.battleCardAttentionPending
            || (chapterBridge != null && chapterBridge.battleCardInInventory());

        // После покупки брони/оружия — мигает «Надеть».
        if (ui.guideToEquipment) {
            ui.guideToEquipment = false;
            ui.equipmentAttention = true;
            ui.potionDrinkAttention = false;
            ui.battleCardAttention = false;
            ui.battleCardOpenAttention = false;
            focusLastPurchasedSlot(slots);
            return;
        }

        // После покупки зелья — мигает «Выпить».
        if (ui.potionDrinkAttention) {
            ui.equipmentAttention = false;
            ui.battleCardAttention = false;
            ui.battleCardOpenAttention = false;
            focusLastPurchasedSlot(slots);
            return;
        }

        // После брифинга — сразу карта / «Открыть».
        if (hasCard && ui.skipEquipmentGuide) {
            ui.skipEquipmentGuide = false;
            ui.battleCardAttentionPending = false;
            ui.equipmentAttention = false;
            ui.battleCardAttention = true;
            ui.battleCardOpenAttention = true;
            focusBattleCardSlot(slots);
            return;
        }

        // Сначала только «Экипировка»; карта — после визита в экран экипировки.
        if (hasCard && model.hasWearableGear()) {
            ui.equipmentAttention = true;
            ui.battleCardAttention = false;
            ui.battleCardOpenAttention = false;
            ui.battleCardAttentionPending = true;
            ui.inventoryFocusedIndex = Math.min(ui.inventoryFocusedIndex, Math.max(0, slots.size() - 1));
            return;
        }
        if (hasCard) {
            ui.battleCardAttentionPending = false;
            ui.equipmentAttention = false;
            ui.battleCardAttention = true;
            ui.battleCardOpenAttention = false;
            for (int i = 0; i < slots.size(); i++) {
                if (slots.get(i).kind() == ShopInventoryKind.BATTLE_CARD) {
                    ui.inventoryFocusedIndex = i;
                    return;
                }
            }
        }
        focusLastPurchasedSlot(slots);
    }

    private void focusLastPurchasedSlot(List<ShopInventorySlot> slots) {
        List<String> names = model.inventoryItemNames();
        if (slots.isEmpty()) {
            ui.inventoryFocusedIndex = 0;
            return;
        }
        if (!names.isEmpty()) {
            String last = names.get(names.size() - 1);
            for (int i = 0; i < slots.size(); i++) {
                if (last.equals(slots.get(i).title())) {
                    ui.inventoryFocusedIndex = i;
                    return;
                }
            }
        }
        ui.inventoryFocusedIndex = Math.min(ui.inventoryFocusedIndex, Math.max(0, slots.size() - 1));
    }

    /** После выхода из экипировки — мигает иконка карты (ещё не «Открыть»). */
    private void promoteBattleCardIconAttention(List<ShopInventorySlot> slots) {
        boolean hasCard = ui.battleCardAttentionPending
            || (chapterBridge != null && chapterBridge.battleCardInInventory());
        if (!hasCard) {
            return;
        }
        ui.battleCardAttentionPending = false;
        ui.battleCardAttention = true;
        ui.battleCardOpenAttention = false;
        focusBattleCardSlot(slots);
    }

    private void scrollInventoryGrids(int mouseX, int mouseY, int wheelNotches,
                                      List<ShopInventorySlot> slots) {
        int specialCount = 0;
        int armourCount = 0;
        for (ShopInventorySlot slot : slots) {
            if (slot.kind().isArmourGrid()) {
                armourCount++;
            } else {
                specialCount++;
            }
        }
        int cols = Math.max(1, INVENTORY_GRID_COLS);
        int specialRows = Math.max(1, (specialCount + cols - 1) / cols);
        int armourRows = Math.max(1, (armourCount + cols - 1) / cols);
        int maxSpecial = Math.max(0, specialRows - INVENTORY_SPECIAL_VISIBLE_ROWS);
        int maxArmour = Math.max(0, armourRows - INVENTORY_ARMOUR_VISIBLE_ROWS);

        // Колесо вниз (положительные notches у нас увеличивают offset каталога) — скроллим вниз.
        if (ui.inventorySpecialGridBounds.contains(mouseX, mouseY)) {
            ui.inventorySpecialScroll = Math.max(0, Math.min(maxSpecial,
                ui.inventorySpecialScroll + wheelNotches));
        } else if (ui.inventoryArmourGridBounds.contains(mouseX, mouseY)) {
            ui.inventoryArmourScroll = Math.max(0, Math.min(maxArmour,
                ui.inventoryArmourScroll + wheelNotches));
        } else if (ui.inventoryPanelBounds.contains(mouseX, mouseY)) {
            if (mouseY < ui.inventoryArmourGridBounds.y) {
                ui.inventorySpecialScroll = Math.max(0, Math.min(maxSpecial,
                    ui.inventorySpecialScroll + wheelNotches));
            } else {
                ui.inventoryArmourScroll = Math.max(0, Math.min(maxArmour,
                    ui.inventoryArmourScroll + wheelNotches));
            }
        }
    }

    private void handleInventoryAction(List<ShopInventorySlot> slots) {
        ShopInventorySlot focused = null;
        if (ui.inventoryFocusedIndex >= 0 && ui.inventoryFocusedIndex < slots.size()) {
            focused = slots.get(ui.inventoryFocusedIndex);
        }
        if (focused == null || !focused.kind().hasActionButton()) {
            return;
        }
        switch (focused.kind()) {
            case POTION -> {
                if (model.drinkPotion(focused.title())) {
                    ui.currentDialog = DukeLines.potionDrunk(focused.title());
                    ui.potionDrinkAttention = false;
                    if (chapterBridge != null) {
                        chapterBridge.onPotionDrunk();
                    }
                    List<ShopInventorySlot> after = inventorySlots();
                    ui.inventoryFocusedIndex = Math.min(ui.inventoryFocusedIndex,
                        Math.max(0, after.size() - 1));
                    refreshToBattleAttention();
                }
            }
            case BATTLE_CARD -> {
                if (chapterBridge != null) {
                    chapterBridge.useBattleCard();
                    ui.inventoryOpen = false;
                    ui.battleCardAttention = false;
                    ui.battleCardOpenAttention = false;
                    ui.battleCardAttentionPending = false;
                    ui.equipmentAttention = false;
                    ui.inventoryAttention = false;
                }
            }
            case ARMOUR -> {
                if (focused.armour() != null) {
                    model.equipArmour(focused.armour());
                }
                openEquipmentFromInventory();
                if (chapterBridge != null) {
                    chapterBridge.onEquip();
                }
            }
            case SET -> {
                if (focused.armourSet() != null) {
                    model.equipSet(focused.armourSet());
                }
                openEquipmentFromInventory();
                if (chapterBridge != null) {
                    chapterBridge.onEquip();
                }
            }
            case WEAPON -> {
                model.equipWeapon(focused);
                openEquipmentFromInventory();
                ui.equipmentFilter = EquipmentFilter.WEAPON;
                if (chapterBridge != null) {
                    chapterBridge.onEquip();
                }
            }
            case WALLET -> {
            }
        }
    }

    private void closeInventory() {
        if (!ui.inventoryOpen) {
            return;
        }
        ui.inventoryOpen = false;
        ui.inventoryFocusedIndex = 0;
        if (chapterBridge != null) {
            chapterBridge.onInventoryBack();
        }
    }

    private void equipFocusedWearable(List<ShopInventorySlot> slots) {
        if (ui.inventoryFocusedIndex < 0 || ui.inventoryFocusedIndex >= slots.size()) {
            return;
        }
        ShopInventorySlot focused = slots.get(ui.inventoryFocusedIndex);
        switch (focused.kind()) {
            case ARMOUR -> {
                if (focused.armour() != null) {
                    model.equipArmour(focused.armour());
                }
            }
            case SET -> {
                if (focused.armourSet() != null) {
                    model.equipSet(focused.armourSet());
                }
            }
            case WEAPON -> model.equipWeapon(focused);
            default -> {
                return;
            }
        }
        if (chapterBridge != null) {
            chapterBridge.onEquip();
        }
    }

    private void openEquipmentFromInventory() {
        ui.equipmentOpen = true;
        ui.inventoryOpen = false;
        ui.equipmentAttention = false;
        refreshToBattleAttention();
        // Карта ещё не мигает — только после возврата из экипировки (если уже выдана).
        if (ui.battleCardAttentionPending
            || (chapterBridge != null && chapterBridge.battleCardInInventory())) {
            ui.battleCardAttentionPending = true;
            ui.battleCardAttention = false;
            ui.battleCardOpenAttention = false;
        }
        ui.equipmentFilter = EquipmentFilter.ALL;
        ui.equipmentHoveredRow = -1;
        ui.equipmentHoveredSlot = -1;
        ui.equipmentHoveredFilter = -1;
    }

    private void updateEquipmentInput(int mouseX, int mouseY, boolean clicked) {
        ui.equipmentHoveredRow = -1;
        ui.equipmentHoveredSlot = -1;
        ui.equipmentHoveredFilter = -1;
        ui.equipmentWeaponHovered = false;
        ui.toBattleHovered = false;
        ui.equipmentWeaponSlotBounds.setBounds(0, 0, 0, 0);
        ui.equipmentBackHovered = ui.equipmentBackButtonBounds.contains(mouseX, mouseY);
        EquipmentFilter[] filters = EquipmentFilter.armourFilters();
        for (int i = 0; i < filters.length; i++) {
            Rectangle bounds = ui.equipmentFilterBounds[i];
            if (bounds != null && bounds.contains(mouseX, mouseY)) {
                ui.equipmentHoveredFilter = i;
                break;
            }
        }
        for (int i = 0; i < ui.equipmentRowBounds.size(); i++) {
            if (ui.equipmentRowBounds.get(i).contains(mouseX, mouseY)) {
                ui.equipmentHoveredRow = i;
                break;
            }
        }
        for (int i = 0; i < ui.equipmentSlotBounds.length; i++) {
            if (ui.equipmentSlotBounds[i] != null && ui.equipmentSlotBounds[i].contains(mouseX, mouseY)) {
                ui.equipmentHoveredSlot = i;
                break;
            }
        }
        if (ui.equipmentWeaponSlotBounds.width > 0
            && ui.equipmentWeaponSlotBounds.contains(mouseX, mouseY)) {
            ui.equipmentWeaponHovered = true;
        }
        if (!clicked) {
            return;
        }
        if (ui.equipmentBackButtonBounds.contains(mouseX, mouseY)) {
            ui.equipmentOpen = false;
            ui.inventoryOpen = true;
            ui.equipmentAttention = false;
            refreshToBattleAttention();
            promoteBattleCardIconAttention(inventorySlots());
            if (chapterBridge != null) {
                chapterBridge.onEquipmentBack();
            }
            return;
        }
        if (ui.equipmentHoveredFilter >= 0) {
            ui.equipmentFilter = filters[ui.equipmentHoveredFilter];
            ui.equipmentHoveredRow = -1;
            return;
        }
        List<EquipmentGridEntry> visible = EquipmentArmourList.gridEntries(model, ui.equipmentFilter);
        if (ui.equipmentHoveredRow >= 0) {
            if (ui.equipmentHoveredRow < visible.size()) {
                EquipmentGridEntry entry = visible.get(ui.equipmentHoveredRow);
                if (entry.isKit()) {
                    var set = entry.armourSet();
                    if (set != null) {
                        if (model.isSetEquipped(set)) {
                            // 2-й клик по иконке комплекта — снимаем все его части.
                            for (Armour armour : set.getArmorPieces()) {
                                EquipSlot slot = EquipSlot.forArmour(armour);
                                if (slot != null) {
                                    model.unequip(slot);
                                }
                            }
                        } else {
                            model.equipSet(set);
                        }
                    }
                } else if (entry.isWeapon()) {
                    ShopInventorySlot weapon = entry.weapon();
                    if (weapon != null) {
                        ShopInventorySlot equipped = model.getEquippedWeapon();
                        if (equipped != null && weapon.title().equals(equipped.title())) {
                            model.unequipWeapon();
                        } else {
                            model.equipWeapon(weapon);
                        }
                    }
                } else if (entry.armour() != null) {
                    Armour armour = entry.armour();
                    if (model.isEquipped(armour)) {
                        EquipSlot slot = EquipSlot.forArmour(armour);
                        if (slot != null) {
                            model.unequip(slot);
                        }
                    } else {
                        model.equipArmour(armour);
                    }
                }
                if (chapterBridge != null) {
                    chapterBridge.onEquip();
                }
                refreshToBattleAttention();
            }
            return;
        }
        if (ui.equipmentHoveredSlot >= 0) {
            EquipSlot slot = EquipSlot.values()[ui.equipmentHoveredSlot];
            if (model.getEquipped(slot) != null) {
                model.unequip(slot);
            }
            refreshToBattleAttention();
            return;
        }
        if (ui.equipmentWeaponSlotBounds.width > 0
            && ui.equipmentWeaponSlotBounds.contains(mouseX, mouseY)) {
            if (model.getEquippedWeapon() != null) {
                model.unequipWeapon();
            } else {
                ui.equipmentFilter = EquipmentFilter.WEAPON;
            }
            refreshToBattleAttention();
            return;
        }
    }

    private void beginWalletReveal() {
        beginWalletReveal(ui.state == ShopScreenState.CATEGORY || ui.state == ShopScreenState.CATEGORY_OPENING);
    }

    private void beginWalletReveal(boolean fromCategory) {
        ui.walletRevealFromCategory = fromCategory;
        ui.state = ShopScreenState.WALLET_REVEAL;
        ui.walletRevealTicks = 0;
        ui.inventoryOpen = false;
        ui.equipmentOpen = false;
        ui.currentDialog = DukeLines.walletReveal();
    }

    private void finishWalletReveal() {
        model.revealWallet();
        ui.walletRevealTicks = 0;
        boolean reopenCategory = ui.walletRevealFromCategory
            && ui.selectedIndex >= 0
            && ui.selectedIndex < ui.showcaseItems.size();
        ui.walletRevealFromCategory = false;
        ui.inventoryOpen = false;
        ui.equipmentOpen = false;
        if (reopenCategory) {
            // Сразу та же категория, что открыли до катсцены кошелька.
            ShopShowcaseItem item = ui.showcaseItems.get(ui.selectedIndex);
            buildCatalogRows(item);
            ui.categoryClosing = false;
            ui.categoryTicks = CATEGORY_OPEN_DURATION_TICKS;
            ui.selectedRowIndex = ui.catalogEntries.isEmpty() ? -1 : 0;
            ui.catalogScrollOffset = 0;
            ui.state = ShopScreenState.CATEGORY;
            String after = DukeLines.walletRevealAfter();
            ui.currentDialog = (after == null || after.isBlank()) ? IDLE_LINE : after;
            ui.buyAttention = true;
            ui.inventoryAttention = false;
            return;
        }
        ui.categoryClosing = false;
        ui.categoryTicks = 0;
        ui.selectedIndex = -1;
        ui.selectedRowIndex = -1;
        ui.catalogEntries.clear();
        ui.catalogScrollOffset = 0;
        ui.state = ShopScreenState.IDLE;
        String after = DukeLines.walletRevealAfter();
        ui.currentDialog = (after == null || after.isBlank()) ? IDLE_LINE : after;
        ui.buyAttention = true;
        ui.inventoryAttention = false;
    }

    /** Анимация выдачи карты боя (как кошелёк) — вызывается из главы 1. */
    public void beginBattleCardReveal(Runnable onComplete) {
        beginBattleCardReveal(onComplete, false);
    }

    public void beginBattleCardReveal(Runnable onComplete, boolean noPurchaseGift) {
        battleCardRevealComplete = onComplete;
        ui.state = ShopScreenState.BATTLE_CARD_REVEAL;
        ui.battleCardRevealTicks = 0;
        ui.inventoryOpen = false;
        ui.equipmentOpen = false;
        ui.currentDialog = noPurchaseGift
            ? DukeLines.battleCardRevealNoPurchase()
            : DukeLines.battleCardReveal();
    }

    private void finishBattleCardReveal() {
        ui.battleCardRevealTicks = 0;
        ui.state = ShopScreenState.IDLE;
        String after = DukeLines.battleCardRevealAfter();
        ui.currentDialog = (after == null || after.isBlank()) ? IDLE_LINE : after;
        // После брифинга: сумка → карта → «Открыть» (без экипировки).
        ui.inventoryAttention = true;
        ui.skipEquipmentGuide = true;
        ui.battleCardAttentionPending = true;
        ui.battleCardAttention = false;
        ui.battleCardOpenAttention = false;
        ui.guideToEquipment = false;
        ui.toBattleAttention = false;
        Runnable done = battleCardRevealComplete;
        battleCardRevealComplete = null;
        if (done != null) {
            done.run();
        }
    }

    private boolean inventoryPanelContains(int mx, int my) {
        return ui.inventoryPanelBounds.contains(mx, my);
    }

    private void tryPurchaseSelected() {
        if (ui.selectedRowIndex < 0 || ui.selectedRowIndex >= ui.catalogEntries.size()) {
            return;
        }
        if (model.needsWalletReveal()) {
            beginWalletReveal();
            return;
        }
        ShopCatalogEntry entry = ui.catalogEntries.get(ui.selectedRowIndex);
        ShopCategory shelf = ui.selectedIndex >= 0 && ui.selectedIndex < ui.showcaseItems.size()
            ? ui.showcaseItems.get(ui.selectedIndex).category
            : null;
        ShopModel.PurchaseResult result = model.purchase(entry, shelf);
        ui.currentDialog = result.dukeLine();
            if (result.success()) {
            ui.buyAttention = false;
            ui.armorHelpEmptyCategoryCount = 0;
            if (chapterBridge != null) {
                chapterBridge.onPurchase();
            }
            beginPurchaseReveal(entry);
        }
    }

    private void beginPurchaseReveal(ShopCatalogEntry entry) {
        ui.purchaseRevealKeepRow = ui.selectedRowIndex;
        if (ui.selectedIndex >= 0 && ui.selectedIndex < ui.showcaseItems.size()) {
            ShopShowcaseItem cat = ui.showcaseItems.get(ui.selectedIndex);
            ui.purchaseRevealCategory = cat.category;
            ui.purchaseRevealIcon = armourIcons.iconForEntry(entry, cat.category);
            if (ui.purchaseRevealIcon == null) {
                ui.purchaseRevealIcon = cat.cardArt != null ? cat.cardArt : cat.icon;
            }
        } else {
            ui.purchaseRevealIcon = null;
            ui.purchaseRevealCategory = null;
        }
        ui.purchaseRevealCrop = null;
        ui.purchaseRevealTicks = 0;
        ui.purchaseRevealCount++;
        ui.purchaseRevealShowSkipHint = ui.purchaseRevealCount >= 2;
        ui.purchaseSkipHintUsed = ui.purchaseRevealShowSkipHint;
        ui.inventoryOpen = false;
        ui.equipmentOpen = false;
        ui.state = ShopScreenState.PURCHASE_REVEAL;
    }

    private void finishPurchaseReveal() {
        ShopCategory bought = ui.purchaseRevealCategory;
        ui.purchaseRevealTicks = 0;
        ui.purchaseRevealIcon = null;
        ui.purchaseRevealCrop = null;
        ui.purchaseRevealCategory = null;
        ui.purchaseRevealShowSkipHint = false;
        if (ui.selectedIndex >= 0) {
            int keepIndex = Math.min(ui.purchaseRevealKeepRow, Math.max(0, ui.catalogEntries.size() - 2));
            buildCatalogRows(ui.showcaseItems.get(ui.selectedIndex));
            ui.selectedRowIndex = ui.catalogEntries.isEmpty()
                ? -1
                : Math.min(keepIndex, ui.catalogEntries.size() - 1);
        }
        ui.purchaseRevealKeepRow = -1;
        refreshShowcasePrices();
        ui.state = ShopScreenState.CATEGORY;
        ui.buyAttention = false;
        ui.inventoryAttention = true;
        boolean boughtWearable = bought == ShopCategory.WEAPON
            || bought == ShopCategory.SETS
            || bought == ShopCategory.CHEST
            || bought == ShopCategory.LEGS
            || bought == ShopCategory.GLOVES
            || bought == ShopCategory.BOOTS;
        ui.guideToEquipment = boughtWearable;
        ui.potionDrinkAttention = bought == ShopCategory.POTION;
        ui.skipEquipmentGuide = false;
        ui.currentDialog = DukeLines.goToInventory();
    }

    private void beginCategoryClose() {
        ui.categoryClosing = true;
        ui.categoryTicks = 0;
        ui.state = ShopScreenState.CATEGORY_CLOSING;
        ui.hoveredRowIndex = -1;
    }

    private void skipCategoryAnimation() {
        if (ui.state == ShopScreenState.CATEGORY_OPENING) {
            ui.categoryTicks = CATEGORY_OPEN_DURATION_TICKS;
            ui.categoryClosing = false;
            ui.state = ShopScreenState.CATEGORY;
            return;
        }
        if (ui.state == ShopScreenState.CATEGORY_CLOSING) {
            ui.categoryTicks = CATEGORY_OPEN_DURATION_TICKS;
            finishCategoryClose();
        }
    }

    private void finishCategoryClose() {
        ui.categoryClosing = false;
        ui.categoryTicks = 0;
        ui.selectedIndex = -1;
        ui.selectedRowIndex = -1;
        ui.catalogEntries.clear();
        ui.catalogScrollOffset = 0;
        ui.state = ShopScreenState.IDLE;
        if (chapterBridge != null
            && model.inventoryItemCount() == 0
            && !model.needsWalletReveal()
            && ui.armorHelpEmptyCategoryCount >= ARMOR_HELP_AFTER_EMPTY_CATEGORY_OPENS) {
            beginArmorHelpOffer();
        } else {
            ui.currentDialog = IDLE_LINE;
            ui.dialogSpeaker = "Герцог";
        }
    }

    private void beginArmorHelpOffer() {
        if (ui.armorHelpActive || model.inventoryItemCount() > 0) {
            ui.currentDialog = IDLE_LINE;
            ui.dialogSpeaker = "Герцог";
            ui.buyAttention = model.inventoryItemCount() == 0;
            return;
        }
        ui.armorHelpEmptyCategoryCount = 0;
        ui.armorHelpActive = true;
        ui.armorHelpHovered = -1;
        ui.armorHelpChoiceBounds.clear();
        ui.currentDialog = DukeLines.armorHelpOffer();
        ui.dialogSpeaker = "Герцог";
        ui.buyAttention = true;
    }

    private void updateArmorHelpInput(int mx, int my, boolean clicked) {
        layoutYesNoChoices();
        ui.armorHelpHovered = main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.hitIndex(
            ui.armorHelpChoiceBounds, mx, my);
        if (!clicked || ui.armorHelpHovered < 0) {
            return;
        }
        if (ui.armorHelpHovered == 0) {
            acceptArmorHelp();
        } else {
            declineArmorHelp();
        }
    }

    private void layoutYesNoChoices() {
        // Та же геометрия, что в ShopSwingView.layoutYesNoChoiceBounds.
        ui.armorHelpChoiceBounds.clear();
        int rowH = 24;
        int gap = 8;
        int btnW = 100;
        if (ui.battleConfirmActive) {
            int totalW = btnW * 2 + gap;
            int x0 = (VIRTUAL_W - totalW) / 2;
            int y0 = VIRTUAL_H / 2 + 16;
            ui.armorHelpChoiceBounds.add(
                new main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.ChoiceRect(0, x0, y0, btnW, rowH));
            ui.armorHelpChoiceBounds.add(
                new main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.ChoiceRect(1, x0 + btnW + gap, y0, btnW, rowH));
            return;
        }
        boolean equipmentScreen = ui.equipmentOpen || ui.outfitConfirmActive;
        if (equipmentScreen) {
            var equip = main.java.com.witcher.ui.shop.view.EquipmentOverlayLayout.compute(VIRTUAL_W, VIRTUAL_H);
            btnW = 88;
            gap = 8;
            rowH = 24;
            int totalW = btnW * 2 + gap;
            int x0 = equip.portraitX + Math.max(0, (equip.portraitW - totalW) / 2 + 28);
            if (x0 + totalW > VIRTUAL_W - 8) {
                x0 = VIRTUAL_W - totalW - 8;
            }
            int y0 = VIRTUAL_H - DIALOG_TEXT_ZONE - rowH + 6;
            ui.armorHelpChoiceBounds.add(
                new main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.ChoiceRect(0, x0, y0, btnW, rowH));
            ui.armorHelpChoiceBounds.add(
                new main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.ChoiceRect(
                    1, x0 + btnW + gap, y0, btnW, rowH));
            return;
        }
        int totalW = btnW * 2 + gap;
        int x0 = (VIRTUAL_W - totalW) / 2;
        int y0 = VIRTUAL_H - DIALOG_TEXT_ZONE - rowH + 6;
        ui.armorHelpChoiceBounds.add(
            new main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.ChoiceRect(0, x0, y0, btnW, rowH));
        ui.armorHelpChoiceBounds.add(
            new main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.ChoiceRect(1, x0 + btnW + gap, y0, btnW, rowH));
    }

    private static java.util.List<main.java.com.witcher.chapter1.vn.VnChoice> armorHelpChoices() {
        return java.util.List.of(
            new main.java.com.witcher.chapter1.vn.VnChoice("yes", "Да", 0, 0),
            new main.java.com.witcher.chapter1.vn.VnChoice("no", "Нет", 0, 0)
        );
    }

    private void acceptArmorHelp() {
        ui.armorHelpActive = false;
        ui.armorHelpChoiceBounds.clear();
        ui.armorHelpHovered = -1;
        ShopModel.PurchaseResult result = model.beginOutfitPreview(model.getWallet());
        if (!result.success()) {
            ui.currentDialog = result.dukeLine();
            ui.dialogSpeaker = "Герцог";
            ui.buyAttention = true;
            return;
        }
        ui.currentDialog = result.dukeLine();
        ui.dialogSpeaker = "Герцог";
        ui.buyAttention = false;
        ui.inventoryAttention = false;
        ui.guideToEquipment = false;
        ui.skipEquipmentGuide = false;
        ui.inventoryOpen = false;
        ui.equipmentOpen = true;
        ui.outfitConfirmActive = true;
        ui.armorHelpHovered = -1;
        ui.toBattleAttention = false;
        refreshShowcasePrices();
    }

    private void updateOutfitConfirmInput(int mx, int my, boolean clicked) {
        layoutYesNoChoices();
        ui.armorHelpHovered = main.java.com.witcher.ui.chapter1.view.VnChoiceLayout.hitIndex(
            ui.armorHelpChoiceBounds, mx, my);
        if (!clicked || ui.armorHelpHovered < 0) {
            return;
        }
        if (ui.armorHelpHovered == 0) {
            confirmOutfitPurchase();
        } else {
            declineOutfitPurchase();
        }
    }

    private void confirmOutfitPurchase() {
        ui.outfitConfirmActive = false;
        ui.armorHelpChoiceBounds.clear();
        ui.armorHelpHovered = -1;
        ShopModel.PurchaseResult result = model.confirmOutfitPreview();
        if (!result.success()) {
            model.cancelOutfitPreview();
            ui.equipmentOpen = false;
            ui.currentDialog = result.dukeLine();
            ui.dialogSpeaker = "Герцог";
            ui.buyAttention = true;
            return;
        }
        if (chapterBridge != null) {
            chapterBridge.onPurchase();
            chapterBridge.onEquip();
        }
        ui.currentDialog = result.dukeLine();
        ui.dialogSpeaker = "Герцог";
        ui.buyAttention = false;
        refreshToBattleAttention();
        refreshShowcasePrices();
    }

    private void declineOutfitPurchase() {
        ui.outfitConfirmActive = false;
        ui.armorHelpChoiceBounds.clear();
        ui.armorHelpHovered = -1;
        model.cancelOutfitPreview();
        ui.equipmentOpen = false;
        ui.inventoryOpen = false;
        ui.currentDialog = DukeLines.dukeOutfitShopYourself();
        ui.dialogSpeaker = "Герцог";
        ui.buyAttention = true;
        ui.toBattleAttention = false;
        refreshShowcasePrices();
    }

    private void declineArmorHelp() {
        ui.armorHelpActive = false;
        ui.armorHelpChoiceBounds.clear();
        ui.armorHelpHovered = -1;
        ui.currentDialog = DukeLines.geraltBuyMyself();
        ui.dialogSpeaker = "Геральт";
        ui.buyAttention = true;
    }

    public float categoryAnimProgress() {
        float t = ui.categoryTicks / (float) CATEGORY_OPEN_DURATION_TICKS;
        if (ui.categoryClosing) {
            return Math.max(0f, 1f - t);
        }
        return Math.min(1f, t);
    }

    private void updateAshParticles() {
        if (ui.tick % 6 == 0 && ui.ashParticles.size() < 15) {
            float x = 130 + ui.rng.nextFloat() * 220;
            float y = 50 + ui.rng.nextFloat() * 140;
            ui.ashParticles.add(new float[]{x, y, 0, -0.12f, 0, 60 + ui.rng.nextInt(60), 1});
        }
        ui.ashParticles.removeIf(p -> ++p[4] >= p[5]);
        for (float[] p : ui.ashParticles) {
            p[1] += p[3];
        }
    }
}
