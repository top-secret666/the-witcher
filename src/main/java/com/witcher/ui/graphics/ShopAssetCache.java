package main.java.com.witcher.ui.graphics;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Ассеты лавки — загрузка и даунскейл один раз за сессию (не при каждом входе в лавку).
 */
final class ShopAssetCache {

    private static ShopAssetCache instance;

    static ShopAssetCache get() {
        if (instance == null) {
            instance = new ShopAssetCache();
        }
        return instance;
    }

    private static final String UI = "/assets/sprites/lavka/ui/";
    private static final String ICONS = "/assets/sprites/lavka/icons/";

    final int hudX;
    final int hudY = 4;
    final int hudW;
    final int hudH;

    final int cardW = 54;
    final int cardH = 81;
    final int cardArtSize = cardW - 10;
    final int btnW = 100;
    final int btnH = 30;

    final int panelW;

    final BufferedImage hudBar;
    final BufferedImage catalogPanel;
    final BufferedImage catalogPanelScaled;
    final BufferedImage cardFrontScaled;
    final BufferedImage cardBackScaled;
    final BufferedImage cardHoverScaled;
    final BufferedImage cardSelectedScaled;
    final BufferedImage btnBuyScaled;
    final BufferedImage crownIconScaled;
    final BufferedImage crownIconSmall;
    final BufferedImage merchantBgScaled;
    final BufferedImage geraltScaled;
    final BufferedImage dukeScaled;
    final BufferedImage dukeLaughScaled;

    final BufferedImage[] itemIcons = new BufferedImage[5];
    final BufferedImage[] itemArts = new BufferedImage[5];

    private ShopAssetCache() {
        long t0 = System.currentTimeMillis();

        BufferedImage hudSrc = load(UI + "shop_hud_bar.png");
        Rectangle hudCrop = hudSrc != null ? ShopScreen.computeContentBoundsPublic(hudSrc) : null;

        // Ширина HUD = ширина панели «Товары» (5 карт + отступы), чтобы плашки не «съезжали»
        panelW = Math.max(5 * cardW + 4 * 6 + 28, Math.min(480 - 88, 380));
        hudW = panelW;
        hudX = (480 - hudW) / 2;
        if (hudCrop != null) {
            hudH = Math.max(52, Math.min(60, Math.round(hudW * (float) hudCrop.height / hudCrop.width)));
            hudBar = PixelScaler.crispScaleRegion(hudSrc, hudCrop, hudW, hudH);
        } else {
            hudH = 56;
            hudBar = hudSrc != null ? PixelScaler.crispScale(hudSrc, hudW, hudH) : null;
        }

        catalogPanel = load(UI + "shop_catalog_panel.png");
        BufferedImage cardFront = loadFirst(UI + "shop_card_front.png", UI + "icon_legendary_frame.png");
        BufferedImage cardBack = load(UI + "shop_card_back.png");
        BufferedImage cardHover = load(UI + "shop_card_hover.png");
        BufferedImage cardSelected = load(UI + "shop_card_selected.png");
        BufferedImage btnBuyDisabled = load(UI + "shop_btn_buy_disabled.png");
        cardFrontScaled = cardFront != null ? PixelScaler.crispScale(cardFront, cardW, cardH) : null;
        cardBackScaled = cardBack != null ? PixelScaler.crispScale(cardBack, cardW, cardH) : null;
        cardHoverScaled = cardHover != null ? PixelScaler.crispScale(cardHover, cardW, cardH) : null;
        cardSelectedScaled = cardSelected != null ? PixelScaler.crispScale(cardSelected, cardW, cardH) : null;
        btnBuyScaled = btnBuyDisabled != null ? PixelScaler.crispScale(btnBuyDisabled, btnW, btnH) : null;

        BufferedImage crownSrc = load(ICONS + "icon_crown.png");
        Rectangle crownCrop = crownSrc != null ? ShopScreen.computeContentBoundsPublic(crownSrc) : null;
        if (crownSrc != null && crownCrop != null) {
            crownIconScaled = PixelScaler.crispScaleRegion(crownSrc, crownCrop, 18, 18);
            crownIconSmall = PixelScaler.crispScaleRegion(crownSrc, crownCrop, 10, 10);
        } else {
            crownIconScaled = null;
            crownIconSmall = null;
        }

        BufferedImage bg = loadFirst(
            "/assets/sprites/lavka/merchant_bg_lavka.png",
            "/assets/sprites/lavka/lavka.png"
        );
        merchantBgScaled = bakeCover(bg, 480, 360);

        int charH = Math.round(360 * 0.70f);
        geraltScaled = bakeChar(loadFirst(
            "/assets/sprites/lavka/geralt_portrait_shop.png",
            "/assets/sprites/screen saver/geralt_portrait.png"), charH);
        dukeScaled = bakeChar(loadFirst(
            "/assets/sprites/lavka/duke_portrait_shop.png",
            "/assets/sprites/screen saver/duke_portrait.png"), charH);
        dukeLaughScaled = bakeChar(loadFirst(
            "/assets/sprites/lavka/duke_portrait_fun_shop.png",
            "/assets/sprites/screen saver/duke_portrait_fun.png"), charH);

        String[] iconPaths = {
            ICONS + "icon_armor_chest.png",
            ICONS + "icon_armor_legs.png",
            ICONS + "icon_armor_gloves.png",
            ICONS + "icon_armor_boots.png",
            ICONS + "icon_potion.png"
        };
        String[] artPaths = {
            ICONS + "card_art_chest.png",
            ICONS + "card_art_legs.png",
            ICONS + "card_art_gloves.png",
            ICONS + "card_art_boots.png",
            ICONS + "card_art_potion.png"
        };
        for (int i = 0; i < 5; i++) {
            BufferedImage icon = load(iconPaths[i]);
            BufferedImage art = load(artPaths[i]);
            itemIcons[i] = icon != null ? PixelScaler.crispScale(icon, cardArtSize, cardArtSize) : null;
            itemArts[i] = art != null ? PixelScaler.crispScale(art, cardArtSize, cardArtSize) : null;
        }

        int headerH = 22;
        int panelY = hudY + hudH + 6;
        int cardsY = panelY + headerH + 6;
        int contentBottom = cardsY + cardH;
        int panelDrawH = contentBottom + 6 + btnH + 4 - panelY;
        catalogPanelScaled = catalogPanel != null
            ? PixelScaler.crispScale(catalogPanel, panelW, panelDrawH) : null;

        System.out.println("Лавка: ассеты загружены за " + (System.currentTimeMillis() - t0) + " мс");
    }

    private static BufferedImage bakeCover(BufferedImage img, int sw, int sh) {
        if (img == null) return null;
        float scale = Math.max((float) sw / img.getWidth(), (float) sh / img.getHeight());
        return PixelScaler.crispScale(img, Math.round(img.getWidth() * scale), Math.round(img.getHeight() * scale));
    }

    private static BufferedImage bakeChar(BufferedImage sprite, int targetH) {
        if (sprite == null || targetH <= 0) return null;
        int w = Math.round(sprite.getWidth() * ((float) targetH / sprite.getHeight()));
        return PixelScaler.crispScale(sprite, w, targetH);
    }

    private static BufferedImage load(String path) {
        Sprite s = Sprite.loadOptional(path);
        return s != null ? s.getImage() : null;
    }

    private static BufferedImage loadFirst(String... paths) {
        for (String p : paths) {
            BufferedImage img = load(p);
            if (img != null) return img;
        }
        return null;
    }
}
