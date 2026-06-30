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

    private static final String UI = "/assets/sprites/lavka/ui/";
    private static final String ICONS = "/assets/sprites/lavka/icons/";
    private static final int DIALOG_TEXT_ZONE = 54;

    private enum ShopState {
        WELCOME,
        BROWSE,
        IDLE
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
        final int dialogTop;

        ShopLayout(int sw, int sh, int itemCount, int hudX, int hudW, int hudH) {
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

            int cardsTotalW = itemCount * cardW + (itemCount - 1) * cardGap;
            panelW = Math.max(cardsTotalW + 28, Math.min(sw - 88, 380));
            panelX = (sw - panelW) / 2;
            panelY = hudY + hudH + 6;
            cardsStartX = panelX + (panelW - cardsTotalW) / 2;
            cardsY = panelY + headerH + 6;
            int contentBottom = cardsY + cardH;
            panelH = contentBottom - panelY + 8;

            btnX = panelX + (panelW - btnW) / 2;
            btnY = contentBottom + 6;
        }
    }

    private static final class ShopItem {
        final String name;
        final String priceLabel;
        final String dukeLine;
        final String[] statLines;
        final BufferedImage icon;
        final BufferedImage cardArt;
        Rectangle bounds = new Rectangle();

        ShopItem(String name, String priceLabel, String dukeLine, String[] statLines,
                 BufferedImage icon, BufferedImage cardArt) {
            this.name = name;
            this.priceLabel = priceLabel;
            this.dukeLine = dukeLine;
            this.statLines = statLines;
            this.icon = icon;
            this.cardArt = cardArt;
        }
    }

    private final BufferedImage merchantBg;
    private final BufferedImage geraltSprite;
    private final BufferedImage dukeSprite;
    private final BufferedImage dukeLaughSprite;

    private final BufferedImage hudBar;
    private final BufferedImage catalogPanel;
    private final BufferedImage cardFront;
    private final BufferedImage cardBack;
    private final BufferedImage cardHover;
    private final BufferedImage cardSelected;
    private final BufferedImage btnBuyDisabled;
    private final BufferedImage crownIcon;

    private final Rectangle hudSrcCrop;
    private final int hudDrawX;
    private final int hudDrawW;
    private final int hudDrawH;

    private final List<ShopItem> items = new ArrayList<>();
    private final List<float[]> ashParticles = new ArrayList<>();
    private final float[] cardFlip = new float[5];
    private final boolean[] cardFaceBack = new boolean[5];
    private final Random rng = new Random();

    private ShopState state = ShopState.WELCOME;
    private String currentDialog;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private int tick = 0;
    private int welcomeTicks = 0;
    private boolean exitRequested = false;

    private static final String WELCOME_LINE = """
            ХО-ХО-ХО-ХА... Приступим к делу, Белый Волк.
            Броня, кирасы, шлемы, наколенники — всё, что душе угодно.
            Только не забудьте кошелёк...""";

    private static final String IDLE_LINE = "Ну же, выбирайте. У меня нет вечности, а у вас — монстров полно.";

    public ShopScreen() {
        merchantBg = loadFirstAvailable(
            "/assets/sprites/lavka/merchant_bg_lavka.png",
            "/assets/sprites/lavka/lavka.png"
        );

        geraltSprite = loadWithFallback(
            "/assets/sprites/lavka/geralt_portrait_shop.png",
            "/assets/sprites/screen saver/geralt_portrait.png"
        );
        dukeSprite = loadWithFallback(
            "/assets/sprites/lavka/duke_portrait_shop.png",
            "/assets/sprites/screen saver/duke_portrait.png"
        );
        dukeLaughSprite = loadWithFallback(
            "/assets/sprites/lavka/duke_portrait_fun_shop.png",
            "/assets/sprites/screen saver/duke_portrait_fun.png"
        );

        hudBar = load(UI + "shop_hud_bar.png");
        catalogPanel = load(UI + "shop_catalog_panel.png");
        cardFront = loadFirstAvailable(UI + "shop_card_front.png", UI + "icon_legendary_frame.png");
        cardBack = load(UI + "shop_card_back.png");
        cardHover = load(UI + "shop_card_hover.png");
        cardSelected = load(UI + "shop_card_selected.png");
        btnBuyDisabled = load(UI + "shop_btn_buy_disabled.png");
        crownIcon = load(ICONS + "icon_crown.png");

        if (hudBar != null && hudBar.getWidth() > 0) {
            hudSrcCrop = computeContentBounds(hudBar);
            hudDrawW = 476;
            float cropAspect = (float) hudSrcCrop.height / hudSrcCrop.width;
            hudDrawH = Math.max(52, Math.min(64, Math.round(hudDrawW * cropAspect)));
            hudDrawX = (480 - hudDrawW) / 2;
        } else {
            hudSrcCrop = null;
            hudDrawX = 2;
            hudDrawW = 476;
            hudDrawH = 56;
        }

        items.add(new ShopItem("Кираса волчьей школы", "120",
            "Отличный выбор! Волчья сталь — как раз для таких, как вы.",
            new String[]{"Защита: 45", "Вес: 12", "Тип: кираса"},
            load(ICONS + "icon_armor_chest.png"),
            load(ICONS + "card_art_chest.png")));
        items.add(new ShopItem("Укреплённые штаны", "45",
            "Штаны крепкие. Ноги целее — монстров больше.",
            new String[]{"Защита: 20", "Вес: 8", "Тип: поножи"},
            load(ICONS + "icon_armor_legs.png"),
            load(ICONS + "card_art_legs.png")));
        items.add(new ShopItem("Перчатки наездника", "30",
            "Рукам тепло, клинку — верно. Берите, не пожалеете.",
            new String[]{"Защита: 12", "Вес: 3", "Тип: перчатки"},
            load(ICONS + "icon_armor_gloves.png"),
            load(ICONS + "card_art_gloves.png")));
        items.add(new ShopItem("Сапоги стражника", "55",
            "В этих сапогах и по болоту пройдёте, и от удара отскочите.",
            new String[]{"Защита: 18", "Вес: 6", "Тип: сапоги"},
            load(ICONS + "icon_armor_boots.png"),
            load(ICONS + "card_art_boots.png")));
        items.add(new ShopItem("Зелье «Чёрный гриф»", "15",
            "Хм... Зелье? Ну что ж, ваш выбор, Белый Волк...",
            new String[]{"Эффект: яд", "Вес: 0.5", "⚠ без чекпоинта"},
            load(ICONS + "icon_potion.png"),
            load(ICONS + "card_art_potion.png")));

        currentDialog = WELCOME_LINE;
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed) {
        tick++;

        welcomeTicks++;

        if (escPressed) {
            exitRequested = true;
            return;
        }

        if (state == ShopState.WELCOME && welcomeTicks > 120) {
            state = ShopState.IDLE;
            currentDialog = IDLE_LINE;
        }

        updateAshParticles();
        updateCardFlipAnimation();

        hoveredIndex = -1;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).bounds.contains(mouseX, mouseY)) {
                hoveredIndex = i;
                break;
            }
        }

        if (clicked && hoveredIndex >= 0) {
            selectedIndex = hoveredIndex;
            state = ShopState.BROWSE;
            currentDialog = items.get(hoveredIndex).dukeLine;
            cardFaceBack[hoveredIndex] = !cardFaceBack[hoveredIndex];
        }
    }

    private void updateCardFlipAnimation() {
        for (int i = 0; i < items.size() && i < cardFlip.length; i++) {
            float target = cardFaceBack[i] ? 1f : 0f;
            float diff = target - cardFlip[i];
            if (Math.abs(diff) > 0.02f) {
                cardFlip[i] += diff * 0.32f;
            } else {
                cardFlip[i] = target;
            }
        }
    }

    public void render(BufferedImage screen, int mouseX, int mouseY) {
        Graphics2D g = screen.createGraphics();
        int sw = screen.getWidth();
        int sh = screen.getHeight();
        applyCrispRendering(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        ShopLayout layout = new ShopLayout(sw, sh, items.size(), hudDrawX, hudDrawW, hudDrawH);
        float alpha = 1f;

        drawScaledBackground(g, merchantBg, sw, sh, 0.75f * alpha);
        drawDarkOverlay(g, sw, sh, layout, alpha);

        BufferedImage dukeDraw = (state == ShopState.BROWSE && selectedIndex >= 0)
            ? (dukeLaughSprite != null ? dukeLaughSprite : dukeSprite)
            : dukeSprite;
        drawCharacter(g, sw, sh, geraltSprite, true, layout.dialogTop, alpha);
        drawCharacter(g, sw, sh, dukeDraw, false, layout.dialogTop, alpha);

        drawHud(g, layout, alpha);
        drawCards(g, layout, alpha);
        drawBuyButton(g, layout, alpha);

        if (alpha > 0.5f) {
            drawAshParticles(g);
        }

        DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", currentDialog,
            DialogBoxRenderer.DUKE_COLOR, alpha);

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

    private void drawHud(Graphics2D g, ShopLayout layout, float alpha) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (hudBar != null) {
            if (hudSrcCrop != null) {
                drawCroppedScaledSprite(g, hudBar, hudSrcCrop,
                    layout.hudX, layout.hudY, layout.hudW, layout.hudH, true);
            } else {
                drawScaledSprite(g, hudBar, layout.hudX, layout.hudY, layout.hudW, layout.hudH, true);
            }
        } else {
            g.setColor(new Color(10, 8, 4, 220));
            g.fillRect(layout.hudX, layout.hudY, layout.hudW, layout.hudH);
        }

        drawCrispText(g);
        g.setFont(new Font("Serif", Font.BOLD, 14));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        FontMetrics titleFm = g.getFontMetrics();
        int titleY = layout.hudY + (layout.hudH + titleFm.getAscent()) / 2 - 1;
        g.drawString("Лавка Герцога", layout.hudX + 18, titleY);

        String wallet = "???";
        int crownSize = 18;
        g.setFont(new Font("Serif", Font.BOLD, 13));
        FontMetrics fm = g.getFontMetrics();
        int walletAnchor = layout.hudX + (int) (layout.hudW * 0.68f);
        int textRight = walletAnchor;
        textRight -= fm.stringWidth(" крон");
        textRight -= fm.stringWidth(wallet);
        if (crownIcon != null) {
            textRight -= crownSize + 4;
            drawCrispIcon(g, crownIcon, textRight, layout.hudY + (layout.hudH - crownSize) / 2,
                crownSize);
            textRight += crownSize + 4;
        }
        g.setColor(new Color(255, 230, 150));
        int walletY = layout.hudY + (layout.hudH + fm.getAscent()) / 2 - 2;
        g.drawString(wallet, textRight, walletY);
        g.setColor(new Color(200, 180, 120));
        g.drawString(" крон", textRight + fm.stringWidth(wallet), walletY);
        g.setComposite(prev);
    }

    private void drawCards(Graphics2D g, ShopLayout layout, float alpha) {
        Composite layer = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int panelBottom = layout.btnY + layout.btnH + 4;
        int panelDrawH = panelBottom - layout.panelY;
        if (catalogPanel != null) {
            drawScaledSprite(g, catalogPanel, layout.panelX, layout.panelY,
                layout.panelW, panelDrawH, true);
        }

        drawCrispText(g);
        g.setFont(new Font("Serif", Font.BOLD, 11));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        g.drawString("— Товары —", layout.panelX + layout.panelW / 2 - 36, layout.panelY + 16);

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            int cardX = layout.cardsStartX + i * (layout.cardW + layout.cardGap);
            int cardY = layout.cardsY;
            item.bounds.setBounds(cardX, cardY, layout.cardW, layout.cardH);
            drawItemCard(g, item, cardX, cardY, layout.cardW, layout.cardH, i,
                i == selectedIndex, i == hoveredIndex, cardFlip[i]);
        }

        g.setComposite(layer);
    }

    private void drawItemCard(Graphics2D g, ShopItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float flip) {
        boolean showBack = flip >= 0.5f;
        float fade = 1f;
        if (flip > 0.05f && flip < 0.95f) {
            fade = flip < 0.5f ? 1f - flip * 2f : (flip - 0.5f) * 2f;
            fade = 0.35f + fade * 0.65f;
        }

        Composite savedComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fade));

        BufferedImage frame = cardFront;
        if (selected && cardSelected != null) {
            frame = cardSelected;
        } else if (hovered && cardHover != null) {
            frame = cardHover;
        }

        Rectangle cardRect;
        if (showBack) {
            if (cardBack != null) {
                cardRect = drawAspectFitSprite(g, cardBack, x, y, w, h, true);
            } else {
                drawFallbackCard(g, x, y, w, h, true);
                cardRect = new Rectangle(x, y, w, h);
            }
            drawCardBackText(g, item, cardRect);
        } else {
            if (frame != null) {
                cardRect = drawAspectFitSprite(g, frame, x, y, w, h, true);
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
        int artSize = Math.min(w - 10, h - 32);
        if (art != null) {
            drawCrispIcon(g, art, x + (w - artSize) / 2, y + 6, artSize);
        }

        drawCrispText(g);
        g.setFont(new Font("Serif", Font.BOLD, 9));
        g.setColor(new Color(235, 215, 155));
        FontMetrics nameFm = g.getFontMetrics();
        String name = truncateToWidth(item.name, nameFm, w - 6);
        g.drawString(name, x + (w - nameFm.stringWidth(name)) / 2, y + h - 17);

        g.setFont(new Font("Serif", Font.BOLD, 11));
        g.setColor(new Color(255, 230, 120));
        FontMetrics priceFm = g.getFontMetrics();
        int crownSize = 10;
        int priceW = priceFm.stringWidth(item.priceLabel) + (crownIcon != null ? crownSize + 2 : 0);
        int priceX = x + (w - priceW) / 2;
        if (crownIcon != null) {
            drawCrispIcon(g, crownIcon, priceX, y + h - 11, crownSize);
            priceX += crownSize + 2;
        }
        g.drawString(item.priceLabel, priceX, y + h - 3);
    }

    private void drawCardBackText(Graphics2D g, ShopItem item, Rectangle card) {
        int x = card.x;
        int y = card.y;
        int w = card.width;

        drawCrispText(g);
        g.setFont(new Font("Serif", Font.BOLD, 8));
        g.setColor(new Color(255, 220, 130));
        FontMetrics fm = g.getFontMetrics();
        int lineY = y + 12;
        for (String line : item.statLines) {
            String text = truncateToWidth(line, fm, w - 6);
            g.drawString(text, x + (w - fm.stringWidth(text)) / 2, lineY);
            lineY += fm.getHeight() + 1;
        }
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
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (btnBuyDisabled != null) {
            drawScaledSprite(g, btnBuyDisabled, layout.btnX, layout.btnY,
                layout.btnW, layout.btnH, true);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 12));
        g.setColor(new Color(90, 75, 50));
        String label = "Скоро";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, layout.btnX + (layout.btnW - fm.stringWidth(label)) / 2, layout.btnY + 19);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setComposite(prev);
    }

    private void drawCharacter(Graphics2D g, int sw, int sh, BufferedImage sprite,
                               boolean isLeft, int dialogTop, float alpha) {
        if (sprite == null) return;

        float charScale = (sh * 0.70f) / sprite.getHeight();
        int cw = Math.round(sprite.getWidth() * charScale);
        int ch = Math.round(sprite.getHeight() * charScale);

        int baseY = dialogTop - ch + Math.round(ch * 0.12f);
        int cx = isLeft ? -Math.round(cw * 0.12f) : sw - cw + Math.round(cw * 0.12f);
        float breathe = (float) Math.sin(tick * 0.04 + (isLeft ? 0 : 2)) * 1.5f;
        int cy = baseY + (int) breathe;

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f * alpha));
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(sprite, cx, cy, cw, ch, null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
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
                if ((img.getRGB(x, y) >>> 24) > 20) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < minX || maxY < minY) {
            return new Rectangle(0, 0, w, h);
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    private void drawScaledBackground(Graphics2D g, BufferedImage img, int sw, int sh, float alpha) {
        if (img == null) return;

        int srcW = img.getWidth();
        int srcH = img.getHeight();
        if (srcW <= 0 || srcH <= 0) return;

        float scale = Math.max((float) sw / srcW, (float) sh / srcH);
        int w = Math.round(srcW * scale);
        int h = Math.round(srcH * scale);
        int x = (sw - w) / 2;
        int y = (sh - h) / 2;

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, x, y, w, h, null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
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

    private static BufferedImage load(String path) {
        Sprite s = Sprite.loadOptional(path);
        return s != null ? s.getImage() : null;
    }

    private static BufferedImage loadFirstAvailable(String... paths) {
        for (String path : paths) {
            BufferedImage img = load(path);
            if (img != null) return img;
        }
        return null;
    }

    private static BufferedImage loadWithFallback(String primary, String fallback) {
        BufferedImage img = loadFirstAvailable(primary);
        return img != null ? img : loadFirstAvailable(fallback);
    }
}
