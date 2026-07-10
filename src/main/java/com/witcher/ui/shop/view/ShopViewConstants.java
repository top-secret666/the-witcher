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

    public static final int CATALOG_PANEL_GAP_ABOVE_BUY = 10;
    /** Правая панель списка товаров в режиме категории. */
    public static final int CATALOG_DETAIL_PANEL_W = 318;
    public static final int CATALOG_DETAIL_PANEL_H = 250;
    public static final int CATALOG_DETAIL_PANEL_Y = 36;
    public static final int CATALOG_DETAIL_PANEL_X_MARGIN = 1;
    /** Левая карточка товара (shop_card_back) в режиме категории. */
    public static final int CATEGORY_OPEN_CARD_X = 2;
    public static final int CATEGORY_OPEN_CARD_Y = 36;
    public static final int CATEGORY_OPEN_CARD_GAP = 3;
    public static final int CATEGORY_OPEN_CARD_W = 156;
    public static final int CATEGORY_OPEN_CARD_H = 234;
    /** Эталон арта {@code shop_card_back.png} — внутренняя зона под текст. */
    public static final int CARD_BACK_REF_W = 832;
    public static final int CARD_BACK_REF_H = 1248;
    public static final int CARD_BACK_INNER_W_REF = 721;
    public static final int CARD_BACK_TEXT_BOTTOM_REF = 90;
    /** Эталон арта {@code shop_catalog_panel_detail} для масштабирования отступов. */
    public static final int CATALOG_DETAIL_REF_W = 292;
    public static final int CATALOG_DETAIL_REF_H = 232;
    /** Горизонтальный отступ золотой рамки (по арту detail panel). */
    public static final int CATALOG_FRAME_INSET_X_REF = 33;
    /** Верх списка товаров от верха панели (по арту). */
    public static final int CATALOG_FRAME_LIST_TOP_REF = 18;
    /** Легенда статов — под кнопкой «Купить». */
    public static final int CATALOG_LEGEND_GAP_BELOW_BUY = 3;

    public static final int PRODUCT_CARD_INSET_X = 7;
    public static final int PRODUCT_CARD_INSET_TOP = 10;
    public static final int PRODUCT_CARD_INSET_BOTTOM = 28;
    public static final int PRODUCT_CARD_ICON_TEXT_GAP = 5;
    public static final int PRODUCT_CARD_NAME_PRICE_GAP = 5;

    public static final int INVENTORY_PANEL_W = 280;
    public static final int INVENTORY_PANEL_H = 238;
    public static final int INVENTORY_POUCH_ICON = 32;
    public static final int INVENTORY_POUCH_LARGE = 96;
    public static final int EQUIP_MARGIN = 2;
    public static final int EQUIP_FILTER_ICON = 22;
    public static final int EQUIP_FILTER_GAP = 4;
    public static final int EQUIP_FILTER_BAR_H = 28;
    public static final int EQUIP_LIST_HEADER_H = 12;
    /** Сетка иконок в инвентаре экипировки (без текста). */
    public static final int EQUIP_GRID_COLS = 4;
    public static final int EQUIP_GRID_CELL = 30;
    public static final int EQUIP_GRID_ICON = 26;
    public static final int EQUIP_LIST_W = EQUIP_GRID_COLS * EQUIP_GRID_CELL + 10;
    /** Колбочки со стеклом — узкая колонка справа под слотами. */
    public static final int EQUIP_STATS_W = 82;
    public static final int EQUIP_STATS_H = 96;
    public static final int EQUIP_RIGHT_COL_W = 52;

    public static final int GRID_COLS = 5;
    public static final int TOP_ROW_COLS = 4;
    public static final int BOTTOM_ROW_COLS = 3;

    /** Минимальная высота HUD; фактическая — по пропорциям арта. */
    public static final int HUD_H = 72;
    /** Чуть меньше полной ширины панели (380). */
    public static final float HUD_BAR_SCALE = 0.90f;
    public static final int HUD_SEAL_MARGIN = 10;

    public static final int HUD_CROWN_W = 24;
    public static final int HUD_CROWN_H = 28;
    public static final int HUD_DUKE_SEAL_W = 38;
    public static final int HUD_DUKE_SEAL_H = 46;

    /** Монетка у цены товара (каталог + карточка). */
    public static final int CATALOG_COIN_SIZE = 8;

    /** Мини-иконки статов в строках каталога. */
    public static final int STAT_ROW_ICON_SIZE = 10;
    /** Иконки в легенде статов под каталогом. */
    public static final int STAT_LEGEND_ICON_SIZE = 12;

    /** Запекание LibGDX: 2× слота (Renderer pixelScale=2). */
    public static final int HUD_ICON_BAKE_SCALE = 2;
    /** Стат-иконки — 6× слота для чёткого даунскейла. */
    public static final int STAT_ICON_BAKE_SCALE = 6;

    public static int hudIconBakePx(int slotW, int slotH) {
        return Math.max(slotW, slotH) * HUD_ICON_BAKE_SCALE;
    }

    public static int catalogCoinBakePx() {
        return CATALOG_COIN_SIZE * HUD_ICON_BAKE_SCALE;
    }

    public static int statIconBakePx() {
        return Math.max(STAT_ROW_ICON_SIZE, STAT_LEGEND_ICON_SIZE) * STAT_ICON_BAKE_SCALE;
    }

    public static int catalogFrameInsetX(int panelW) {
        return Math.round(CATALOG_FRAME_INSET_X_REF * (float) panelW / CATALOG_DETAIL_REF_W);
    }

    public static int catalogRowContentW(int panelW) {
        return panelW - catalogFrameInsetX(panelW) * 2;
    }

    public static int catalogListTopInset(int panelH) {
        return Math.round(CATALOG_FRAME_LIST_TOP_REF * (float) panelH / CATALOG_DETAIL_REF_H);
    }

    public static int catalogRowX(int panelX, int panelW) {
        return panelX + catalogFrameInsetX(panelW);
    }

    public static int catalogDetailPanelX() {
        return VIRTUAL_W - CATALOG_DETAIL_PANEL_W - CATALOG_DETAIL_PANEL_X_MARGIN;
    }

    /** Ширина текста на shop_card_back — по внутренней коричневой зоне арта. */
    public static int categoryCardTextMaxW(int cardW) {
        int inner = Math.round(cardW * (float) CARD_BACK_INNER_W_REF / CARD_BACK_REF_W);
        return Math.max(48, inner - 10);
    }

    public static int categoryCardTextX(int cardX, int cardW) {
        int maxW = categoryCardTextMaxW(cardW);
        return cardX + (cardW - maxW) / 2;
    }

    public static int categoryCardTextBottomInset(int cardH) {
        return Math.round(CARD_BACK_TEXT_BOTTOM_REF * (float) cardH / CARD_BACK_REF_H);
    }

    public static int categoryBuyButtonY(int panelY, int panelH, int btnH) {
        return panelY + panelH - btnH - 8;
    }

    public static int categoryStatLegendY(int panelY, int panelH, int btnH) {
        int btnY = categoryBuyButtonY(panelY, panelH, btnH);
        return btnY + btnH + CATALOG_LEGEND_GAP_BELOW_BUY;
    }

    public static int catalogListBottomY(int panelY, int panelH, int btnH) {
        return categoryBuyButtonY(panelY, panelH, btnH) - CATALOG_PANEL_GAP_ABOVE_BUY;
    }

    /** Текст поверх CRT-фильтра — читаемость без «дробления». */
    public static final boolean DEFER_UI_TEXT_TO_OVERLAY = true;

    private ShopViewConstants() {
    }
}
