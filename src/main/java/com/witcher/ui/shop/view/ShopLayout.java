package main.java.com.witcher.ui.shop.view;

import java.awt.Point;
import java.awt.Rectangle;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.*;

/** Раскладка витрины на виртуальном кадре 480×360. */
public final class ShopLayout {

    public final int hudY;
    public final int hudH;
    public final int hudX;
    public final int hudW;
    public final int panelX;
    public final int panelY;
    public final int panelW;
    public final int panelH;
    public final int headerH;
    public final int btnX;
    public final int btnY;
    public final int btnW;
    public final int btnH;
    public final int cardW;
    public final int cardH;
    public final int cardGap;
    public final int cardsStartX;
    public final int cardsY;
    public final int gridCols;
    public final int gridRows;
    public final int dialogTop;
    public final int cardsStartXBottom;

    public ShopLayout(int sw, int sh, int itemCount, int hudX, int hudW, int hudH, int fixedPanelW,
                      int panelHeaderH, int topRowCols, int bottomRowCols) {
        this.hudX = hudX;
        this.hudW = hudW;
        hudY = 4;
        this.hudH = hudH;
        dialogTop = sh - DIALOG_TEXT_ZONE;
        btnH = 30;
        btnW = 100;
        headerH = panelHeaderH;
        cardW = 54;
        cardH = 81;
        cardGap = 6;
        gridCols = GRID_COLS;
        gridRows = itemCount > topRowCols ? 2 : 1;

        int rowW = topRowCols * cardW + (topRowCols - 1) * cardGap;
        panelW = fixedPanelW;
        panelX = (sw - panelW) / 2;
        panelY = hudY + hudH + 6;
        cardsStartX = panelX + (panelW - rowW) / 2;

        int bottomCount = Math.min(bottomRowCols, Math.max(0, itemCount - topRowCols));
        int bottomRowW = bottomCount * cardW + Math.max(0, bottomCount - 1) * cardGap;
        cardsStartXBottom = panelX + (panelW - bottomRowW) / 2;

        int gridContentH = gridRows * cardH + (gridRows - 1) * cardGap;
        panelH = dialogTop - panelY - PANEL_BOTTOM_MARGIN;
        int innerTop = panelY + headerH;
        int innerH = Math.max(gridContentH, panelH - headerH);
        cardsY = innerTop + Math.max(4, (innerH - gridContentH) / 2);

        int contentBottom = cardsY + gridContentH;
        btnX = panelX + (panelW - btnW) / 2;
        btnY = contentBottom + 6;
    }

    public Point cardSlot(int index) {
        if (index < TOP_ROW_COLS) {
            int col = index;
            int x = cardsStartX + col * (cardW + cardGap);
            int y = cardsY;
            return new Point(x, y);
        }
        int bottomIndex = index - TOP_ROW_COLS;
        int x = cardsStartXBottom + bottomIndex * (cardW + cardGap);
        int y = cardsY + cardH + cardGap;
        return new Point(x, y);
    }

    public Rectangle leftCategoryCardSlot(int detailPanelW) {
        int catalogX = VIRTUAL_W - detailPanelW - 8;
        int gap = 10;
        int x = 4;
        int w = catalogX - gap - x;
        int h = w * 81 / 54;
        int maxH = dialogTop - 12;
        if (h > maxH) {
            h = maxH;
            w = h * 54 / 81;
        }
        int y = 8;
        return new Rectangle(x, y, w, h);
    }

    public Rectangle detailListPanelSlot(int detailW, int detailH) {
        int x = VIRTUAL_W - detailW - 8;
        int y = 50;
        return new Rectangle(x, y, detailW, detailH);
    }

    public int categoryCounterY() {
        return 6;
    }

    public int categoryCounterH(int dialogTopY) {
        return dialogTopY - categoryCounterY() - 4;
    }
}
