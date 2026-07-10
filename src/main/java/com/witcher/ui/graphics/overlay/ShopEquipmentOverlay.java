package main.java.com.witcher.ui.graphics.overlay;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.ShopStatBarRenderer;
import main.java.com.witcher.ui.graphics.UiChrome;
import main.java.com.witcher.ui.shop.EquipmentArmourList;
import main.java.com.witcher.ui.shop.EquipmentFilter;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEquipSlot;
import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Locale;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_BAR_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_GAP;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_ICON;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_GRID_CELL;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_GRID_COLS;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_GRID_ICON;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_LIST_HEADER_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_LIST_W;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_MARGIN;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_STATS_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_STATS_PAD_X;

/**
 * Слой экипировки поверх лавки — вынесен из {@code ShopSwingView}.
 */
public final class ShopEquipmentOverlay {

    @FunctionalInterface
    public interface SpriteCallbacks {
        void drawScaledSprite(Graphics2D g, BufferedImage img, int x, int y, int w, int h, boolean pixelArt);
    }

    private ShopEquipmentOverlay() {
    }

    public static void draw(Graphics2D g, ShopOverlayContext ctx, SpriteCallbacks sprites, int sw, int sh) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        int px = EQUIP_MARGIN;
        int py = EQUIP_MARGIN;
        int panelW = sw - EQUIP_MARGIN * 2;
        int panelH = sh - EQUIP_MARGIN * 2;
        ctx.ui().equipmentPanelBounds.setBounds(px, py, panelW, panelH);
        ctx.ui().equipmentRowBounds.clear();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.97f));
        g.setColor(new Color(14, 10, 6, 248));
        g.fillRoundRect(px, py, panelW, panelH, 6, 6);
        g.setColor(new Color(155, 115, 50));
        g.drawRoundRect(px, py, panelW, panelH, 6, 6);

        int backX = px + 8;
        int backY = py + 8;
        ctx.ui().equipmentBackButtonBounds.setBounds(backX, backY, UiChrome.BTN_SIZE, UiChrome.BTN_SIZE);
        UiChrome.drawArrowBackButton(g, ctx.ui().equipmentBackButtonBounds, ctx.ui().equipmentBackHovered, 1f);

        int filterY = py + 8;
        int filterX = px + UiChrome.BTN_SIZE + 14;
        int filterW = panelW - (filterX - px) - 8;
        drawFilterBar(g, ctx, filterX, filterY, filterW);

        int slotSize = 48;
        int slotGap = 10;
        int slotX = px + panelW - slotSize - 10;
        int slotY = py + 36;

        int contentTop = filterY + EQUIP_FILTER_BAR_H + 6;
        int statsH = EQUIP_STATS_H;
        int statsY = py + panelH - statsH - 6;
        int contentH = statsY - contentTop - 6;

        int listX = px + 8;
        int listY = contentTop;
        int listW = EQUIP_LIST_W;
        int listH = contentH;
        g.setColor(new Color(8, 6, 4, 180));
        g.fillRoundRect(listX, listY, listW, listH, 4, 4);
        g.setColor(new Color(100, 75, 40));
        g.drawRoundRect(listX, listY, listW, listH, 4, 4);

        int sectionY = listY + 6;
        String section = ctx.ui().equipmentFilter == EquipmentFilter.ALL
            ? "БРОНЯ"
            : ctx.ui().equipmentFilter.sectionLabel.toUpperCase(Locale.ROOT);
        ShopOverlayText.drawEquipText(g, GameFonts.get().uiBold(9), section,
            listX + 8, sectionY + 10, new Color(150, 165, 180));

        int itemsTop = sectionY + EQUIP_LIST_HEADER_H + 4;
        g.setColor(new Color(90, 68, 36, 120));
        g.drawLine(listX + 6, itemsTop, listX + listW - 6, itemsTop);

        int gridBottom = listY + listH - 6;

        List<Armour> owned = EquipmentArmourList.filter(
            ctx.presenter().model().ownedArmour(), ctx.ui().equipmentFilter);
        int gridX0 = listX + (listW - EQUIP_GRID_COLS * EQUIP_GRID_CELL) / 2;
        int gridY0 = itemsTop + 8;
        Armour tooltipArmour = null;
        int tooltipAnchorY = gridY0;

        for (int i = 0; i < owned.size(); i++) {
            int col = i % EQUIP_GRID_COLS;
            int row = i / EQUIP_GRID_COLS;
            int cellX = gridX0 + col * EQUIP_GRID_CELL;
            int cellY = gridY0 + row * EQUIP_GRID_CELL;
            if (cellY + EQUIP_GRID_CELL > gridBottom) {
                break;
            }

            Armour armour = owned.get(i);
            Rectangle cell = new Rectangle(cellX, cellY, EQUIP_GRID_CELL, EQUIP_GRID_CELL);
            ctx.ui().equipmentRowBounds.add(cell);
            boolean hovered = i == ctx.ui().equipmentHoveredRow;
            boolean equipped = ctx.presenter().model().isEquipped(armour);

            if (hovered || equipped) {
                g.setColor(equipped ? new Color(48, 62, 82, 220) : new Color(36, 48, 68, 190));
                g.fillRoundRect(cell.x, cell.y, cell.width, cell.height, 3, 3);
            }
            if (hovered) {
                g.setColor(new Color(88, 148, 210, 200));
                g.drawRoundRect(cell.x, cell.y, cell.width, cell.height, 3, 3);
            } else if (equipped) {
                g.setColor(new Color(200, 175, 90, 180));
                g.drawRoundRect(cell.x, cell.y, cell.width, cell.height, 3, 3);
            } else {
                g.setColor(new Color(55, 48, 38, 160));
                g.drawRoundRect(cell.x, cell.y, cell.width, cell.height, 2, 2);
            }

            ShopCategory cat = EquipmentArmourList.categoryFor(armour);
            BufferedImage itemIcon = ctx.armourIcons().iconForArmour(armour, cat, EQUIP_GRID_ICON);
            int iconX = cellX + (EQUIP_GRID_CELL - EQUIP_GRID_ICON) / 2;
            int iconY = cellY + (EQUIP_GRID_CELL - EQUIP_GRID_ICON) / 2;
            if (itemIcon != null) {
                Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(itemIcon, iconX, iconY, EQUIP_GRID_ICON, EQUIP_GRID_ICON, null);
                if (interp != null) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                }
            }
            if (hovered) {
                tooltipArmour = armour;
                tooltipAnchorY = cell.y;
            }
        }
        if (owned.isEmpty()) {
            ShopOverlayText.drawEquipText(g, GameFonts.get().uiPlain(9), "Пусто",
                listX + 10, gridY0 + 12, new Color(130, 145, 165));
        }

        int portraitX = listX + listW + 8;
        int portraitY = contentTop;
        int portraitW = slotX - portraitX - 8;
        int portraitH = contentH;
        g.setColor(new Color(6, 4, 2, 160));
        g.fillRoundRect(portraitX - 4, portraitY - 4, portraitW + 8, portraitH + 8, 6, 6);
        drawPortraitFit(g, sprites, ctx.assets().geraltPortraitShop(), portraitX, portraitY, portraitW, portraitH);

        ShopEquipSlot[] slots = ShopEquipSlot.values();
        for (int i = 0; i < slots.length; i++) {
            ShopEquipSlot slot = slots[i];
            int sy = slotY + i * (slotSize + slotGap);
            ctx.ui().equipmentSlotBounds[i] = new Rectangle(slotX, sy, slotSize, slotSize);
            boolean hovered = ctx.ui().equipmentHoveredSlot == i;
            Armour equipped = ctx.presenter().model().getEquipped(slot);
            g.setColor(new Color(22, 14, 8, 220));
            g.fillRoundRect(slotX, sy, slotSize, slotSize, 4, 4);
            g.setColor(hovered ? new Color(200, 160, 70) : new Color(120, 90, 45));
            g.drawRoundRect(slotX, sy, slotSize, slotSize, 4, 4);
            BufferedImage icon = ctx.assets().equipSlotPlaceholder(slot.iconIndex);
            if (equipped != null) {
                ShopCategory slotCategory = switch (slot) {
                    case CHEST -> ShopCategory.CHEST;
                    case LEGS -> ShopCategory.LEGS;
                    case GLOVES -> ShopCategory.GLOVES;
                    case BOOTS -> ShopCategory.BOOTS;
                };
                BufferedImage armourArt = ctx.armourIcons().iconForArmour(equipped, slotCategory, 30);
                if (armourArt != null) {
                    icon = armourArt;
                }
            }
            if (equipped != null && icon != null) {
                int iconSz = 30;
                g.drawImage(icon, slotX + (slotSize - iconSz) / 2, sy + 7, iconSz, iconSz, null);
            } else if (icon != null) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f));
                int iconSz = 26;
                g.drawImage(icon, slotX + (slotSize - iconSz) / 2, sy + 9, iconSz, iconSz, null);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.97f));
            }
            String slotLabel = slot.label;
            Font slotFont = GameFonts.get().uiPlain(9);
            FontMetrics sfm = g.getFontMetrics(slotFont);
            ShopOverlayText.drawEquipText(g, slotFont, slotLabel,
                slotX + (slotSize - sfm.stringWidth(slotLabel)) / 2, sy + slotSize - 5,
                new Color(170, 140, 90));
        }

        int statsX = px + EQUIP_STATS_PAD_X;
        int statsW = panelW - EQUIP_STATS_PAD_X * 2;
        g.setColor(new Color(90, 68, 36, 100));
        g.drawLine(statsX, statsY - 2, statsX + statsW, statsY - 2);
        drawEquipmentStats(g, ctx, statsX, statsY, statsW, statsH,
            ctx.presenter().model().equippedStatPreview());

        if (tooltipArmour == null && ctx.ui().equipmentHoveredSlot >= 0) {
            ShopEquipSlot slot = ShopEquipSlot.values()[ctx.ui().equipmentHoveredSlot];
            tooltipArmour = ctx.presenter().model().getEquipped(slot);
            if (tooltipArmour != null && ctx.ui().equipmentSlotBounds[ctx.ui().equipmentHoveredSlot] != null) {
                tooltipAnchorY = ctx.ui().equipmentSlotBounds[ctx.ui().equipmentHoveredSlot].y;
            }
        }

        if (tooltipArmour != null) {
            int tipW = ShopEquipmentTooltipRenderer.preferredWidth();
            int tipX = portraitX + 6;
            int tipY = Math.max(portraitY + 4, Math.min(tooltipAnchorY, portraitY + portraitH - 100));
            if (tipX + tipW > slotX - 6) {
                tipX = Math.max(listX + listW + 4, slotX - tipW - 6);
            }
            ShopEquipmentTooltipRenderer.draw(g, tipX, tipY, tipW, tooltipArmour, ctx.presenter().model());
        }

        g.setComposite(prev);
    }

    private static void drawPortraitFit(Graphics2D g, SpriteCallbacks sprites, BufferedImage img,
                                        int x, int y, int w, int h) {
        if (img == null || sprites == null || w <= 0 || h <= 0) {
            return;
        }
        int iw = img.getWidth();
        int ih = img.getHeight();
        if (iw <= 0 || ih <= 0) {
            return;
        }
        float scale = Math.min(w / (float) iw, h / (float) ih);
        int dw = Math.max(1, Math.round(iw * scale));
        int dh = Math.max(1, Math.round(ih * scale));
        int dx = x + (w - dw) / 2;
        int dy = y + (h - dh) / 2;
        sprites.drawScaledSprite(g, img, dx, dy, dw, dh, true);
    }

    private static void drawFilterBar(Graphics2D g, ShopOverlayContext ctx, int listX, int listY, int listW) {
        EquipmentFilter[] filters = EquipmentFilter.values();
        int totalW = filters.length * EQUIP_FILTER_ICON + (filters.length - 1) * EQUIP_FILTER_GAP;
        int startX = listX + (listW - totalW) / 2;
        int iconY = listY + 6;

        for (int i = 0; i < filters.length; i++) {
            EquipmentFilter filter = filters[i];
            int fx = startX + i * (EQUIP_FILTER_ICON + EQUIP_FILTER_GAP);
            Rectangle bounds = new Rectangle(fx - 2, iconY - 2, EQUIP_FILTER_ICON + 4, EQUIP_FILTER_ICON + 4);
            ctx.ui().equipmentFilterBounds[i] = bounds;

            boolean active = ctx.ui().equipmentFilter == filter;
            boolean hovered = i == ctx.ui().equipmentHoveredFilter;
            if (active) {
                g.setColor(new Color(210, 215, 225, 90));
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 3, 3);
            } else if (hovered) {
                g.setColor(new Color(120, 150, 190, 70));
                g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 3, 3);
            }

            BufferedImage icon = ctx.assets().equipmentFilterIcon(filter);
            if (icon != null) {
                Composite composite = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, active ? 1f : 0.72f));
                Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(icon, fx, iconY, EQUIP_FILTER_ICON, EQUIP_FILTER_ICON, null);
                if (interp != null) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                }
                g.setComposite(composite);
            }
            if (active) {
                g.setColor(new Color(200, 220, 245));
                g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 3, 3);
            }
        }
    }

    private static void drawEquipmentStats(Graphics2D g, ShopOverlayContext ctx, int x, int y, int w, int h,
                                           ShopModel.StatPreview preview) {
        ShopStatBarRenderer.drawEquipmentStrip(g, x, y, w, h, preview, ctx.ui().tick);
    }
}
