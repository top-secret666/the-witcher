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

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_POUCH_ICON;

/**
 * Слой инвентаря поверх лавки — иконки (кошелёк, карта, зелья, оружие).
 */
public final class ShopInventoryOverlay {

  public interface SlotCallbacks {
    void drawIcon(Graphics2D g, ShopInventorySlot slot, int x, int y, int size,
                  boolean focused, boolean hovered);

    int drawDetail(Graphics2D g, ShopInventorySlot slot, int x, int y, int maxW);
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
    int py = (sh - INVENTORY_PANEL_H) / 2 - 16;
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

    slotBoundsOut.clear();
    int iconX = px + 12;
    int iconY = py + 34;
    int gap = 6;
    for (int i = 0; i < slots.size(); i++) {
      Rectangle bounds = new Rectangle(iconX, iconY, INVENTORY_POUCH_ICON, INVENTORY_POUCH_ICON);
      slotBoundsOut.add(bounds);
      boolean focused = i == focusedIndex;
      boolean hovered = i == hoveredIndex;
      callbacks.drawIcon(g, slots.get(i), iconX, iconY, INVENTORY_POUCH_ICON, focused, hovered);
      iconX += INVENTORY_POUCH_ICON + gap;
      if (iconX + INVENTORY_POUCH_ICON > px + INVENTORY_PANEL_W - 12) {
        iconX = px + 12;
        iconY += INVENTORY_POUCH_ICON + gap;
      }
    }

    int detailX = px + 12;
    int detailY = iconY + INVENTORY_POUCH_ICON + 10;
    int detailW = INVENTORY_PANEL_W - 24;
    int detailBottom = detailY;
    if (!slots.isEmpty() && focusedIndex >= 0 && focusedIndex < slots.size()) {
      detailBottom = callbacks.drawDetail(g, slots.get(focusedIndex), detailX, detailY, detailW);
    }

    int equipBtnW = 108;
    int equipBtnH = 22;
    int equipBtnX = px + INVENTORY_PANEL_W - equipBtnW - 10;
    int equipBtnY = py + INVENTORY_PANEL_H - equipBtnH - 8;
    ctx.ui().inventoryEquipButtonBounds.setBounds(equipBtnX, equipBtnY, equipBtnW, equipBtnH);
    g.setFont(GameFonts.get().uiBold(10));
    g.setColor(new Color(28, 18, 8, 220));
    g.fillRoundRect(equipBtnX, equipBtnY, equipBtnW, equipBtnH, 5, 5);
    g.setColor(new Color(170, 125, 55));
    g.drawRoundRect(equipBtnX, equipBtnY, equipBtnW, equipBtnH, 5, 5);
    g.setColor(new Color(255, 225, 150));
    String equipLabel = "Экипировка";
    FontMetrics efm = g.getFontMetrics();
    g.drawString(equipLabel, equipBtnX + (equipBtnW - efm.stringWidth(equipLabel)) / 2, equipBtnY + 15);

    g.setComposite(prev);
  }
}
