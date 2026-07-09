package main.java.com.witcher.ui.shop.view;

/** Геометрия и тайминги лавки — без логики и без отрисовки. */
public final class ShopViewConstants {

    public static final int VIRTUAL_W = 480;
    public static final int VIRTUAL_H = 360;
    public static final int DIALOG_TEXT_ZONE = 54;
    public static final int PANEL_BOTTOM_MARGIN = 4;

    public static final int REVEAL_DURATION_TICKS = 84;
    public static final int CATEGORY_OPEN_DURATION_TICKS = 28;

    public static final int WALLET_APPEAR_TICKS = 30;
    public static final int WALLET_FLY_TICKS = 40;
    public static final int WALLET_FADE_TICKS = 10;
    public static final int WALLET_CLOSE_TICKS = 8;
    public static final int WALLET_BAG_CLOSE_TICKS = WALLET_FADE_TICKS + WALLET_CLOSE_TICKS;
    public static final int WALLET_COUNT_TICKS = 28;
    public static final int WALLET_REVEAL_TOTAL =
        WALLET_APPEAR_TICKS + WALLET_FLY_TICKS + WALLET_BAG_CLOSE_TICKS + WALLET_COUNT_TICKS;

    public static final int PURCHASE_APPEAR_TICKS = 24;
    public static final int PURCHASE_FLY_TICKS = 34;
    public static final int PURCHASE_FADE_TICKS = 10;
    public static final int PURCHASE_CLOSE_TICKS = 10;
    public static final int PURCHASE_TUCK_TICKS = PURCHASE_FADE_TICKS + PURCHASE_CLOSE_TICKS;
    public static final int PURCHASE_REVEAL_TOTAL =
        PURCHASE_APPEAR_TICKS + PURCHASE_FLY_TICKS + PURCHASE_TUCK_TICKS;

    public static final int INVENTORY_BAG_SIZE = 40;
    public static final int INVENTORY_BAG_MARGIN = 8;

    public static final int CATALOG_PANEL_INSET_X = 14;
    public static final int CATALOG_PANEL_INSET_TOP = 20;
    public static final int CATALOG_PANEL_GAP_ABOVE_BUY = 10;

    public static final int PRODUCT_CARD_INSET_X = 7;
    public static final int PRODUCT_CARD_INSET_TOP = 10;
    public static final int PRODUCT_CARD_INSET_BOTTOM = 24;
    public static final int PRODUCT_CARD_ICON_TEXT_GAP = 5;
    public static final int PRODUCT_CARD_NAME_PRICE_GAP = 5;

    public static final int INVENTORY_PANEL_W = 280;
    public static final int INVENTORY_PANEL_H = 238;
    public static final int INVENTORY_POUCH_ICON = 32;
    public static final int INVENTORY_POUCH_LARGE = 96;
    public static final int EQUIP_MARGIN = 4;

    public static final int GRID_COLS = 5;
    public static final int TOP_ROW_COLS = 5;
    public static final int BOTTOM_ROW_COLS = 2;

    /** Минимальная высота HUD; фактическая — по пропорциям арта. */
    public static final int HUD_H = 72;
    /** Чуть меньше полной ширины панели (380). */
    public static final float HUD_BAR_SCALE = 0.90f;
    public static final int HUD_SEAL_MARGIN = 10;

    public static final int HUD_CROWN_W = 24;
    public static final int HUD_CROWN_H = 28;
    public static final int HUD_DUKE_SEAL_W = 38;
    public static final int HUD_DUKE_SEAL_H = 46;

    /** Запекание LibGDX: 2× слота (Renderer pixelScale=2). */
    public static int hudIconBakePx(int slotW, int slotH) {
        return Math.max(slotW, slotH) * 2;
    }

    /** Текст поверх CRT-фильтра — читаемость без «дробления». */
    public static final boolean DEFER_UI_TEXT_TO_OVERLAY = true;

    private ShopViewConstants() {
    }
}
