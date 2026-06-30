package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.PixelTextures;

/**
 * Текстуры лавки. Приоритет: {@code sprites/lavka/1x/} (общие с GameWindow / bake_lavka_assets.py).
 */
public final class ShopAssets implements Disposable {

    public static final int PANEL_W = 380;
    public static final int CARD_W = 54;
    public static final int CARD_H = 81;
    public static final int GRID_COLS = 5;

    public Texture merchantBg;
    public String merchantBgSource;
    public Texture geraltPortrait;
    public Texture dukePortrait;
    public Texture dukeLaughPortrait;

    public Texture hudBar;
    public TextureRegion hudBarRegion;
    public Texture catalogPanel;
    public Texture cardFront;
    public Texture cardBack;
    public Texture cardHover;
    public Texture cardSelected;
    public Texture btnBuyDisabled;
    public Texture crownIcon;
    public Texture crownIconSmall;
    public Texture setCatalogIcon;

    public Texture iconChest;
    public Texture iconLegs;
    public Texture iconGloves;
    public Texture iconBoots;
    public Texture iconPotion;

    public final HudLayout hud = new HudLayout();

    public static final class HudLayout {
        public int drawX;
        public int drawW = PANEL_W;
        public int drawH = 58;
    }

    public void load() {
        PixelTextures.LoadedTexture bg = PixelTextures.loadLavkaMeta("merchant_bg_lavka.png",
            "sprites/lavka/lavka.png");
        if (bg != null) {
            merchantBg = bg.texture;
            merchantBgSource = bg.filePath;
        } else {
            merchantBg = PixelTextures.createFallbackShopBg(640, 360);
            merchantBgSource = "generated";
        }

        geraltPortrait = PixelTextures.loadLavka("geralt_portrait_shop.png",
            "sprites/screen saver/geralt_portrait.png");
        dukePortrait = PixelTextures.loadLavka("duke_portrait_shop.png",
            "sprites/screen saver/duke_portrait.png");
        dukeLaughPortrait = PixelTextures.loadLavka("duke_portrait_fun_shop.png",
            "sprites/screen saver/duke_portrait_fun.png");

        PixelTextures.LoadedTexture hudLoaded = PixelTextures.loadLavkaMeta("ui/shop_hud_bar.png");
        if (hudLoaded != null) {
            hudBar = hudLoaded.texture;
            hudBarRegion = new TextureRegion(hudBar);
            hud.drawW = hudBar.getWidth();
            hud.drawH = hudBar.getHeight();
            hud.drawX = (int) ((WitcherGame.VIRTUAL_W - hud.drawW) * 0.5f);
            com.badlogic.gdx.Gdx.app.log("ShopAssets", "HUD 1x: " + hud.drawW + "x" + hud.drawH
                + " <- " + hudLoaded.filePath);
        }

        catalogPanel = PixelTextures.loadLavka("ui/shop_catalog_panel.png");
        cardFront = PixelTextures.loadLavka("ui/shop_card_front.png", "sprites/lavka/ui/icon_legendary_frame.png");
        cardBack = PixelTextures.loadLavka("ui/shop_card_back.png");
        cardHover = PixelTextures.loadLavka("ui/shop_card_hover.png");
        cardSelected = PixelTextures.loadLavka("ui/shop_card_selected.png");
        btnBuyDisabled = PixelTextures.loadLavka("ui/shop_btn_buy_disabled.png");
        crownIcon = PixelTextures.loadLavka("icons/icon_crown.png");
        crownIconSmall = PixelTextures.loadLavka("icons/icon_crown_small.png", "icons/icon_crown.png");

        setCatalogIcon = PixelTextures.loadLavka("ui/icon_legendary_frame.png");
        if (setCatalogIcon == null) {
            setCatalogIcon = crownIcon;
        }

        iconChest = PixelTextures.loadLavka("icons/icon_armor_chest.png");
        iconLegs = PixelTextures.loadLavka("icons/icon_armor_legs.png");
        iconGloves = PixelTextures.loadLavka("icons/icon_armor_gloves.png");
        iconBoots = PixelTextures.loadLavka("icons/icon_armor_boots.png");
        iconPotion = PixelTextures.loadLavka("icons/icon_potion.png");
    }

    @Override
    public void dispose() {
        PixelTextures.dispose(merchantBg);
        PixelTextures.dispose(geraltPortrait);
        PixelTextures.dispose(dukePortrait);
        PixelTextures.dispose(dukeLaughPortrait);
        PixelTextures.dispose(hudBar);
        PixelTextures.dispose(catalogPanel);
        PixelTextures.dispose(cardFront);
        PixelTextures.dispose(cardBack);
        PixelTextures.dispose(cardHover);
        PixelTextures.dispose(cardSelected);
        PixelTextures.dispose(btnBuyDisabled);
        PixelTextures.dispose(crownIcon);
        PixelTextures.dispose(crownIconSmall);
        PixelTextures.dispose(setCatalogIcon);
        PixelTextures.dispose(iconChest);
        PixelTextures.dispose(iconLegs);
        PixelTextures.dispose(iconGloves);
        PixelTextures.dispose(iconBoots);
        PixelTextures.dispose(iconPotion);
    }
}
