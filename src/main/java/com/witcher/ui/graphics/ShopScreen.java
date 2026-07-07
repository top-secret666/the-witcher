package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.DukeLines;
import main.java.com.witcher.ui.shop.ArmourIconRegistry;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.shop.ShopEquipSlot;
import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Пиксельный экран лавки Герцога.
 */
public class ShopScreen {

    private static final int VIRTUAL_W = 480;
    private static final int VIRTUAL_H = 360;
    private static final int DIALOG_TEXT_ZONE = 54;
    /** Зазор между низом витрины и зоной диалога Герцога. */
    private static final int PANEL_BOTTOM_MARGIN = 4;

    private enum ShopState {
        REVEAL,
        IDLE,
        WALLET_REVEAL,
        PURCHASE_REVEAL,
        CATEGORY_OPENING,
        CATEGORY,
        CATEGORY_CLOSING
    }

    /** ~3 с при 30 FPS — появление витрины. */
    private static final int REVEAL_DURATION_TICKS = 84;
    private static final int CATEGORY_OPEN_DURATION_TICKS = 28;
    /** Сцена кошелька: появление → полёт в сумку → закрытие → счётчик. */
    private static final int WALLET_APPEAR_TICKS = 30;
    private static final int WALLET_FLY_TICKS = 40;
    private static final int WALLET_FADE_TICKS = 10;
    private static final int WALLET_CLOSE_TICKS = 8;
    private static final int WALLET_BAG_CLOSE_TICKS = WALLET_FADE_TICKS + WALLET_CLOSE_TICKS;
    private static final int WALLET_COUNT_TICKS = 28;
    private static final int WALLET_REVEAL_TOTAL =
        WALLET_APPEAR_TICKS + WALLET_FLY_TICKS + WALLET_BAG_CLOSE_TICKS + WALLET_COUNT_TICKS;
    /** Сцена покупки: иконка товара → сумка. */
    private static final int PURCHASE_APPEAR_TICKS = 24;
    private static final int PURCHASE_FLY_TICKS = 34;
    private static final int PURCHASE_FADE_TICKS = 10;
    private static final int PURCHASE_CLOSE_TICKS = 10;
    private static final int PURCHASE_TUCK_TICKS = PURCHASE_FADE_TICKS + PURCHASE_CLOSE_TICKS;
    private static final int PURCHASE_REVEAL_TOTAL =
        PURCHASE_APPEAR_TICKS + PURCHASE_FLY_TICKS + PURCHASE_TUCK_TICKS;
    private static final int INVENTORY_BAG_SIZE = 40;
    private static final int INVENTORY_BAG_MARGIN = 8;
    private static final int INVENTORY_PANEL_W = 280;
    private static final int INVENTORY_PANEL_H = 238;
    private static final int INVENTORY_POUCH_ICON = 32;
    private static final int INVENTORY_POUCH_LARGE = 96;
    private static final int EQUIP_MARGIN = 4;
    private static final int GRID_COLS = 5;
    private static final int TOP_ROW_COLS = 5;
    private static final int BOTTOM_ROW_COLS = 2;

    private static final BufferedImage MENU_CURSOR = loadMenuCursor();

    private static BufferedImage loadMenuCursor() {
        Sprite s = Sprite.loadOptional("/assets/sprites/menu/menu_cursor.png");
        return s != null ? s.getImage() : null;
    }

    private static final class ShopLayout {
        final int hudY;
        final int hudH;
        final int hudX;
        final int hudW;
        final int panelX;
        final int panelY;
        final int panelW;
        final int panelH;
        final int headerH;
        final int btnX;
        final int btnY;
        final int btnW;
        final int btnH;
        final int cardW;
        final int cardH;
        final int cardGap;
        final int cardsStartX;
        final int cardsY;
        final int gridCols;
        final int gridRows;
        final int dialogTop;
        final int cardsStartXBottom;

        ShopLayout(int sw, int sh, int itemCount, int hudX, int hudW, int hudH, int fixedPanelW,
                   int panelHeaderH, int topRowCols, int bottomRowCols) {
            this.hudX = hudX;
            this.hudW = hudW;
            hudY = 4;
            this.hudH = hudH;
            dialogTop = sh - DIALOG_TEXT_ZONE;
            btnH = 30;
            btnW = 100;
            headerH = panelHeaderH;
            cardW = 54;
            cardH = 81;
            cardGap = 6;
            gridCols = GRID_COLS;
            gridRows = itemCount > topRowCols ? 2 : 1;

            int rowW = topRowCols * cardW + (topRowCols - 1) * cardGap;
            panelW = fixedPanelW;
            panelX = (sw - panelW) / 2;
            panelY = hudY + hudH + 6;
            cardsStartX = panelX + (panelW - rowW) / 2;

            int bottomCount = Math.min(bottomRowCols, Math.max(0, itemCount - topRowCols));
            int bottomRowW = bottomCount * cardW + Math.max(0, bottomCount - 1) * cardGap;
            cardsStartXBottom = panelX + (panelW - bottomRowW) / 2;

            int gridContentH = gridRows * cardH + (gridRows - 1) * cardGap;
            panelH = dialogTop - panelY - PANEL_BOTTOM_MARGIN;
            int innerTop = panelY + headerH;
            int innerH = Math.max(gridContentH, panelH - headerH);
            cardsY = innerTop + Math.max(4, (innerH - gridContentH) / 2);

            int contentBottom = cardsY + gridContentH;
            btnX = panelX + (panelW - btnW) / 2;
            btnY = contentBottom + 6;
        }

        Point cardSlot(int index) {
            if (index < TOP_ROW_COLS) {
                int col = index;
                int x = cardsStartX + col * (cardW + cardGap);
                int y = cardsY;
                return new Point(x, y);
            }
            int bottomIndex = index - TOP_ROW_COLS;
            int x = cardsStartXBottom + bottomIndex * (cardW + cardGap);
            int y = cardsY + cardH + cardGap;
            return new Point(x, y);
        }

        /** Карточка категории — пропорции как у shop_card (54×81), ширина до панели списка. */
        Rectangle leftCategoryCardSlot(int detailPanelW) {
            int catalogX = VIRTUAL_W - detailPanelW - 8;
            int gap = 10;
            int x = 4;
            int w = catalogX - gap - x;
            int h = w * 81 / 54;
            int maxH = dialogTop - 12;
            if (h > maxH) {
                h = maxH;
                w = h * 54 / 81;
            }
            int y = 8;
            return new Rectangle(x, y, w, h);
        }

        Rectangle detailListPanelSlot(int detailW, int detailH) {
            int x = VIRTUAL_W - detailW - 8;
            int y = 50;
            return new Rectangle(x, y, detailW, detailH);
        }

        int categoryCounterY() {
            return 6;
        }

        int categoryCounterH(int dialogTop) {
            return dialogTop - categoryCounterY() - 4;
        }
    }

    private static final ShopCategory[] GRID_CATEGORIES = {
        ShopCategory.CHEST, ShopCategory.LEGS, ShopCategory.GLOVES,
        ShopCategory.BOOTS, ShopCategory.POTION, ShopCategory.SETS, ShopCategory.WEAPON
    };

    private static final class ShopItem {
        final ItemKind kind;
        final ShopCategory category;
        String priceLabel;
        final String dukeLine;
        final String[] statLines;
        final BufferedImage icon;
        final BufferedImage cardArt;
        Rectangle bounds = new Rectangle();

        ShopItem(ItemKind kind, ShopCategory category, String priceLabel, String dukeLine,
                 String[] statLines, BufferedImage icon, BufferedImage cardArt) {
            this.kind = kind;
            this.category = category;
            this.priceLabel = priceLabel;
            this.dukeLine = dukeLine;
            this.statLines = statLines;
            this.icon = icon;
            this.cardArt = cardArt;
        }

        String displayName() {
            return category.label;
        }
    }

    private final ShopModel model;
    private final ShopAssetCache assets = ShopAssetCache.get();
    private final ArmourIconRegistry armourIcons;

    private final List<ShopItem> items = new ArrayList<>();
    private final List<ShopCatalogEntry> catalogEntries = new ArrayList<>();
    private final List<float[]> ashParticles = new ArrayList<>();
    private final Random rng = new Random();

    private ShopState state = ShopState.REVEAL;
    private String currentDialog;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private int hoveredRowIndex = -1;
    private int selectedRowIndex = -1;
    private int tick = 0;
    private int revealTicks = 0;
    private int categoryTicks = 0;
    private boolean categoryClosing = false;
    private Rectangle categoryFromRect = new Rectangle();
    private Rectangle categoryBuyBounds = new Rectangle();
    private int walletRevealTicks = 0;
    private boolean walletRevealFromCategory = false;
    private int purchaseRevealTicks = 0;
    private BufferedImage purchaseRevealIcon;
    private int purchaseRevealKeepRow = -1;
    private boolean inventoryOpen = false;
    private boolean equipmentOpen = false;
    private boolean inventoryPouchFocused = true;
    private boolean inventoryBagHovered = false;
    private boolean inventoryPouchIconHovered = false;
    private final Rectangle inventoryBagBounds = new Rectangle();
    private final Rectangle inventoryPanelBounds = new Rectangle();
    private final Rectangle inventoryPouchIconBounds = new Rectangle();
    private final Rectangle inventoryEquipButtonBounds = new Rectangle();
    private final Rectangle inventoryCloseBounds = new Rectangle();
    private boolean inventoryCloseHovered = false;
    private final Rectangle equipmentPanelBounds = new Rectangle();
    private final Rectangle equipmentBackButtonBounds = new Rectangle();
    private final Rectangle[] equipmentSlotBounds = new Rectangle[ShopEquipSlot.values().length];
    private final List<Rectangle> equipmentRowBounds = new ArrayList<>();
    private int equipmentHoveredRow = -1;
    private int equipmentHoveredSlot = -1;
    private boolean categoryBuyHovered = false;
    private final Rectangle categoryBackBounds = new Rectangle();
    private boolean categoryBackHovered = false;
    private int catalogScrollOffset = 0;
    private boolean exitRequested = false;

    private static final String WELCOME_LINE = DukeLines.WELCOME;

    private static final String IDLE_LINE = DukeLines.IDLE;

    public ShopScreen() {
        this(ShopModel.createNewSession());
    }

    public ShopScreen(ShopModel model) {
        this.model = model;
        this.armourIcons = ArmourIconRegistry.get(assets.cardArtSize);
        initShowcaseFromModel();
        currentDialog = WELCOME_LINE;
    }

    private void initShowcaseFromModel() {
        items.clear();
        for (ShopCategory cat : GRID_CATEGORIES) {
            ItemKind kind = cat == ShopCategory.SETS ? ItemKind.SET_CATALOG : ItemKind.PIECE;
            BufferedImage icon = iconForCategory(cat);
            items.add(new ShopItem(
                kind, cat,
                model.priceLabelForCategory(cat),
                model.dukeLineForCategory(cat),
                model.statLinesForCategory(cat),
                icon, icon));
        }
    }

    private BufferedImage iconForCategory(ShopCategory cat) {
        if (cat == ShopCategory.SETS) {
            return assets.setsIcon;
        }
        if (cat == ShopCategory.WEAPON) {
            return assets.weaponIcon;
        }
        if (cat.iconIndex >= 0 && cat.iconIndex < assets.itemIcons.length) {
            return assets.itemIcons[cat.iconIndex];
        }
        return null;
    }

    private void refreshShowcasePrices() {
        for (ShopItem item : items) {
            item.priceLabel = model.priceLabelForCategory(item.category);
        }
    }

    private enum ItemKind {
        PIECE,
        SET_CATALOG
    }

    private void buildCatalogRows(ShopItem category) {
        catalogEntries.clear();
        hoveredRowIndex = -1;
        catalogScrollOffset = 0;
        catalogEntries.addAll(model.getCatalog(category.category));
        selectedRowIndex = catalogEntries.isEmpty() ? -1 : 0;
    }

    private int catalogRowStep() {
        return assets.rowH + 4;
    }

    private int catalogListTop(int panelY) {
        return panelY + 12;
    }

    private int catalogListBottom(int panelY) {
        return panelY + assets.detailPanelH - assets.btnH - 10;
    }

    private int maxCatalogScroll(int panelY) {
        int visible = catalogListBottom(panelY) - catalogListTop(panelY);
        int content = catalogEntries.size() * catalogRowStep() - 4;
        return Math.max(0, content - visible);
    }

    private void scrollCatalogBy(int panelY, int wheelNotches) {
        catalogScrollOffset += wheelNotches * catalogRowStep();
        catalogScrollOffset = Math.max(0, Math.min(maxCatalogScroll(panelY), catalogScrollOffset));
    }

    private void ensureRowVisible(int panelY, int rowIndex) {
        if (rowIndex < 0) {
            return;
        }
        int listTop = catalogListTop(panelY);
        int listBottom = catalogListBottom(panelY);
        int rowY = listTop + rowIndex * catalogRowStep() - catalogScrollOffset;
        int rowBottom = rowY + assets.rowH;
        if (rowY < listTop) {
            catalogScrollOffset -= listTop - rowY;
        } else if (rowBottom > listBottom) {
            catalogScrollOffset += rowBottom - listBottom;
        }
        catalogScrollOffset = Math.max(0, Math.min(maxCatalogScroll(panelY), catalogScrollOffset));
    }

    public static Rectangle computeContentBoundsPublic(BufferedImage img) {
        return computeContentBounds(img);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed) {
        update(mouseX, mouseY, clicked, escPressed, 0);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
        tick++;

        if (escPressed) {
            if (equipmentOpen) {
                equipmentOpen = false;
                inventoryOpen = true;
                return;
            }
            if (inventoryOpen) {
                inventoryOpen = false;
                inventoryPouchFocused = true;
                return;
            }
            if (state == ShopState.WALLET_REVEAL) {
                walletRevealTicks = WALLET_REVEAL_TOTAL - 1;
                return;
            }
            if (state == ShopState.PURCHASE_REVEAL) {
                purchaseRevealTicks = PURCHASE_REVEAL_TOTAL - 1;
                return;
            }
            if (state == ShopState.CATEGORY || state == ShopState.CATEGORY_OPENING) {
                beginCategoryClose();
                return;
            }
            if (state == ShopState.CATEGORY_CLOSING) {
                return;
            }
            exitRequested = true;
            return;
        }

        if (state == ShopState.REVEAL) {
            revealTicks++;
            if (revealTicks >= REVEAL_DURATION_TICKS) {
                state = ShopState.IDLE;
                currentDialog = IDLE_LINE;
            }
        }

        if (state == ShopState.WALLET_REVEAL) {
            walletRevealTicks++;
            if (walletRevealTicks >= WALLET_REVEAL_TOTAL) {
                finishWalletReveal();
            }
        }

        if (state == ShopState.PURCHASE_REVEAL) {
            purchaseRevealTicks++;
            if (purchaseRevealTicks >= PURCHASE_REVEAL_TOTAL) {
                finishPurchaseReveal();
            }
        }

        if (state == ShopState.CATEGORY_OPENING || state == ShopState.CATEGORY_CLOSING) {
            categoryTicks++;
            if (categoryClosing) {
                if (categoryTicks >= CATEGORY_OPEN_DURATION_TICKS) {
                    finishCategoryClose();
                }
            } else if (categoryTicks >= CATEGORY_OPEN_DURATION_TICKS) {
                state = ShopState.CATEGORY;
            }
        }

        updateAshParticles();

        ShopRevealAnimator reveal = revealAnimator();
        boolean showcaseInteractive = reveal.uiInteractive && state == ShopState.IDLE;

        ShopLayout layout = new ShopLayout(VIRTUAL_W, VIRTUAL_H, items.size(),
            assets.hudX, assets.hudW, assets.hudH, assets.panelW,
            assets.panelHeaderH, assets.topRowCols, assets.bottomRowCols);

        if (state == ShopState.WALLET_REVEAL && clicked) {
            walletRevealTicks = WALLET_REVEAL_TOTAL - 1;
            return;
        }

        if (state == ShopState.PURCHASE_REVEAL && clicked) {
            purchaseRevealTicks = PURCHASE_REVEAL_TOTAL - 1;
            return;
        }

        if ((state == ShopState.CATEGORY_OPENING || state == ShopState.CATEGORY_CLOSING) && clicked) {
            skipCategoryAnimation();
            return;
        }

        boolean bagUnlocked = !model.needsWalletReveal()
            && state != ShopState.WALLET_REVEAL
            && state != ShopState.PURCHASE_REVEAL;
        if (bagUnlocked) {
            updateInventoryInput(mouseX, mouseY, clicked);
        } else {
            inventoryBagBounds.setBounds(0, 0, 0, 0);
            inventoryBagHovered = false;
        }

        if (inventoryOpen || equipmentOpen) {
            hoveredIndex = -1;
            hoveredRowIndex = -1;
            categoryBuyHovered = false;
            categoryBackHovered = false;
            inventoryBagHovered = false;
            return;
        }

        hoveredIndex = -1;
        hoveredRowIndex = -1;
        categoryBuyHovered = false;
        categoryBackHovered = false;

        if (showcaseInteractive) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).bounds.contains(mouseX, mouseY)) {
                    hoveredIndex = i;
                    break;
                }
            }
        }

        if (state == ShopState.CATEGORY || state == ShopState.CATEGORY_OPENING
            || state == ShopState.CATEGORY_CLOSING) {
            ShopCategoryAnimator catAnim = categoryAnimator(layout);
            if (wheelNotches != 0 && catAnim.listInteractive) {
                Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
                scrollCatalogBy(panel.y, wheelNotches);
            }
            if (catAnim.listInteractive) {
                for (int i = 0; i < catalogEntries.size(); i++) {
                    if (catalogEntries.get(i).bounds.contains(mouseX, mouseY)) {
                        hoveredRowIndex = i;
                        break;
                    }
                }
            }
        }

        if (showcaseInteractive && clicked && hoveredIndex >= 0) {
            selectedIndex = hoveredIndex;
            ShopItem item = items.get(hoveredIndex);
            currentDialog = item.dukeLine;
            state = ShopState.CATEGORY_OPENING;
            categoryClosing = false;
            categoryTicks = 0;
            Point slot = layout.cardSlot(hoveredIndex);
            categoryFromRect.setBounds(slot.x, slot.y, layout.cardW, layout.cardH);
            buildCatalogRows(item);
        }

        if (state == ShopState.CATEGORY && clicked) {
            ShopCategoryAnimator cat = categoryAnimator(layout);
            if (categoryBackBounds.contains(mouseX, mouseY) && cat.listInteractive) {
                beginCategoryClose();
            } else if (categoryBuyBounds.contains(mouseX, mouseY) && isBuyButtonEnabled()) {
                tryPurchaseSelected();
            } else if (hoveredRowIndex >= 0) {
                selectedRowIndex = hoveredRowIndex;
                ShopCatalogEntry row = catalogEntries.get(hoveredRowIndex);
                Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
                ensureRowVisible(panel.y, selectedRowIndex);
                currentDialog = DukeLines.rowInspect(row.name, row.price);
            }
        }

        if (state == ShopState.CATEGORY && categoryBuyBounds.width > 0) {
            categoryBuyHovered = categoryBuyBounds.contains(mouseX, mouseY);
        }
        if (state == ShopState.CATEGORY && categoryBackBounds.width > 0) {
            categoryBackHovered = categoryBackBounds.contains(mouseX, mouseY);
        }

        if (bagUnlocked) {
            inventoryBagHovered = inventoryBagBounds.contains(mouseX, mouseY);
        }
    }

    private void updateInventoryInput(int mouseX, int mouseY, boolean clicked) {
        inventoryBagSlot();
        if (equipmentOpen) {
            updateEquipmentInput(mouseX, mouseY, clicked);
            return;
        }
        if (inventoryOpen) {
            inventoryPouchIconHovered = inventoryPouchIconBounds.contains(mouseX, mouseY);
            inventoryCloseHovered = inventoryCloseBounds.contains(mouseX, mouseY);
            if (clicked) {
                if (inventoryCloseBounds.contains(mouseX, mouseY)) {
                    inventoryOpen = false;
                    inventoryPouchFocused = true;
                } else if (inventoryEquipButtonBounds.contains(mouseX, mouseY)) {
                    equipmentOpen = true;
                    inventoryOpen = false;
                    equipmentHoveredRow = -1;
                    equipmentHoveredSlot = -1;
                } else if (inventoryPouchIconBounds.contains(mouseX, mouseY)) {
                    inventoryPouchFocused = true;
                } else if (!inventoryPanelBounds.contains(mouseX, mouseY)) {
                    inventoryOpen = false;
                    inventoryPouchFocused = true;
                }
            }
        } else if (clicked && inventoryBagBounds.contains(mouseX, mouseY)) {
            inventoryOpen = true;
            inventoryPouchFocused = true;
        }
    }

    private void updateEquipmentInput(int mouseX, int mouseY, boolean clicked) {
        equipmentHoveredRow = -1;
        equipmentHoveredSlot = -1;
        for (int i = 0; i < equipmentRowBounds.size(); i++) {
            if (equipmentRowBounds.get(i).contains(mouseX, mouseY)) {
                equipmentHoveredRow = i;
                break;
            }
        }
        for (int i = 0; i < equipmentSlotBounds.length; i++) {
            if (equipmentSlotBounds[i] != null && equipmentSlotBounds[i].contains(mouseX, mouseY)) {
                equipmentHoveredSlot = i;
                break;
            }
        }
        if (!clicked) {
            return;
        }
        if (equipmentBackButtonBounds.contains(mouseX, mouseY)) {
            equipmentOpen = false;
            inventoryOpen = true;
            return;
        }
        if (equipmentHoveredRow >= 0) {
            List<Armour> owned = model.ownedArmour();
            if (equipmentHoveredRow < owned.size()) {
                model.equipArmour(owned.get(equipmentHoveredRow));
            }
            return;
        }
        if (equipmentHoveredSlot >= 0) {
            ShopEquipSlot slot = ShopEquipSlot.values()[equipmentHoveredSlot];
            if (model.getEquipped(slot) != null) {
                model.unequip(slot);
            }
        }
    }

    private void beginWalletReveal() {
        walletRevealFromCategory = state == ShopState.CATEGORY;
        state = ShopState.WALLET_REVEAL;
        walletRevealTicks = 0;
        inventoryOpen = false;
        equipmentOpen = false;
        currentDialog = DukeLines.walletReveal();
    }

    private void finishWalletReveal() {
        model.revealWallet();
        walletRevealTicks = 0;
        state = walletRevealFromCategory ? ShopState.CATEGORY : ShopState.IDLE;
        walletRevealFromCategory = false;
        currentDialog = DukeLines.walletRevealAfter();
    }

    private boolean isBuyButtonEnabled() {
        if (selectedRowIndex < 0 || selectedRowIndex >= catalogEntries.size()) {
            return false;
        }
        return model.canPurchase(catalogEntries.get(selectedRowIndex));
    }

    private boolean inventoryPanelContains(int mx, int my) {
        return inventoryPanelBounds.contains(mx, my);
    }

    private void tryPurchaseSelected() {
        if (selectedRowIndex < 0 || selectedRowIndex >= catalogEntries.size()) {
            return;
        }
        if (model.needsWalletReveal()) {
            beginWalletReveal();
            return;
        }
        ShopCatalogEntry entry = catalogEntries.get(selectedRowIndex);
        ShopModel.PurchaseResult result = model.purchase(entry);
        currentDialog = result.dukeLine();
        if (result.success()) {
            beginPurchaseReveal(entry);
        }
    }

    private void beginPurchaseReveal(ShopCatalogEntry entry) {
        purchaseRevealKeepRow = selectedRowIndex;
        if (selectedIndex >= 0 && selectedIndex < items.size()) {
            ShopItem cat = items.get(selectedIndex);
            purchaseRevealIcon = armourIcons.iconForEntry(entry, cat.category);
            if (purchaseRevealIcon == null) {
                purchaseRevealIcon = cat.cardArt != null ? cat.cardArt : cat.icon;
            }
        } else {
            purchaseRevealIcon = null;
        }
        purchaseRevealTicks = 0;
        inventoryOpen = false;
        equipmentOpen = false;
        state = ShopState.PURCHASE_REVEAL;
    }

    private void finishPurchaseReveal() {
        purchaseRevealTicks = 0;
        purchaseRevealIcon = null;
        if (selectedIndex >= 0) {
            int keepIndex = Math.min(purchaseRevealKeepRow, Math.max(0, catalogEntries.size() - 2));
            buildCatalogRows(items.get(selectedIndex));
            selectedRowIndex = catalogEntries.isEmpty()
                ? -1
                : Math.min(keepIndex, catalogEntries.size() - 1);
        }
        purchaseRevealKeepRow = -1;
        refreshShowcasePrices();
        state = ShopState.CATEGORY;
    }

    private void beginCategoryClose() {
        categoryClosing = true;
        categoryTicks = 0;
        state = ShopState.CATEGORY_CLOSING;
        hoveredRowIndex = -1;
    }

    private void skipCategoryAnimation() {
        if (state == ShopState.CATEGORY_OPENING) {
            categoryTicks = CATEGORY_OPEN_DURATION_TICKS;
            categoryClosing = false;
            state = ShopState.CATEGORY;
            return;
        }
        if (state == ShopState.CATEGORY_CLOSING) {
            categoryTicks = CATEGORY_OPEN_DURATION_TICKS;
            finishCategoryClose();
        }
    }

    private void finishCategoryClose() {
        categoryClosing = false;
        categoryTicks = 0;
        selectedIndex = -1;
        selectedRowIndex = -1;
        catalogEntries.clear();
        catalogScrollOffset = 0;
        state = ShopState.IDLE;
        currentDialog = IDLE_LINE;
    }

    private float categoryAnimProgress() {
        float t = categoryTicks / (float) CATEGORY_OPEN_DURATION_TICKS;
        if (categoryClosing) {
            return Math.max(0f, 1f - t);
        }
        return Math.min(1f, t);
    }

    private ShopCategoryAnimator categoryAnimator(ShopLayout layout) {
        Rectangle to = layout.leftCategoryCardSlot(assets.detailPanelW);
        float t = categoryAnimProgress();
        if (t >= 1f && !categoryClosing) {
            return ShopCategoryAnimator.open(to.x, to.y, to.width, to.height);
        }
        return ShopCategoryAnimator.opening(t,
            categoryFromRect.x, categoryFromRect.y, categoryFromRect.width, categoryFromRect.height,
            to.x, to.y, to.width, to.height);
    }

    private ShopRevealAnimator revealAnimator() {
        return switch (state) {
            case REVEAL -> ShopRevealAnimator.forProgress(
                revealTicks / (float) REVEAL_DURATION_TICKS, items.size(), true);
            case WALLET_REVEAL, PURCHASE_REVEAL -> ShopRevealAnimator.complete(items.size());
            default -> ShopRevealAnimator.complete(items.size());
        };
    }

    public void render(BufferedImage screen, int mouseX, int mouseY) {
        Graphics2D g = screen.createGraphics();
        int sw = screen.getWidth();
        int sh = screen.getHeight();
        applyCrispRendering(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        ShopLayout layout = new ShopLayout(sw, sh, items.size(),
            assets.hudX, assets.hudW, assets.hudH, assets.panelW,
            assets.panelHeaderH, assets.topRowCols, assets.bottomRowCols);
        ShopRevealAnimator reveal = revealAnimator();
        float brighten = reveal.sceneBrighten;

        drawScaledBackground(g, assets.merchantBgScaled, sw, sh, 0.75f * brighten);

        boolean categoryMode = state == ShopState.CATEGORY_OPENING
            || state == ShopState.CATEGORY || state == ShopState.CATEGORY_CLOSING;
        boolean walletScene = state == ShopState.WALLET_REVEAL;
        boolean purchaseScene = state == ShopState.PURCHASE_REVEAL;

        if (walletScene) {
            drawWalletRevealScene(g, sw, sh, layout, mouseX, mouseY);
            DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", currentDialog,
                DialogBoxRenderer.DUKE_COLOR, 1f);
            drawCursor(g, mouseX, mouseY);
            g.dispose();
            return;
        }

        if (purchaseScene) {
            drawPurchaseRevealScene(g, sw, sh, layout);
            DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", currentDialog,
                DialogBoxRenderer.DUKE_COLOR, 1f);
            drawCursor(g, mouseX, mouseY);
            g.dispose();
            return;
        }

        if (!categoryMode) {
            drawDarkOverlay(g, sw, sh, layout, brighten * Math.max(0.25f, reveal.panelAlpha * 0.85f));
        } else {
            drawCategoryOverlay(g, sw, sh, layout, brighten);
        }

        if (!categoryMode) {
            float portraitAlpha = brighten;
            BufferedImage dukeDraw = assets.dukeScaled;
            drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, portraitAlpha);
            drawCharacter(g, sw, sh, dukeDraw, false, layout.dialogTop, portraitAlpha);
        }

        if (!categoryMode) {
            drawHud(g, layout, reveal.hudAlpha, reveal.hudSlideY);
        }

        if (categoryMode && selectedIndex >= 0) {
            ShopCategoryAnimator catAnim = categoryAnimator(layout);
            drawCategoryView(g, layout, reveal, catAnim, mouseX, mouseY);
            drawCategoryStatLegend(g, layout, catAnim);
            drawCornerWallet(g, 1f);
        } else {
            drawCards(g, layout, reveal);
        }

        if (reveal.panelAlpha > 0.45f && !categoryMode) {
            drawAshParticles(g);
        }

        DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", currentDialog,
            DialogBoxRenderer.DUKE_COLOR, 1f);

        if (!model.needsWalletReveal()) {
            drawInventoryBag(g, 1f);
        }

        if (equipmentOpen) {
            drawEquipmentOverlay(g, sw, sh);
        } else if (inventoryOpen) {
            drawInventoryOverlay(g, sw, sh);
        }

        drawCursor(g, mouseX, mouseY);

        g.dispose();
    }

    private static void drawCursor(Graphics2D g, int mouseX, int mouseY) {
        if (MENU_CURSOR != null) {
            int cw = 28;
            int ch = Math.max(1, cw * MENU_CURSOR.getHeight() / MENU_CURSOR.getWidth());
            Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(MENU_CURSOR, mouseX - 4, mouseY - 4, cw, ch, null);
            if (prevInterp != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
            }
        }
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void clearExitRequest() {
        exitRequested = false;
    }

    private void drawWalletRevealScene(Graphics2D g, int sw, int sh, ShopLayout layout,
                                       int mouseX, int mouseY) {
        drawScaledBackground(g, assets.merchantBgScaled, sw, sh, 0.35f);

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);

        drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, 1f);
        drawCharacter(g, sw, sh, assets.dukeScaled, false, layout.dialogTop, 1f);

        drawWalletRevealBag(g, layout);
        drawWalletRevealPouch(g, layout);
    }

    private void drawPurchaseRevealScene(Graphics2D g, int sw, int sh, ShopLayout layout) {
        drawScaledBackground(g, assets.merchantBgScaled, sw, sh, 0.35f);

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);

        drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, 1f);
        drawCharacter(g, sw, sh, assets.dukeScaled, false, layout.dialogTop, 1f);

        drawPurchaseRevealBag(g, layout);
        drawPurchaseRevealItem(g, layout);
    }

    private void drawPurchaseRevealBag(Graphics2D g, ShopLayout layout) {
        Point slot = inventoryBagSlot();
        float openT = bagOpenProgress(
            purchaseRevealTicks,
            PURCHASE_APPEAR_TICKS,
            PURCHASE_FLY_TICKS,
            PURCHASE_FADE_TICKS,
            PURCHASE_CLOSE_TICKS);
        drawInventoryBagSprite(g, slot.x, slot.y, INVENTORY_BAG_SIZE, openT, false, 1f);
    }

    /** Прогресс открытия сумки: закрыта → открыта на полёте → держится открытой → закрывается. */
    private float bagOpenProgress(int ticks, int appearTicks, int flyTicks, int fadeTicks, int closeTicks) {
        int bagShow = Math.max(0, appearTicks - 4);
        int flyEnd = appearTicks + flyTicks;
        int fadeEnd = flyEnd + fadeTicks;
        int closeEnd = fadeEnd + closeTicks;
        if (ticks < bagShow) {
            return 0f;
        }
        if (ticks < flyEnd) {
            return smoothstep((ticks - bagShow) / (float) Math.max(1, flyEnd - bagShow));
        }
        if (ticks < fadeEnd) {
            return 1f;
        }
        if (ticks < closeEnd) {
            float closeT = (ticks - fadeEnd) / (float) Math.max(1, closeTicks);
            return smoothstep(1f - closeT);
        }
        return 0f;
    }

    private void drawPurchaseRevealItem(Graphics2D g, ShopLayout layout) {
        if (purchaseRevealIcon == null) {
            return;
        }

        int appearEnd = PURCHASE_APPEAR_TICKS;
        int flyEnd = appearEnd + PURCHASE_FLY_TICKS;
        int fadeEnd = flyEnd + PURCHASE_FADE_TICKS;
        int tuckEnd = fadeEnd + PURCHASE_CLOSE_TICKS;

        if (purchaseRevealTicks > fadeEnd) {
            return;
        }

        float appearT = smoothstep(purchaseRevealTicks / (float) appearEnd);
        float maxSize = 76f;
        float minSize = 14f;

        int centerX = VIRTUAL_W / 2;
        int centerY = layout.dialogTop / 2 + 4;
        Point bagSlot = inventoryBagSlot();
        float bagCenterX = bagSlot.x + INVENTORY_BAG_SIZE / 2f;
        float bagCenterY = bagSlot.y + INVENTORY_BAG_SIZE / 2f;

        float px;
        float py;
        float pw;
        float alpha;

        if (purchaseRevealTicks <= appearEnd) {
            pw = maxSize * (0.28f + appearT * 0.72f);
            px = centerX - pw / 2f;
            py = centerY - pw / 2f;
            alpha = Math.min(1f, appearT * 1.1f);
        } else {
            float posT = smoothstep((purchaseRevealTicks - appearEnd) / (float) PURCHASE_FLY_TICKS);
            float sizeT = posT * posT * (3f - 2f * posT);

            pw = maxSize + (minSize - maxSize) * sizeT;
            float cx = centerX + (bagCenterX - centerX) * posT;
            float cy = centerY + (bagCenterY - centerY) * posT;
            px = cx - pw / 2f;
            py = cy - pw / 2f;
            alpha = 1f;
            if (purchaseRevealTicks > flyEnd) {
                float fadeT = smoothstep((purchaseRevealTicks - flyEnd) / (float) PURCHASE_FADE_TICKS);
                pw = minSize;
                px = bagCenterX - pw / 2f;
                py = bagCenterY - pw / 2f;
                alpha = Math.max(0f, 1f - fadeT);
            }
        }

        if (alpha <= 0.02f) {
            return;
        }

        int ipw = Math.round(pw);
        int ipx = Math.round(px);
        int ipy = Math.round(py);

        float glowAppearT = purchaseRevealTicks <= appearEnd ? appearT : 1f;
        float glowFlyT = purchaseRevealTicks <= appearEnd ? 0f
            : Math.min(1f, (purchaseRevealTicks - appearEnd) / (float) PURCHASE_FLY_TICKS);
        float glowTuckT = purchaseRevealTicks > flyEnd
            ? smoothstep((purchaseRevealTicks - flyEnd) / (float) PURCHASE_FADE_TICKS) : 0f;
        drawItemRevealGlow(g, ipx, ipy, ipw, alpha, glowAppearT, glowFlyT, glowTuckT);

        Rectangle crop = computeContentBounds(purchaseRevealIcon);
        Composite comp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        if (crop.width > 0 && crop.height > 0) {
            g.drawImage(purchaseRevealIcon, ipx, ipy, ipx + ipw, ipy + ipw,
                crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
        } else {
            g.drawImage(purchaseRevealIcon, ipx, ipy, ipw, ipw, null);
        }
        g.setComposite(comp);
    }

    private void drawItemRevealGlow(Graphics2D g, int px, int py, int pw,
                                    float alpha, float appearT, float flyT, float tuckT) {
        Composite prev = g.getComposite();
        int cx = px + pw / 2;
        int cy = py + pw / 2;
        float glow = alpha * (0.4f + appearT * 0.5f) * (1f - flyT * 0.2f) * (1f - tuckT);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.2f));
        g.setColor(new Color(255, 210, 80));
        int outer = Math.round(pw * 1.7f);
        g.fillOval(cx - outer / 2, cy - outer / 2, outer, outer);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.38f));
        g.setColor(new Color(255, 235, 150));
        int mid = Math.round(pw * 1.1f);
        g.fillOval(cx - mid / 2, cy - mid / 2, mid, mid);
        g.setComposite(prev);
    }

    private Point inventoryBagSlot() {
        int bagX = INVENTORY_BAG_MARGIN;
        int bagY = INVENTORY_BAG_MARGIN;
        inventoryBagBounds.setBounds(bagX, bagY, INVENTORY_BAG_SIZE, INVENTORY_BAG_SIZE);
        return new Point(bagX, bagY);
    }

    private void drawWalletRevealBag(Graphics2D g, ShopLayout layout) {
        Point slot = inventoryBagSlot();
        int bagX = slot.x;
        int bagY = slot.y;
        int bagSize = INVENTORY_BAG_SIZE;

        int appearEnd = WALLET_APPEAR_TICKS;
        int flyEnd = appearEnd + WALLET_FLY_TICKS;
        int closeEnd = flyEnd + WALLET_BAG_CLOSE_TICKS;
        boolean bagVisible = walletRevealTicks >= appearEnd - 4;

        if (!bagVisible) {
            return;
        }

        float openT = bagOpenProgress(
            walletRevealTicks,
            WALLET_APPEAR_TICKS,
            WALLET_FLY_TICKS,
            WALLET_FADE_TICKS,
            WALLET_CLOSE_TICKS);
        float alpha = Math.min(1f, (walletRevealTicks - (appearEnd - 4)) / 8f);
        drawInventoryBagSprite(g, bagX, bagY, bagSize, openT, false, alpha);

        int countStart = closeEnd;
        if (walletRevealTicks >= countStart) {
            drawBagWalletAmount(g, bagX, bagY, bagSize, alpha);
        }
    }

    private void drawInventoryBag(Graphics2D g, float alpha) {
        Point slot = inventoryBagSlot();
        float openT = 0f;
        if (inventoryOpen && assets.inventoryBagOpenFrames != null
            && assets.inventoryBagOpenFrames.length > 0) {
            openT = 1f;
        }
        drawInventoryBagSprite(g, slot.x, slot.y, INVENTORY_BAG_SIZE, openT, inventoryBagHovered, alpha);
    }

    private void drawInventoryBagSprite(Graphics2D g, int x, int y, int size, float openT,
                                        boolean hovered, float alpha) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        BufferedImage sprite = pickBagSprite(openT, hovered);

        if (sprite != null) {
            drawScaledSprite(g, sprite, x, y, size, size, true);
        } else {
            g.setColor(new Color(72, 48, 28));
            g.fillRoundRect(x + 4, y + 10, size - 8, size - 14, 4, 4);
            g.setColor(new Color(110, 78, 42));
            g.drawRoundRect(x + 4, y + 10, size - 8, size - 14, 4, 4);
            if (openT > 0.2f) {
                g.setColor(new Color(30, 20, 12));
                g.fillOval(x + 8, y + 6, size - 16, 10);
            }
        }

        if (hovered && assets.inventoryBagHover == null && openT < 0.05f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.35f));
            g.setColor(new Color(255, 220, 120));
            g.drawRoundRect(x - 1, y - 1, size + 2, size + 2, 6, 6);
        }

        if (shouldShowPouchOnBag(openT) && assets.walletPouch != null) {
            int pouchSize = Math.max(12, size / 3);
            int pouchX = x + (size - pouchSize) / 2;
            int pouchY = y + size / 2 - pouchSize / 2 - 2;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.drawImage(assets.walletPouch, pouchX, pouchY, pouchSize, pouchSize, null);
        }
        g.setComposite(prev);
    }

    private boolean shouldShowPouchOnBag(float openT) {
        if (model.needsWalletReveal()) {
            return false;
        }
        if (state == ShopState.PURCHASE_REVEAL || state == ShopState.WALLET_REVEAL) {
            return false;
        }
        return openT < 0.001f;
    }

    private BufferedImage pickBagSprite(float openT, boolean hovered) {
        BufferedImage[] frames = assets.inventoryBagOpenFrames;
        if (frames != null && frames.length > 0) {
            if (openT > 0.001f) {
                int frame = Math.min(frames.length - 1,
                    Math.max(0, Math.round(openT * (frames.length - 1))));
                if (frames[frame] != null) {
                    return frames[frame];
                }
            } else if (state == ShopState.PURCHASE_REVEAL || state == ShopState.WALLET_REVEAL) {
                return frames[0];
            }
        }
        if (hovered && openT < 0.05f && assets.inventoryBagHover != null) {
            return assets.inventoryBagHover;
        }
        if (assets.inventoryBagClosed != null) {
            return assets.inventoryBagClosed;
        }
        return assets.inventoryBagIcon;
    }

    private void drawBagWalletAmount(Graphics2D g, int bagX, int bagY, int bagSize, float alpha) {
        if (model.needsWalletReveal()) {
            return;
        }
        String wallet = walletHudAmountText();
        drawCrispText(g);
        g.setFont(GameFonts.get().uiBold( 11));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(wallet);
        int tx = bagX + (bagSize - textW) / 2;
        int ty = bagY + bagSize + 12;
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(20, 14, 6, 200));
        g.fillRoundRect(tx - 4, ty - fm.getAscent(), textW + 8, fm.getHeight() + 2, 4, 4);
        g.setColor(new Color(255, 230, 150));
        g.drawString(wallet, tx, ty);
        g.setComposite(prev);
    }

    /** Кошелёк в правом верхнем углу — экран списка товаров. */
    private Rectangle cornerWalletBounds(Graphics2D g) {
        String wallet = walletHudAmountText();
        String suffix = model.walletSuffix();
        int crownSize = 16;
        int crownGap = 4;
        int margin = 8;
        int padX = 7;
        int padY = 4;

        drawCrispText(g);
        g.setFont(GameFonts.get().uiBold(12));
        FontMetrics fm = g.getFontMetrics();
        int blockW = fm.stringWidth(wallet) + fm.stringWidth(suffix);
        if (assets.crownIconScaled != null) {
            blockW += crownSize + crownGap;
        }
        int blockH = Math.max(crownSize, fm.getHeight()) + padY * 2;
        int blockX = VIRTUAL_W - margin - blockW - padX * 2;
        int blockY = INVENTORY_BAG_MARGIN;
        return new Rectangle(blockX, blockY, blockW + padX * 2, blockH);
    }

    private void drawCornerWallet(Graphics2D g, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        String wallet = walletHudAmountText();
        String suffix = model.walletSuffix();
        int crownSize = 16;
        int crownGap = 4;
        int padX = 7;

        Rectangle block = cornerWalletBounds(g);
        drawGoldHudChip(g, block, alpha);

        int blockX = block.x;
        int blockY = block.y;
        int blockW = block.width;
        int blockH = block.height;

        FontMetrics fm = g.getFontMetrics();
        int textX = blockX + padX;
        if (assets.crownIconScaled != null) {
            int crownY = blockY + (blockH - crownSize) / 2;
            g.drawImage(assets.crownIconScaled, textX, crownY, crownSize, crownSize, null);
            textX += crownSize + crownGap;
        }
        g.setColor(new Color(255, 230, 150));
        int walletY = blockY + (blockH + fm.getAscent()) / 2 - 2;
        g.drawString(wallet, textX, walletY);
        g.setColor(new Color(200, 180, 120));
        g.drawString(suffix, textX + fm.stringWidth(wallet), walletY);

        g.setComposite(prev);
    }

    private void drawCategoryStatLegend(Graphics2D g, ShopLayout layout, ShopCategoryAnimator cat) {
        if (selectedIndex < 0 || selectedIndex >= items.size()) {
            return;
        }
        if (items.get(selectedIndex).kind != ItemKind.PIECE) {
            return;
        }
        float alpha = cat.detailPanelAlpha;
        if (alpha <= 0.01f) {
            return;
        }

        Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
        int panelX = panel.x + Math.round(cat.detailPanelSlideX);
        int backRight = panelX + 4 + UiChrome.BTN_SIZE + 6;
        Rectangle wallet = cornerWalletBounds(g);
        int walletLeft = wallet.x;

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawCrispText(g);
        g.setFont(GameFonts.get().uiPlain(10));
        FontMetrics fm = g.getFontMetrics();
        Rectangle legendBox = ShopStatGlyphs.legendBounds(fm);
        int gap = walletLeft - backRight;
        int legendX = backRight + Math.max(0, (gap - legendBox.width) / 2);
        int legendY = INVENTORY_BAG_MARGIN + (UiChrome.BTN_SIZE - legendBox.height) / 2;
        ShopStatGlyphs.drawLegend(g, legendX, legendY, fm, alpha);
        g.setComposite(prev);
    }

    static void drawGoldHudChip(Graphics2D g, Rectangle box, float alpha) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(10, 7, 3, 200));
        g.fillRoundRect(box.x, box.y, box.width, box.height, 6, 6);
        g.setColor(new Color(140, 105, 45, 200));
        g.drawRoundRect(box.x, box.y, box.width, box.height, 6, 6);
        g.setColor(new Color(255, 210, 90, 60));
        g.drawRoundRect(box.x + 1, box.y + 1, box.width - 2, box.height - 2, 5, 5);
        g.setComposite(prev);
    }

    private void drawInventoryOverlay(Graphics2D g, int sw, int sh) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        int px = (sw - INVENTORY_PANEL_W) / 2;
        int py = (sh - INVENTORY_PANEL_H) / 2 - 16;
        inventoryPanelBounds.setBounds(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.96f));
        g.setColor(new Color(18, 12, 8, 245));
        g.fillRoundRect(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 8, 8);
        g.setColor(new Color(150, 110, 50));
        g.drawRoundRect(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 8, 8);

        drawCrispText(g);
        g.setFont(GameFonts.get().uiBold( 13));
        g.setColor(new Color(255, 220, 140));
        g.drawString("Инвентарь", px + 12, py + 20);

        inventoryCloseBounds.setBounds(UiChrome.closeButtonRect(px, py, INVENTORY_PANEL_W));
        UiChrome.drawCloseButton(g, inventoryCloseBounds, inventoryCloseHovered, 1f);

        int iconX = px + 12;
        int iconY = py + 34;
        inventoryPouchIconBounds.setBounds(iconX, iconY, INVENTORY_POUCH_ICON, INVENTORY_POUCH_ICON);
        drawInventoryPouchIcon(g, iconX, iconY, INVENTORY_POUCH_ICON,
            inventoryPouchFocused, inventoryPouchIconHovered);

        int detailX = px + 56;
        int detailY = py + 28;
        int detailW = INVENTORY_PANEL_W - 68;
        int detailBottom = iconY + INVENTORY_POUCH_ICON;
        if (inventoryPouchFocused) {
            detailBottom = drawInventoryPouchDetail(g, detailX, detailY, detailW);
        }

        int listY = detailBottom + 12;
        g.setColor(new Color(100, 75, 40, 140));
        g.drawLine(px + 10, listY - 4, px + INVENTORY_PANEL_W - 10, listY - 4);
        g.setFont(GameFonts.get().uiBold( 10));
        g.setColor(new Color(180, 140, 80));
        g.drawString("Куплено:", px + 12, listY + 10);
        listY += 24;

        g.setFont(GameFonts.get().uiPlain( 11));
        g.setColor(new Color(200, 180, 130));
        List<String> items = model.inventoryItemNames();
        if (items.isEmpty()) {
            g.drawString("Пока пусто…", px + 12, listY);
        } else {
            for (String name : items) {
                if (listY > py + INVENTORY_PANEL_H - 20) {
                    g.drawString("…", px + 12, listY);
                    break;
                }
                g.drawString("• " + truncateToWidth(name, g.getFontMetrics(), INVENTORY_PANEL_W - 24),
                    px + 12, listY);
                listY += 14;
            }
        }

        int equipBtnW = 108;
        int equipBtnH = 22;
        int equipBtnX = px + INVENTORY_PANEL_W - equipBtnW - 10;
        int equipBtnY = py + INVENTORY_PANEL_H - equipBtnH - 8;
        inventoryEquipButtonBounds.setBounds(equipBtnX, equipBtnY, equipBtnW, equipBtnH);
        g.setFont(GameFonts.get().uiBold( 10));
        g.setColor(new Color(28, 18, 8, 220));
        g.fillRoundRect(equipBtnX, equipBtnY, equipBtnW, equipBtnH, 5, 5);
        g.setColor(new Color(170, 125, 55));
        g.drawRoundRect(equipBtnX, equipBtnY, equipBtnW, equipBtnH, 5, 5);
        g.setColor(new Color(255, 225, 150));
        String equipLabel = "Экипировка";
        FontMetrics efm = g.getFontMetrics();
        g.drawString(equipLabel, equipBtnX + (equipBtnW - efm.stringWidth(equipLabel)) / 2,
            equipBtnY + 15);

        g.setComposite(prev);
    }

    private void drawEquipmentOverlay(Graphics2D g, int sw, int sh) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        int px = EQUIP_MARGIN;
        int py = EQUIP_MARGIN;
        int panelW = sw - EQUIP_MARGIN * 2;
        int panelH = sh - EQUIP_MARGIN * 2;
        equipmentPanelBounds.setBounds(px, py, panelW, panelH);
        equipmentRowBounds.clear();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.97f));
        g.setColor(new Color(14, 10, 6, 248));
        g.fillRoundRect(px, py, panelW, panelH, 6, 6);
        g.setColor(new Color(155, 115, 50));
        g.drawRoundRect(px, py, panelW, panelH, 6, 6);

        drawEquipText(g, GameFonts.get().uiBold(15), "Экипировка", px + 14, py + 24,
            new Color(255, 220, 140));

        int listX = px + 10;
        int listY = py + 34;
        int listW = 168;
        int listH = panelH - 48;
        g.setColor(new Color(8, 6, 4, 180));
        g.fillRoundRect(listX, listY, listW, listH, 4, 4);
        g.setColor(new Color(100, 75, 40));
        g.drawRoundRect(listX, listY, listW, listH, 4, 4);

        drawEquipText(g, GameFonts.get().uiBold(11), "Куплено", listX + 8, listY + 14,
            new Color(180, 140, 80));
        int itemsTop = listY + 22;
        g.setColor(new Color(90, 68, 36, 160));
        g.drawLine(listX + 6, itemsTop, listX + listW - 6, itemsTop);

        List<Armour> owned = model.ownedArmour();
        int rowH = 18;
        int rowY = itemsTop + 14;
        Font itemFont = GameFonts.get().uiPlain(11);
        FontMetrics itemFm = g.getFontMetrics(itemFont);
        for (int i = 0; i < owned.size(); i++) {
            if (rowY + 4 > listY + listH - 6) {
                break;
            }
            Armour armour = owned.get(i);
            Rectangle row = new Rectangle(listX + 4, rowY - 13, listW - 8, rowH);
            equipmentRowBounds.add(row);
            boolean hovered = i == equipmentHoveredRow;
            boolean equipped = model.isEquipped(armour);
            if (hovered || equipped) {
                g.setColor(equipped ? new Color(70, 52, 24, 200) : new Color(50, 38, 18, 170));
                g.fillRoundRect(row.x, row.y, row.width, row.height, 3, 3);
            }
            String line = truncateToWidth(armour.getName(), itemFm, listW - 20);
            drawEquipText(g, itemFont, line, listX + 8, rowY,
                equipped ? new Color(255, 230, 150) : new Color(200, 180, 130));
            rowY += rowH;
        }
        if (owned.isEmpty()) {
            drawEquipText(g, GameFonts.get().uiPlain(11), "Пока нет брони…", listX + 8, rowY,
                new Color(150, 130, 90));
        }

        int portraitX = listX + listW + 12;
        int portraitY = py + 30;
        int portraitW = 152;
        int portraitH = panelH - 86;
        g.setColor(new Color(6, 4, 2, 160));
        g.fillRoundRect(portraitX - 4, portraitY - 4, portraitW + 8, portraitH + 8, 6, 6);
        if (assets.geraltScaled != null) {
            drawScaledSprite(g, assets.geraltScaled, portraitX, portraitY, portraitW, portraitH, true);
        }

        int slotSize = 48;
        int slotGap = 10;
        int slotX = px + panelW - slotSize - 12;
        int slotY = py + 36;
        ShopEquipSlot[] slots = ShopEquipSlot.values();
        for (int i = 0; i < slots.length; i++) {
            ShopEquipSlot slot = slots[i];
            int sy = slotY + i * (slotSize + slotGap);
            equipmentSlotBounds[i] = new Rectangle(slotX, sy, slotSize, slotSize);
            boolean hovered = equipmentHoveredSlot == i;
            Armour equipped = model.getEquipped(slot);
            g.setColor(new Color(22, 14, 8, 220));
            g.fillRoundRect(slotX, sy, slotSize, slotSize, 4, 4);
            g.setColor(hovered ? new Color(200, 160, 70) : new Color(120, 90, 45));
            g.drawRoundRect(slotX, sy, slotSize, slotSize, 4, 4);
            BufferedImage icon = slot.iconIndex >= 0 && slot.iconIndex < assets.itemIcons.length
                ? assets.itemIcons[slot.iconIndex] : null;
            if (equipped != null) {
                ShopCategory slotCategory = switch (slot) {
                    case CHEST -> ShopCategory.CHEST;
                    case LEGS -> ShopCategory.LEGS;
                    case GLOVES -> ShopCategory.GLOVES;
                    case BOOTS -> ShopCategory.BOOTS;
                };
                BufferedImage armourArt = armourIcons.iconFor(equipped, slotCategory, 30);
                if (armourArt != null) {
                    icon = armourArt;
                }
            }
            if (equipped != null && icon != null) {
                int iconSz = 30;
                g.drawImage(icon, slotX + (slotSize - iconSz) / 2, sy + 7, iconSz, iconSz, null);
            } else if (icon != null) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                int iconSz = 26;
                g.drawImage(icon, slotX + (slotSize - iconSz) / 2, sy + 9, iconSz, iconSz, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.97f));
            }
            String slotLabel = slot.label;
            Font slotFont = GameFonts.get().uiPlain(9);
            FontMetrics sfm = g.getFontMetrics(slotFont);
            drawEquipText(g, slotFont, slotLabel,
                slotX + (slotSize - sfm.stringWidth(slotLabel)) / 2, sy + slotSize - 5,
                new Color(170, 140, 90));
        }

        int statsX = portraitX;
        int statsY = py + panelH - 80;
        int statsW = slotX + slotSize - statsX;
        int statsH = 70;
        g.setColor(new Color(10, 7, 4, 200));
        g.fillRoundRect(statsX, statsY, statsW, statsH, 4, 4);
        g.setColor(new Color(100, 75, 40));
        g.drawRoundRect(statsX, statsY, statsW, statsH, 4, 4);
        drawEquipmentStats(g, statsX, statsY, statsW, statsH, model.equippedStatPreview());

        int backW = 72;
        int backH = 22;
        int backX = px + 12;
        int backY = listY + listH - backH;
        equipmentBackButtonBounds.setBounds(backX, backY, backW, backH);
        g.setColor(new Color(28, 18, 8, 220));
        g.fillRoundRect(backX, backY, backW, backH, 5, 5);
        g.setColor(new Color(150, 110, 50));
        g.drawRoundRect(backX, backY, backW, backH, 5, 5);
        Font backFont = GameFonts.get().uiBold(11);
        FontMetrics bfm = g.getFontMetrics(backFont);
        String backLabel = "Назад";
        int labelX = backX + (backW - bfm.stringWidth(backLabel)) / 2;
        int labelY = backY + (backH + bfm.getAscent() - bfm.getDescent()) / 2;
        drawEquipText(g, backFont, backLabel, labelX, labelY, new Color(230, 200, 140));

        g.setComposite(prev);
    }

    private void drawEquipmentStats(Graphics2D g, int x, int y, int w, int h, ShopModel.StatPreview preview) {
        String header = "ХАРАКТЕРИСТИКИ";
        Font headerFont = GameFonts.get().uiBold(11);
        FontMetrics hfm = g.getFontMetrics(headerFont);
        drawEquipText(g, headerFont, header, x + (w - hfm.stringWidth(header)) / 2, y + 18,
            new Color(220, 200, 140));

        String[] labels = {"Защита", "Выносл.", "Знаки"};
        Font lineFont = GameFonts.get().uiPlain(11);
        int lineY = y + 36;
        ShopModel.StatRow[] rows = preview.rows();
        for (int i = 0; i < labels.length && i < rows.length; i++) {
            ShopModel.StatRow row = rows[i];
            String delta = "";
            if (row.delta() > 0) {
                delta = " (+" + row.delta() + ")";
            } else if (row.delta() < 0) {
                delta = " (" + row.delta() + ")";
            }
            drawEquipText(g, lineFont, labels[i] + ": " + row.value() + delta, x + 12, lineY,
                new Color(200, 180, 130));
            lineY += 15;
        }
    }

    private void drawInventoryPouchIcon(Graphics2D g, int x, int y, int size,
                                        boolean selected, boolean hovered) {
        if (assets.walletPouch == null) {
            return;
        }
        Composite prev = g.getComposite();
        if (selected || hovered) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.9f));
            g.setColor(selected ? new Color(120, 90, 40) : new Color(80, 60, 30));
            g.fillRoundRect(x - 2, y - 2, size + 4, size + 4, 4, 4);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.drawImage(assets.walletPouch, x, y, size, size, null);
        g.setComposite(prev);
    }

    private int drawInventoryPouchDetail(Graphics2D g, int x, int y, int maxW) {
        if (assets.walletPouch == null) {
            return y + INVENTORY_POUCH_ICON;
        }
        int large = INVENTORY_POUCH_LARGE;
        int pouchX = x + (maxW - large) / 2;
        int pouchY = y;
        Shape prevClip = g.getClip();
        g.clipRect(x, y, maxW, large + 40);

        Rectangle crop = computeContentBounds(assets.walletPouch);
        if (crop.width > 0 && crop.height > 0) {
            g.drawImage(assets.walletPouch, pouchX, pouchY, pouchX + large, pouchY + large,
                crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
        } else {
            g.drawImage(assets.walletPouch, pouchX, pouchY, large, large, null);
        }

        String amount = model.walletAmountText() + model.walletSuffix();
        String[] lines = {
            "Золотой мешок с гонораром.",
            amount + " — плата за Арнскрон."
        };
        g.setFont(GameFonts.get().uiPlain( 10));
        g.setColor(new Color(220, 195, 130));
        int textY = pouchY + large + 12;
        for (String line : lines) {
            g.drawString(truncateToWidth(line, g.getFontMetrics(), maxW), x, textY);
            textY += 13;
        }
        g.setClip(prevClip);
        return textY;
    }

    private static float smoothstep(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    private void drawDarkOverlay(Graphics2D g, int sw, int sh, ShopLayout layout, float alpha) {
        Composite prev = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f * alpha));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * alpha));
        int pad = 6;
        g.fillRoundRect(layout.panelX - pad, layout.panelY - pad,
            layout.panelW + pad * 2, layout.panelH + pad * 2, 4, 4);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f * alpha));
        GradientPaint sides = new GradientPaint(0, 0, new Color(0, 0, 0, 200),
            layout.panelX - 20, 0, new Color(0, 0, 0, 0));
        g.setPaint(sides);
        g.fillRect(0, 0, layout.panelX - 10, layout.dialogTop);
        GradientPaint right = new GradientPaint(layout.panelX + layout.panelW + 20, 0,
            new Color(0, 0, 0, 0), sw, 0, new Color(0, 0, 0, 200));
        g.setPaint(right);
        g.fillRect(layout.panelX + layout.panelW + 10, 0, sw - layout.panelX - layout.panelW - 10, layout.dialogTop);

        g.setComposite(prev);
    }

    private void drawCategoryOverlay(Graphics2D g, int sw, int sh, ShopLayout layout, float alpha) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f * alpha));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);
    }

    private void drawHud(Graphics2D g, ShopLayout layout, float alpha, float slideY) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int hudY = layout.hudY + Math.round(slideY);

        if (assets.hudBar != null) {
            g.drawImage(assets.hudBar, layout.hudX, hudY, null);
        } else {
            g.setColor(new Color(10, 8, 4, 220));
            g.fillRect(layout.hudX, hudY, layout.hudW, layout.hudH);
        }

        if (assets.dukeSealIconScaled != null) {
            int seal = assets.dukeSealSize;
            int sealX = layout.hudX + 12;
            int sealY = hudY + (layout.hudH - seal) / 2;
            drawCrispIcon(g, assets.dukeSealIconScaled, sealX, sealY, seal);
        }

        String wallet = walletHudAmountText();
        String suffix = model.walletSuffix();
        int crownSize = 18;
        int crownGap = 4;
        drawCrispText(g);
        g.setFont(GameFonts.get().uiBold( 13));
        FontMetrics fm = g.getFontMetrics();
        int blockW = fm.stringWidth(wallet) + fm.stringWidth(suffix);
        if (assets.crownIconScaled != null) {
            blockW += crownSize + crownGap;
        }
        int blockX = layout.hudX + (layout.hudW - blockW) / 2;
        int textX = blockX;
        if (assets.crownIconScaled != null) {
            int crownY = hudY + (layout.hudH - crownSize) / 2;
            g.drawImage(assets.crownIconScaled, blockX, crownY, null);
            textX = blockX + crownSize + crownGap;
        }
        g.setColor(new Color(255, 230, 150));
        int walletY = hudY + (layout.hudH + fm.getAscent()) / 2 - 2;
        g.drawString(wallet, textX, walletY);
        g.setColor(new Color(200, 180, 120));
        g.drawString(suffix, textX + fm.stringWidth(wallet), walletY);
        g.setComposite(prev);
    }

    private String walletHudAmountText() {
        if (state != ShopState.WALLET_REVEAL) {
            return model.walletAmountText();
        }
        int countStart = WALLET_APPEAR_TICKS + WALLET_FLY_TICKS + WALLET_BAG_CLOSE_TICKS;
        if (walletRevealTicks < countStart) {
            return "???";
        }
        float t = Math.min(1f, (walletRevealTicks - countStart) / (float) WALLET_COUNT_TICKS);
        t = t * t * (3f - 2f * t);
        return String.valueOf(Math.round(model.getWallet() * t));
    }

    private void drawWalletRevealPouch(Graphics2D g, ShopLayout layout) {
        if (assets.walletPouch == null) {
            return;
        }

        int appearEnd = WALLET_APPEAR_TICKS;
        int flyEnd = appearEnd + WALLET_FLY_TICKS;
        int fadeEnd = flyEnd + WALLET_FADE_TICKS;
        int closeEnd = fadeEnd + WALLET_CLOSE_TICKS;

        if (walletRevealTicks > fadeEnd) {
            return;
        }

        float appearT = smoothstep(walletRevealTicks / (float) appearEnd);
        float maxSize = 80f;
        float minSize = 13f;

        int centerX = VIRTUAL_W / 2;
        int centerY = layout.dialogTop / 2 + 6;
        Point bagSlot = inventoryBagSlot();
        float bagCenterX = bagSlot.x + INVENTORY_BAG_SIZE / 2f;
        float bagCenterY = bagSlot.y + INVENTORY_BAG_SIZE / 2f;

        float px;
        float py;
        float pw;
        float alpha;

        if (walletRevealTicks <= appearEnd) {
            pw = maxSize * (0.32f + appearT * 0.68f);
            px = centerX - pw / 2f;
            py = centerY - pw / 2f;
            alpha = Math.min(1f, appearT * 1.15f);
        } else {
            float posT = smoothstep((walletRevealTicks - appearEnd) / (float) WALLET_FLY_TICKS);
            float sizeT = posT * posT * (3f - 2f * posT);

            pw = maxSize + (minSize - maxSize) * sizeT;
            float cx = centerX + (bagCenterX - centerX) * posT;
            float cy = centerY + (bagCenterY - centerY) * posT;
            px = cx - pw / 2f;
            py = cy - pw / 2f;
            alpha = 1f;
            if (walletRevealTicks > flyEnd) {
                float fadeT = smoothstep((walletRevealTicks - flyEnd) / (float) WALLET_FADE_TICKS);
                pw = minSize;
                px = bagCenterX - pw / 2f;
                py = bagCenterY - pw / 2f;
                alpha = Math.max(0f, 1f - fadeT);
            }
        }

        if (alpha <= 0.02f) {
            return;
        }

        int ipw = Math.round(pw);
        int iph = ipw;
        int ipx = Math.round(px);
        int ipy = Math.round(py);

        float glowAppearT = walletRevealTicks <= appearEnd ? appearT : 1f;
        float glowFlyT = walletRevealTicks <= appearEnd ? 0f
            : Math.min(1f, (walletRevealTicks - appearEnd) / (float) WALLET_FLY_TICKS);
        float glowTuckT = walletRevealTicks > flyEnd
            ? smoothstep((walletRevealTicks - flyEnd) / (float) WALLET_FADE_TICKS) : 0f;
        drawPouchGlow(g, ipx, ipy, ipw, iph, alpha, glowAppearT, glowFlyT, glowTuckT);

        Rectangle crop = computeContentBounds(assets.walletPouch);
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        if (crop.width > 0 && crop.height > 0) {
            g.drawImage(assets.walletPouch, ipx, ipy, ipx + ipw, ipy + iph,
                crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
        } else {
            g.drawImage(assets.walletPouch, ipx, ipy, ipw, iph, null);
        }
        g.setComposite(prev);
    }

    private void drawPouchGlow(Graphics2D g, int px, int py, int pw, int ph,
                               float alpha, float appearT, float flyT, float tuckT) {
        Composite prev = g.getComposite();
        int cx = px + pw / 2;
        int cy = py + ph / 2;
        float glow = alpha * (0.45f + appearT * 0.55f) * (1f - flyT * 0.25f) * (1f - tuckT);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.22f));
        g.setColor(new Color(255, 200, 60));
        int outer = Math.round(pw * 1.8f);
        g.fillOval(cx - outer / 2, cy - outer / 2, outer, outer);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.45f));
        g.setColor(new Color(255, 230, 120));
        int mid = Math.round(pw * 1.15f);
        g.fillOval(cx - mid / 2, cy - mid / 2, mid, mid);

        if (flyT > 0.05f && flyT < 0.95f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.3f * (1f - flyT)));
            g.setColor(new Color(255, 215, 90));
            g.fillOval(cx - 4, cy - 4, 8, 8);
        }
        g.setComposite(prev);
    }

    private void drawCards(Graphics2D g, ShopLayout layout, ShopRevealAnimator reveal) {
        if (reveal.panelAlpha <= 0.01f) {
            for (int i = 0; i < items.size(); i++) {
                items.get(i).bounds.setBounds(0, 0, 0, 0);
            }
            return;
        }

        Composite layer = g.getComposite();
        float scale = reveal.panelScale;
        int panelCx = layout.panelX + layout.panelW / 2;
        int panelCy = layout.panelY + layout.panelH / 2 + Math.round(reveal.panelSlideY);

        if (assets.catalogPanelScaled != null) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, reveal.panelAlpha));
            drawScaledCentered(g, assets.catalogPanelScaled,
                layout.panelX, layout.panelY + Math.round(reveal.panelSlideY),
                layout.panelW, layout.panelH, panelCx, panelCy, scale);
        }

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            Point slot = layout.cardSlot(i);
            float cardA = i < reveal.cardAlpha.length ? reveal.cardAlpha[i] : 1f;
            float cardS = i < reveal.cardScale.length ? reveal.cardScale[i] : 1f;
            float cardSlide = i < reveal.cardSlideY.length ? reveal.cardSlideY[i] : 0f;
            if (cardA <= 0.01f) {
                item.bounds.setBounds(0, 0, 0, 0);
                continue;
            }
            int cardW = Math.round(layout.cardW * cardS);
            int cardH = Math.round(layout.cardH * cardS);
            int cardX = slot.x + (layout.cardW - cardW) / 2;
            int cardY = slot.y + (layout.cardH - cardH) / 2 + Math.round(cardSlide);
            item.bounds.setBounds(cardX, cardY, cardW, cardH);
            drawItemCard(g, item, cardX, cardY, cardW, cardH, i,
                false, i == hoveredIndex, cardA);
        }

        g.setComposite(layer);
    }

    private void drawCategoryView(Graphics2D g, ShopLayout layout, ShopRevealAnimator reveal,
                                  ShopCategoryAnimator cat, int mouseX, int mouseY) {
        Composite layer = g.getComposite();
        ShopItem item = items.get(selectedIndex);

        if (assets.counterForeground != null && cat.counterAlpha > 0.02f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cat.counterAlpha));
            int cy = layout.categoryCounterY();
            int ch = layout.categoryCounterH(layout.dialogTop);
            g.drawImage(assets.counterForeground, assets.counterX, cy, assets.counterW, ch, null);
        }

        for (int i = 0; i < items.size(); i++) {
            if (i == selectedIndex) {
                continue;
            }
            if (cat.gridCardsAlpha <= 0.02f) {
                items.get(i).bounds.setBounds(0, 0, 0, 0);
                continue;
            }
            ShopItem other = items.get(i);
            Point slot = layout.cardSlot(i);
            float cardA = (i < reveal.cardAlpha.length ? reveal.cardAlpha[i] : 1f) * cat.gridCardsAlpha;
            items.get(i).bounds.setBounds(0, 0, 0, 0);
            drawItemCard(g, other, slot.x, slot.y, layout.cardW, layout.cardH,
                i, false, false, cardA);
        }

        item.bounds.setBounds(cat.cardX, cat.cardY, cat.cardW, cat.cardH);
        drawFlippingCategoryCard(g, item, cat.cardX, cat.cardY, cat.cardW, cat.cardH, true);

        if (cat.detailPanelAlpha > 0.02f) {
            Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
            int px = panel.x + Math.round(cat.detailPanelSlideX);
            int py = panel.y;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cat.detailPanelAlpha));
            if (assets.catalogDetailPanel != null) {
                g.drawImage(assets.catalogDetailPanel, px, py, null);
            }
            drawCatalogRows(g, px, py, cat.listInteractive);
            drawCategoryBackButton(g, px, cat.detailPanelAlpha, cat.listInteractive);
            drawCategoryBuyButton(g, px, py, cat.detailPanelAlpha, cat.listInteractive);
        }

        g.setComposite(layer);
    }

    private BufferedImage itemArtForEntry(ShopCatalogEntry entry, ShopItem categoryItem) {
        if (categoryItem == null || categoryItem.kind != ItemKind.PIECE) {
            return null;
        }
        if (entry != null && entry.armour != null) {
            BufferedImage icon = armourIcons.iconForEntry(entry, categoryItem.category);
            if (icon != null) {
                return icon;
            }
        }
        return categoryItem.cardArt != null ? categoryItem.cardArt : categoryItem.icon;
    }

    /** Переворот категории → товар при открытии и обратно при закрытии. */
    private void drawFlippingCategoryCard(Graphics2D g, ShopItem item, int x, int y, int w, int h,
                                          boolean smoothIconGrowth) {
        float flipT = categoryAnimProgress();
        float scaleX = Math.abs((float) Math.cos(flipT * Math.PI));
        if (scaleX < 0.04f) {
            scaleX = 0.04f;
        }
        boolean itemFace = flipT >= 0.5f;

        AffineTransform saved = g.getTransform();
        int cx = x + w / 2;
        int cy = y + h / 2;
        AffineTransform flip = new AffineTransform(saved);
        flip.translate(cx, cy);
        flip.scale(scaleX, 1.0);
        flip.translate(-cx, -cy);
        g.setTransform(flip);

        ShopCatalogEntry statEntry = selectedCatalogEntry();
        String priceForCard = itemFace ? selectedCatalogPrice() : null;
        BufferedImage cardArt = itemFace ? itemArtForEntry(statEntry, item) : null;
        String nameOverride = itemFace && statEntry != null ? statEntry.name : null;
        BufferedImage frame = itemFace
            ? (assets.cardBackScaled != null ? assets.cardBackScaled : assets.cardFrontScaled)
            : assets.cardSelectedScaled;

        drawItemCard(g, item, x, y, w, h, selectedIndex, true, false, 1f,
            priceForCard, smoothIconGrowth, cardArt, nameOverride, frame);

        g.setTransform(saved);
    }

    private ShopCatalogEntry selectedCatalogEntry() {
        if (selectedRowIndex >= 0 && selectedRowIndex < catalogEntries.size()) {
            return catalogEntries.get(selectedRowIndex);
        }
        return null;
    }

    private String selectedCatalogPrice() {
        if (selectedRowIndex >= 0 && selectedRowIndex < catalogEntries.size()) {
            return catalogEntries.get(selectedRowIndex).priceLabel();
        }
        if (selectedIndex >= 0 && selectedIndex < items.size()) {
            return items.get(selectedIndex).priceLabel;
        }
        return "···";
    }

    private void drawCategoryBackButton(Graphics2D g, int panelX, float alpha, boolean interactive) {
        if (alpha <= 0.01f) {
            categoryBackBounds.setBounds(0, 0, 0, 0);
            return;
        }
        int size = UiChrome.BTN_SIZE;
        int backX = panelX + 4;
        int backY = INVENTORY_BAG_MARGIN;
        if (interactive) {
            categoryBackBounds.setBounds(backX, backY, size, size);
        } else {
            categoryBackBounds.setBounds(0, 0, 0, 0);
        }
        UiChrome.drawArrowBackButton(g, categoryBackBounds, categoryBackHovered, alpha);
    }

    private void drawCategoryBuyButton(Graphics2D g, int panelX, int panelY, float alpha, boolean interactive) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int btnW = assets.btnW;
        int btnH = assets.btnH;
        int btnX = panelX + (assets.detailPanelW - btnW) / 2;
        int btnY = panelY + assets.detailPanelH - btnH - 8;
        boolean enabled = isBuyButtonEnabled();
        if (interactive) {
            categoryBuyBounds.setBounds(btnX, btnY, btnW, btnH);
        } else {
            categoryBuyBounds.setBounds(0, 0, 0, 0);
        }
        BufferedImage btnImg = enabled ? assets.btnBuyNormal : assets.btnBuyDisabled;
        if (btnImg == null) {
            btnImg = assets.btnBuyDisabled;
        }
        if (btnImg != null) {
            g.drawImage(btnImg, btnX, btnY, null);
        }
        drawCrispText(g);
        g.setFont(cardFont(10));
        String label = "Купить";
        FontMetrics fm = g.getFontMetrics();
        int tx = btnX + (btnW - fm.stringWidth(label)) / 2;
        Color labelColor = enabled
            ? (categoryBuyHovered ? new Color(255, 240, 180) : new Color(220, 200, 140))
            : new Color(120, 105, 75);
        drawOutlinedText(g, label, tx, btnY + 19, labelColor);
        g.setComposite(prev);
    }

    private int[] catalogStatDeltas(ShopCatalogEntry row) {
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

    private void drawCatalogRows(Graphics2D g, int panelX, int panelY, boolean interactive) {
        int rowGap = 4;
        int rowStep = assets.rowH + rowGap;
        int listTop = catalogListTop(panelY);
        int listBottom = catalogListBottom(panelY);
        int clipX = panelX + 6;
        int clipW = assets.detailPanelW - 12;
        int clipH = listBottom - listTop;
        int x = panelX + 8;

        Shape prevClip = g.getClip();
        g.clipRect(clipX, listTop, clipW, clipH);

        drawCardText(g);
        g.setFont(GameFonts.get().uiPlain(9));

        for (int i = 0; i < catalogEntries.size(); i++) {
            ShopCatalogEntry row = catalogEntries.get(i);
            int y = listTop + i * rowStep - catalogScrollOffset;
            if (y + assets.rowH < listTop || y > listBottom) {
                row.bounds.setBounds(0, 0, 0, 0);
                continue;
            }

            boolean hovered = interactive && i == hoveredRowIndex;
            boolean selected = i == selectedRowIndex;

            BufferedImage bg = assets.rowNormal;
            if (selected && assets.rowSelected != null) {
                bg = assets.rowSelected;
            } else if (hovered && assets.rowHover != null) {
                bg = assets.rowHover;
            }
            if (bg != null) {
                g.drawImage(bg, x, y, null);
            }

            row.bounds.setBounds(interactive ? x : 0, interactive ? y : 0, assets.rowW, assets.rowH);

            g.setFont(GameFonts.get().uiPlain(9));
            FontMetrics fm = g.getFontMetrics();
            String price = row.priceLabel();
            int priceW = fm.stringWidth(price);
            if (assets.crownIconSmall != null && !price.equals("···")) {
                priceW += assets.crownIconSmall.getWidth() + 2;
            }
            int priceX = x + assets.rowW - priceW - 8;

            int[] deltas = catalogStatDeltas(row);
            g.setFont(GameFonts.get().uiPlain(8));
            FontMetrics statsFm = g.getFontMetrics();
            int statsW = ShopStatGlyphs.rowWidth(statsFm, deltas[0], deltas[1], deltas[2]);
            if (statsW > 0) {
                statsW += 4;
            }
            int statsRight = priceX - 2;

            g.setFont(GameFonts.get().uiPlain(9));
            fm = g.getFontMetrics();
            int nameMaxW = Math.max(20, statsRight - statsW - (x + 10));
            String label = truncateToWidth(row.name, fm, nameMaxW);
            int textY = y + (assets.rowH + fm.getAscent()) / 2 - 1;
            drawOutlinedText(g, label, x + 10, textY, new Color(235, 215, 155));

            if (statsW > 0) {
                g.setFont(GameFonts.get().uiPlain(8));
                statsFm = g.getFontMetrics();
                ShopStatGlyphs.drawRow(g, statsRight, textY, statsFm, deltas[0], deltas[1], deltas[2]);
            }

            if (assets.crownIconSmall != null && !price.equals("···")) {
                g.drawImage(assets.crownIconSmall, priceX, y + 6, null);
                priceX += assets.crownIconSmall.getWidth() + 2;
            }
            drawOutlinedText(g, price, priceX, textY, new Color(255, 220, 100));
        }

        g.setClip(prevClip);

        if (interactive && maxCatalogScroll(panelY) > 0) {
            drawCatalogScrollHint(g, clipX + clipW - 5, listTop, clipH - 8, panelY);
        }
    }

    private void drawCatalogScrollHint(Graphics2D g, int x, int trackTop, int trackH, int panelY) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(new Color(200, 170, 90));
        int thumbH = Math.max(12, trackH / 4);
        int max = maxCatalogScroll(panelY);
        float t = max > 0 ? catalogScrollOffset / (float) max : 0f;
        int thumbY = trackTop + Math.round((trackH - thumbH) * t);
        g.fillRoundRect(x, thumbY, 3, thumbH, 2, 2);
        g.setComposite(prev);
    }

    private static void drawScaledCentered(Graphics2D g, BufferedImage img,
                                           int x, int y, int w, int h, int cx, int cy, float scale) {
        int sw = Math.round(w * scale);
        int sh = Math.round(h * scale);
        int sx = cx - sw / 2;
        int sy = cy - sh / 2;
        g.drawImage(img, sx, sy, sw, sh, null);
    }

    private void drawItemCard(Graphics2D g, ShopItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered, revealAlpha, null);
    }

    private void drawItemCard(Graphics2D g, ShopItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered, revealAlpha, priceOverride, false);
    }

    private void drawItemCard(Graphics2D g, ShopItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride,
                              boolean smoothIconGrowth) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered,
            revealAlpha, priceOverride, smoothIconGrowth, null);
    }

    private void drawItemCard(Graphics2D g, ShopItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride,
                              boolean smoothIconGrowth, BufferedImage cardArtOverride) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered,
            revealAlpha, priceOverride, smoothIconGrowth, cardArtOverride, null);
    }

    private void drawItemCard(Graphics2D g, ShopItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride,
                              boolean smoothIconGrowth, BufferedImage cardArtOverride, String nameOverride) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered,
            revealAlpha, priceOverride, smoothIconGrowth, cardArtOverride, nameOverride, null);
    }

    private void drawItemCard(Graphics2D g, ShopItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride,
                              boolean smoothIconGrowth, BufferedImage cardArtOverride, String nameOverride,
                              BufferedImage frameOverride) {
        Composite savedComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, revealAlpha));

        BufferedImage frame = frameOverride;
        if (frame == null) {
            frame = assets.cardFrontScaled;
            if (selected && assets.cardSelectedScaled != null) {
                frame = assets.cardSelectedScaled;
            } else if (hovered && assets.cardHoverScaled != null) {
                frame = assets.cardHoverScaled;
            }
        }

        Rectangle cardRect;
        if (frame != null) {
            Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(frame, x, y, w, h, null);
            if (prevInterp != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
            }
            cardRect = new Rectangle(x, y, w, h);
        } else {
            drawFallbackCard(g, x, y, w, h, false);
            cardRect = new Rectangle(x, y, w, h);
        }
        drawCardFrontContent(g, item, cardRect, w, h, priceOverride, smoothIconGrowth, cardArtOverride, nameOverride);

        g.setComposite(savedComposite);
    }

    private void drawFallbackCard(Graphics2D g, int x, int y, int w, int h, boolean back) {
        g.setColor(back ? new Color(28, 22, 14, 235) : new Color(38, 28, 16, 235));
        g.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
        g.setColor(new Color(180, 130, 45));
        g.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
    }

    private void drawCardFrontContent(Graphics2D g, ShopItem item, Rectangle card, int w, int h,
                                    String priceOverride, boolean smoothIconGrowth,
                                    BufferedImage cardArtOverride, String nameOverride) {
        int x = card.x;
        int y = card.y;
        boolean categoryGrid = priceOverride == null;

        drawCardText(g);
        int fontSize;
        if (categoryGrid) {
            fontSize = w >= 50 ? 11 : Math.max(9, Math.round(11f * w / 54f));
        } else {
            fontSize = w < 60 ? 8 : (h > 200 ? 12 : (w < 90 ? 9 : 10));
        }
        g.setFont(categoryGrid ? GameFonts.get().uiPlain(fontSize) : cardFont(fontSize));
        String name = nameOverride != null ? nameOverride : item.displayName();
        if (categoryGrid) {
            g.setFont(fitUiFontToWidth(g, name, w - 4, fontSize, 7));
        } else {
            g.setFont(fitUiFontToWidth(g, name, w - 8, fontSize, 7));
        }
        FontMetrics nameFm = g.getFontMetrics();
        int nameY = categoryGrid
            ? y + h - 6
            : priceOverride != null
                ? y + h - Math.max(18, Math.round(h * 0.22f))
                : y + h - 10;

        BufferedImage art = cardArtOverride != null
            ? cardArtOverride
            : item.cardArt != null ? item.cardArt : item.icon;
        if (art != null) {
            int slotX = x + 4;
            int slotW = w - 8;
            int slotTop = y + Math.round(h * 0.12f);
            int labelReserve = categoryGrid
                ? nameFm.getHeight() + 6
                : Math.round(h * 0.10f);
            int slotBottom = nameY - labelReserve;
            int slotH = Math.max(1, slotBottom - slotTop);
            Rectangle crop = computeContentBounds(art);
            int maxArt = iconCapForCard(slotW, slotH, smoothIconGrowth);
            Rectangle artBounds = aspectFitCroppedBounds(crop, slotX, slotTop, slotW, slotH, maxArt);
            if (!categoryGrid) {
                drawItemArtGoldContour(g, artBounds);
            }
            drawCroppedScaledSprite(g, art, crop, artBounds.x, artBounds.y,
                artBounds.width, artBounds.height, true);
        }

        Color nameColor = item.kind == ItemKind.SET_CATALOG
            ? new Color(255, 210, 100) : new Color(245, 230, 190);
        if (categoryGrid) {
            drawCategoryGridLabel(g, name, x, y, w, h, nameColor, nameFm);
        } else {
            int nameX = x + (w - nameFm.stringWidth(name)) / 2;
            drawOutlinedText(g, name, nameX, nameY, nameColor);
        }

        if (priceOverride == null) {
            return;
        }

        String priceLabel = priceOverride;
        if (item.kind == ItemKind.SET_CATALOG && "···".equals(priceLabel)) {
            return;
        }

        g.setFont(cardFont(Math.max(7, fontSize)));
        FontMetrics priceFm = g.getFontMetrics();
        BufferedImage coin = assets.crownIconSmall != null ? assets.crownIconSmall : assets.crownIconScaled;
        int priceW = priceFm.stringWidth(priceLabel);
        int coinH = coin != null ? coin.getHeight() : 0;
        if (coin != null) {
            priceW += coin.getWidth() + 2;
        }
        int priceRowY = y + h - 8;
        int priceX = x + (w - priceW) / 2;
        if (coin != null) {
            int coinY = priceRowY - coinH + 1;
            g.drawImage(coin, priceX, coinY, null);
            priceX += coin.getWidth() + 2;
        }
        drawOutlinedText(g, priceLabel, priceX, priceRowY, new Color(255, 220, 90));
    }

    /** Мягкая золотая подложка под иконку товара — читается на тёмном фоне карточки. */
    private static void drawItemArtGoldContour(Graphics2D g, Rectangle drawBounds) {
        if (drawBounds == null || drawBounds.width <= 0 || drawBounds.height <= 0) {
            return;
        }
        int pad = Math.max(3, Math.min(drawBounds.width, drawBounds.height) / 9);
        int rx = drawBounds.x - pad;
        int ry = drawBounds.y - pad;
        int rw = drawBounds.width + pad * 2;
        int rh = drawBounds.height + pad * 2;

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.38f));
        g.setColor(new Color(255, 196, 64));
        g.fillRoundRect(rx, ry, rw, rh, 6, 6);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setColor(new Color(10, 7, 3, 170));
        g.fillRoundRect(rx + 1, ry + 1, rw - 2, rh - 2, 5, 5);
        g.setColor(new Color(150, 110, 40, 220));
        g.drawRoundRect(rx, ry, rw, rh, 6, 6);
        g.setColor(new Color(255, 225, 140, 110));
        g.drawRoundRect(rx + 1, ry + 1, rw - 2, rh - 2, 5, 5);
        g.setComposite(prev);
    }

    /** Иконка растёт вместе с слотом; при анимации — без скачка 32→96. */
    private int iconCapForCard(int slotW, int slotH, boolean smoothGrowth) {
        int base = assets.cardArtSize;
        int slotMax = Math.min(slotW, slotH);
        int target = Math.round(slotMax * 0.72f);
        if (smoothGrowth) {
            return Math.max(base, Math.min(slotMax, target));
        }
        if (slotMax <= base + 2) {
            return base;
        }
        int mult = Math.max(1, target / base);
        int cap = mult * base;
        if (cap > slotMax) {
            cap = Math.max(base, (slotMax / base) * base);
        }
        return cap;
    }

    private static Font cardFont(int size) {
        return GameFonts.get().uiBold(size);
    }

    private static void drawCardText(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /** Подпись на витрине — одна строка, шрифт уже подогнан под ширину карточки. */
    private static void drawCategoryGridLabel(Graphics2D g, String name, int x, int y, int w, int h,
                                              Color color, FontMetrics fm) {
        int nameY = y + h - 6;
        int nameX = x + (w - fm.stringWidth(name)) / 2;
        drawCategoryLabel(g, name, nameX, nameY, color);
    }

    private static Font fitUiFontToWidth(Graphics2D g, String text, int maxWidth, int startSize, int minSize) {
        for (int size = startSize; size >= minSize; size--) {
            Font font = GameFonts.get().uiPlain(size);
            g.setFont(font);
            if (g.getFontMetrics().stringWidth(text) <= maxWidth) {
                return font;
            }
        }
        Font font = GameFonts.get().uiPlain(minSize);
        g.setFont(font);
        return font;
    }

    /** Подпись на сетке категорий — крупнее и строго по пиксельной сетке. */
    private static void drawCategoryLabel(Graphics2D g, String text, int tx, int ty, Color fill) {
        tx = (tx + 1) & ~1;
        ty = (ty + 1) & ~1;
        g.setColor(new Color(8, 4, 2, 240));
        g.drawString(text, tx + 1, ty);
        g.drawString(text, tx - 1, ty);
        g.drawString(text, tx, ty + 1);
        g.setColor(fill);
        g.drawString(text, tx, ty);
    }

    /** Тёмная обводка — текст читается на золотой рамке карточки. */
    static void drawOutlinedText(Graphics2D g, String text, int tx, int ty, Color fill) {
        tx = Math.round(tx);
        ty = Math.round(ty);
        g.setColor(new Color(20, 12, 4, 220));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(text, tx + dx, ty + dy);
                }
            }
        }
        g.setColor(fill);
        g.drawString(text, tx, ty);
    }

    /** Рисует спрайт в слот без искажения пропорций. */
    private Rectangle drawAspectFitSprite(Graphics2D g, BufferedImage img, int x, int y, int w, int h,
                                          boolean pixelArt) {
        if (img == null || w <= 0 || h <= 0) {
            return new Rectangle(x, y, w, h);
        }
        int srcW = img.getWidth();
        int srcH = img.getHeight();
        if (srcW <= 0 || srcH <= 0) {
            return new Rectangle(x, y, w, h);
        }

        float srcAspect = (float) srcW / srcH;
        float dstAspect = (float) w / h;
        int drawW;
        int drawH;
        if (srcAspect > dstAspect) {
            drawW = w;
            drawH = Math.max(1, Math.round(w / srcAspect));
        } else {
            drawH = h;
            drawW = Math.max(1, Math.round(h * srcAspect));
        }
        int drawX = x + (w - drawW) / 2;
        int drawY = y + (h - drawH) / 2;
        drawScaledSprite(g, img, drawX, drawY, drawW, drawH, pixelArt);
        return new Rectangle(drawX, drawY, drawW, drawH);
    }

    private static Rectangle aspectFitCroppedBounds(Rectangle crop, int x, int y, int w, int h,
                                                    int maxPixelSize) {
        if (crop == null || crop.width <= 0 || crop.height <= 0 || w <= 0 || h <= 0) {
            return new Rectangle(x, y, w, h);
        }
        float srcAspect = (float) crop.width / crop.height;
        float dstAspect = (float) w / h;
        int drawW;
        int drawH;
        if (srcAspect > dstAspect) {
            drawW = w;
            drawH = Math.max(1, Math.round(w / srcAspect));
        } else {
            drawH = h;
            drawW = Math.max(1, Math.round(h * srcAspect));
        }
        if (maxPixelSize > 0) {
            int cap = Math.min(maxPixelSize, Math.min(w, h));
            if (drawW > cap || drawH > cap) {
                if (drawW >= drawH) {
                    drawW = cap;
                    drawH = Math.max(1, Math.round(cap / srcAspect));
                } else {
                    drawH = cap;
                    drawW = Math.max(1, Math.round(cap * srcAspect));
                }
            }
        }
        int drawX = x + (w - drawW) / 2;
        int drawY = y + (h - drawH) / 2;
        return new Rectangle(drawX, drawY, drawW, drawH);
    }

    private Rectangle drawAspectFitCroppedSprite(Graphics2D g, BufferedImage img, Rectangle crop,
                                                 int x, int y, int w, int h, boolean pixelArt,
                                                 int maxPixelSize) {
        if (img == null || crop == null || crop.width <= 0 || crop.height <= 0 || w <= 0 || h <= 0) {
            return new Rectangle(x, y, w, h);
        }
        Rectangle bounds = aspectFitCroppedBounds(crop, x, y, w, h, pixelArt ? maxPixelSize : 0);
        drawCroppedScaledSprite(g, img, crop, bounds.x, bounds.y, bounds.width, bounds.height, pixelArt);
        return bounds;
    }

    /** Отрисовка иконки без размытия — nearest-neighbor, целые координаты. */
    private void drawCrispIcon(Graphics2D g, BufferedImage icon, int x, int y, int size) {
        if (icon == null || size <= 0) return;
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = icon.getWidth();
        int ih = icon.getHeight();
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object prevRender = g.getRenderingHint(RenderingHints.KEY_RENDERING);
        Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        if (iw == size && ih == size) {
            g.drawImage(icon, ix, iy, null);
        } else if (iw * 2 == size && ih * 2 == size) {
            g.drawImage(icon, ix, iy, ix + size, iy + size, 0, 0, iw, ih, null);
        } else if (iw == size * 2 && ih == size * 2) {
            g.drawImage(icon, ix, iy, ix + size, iy + size, 0, 0, iw, ih, null);
        } else {
            g.drawImage(icon, ix, iy, ix + size, iy + size, 0, 0, iw, ih, null);
        }
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
        if (prevRender != null) {
            g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRender);
        }
        if (prevAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
        }
    }

    private static String truncateToWidth(String text, FontMetrics fm, int maxW) {
        if (fm.stringWidth(text) <= maxW) return text;
        String ellipsis = "…";
        for (int len = text.length() - 1; len > 0; len--) {
            String cut = text.substring(0, len) + ellipsis;
            if (fm.stringWidth(cut) <= maxW) return cut;
        }
        return ellipsis;
    }

    private static void applyCrispRendering(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    private static void drawCrispText(Graphics2D g) {
        GameFonts.applyGameHints(g);
    }

    private static void drawEquipText(Graphics2D g, Font font, String text, int x, int y, Color color) {
        drawCardText(g);
        g.setFont(font);
        int tx = (x + 1) & ~1;
        int ty = (y + 1) & ~1;
        GameFonts.drawOutlined(g, text, tx, ty, color);
    }

    private void drawCharacter(Graphics2D g, int sw, int sh, BufferedImage sprite,
                               boolean isLeft, int dialogTop, float alpha) {
        if (sprite == null) return;

        int cw = sprite.getWidth();
        int ch = sprite.getHeight();

        int baseY = dialogTop - ch + Math.round(ch * 0.12f);
        int cx = isLeft ? -Math.round(cw * 0.12f) : sw - cw + Math.round(cw * 0.12f);
        float breathe = (float) Math.sin(tick * 0.04 + (isLeft ? 0 : 2)) * 1.5f;
        int cy = baseY + Math.round(breathe);

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f * alpha));
        g.drawImage(sprite, cx, cy, null);
        g.setComposite(prev);
    }

    private void drawScaledSprite(Graphics2D g, BufferedImage img, int x, int y, int w, int h, boolean pixelArt) {
        if (img == null || w <= 0 || h <= 0) return;
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            pixelArt ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                     : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, x, y, w, h, null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
    }

    private void drawCroppedScaledSprite(Graphics2D g, BufferedImage img, Rectangle crop,
                                         int x, int y, int w, int h, boolean pixelArt) {
        if (img == null || crop == null || crop.width <= 0 || crop.height <= 0 || w <= 0 || h <= 0) return;
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            pixelArt ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                     : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, x, y, x + w, y + h,
            crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
    }

    private static Rectangle computeContentBounds(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int minX = w;
        int minY = h;
        int maxX = 0;
        int maxY = 0;
        int step = Math.max(1, Math.min(w, h) / 256);
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) & 0xff;
                if (a <= 20) {
                    continue;
                }
                int r = (argb >>> 16) & 0xff;
                int g = (argb >>> 8) & 0xff;
                int b = argb & 0xff;
                if (r < 24 && g < 24 && b < 24) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) {
            return new Rectangle(0, 0, w, h);
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private void drawScaledBackground(Graphics2D g, BufferedImage img, int sw, int sh, float alpha) {
        if (img == null) return;

        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= 0 || h <= 0) return;

        int x = (sw - w) / 2;
        int y = (sh - h) / 2;

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(img, x, y, null);
        g.setComposite(prev);
    }

    private void updateAshParticles() {
        if (tick % 6 == 0 && ashParticles.size() < 15) {
            float x = 130 + rng.nextFloat() * 220;
            float y = 50 + rng.nextFloat() * 140;
            ashParticles.add(new float[]{x, y, 0, -0.12f, 0, 60 + rng.nextInt(60), 1});
        }
        ashParticles.removeIf(p -> ++p[4] >= p[5]);
        for (float[] p : ashParticles) {
            p[1] += p[3];
        }
    }

    private void drawAshParticles(Graphics2D g) {
        for (float[] p : ashParticles) {
            float life = 1f - p[4] / p[5];
            int a = Math.max(0, Math.min(255, (int) (life * 50)));
            g.setColor(new Color(200, 170, 100, a));
            g.fillRect(Math.round(p[0]), Math.round(p[1]), 1, 1);
        }
    }
}
