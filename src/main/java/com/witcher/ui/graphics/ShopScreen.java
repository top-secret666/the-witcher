package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.DukeLines;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.*;
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

    private enum ShopState {
        REVEAL,
        IDLE,
        CATEGORY_OPENING,
        CATEGORY,
        CATEGORY_CLOSING
    }

    /** ~3 с при 30 FPS — появление витрины. */
    private static final int REVEAL_DURATION_TICKS = 84;
    private static final int CATEGORY_OPEN_DURATION_TICKS = 50;
    private static final int CARD_FLIP_TICKS = 12;
    /** ~1 мин при 30 FPS — оборот сам возвращается на лицо, если игрок AFK. */
    private static final int CARD_FLIP_IDLE_TICKS = 30 * 60;

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

            cardsY = panelY + headerH + 4;
            int contentBottom = cardsY + gridRows * cardH + (gridRows - 1) * cardGap;
            panelH = contentBottom - panelY + 8;

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
    private int catalogScrollOffset = 0;
    private float cardFlipT = 0f;
    private int cardFlipTarget = 0;
    private int cardFlipIdleTicks = 0;
    private boolean exitRequested = false;

    private static final String WELCOME_LINE = DukeLines.WELCOME;

    private static final String IDLE_LINE = DukeLines.IDLE;

    public ShopScreen() {
        this(ShopModel.createNewSession());
    }

    public ShopScreen(ShopModel model) {
        this.model = model;
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
        cardFlipT = 0f;
        cardFlipTarget = 0;
        cardFlipIdleTicks = 0;
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

    static Rectangle computeContentBoundsPublic(BufferedImage img) {
        return computeContentBounds(img);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed) {
        update(mouseX, mouseY, clicked, escPressed, 0);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
        tick++;

        if (escPressed) {
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
        updateCardFlip();

        ShopRevealAnimator reveal = revealAnimator();
        boolean showcaseInteractive = reveal.uiInteractive && state == ShopState.IDLE;

        ShopLayout layout = new ShopLayout(VIRTUAL_W, VIRTUAL_H, items.size(),
            assets.hudX, assets.hudW, assets.hudH, assets.panelW,
            assets.panelHeaderH, assets.topRowCols, assets.bottomRowCols);

        hoveredIndex = -1;
        hoveredRowIndex = -1;

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
            if (categoryBuyBounds.contains(mouseX, mouseY)) {
                tryPurchaseSelected();
            } else if (hoveredRowIndex >= 0) {
                selectedRowIndex = hoveredRowIndex;
                cardFlipTarget = 1;
                cardFlipIdleTicks = 0;
                ShopCatalogEntry row = catalogEntries.get(hoveredRowIndex);
                Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
                ensureRowVisible(panel.y, selectedRowIndex);
                currentDialog = DukeLines.rowInspect(row.name, row.price);
            } else if (cat.listInteractive && categoryCardContains(cat, mouseX, mouseY)) {
                cardFlipTarget = cardFlipTarget == 0 ? 1 : 0;
                cardFlipIdleTicks = 0;
            }
        }
    }

    private boolean categoryCardContains(ShopCategoryAnimator cat, int mx, int my) {
        return mx >= cat.cardX && my >= cat.cardY
            && mx < cat.cardX + cat.cardW && my < cat.cardY + cat.cardH;
    }

    private void updateCardFlip() {
        if (state != ShopState.CATEGORY && state != ShopState.CATEGORY_CLOSING) {
            if (state == ShopState.IDLE) {
                cardFlipT = 0f;
                cardFlipTarget = 0;
                cardFlipIdleTicks = 0;
            }
            return;
        }
        float step = 1f / CARD_FLIP_TICKS;
        if (cardFlipT < cardFlipTarget) {
            cardFlipT = Math.min(1f, cardFlipT + step);
        } else if (cardFlipT > cardFlipTarget) {
            cardFlipT = Math.max(0f, cardFlipT - step);
        }

        if (state == ShopState.CATEGORY && cardFlipTarget == 1 && cardFlipT >= 1f) {
            cardFlipIdleTicks++;
            if (cardFlipIdleTicks >= CARD_FLIP_IDLE_TICKS) {
                cardFlipTarget = 0;
                cardFlipIdleTicks = 0;
            }
        } else if (cardFlipTarget == 0 && cardFlipT <= 0f) {
            cardFlipIdleTicks = 0;
        }
    }

    private void tryPurchaseSelected() {
        if (selectedRowIndex < 0 || selectedRowIndex >= catalogEntries.size()) {
            return;
        }
        ShopCatalogEntry entry = catalogEntries.get(selectedRowIndex);
        ShopModel.PurchaseResult result = model.purchase(entry);
        currentDialog = result.dukeLine();
        if (result.success()) {
            int keepIndex = Math.min(selectedRowIndex, Math.max(0, catalogEntries.size() - 2));
            if (selectedIndex >= 0) {
                buildCatalogRows(items.get(selectedIndex));
                selectedRowIndex = catalogEntries.isEmpty() ? -1 : Math.min(keepIndex, catalogEntries.size() - 1);
            }
            refreshShowcasePrices();
        }
    }

    private void beginCategoryClose() {
        categoryClosing = true;
        categoryTicks = 0;
        state = ShopState.CATEGORY_CLOSING;
        hoveredRowIndex = -1;
        cardFlipTarget = 0;
        cardFlipIdleTicks = 0;
    }

    private void finishCategoryClose() {
        categoryClosing = false;
        categoryTicks = 0;
        selectedIndex = -1;
        selectedRowIndex = -1;
        catalogEntries.clear();
        catalogScrollOffset = 0;
        cardFlipT = 0f;
        cardFlipTarget = 0;
        cardFlipIdleTicks = 0;
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

        if (!categoryMode) {
            drawDarkOverlay(g, sw, sh, layout, brighten * Math.max(0.25f, reveal.panelAlpha * 0.85f));
        } else {
            drawCategoryOverlay(g, sw, sh, layout, brighten);
        }

        if (!categoryMode) {
            BufferedImage dukeDraw = assets.dukeScaled;
            drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, brighten);
            drawCharacter(g, sw, sh, dukeDraw, false, layout.dialogTop, brighten);
        }

        if (!categoryMode) {
            drawHud(g, layout, reveal.hudAlpha, reveal.hudSlideY);
        }

        if (categoryMode && selectedIndex >= 0) {
            drawCategoryView(g, layout, reveal, categoryAnimator(layout));
            drawCornerWallet(g, 1f);
        } else {
            drawCards(g, layout, reveal);
            drawBuyButton(g, layout, reveal.btnAlpha, reveal.btnSlideY);
        }

        if (reveal.panelAlpha > 0.45f) {
            drawAshParticles(g);
        }

        DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", currentDialog,
            DialogBoxRenderer.DUKE_COLOR, 1f);

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

    private void drawDarkOverlay(Graphics2D g, int sw, int sh, ShopLayout layout, float alpha) {
        Composite prev = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f * alpha));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * alpha));
        int pad = 6;
        g.fillRoundRect(layout.panelX - pad, layout.panelY - pad,
            layout.panelW + pad * 2, layout.panelH + layout.btnH + pad * 2 + 10, 4, 4);

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

        String wallet = model.walletAmountText();
        String suffix = model.walletSuffix();
        int crownSize = 18;
        int crownGap = 4;
        drawCrispText(g);
        g.setFont(new Font("Serif", Font.BOLD, 13));
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

    /** Кошелёк в правом верхнем углу — экран списка категории. */
    private void drawCornerWallet(Graphics2D g, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        String wallet = model.walletAmountText();
        String suffix = model.walletSuffix();
        int crownSize = 16;
        int crownGap = 4;
        int margin = 8;
        int padX = 7;
        int padY = 4;

        drawCrispText(g);
        g.setFont(new Font("Serif", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        int blockW = fm.stringWidth(wallet) + fm.stringWidth(suffix);
        if (assets.crownIconScaled != null) {
            blockW += crownSize + crownGap;
        }
        int blockH = Math.max(crownSize, fm.getHeight()) + padY * 2;
        int blockX = VIRTUAL_W - margin - blockW - padX * 2;
        int blockY = 8;

        g.setColor(new Color(10, 7, 3, 185));
        g.fillRoundRect(blockX, blockY, blockW + padX * 2, blockH, 6, 6);
        g.setColor(new Color(140, 105, 45, 160));
        g.drawRoundRect(blockX, blockY, blockW + padX * 2, blockH, 6, 6);

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
                                  ShopCategoryAnimator cat) {
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
        boolean cardGrowing = state == ShopState.CATEGORY_OPENING || state == ShopState.CATEGORY_CLOSING;
        ShopCatalogEntry statEntry = selectedCatalogEntry();
        drawFlippableItemCard(g, item, cat.cardX, cat.cardY, cat.cardW, cat.cardH,
            true, false, 1f, selectedCatalogPrice(), cardGrowing, cardFlipT, statEntry);

        if (cat.detailPanelAlpha > 0.02f) {
            Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
            int px = panel.x + Math.round(cat.detailPanelSlideX);
            int py = panel.y;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cat.detailPanelAlpha));
            if (assets.catalogDetailPanel != null) {
                g.drawImage(assets.catalogDetailPanel, px, py, null);
            }
            drawCatalogRows(g, px, py, cat.listInteractive);
            drawCategoryBuyButton(g, px, py, cat.detailPanelAlpha, cat.listInteractive);
        }

        g.setComposite(layer);
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
        if (interactive) {
            categoryBuyBounds.setBounds(btnX, btnY, btnW, btnH);
        } else {
            categoryBuyBounds.setBounds(0, 0, 0, 0);
        }
        if (assets.btnBuyScaled != null) {
            g.drawImage(assets.btnBuyScaled, btnX, btnY, null);
        }
        drawCrispText(g);
        g.setFont(cardFont(10));
        String label = "Купить";
        FontMetrics fm = g.getFontMetrics();
        int tx = btnX + (btnW - fm.stringWidth(label)) / 2;
        drawOutlinedText(g, label, tx, btnY + 19, new Color(220, 200, 140));
        g.setComposite(prev);
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
        g.setFont(cardFont(8));

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

            FontMetrics fm = g.getFontMetrics();
            String label = truncateToWidth(row.name, fm, assets.rowW - 54);
            int textY = y + (assets.rowH + fm.getAscent()) / 2 - 2;
            drawOutlinedText(g, label, x + 8, textY, new Color(235, 215, 155));

            String price = row.priceLabel();
            int priceW = fm.stringWidth(price);
            if (assets.crownIconSmall != null && !price.equals("···")) {
                priceW += assets.crownIconSmall.getWidth() + 2;
            }
            int priceX = x + assets.rowW - priceW - 8;
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
        Composite savedComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, revealAlpha));

        BufferedImage frame = assets.cardFrontScaled;
        if (selected && assets.cardSelectedScaled != null) {
            frame = assets.cardSelectedScaled;
        } else if (hovered && assets.cardHoverScaled != null) {
            frame = assets.cardHoverScaled;
        }

        Rectangle cardRect;
        if (frame != null) {
            g.drawImage(frame, x, y, w, h, null);
            cardRect = new Rectangle(x, y, w, h);
        } else {
            drawFallbackCard(g, x, y, w, h, false);
            cardRect = new Rectangle(x, y, w, h);
        }
        drawCardFrontContent(g, item, cardRect, w, h, priceOverride, smoothIconGrowth);

        g.setComposite(savedComposite);
    }

    private void drawFlippableItemCard(Graphics2D g, ShopItem item, int x, int y, int w, int h,
                                       boolean selected, boolean hovered, float revealAlpha,
                                       String priceOverride, boolean smoothIconGrowth,
                                       float flipT, ShopCatalogEntry statEntry) {
        Shape savedClip = g.getClip();
        g.clipRect(x, y, w, h);
        try {
            if (flipT <= 0.001f) {
                drawItemCard(g, item, x, y, w, h, selectedIndex, selected, hovered,
                    revealAlpha, priceOverride, smoothIconGrowth);
                return;
            }
            if (flipT >= 0.999f) {
                drawItemCardBack(g, x, y, w, h, revealAlpha, statEntry);
                return;
            }

            float squeeze = flipT < 0.5f ? 1f - flipT * 2f : (flipT - 0.5f) * 2f;
            int sw = Math.max(2, Math.round(w * squeeze));
            int sx = x + (w - sw) / 2;
            if (flipT < 0.5f) {
                drawItemCard(g, item, sx, y, sw, h, selectedIndex, selected, hovered,
                    revealAlpha, priceOverride, smoothIconGrowth);
            } else {
                drawItemCardBack(g, sx, y, sw, h, revealAlpha, statEntry);
            }
        } finally {
            g.setClip(savedClip);
        }
    }

    private void drawItemCardBack(Graphics2D g, int x, int y, int w, int h, float alpha,
                                  ShopCatalogEntry statEntry) {
        Composite saved = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        BufferedImage frame = assets.cardBackScaled;
        if (frame != null) {
            Object prev = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(frame, x, y, w, h, null);
            if (prev != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prev);
            }
        } else {
            drawFallbackCard(g, x, y, w, h, true);
        }

        if (statEntry != null && w >= 70 && h >= 100) {
            ShopStatBarRenderer.draw(g, x, y, w, h, model.statPreview(statEntry),
                assets.statVialEmpty, assets.statVialOverlay);
        }

        g.setComposite(saved);
    }

    private void drawFallbackCard(Graphics2D g, int x, int y, int w, int h, boolean back) {
        g.setColor(back ? new Color(28, 22, 14, 235) : new Color(38, 28, 16, 235));
        g.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
        g.setColor(new Color(180, 130, 45));
        g.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
    }

    private void drawCardFrontContent(Graphics2D g, ShopItem item, Rectangle card, int w, int h,
                                    String priceOverride, boolean smoothIconGrowth) {
        int x = card.x;
        int y = card.y;

        drawCardText(g);
        int fontSize = w < 60 ? 7 : (h > 200 ? 12 : (w < 90 ? 8 : 10));
        g.setFont(cardFont(fontSize));
        FontMetrics nameFm = g.getFontMetrics();
        String name = truncateToWidth(item.displayName(), nameFm, w - 8);
        int nameY = priceOverride != null
            ? y + h - Math.max(18, Math.round(h * 0.22f))
            : y + h - 10;

        BufferedImage art = item.cardArt != null ? item.cardArt : item.icon;
        if (art != null) {
            int slotX = x + 4;
            int slotW = w - 8;
            int slotTop = y + Math.round(h * 0.12f);
            int slotBottom = nameY - Math.round(h * 0.10f);
            int slotH = Math.max(1, slotBottom - slotTop);
            Rectangle crop = computeContentBounds(art);
            int maxArt = iconCapForCard(slotW, slotH, smoothIconGrowth);
            drawAspectFitCroppedSprite(g, art, crop, slotX, slotTop, slotW, slotH, true, maxArt);
        }

        Color nameColor = item.kind == ItemKind.SET_CATALOG
            ? new Color(255, 210, 100) : new Color(245, 230, 190);
        drawOutlinedText(g, name, x + (w - nameFm.stringWidth(name)) / 2, nameY, nameColor);

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
        return new Font(Font.SANS_SERIF, Font.BOLD, size);
    }

    private static void drawCardText(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /** Тёмная обводка — текст читается на золотой рамке карточки. */
    private static void drawOutlinedText(Graphics2D g, String text, int tx, int ty, Color fill) {
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

    private Rectangle drawAspectFitCroppedSprite(Graphics2D g, BufferedImage img, Rectangle crop,
                                                 int x, int y, int w, int h, boolean pixelArt,
                                                 int maxPixelSize) {
        if (img == null || crop == null || crop.width <= 0 || crop.height <= 0 || w <= 0 || h <= 0) {
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
        if (pixelArt && maxPixelSize > 0) {
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
        drawCroppedScaledSprite(g, img, crop, drawX, drawY, drawW, drawH, pixelArt);
        return new Rectangle(drawX, drawY, drawW, drawH);
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

    private void drawBuyButton(Graphics2D g, ShopLayout layout, float alpha, float slideY) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int btnY = layout.btnY + Math.round(slideY);

        if (assets.btnBuyScaled != null) {
            g.drawImage(assets.btnBuyScaled, layout.btnX, btnY, null);
        }

        drawCrispText(g);
        g.setFont(cardFont(10));
        String label = "Купить";
        FontMetrics fm = g.getFontMetrics();
        int tx = layout.btnX + (layout.btnW - fm.stringWidth(label)) / 2;
        drawOutlinedText(g, label, tx, btnY + 19, new Color(220, 200, 140));
        g.setComposite(prev);
    }

    private static void applyCrispRendering(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    private static void drawCrispText(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
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
