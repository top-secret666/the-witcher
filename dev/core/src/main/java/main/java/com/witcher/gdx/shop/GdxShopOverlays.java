package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.SwingCoords;
import main.java.com.witcher.ui.shop.ShopIcon;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.shop.EquipSlot;
import main.java.com.witcher.ui.shop.ShopEntryIcons;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.presenter.ShopPresenter;
import main.java.com.witcher.ui.shop.presenter.ShopSessionState;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.*;

/**
 * Оверлеи инвентаря и экипировки — порт Swing {@code ShopSwingView}.
 * Координаты bounds — Swing (как в presenter); рисование через {@link SwingCoords}.
 */
public final class GdxShopOverlays {

    private static final int CHROME_BTN = 18;
    private static final Color PANEL_BG = new Color(18f / 255f, 12f / 255f, 8f / 255f, 0.96f);
    private static final Color PANEL_BORDER = new Color(150f / 255f, 110f / 255f, 50f / 255f, 1f);
    private static final Color TITLE = new Color(255f / 255f, 220f / 255f, 140f / 255f, 1f);
    private static final Color BODY = new Color(200f / 255f, 180f / 255f, 130f / 255f, 1f);
    private static final Color MUTED = new Color(180f / 255f, 140f / 255f, 80f / 255f, 1f);

    private GdxShopOverlays() {
    }

    public static void drawInventory(SpriteBatch batch, ShapeRenderer shapes, GameFonts fonts,
                                     ShopPresenter presenter, GdxShopRuntimeAssets assets,
                                     SwingCoords c, float viewW, float viewH) {
        ShopSessionState ui = presenter.ui();
        GlyphLayout glyph = new GlyphLayout();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(0f, 0f, viewW, viewH);
        shapes.end();

        int px = (int) ((viewW - INVENTORY_PANEL_W) / 2f);
        int py = (int) (viewH / 2f - INVENTORY_PANEL_H / 2f - 16);
        ui.inventoryPanelBounds.setBounds(px, py, INVENTORY_PANEL_W, INVENTORY_PANEL_H);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(PANEL_BG);
        shapes.rect(px, c.rectY(py, INVENTORY_PANEL_H), INVENTORY_PANEL_W, INVENTORY_PANEL_H);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(PANEL_BORDER);
        shapes.rect(px, c.rectY(py, INVENTORY_PANEL_H), INVENTORY_PANEL_W, INVENTORY_PANEL_H);
        shapes.end();

        BitmapFont titleFont = fonts.ui;
        titleFont.setColor(TITLE);
        titleFont.draw(batch, "Инвентарь", px + 12, c.textBaseline(py + 20));

        ui.inventoryCloseBounds.setBounds(closeButtonRect(px, py, INVENTORY_PANEL_W));
        drawChromeButton(batch, shapes, ui.inventoryCloseBounds, ui.inventoryCloseHovered, true, c);

        int iconX = px + 12;
        int iconY = py + 34;
        ui.inventoryPouchIconBounds.setBounds(iconX, iconY, INVENTORY_POUCH_ICON, INVENTORY_POUCH_ICON);
        drawPouchIcon(batch, shapes, assets, iconX, iconY, INVENTORY_POUCH_ICON,
            ui.inventoryPouchFocused, ui.inventoryPouchIconHovered, c);

        int listY = iconY + INVENTORY_POUCH_ICON + 12;
        BitmapFont small = fonts.uiSmall;
        small.setColor(MUTED);
        small.draw(batch, "Куплено:", px + 12, c.textBaseline(listY + 10));
        listY += 24;

        small.setColor(BODY);
        List<String> items = presenter.model().inventoryItemNames();
        if (items.isEmpty()) {
            small.draw(batch, "Пока пусто…", px + 12, c.textBaseline(listY));
        } else {
            for (String name : items) {
                if (listY > py + INVENTORY_PANEL_H - 20) {
                    small.draw(batch, "…", px + 12, c.textBaseline(listY));
                    break;
                }
                String line = truncate(small, glyph, "• " + name, INVENTORY_PANEL_W - 24);
                small.draw(batch, line, px + 12, c.textBaseline(listY));
                listY += 14;
            }
        }

        int equipBtnW = 108;
        int equipBtnH = 22;
        int equipBtnX = px + INVENTORY_PANEL_W - equipBtnW - 10;
        int equipBtnY = py + INVENTORY_PANEL_H - equipBtnH - 8;
        ui.inventoryEquipButtonBounds.setBounds(equipBtnX, equipBtnY, equipBtnW, equipBtnH);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(28f / 255f, 18f / 255f, 8f / 255f, 0.86f);
        shapes.rect(equipBtnX, c.rectY(equipBtnY, equipBtnH), equipBtnW, equipBtnH);
        shapes.end();
        small.setColor(new Color(255f / 255f, 225f / 255f, 150f / 255f, 1f));
        small.draw(batch, "Экипировка", equipBtnX + 24, c.textBaseline(equipBtnY + 15));
    }

    public static void drawEquipment(SpriteBatch batch, ShapeRenderer shapes, GameFonts fonts,
                                      ShopPresenter presenter, GdxShopRuntimeAssets assets,
                                      ShopEntryIcons armourIcons, SwingCoords c,
                                      float viewW, float viewH) {
        ShopSessionState ui = presenter.ui();
        GlyphLayout glyph = new GlyphLayout();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(0f, 0f, viewW, viewH);
        shapes.end();

        int px = EQUIP_MARGIN;
        int py = EQUIP_MARGIN;
        int panelW = (int) viewW - EQUIP_MARGIN * 2;
        int panelH = (int) viewH - EQUIP_MARGIN * 2;
        ui.equipmentPanelBounds.setBounds(px, py, panelW, panelH);
        ui.equipmentRowBounds.clear();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(14f / 255f, 10f / 255f, 6f / 255f, 0.97f);
        shapes.rect(px, c.rectY(py, panelH), panelW, panelH);
        shapes.end();

        BitmapFont titleFont = fonts.ui;
        titleFont.setColor(TITLE);
        titleFont.draw(batch, "Экипировка", px + 38, c.textBaseline(py + 24));

        int backX = px + 10;
        int backY = py + 10;
        ui.equipmentBackButtonBounds.setBounds(backX, backY, CHROME_BTN, CHROME_BTN);
        drawChromeButton(batch, shapes, ui.equipmentBackButtonBounds, ui.equipmentBackHovered, false, c);

        int listX = px + 10;
        int listY = py + 34;
        int listW = 168;
        int listH = panelH - 48;
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(8f / 255f, 6f / 255f, 4f / 255f, 0.7f);
        shapes.rect(listX, c.rectY(listY, listH), listW, listH);
        shapes.end();

        BitmapFont small = fonts.uiSmall;
        small.setColor(MUTED);
        small.draw(batch, "Куплено", listX + 8, c.textBaseline(listY + 14));

        List<Armour> owned = presenter.model().ownedArmour();
        int rowH = 18;
        int rowY = listY + 36;
        for (int i = 0; i < owned.size(); i++) {
            if (rowY + 4 > listY + listH - 6) {
                break;
            }
            Armour armour = owned.get(i);
            Rectangle row = new Rectangle(listX + 4, rowY - 13, listW - 8, rowH);
            ui.equipmentRowBounds.add(row);
            boolean hovered = i == ui.equipmentHoveredRow;
            boolean equipped = presenter.model().isEquipped(armour);
            if (hovered || equipped) {
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(equipped ? 70f / 255f : 50f / 255f,
                    equipped ? 52f / 255f : 38f / 255f, equipped ? 24f / 255f : 18f / 255f, 0.78f);
                shapes.rect(row.x, c.rectY(row.y, row.height), row.width, row.height);
                shapes.end();
            }
            String line = truncate(small, glyph, armour.getName(), listW - 20);
            small.setColor(equipped ? TITLE : BODY);
            small.draw(batch, line, listX + 8, c.textBaseline(rowY));
            rowY += rowH;
        }
        if (owned.isEmpty()) {
            small.setColor(MUTED);
            small.draw(batch, "Пока нет брони…", listX + 8, c.textBaseline(rowY));
        }

        int portraitX = listX + listW + 12;
        int portraitY = py + 30;
        int portraitW = 152;
        int portraitH = panelH - 86;
        if (assets.geraltScaled != null) {
            float ph = portraitH;
            float pw = portraitW;
            batch.draw(assets.geraltScaled, portraitX, c.rectY(portraitY, ph), pw, ph);
        }

        int slotSize = 48;
        int slotGap = 10;
        int slotX = px + panelW - slotSize - 12;
        int slotY = py + 36;
        EquipSlot[] slots = EquipSlot.values();
        for (int i = 0; i < slots.length; i++) {
            EquipSlot slot = slots[i];
            int sy = slotY + i * (slotSize + slotGap);
            ui.equipmentSlotBounds[i] = new Rectangle(slotX, sy, slotSize, slotSize);
            boolean hovered = ui.equipmentHoveredSlot == i;
            Armour equipped = presenter.model().getEquipped(slot);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(22f / 255f, 14f / 255f, 8f / 255f, 0.86f);
            shapes.rect(slotX, c.rectY(sy, slotSize), slotSize, slotSize);
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(hovered ? 200f / 255f : 120f / 255f,
                hovered ? 160f / 255f : 90f / 255f, hovered ? 70f / 255f : 45f / 255f, 1f);
            shapes.rect(slotX, c.rectY(sy, slotSize), slotSize, slotSize);
            shapes.end();

            BufferedImage icon = null;
            if (equipped != null) {
                ShopCategory cat = switch (slot) {
                    case CHEST -> ShopCategory.CHEST;
                    case LEGS -> ShopCategory.LEGS;
                    case GLOVES -> ShopCategory.GLOVES;
                    case BOOTS -> ShopCategory.BOOTS;
                };
                if (armourIcons instanceof GdxArmourIconRegistry reg) {
                    icon = reg.iconForName(equipped.getName(), cat);
                }
            } else if (slot.iconIndex >= 0 && slot.iconIndex < assets.itemIcons.length
                && assets.itemIcons[slot.iconIndex] != null) {
                icon = assets.iconForCategory(switch (slot) {
                    case CHEST -> ShopCategory.CHEST;
                    case LEGS -> ShopCategory.LEGS;
                    case GLOVES -> ShopCategory.GLOVES;
                    case BOOTS -> ShopCategory.BOOTS;
                });
            }
            if (icon != null) {
                int iconSz = equipped != null ? 30 : 26;
                float alpha = equipped != null ? 1f : 0.35f;
                batch.setColor(1f, 1f, 1f, alpha);
                batch.draw(GdxShopIcons.textureFor(ShopIcon.of(icon)),
                    slotX + (slotSize - iconSz) / 2f, c.rectY(sy + 7, iconSz), iconSz, iconSz);
                batch.setColor(1f, 1f, 1f, 1f);
            }
            small.setColor(MUTED);
            small.draw(batch, slot.label, slotX + 6, c.textBaseline(sy + slotSize - 5));
        }

        int statsX = portraitX;
        int statsY = py + panelH - 80;
        int statsW = slotX + slotSize - statsX;
        int statsH = 70;
        drawEquipmentStats(batch, fonts, c, statsX, statsY, statsW, statsH,
            presenter.model().equippedStatPreview());
    }

    private static void drawEquipmentStats(SpriteBatch batch, GameFonts fonts, SwingCoords c,
                                           int x, int y, int w, int h, ShopModel.StatPreview preview) {
        BitmapFont headerFont = fonts.ui;
        BitmapFont lineFont = fonts.uiSmall;
        headerFont.setColor(220f / 255f, 200f / 255f, 140f / 255f, 1f);
        headerFont.draw(batch, "ХАРАКТЕРИСТИКИ", x + 12, c.textBaseline(y + 18));
        String[] labels = {"Защита", "Выносл.", "Знаки"};
        int lineY = y + 36;
        ShopModel.StatRow[] rows = preview.rows();
        lineFont.setColor(BODY);
        for (int i = 0; i < labels.length && i < rows.length; i++) {
            ShopModel.StatRow row = rows[i];
            String delta = row.delta() > 0 ? " (+" + row.delta() + ")"
                : row.delta() < 0 ? " (" + row.delta() + ")" : "";
            lineFont.draw(batch, labels[i] + ": " + row.value() + delta, x + 12, c.textBaseline(lineY));
            lineY += 15;
        }
    }

    private static void drawPouchIcon(SpriteBatch batch, ShapeRenderer shapes, GdxShopRuntimeAssets assets,
                                      int x, int topY, int size, boolean selected, boolean hovered,
                                      SwingCoords c) {
        if (assets.walletPouch == null) {
            return;
        }
        if (selected || hovered) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(selected ? 120f / 255f : 80f / 255f,
                selected ? 90f / 255f : 60f / 255f, selected ? 40f / 255f : 30f / 255f, 0.9f);
            shapes.rect(x - 2, c.rectY(topY - 2, size + 4), size + 4, size + 4);
            shapes.end();
        }
        batch.draw(assets.walletPouch, x, c.rectY(topY, size), size, size);
    }

    private static void drawChromeButton(SpriteBatch batch, ShapeRenderer shapes, Rectangle r,
                                         boolean hovered, boolean close, SwingCoords c) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(hovered ? 0.55f : 0.35f, hovered ? 0.25f : 0.18f, 0.08f, 0.95f);
        float gy = c.rectY(r.y, r.height);
        shapes.rect(r.x, gy, r.width, r.height);
        shapes.end();
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.9f, 0.75f, 0.4f, 1f);
        if (close) {
            float cx = r.x + r.width * 0.5f;
            float cy = gy + r.height * 0.5f;
            shapes.line(cx - 4, cy - 4, cx + 4, cy + 4);
            shapes.line(cx + 4, cy - 4, cx - 4, cy + 4);
        } else {
            shapes.line(r.x + r.width - 4, gy + r.height * 0.5f, r.x + 4, gy + r.height * 0.5f);
            shapes.line(r.x + 7, gy + r.height * 0.5f - 3, r.x + 4, gy + r.height * 0.5f);
            shapes.line(r.x + 7, gy + r.height * 0.5f + 3, r.x + 4, gy + r.height * 0.5f);
        }
        shapes.end();
    }

    private static Rectangle closeButtonRect(int panelX, int panelY, int panelW) {
        return new Rectangle(panelX + panelW - CHROME_BTN - 6, panelY + 5, CHROME_BTN, CHROME_BTN);
    }

    private static String truncate(BitmapFont font, GlyphLayout glyph, String text, float maxW) {
        glyph.setText(font, text);
        if (glyph.width <= maxW) {
            return text;
        }
        String ell = "…";
        for (int len = text.length() - 1; len > 0; len--) {
            String sub = text.substring(0, len) + ell;
            glyph.setText(font, sub);
            if (glyph.width <= maxW) {
                return sub;
            }
        }
        return ell;
    }
}
