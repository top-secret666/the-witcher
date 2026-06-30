package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.utils.Disposable;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import com.badlogic.gdx.graphics.Texture;

/** Текстуры лавки — один раз загрузить, потом dispose. */
public final class ShopAssets implements Disposable {

    public Texture merchantBg;
    public Texture geraltPortrait;
    public Texture dukePortrait;
    public Texture dukeLaughPortrait;

    public Texture hudBar;
    public Texture catalogPanel;
    public Texture cardFront;
    public Texture cardBack;
    public Texture cardHover;
    public Texture cardSelected;
    public Texture btnBuyDisabled;
    public Texture crownIcon;

    public Texture iconChest;
    public Texture iconLegs;
    public Texture iconGloves;
    public Texture iconBoots;
    public Texture iconPotion;

    /** Обрезка HUD-плашки (убираем пустые поля в исходном PNG). */
    public final HudLayout hud = new HudLayout();

    public static final class HudLayout {
        public boolean cropped;
        public int cropX;
        public int cropY;
        public int cropW;
        public int cropH;
        public int drawX;
        public int drawW = 476;
        public int drawH = 56;
    }

    public void load() {
        merchantBg = PixelTextures.loadFirst(
            "sprites/lavka/merchant_bg_lavka.png",
            "sprites/lavka/lavka.png",
            "sprites/screen saver/lavka.png"
        );
        if (merchantBg == null) {
            com.badlogic.gdx.Gdx.app.error("ShopAssets",
                "Fon lavki ne najden. Polozhi merchant_bg_lavka.png v assets/sprites/lavka/");
        }

        geraltPortrait = PixelTextures.loadFirst(
            "sprites/lavka/geralt_portrait_shop.png",
            "sprites/screen saver/geralt_portrait.png"
        );
        dukePortrait = PixelTextures.loadFirst(
            "sprites/lavka/duke_portrait_shop.png",
            "sprites/screen saver/duke_portrait.png"
        );
        dukeLaughPortrait = PixelTextures.loadFirst(
            "sprites/lavka/duke_portrait_fun_shop.png",
            "sprites/screen saver/duke_portrait_fun.png"
        );

        hudBar = PixelTextures.loadOptional("sprites/lavka/ui/shop_hud_bar.png");
        if (hudBar != null) {
            int[] crop = PixelTextures.computeOpaqueBounds("sprites/lavka/ui/shop_hud_bar.png");
            if (crop != null) {
                hud.cropped = true;
                hud.cropX = crop[0];
                hud.cropY = crop[1];
                hud.cropW = crop[2];
                hud.cropH = crop[3];
                float aspect = (float) crop[3] / crop[2];
                hud.drawW = 476;
                hud.drawH = Math.max(52, Math.min(64, Math.round(hud.drawW * aspect)));
            }
            hud.drawX = (int) ((WitcherGame.VIRTUAL_W - hud.drawW) * 0.5f);
        }

        catalogPanel = PixelTextures.loadOptional("sprites/lavka/ui/shop_catalog_panel.png");
        cardFront = PixelTextures.loadFirst(
            "sprites/lavka/ui/shop_card_front.png",
            "sprites/lavka/ui/icon_legendary_frame.png"
        );
        cardBack = PixelTextures.loadOptional("sprites/lavka/ui/shop_card_back.png");
        cardHover = PixelTextures.loadOptional("sprites/lavka/ui/shop_card_hover.png");
        cardSelected = PixelTextures.loadOptional("sprites/lavka/ui/shop_card_selected.png");
        btnBuyDisabled = PixelTextures.loadOptional("sprites/lavka/ui/shop_btn_buy_disabled.png");
        crownIcon = PixelTextures.loadOptional("sprites/lavka/icons/icon_crown.png");

        iconChest = PixelTextures.loadOptional("sprites/lavka/icons/icon_armor_chest.png");
        iconLegs = PixelTextures.loadOptional("sprites/lavka/icons/icon_armor_legs.png");
        iconGloves = PixelTextures.loadOptional("sprites/lavka/icons/icon_armor_gloves.png");
        iconBoots = PixelTextures.loadOptional("sprites/lavka/icons/icon_armor_boots.png");
        iconPotion = PixelTextures.loadOptional("sprites/lavka/icons/icon_potion.png");
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
        PixelTextures.dispose(iconChest);
        PixelTextures.dispose(iconLegs);
        PixelTextures.dispose(iconGloves);
        PixelTextures.dispose(iconBoots);
        PixelTextures.dispose(iconPotion);
    }
}
