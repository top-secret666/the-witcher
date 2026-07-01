package main.java.com.witcher.ui.graphics;

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

    /** ~4 с при 30 FPS — заметное появление витрины. */
    private static final int REVEAL_DURATION_TICKS = 120;
    private static final int CATEGORY_OPEN_DURATION_TICKS = 54;

    private static final int GRID_COLS = 5;

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

        ShopLayout(int sw, int sh, int itemCount, int hudX, int hudW, int hudH, int fixedPanelW) {
            this.hudX = hudX;
            this.hudW = hudW;
            hudY = 4;
            this.hudH = hudH;
            dialogTop = sh - DIALOG_TEXT_ZONE;
            btnH = 30;
            btnW = 100;
            headerH = 22;
            cardW = 54;
            cardH = 81;
            cardGap = 6;
            gridCols = GRID_COLS;
            gridRows = Math.max(1, (itemCount + gridCols - 1) / gridCols);

            int rowW = gridCols * cardW + (gridCols - 1) * cardGap;
            panelW = fixedPanelW;
            panelX = (sw - panelW) / 2;
            panelY = hudY + hudH + 6;
            cardsStartX = panelX + (panelW - rowW) / 2;
            cardsY = panelY + headerH + 6;
            int contentBottom = cardsY + gridRows * cardH + (gridRows - 1) * cardGap;
            panelH = contentBottom - panelY + 8;

            btnX = panelX + (panelW - btnW) / 2;
            btnY = contentBottom + 6;
        }

        Point cardSlot(int index) {
            int col = index % gridCols;
            int row = index / gridCols;
            int x = cardsStartX + col * (cardW + cardGap);
            int y = cardsY + row * (cardH + cardGap);
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

    private static final class CatalogRow {
        final String name;
        final String price;
        Rectangle bounds = new Rectangle();

        CatalogRow(String name, String price) {
            this.name = name;
            this.price = price;
        }
    }

    private enum ItemKind {
        PIECE,
        SET_CATALOG
    }

    private static final class ShopItem {
        final ItemKind kind;
        final String name;
        final String priceLabel;
        final String dukeLine;
        final String[] statLines;
        final BufferedImage icon;
        final BufferedImage cardArt;
        Rectangle bounds = new Rectangle();

        ShopItem(ItemKind kind, String name, String priceLabel, String dukeLine, String[] statLines,
                 BufferedImage icon, BufferedImage cardArt) {
            this.kind = kind;
            this.name = name;
            this.priceLabel = priceLabel;
            this.dukeLine = dukeLine;
            this.statLines = statLines;
            this.icon = icon;
            this.cardArt = cardArt;
        }
    }

    private final ShopAssetCache assets = ShopAssetCache.get();

    private final List<ShopItem> items = new ArrayList<>();
    private final List<CatalogRow> catalogRows = new ArrayList<>();
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
    private boolean exitRequested = false;

    private static final String WELCOME_LINE = """
            ХО-ХО-ХО-ХА... Приступим к делу, Белый Волк.
            Броня, кирасы, шлемы, наколенники — всё, что душе угодно.
            Только не забудьте кошелёк...""";

    private static final String IDLE_LINE = "Ну же, выбирайте. У меня нет вечности, а у вас — монстров полно.";

    public ShopScreen() {
        items.add(new ShopItem(ItemKind.PIECE, "Кираса", "120",
            "Отличный выбор! Волчья сталь — как раз для таких, как вы.",
            new String[]{"Защ. 45", "Вес 12", "Кираса"},
            assets.itemIcons[0], assets.itemArts[0]));
        items.add(new ShopItem(ItemKind.PIECE, "Штаны", "45",
            "Штаны крепкие. Ноги целее — монстров больше.",
            new String[]{"Защ. 20", "Вес 8", "Поножи"},
            assets.itemIcons[1], assets.itemArts[1]));
        items.add(new ShopItem(ItemKind.PIECE, "Перчатки", "30",
            "Рукам тепло, клинку — верно. Берите, не пожалеете.",
            new String[]{"Защ. 12", "Вес 3", "Руки"},
            assets.itemIcons[2], assets.itemArts[2]));
        items.add(new ShopItem(ItemKind.PIECE, "Сапоги", "55",
            "В этих сапогах и по болоту пройдёте, и от удара отскочите.",
            new String[]{"Защ. 18", "Вес 6", "Сапоги"},
            assets.itemIcons[3], assets.itemArts[3]));
        items.add(new ShopItem(ItemKind.PIECE, "Зелье", "15",
            "Хм... Зелье? Ну что ж, ваш выбор, Белый Волк...",
            new String[]{"Яд", "0.5 кг", "Осторожно"},
            assets.itemIcons[4], assets.itemArts[4]));
        items.add(new ShopItem(ItemKind.SET_CATALOG, "Комплекты", "···",
            "Ах, охотник на целые комплекты! Волчья, Кошачья, Грифонья — выбирайте.",
            new String[]{"Школьные", "Легендар.", "4 части"},
            assets.setCatalogIcon, assets.setCatalogIcon));

        currentDialog = WELCOME_LINE;
    }

    private void buildCatalogRows(ShopItem category) {
        catalogRows.clear();
        selectedRowIndex = 0;
        hoveredRowIndex = -1;
        switch (category.name) {
            case "Кираса" -> {
                catalogRows.add(new CatalogRow("Кираса волчьей школы", "120"));
                catalogRows.add(new CatalogRow("Укреплённая кираса", "85"));
                catalogRows.add(new CatalogRow("Кираса стражника", "65"));
                catalogRows.add(new CatalogRow("Нагрудник охотника", "48"));
            }
            case "Штаны" -> {
                catalogRows.add(new CatalogRow("Укреплённые штаны", "45"));
                catalogRows.add(new CatalogRow("Поножи наёмника", "38"));
                catalogRows.add(new CatalogRow("Штаны ведьмака", "52"));
            }
            case "Перчатки" -> {
                catalogRows.add(new CatalogRow("Перчатки наездника", "30"));
                catalogRows.add(new CatalogRow("Рукавицы стражи", "24"));
                catalogRows.add(new CatalogRow("Перчатки школы Волка", "55"));
            }
            case "Сапоги" -> {
                catalogRows.add(new CatalogRow("Сапоги стражника", "55"));
                catalogRows.add(new CatalogRow("Сапоги путника", "32"));
                catalogRows.add(new CatalogRow("Ботфорты охотника", "41"));
            }
            case "Зелье" -> {
                catalogRows.add(new CatalogRow("Зелье «Чёрный гриф»", "15"));
                catalogRows.add(new CatalogRow("Эликсир кошки", "22"));
                catalogRows.add(new CatalogRow("Отвар грифона", "28"));
            }
            case "Комплекты" -> {
                catalogRows.add(new CatalogRow("Школа Волка", "···"));
                catalogRows.add(new CatalogRow("Школа Кота", "···"));
                catalogRows.add(new CatalogRow("Школа Грифона", "···"));
                catalogRows.add(new CatalogRow("Школа Мантикоры", "···"));
            }
            default -> catalogRows.add(new CatalogRow(category.name, category.priceLabel));
        }
    }

    static Rectangle computeContentBoundsPublic(BufferedImage img) {
        return computeContentBounds(img);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed) {
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

        ShopRevealAnimator reveal = revealAnimator();
        boolean showcaseInteractive = reveal.uiInteractive && state == ShopState.IDLE;

        ShopLayout layout = new ShopLayout(VIRTUAL_W, VIRTUAL_H, items.size(),
            assets.hudX, assets.hudW, assets.hudH, assets.panelW);

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
            if (catAnim.listInteractive) {
                for (int i = 0; i < catalogRows.size(); i++) {
                    if (catalogRows.get(i).bounds.contains(mouseX, mouseY)) {
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

        if ((state == ShopState.CATEGORY) && clicked && hoveredRowIndex >= 0) {
            selectedRowIndex = hoveredRowIndex;
            CatalogRow row = catalogRows.get(hoveredRowIndex);
            currentDialog = "Глядите, " + row.name + " — за " + row.price
                + (row.price.equals("···") ? "" : " крон. Берите, не стыдно.");
        }
    }

    private void beginCategoryClose() {
        categoryClosing = true;
        categoryTicks = 0;
        state = ShopState.CATEGORY_CLOSING;
        hoveredRowIndex = -1;
    }

    private void finishCategoryClose() {
        categoryClosing = false;
        categoryTicks = 0;
        selectedIndex = -1;
        selectedRowIndex = -1;
        catalogRows.clear();
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
            assets.hudX, assets.hudW, assets.hudH, assets.panelW);
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
            drawCornerWallet(g, 1f, layout.dialogTop);
        } else {
            drawCards(g, layout, reveal);
            drawBuyButton(g, layout, reveal.btnAlpha, reveal.btnSlideY);
        }

        if (reveal.panelAlpha > 0.45f) {
            drawAshParticles(g);
        }

        DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", currentDialog,
            DialogBoxRenderer.DUKE_COLOR, 1f);

        g.dispose();
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

        String wallet = "???";
        String suffix = " крон";
        int crownSize = 18;
        int crownGap = 4;
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

    /** Кошелёк в правом верхнем углу — поверх прилавка, выше панели списка. */
    private void drawCornerWallet(Graphics2D g, float alpha, int dialogTop) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        String wallet = "???";
        String suffix = " крон";
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
        drawItemCard(g, item, cat.cardX, cat.cardY, cat.cardW, cat.cardH,
            selectedIndex, true, false, 1f);

        if (cat.detailPanelAlpha > 0.02f) {
            Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
            int px = panel.x + Math.round(cat.detailPanelSlideX);
            int py = panel.y;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cat.detailPanelAlpha));
            if (assets.catalogDetailPanel != null) {
                g.drawImage(assets.catalogDetailPanel, px, py, null);
            }
            drawCatalogRows(g, px, py, cat.listInteractive);
            drawCategoryBuyButton(g, px, py, cat.detailPanelAlpha);
        }

        g.setComposite(layer);
    }

    private void drawCategoryBuyButton(Graphics2D g, int panelX, int panelY, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int btnW = assets.btnW;
        int btnH = assets.btnH;
        int btnX = panelX + (assets.detailPanelW - btnW) / 2;
        int btnY = panelY + assets.detailPanelH - btnH - 8;
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
        int startY = panelY + 12;
        int x = panelX + 8;
        drawCardText(g);
        g.setFont(cardFont(8));

        for (int i = 0; i < catalogRows.size(); i++) {
            CatalogRow row = catalogRows.get(i);
            int y = startY + i * (assets.rowH + rowGap);
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

            String price = row.price;
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
        drawCardFrontContent(g, item, cardRect, w, h);

        g.setComposite(savedComposite);
    }

    private void drawFallbackCard(Graphics2D g, int x, int y, int w, int h, boolean back) {
        g.setColor(back ? new Color(28, 22, 14, 235) : new Color(38, 28, 16, 235));
        g.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
        g.setColor(new Color(180, 130, 45));
        g.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
    }

    private void drawCardFrontContent(Graphics2D g, ShopItem item, Rectangle card, int w, int h) {
        int x = card.x;
        int y = card.y;

        BufferedImage art = item.cardArt != null ? item.cardArt : item.icon;
        if (art != null) {
            int maxArt = w < 60 ? 32 : (h > 200 ? 80 : (w < 90 ? 44 : 56));
            int artSize = Math.min(maxArt, Math.min(w - 10, h - 48));
            int ax = x + (w - artSize) / 2;
            int ay = y + Math.max(6, Math.round(h * 0.08f));
            drawCrispIcon(g, art, ax, ay, artSize);
        }

        drawCardText(g);
        int fontSize = w < 60 ? 7 : (h > 200 ? 12 : (w < 90 ? 8 : 10));
        g.setFont(cardFont(fontSize));
        FontMetrics nameFm = g.getFontMetrics();
        String name = truncateToWidth(item.name, nameFm, w - 8);
        int nameY = y + h - Math.max(18, Math.round(h * 0.22f));
        Color nameColor = item.kind == ItemKind.SET_CATALOG
            ? new Color(255, 210, 100) : new Color(245, 230, 190);
        drawOutlinedText(g, name, x + (w - nameFm.stringWidth(name)) / 2, nameY, nameColor);

        if (item.kind == ItemKind.SET_CATALOG) {
            return;
        }

        g.setFont(cardFont(Math.max(7, fontSize)));
        FontMetrics priceFm = g.getFontMetrics();
        BufferedImage coin = assets.crownIconSmall != null ? assets.crownIconSmall : assets.crownIconScaled;
        int priceW = priceFm.stringWidth(item.priceLabel);
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
        drawOutlinedText(g, item.priceLabel, priceX, priceRowY, new Color(255, 220, 90));
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

    /** Отрисовка иконки без размытия — nearest-neighbor, целые координаты. */
    private void drawCrispIcon(Graphics2D g, BufferedImage icon, int x, int y, int size) {
        if (icon == null || size <= 0) return;
        int ix = Math.round(x);
        int iy = Math.round(y);
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object prevRender = g.getRenderingHint(RenderingHints.KEY_RENDERING);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.drawImage(icon, ix, iy, ix + size, iy + size, 0, 0, icon.getWidth(), icon.getHeight(), null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
        if (prevRender != null) {
            g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRender);
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
