package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.HUD_H;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopRuntimeAssets;
import main.java.com.witcher.ui.shop.view.LavkaAssetPaths;

import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.BOTTOM_ROW_COLS;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.TOP_ROW_COLS;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.CATALOG_DETAIL_PANEL_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.CATALOG_DETAIL_PANEL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.CATALOG_DETAIL_PANEL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.catalogRowContentW;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.DIALOG_TEXT_ZONE;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.PANEL_BOTTOM_MARGIN;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.VIRTUAL_H;

/**
 * LibGDX-ассеты лавки — аналог {@link main.java.com.witcher.ui.graphics.ShopAssetCache}.
 */
public final class GdxShopRuntimeAssets implements ShopRuntimeAssets {

    private static GdxShopRuntimeAssets instance;

    public static GdxShopRuntimeAssets get() {
        if (instance == null) {
            instance = new GdxShopRuntimeAssets();
        }
        return instance;
    }

    public final int hudX;
    public final int hudY = 4;
    public final int hudW;
    public final int hudH;

    public final int cardW = 54;
    public final int cardH = 81;
    public final int cardArtSize = 38;
    public final int gridCols = 5;
    public final int gridRows = 2;
    public final int topRowCols = TOP_ROW_COLS;
    public final int bottomRowCols = BOTTOM_ROW_COLS;
    public final int panelHeaderH = 8;
    public final int btnW = 100;
    public final int btnH = 30;
    public final int panelW = 380;
    public final int dukeSealSize = Math.max(
        main.java.com.witcher.ui.shop.view.ShopViewConstants.HUD_DUKE_SEAL_W,
        main.java.com.witcher.ui.shop.view.ShopViewConstants.HUD_DUKE_SEAL_H);

    public Texture merchantBgScaled;
    public Texture geraltScaled;
    public Texture dukeScaled;
    public Texture dukeLaughScaled;

    public Texture hudBar;
    public Texture counterForeground;
    public final int counterX = 0;
    public final int counterY;
    public final int counterW = 480;
    public final int counterH;

    public Texture catalogPanelScaled;
    public Texture catalogDetailPanel;
    public Texture rowNormal;
    public Texture rowHover;
    public Texture rowSelected;
    public final int rowW;
    public final int rowH = 24;
    public final int detailPanelW;
    public final int detailPanelH;

    public Texture cardFrontScaled;
    public Texture cardBackScaled;
    public Texture cardHoverScaled;
    public Texture cardSelectedScaled;
    public Texture btnBuyNormal;
    public Texture btnBuyDisabled;
    public Texture crownIconScaled;
    public Texture crownIconSmall;
    public Texture dukeSealIconScaled;

    public final Texture[] itemIcons = new Texture[5];
    public Texture weaponIcon;
    public Texture setsIcon;

    public Texture inventoryBagIcon;
    public Texture inventoryBagClosed;
    public Texture inventoryBagOpen;
    public Texture inventoryBagHover;
    public Texture[] inventoryBagOpenFrames;

    public Texture statVialEmpty;
    public Texture statVialOverlay;
    public Texture statVialEndCap;
    public Texture walletPouch;

    private final Map<ShopCategory, BufferedImage> categoryIconCache = new EnumMap<>(ShopCategory.class);

    private GdxShopRuntimeAssets() {
        long t0 = System.currentTimeMillis();

        hudW = panelW;
        hudX = (480 - hudW) / 2;
        hudH = HUD_H;

        hudBar = loadLavka("ui/shop_hud_bar.png");
        cardFrontScaled = loadLavka("ui/shop_card_front.png");
        cardBackScaled = loadLavkaUiOriginal("shop_card_back.png");
        cardHoverScaled = loadLavka("ui/shop_card_hover.png");
        cardSelectedScaled = loadLavka("ui/shop_card_selected.png");
        btnBuyNormal = loadLavka("ui/shop_btn_buy_normal.png");
        btnBuyDisabled = loadLavka("ui/shop_btn_buy_disabled.png");

        crownIconScaled = loadCategoryIcon("icon_crown.png");
        crownIconSmall = loadCategoryIcon(
            PixelTextures.loadLavkaCategoryIcon("icon_crown_small.png") != null
                ? "icon_crown_small.png" : "icon_crown.png");
        dukeSealIconScaled = loadCategoryIcon("icon_duke_seal.png");

        merchantBgScaled = loadLavka("merchant_bg_lavka.png", "lavka.png");
        if (merchantBgScaled == null) {
            merchantBgScaled = PixelTextures.createFallbackShopBg(640, 360);
        }

        geraltScaled = loadLavka("geralt_portrait_shop.png", "sprites/screen saver/geralt_portrait.png");
        dukeScaled = loadLavka("duke_portrait_shop.png", "sprites/screen saver/duke_portrait.png");
        dukeLaughScaled = loadLavka("duke_portrait_fun_shop.png", "sprites/screen saver/duke_portrait_fun.png");

        String[] iconNames = {
            "icon_armor_chest.png", "icon_armor_legs.png", "icon_armor_gloves.png",
            "icon_armor_boots.png", "icon_potion.png"
        };
        for (int i = 0; i < 5; i++) {
            itemIcons[i] = loadCategoryIcon(iconNames[i]);
        }
        for (ShopCategory cat : ShopCategory.values()) {
            if (cat.iconIndex >= 0 && cat.iconIndex < iconNames.length) {
                categoryIconCache.put(cat,
                    PixelTextures.loadLavkaCategoryIcon(iconNames[cat.iconIndex]));
            }
        }

        weaponIcon = loadCategoryIcon("icon_weapon.png");
        setsIcon = loadCategoryIcon("icon_armor_set.png");
        categoryIconCache.put(ShopCategory.WEAPON,
            PixelTextures.loadLavkaCategoryIcon("icon_weapon.png"));
        categoryIconCache.put(ShopCategory.SETS,
            PixelTextures.loadLavkaCategoryIcon("icon_armor_set.png"));

        int bagSize = 40;
        inventoryBagIcon = loadCategoryIcon("icon_inventory_bag.png");
        inventoryBagClosed = loadLavka("ui/inventory_bag_closed.png");
        inventoryBagOpen = loadLavka("ui/inventory_bag_open.png");
        inventoryBagHover = loadLavka("ui/inventory_bag_hover.png");
        inventoryBagOpenFrames = loadBagOpenFrames(bagSize);

        statVialEmpty = loadLavka("ui/stat_vial_empty.png");
        statVialOverlay = loadLavka("ui/stat_vial_glass_overlay.png");
        statVialEndCap = loadLavka("ui/stat_vial_end_cap.png");
        walletPouch = loadLavka("wallet_pouch_gold.png");

        int panelY = hudY + hudH + 6;
        int dialogTop = VIRTUAL_H - DIALOG_TEXT_ZONE;
        int panelDrawH = dialogTop - panelY - PANEL_BOTTOM_MARGIN;
        counterY = hudY + hudH + 2;
        counterH = dialogTop - counterY - 4;
        counterForeground = loadLavka("ui/shop_counter_foreground.png");

        catalogPanelScaled = loadLavka("ui/shop_catalog_panel.png");
        detailPanelW = CATALOG_DETAIL_PANEL_W;
        detailPanelH = CATALOG_DETAIL_PANEL_H;
        catalogDetailPanel = loadLavka("ui/shop_catalog_panel_detail.png", "ui/shop_catalog_panel.png");
        rowW = catalogRowContentW(CATALOG_DETAIL_PANEL_W);
        rowNormal = loadLavka("ui/shop_row_normal.png");
        rowHover = loadLavka("ui/shop_row_hover.png");
        rowSelected = loadLavka("ui/shop_row_selected.png");

        System.out.println("GdxShopRuntimeAssets: loaded in " + (System.currentTimeMillis() - t0) + " ms");
    }

    private static Texture loadLavka(String relativePath, String... extraFallbacks) {
        return PixelTextures.loadLavka(relativePath, extraFallbacks);
    }

    private static Texture loadLavkaUiOriginal(String fileName) {
        Texture texture = PixelTextures.loadFirst(LavkaAssetPaths.gdxUi(fileName));
        if (texture != null) {
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }
        return texture;
    }

    private static Texture loadCategoryIcon(String fileName) {
        Texture texture = PixelTextures.loadLavkaCategoryIconTexture(fileName);
        if (texture != null) {
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        }
        return texture;
    }

    private Texture[] loadBagOpenFrames(int size) {
        Texture[] frames = new Texture[10];
        int loaded = 0;
        for (int i = 0; i < frames.length; i++) {
            String name = String.format("ui/inventory_bag_open_%02d.png", i);
            Texture frame = loadLavka(name);
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

    private Texture[] splitBagOpenSheet(int size) {
        Texture sheet = loadLavka("ui/inventory_bag_open_sheet.png");
        if (sheet == null) {
            return new Texture[0];
        }
        if (inventoryBagOpen != null) {
            Texture[] fallback = new Texture[10];
            java.util.Arrays.fill(fallback, inventoryBagOpen);
            return fallback;
        }
        return new Texture[0];
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

    @Override
    public BufferedImage iconForCategory(ShopCategory cat) {
        return categoryIconCache.get(cat);
    }

    public void dispose() {
        disposeTex(merchantBgScaled);
        disposeTex(geraltScaled);
        disposeTex(dukeScaled);
        disposeTex(dukeLaughScaled);
        disposeTex(hudBar);
        disposeTex(counterForeground);
        disposeTex(catalogPanelScaled);
        disposeTex(catalogDetailPanel);
        disposeTex(rowNormal);
        disposeTex(rowHover);
        disposeTex(rowSelected);
        disposeTex(cardFrontScaled);
        disposeTex(cardBackScaled);
        disposeTex(cardHoverScaled);
        disposeTex(cardSelectedScaled);
        disposeTex(btnBuyNormal);
        disposeTex(btnBuyDisabled);
        disposeTex(crownIconScaled);
        disposeTex(crownIconSmall);
        disposeTex(dukeSealIconScaled);
        for (Texture icon : itemIcons) {
            disposeTex(icon);
        }
        disposeTex(weaponIcon);
        disposeTex(setsIcon);
        disposeTex(inventoryBagIcon);
        disposeTex(inventoryBagClosed);
        disposeTex(inventoryBagOpen);
        disposeTex(inventoryBagHover);
        if (inventoryBagOpenFrames != null) {
            for (Texture frame : inventoryBagOpenFrames) {
                disposeTex(frame);
            }
        }
        disposeTex(statVialEmpty);
        disposeTex(statVialOverlay);
        disposeTex(statVialEndCap);
        disposeTex(walletPouch);
        categoryIconCache.clear();
        if (instance == this) {
            instance = null;
        }
    }

    private static void disposeTex(Texture texture) {
        PixelTextures.dispose(texture);
    }
}
