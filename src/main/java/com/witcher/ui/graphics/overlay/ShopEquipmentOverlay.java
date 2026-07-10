package main.java.com.witcher.ui.graphics.overlay;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.ShopAssetCache;
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
import java.awt.image.BufferedImage;
import java.awt.RenderingHints;
import java.util.List;
import java.util.Locale;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_BAR_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_GAP;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_ICON;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_ITEM_ICON;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_ITEM_ROW_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_LIST_HEADER_H;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_MARGIN;

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

        ShopOverlayText.drawEquipText(g, GameFonts.get().uiBold(15), "Экипировка",
            px + 14 + UiChrome.BTN_SIZE + 6, py + 24, new Color(255, 220, 140));

        int backX = px + 10;
        int backY = py + 10;
        ctx.ui().equipmentBackButtonBounds.setBounds(backX, backY, UiChrome.BTN_SIZE, UiChrome.BTN_SIZE);
        UiChrome.drawArrowBackButton(g, ctx.ui().equipmentBackButtonBounds, ctx.ui().equipmentBackHovered, 1f);

        int listX = px + 10;
        int listY = py + 34;
        int listW = 168;
        int listH = panelH - 48;
        g.setColor(new Color(8, 6, 4, 180));
        g.fillRoundRect(listX, listY, listW, listH, 4, 4);
        g.setColor(new Color(100, 75, 40));
        g.drawRoundRect(listX, listY, listW, listH, 4, 4);

        drawFilterBar(g, ctx, listX, listY, listW);

        int sectionY = listY + EQUIP_FILTER_BAR_H + 6;
        String section = ctx.ui().equipmentFilter == EquipmentFilter.ALL
            ? "БРОНЯ"
            : ctx.ui().equipmentFilter.sectionLabel.toUpperCase(Locale.ROOT);
        ShopOverlayText.drawEquipText(g, GameFonts.get().uiBold(9), section,
            listX + 8, sectionY + 10, new Color(150, 165, 180));

        int itemsTop = sectionY + EQUIP_LIST_HEADER_H + 4;
        g.setColor(new Color(90, 68, 36, 120));
        g.drawLine(listX + 6, itemsTop, listX + listW - 6, itemsTop);

        List<Armour> owned = EquipmentArmourList.filter(
            ctx.presenter().model().ownedArmour(), ctx.ui().equipmentFilter);
        int rowY = itemsTop + 12;
        Font itemFont = GameFonts.get().uiPlain(10);
        FontMetrics itemFm = g.getFontMetrics(itemFont);
        int textX = listX + 8 + EQUIP_ITEM_ICON + 6;
        int textW = listW - 8 - EQUIP_ITEM_ICON - 10;
        Armour tooltipArmour = null;
        int tooltipAnchorY = rowY;

        for (int i = 0; i < owned.size(); i++) {
            if (rowY + 6 > listY + listH - 6) {
                break;
            }
            Armour armour = owned.get(i);
            Rectangle row = new Rectangle(listX + 4, rowY - EQUIP_ITEM_ROW_H + 8, listW - 8, EQUIP_ITEM_ROW_H);
            ctx.ui().equipmentRowBounds.add(row);
            boolean hovered = i == ctx.ui().equipmentHoveredRow;
            boolean equipped = ctx.presenter().model().isEquipped(armour);
            if (hovered || equipped) {
                g.setColor(equipped ? new Color(48, 62, 82, 210) : new Color(36, 48, 68, 190));
                g.fillRoundRect(row.x, row.y, row.width, row.height, 3, 3);
                if (hovered) {
                    g.setColor(new Color(88, 148, 210, 180));
                    g.drawRoundRect(row.x, row.y, row.width, row.height, 3, 3);
                }
            }
            ShopCategory cat = EquipmentArmourList.categoryFor(armour);
            BufferedImage itemIcon = ctx.armourIcons().iconForArmour(armour, cat, EQUIP_ITEM_ICON);
            int iconX = listX + 8;
            int iconY = rowY - EQUIP_ITEM_ICON + 6;
            g.setColor(new Color(12, 10, 8, 200));
            g.fillRoundRect(iconX - 1, iconY - 1, EQUIP_ITEM_ICON + 2, EQUIP_ITEM_ICON + 2, 2, 2);
            if (itemIcon != null) {
                Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(itemIcon, iconX, iconY, EQUIP_ITEM_ICON, EQUIP_ITEM_ICON, null);
                if (interp != null) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                }
            }
            String line = ShopOverlayText.truncateToWidth(armour.getName(), itemFm, textW);
            ShopOverlayText.drawEquipText(g, itemFont, line, textX, rowY,
                equipped ? new Color(200, 220, 255) : (hovered ? new Color(230, 235, 245) : new Color(185, 195, 210)));
            if (hovered) {
                tooltipArmour = armour;
                tooltipAnchorY = row.y;
            }
            rowY += EQUIP_ITEM_ROW_H;
        }
        if (owned.isEmpty()) {
            String empty = ctx.ui().equipmentFilter == EquipmentFilter.ALL
                ? "Пока нет брони…"
                : "Нет предметов в категории";
            ShopOverlayText.drawEquipText(g, GameFonts.get().uiPlain(10), empty,
                listX + 8, rowY, new Color(130, 145, 165));
        }

        int portraitX = listX + listW + 12;
        int portraitY = py + 30;
        int portraitW = 152;
        int statsH = 132;
        int statsY = py + panelH - statsH - 8;
        int portraitH = statsY - portraitY - 8;
        g.setColor(new Color(6, 4, 2, 160));
        g.fillRoundRect(portraitX - 4, portraitY - 4, portraitW + 8, portraitH + 8, 6, 6);
        if (ctx.assets().geraltPortraitShop() != null) {
            sprites.drawScaledSprite(g, ctx.assets().geraltPortraitShop(), portraitX, portraitY, portraitW, portraitH, true);
        }

        int slotSize = 48;
        int slotGap = 10;
        int slotX = px + panelW - slotSize - 12;
        int slotY = py + 36;
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

        int statsX = portraitX;
        int statsW = slotX + slotSize - statsX;
        g.setColor(new Color(10, 7, 4, 200));
        g.fillRoundRect(statsX, statsY, statsW, statsH, 4, 4);
        g.setColor(new Color(100, 75, 40));
        g.drawRoundRect(statsX, statsY, statsW, statsH, 4, 4);
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
            int tipX = listX + listW + 6;
            if (tipX + tipW > portraitX - 4) {
                tipX = Math.max(listX + 4, portraitX - tipW - 4);
            }
            int tipY = Math.max(listY + 8, Math.min(tooltipAnchorY, statsY - 100));
            ShopEquipmentTooltipRenderer.draw(g, tipX, tipY, tipW, tooltipArmour, ctx.presenter().model());
        }

        g.setComposite(prev);
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
                Composite saved = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, active ? 1f : 0.72f));
                Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(icon, fx, iconY, EQUIP_FILTER_ICON, EQUIP_FILTER_ICON, null);
                if (interp != null) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                }
                g.setComposite(saved);
            }
            if (active) {
                g.setColor(new Color(200, 220, 245));
                g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 3, 3);
            }
        }
    }

    private static void drawEquipmentStats(Graphics2D g, ShopOverlayContext ctx, int x, int y, int w, int h,
                                           ShopModel.StatPreview preview) {
        ShopAssetCache assets = ctx.assets();
        ShopStatBarRenderer.draw(g, x, y, w, h, preview,
            assets.statVialEmpty(), assets.statVialOverlay(), assets.statVialEndCap(), ctx.ui().tick);
    }
}
