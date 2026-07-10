package main.java.com.witcher.ui.shop.swing.overlay;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.shop.swing.ShopAssetCache;
import main.java.com.witcher.ui.shop.swing.ShopStatBarRenderer;
import main.java.com.witcher.ui.graphics.UiChrome;
import main.java.com.witcher.ui.shop.EquipmentArmourList;
import main.java.com.witcher.ui.shop.EquipmentFilter;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEquipSlot;
import main.java.com.witcher.ui.shop.ShopModel;

import main.java.com.witcher.ui.shop.view.EquipmentOverlayLayout;

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

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_FILTER_ICON;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_GRID_CELL;
import static main.java.com.witcher.ui.shop.view.ShopViewConstants.EQUIP_GRID_ICON;

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

        EquipmentOverlayLayout layout = EquipmentOverlayLayout.compute(sw, sh);
        int px = layout.panel.x;
        int py = layout.panel.y;
        int panelW = layout.panel.width;
        int panelH = layout.panel.height;
        ctx.ui().equipmentPanelBounds.setBounds(layout.panel);
        ctx.ui().equipmentRowBounds.clear();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.97f));
        g.setColor(new Color(14, 10, 6, 248));
        g.fillRoundRect(px, py, panelW, panelH, 6, 6);
        g.setColor(new Color(155, 115, 50));
        g.drawRoundRect(px, py, panelW, panelH, 6, 6);

        ctx.ui().equipmentBackButtonBounds.setBounds(layout.backButton);
        UiChrome.drawArrowBackButton(g, ctx.ui().equipmentBackButtonBounds, ctx.ui().equipmentBackHovered, 1f);

        g.setColor(new Color(8, 6, 4, 180));
        g.fillRoundRect(layout.listX, layout.listY, layout.listW, layout.leftPanelH, 4, 4);
        g.setColor(new Color(100, 75, 40));
        g.drawRoundRect(layout.listX, layout.listY, layout.listW, layout.leftPanelH, 4, 4);

        drawFilterButtons(g, ctx, layout);

        Armour tooltipArmour = null;
        int tooltipAnchorY = layout.gridY0;

        List<Armour> owned = EquipmentArmourList.filter(
            ctx.presenter().model().ownedArmour(), ctx.ui().equipmentFilter);

        for (int i = 0; i < owned.size(); i++) {
            if (!layout.gridCellFits(i)) {
                break;
            }

            Armour armour = owned.get(i);
            Rectangle cell = layout.gridCell(i);
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
            int iconX = cell.x + (EQUIP_GRID_CELL - EQUIP_GRID_ICON) / 2;
            int iconY = cell.y + (EQUIP_GRID_CELL - EQUIP_GRID_ICON) / 2;
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
                layout.listX + 10, layout.gridY0 + 12, new Color(130, 145, 165));
        }

        drawEquipmentStats(g, ctx, layout.statsX, layout.statsY, layout.statsW, layout.statsH,
            ctx.presenter().model().equippedStatPreview());

        drawPortraitFit(g, sprites, ctx.assets().geraltPortraitShop(),
            layout.portraitX, layout.portraitY, layout.portraitW, layout.portraitH);

        ShopEquipSlot[] slots = layout.equipSlots();
        for (int i = 0; i < slots.length; i++) {
            ShopEquipSlot slot = slots[i];
            Rectangle slotBounds = layout.equipSlot(i);
            int slotX = slotBounds.x;
            int sy = slotBounds.y;
            int slotSize = slotBounds.width;
            ctx.ui().equipmentSlotBounds[i] = slotBounds;
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
            Font slotFont = GameFonts.get().uiPlain(8);
            FontMetrics sfm = g.getFontMetrics(slotFont);
            ShopOverlayText.drawEquipText(g, slotFont, slotLabel,
                slotX + (slotSize - sfm.stringWidth(slotLabel)) / 2, sy + slotSize - 4,
                new Color(170, 140, 90));
        }

        if (tooltipArmour == null && ctx.ui().equipmentHoveredSlot >= 0) {
            ShopEquipSlot slot = ShopEquipSlot.values()[ctx.ui().equipmentHoveredSlot];
            tooltipArmour = ctx.presenter().model().getEquipped(slot);
            if (tooltipArmour != null && ctx.ui().equipmentSlotBounds[ctx.ui().equipmentHoveredSlot] != null) {
                tooltipAnchorY = ctx.ui().equipmentSlotBounds[ctx.ui().equipmentHoveredSlot].y;
            }
        }

        if (tooltipArmour != null) {
            int tipW = ShopEquipmentTooltipRenderer.preferredWidth();
            int tipX = layout.portraitX + (layout.portraitW - tipW) / 2;
            int tipY = Math.max(layout.portraitY + 4,
                Math.min(tooltipAnchorY, layout.portraitY + layout.portraitH - 110));
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
        g.setColor(new Color(6, 4, 2, 140));
        g.fillRoundRect(x - 2, y - 2, w + 4, h + 4, 6, 6);

        float scale = Math.min(w / (float) iw, h / (float) ih);
        int dw = Math.max(1, Math.round(iw * scale));
        int dh = Math.max(1, Math.round(ih * scale));
        int dx = x + (w - dw) / 2;
        int dy = y + (h - dh) / 2;
        sprites.drawScaledSprite(g, img, dx, dy, dw, dh, true);
    }

    private static void drawFilterButtons(Graphics2D g, ShopOverlayContext ctx,
                                          EquipmentOverlayLayout layout) {
        EquipmentFilter[] filters = EquipmentFilter.values();
        int iconY = layout.filterY + 2;

        for (int i = 0; i < filters.length; i++) {
            EquipmentFilter filter = filters[i];
            Rectangle bounds = layout.filterButton(i, layout.listX, layout.filterY, layout.listW);
            int fx = layout.filterIconX(i, layout.listX, layout.listW);
            ctx.ui().equipmentFilterBounds[i] = bounds;

            boolean active = ctx.ui().equipmentFilter == filter;
            boolean hovered = i == ctx.ui().equipmentHoveredFilter;

            g.setColor(new Color(22, 14, 8, 220));
            g.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 3, 3);
            if (active) {
                g.setColor(new Color(210, 215, 225, 100));
                g.fillRoundRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2, 2, 2);
            } else if (hovered) {
                g.setColor(new Color(120, 150, 190, 80));
                g.fillRoundRect(bounds.x + 1, bounds.y + 1, bounds.width - 2, bounds.height - 2, 2, 2);
            }
            g.setColor(active ? new Color(200, 220, 245) : new Color(120, 90, 45));
            g.drawRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 3, 3);

            BufferedImage icon = ctx.assets().equipmentFilterIcon(filter);
            if (icon != null) {
                Composite composite = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, active ? 1f : 0.75f));
                Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.drawImage(icon, fx, iconY, EQUIP_FILTER_ICON, EQUIP_FILTER_ICON, null);
                if (interp != null) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                }
                g.setComposite(composite);
            }
        }
    }

    private static void drawEquipmentStats(Graphics2D g, ShopOverlayContext ctx, int x, int y, int w, int h,
                                           ShopModel.StatPreview preview) {
        ShopAssetCache assets = ctx.assets();
        ShopStatBarRenderer.drawEquipmentCompact(g, x, y, w, h, preview,
            assets.statVialEmpty(), assets.statVialOverlay(), assets.statVialEndCap(), ctx.ui().tick);
    }
}
