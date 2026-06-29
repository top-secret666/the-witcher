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
    private static final float DIALOG_HEIGHT_RATIO = 0.12f;

    private enum ShopState {
        WELCOME,
        BROWSE,
        IDLE
    }

    private static final class ShopLayout {
        final int hudY;
        final int hudH;
        final int panelX;
        final int panelY;
        final int panelW;
        final int panelH;
        final int rowH;
        final int headerH;
        final int listY;
        final int btnX;
        final int btnY;
        final int btnW;
        final int btnH;
        final int iconSize;
        final int dialogTop;

        ShopLayout(int sw, int sh, int itemCount) {
            hudY = 3;
            hudH = 42;
            iconSize = 32;
            dialogTop = sh - Math.round(sh * DIALOG_HEIGHT_RATIO) - 6;
            btnH = 32;
            btnW = 104;
            rowH = 38;
            headerH = 28;

            panelW = 288;
            panelX = (sw - panelW) / 2;
            panelY = hudY + hudH + 6;
            int listH = headerH + rowH * itemCount + 8;
            int maxPanelBottom = dialogTop - btnH - 10;
            panelH = Math.min(listH + 12, maxPanelBottom - panelY);
            listY = panelY + headerH + 6;

            btnX = panelX + (panelW - btnW) / 2;
            btnY = panelY + panelH + 6;
        }
    }

    private static final class ShopItem {
        final String name;
        final String priceLabel;
        final String dukeLine;
        final BufferedImage icon;
        Rectangle bounds = new Rectangle();

        ShopItem(String name, String priceLabel, String dukeLine, BufferedImage icon) {
            this.name = name;
            this.priceLabel = priceLabel;
            this.dukeLine = dukeLine;
            this.icon = icon;
        }
    }

    private final BufferedImage merchantBg;
    private final BufferedImage geraltSprite;
    private final BufferedImage dukeSprite;
    private final BufferedImage dukeLaughSprite;

    private final BufferedImage hudBar;
    private final BufferedImage catalogPanel;
    private final BufferedImage rowNormal;
    private final BufferedImage rowHover;
    private final BufferedImage rowSelected;
    private final BufferedImage btnBuyDisabled;
    private final BufferedImage crownIcon;

    private final List<ShopItem> items = new ArrayList<>();
    private final List<float[]> ashParticles = new ArrayList<>();
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
        rowNormal = load(UI + "shop_row_normal.png");
        rowHover = load(UI + "shop_row_hover.png");
        rowSelected = load(UI + "shop_row_selected.png");
        btnBuyDisabled = load(UI + "shop_btn_buy_disabled.png");
        crownIcon = load(ICONS + "icon_crown.png");

        items.add(new ShopItem("Кираса волчьей школы", "120",
            "Отличный выбор! Волчья сталь — как раз для таких, как вы.",
            load(ICONS + "icon_armor_chest.png")));
        items.add(new ShopItem("Укреплённые штаны", "45",
            "Штаны крепкие. Ноги целее — монстров больше.",
            load(ICONS + "icon_armor_legs.png")));
        items.add(new ShopItem("Перчатки наездника", "30",
            "Рукам тепло, клинку — верно. Берите, не пожалеете.",
            load(ICONS + "icon_armor_gloves.png")));
        items.add(new ShopItem("Сапоги стражника", "55",
            "В этих сапогах и по болоту пройдёте, и от удара отскочите.",
            load(ICONS + "icon_armor_boots.png")));
        items.add(new ShopItem("Зелье «Чёрный гриф»", "15",
            "Хм... Зелье? Ну что ж, ваш выбор, Белый Волк...",
            load(ICONS + "icon_potion.png")));

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
        }
    }

    public void render(BufferedImage screen, int mouseX, int mouseY) {
        Graphics2D g = screen.createGraphics();
        int sw = screen.getWidth();
        int sh = screen.getHeight();
        ShopLayout layout = new ShopLayout(sw, sh, items.size());

        g.setColor(new Color(8, 6, 4));
        g.fillRect(0, 0, sw, sh);

        // 1. Фон лавки (приглушённый)
        drawScaledBackground(g, merchantBg, sw, sh, 0.75f);
        drawDarkOverlay(g, sw, sh, layout);

        // 2. Персонажи сзади — мельче и по краям
        BufferedImage dukeDraw = (state == ShopState.BROWSE && selectedIndex >= 0)
            ? (dukeLaughSprite != null ? dukeLaughSprite : dukeSprite)
            : dukeSprite;
        drawCharacter(g, sw, sh, geraltSprite, true, layout.dialogTop);
        drawCharacter(g, sw, sh, dukeDraw, false, layout.dialogTop);

        // 3. UI поверх персонажей
        drawHud(g, sw, layout);
        drawCatalog(g, layout);
        drawBuyButton(g, layout);

        drawAshParticles(g);

        // 4. Диалог — компактное окно
        DialogBoxRenderer.Layout dialogLayout = DialogBoxRenderer.computeCompactLayout(sw, sh);
        DialogBoxRenderer.drawSpeakerText(g, "Герцог", currentDialog,
            DialogBoxRenderer.DUKE_COLOR, dialogLayout, 1f);

        g.dispose();
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void clearExitRequest() {
        exitRequested = false;
    }

    /** Затемнение по краям + подложка под каталог для читаемости. */
    private void drawDarkOverlay(Graphics2D g, int sw, int sh, ShopLayout layout) {
        Composite prev = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        int pad = 6;
        g.fillRoundRect(layout.panelX - pad, layout.panelY - pad,
            layout.panelW + pad * 2, layout.panelH + layout.btnH + pad * 2 + 10, 4, 4);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
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

    private void drawHud(Graphics2D g, int sw, ShopLayout layout) {
        int barX = 10;
        int barW = sw - 20;

        if (hudBar != null) {
            drawScaledSprite(g, hudBar, barX, layout.hudY, barW, layout.hudH, true);
        } else {
            g.setColor(new Color(10, 8, 4, 220));
            g.fillRect(barX, layout.hudY, barW, layout.hudH);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 15));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        FontMetrics titleFm = g.getFontMetrics();
        int titleY = layout.hudY + (layout.hudH + titleFm.getAscent()) / 2 - 2;
        g.drawString("Лавка Герцога", barX + 14, titleY);

        String wallet = "???";
        FontMetrics fm = g.getFontMetrics();
        int crownSize = 18;
        int textRight = barX + barW - 14;
        g.setFont(new Font("Serif", Font.BOLD, 14));
        fm = g.getFontMetrics();
        textRight -= fm.stringWidth(" крон");
        textRight -= fm.stringWidth(wallet);
        if (crownIcon != null) {
            textRight -= crownSize + 4;
            int crownY = layout.hudY + (layout.hudH - crownSize) / 2;
            drawScaledSprite(g, crownIcon, textRight, crownY, crownSize, crownSize, true);
            textRight += crownSize + 4;
        }
        g.setColor(new Color(220, 200, 140));
        int walletY = layout.hudY + (layout.hudH + fm.getAscent()) / 2 - 2;
        g.drawString(wallet, textRight, walletY);
        g.setColor(new Color(170, 155, 110));
        g.drawString(" крон", textRight + fm.stringWidth(wallet), walletY);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    private void drawCatalog(Graphics2D g, ShopLayout layout) {
        if (catalogPanel != null) {
            drawScaledSprite(g, catalogPanel, layout.panelX, layout.panelY,
                layout.panelW, layout.panelH, true);
        } else {
            g.setColor(new Color(12, 9, 5, 230));
            g.fillRect(layout.panelX, layout.panelY, layout.panelW, layout.panelH);
            g.setColor(DialogBoxRenderer.BOX_BORDER);
            g.drawRect(layout.panelX, layout.panelY, layout.panelW, layout.panelH);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 12));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        g.drawString("— Товары —", layout.panelX + layout.panelW / 2 - 36, layout.panelY + 18);

        int rowX = layout.panelX + 8;
        int rowW = layout.panelW - 16;
        int iconSize = layout.iconSize;
        int y = layout.listY;

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            boolean selected = i == selectedIndex;
            boolean hovered = i == hoveredIndex;

            BufferedImage rowBg = rowNormal;
            if (selected && rowSelected != null) rowBg = rowSelected;
            else if (hovered && rowHover != null) rowBg = rowHover;

            int rowY = y;
            if (rowBg != null) {
                drawScaledSprite(g, rowBg, rowX, rowY, rowW, layout.rowH - 2, true);
            } else {
                g.setColor(selected ? new Color(70, 55, 20, 180) : new Color(30, 24, 12, 160));
                g.fillRect(rowX, rowY, rowW, layout.rowH - 2);
            }

            item.bounds.setBounds(rowX, rowY, rowW, layout.rowH - 2);

            int iconX = rowX + 6;
            int iconY = rowY + (layout.rowH - iconSize) / 2 - 1;
            if (item.icon != null) {
                drawScaledSprite(g, item.icon, iconX, iconY, iconSize, iconSize, true);
            }

            g.setFont(new Font("Serif", Font.PLAIN, 11));
            g.setColor(selected ? new Color(255, 230, 140) : new Color(210, 195, 150));
            int textX = iconX + iconSize + 8;
            int textBaseline = rowY + layout.rowH / 2 + 4;
            g.drawString(item.name, textX, textBaseline);

            g.setFont(new Font("Serif", Font.BOLD, 12));
            g.setColor(new Color(200, 175, 100));
            FontMetrics fm = g.getFontMetrics();
            int crownW = crownIcon != null ? 14 : 0;
            int priceBlockW = fm.stringWidth(item.priceLabel) + crownW + 3;
            int priceBlockX = rowX + rowW - 10 - priceBlockW;
            if (crownIcon != null) {
                drawScaledSprite(g, crownIcon, priceBlockX, rowY + (layout.rowH - 12) / 2, 12, 12, true);
                g.drawString(item.priceLabel, priceBlockX + crownW + 3, textBaseline);
            } else {
                g.drawString(item.priceLabel, priceBlockX, textBaseline);
            }

            y += layout.rowH;
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    private void drawBuyButton(Graphics2D g, ShopLayout layout) {
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
    }

    private void drawCharacter(Graphics2D g, int sw, int sh, BufferedImage sprite,
                               boolean isLeft, int dialogTop) {
        if (sprite == null) return;

        float charScale = (sh * 0.70f) / sprite.getHeight();
        int cw = Math.round(sprite.getWidth() * charScale);
        int ch = Math.round(sprite.getHeight() * charScale);

        int baseY = dialogTop - ch + Math.round(ch * 0.12f);
        int cx = isLeft ? -Math.round(cw * 0.12f) : sw - cw + Math.round(cw * 0.12f);

        float breathe = (float) Math.sin(tick * 0.04 + (isLeft ? 0 : 2)) * 1.5f;
        int cy = baseY + (int) breathe;

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
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
            int alpha = Math.max(0, Math.min(255, (int) (life * 50)));
            g.setColor(new Color(200, 170, 100, alpha));
            g.fillRect(Math.round(p[0]), Math.round(p[1]), 1, 1);
        }
    }

    private static BufferedImage load(String path) {
        Sprite s = Sprite.load(path);
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
