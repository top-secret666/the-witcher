package main.java.com.witcher.ui.shop.view;

import main.java.com.witcher.ui.shop.EquipmentFilter;
import main.java.com.witcher.shop.EquipSlot;

import java.awt.Rectangle;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_BAR_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_ICON;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_GRID_CELL;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_GRID_COLS;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_LIST_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_MARGIN;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_RIGHT_COL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_STATS_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_STATS_W;

/** Геометрия оверлея экипировки — без отрисовки и без логики. */
public final class EquipmentOverlayLayout {

    public static final int SLOT_SIZE = 48;
    private static final int SLOT_GAP = 8;

    public final Rectangle panel;
    public final Rectangle backButton;
    public final int listX;
    public final int listW;
    public final int listY;
    public final int leftPanelH;
    public final int filterY;
    public final int statsX;
    public final int statsY;
    public final int statsW;
    public final int statsH;
    public final int gridX0;
    public final int gridY0;
    public final int gridBottom;
    public final int portraitX;
    public final int portraitY;
    public final int portraitW;
    public final int portraitH;
    public final int slotX;
    public final int slotY0;
    public final int contentTop;

    private EquipmentOverlayLayout(Rectangle panel, Rectangle backButton, int listX, int listW, int listY,
                                   int leftPanelH, int filterY, int statsX, int statsY, int statsW, int statsH,
                                   int gridX0, int gridY0, int gridBottom, int portraitX, int portraitY,
                                   int portraitW, int portraitH, int slotX, int slotY0, int contentTop) {
        this.panel = panel;
        this.backButton = backButton;
        this.listX = listX;
        this.listW = listW;
        this.listY = listY;
        this.leftPanelH = leftPanelH;
        this.filterY = filterY;
        this.statsX = statsX;
        this.statsY = statsY;
        this.statsW = statsW;
        this.statsH = statsH;
        this.gridX0 = gridX0;
        this.gridY0 = gridY0;
        this.gridBottom = gridBottom;
        this.portraitX = portraitX;
        this.portraitY = portraitY;
        this.portraitW = portraitW;
        this.portraitH = portraitH;
        this.slotX = slotX;
        this.slotY0 = slotY0;
        this.contentTop = contentTop;
    }

    public static EquipmentOverlayLayout compute(int sw, int sh) {
        int px = EQUIP_MARGIN;
        int py = EQUIP_MARGIN;
        int panelW = sw - EQUIP_MARGIN * 2;
        int panelH = sh - EQUIP_MARGIN * 2;

        int backX = px + 8;
        int backY = py + 8;

        int rightX = px + panelW - EQUIP_RIGHT_COL_W - 8;
        int slotX = rightX + (EQUIP_RIGHT_COL_W - SLOT_SIZE) / 2;

        int contentTop = py + 32;
        int contentBottom = py + panelH - 8;

        int listX = px + 8;
        int listW = EQUIP_LIST_W;
        int listY = contentTop;
        int panelBottom = contentBottom - 6;
        int leftPanelH = panelBottom - listY;

        int filterY = listY + 5;

        int statsH = EQUIP_STATS_H;
        int statsW = EQUIP_STATS_W;
        int statsX = listX + 4;
        int statsY = panelBottom - statsH - 5;

        int gridY0 = filterY + EQUIP_FILTER_BAR_H + 5;
        int gridBottom = statsY - 5;
        int gridX0 = listX + (listW - EQUIP_GRID_COLS * EQUIP_GRID_CELL) / 2;

        int portraitX = listX + listW + 6;
        int portraitY = contentTop;
        int portraitW = rightX - portraitX - 6;
        int portraitH = contentBottom - contentTop;

        return new EquipmentOverlayLayout(
            new Rectangle(px, py, panelW, panelH),
            new Rectangle(backX, backY, 18, 18),
            listX, listW, listY, leftPanelH, filterY,
            statsX, statsY, statsW, statsH,
            gridX0, gridY0, gridBottom,
            portraitX, portraitY, portraitW, portraitH,
            slotX, contentTop, contentTop);
    }

    public Rectangle gridCell(int index) {
        int col = index % EQUIP_GRID_COLS;
        int row = index / EQUIP_GRID_COLS;
        int cellX = gridX0 + col * EQUIP_GRID_CELL;
        int cellY = gridY0 + row * EQUIP_GRID_CELL;
        return new Rectangle(cellX, cellY, EQUIP_GRID_CELL, EQUIP_GRID_CELL);
    }

    public boolean gridCellFits(int index) {
        return gridCell(index).y + EQUIP_GRID_CELL <= gridBottom;
    }

    public Rectangle filterButton(int index, int colX, int colY, int colW) {
        int filterCount = EquipmentFilter.values().length;
        int padX = 5;
        int cellW = (colW - padX * 2) / filterCount;
        int btnSize = EQUIP_FILTER_ICON + 4;
        int iconY = colY + 2;
        int cellX = colX + padX + index * cellW;
        return new Rectangle(cellX + (cellW - btnSize) / 2, iconY - 1, btnSize, btnSize);
    }

    public int filterIconX(int index, int colX, int colW) {
        int filterCount = EquipmentFilter.values().length;
        int padX = 5;
        int cellW = (colW - padX * 2) / filterCount;
        int cellX = colX + padX + index * cellW;
        return cellX + (cellW - EQUIP_FILTER_ICON) / 2;
    }

    public Rectangle equipSlot(int index) {
        int sy = slotY0 + index * (SLOT_SIZE + SLOT_GAP);
        return new Rectangle(slotX, sy, SLOT_SIZE, SLOT_SIZE);
    }

    public EquipSlot[] equipSlots() {
        return EquipSlot.values();
    }
}
