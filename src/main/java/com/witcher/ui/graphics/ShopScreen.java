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

    private enum ShopState {
        WELCOME,
        BROWSE,
        IDLE
    }

    private static final class ShopItem {
        final String name;
        final String priceLabel;
        final String dukeLine;
        Rectangle bounds = new Rectangle();

        ShopItem(String name, String priceLabel, String dukeLine) {
            this.name = name;
            this.priceLabel = priceLabel;
            this.dukeLine = dukeLine;
        }
    }

    private final BufferedImage merchantBg;
    private final BufferedImage geraltSprite;
    private final BufferedImage dukeSprite;
    private final BufferedImage dukeLaughSprite;

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
            "/assets/sprites/screen saver/lavka.png",
            "/assets/sprites/lavka.png"
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

        items.add(new ShopItem("Кираса волчьей школы", "120 крон",
            "Отличный выбор! Волчья сталь — как раз для таких, как вы."));
        items.add(new ShopItem("Укреплённые штаны", "45 крон",
            "Штаны крепкие. Ноги целее — монстров больше."));
        items.add(new ShopItem("Перчатки наездника", "30 крон",
            "Рукам тепло, клинку — верно. Берите, не пожалеете."));
        items.add(new ShopItem("Сапоги стражника", "55 крон",
            "В этих сапогах и по болоту пройдёте, и от удара отскочите."));
        items.add(new ShopItem("Зелье «Чёрный гриф»", "15 крон",
            "Хм... Зелье? Ну что ж, ваш выбор, Белый Волк..."));

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
        drawAshParticles(g, sw, sh);

        BufferedImage dukeDraw = (state == ShopState.BROWSE && selectedIndex >= 0)
            ? (dukeLaughSprite != null ? dukeLaughSprite : dukeSprite)
            : dukeSprite;

        drawCharacter(g, sw, sh, geraltSprite, true);
        drawCharacter(g, sw, sh, dukeDraw, false);

        drawHud(g, sw);
        drawCatalog(g, sw, sh);
        drawBuyButton(g, sw, sh);

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

    private void drawHud(Graphics2D g, int sw) {
        int pad = 10;
        int hudH = 28;

        g.setColor(new Color(10, 8, 4, 200));
        g.fillRect(pad, pad, sw - pad * 2, hudH);
        g.setColor(DialogBoxRenderer.BOX_BORDER);
        g.drawRect(pad, pad, sw - pad * 2, hudH);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 14));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        g.drawString("Лавка Герцога", pad + 8, pad + 19);

        g.setFont(new Font("Serif", Font.PLAIN, 13));
        g.setColor(new Color(200, 185, 140));
        String wallet = "??? крон";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(wallet, sw - pad - 8 - fm.stringWidth(wallet), pad + 19);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    private void drawCatalog(Graphics2D g, int sw, int sh) {
        int catalogX = (int) (sw * 0.28f);
        int catalogY = (int) (sh * 0.12f);
        int catalogW = (int) (sw * 0.44f);
        int rowH = 26;
        int catalogH = rowH * items.size() + 16;

        g.setColor(new Color(8, 6, 3, 210));
        g.fillRect(catalogX, catalogY, catalogW, catalogH);
        g.setColor(DialogBoxRenderer.BOX_BORDER);
        g.drawRect(catalogX, catalogY, catalogW, catalogH);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 13));
        g.setColor(DialogBoxRenderer.DUKE_COLOR);
        g.drawString("— Товары —", catalogX + 10, catalogY + 16);

        g.setFont(new Font("Serif", Font.PLAIN, 12));
        int y = catalogY + 30;
        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            boolean selected = i == selectedIndex;
            boolean hovered = i == hoveredIndex;

            if (selected || hovered) {
                g.setColor(selected
                    ? new Color(80, 60, 20, 180)
                    : new Color(50, 40, 15, 140));
                g.fillRect(catalogX + 4, y - 14, catalogW - 8, rowH - 2);
            }

            item.bounds.setBounds(catalogX + 4, y - 14, catalogW - 8, rowH - 2);

            g.setColor(selected ? new Color(255, 220, 120) : new Color(210, 195, 150));
            g.drawString(item.name, catalogX + 10, y);

            g.setColor(new Color(160, 140, 90));
            FontMetrics fm = g.getFontMetrics();
            g.drawString(item.priceLabel, catalogX + catalogW - 10 - fm.stringWidth(item.priceLabel), y);
            y += rowH;
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    private void drawBuyButton(Graphics2D g, int sw, int sh) {
        int btnW = 90;
        int btnH = 24;
        int btnX = (int) (sw * 0.72f) - btnW / 2;
        int btnY = (int) (sh * 0.52f);

        g.setColor(new Color(40, 32, 18, 200));
        g.fillRect(btnX, btnY, btnW, btnH);
        g.setColor(new Color(90, 70, 30));
        g.drawRect(btnX, btnY, btnW, btnH);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Serif", Font.BOLD, 12));
        g.setColor(new Color(120, 100, 60));
        String label = "Скоро";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(label, btnX + (btnW - fm.stringWidth(label)) / 2, btnY + 16);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
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

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(sprite, cx, cy, cw, ch, null);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
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
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
        GradientPaint top = new GradientPaint(0, 0, new Color(0, 0, 0, 180), 0, sh * 0.2f, new Color(0, 0, 0, 0));
        g.setPaint(top);
        g.fillRect(0, 0, sw, (int) (sh * 0.2f));
        GradientPaint bottom = new GradientPaint(0, sh * 0.55f, new Color(0, 0, 0, 0), 0, sh, new Color(0, 0, 0, 120));
        g.setPaint(bottom);
        g.fillRect(0, (int) (sh * 0.55f), sw, (int) (sh * 0.45f));
        g.setComposite(prev);
    }

    private void updateAshParticles() {
        if (tick % 4 == 0 && ashParticles.size() < 30) {
            float x = rng.nextFloat() * 480;
            float y = rng.nextFloat() * 360;
            float vx = (rng.nextFloat() - 0.5f) * 0.2f;
            float vy = -0.15f - rng.nextFloat() * 0.3f;
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
            int alpha = Math.max(0, Math.min(255, (int) (life * 80)));
            int size = Math.max(1, Math.round(p[6]));
            g.setColor(new Color(180, 160, 120, alpha));
            g.fillRect(Math.round(p[0]), Math.round(p[1]), size, size);
        }
    }

    private static BufferedImage loadFirstAvailable(String... paths) {
        for (String path : paths) {
            Sprite s = Sprite.load(path);
            if (s != null) return s.getImage();
        }
        return null;
    }

    private static BufferedImage loadWithFallback(String primary, String fallback) {
        BufferedImage img = loadFirstAvailable(primary);
        return img != null ? img : loadFirstAvailable(fallback);
    }
}
