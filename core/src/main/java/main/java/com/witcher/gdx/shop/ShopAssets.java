package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.utils.Disposable;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/** Текстуры лавки — один раз загрузить, потом dispose. */
public final class ShopAssets implements Disposable {

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

    public Texture iconChest;
    public Texture iconLegs;
    public Texture iconGloves;
    public Texture iconBoots;
    public Texture iconPotion;

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
        PixelTextures.LoadedTexture bg = PixelTextures.loadFirstMeta(
            "sprites/lavka/merchant_bg_lavka.png",
            "sprites/lavka/lavka.png",
            "sprites/screen saver/lavka.png"
        );
        if (bg != null) {
            merchantBg = bg.texture;
            merchantBgSource = bg.filePath;
        } else {
            merchantBg = PixelTextures.createFallbackShopBg(640, 360);
            merchantBgSource = "generated";
            com.badlogic.gdx.Gdx.app.error("ShopAssets",
                "Fon lavki ne najden (merchant_bg_lavka.png / lavka.png)");
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

        PixelTextures.LoadedTexture hudLoaded = PixelTextures.loadOptionalMeta("sprites/lavka/ui/shop_hud_bar.png");
        if (hudLoaded == null) {
            com.badlogic.gdx.Gdx.app.error("ShopAssets",
                "HUD ne najden: sprites/lavka/ui/shop_hud_bar.png");
        } else {
            hudBar = hudLoaded.texture;
            int[] crop = PixelTextures.computeContentBounds("sprites/lavka/ui/shop_hud_bar.png");
            if (crop != null && crop[2] > 4 && crop[3] > 4) {
                int padX = 6;
                int padTop = 14;
                int padBottom = 6;
                int cx = Math.max(0, crop[0] - padX);
                int cy = Math.max(0, crop[1] - padTop);
                int cw = Math.min(hudBar.getWidth() - cx, crop[2] + padX * 2);
                int ch = Math.min(hudBar.getHeight() - cy, crop[3] + padTop + padBottom);
                hud.cropped = true;
                hud.cropX = cx;
                hud.cropY = cy;
                hud.cropW = cw;
                hud.cropH = ch;
                int srcY = hudBar.getHeight() - cy - ch;
                hudBarRegion = new TextureRegion(hudBar, cx, srcY, cw, ch);
                float aspect = (float) ch / cw;
                hud.drawH = 58;
                hud.drawW = Math.round(hud.drawH / aspect);
                if (hud.drawW > 476) {
                    hud.drawW = 476;
                    hud.drawH = Math.max(40, Math.round(hud.drawW * aspect));
                }
            } else {
                hud.cropped = false;
                hudBarRegion = new TextureRegion(hudBar);
                float aspect = (float) hudBar.getHeight() / hudBar.getWidth();
                hud.drawH = 58;
                hud.drawW = Math.round(hud.drawH / aspect);
                if (hud.drawW > 476) {
                    hud.drawW = 476;
                    hud.drawH = Math.max(40, Math.round(hud.drawW * aspect));
                }
            }
            hud.drawX = (int) ((WitcherGame.VIRTUAL_W - hud.drawW) * 0.5f);
            com.badlogic.gdx.Gdx.app.log("ShopAssets",
                "HUD: cropped=" + hud.cropped + " crop=" + hud.cropW + "x" + hud.cropH
                    + " draw=" + hud.drawW + "x" + hud.drawH);
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
