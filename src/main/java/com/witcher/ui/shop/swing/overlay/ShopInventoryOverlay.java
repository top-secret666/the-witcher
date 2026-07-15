package main.java.com.witcher.ui.shop.swing.overlay;

import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.UiChrome;
import main.java.com.witcher.ui.shop.ShopInventorySlot;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.List;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_ACTION_BTN_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_DETAIL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_GRID_COLS;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_POUCH_ICON;

/**
 * Инвентарь: слева арт + описание + действие, справа сетка иконок без линий.
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
    ctx.ui().inventoryPanelBounds.setBounds(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.96f));
    g.setColor(new Color(18, 12, 8, 245));
    g.fillRoundRect(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 8, 8);
    g.setColor(new Color(150, 110, 50));
    g.drawRoundRect(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 8, 8);

    GameFonts.applyGothicHints(g);
    g.setFont(GameFonts.get().uiBold(13));
    g.setColor(new Color(255, 220, 140));
    g.drawString("Инвентарь", px + 12, py + 20);

    ctx.ui().inventoryCloseBounds.setBounds(UiChrome.closeButtonRect(px, py, INVENTORY_PANEL_W));
    UiChrome.drawCloseButton(g, ctx.ui().inventoryCloseBounds, ctx.ui().inventoryCloseHovered, 1f);

    int contentTop = py + 30;
    int contentBottom = py + INVENTORY_PANEL_H - 10;
    int detailX = px + 12;
    int detailW = INVENTORY_DETAIL_W;
    int gap = 10;
    int gridX = detailX + detailW + gap;
    int gridW = px + INVENTORY_PANEL_W - 12 - gridX;
    int cell = INVENTORY_POUCH_ICON;
    int cellGap = 6;
    int cols = Math.max(1, Math.min(INVENTORY_GRID_COLS, Math.max(1, (gridW + cellGap) / (cell + cellGap))));

    slotBoundsOut.clear();
    for (int i = 0; i < slots.size(); i++) {
      int col = i % cols;
      int row = i / cols;
      int x = gridX + col * (cell + cellGap);
      int y = contentTop + row * (cell + cellGap);
      if (y + cell > contentBottom) {
        break;
      }
      Rectangle bounds = new Rectangle(x, y, cell, cell);
      slotBoundsOut.add(bounds);
      boolean focused = i == focusedIndex;
      boolean hovered = i == hoveredIndex;
      callbacks.drawIcon(g, slots.get(i), x, y, cell, focused, hovered);
    }

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
    ctx.ui().inventoryEquipButtonBounds.setBounds(equipBtnX, equipBtnY, equipBtnW, actionH);
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
}
