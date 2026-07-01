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
        WELCOME,
        REVEAL,
        BROWSE,
        IDLE
    }

    private static final int WELCOME_TICKS = 120;
    private static final int REVEAL_DURATION_TICKS = 78;

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
    private final List<float[]> ashParticles = new ArrayList<>();
    private float[] cardFlip;
    private boolean[] cardFaceBack;
    private final Random rng = new Random();

    private ShopState state = ShopState.WELCOME;
    private String currentDialog;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private int tick = 0;
    private int welcomeTicks = 0;
    private int revealTicks = 0;
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

        cardFlip = new float[items.size()];
        cardFaceBack = new boolean[items.size()];
        currentDialog = WELCOME_LINE;
    }

    static Rectangle computeContentBoundsPublic(BufferedImage img) {
        return computeContentBounds(img);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed) {
        tick++;

        welcomeTicks++;

        if (escPressed) {
            exitRequested = true;
            return;
        }

        if (state == ShopState.WELCOME && welcomeTicks >= WELCOME_TICKS) {
            state = ShopState.REVEAL;
            revealTicks = 0;
        }
        if (state == ShopState.REVEAL) {
            revealTicks++;
            if (revealTicks >= REVEAL_DURATION_TICKS) {
                state = ShopState.IDLE;
                currentDialog = IDLE_LINE;
            }
        }

        updateAshParticles();
        updateCardFlipAnimation();

        ShopRevealAnimator reveal = revealAnimator();
        boolean canInteract = reveal.uiInteractive;

        hoveredIndex = -1;
        if (canInteract) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).bounds.contains(mouseX, mouseY)) {
                    hoveredIndex = i;
                    break;
                }
            }
        }

        if (canInteract && clicked && hoveredIndex >= 0) {
            selectedIndex = hoveredIndex;
            state = ShopState.BROWSE;
            currentDialog = items.get(hoveredIndex).dukeLine;
            if (items.get(hoveredIndex).kind == ItemKind.PIECE) {
                cardFaceBack[hoveredIndex] = !cardFaceBack[hoveredIndex];
            }
        }
    }

    private void updateCardFlipAnimation() {
        for (int i = 0; i < items.size() && i < cardFlip.length; i++) {
            if (items.get(i).kind != ItemKind.PIECE) {
                cardFlip[i] = 0f;
                continue;
            }
            float target = cardFaceBack[i] ? 1f : 0f;
            float diff = target - cardFlip[i];
            if (Math.abs(diff) > 0.02f) {
                cardFlip[i] += diff * 0.32f;
            } else {
                cardFlip[i] = target;
            }
        }
    }

    private ShopRevealAnimator revealAnimator() {
        return switch (state) {
            case WELCOME -> ShopRevealAnimator.hidden(items.size());
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
        float sceneAlpha = 1f;

        drawScaledBackground(g, assets.merchantBgScaled, sw, sh, 0.75f * sceneAlpha);
        drawDarkOverlay(g, sw, sh, layout, sceneAlpha * Math.max(reveal.panelAlpha, 0.35f));

        BufferedImage dukeDraw = (state == ShopState.BROWSE && selectedIndex >= 0)
            ? (assets.dukeLaughScaled != null ? assets.dukeLaughScaled : assets.dukeScaled)
            : assets.dukeScaled;
        drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, sceneAlpha);
        drawCharacter(g, sw, sh, dukeDraw, false, layout.dialogTop, sceneAlpha);

        drawHud(g, layout, reveal.hudAlpha, reveal.hudSlideY);
        drawCards(g, layout, reveal);
        drawBuyButton(g, layout, reveal.btnAlpha);

        if (reveal.panelAlpha > 0.45f) {
            drawAshParticles(g);
        }

        DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", currentDialog,
            DialogBoxRenderer.DUKE_COLOR, sceneAlpha);

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

        drawCrispText(g);
        g.setFont(new Font("Serif", Font.BOLD, 14));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        FontMetrics titleFm = g.getFontMetrics();
        int titleY = hudY + (layout.hudH + titleFm.getAscent()) / 2 - 1;
        g.drawString("Лавка Герцога", layout.hudX + 18, titleY);

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
        int panelCy = layout.panelY + layout.panelH / 2;

        if (assets.catalogPanelScaled != null) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, reveal.panelAlpha));
            drawScaledCentered(g, assets.catalogPanelScaled,
                layout.panelX, layout.panelY, layout.panelW, layout.panelH, panelCx, panelCy, scale);
        }

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            Point slot = layout.cardSlot(i);
            float cardA = i < reveal.cardAlpha.length ? reveal.cardAlpha[i] : 1f;
            float cardS = i < reveal.cardScale.length ? reveal.cardScale[i] : 1f;
            if (cardA <= 0.01f) {
                item.bounds.setBounds(0, 0, 0, 0);
                continue;
            }
            int cardW = Math.round(layout.cardW * cardS);
            int cardH = Math.round(layout.cardH * cardS);
            int cardX = slot.x + (layout.cardW - cardW) / 2;
            int cardY = slot.y + (layout.cardH - cardH) / 2;
            item.bounds.setBounds(cardX, cardY, cardW, cardH);
            drawItemCard(g, item, cardX, cardY, cardW, cardH, i,
                i == selectedIndex, i == hoveredIndex, cardFlip[i], cardA);
        }

        g.setComposite(layer);
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
                              boolean selected, boolean hovered, float flip, float revealAlpha) {
        boolean showBack = item.kind == ItemKind.PIECE && flip >= 0.5f;
        float fade = revealAlpha;
        if (flip > 0.05f && flip < 0.95f) {
            float flipFade = flip < 0.5f ? 1f - flip * 2f : (flip - 0.5f) * 2f;
            flipFade = 0.35f + flipFade * 0.65f;
            fade *= flipFade;
        }

        Composite savedComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));

        BufferedImage frame = assets.cardFrontScaled;
        if (selected && assets.cardSelectedScaled != null) {
            frame = assets.cardSelectedScaled;
        } else if (hovered && assets.cardHoverScaled != null) {
            frame = assets.cardHoverScaled;
        }

        Rectangle cardRect;
        if (showBack) {
            BufferedImage back = assets.cardBackScaled;
            if (back != null) {
                g.drawImage(back, x, y, null);
                cardRect = new Rectangle(x, y, w, h);
            } else {
                drawFallbackCard(g, x, y, w, h, true);
                cardRect = new Rectangle(x, y, w, h);
            }
            drawCardBackText(g, item, cardRect);
        } else {
            if (frame != null) {
                g.drawImage(frame, x, y, null);
                cardRect = new Rectangle(x, y, w, h);
            } else {
                drawFallbackCard(g, x, y, w, h, false);
                cardRect = new Rectangle(x, y, w, h);
            }
            drawCardFrontContent(g, item, cardRect);
        }

        g.setComposite(savedComposite);
    }

    private void drawFallbackCard(Graphics2D g, int x, int y, int w, int h, boolean back) {
        g.setColor(back ? new Color(28, 22, 14, 235) : new Color(38, 28, 16, 235));
        g.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
        g.setColor(new Color(180, 130, 45));
        g.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
    }

    private void drawCardFrontContent(Graphics2D g, ShopItem item, Rectangle card) {
        int x = card.x;
        int y = card.y;
        int w = card.width;
        int h = card.height;

        BufferedImage art = item.cardArt != null ? item.cardArt : item.icon;
        if (art != null) {
            int ax = x + (w - art.getWidth()) / 2;
            int ay = y + 8;
            g.drawImage(art, ax, ay, null);
        }

        drawCardText(g);
        g.setFont(cardFont(8));
        FontMetrics nameFm = g.getFontMetrics();
        String name = truncateToWidth(item.name, nameFm, w - 8);
        int nameY = y + h - 22;
        Color nameColor = item.kind == ItemKind.SET_CATALOG
            ? new Color(255, 210, 100) : new Color(245, 230, 190);
        drawOutlinedText(g, name, x + (w - nameFm.stringWidth(name)) / 2, nameY, nameColor);

        if (item.kind == ItemKind.SET_CATALOG) {
            return;
        }

        g.setFont(cardFont(9));
        FontMetrics priceFm = g.getFontMetrics();
        BufferedImage coin = assets.crownIconSmall != null ? assets.crownIconSmall : assets.crownIconScaled;
        int priceW = priceFm.stringWidth(item.priceLabel);
        int coinH = coin != null ? coin.getHeight() : 0;
        if (coin != null) {
            priceW += coin.getWidth() + 2;
        }
        int priceRowY = y + h - 9;
        int priceX = x + (w - priceW) / 2;
        if (coin != null) {
            int coinY = priceRowY - coinH + 1;
            g.drawImage(coin, priceX, coinY, null);
            priceX += coin.getWidth() + 2;
        }
        drawOutlinedText(g, item.priceLabel, priceX, priceRowY, new Color(255, 220, 90));
    }

    private void drawCardBackText(Graphics2D g, ShopItem item, Rectangle card) {
        int x = card.x;
        int y = card.y;
        int w = card.width;
        int h = card.height;

        drawCardText(g);
        g.setFont(cardFont(7));
        FontMetrics fm = g.getFontMetrics();
        int lineStep = fm.getHeight() + 2;
        int totalH = item.statLines.length * lineStep - 2;
        int lineY = y + (h - totalH) / 2 + fm.getAscent();
        for (String line : item.statLines) {
            String text = truncateToWidth(line, fm, w - 8);
            drawOutlinedText(g, text, x + (w - fm.stringWidth(text)) / 2, lineY,
                new Color(255, 225, 140));
            lineY += lineStep;
        }
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

    private void drawBuyButton(Graphics2D g, ShopLayout layout, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (assets.btnBuyScaled != null) {
            g.drawImage(assets.btnBuyScaled, layout.btnX, layout.btnY, null);
        }

        drawCrispText(g);
        g.setFont(cardFont(10));
        String label = "Купить";
        FontMetrics fm = g.getFontMetrics();
        int tx = layout.btnX + (layout.btnW - fm.stringWidth(label)) / 2;
        drawOutlinedText(g, label, tx, layout.btnY + 19, new Color(220, 200, 140));
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
