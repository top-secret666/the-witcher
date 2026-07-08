package main.java.com.witcher.ui.shop.presenter;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.graphics.ShopAssetCache;
import main.java.com.witcher.ui.shop.ArmourIconRegistry;
import main.java.com.witcher.ui.shop.DukeLines;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEquipSlot;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.view.ShopLayout;
import main.java.com.witcher.ui.shop.view.ShopUiMetrics;
import main.java.com.witcher.ui.shop.view.ShopShowcaseItem;
import main.java.com.witcher.ui.shop.view.anim.ShopCategoryAnimator;
import main.java.com.witcher.ui.shop.view.anim.ShopRevealAnimator;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.*;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

/** MVP presenter для экрана лавки — вся логика ввода, состояний и анимаций. */
public final class ShopPresenter {

    private static final ShopCategory[] GRID_CATEGORIES = {
        ShopCategory.CHEST, ShopCategory.LEGS, ShopCategory.GLOVES,
        ShopCategory.BOOTS, ShopCategory.POTION, ShopCategory.SETS, ShopCategory.WEAPON
    };

    private static final String WELCOME_LINE = DukeLines.WELCOME;

    private static final String IDLE_LINE = DukeLines.IDLE;

    private final ShopModel model;
    private final ShopSessionState ui = new ShopSessionState();
    private final ShopAssetCache assets = ShopAssetCache.get();
    private final ShopUiMetrics metrics = assets;
    private final ArmourIconRegistry armourIcons;

    public ShopPresenter(ShopModel model) {
        this.model = model;
        this.armourIcons = ArmourIconRegistry.get(metrics.cardArtSize());
        initShowcaseFromModel();
        ui.currentDialog = WELCOME_LINE;
    }

    public void update(ShopInput input) {
        ui.tick++;

        if (input.escPressed()) {
            if (ui.equipmentOpen) {
                ui.equipmentOpen = false;
                ui.inventoryOpen = true;
                return;
            }
            if (ui.inventoryOpen) {
                ui.inventoryOpen = false;
                ui.inventoryPouchFocused = true;
                return;
            }
            if (ui.state == ShopScreenState.WALLET_REVEAL) {
                ui.walletRevealTicks = WALLET_REVEAL_TOTAL - 1;
                return;
            }
            if (ui.state == ShopScreenState.PURCHASE_REVEAL) {
                ui.purchaseRevealTicks = PURCHASE_REVEAL_TOTAL - 1;
                return;
            }
            if (ui.state == ShopScreenState.CATEGORY || ui.state == ShopScreenState.CATEGORY_OPENING) {
                beginCategoryClose();
                return;
            }
            if (ui.state == ShopScreenState.CATEGORY_CLOSING) {
                return;
            }
            ui.exitRequested = true;
            return;
        }

        if (ui.state == ShopScreenState.REVEAL) {
            ui.revealTicks++;
            if (ui.revealTicks >= REVEAL_DURATION_TICKS) {
                ui.state = ShopScreenState.IDLE;
                ui.currentDialog = IDLE_LINE;
            }
        }

        if (ui.state == ShopScreenState.WALLET_REVEAL) {
            ui.walletRevealTicks++;
            if (ui.walletRevealTicks >= WALLET_REVEAL_TOTAL) {
                finishWalletReveal();
            }
        }

        if (ui.state == ShopScreenState.PURCHASE_REVEAL) {
            ui.purchaseRevealTicks++;
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

        updateAshParticles();

        ShopRevealAnimator reveal = revealAnimator();
        boolean showcaseInteractive = reveal.uiInteractive && ui.state == ShopScreenState.IDLE;

        ShopLayout layout = createLayout();

        if (ui.state == ShopScreenState.WALLET_REVEAL && input.clicked()) {
            ui.walletRevealTicks = WALLET_REVEAL_TOTAL - 1;
            return;
        }

        if (ui.state == ShopScreenState.PURCHASE_REVEAL && input.clicked()) {
            ui.purchaseRevealTicks = PURCHASE_REVEAL_TOTAL - 1;
            return;
        }

        if ((ui.state == ShopScreenState.CATEGORY_OPENING || ui.state == ShopScreenState.CATEGORY_CLOSING)
            && input.clicked()) {
            skipCategoryAnimation();
            return;
        }

        boolean bagUnlocked = !model.needsWalletReveal()
            && ui.state != ShopScreenState.WALLET_REVEAL
            && ui.state != ShopScreenState.PURCHASE_REVEAL;
        if (bagUnlocked) {
            updateInventoryInput(input.mouseX(), input.mouseY(), input.clicked());
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
            ui.state = ShopScreenState.CATEGORY_OPENING;
            ui.categoryClosing = false;
            ui.categoryTicks = 0;
            Point slot = layout.cardSlot(ui.hoveredIndex);
            ui.categoryFromRect.setBounds(slot.x, slot.y, layout.cardW, layout.cardH);
            buildCatalogRows(item);
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
                ui.currentDialog = DukeLines.rowInspect(row.name, row.price);
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
        }
    }

    public ShopSessionState ui() {
        return ui;
    }

    public ShopModel model() {
        return model;
    }

    public ShopAssetCache assets() {
        return assets;
    }

    public ArmourIconRegistry armourIcons() {
        return armourIcons;
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
            case WALLET_REVEAL, PURCHASE_REVEAL -> ShopRevealAnimator.complete(ui.showcaseItems.size());
            default -> ShopRevealAnimator.complete(ui.showcaseItems.size());
        };
    }

    public ShopCategoryAnimator categoryAnimator(ShopLayout layout) {
        Rectangle to = layout.leftCategoryCardSlot(metrics.detailPanelW());
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
        float t = Math.min(1f, (ui.walletRevealTicks - countStart) / (float) WALLET_COUNT_TICKS);
        t = t * t * (3f - 2f * t);
        return String.valueOf(Math.round(model.getWallet() * t));
    }

    public boolean isCategoryMode() {
        return ui.state == ShopScreenState.CATEGORY_OPENING
            || ui.state == ShopScreenState.CATEGORY
            || ui.state == ShopScreenState.CATEGORY_CLOSING;
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
        return panelY + CATALOG_PANEL_INSET_TOP;
    }

    public int catalogListBottom(int panelY) {
        return categoryBuyButtonY(panelY) - CATALOG_PANEL_GAP_ABOVE_BUY;
    }

    public int catalogRowContentW() {
        return metrics.detailPanelW() - CATALOG_PANEL_INSET_X * 2;
    }

    public int catalogRowX(int panelX) {
        return panelX + CATALOG_PANEL_INSET_X;
    }

    public int categoryBuyButtonY(int panelY) {
        return panelY + metrics.detailPanelH() - metrics.btnH() - 8;
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

    private void updateInventoryInput(int mouseX, int mouseY, boolean clicked) {
        inventoryBagSlot();
        if (ui.equipmentOpen) {
            updateEquipmentInput(mouseX, mouseY, clicked);
            return;
        }
        if (ui.inventoryOpen) {
            ui.inventoryPouchIconHovered = ui.inventoryPouchIconBounds.contains(mouseX, mouseY);
            ui.inventoryCloseHovered = ui.inventoryCloseBounds.contains(mouseX, mouseY);
            if (clicked) {
                if (ui.inventoryCloseBounds.contains(mouseX, mouseY)) {
                    ui.inventoryOpen = false;
                    ui.inventoryPouchFocused = true;
                } else if (ui.inventoryEquipButtonBounds.contains(mouseX, mouseY)) {
                    ui.equipmentOpen = true;
                    ui.inventoryOpen = false;
                    ui.equipmentHoveredRow = -1;
                    ui.equipmentHoveredSlot = -1;
                } else if (ui.inventoryPouchIconBounds.contains(mouseX, mouseY)) {
                    ui.inventoryPouchFocused = true;
                } else if (!ui.inventoryPanelBounds.contains(mouseX, mouseY)) {
                    ui.inventoryOpen = false;
                    ui.inventoryPouchFocused = true;
                }
            }
        } else if (clicked && ui.inventoryBagBounds.contains(mouseX, mouseY)) {
            ui.inventoryOpen = true;
            ui.inventoryPouchFocused = true;
        }
    }

    private void updateEquipmentInput(int mouseX, int mouseY, boolean clicked) {
        ui.equipmentHoveredRow = -1;
        ui.equipmentHoveredSlot = -1;
        ui.equipmentBackHovered = ui.equipmentBackButtonBounds.contains(mouseX, mouseY);
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
        if (!clicked) {
            return;
        }
        if (ui.equipmentBackButtonBounds.contains(mouseX, mouseY)) {
            ui.equipmentOpen = false;
            ui.inventoryOpen = true;
            return;
        }
        if (ui.equipmentHoveredRow >= 0) {
            List<Armour> owned = model.ownedArmour();
            if (ui.equipmentHoveredRow < owned.size()) {
                model.equipArmour(owned.get(ui.equipmentHoveredRow));
            }
            return;
        }
        if (ui.equipmentHoveredSlot >= 0) {
            ShopEquipSlot slot = ShopEquipSlot.values()[ui.equipmentHoveredSlot];
            if (model.getEquipped(slot) != null) {
                model.unequip(slot);
            }
        }
    }

    private void beginWalletReveal() {
        ui.walletRevealFromCategory = ui.state == ShopScreenState.CATEGORY;
        ui.state = ShopScreenState.WALLET_REVEAL;
        ui.walletRevealTicks = 0;
        ui.inventoryOpen = false;
        ui.equipmentOpen = false;
        ui.currentDialog = DukeLines.walletReveal();
    }

    private void finishWalletReveal() {
        model.revealWallet();
        ui.walletRevealTicks = 0;
        ui.state = ui.walletRevealFromCategory ? ShopScreenState.CATEGORY : ShopScreenState.IDLE;
        ui.walletRevealFromCategory = false;
        ui.currentDialog = DukeLines.walletRevealAfter();
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
        ShopModel.PurchaseResult result = model.purchase(entry);
        ui.currentDialog = result.dukeLine();
        if (result.success()) {
            beginPurchaseReveal(entry);
        }
    }

    private void beginPurchaseReveal(ShopCatalogEntry entry) {
        ui.purchaseRevealKeepRow = ui.selectedRowIndex;
        if (ui.selectedIndex >= 0 && ui.selectedIndex < ui.showcaseItems.size()) {
            ShopShowcaseItem cat = ui.showcaseItems.get(ui.selectedIndex);
            ui.purchaseRevealIcon = armourIcons.iconForEntry(entry, cat.category);
            if (ui.purchaseRevealIcon == null) {
                ui.purchaseRevealIcon = cat.cardArt != null ? cat.cardArt : cat.icon;
            }
        } else {
            ui.purchaseRevealIcon = null;
        }
        ui.purchaseRevealTicks = 0;
        ui.inventoryOpen = false;
        ui.equipmentOpen = false;
        ui.state = ShopScreenState.PURCHASE_REVEAL;
    }

    private void finishPurchaseReveal() {
        ui.purchaseRevealTicks = 0;
        ui.purchaseRevealIcon = null;
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
        ui.currentDialog = IDLE_LINE;
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
