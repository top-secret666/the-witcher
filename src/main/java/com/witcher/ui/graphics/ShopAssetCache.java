package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopRuntimeAssets;
import main.java.com.witcher.ui.shop.view.ShopUiMetrics;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Ассеты лавки — один раз за сессию.
 * Иконки товаров/категорий — оригиналы {@code lavka/icons/} (bilinear).
 * UI-карточки — приоритет {@code lavka/1x/ui/}, иначе даунскейл с оригинала.
 */
public final class ShopAssetCache implements ShopRuntimeAssets {

    private static final String BASE = "/assets/sprites/lavka/";
    private static final String BAKED = BASE + "1x/";
    private static final String UI = BAKED + "ui/";
    private static final String ICONS_SRC = BASE + "icons/";
    private static final String ICONS_BAKED = BAKED + "icons/";
    private static final String UI_SRC = BASE + "ui/";
    /** Размер оборота карточки в режиме категории (см. {@code ShopLayout#leftCategoryCardSlot}). */
    private static final int CATEGORY_CARD_W = 166;
    private static final int CATEGORY_CARD_H = 249;

    private static ShopAssetCache instance;

    public static ShopAssetCache get() {
        if (instance == null) {
            instance = new ShopAssetCache();
        }
        return instance;
    }

    /** После GDX bake — пересоздать кэш, чтобы подтянуть GPU-иконки плашки. */
    public static void resetAfterGdxBake() {
        instance = null;
    }

    final int hudX;
    final int hudY = 4;
    final int hudW;
    final int hudH;

    final int cardW = 54;
    final int cardH = 81;
    /** Иконка на карточке — см. tools/bake_lavka_assets.py CARD_ART. */
    final int cardArtSize = 38;
    final int gridCols = 5;
    final int gridRows = 2;
    final int topRowCols = 5;
    final int bottomRowCols = 2;
    /** Отступ над сеткой карточек на главной витрине. */
    final int panelHeaderH = 8;
    final int btnW = 100;
    final int btnH = 30;
    final int panelW = 380;
    private static final int VIRTUAL_H = 360;
    private static final int DIALOG_TEXT_ZONE = 54;
    private static final int PANEL_BOTTOM_MARGIN = 4;

    final BufferedImage hudBar;
    final BufferedImage counterForeground;
    final int counterX = 0;
    final int counterY;
    final int counterW = 480;
    final int counterH;
    final BufferedImage catalogPanelScaled;
    final BufferedImage catalogDetailPanel;
    final BufferedImage rowNormal;
    final BufferedImage rowHover;
    final BufferedImage rowSelected;
    final int rowW;
    final int rowH = 24;
    final int detailPanelW;
    final int detailPanelH;
    final BufferedImage cardFrontScaled;
    final BufferedImage cardBackScaled;
    final BufferedImage cardHoverScaled;
    final BufferedImage cardSelectedScaled;
    final BufferedImage btnBuyNormal;
    final BufferedImage btnBuyDisabled;
    final BufferedImage crownIconScaled;
    /** Монетка в списке товаров и на карточках — без LibGDX. */
    final BufferedImage crownIconSmall;
    final int catalogCoinSize = 8;
    /** Печать герцога на HUD-плашке (LibGDX). */
    final BufferedImage dukeSealIconScaled;
    final int dukeSealSize = 32;
    final BufferedImage merchantBgScaled;
    final BufferedImage geraltScaled;
    final BufferedImage dukeScaled;
    final BufferedImage dukeLaughScaled;

    final BufferedImage[] itemIcons = new BufferedImage[5];
    final BufferedImage[] itemArts = new BufferedImage[5];
    final BufferedImage weaponIcon;
    final BufferedImage setsIcon;
    final BufferedImage inventoryBagIcon;
    final BufferedImage inventoryBagClosed;
    final BufferedImage inventoryBagOpen;
    final BufferedImage inventoryBagHover;
    final BufferedImage[] inventoryBagOpenFrames;
    final BufferedImage statVialEmpty;
    final BufferedImage statVialOverlay;
    final BufferedImage statVialEndCap;
    final BufferedImage walletPouch;

    private ShopAssetCache() {
        long t0 = System.currentTimeMillis();
        boolean baked = probeBaked();

        hudW = panelW;
        hudX = (480 - hudW) / 2;
        hudH = 58;
        hudBar = loadSized(UI + "shop_hud_bar.png", hudW, hudH, BASE + "ui/shop_hud_bar.png", true);

        cardFrontScaled = loadSized(UI + "shop_card_front.png", cardW, cardH,
            BASE + "ui/shop_card_front.png", false);
        cardBackScaled = loadGdxUi(ShopUiAssetsFactory.KEY_CARD_BACK,
            loadSharpUi("shop_card_back.png", CATEGORY_CARD_W, CATEGORY_CARD_H));
        cardHoverScaled = loadSized(UI + "shop_card_hover.png", cardW, cardH,
            BASE + "ui/shop_card_hover.png", false);
        cardSelectedScaled = loadSized(UI + "shop_card_selected.png", cardW, cardH,
            BASE + "ui/shop_card_selected.png", false);
        btnBuyNormal = loadSized(UI + "shop_btn_buy_normal.png", btnW, btnH,
            BASE + "ui/shop_btn_buy_normal.png", false);
        btnBuyDisabled = loadSized(UI + "shop_btn_buy_disabled.png", btnW, btnH,
            BASE + "ui/shop_btn_buy_disabled.png", false);

        crownIconScaled = loadGdxUi(ShopUiAssetsFactory.KEY_HUD_CROWN,
            loadHudIconFallback("icon_crown.png", 36));
        crownIconSmall = loadCatalogCoinIcon();
        dukeSealIconScaled = loadGdxUi(ShopUiAssetsFactory.KEY_HUD_DUKE_SEAL,
            loadHudIconFallback("icon_duke_seal.png", 64));
        logHudIconSource();

        merchantBgScaled = loadBackground();
        int charH = Math.round(360 * 0.82f);
        geraltScaled = loadPortrait("geralt_portrait_shop.png", charH);
        dukeScaled = loadPortrait("duke_portrait_shop.png", charH);
        dukeLaughScaled = loadPortrait("duke_portrait_fun_shop.png", charH);

        String[] iconNames = {
            "icon_armor_chest.png", "icon_armor_legs.png", "icon_armor_gloves.png",
            "icon_armor_boots.png", "icon_potion.png"
        };
        for (int i = 0; i < 5; i++) {
            itemIcons[i] = loadLavkaIcon(iconNames[i], 0);
            itemArts[i] = itemIcons[i];
        }

        weaponIcon = loadLavkaIcon("icon_weapon.png", 0);
        setsIcon = loadLavkaIcon("icon_armor_set.png", 0);
        int bagSize = 40;
        inventoryBagIcon = loadLavkaIcon("icon_inventory_bag.png", 0);
        inventoryBagClosed = loadSized(UI + "inventory_bag_closed.png", bagSize, bagSize,
            BASE + "ui/inventory_bag_closed.png", true);
        inventoryBagOpen = loadSized(UI + "inventory_bag_open.png", bagSize, bagSize,
            BASE + "ui/inventory_bag_open.png", true);
        inventoryBagHover = loadSized(UI + "inventory_bag_hover.png", bagSize, bagSize,
            BASE + "ui/inventory_bag_hover.png", true);
        inventoryBagOpenFrames = loadBagOpenFrames(bagSize);
        statVialEmpty = loadFirst(BASE + "ui/stat_vial_empty.png");
        statVialOverlay = loadFirst(BASE + "ui/stat_vial_glass_overlay.png");
        statVialEndCap = loadFirst(BAKED + "ui/stat_vial_end_cap.png", BASE + "ui/stat_vial_end_cap.png");
        walletPouch = loadFirst(BASE + "wallet_pouch_gold.png");

        int headerH = panelHeaderH;
        int panelY = hudY + hudH + 6;
        int dialogTop = VIRTUAL_H - DIALOG_TEXT_ZONE;
        int panelDrawH = dialogTop - panelY - PANEL_BOTTOM_MARGIN;
        counterY = hudY + hudH + 2;
        counterH = dialogTop - counterY - 4;
        counterForeground = loadSized(UI + "shop_counter_foreground.png", counterW, counterH,
            BASE + "ui/shop_counter_foreground.png", false);

        catalogPanelScaled = loadSized(UI + "shop_catalog_panel.png", panelW, panelDrawH,
            BASE + "ui/shop_catalog_panel.png", false);

        detailPanelW = 292;
        detailPanelH = 232;
        catalogDetailPanel = loadSized(UI + "shop_catalog_panel_detail.png", detailPanelW, detailPanelH,
            BASE + "ui/shop_catalog_panel.png", false);
        rowW = detailPanelW - 16;
        rowNormal = loadSized(UI + "shop_row_normal.png", rowW, rowH,
            BASE + "ui/shop_row_normal.png", false);
        rowHover = loadSized(UI + "shop_row_hover.png", rowW, rowH,
            BASE + "ui/shop_row_hover.png", false);
        rowSelected = loadSized(UI + "shop_row_selected.png", rowW, rowH,
            BASE + "ui/shop_row_selected.png", false);

        String mode = baked ? "1x (готовые)" : "runtime scale";
        System.out.println("Лавка: ассеты [" + mode + "] за " + (System.currentTimeMillis() - t0) + " мс");
    }

    private static boolean probeBaked() {
        return Sprite.loadOptional(BAKED + "ui/shop_hud_bar.png") != null;
    }

    private BufferedImage[] loadBagOpenFrames(int size) {
        BufferedImage[] frames = new BufferedImage[10];
        int loaded = 0;
        for (int i = 0; i < frames.length; i++) {
            String name = String.format("inventory_bag_open_%02d.png", i);
            BufferedImage frame = loadSized(UI + name, size, size, BASE + "ui/" + name, true);
            if (frame != null) {
                frames[i] = frame;
                loaded++;
            }
        }
        if (loaded == frames.length) {
            return frames;
        }
        return splitBagOpenSheet(size);
    }

    private BufferedImage[] splitBagOpenSheet(int size) {
        BufferedImage sheet = loadFirst(
            BASE + "ui/inventory_bag_open_sheet.png",
            BAKED + "ui/inventory_bag_open_sheet.png");
        if (sheet == null) {
            return new BufferedImage[0];
        }
        int cols = 5;
        int rows = 2;
        int fw = sheet.getWidth() / cols;
        int fh = sheet.getHeight() / rows;
        BufferedImage[] frames = new BufferedImage[10];
        int idx = 0;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = col * fw;
                int y = row * fh;
                BufferedImage cell = sheet.getSubimage(x, y, fw, fh);
                Rectangle box = ShopImageBounds.compute(cell);
                frames[idx++] = PixelScaler.crispScaleRegion(cell, box, size, size);
            }
        }
        return frames;
    }

    private BufferedImage loadBakedIcon(String fileName) {
        return load(ICONS_BAKED + fileName);
    }

    /** Fallback HUD: оригинал {@code lavka/icons/} → сглаженный даунскейл (не 1x). */
    private BufferedImage loadHudIconFallback(String fileName, int bakePx) {
        BufferedImage src = load(ICONS_SRC + fileName);
        if (src == null) {
            BufferedImage baked = loadBakedIcon(fileName);
            return baked != null ? PixelScaler.smoothScaleUniform(baked, bakePx) : null;
        }
        Rectangle box = ShopImageBounds.compute(src);
        BufferedImage cropped = box != null && box.width > 0 && box.height > 0
            ? src.getSubimage(box.x, box.y, box.width, box.height)
            : src;
        return PixelScaler.smoothScaleUniform(cropped, bakePx);
    }

    private BufferedImage loadCatalogCoinIcon() {
        BufferedImage baked = loadBakedIcon("icon_crown_small.png");
        if (baked == null) {
            baked = loadBakedIcon("icon_crown.png");
        }
        if (baked != null) {
            return PixelScaler.sharpScaleUniform(baked, catalogCoinSize);
        }
        BufferedImage src = load(ICONS_SRC + "icon_crown.png");
        if (src == null) {
            return null;
        }
        Rectangle box = ShopImageBounds.compute(src);
        BufferedImage cropped = box != null && box.width > 0 && box.height > 0
            ? src.getSubimage(box.x, box.y, box.width, box.height)
            : src;
        return PixelScaler.sharpScaleUniform(cropped, catalogCoinSize);
    }

    private void logHudIconSource() {
        if (crownIconScaled != null) {
            boolean gdx = ShopUiAssetsFactory.get(ShopUiAssetsFactory.KEY_HUD_CROWN) != null;
            System.out.println("HUD crown: baked " + crownIconScaled.getWidth() + "x" + crownIconScaled.getHeight()
                + " draw@18" + (gdx ? " [GDX]" : " [fallback]"));
        }
        if (dukeSealIconScaled != null) {
            boolean gdx = ShopUiAssetsFactory.get(ShopUiAssetsFactory.KEY_HUD_DUKE_SEAL) != null;
            System.out.println("HUD duke seal: baked " + dukeSealIconScaled.getWidth() + "x"
                + dukeSealIconScaled.getHeight() + " draw@32" + (gdx ? " [GDX]" : " [fallback]"));
        }
    }

    private BufferedImage loadGdxUi(String key, BufferedImage fallback) {
        BufferedImage gdx = ShopUiAssetsFactory.get(key);
        return gdx != null ? gdx : fallback;
    }

    private BufferedImage loadLavkaIcon(String fileName, int maxSize) {
        BufferedImage src = load(ICONS_SRC + fileName);
        if (src == null) {
            return null;
        }
        Rectangle box = ShopImageBounds.compute(src);
        BufferedImage cropped = box != null && box.width > 0 && box.height > 0
            ? src.getSubimage(box.x, box.y, box.width, box.height)
            : src;
        if (maxSize > 0 && (cropped.getWidth() > maxSize || cropped.getHeight() > maxSize)) {
            return PixelScaler.smoothScaleUniform(cropped, maxSize);
        }
        return cropped;
    }

    private BufferedImage loadSharpUi(String fileName, int w, int h) {
        BufferedImage src = load(UI_SRC + fileName);
        if (src == null) {
            return null;
        }
        return PixelScaler.sharpScale(src, w, h);
    }

    private BufferedImage loadSized(String bakedPath, int w, int h, String fallbackPath, boolean crop) {
        BufferedImage baked = load(bakedPath);
        if (baked != null) {
            if (baked.getWidth() == w && baked.getHeight() == h) {
                return baked;
            }
            return PixelScaler.crispScale(baked, w, h);
        }
        BufferedImage src = load(fallbackPath);
        if (src == null) {
            return baked;
        }
        if (crop) {
            Rectangle box = ShopImageBounds.compute(src);
            return PixelScaler.crispScaleRegion(src, box, w, h);
        }
        return PixelScaler.crispScale(src, w, h);
    }

    private BufferedImage loadBackground() {
        BufferedImage baked = load(BAKED + "merchant_bg_lavka.png");
        if (baked != null) {
            return baked;
        }
        BufferedImage src = loadFirst(BASE + "merchant_bg_lavka.png", BASE + "lavka.png");
        if (src == null) {
            return null;
        }
        float scale = Math.max(480f / src.getWidth(), 360f / src.getHeight());
        int w = Math.round(src.getWidth() * scale);
        int h = Math.round(src.getHeight() * scale);
        return PixelScaler.crispScale(src, w, h);
    }

    private BufferedImage loadPortrait(String name, int targetH) {
        BufferedImage baked = load(BAKED + name);
        if (baked != null && baked.getHeight() == targetH) {
            return baked;
        }
        BufferedImage src = loadFirst(
            BASE + name,
            "/assets/sprites/screen saver/" + portraitFallback(name)
        );
        if (src == null) {
            return baked;
        }
        Rectangle box = ShopImageBounds.compute(src);
        int cropW = box.width;
        int cropH = box.height;
        int w = Math.round(cropW * ((float) targetH / cropH));
        return PixelScaler.crispScaleRegion(src, box, w, targetH);
    }

    private static String portraitFallback(String shopName) {
        return switch (shopName) {
            case "geralt_portrait_shop.png" -> "geralt_portrait.png";
            case "duke_portrait_shop.png" -> "duke_portrait.png";
            case "duke_portrait_fun_shop.png" -> "duke_portrait_fun.png";
            default -> shopName;
        };
    }

    private static BufferedImage load(String path) {
        Sprite s = Sprite.loadOptional(path);
        return s != null ? s.getImage() : null;
    }

    private static BufferedImage loadFirst(String... paths) {
        for (String p : paths) {
            BufferedImage img = load(p);
            if (img != null) {
                return img;
            }
        }
        return null;
    }

    @Override
    public int hudX() {
        return hudX;
    }

    @Override
    public int hudW() {
        return hudW;
    }

    @Override
    public int hudH() {
        return hudH;
    }

    @Override
    public int panelW() {
        return panelW;
    }

    @Override
    public int panelHeaderH() {
        return panelHeaderH;
    }

    @Override
    public int topRowCols() {
        return topRowCols;
    }

    @Override
    public int bottomRowCols() {
        return bottomRowCols;
    }

    @Override
    public int detailPanelW() {
        return detailPanelW;
    }

    @Override
    public int detailPanelH() {
        return detailPanelH;
    }

    @Override
    public int rowH() {
        return rowH;
    }

    @Override
    public int btnW() {
        return btnW;
    }

    @Override
    public int btnH() {
        return btnH;
    }

    @Override
    public int cardArtSize() {
        return cardArtSize;
    }

    @Override
    public int dukeSealSize() {
        return dukeSealSize;
    }

    /** Иконка категории на витрине (для presenter без доступа к package-private полям). */
    public BufferedImage iconForCategory(ShopCategory cat) {
        if (cat == ShopCategory.SETS) {
            return setsIcon;
        }
        if (cat == ShopCategory.WEAPON) {
            return weaponIcon;
        }
        if (cat.iconIndex >= 0 && cat.iconIndex < itemIcons.length) {
            return itemIcons[cat.iconIndex];
        }
        return null;
    }
}
