package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Пиксельный экран лавки Герцога.
 * Визуальная итерация: каталог мок-товаров, диалог, без логики покупки.
 */
public class ShopScreen {

    private static final String UI = "/assets/sprites/lavka/ui/";
    private static final String ICONS = "/assets/sprites/lavka/icons/";

    private enum ShopState {
        WELCOME,
        BROWSE,
        IDLE
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
    private final BufferedImage signTitle;
    private final BufferedImage catalogPanel;
    private final BufferedImage rowNormal;
    private final BufferedImage rowHover;
    private final BufferedImage rowSelected;
    private final BufferedImage btnBuyNormal;
    private final BufferedImage btnBuyDisabled;
    private final BufferedImage counterForeground;
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
            "/assets/sprites/lavka/lavka.png",
            "/assets/sprites/screen saver/lavka.png"
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
        signTitle = load(UI + "shop_sign_title.png");
        catalogPanel = load(UI + "shop_catalog_panel.png");
        rowNormal = load(UI + "shop_row_normal.png");
        rowHover = load(UI + "shop_row_hover.png");
        rowSelected = load(UI + "shop_row_selected.png");
        btnBuyNormal = load(UI + "shop_btn_buy_normal.png");
        btnBuyDisabled = load(UI + "shop_btn_buy_disabled.png");
        counterForeground = load(UI + "shop_counter_foreground.png");
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

        g.setColor(new Color(12, 10, 6));
        g.fillRect(0, 0, sw, sh);

        drawScaledBackground(g, merchantBg, sw, sh, 1f);
        drawVignette(g, sw, sh);

        BufferedImage dukeDraw = (state == ShopState.BROWSE && selectedIndex >= 0)
            ? (dukeLaughSprite != null ? dukeLaughSprite : dukeSprite)
            : dukeSprite;

        drawCharacter(g, sw, sh, geraltSprite, true);
        drawCharacter(g, sw, sh, dukeDraw, false);

        drawSign(g, sw);
        drawHud(g, sw);
        drawCatalog(g, sw, sh);
        drawBuyButton(g, sw, sh);

        drawCounterForeground(g, sw, sh);
        drawAshParticles(g, sw, sh);

        DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
        DialogBoxRenderer.drawSpeakerText(g, "Герцог", currentDialog,
            DialogBoxRenderer.DUKE_COLOR, layout, 1f);

        g.dispose();
    }

    public boolean isExitRequested() {
        return exitRequested;
    }

    public void clearExitRequest() {
        exitRequested = false;
    }

    private void drawSign(Graphics2D g, int sw) {
        if (signTitle == null) return;
        int targetW = Math.min(200, sw / 2);
        int targetH = Math.round(signTitle.getHeight() * (targetW / (float) signTitle.getWidth()));
        int x = (sw - targetW) / 2;
        int y = 42;
        drawScaledSprite(g, signTitle, x, y, targetW, targetH, true);
    }

    private void drawHud(Graphics2D g, int sw) {
        int pad = 8;
        int barH = 30;
        int barW = sw - pad * 2;
        int barX = pad;
        int barY = 6;

        if (hudBar != null) {
            drawScaledSprite(g, hudBar, barX, barY, barW, barH, true);
        } else {
            g.setColor(new Color(10, 8, 4, 200));
            g.fillRect(barX, barY, barW, barH);
            g.setColor(DialogBoxRenderer.BOX_BORDER);
            g.drawRect(barX, barY, barW, barH);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 13));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        g.drawString("Лавка Герцога", barX + 14, barY + 20);

        String wallet = "???";
        int crownSize = 16;
        int walletX = barX + barW - 14;
        FontMetrics fm = g.getFontMetrics();
        walletX -= fm.stringWidth(" крон");
        walletX -= fm.stringWidth(wallet);
        if (crownIcon != null) {
            walletX -= crownSize + 4;
            drawScaledSprite(g, crownIcon, walletX, barY + 7, crownSize, crownSize, true);
            walletX += crownSize + 4;
        }
        g.setColor(new Color(220, 200, 140));
        g.drawString(wallet, walletX, barY + 20);
        g.setColor(new Color(180, 165, 120));
        g.drawString(" крон", walletX + fm.stringWidth(wallet), barY + 20);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    private void drawCatalog(Graphics2D g, int sw, int sh) {
        int panelW = 210;
        int rowH = 30;
        int headerH = 28;
        int panelH = headerH + rowH * items.size() + 12;
        int panelX = (sw - panelW) / 2;
        int panelY = 88;

        if (catalogPanel != null) {
            drawScaledSprite(g, catalogPanel, panelX, panelY, panelW, panelH, true);
        } else {
            g.setColor(new Color(8, 6, 3, 210));
            g.fillRect(panelX, panelY, panelW, panelH);
            g.setColor(DialogBoxRenderer.BOX_BORDER);
            g.drawRect(panelX, panelY, panelW, panelH);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 12));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        g.drawString("— Товары —", panelX + panelW / 2 - 36, panelY + 18);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        int rowX = panelX + 8;
        int rowW = panelW - 16;
        int y = panelY + headerH + 4;
        int iconSize = 22;

        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            boolean selected = i == selectedIndex;
            boolean hovered = i == hoveredIndex;

            BufferedImage rowBg = rowNormal;
            if (selected && rowSelected != null) {
                rowBg = rowSelected;
            } else if (hovered && rowHover != null) {
                rowBg = rowHover;
            }

            int rowY = y;
            if (rowBg != null) {
                drawScaledSprite(g, rowBg, rowX, rowY, rowW, rowH - 2, true);
            }

            item.bounds.setBounds(rowX, rowY, rowW, rowH - 2);

            int iconX = rowX + 6;
            int iconY = rowY + (rowH - iconSize) / 2 - 1;
            if (item.icon != null) {
                drawScaledSprite(g, item.icon, iconX, iconY, iconSize, iconSize, true);
            }

            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setFont(new Font("Serif", Font.PLAIN, 11));
            g.setColor(selected ? new Color(255, 230, 140) : new Color(210, 195, 150));
            int textX = iconX + iconSize + 6;
            g.drawString(item.name, textX, rowY + 18);

            g.setFont(new Font("Serif", Font.BOLD, 11));
            g.setColor(new Color(200, 175, 100));
            String price = item.priceLabel;
            FontMetrics fm = g.getFontMetrics();
            int priceX = rowX + rowW - 8 - fm.stringWidth(price);
            g.drawString(price, priceX, rowY + 18);

            if (crownIcon != null) {
                drawScaledSprite(g, crownIcon, priceX - 14, rowY + 5, 12, 12, true);
            }
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

            y += rowH;
        }
    }

    private void drawBuyButton(Graphics2D g, int sw, int sh) {
        int btnW = 100;
        int btnH = 30;
        int btnX = (int) (sw * 0.72f) - btnW / 2;
        int btnY = (int) (sh * 0.48f);

        BufferedImage btn = btnBuyDisabled != null ? btnBuyDisabled : btnBuyNormal;
        if (btn != null) {
            drawScaledSprite(g, btn, btnX, btnY, btnW, btnH, true);
        } else {
            g.setColor(new Color(40, 32, 18, 200));
            g.fillRect(btnX, btnY, btnW, btnH);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 12));
        g.setColor(new Color(100, 85, 55));
        String label = "Скоро";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, btnX + (btnW - fm.stringWidth(label)) / 2, btnY + 19);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    private void drawCounterForeground(Graphics2D g, int sw, int sh) {
        if (counterForeground == null) return;
        float scale = sw / (float) counterForeground.getWidth();
        int w = sw;
        int h = Math.round(counterForeground.getHeight() * scale);
        int x = 0;
        int y = sh - h;
        drawScaledSprite(g, counterForeground, x, y, w, h, true);
    }

    private void drawCharacter(Graphics2D g, int sw, int sh, BufferedImage sprite, boolean isLeft) {
        if (sprite == null) return;

        float charScale = (sh * 0.85f) / sprite.getHeight() * 0.92f;
        int cw = Math.round(sprite.getWidth() * charScale);
        int ch = Math.round(sprite.getHeight() * charScale);

        int dialogZone = (int) (sh * 0.15f);
        int baseY = sh - dialogZone - ch + (int) (ch * 0.15f) + Math.round(ch * 0.06f);
        int cx = isLeft ? (int) (sw * 0.02f) : (int) (sw - cw - sw * 0.02f);

        if (!isLeft) {
            baseY -= Math.round(ch * 0.08f);
        }

        float breathe = (float) Math.sin(tick * 0.04 + (isLeft ? 0 : 2)) * 2;
        int cy = baseY + (int) breathe;

        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(sprite, cx, cy, cw, ch, null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
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

    private void drawVignette(Graphics2D g, int sw, int sh) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
        GradientPaint top = new GradientPaint(0, 0, new Color(0, 0, 0, 140), 0, sh * 0.15f, new Color(0, 0, 0, 0));
        g.setPaint(top);
        g.fillRect(0, 0, sw, (int) (sh * 0.15f));
        g.setComposite(prev);
    }

    private void updateAshParticles() {
        if (tick % 4 == 0 && ashParticles.size() < 25) {
            float x = 80 + rng.nextFloat() * 320;
            float y = 60 + rng.nextFloat() * 200;
            float vx = (rng.nextFloat() - 0.5f) * 0.15f;
            float vy = -0.1f - rng.nextFloat() * 0.25f;
            int maxAge = 80 + rng.nextInt(100);
            float size = 1 + rng.nextFloat();
            ashParticles.add(new float[]{x, y, vx, vy, 0, maxAge, size});
        }

        Iterator<float[]> it = ashParticles.iterator();
        while (it.hasNext()) {
            float[] p = it.next();
            p[0] += p[2];
            p[1] += p[3];
            p[4]++;
            if (p[4] >= p[5]) it.remove();
        }
    }

    private void drawAshParticles(Graphics2D g, int sw, int sh) {
        for (float[] p : ashParticles) {
            float life = 1f - p[4] / p[5];
            int alpha = Math.max(0, Math.min(255, (int) (life * 70)));
            int size = Math.max(1, Math.round(p[6]));
            g.setColor(new Color(200, 170, 100, alpha));
            g.fillRect(Math.round(p[0]), Math.round(p[1]), size, size);
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
