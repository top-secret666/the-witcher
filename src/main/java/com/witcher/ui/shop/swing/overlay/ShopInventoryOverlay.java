package main.java.com.witcher.ui.shop.swing.overlay;

import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.UiChrome;
import main.java.com.witcher.ui.shop.ShopInventoryKind;
import main.java.com.witcher.ui.shop.ShopInventorySlot;
import main.java.com.witcher.ui.shop.presenter.ShopSessionState;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_ACTION_BTN_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_ARMOUR_VISIBLE_ROWS;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_DETAIL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_GRID_COLS;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_GRID_GAP;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_POUCH_ICON;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_SCROLLBAR_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_SPECIAL_VISIBLE_ROWS;

/**
 * Инвентарь: слева арт + описание + кнопка, справа две сетки
 * (особые сверху, купленная броня снизу) со своими скроллбарами.
 */
public final class ShopInventoryOverlay {

  public interface SlotCallbacks {
    void drawIcon(Graphics2D g, ShopInventorySlot slot, int x, int y, int size,
                  boolean focused, boolean hovered);

    int drawDetail(Graphics2D g, ShopInventorySlot slot, int x, int y, int maxW, int maxH);
  }

  private ShopInventoryOverlay() {
  }

  public static void draw(
      Graphics2D g,
      ShopOverlayContext ctx,
      List<ShopInventorySlot> slots,
      int focusedIndex,
      int hoveredIndex,
      List<Rectangle> slotBoundsOut,
      SlotCallbacks callbacks,
      int sw,
      int sh) {
    Composite prev = g.getComposite();
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    int px = (sw - INVENTORY_PANEL_W) / 2;
    int py = (sh - INVENTORY_PANEL_H) / 2 - 8;
    ShopSessionState ui = ctx.ui();
    ui.inventoryPanelBounds.setBounds(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.96f));
    g.setColor(new Color(18, 12, 8, 245));
    g.fillRoundRect(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 8, 8);
    g.setColor(new Color(150, 110, 50));
    g.drawRoundRect(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 8, 8);

    GameFonts.applyGothicHints(g);
    g.setFont(GameFonts.get().uiBold(13));
    g.setColor(new Color(255, 220, 140));
    g.drawString("Инвентарь", px + 12, py + 20);

    ui.inventoryCloseBounds.setBounds(UiChrome.closeButtonRect(px, py, INVENTORY_PANEL_W));
    UiChrome.drawCloseButton(g, ui.inventoryCloseBounds, ui.inventoryCloseHovered, 1f);

    int contentTop = py + 30;
    int contentBottom = py + INVENTORY_PANEL_H - 10;
    int detailX = px + 12;
    int detailW = INVENTORY_DETAIL_W;
    int gap = 10;
    int gridX = detailX + detailW + gap;
    int gridRight = px + INVENTORY_PANEL_W - 12;
    int gridW = gridRight - gridX;

    int cell = INVENTORY_POUCH_ICON;
    int cellGap = 6;
    int cols = Math.max(1, Math.min(INVENTORY_GRID_COLS,
        Math.max(1, (gridW - INVENTORY_SCROLLBAR_W - 2 + cellGap) / (cell + cellGap))));

    List<Integer> specialIdx = new ArrayList<>();
    List<Integer> armourIdx = new ArrayList<>();
    for (int i = 0; i < slots.size(); i++) {
      if (slots.get(i).kind() == ShopInventoryKind.ARMOUR) {
        armourIdx.add(i);
      } else {
        specialIdx.add(i);
      }
    }

    int labelH = 11;
    int rowH = cell + cellGap;
    int specialH = INVENTORY_SPECIAL_VISIBLE_ROWS * rowH - cellGap;
    int armourH = INVENTORY_ARMOUR_VISIBLE_ROWS * rowH - cellGap;
    int specialLabelY = contentTop + labelH - 2;
    int specialY = contentTop + labelH;
    int armourLabelY = specialY + specialH + INVENTORY_GRID_GAP + labelH - 2;
    int armourY = specialY + specialH + INVENTORY_GRID_GAP + labelH;
    if (armourY + armourH > contentBottom) {
      armourH = Math.max(cell, contentBottom - armourY);
    }

    g.setFont(GameFonts.get().uiPlain(8));
    g.setColor(new Color(160, 130, 80));
    g.drawString("Особые", gridX, specialLabelY);
    g.drawString("Броня", gridX, armourLabelY);

    Rectangle specialBounds = new Rectangle(gridX, specialY, gridW, specialH);
    Rectangle armourBounds = new Rectangle(gridX, armourY, gridW, armourH);
    ui.inventorySpecialGridBounds.setBounds(specialBounds);
    ui.inventoryArmourGridBounds.setBounds(armourBounds);

    int specialRows = Math.max(1, (specialIdx.size() + cols - 1) / cols);
    int armourRows = Math.max(1, (armourIdx.size() + cols - 1) / cols);
    int maxSpecialScroll = Math.max(0, specialRows - INVENTORY_SPECIAL_VISIBLE_ROWS);
    int maxArmourScroll = Math.max(0, armourRows - INVENTORY_ARMOUR_VISIBLE_ROWS);
    ui.inventorySpecialScroll = clamp(ui.inventorySpecialScroll, 0, maxSpecialScroll);
    ui.inventoryArmourScroll = clamp(ui.inventoryArmourScroll, 0, maxArmourScroll);

    slotBoundsOut.clear();
    for (int i = 0; i < slots.size(); i++) {
      slotBoundsOut.add(new Rectangle(0, 0, 0, 0));
    }

    drawGridSection(g, slots, specialIdx, specialBounds, cols, cell, cellGap,
        ui.inventorySpecialScroll, INVENTORY_SPECIAL_VISIBLE_ROWS,
        focusedIndex, hoveredIndex, slotBoundsOut, callbacks, maxSpecialScroll > 0,
        ui.inventorySpecialScroll, maxSpecialScroll);

    drawGridSection(g, slots, armourIdx, armourBounds, cols, cell, cellGap,
        ui.inventoryArmourScroll, INVENTORY_ARMOUR_VISIBLE_ROWS,
        focusedIndex, hoveredIndex, slotBoundsOut, callbacks, maxArmourScroll > 0,
        ui.inventoryArmourScroll, maxArmourScroll);

    int actionH = INVENTORY_ACTION_BTN_H;
    int actionY = contentBottom - actionH;
    int detailMaxH = actionY - contentTop - 8;
    int detailBottom = contentTop;
    String actionLabel = "Экипировка";
    if (!slots.isEmpty() && focusedIndex >= 0 && focusedIndex < slots.size()) {
      ShopInventorySlot focused = slots.get(focusedIndex);
      actionLabel = focused.actionLabel();
      detailBottom = callbacks.drawDetail(g, focused, detailX, contentTop, detailW, detailMaxH);
    }

    int equipBtnW = Math.min(detailW, 120);
    int equipBtnX = detailX + (detailW - equipBtnW) / 2;
    int equipBtnY = Math.max(actionY, Math.min(contentBottom - actionH, detailBottom + 6));
    ui.inventoryEquipButtonBounds.setBounds(equipBtnX, equipBtnY, equipBtnW, actionH);
    g.setFont(GameFonts.get().uiBold(10));
    g.setColor(new Color(28, 18, 8, 220));
    g.fillRoundRect(equipBtnX, equipBtnY, equipBtnW, actionH, 5, 5);
    g.setColor(new Color(170, 125, 55));
    g.drawRoundRect(equipBtnX, equipBtnY, equipBtnW, actionH, 5, 5);
    g.setColor(new Color(255, 225, 150));
    FontMetrics efm = g.getFontMetrics();
    g.drawString(actionLabel, equipBtnX + (equipBtnW - efm.stringWidth(actionLabel)) / 2, equipBtnY + 16);

    g.setComposite(prev);
  }

  private static void drawGridSection(
      Graphics2D g,
      List<ShopInventorySlot> slots,
      List<Integer> indices,
      Rectangle area,
      int cols,
      int cell,
      int cellGap,
      int scrollRows,
      int visibleRows,
      int focusedIndex,
      int hoveredIndex,
      List<Rectangle> slotBoundsOut,
      SlotCallbacks callbacks,
      boolean showScrollbar,
      int scrollValue,
      int scrollMax) {
    int trackW = showScrollbar ? INVENTORY_SCROLLBAR_W : 0;
    int gridInnerW = area.width - trackW - (showScrollbar ? 2 : 0);
    int rowH = cell + cellGap;

    Shape prevClip = g.getClip();
    // Запас для скруглённой рамки выделения (иначе верх/бок срезаются клипом).
    int pad = 3;
    g.clipRect(area.x - pad, area.y - pad, gridInnerW + pad * 2, area.height + pad * 2);

    for (int local = 0; local < indices.size(); local++) {
      int col = local % cols;
      int row = local / cols;
      if (row < scrollRows || row >= scrollRows + visibleRows) {
        continue;
      }
      int drawRow = row - scrollRows;
      int x = area.x + col * (cell + cellGap);
      int y = area.y + drawRow * rowH;
      if (y + cell > area.y + area.height + pad) {
        break;
      }
      int global = indices.get(local);
      Rectangle bounds = new Rectangle(x, y, cell, cell);
      slotBoundsOut.set(global, bounds);
      callbacks.drawIcon(g, slots.get(global), x, y, cell,
          global == focusedIndex, global == hoveredIndex);
    }
    g.setClip(prevClip);

    if (showScrollbar) {
      drawScrollbar(g, area.x + area.width - trackW, area.y, trackW, area.height,
          scrollValue, scrollMax, visibleRows);
    }
  }

  private static void drawScrollbar(Graphics2D g, int x, int y, int w, int h,
                                    int scroll, int maxScroll, int visibleRows) {
    g.setColor(new Color(40, 28, 16, 200));
    g.fillRoundRect(x, y, w, h, 3, 3);
    g.setColor(new Color(100, 75, 40));
    g.drawRoundRect(x, y, w, h, 3, 3);

    int total = maxScroll + visibleRows;
    if (total <= 0) {
      return;
    }
    float thumbRatio = visibleRows / (float) total;
    int thumbH = Math.max(12, Math.round(h * thumbRatio));
    float t = maxScroll <= 0 ? 0f : scroll / (float) maxScroll;
    int thumbY = y + Math.round((h - thumbH) * t);
    g.setColor(new Color(190, 150, 70));
    g.fillRoundRect(x + 1, thumbY, w - 2, thumbH, 2, 2);
  }

  private static int clamp(int v, int min, int max) {
    return Math.max(min, Math.min(max, v));
  }
}
