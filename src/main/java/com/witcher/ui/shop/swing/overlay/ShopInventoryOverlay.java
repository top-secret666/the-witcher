package main.java.com.witcher.ui.shop.swing.overlay;

import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.UiChrome;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.List;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_PANEL_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.INVENTORY_POUCH_ICON;

/**
 * Слой инвентаря поверх лавки — вынесен из {@code ShopSwingView}.
 */
public final class ShopInventoryOverlay {

    public interface PouchCallbacks {
        void drawIcon(Graphics2D g, int x, int y, int size, boolean focused, boolean hovered);

        int drawDetail(Graphics2D g, int x, int y, int maxW);
    }

    private ShopInventoryOverlay() {
    }

    public static void draw(Graphics2D g, ShopOverlayContext ctx, PouchCallbacks pouch,
                            int sw, int sh) {
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

        int iconX = px + 12;
        int iconY = py + 34;
        ctx.ui().inventoryPouchIconBounds.setBounds(iconX, iconY, INVENTORY_POUCH_ICON, INVENTORY_POUCH_ICON);
        pouch.drawIcon(g, iconX, iconY, INVENTORY_POUCH_ICON,
            ctx.ui().inventoryPouchFocused, ctx.ui().inventoryPouchIconHovered);

        int detailX = px + 56;
        int detailY = py + 28;
        int detailW = INVENTORY_PANEL_W - 68;
        int detailBottom = iconY + INVENTORY_POUCH_ICON;
        if (ctx.ui().inventoryPouchFocused) {
            detailBottom = pouch.drawDetail(g, detailX, detailY, detailW);
        }

        int listY = detailBottom + 12;
        g.setColor(new Color(100, 75, 40, 140));
        g.drawLine(px + 10, listY - 4, px + INVENTORY_PANEL_W - 10, listY - 4);
        g.setFont(GameFonts.get().uiBold(10));
        g.setColor(new Color(180, 140, 80));
        g.drawString("Куплено:", px + 12, listY + 10);
        listY += 24;

        g.setFont(GameFonts.get().uiPlain(11));
        g.setColor(new Color(200, 180, 130));
        List<String> items = ctx.presenter().model().inventoryItemNames();
        if (ctx.ui().showcaseItems.isEmpty()) {
            g.drawString("Пока пусто…", px + 12, listY);
        } else {
            FontMetrics fm = g.getFontMetrics();
            for (String name : items) {
                if (listY > py + INVENTORY_PANEL_H - 20) {
                    g.drawString("…", px + 12, listY);
                    break;
                }
                g.drawString("• " + ShopOverlayText.truncateToWidth(name, fm, INVENTORY_PANEL_W - 24),
                    px + 12, listY);
                listY += 14;
            }
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
        g.drawString(equipLabel, equipBtnX + (equipBtnW - efm.stringWidth(equipLabel)) / 2,
            equipBtnY + 15);

        g.setComposite(prev);
    }
}
