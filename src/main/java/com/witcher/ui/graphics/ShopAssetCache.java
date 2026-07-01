package main.java.com.witcher.ui.graphics;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Ассеты лавки — один раз за сессию.
 * Сначала грузит готовые {@code lavka/1x/} (см. tools/bake_lavka_assets.py), иначе даунскейлит на лету.
 */
final class ShopAssetCache {

    private static final String BASE = "/assets/sprites/lavka/";
    private static final String BAKED = BASE + "1x/";
    private static final String UI = BAKED + "ui/";
    private static final String ICONS = BAKED + "icons/";

    private static ShopAssetCache instance;

    static ShopAssetCache get() {
        if (instance == null) {
            instance = new ShopAssetCache();
        }
        return instance;
    }

    final int hudX;
    final int hudY = 4;
    final int hudW;
    final int hudH;

    final int cardW = 54;
    final int cardH = 81;
    /** Иконка на карточке — меньше рамки, чтобы текст не давил. */
    final int cardArtSize = 32;
    final int gridCols = 5;
    final int gridRows = 2;
    final int topRowCols = 5;
    final int bottomRowCols = 2;
    final int signTitleW = 300;
    final int signTitleH = 40;
    final int panelHeaderH = signTitleH + 8;
    final int btnW = 100;
    final int btnH = 30;
    final int panelW = 380;

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
    final BufferedImage btnBuyScaled;
    final BufferedImage shopSignTitle;
    final BufferedImage crownIconScaled;
    final BufferedImage crownIconSmall;
    /** Печать герцога вместо текста «Лавка Герцога» в шапке. */
    final BufferedImage dukeSealIconScaled;
    final int dukeSealSize = 32;
    final BufferedImage merchantBgScaled;
    final BufferedImage geraltScaled;
    final BufferedImage dukeScaled;
    final BufferedImage dukeLaughScaled;

    final BufferedImage[] itemIcons = new BufferedImage[5];
    final BufferedImage[] itemArts = new BufferedImage[5];
    final BufferedImage weaponIcon;

    private ShopAssetCache() {
        long t0 = System.currentTimeMillis();
        boolean baked = probeBaked();

        hudW = panelW;
        hudX = (480 - hudW) / 2;
        hudH = 58;
        hudBar = loadSized(UI + "shop_hud_bar.png", hudW, hudH, BASE + "ui/shop_hud_bar.png", true);

        cardFrontScaled = loadSized(UI + "shop_card_front.png", cardW, cardH,
            BASE + "ui/shop_card_front.png", false);
        cardBackScaled = loadSized(UI + "shop_card_back.png", cardW, cardH,
            BASE + "ui/shop_card_back.png", false);
        cardHoverScaled = loadSized(UI + "shop_card_hover.png", cardW, cardH,
            BASE + "ui/shop_card_hover.png", false);
        cardSelectedScaled = loadSized(UI + "shop_card_selected.png", cardW, cardH,
            BASE + "ui/shop_card_selected.png", false);
        btnBuyScaled = loadSized(UI + "shop_btn_buy_disabled.png", btnW, btnH,
            BASE + "ui/shop_btn_buy_disabled.png", false);

        shopSignTitle = loadSized(UI + "shop_sign_title.png", signTitleW, signTitleH,
            BASE + "ui/shop_sign_title.png", true);

        crownIconScaled = loadSized(ICONS + "icon_crown.png", 18, 18,
            BASE + "icons/icon_crown.png", true);
        crownIconSmall = loadSized(ICONS + "icon_crown_small.png", 10, 10,
            BASE + "icons/icon_crown.png", true);
        dukeSealIconScaled = loadSized(ICONS + "icon_duke_seal.png", dukeSealSize, dukeSealSize,
            BASE + "icons/icon_duke_seal.png", true);

        merchantBgScaled = loadBackground();
        int charH = Math.round(360 * 0.70f);
        geraltScaled = loadPortrait("geralt_portrait_shop.png", charH);
        dukeScaled = loadPortrait("duke_portrait_shop.png", charH);
        dukeLaughScaled = loadPortrait("duke_portrait_fun_shop.png", charH);

        String[] iconNames = {
            "icon_armor_chest.png", "icon_armor_legs.png", "icon_armor_gloves.png",
            "icon_armor_boots.png", "icon_potion.png"
        };
        for (int i = 0; i < 5; i++) {
            itemIcons[i] = loadSized(ICONS + iconNames[i], cardArtSize, cardArtSize,
                BASE + "icons/" + iconNames[i], true);
            itemArts[i] = itemIcons[i];
        }

        weaponIcon = loadSized(ICONS + "icon_weapon.png", cardArtSize, cardArtSize,
            BASE + "icons/icon_weapon.png", true);

        int headerH = panelHeaderH;
        int panelY = hudY + hudH + 6;
        int cardsY = panelY + headerH + 6;
        int cardGap = 6;
        int contentBottom = cardsY + gridRows * cardH + (gridRows - 1) * cardGap;
        int panelDrawH = contentBottom + 6 + btnH + 4 - panelY;
        counterY = hudY + hudH + 2;
        counterH = (360 - 54) - counterY - 4;
        counterForeground = loadSized(BASE + "ui/shop_counter_foreground.png", counterW, counterH,
            BASE + "ui/shop_counter_foreground.png", false);

        catalogPanelScaled = loadSized(UI + "shop_catalog_panel.png", panelW, panelDrawH,
            BASE + "ui/shop_catalog_panel.png", false);

        detailPanelW = 292;
        detailPanelH = 232;
        catalogDetailPanel = loadSized(UI + "shop_catalog_panel.png", detailPanelW, detailPanelH,
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

    private BufferedImage loadSized(String bakedPath, int w, int h, String fallbackPath, boolean crop) {
        BufferedImage baked = load(bakedPath);
        if (baked != null && baked.getWidth() == w && baked.getHeight() == h) {
            return baked;
        }
        BufferedImage src = load(fallbackPath);
        if (src == null) {
            return baked;
        }
        if (crop) {
            Rectangle box = ShopScreen.computeContentBoundsPublic(src);
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
        int w = Math.round(src.getWidth() * ((float) targetH / src.getHeight()));
        return PixelScaler.crispScale(src, w, targetH);
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
}
