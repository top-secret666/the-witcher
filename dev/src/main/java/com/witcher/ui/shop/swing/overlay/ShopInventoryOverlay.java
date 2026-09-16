package com.witcher.ui.shop.swing.overlay;

import com.witcher.ui.graphics.GameFonts;
import com.witcher.ui.graphics.UiChrome;
import com.witcher.ui.shop.ShopInventoryKind;
import com.witcher.ui.shop.ShopInventorySlot;
import com.witcher.ui.shop.presenter.ShopSessionState;
import com.witcher.ui.shop.swing.ShopAttentionPulse;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_ACTION_BTN_H;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_ARMOUR_VISIBLE_ROWS;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_DETAIL_W;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_GRID_COLS;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_GRID_GAP;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_H;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_PAD;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_W;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_POUCH_ICON;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_SCROLLBAR_W;
import static com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_SPECIAL_VISIBLE_ROWS;

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
    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.88f));
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, sw, sh);

    int px = (sw - INVENTORY_PANEL_W) / 2;
    int py = Math.max(4, (sh - INVENTORY_PANEL_H) / 2);
    ShopSessionState ui = ctx.ui();
    ui.inventoryPanelBounds.setBounds(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H);

    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
    BufferedImage panelBg = ctx.assets().inventoryPanelBg();
    if (panelBg != null) {
      g.drawImage(panelBg, px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, null);
    } else {
      g.setColor(new Color(18, 12, 8, 255));
      g.fillRoundRect(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 8, 8);
      g.setColor(new Color(150, 110, 50));
      g.drawRoundRect(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H, 8, 8);
    }

    int pad = INVENTORY_PANEL_PAD;
    int innerX = px + pad;
    int innerY = py + pad;
    int innerW = INVENTORY_PANEL_W - pad * 2;
    int innerH = INVENTORY_PANEL_H - pad * 2;
    int innerRight = innerX + innerW;
    int innerBottom = innerY + innerH;

    GameFonts.applyGothicHints(g);
    g.setFont(GameFonts.get().uiBold(13));
    g.setColor(new Color(255, 220, 140));
    g.drawString("Инвентарь", innerX, innerY + 14);

    ui.inventoryCloseBounds.setBounds(
        innerRight - UiChrome.BTN_SIZE,
        innerY,
        UiChrome.BTN_SIZE,
        UiChrome.BTN_SIZE);
    UiChrome.drawCloseButton(g, ui.inventoryCloseBounds, ui.inventoryCloseHovered, 1f);

    int contentTop = innerY + 22;
    int contentBottom = innerBottom - 4;
    int detailX = innerX;
    int detailW = Math.min(INVENTORY_DETAIL_W, innerW / 2 - 4);
    int gap = 10;
    int gridX = detailX + detailW + gap;
    int gridRight = innerRight;
    int gridW = Math.max(1, gridRight - gridX);

    int cell = INVENTORY_POUCH_ICON;
    int cellGap = 6;
    int cols = Math.max(1, Math.min(INVENTORY_GRID_COLS,
        Math.max(1, (gridW - INVENTORY_SCROLLBAR_W - 2 + cellGap) / (cell + cellGap))));

    boolean showToBattle = ctx.presenter().canShowToBattleButton();
    int actionH = INVENTORY_ACTION_BTN_H;
    int battleReserve = showToBattle ? actionH + 8 : 0;

    List<Integer> specialIdx = new ArrayList<>();
    List<Integer> armourIdx = new ArrayList<>();
    for (int i = 0; i < slots.size(); i++) {
      if (slots.get(i).kind().isArmourGrid()) {
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
    if (armourY + armourH > contentBottom - battleReserve) {
      armourH = Math.max(cell, contentBottom - battleReserve - armourY);
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

    int actionY = contentBottom - actionH;
    int detailMaxH = actionY - contentTop - 8;
    int detailBottom = contentTop;
    boolean showActionButton = false;
    String actionLabel = "";
    ShopInventorySlot focused = null;
    if (!slots.isEmpty() && focusedIndex >= 0 && focusedIndex < slots.size()) {
      focused = slots.get(focusedIndex);
      if (focused.kind().hasActionButton()) {
        showActionButton = true;
        actionLabel = focused.actionLabel();
      }
      detailBottom = callbacks.drawDetail(g, focused, detailX, contentTop, detailW, detailMaxH);
    }
        boolean showGearButton = focused != null
            && (focused.kind() == ShopInventoryKind.WEAPON
                || focused.kind() == ShopInventoryKind.ARMOUR
                || focused.kind() == ShopInventoryKind.SET);

    ui.inventoryGearButtonBounds.setBounds(0, 0, 0, 0);
    if (showActionButton || showGearButton) {
      int btnGap = (showActionButton && showGearButton) ? 6 : 0;
      int btnCount = (showActionButton ? 1 : 0) + (showGearButton ? 1 : 0);
      int btnW = btnCount > 1 ? Math.min((detailW - btnGap) / 2, 96) : Math.min(detailW, 120);
      int totalW = btnCount > 1 ? btnW * 2 + btnGap : btnW;
      int rowX = detailX + (detailW - totalW) / 2;
      int equipBtnY = Math.max(actionY, Math.min(contentBottom - actionH, detailBottom + 6));

      int nextX = rowX;
      if (showActionButton) {
        ui.inventoryEquipButtonBounds.setBounds(nextX, equipBtnY, btnW, actionH);
        boolean pulseOpen = focused != null
            && focused.kind() == ShopInventoryKind.BATTLE_CARD
            && ui.battleCardOpenAttention
            && ShopAttentionPulse.hoverPhase(ui.tick);
        boolean pulseDrink = focused != null
            && focused.kind() == ShopInventoryKind.POTION
            && ui.potionDrinkAttention
            && ShopAttentionPulse.hoverPhase(ui.tick);
        drawActionButton(g, actionLabel, nextX, equipBtnY, btnW, actionH, pulseOpen || pulseDrink);
        nextX += btnW + btnGap;
      } else {
        ui.inventoryEquipButtonBounds.setBounds(0, 0, 0, 0);
      }
      if (showGearButton) {
        ui.inventoryGearButtonBounds.setBounds(nextX, equipBtnY, btnW, actionH);
        boolean pulseGear = ui.equipmentAttention && ShopAttentionPulse.hoverPhase(ui.tick);
        drawActionButton(g, "Надеть", nextX, equipBtnY, btnW, actionH, pulseGear);
      }
    } else {
      ui.inventoryEquipButtonBounds.setBounds(0, 0, 0, 0);
    }

    if (showToBattle) {
      int battleW = 72;
      int battleX = innerRight - battleW;
      int battleY = innerBottom - 4 - actionH;
      ui.toBattleButtonBounds.setBounds(battleX, battleY, battleW, actionH);
      boolean pulse = ui.toBattleAttention && ShopAttentionPulse.hoverPhase(ui.tick);
      boolean hovered = ui.toBattleHovered || pulse;
      drawActionButton(g, "В БОЙ", battleX, battleY, battleW, actionH, hovered);
      if (pulse) {
        g.setColor(new Color(255, 220, 120, 80));
        g.fillRoundRect(battleX + 1, battleY + 1, battleW - 2, actionH - 2, 4, 4);
      }
    } else {
      ui.toBattleButtonBounds.setBounds(0, 0, 0, 0);
    }

    g.setComposite(prev);
  }

  private static void drawActionButton(Graphics2D g, String label, int x, int y, int w, int h,
                                       boolean hoverLook) {
    g.setFont(GameFonts.get().uiBold(10));
    g.setColor(hoverLook ? new Color(55, 38, 14, 235) : new Color(28, 18, 8, 220));
    g.fillRoundRect(x, y, w, h, 5, 5);
    g.setColor(hoverLook ? new Color(255, 210, 90) : new Color(170, 125, 55));
    g.drawRoundRect(x, y, w, h, 5, 5);
    g.setColor(hoverLook ? new Color(255, 245, 190) : new Color(255, 225, 150));
    FontMetrics efm = g.getFontMetrics();
    g.drawString(label, x + (w - efm.stringWidth(label)) / 2, y + 16);
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
