package main.java.com.witcher.ui.graphics.overlay;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.enums.ArmourType;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.shop.EquipmentArmourList;
import main.java.com.witcher.ui.shop.ShopEquipSlot;
import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Тултип предмета в стиле инвентаря Ведьмака 3. */
public final class ShopEquipmentTooltipRenderer {

    private static final Color BG = new Color(6, 10, 18, 235);
    private static final Color BORDER = new Color(72, 148, 210);
    private static final Color TITLE = new Color(118, 188, 248);
    private static final Color CATEGORY = new Color(148, 158, 172);
    private static final Color EFFECT = new Color(238, 198, 58);
    private static final Color BODY = new Color(214, 218, 226);
    private static final Color RARITY = new Color(98, 168, 232);
    private static final Color FOOTER = new Color(170, 176, 188);

    private ShopEquipmentTooltipRenderer() {
    }

    public static void draw(Graphics2D g, int x, int y, int maxW, Armour armour, ShopModel model) {
        if (armour == null || maxW < 80) {
            return;
        }
        GameFonts.applyGothicHints(g);
        int pad = 8;
        int innerW = maxW - pad * 2;

        Font titleFont = GameFonts.get().uiBold(10);
        Font catFont = GameFonts.get().uiPlain(8);
        Font effectFont = GameFonts.get().uiBold(9);
        Font bodyFont = GameFonts.get().uiPlain(8);
        Font footerFont = GameFonts.get().uiPlain(8);

        g.setFont(bodyFont);
        List<String> bodyLines = wrapLines(armour.getDescription(), g.getFontMetrics(bodyFont), innerW);
        String bonus = model.armourBonusLine(armour);
        String rarity = rarityLabel(armour.getType());
        String slotLabel = slotLabel(armour);

        FontMetrics titleFm = g.getFontMetrics(titleFont);
        FontMetrics catFm = g.getFontMetrics(catFont);
        FontMetrics effectFm = g.getFontMetrics(effectFont);
        FontMetrics bodyFm = g.getFontMetrics(bodyFont);
        FontMetrics footerFm = g.getFontMetrics(footerFont);

        int h = pad + titleFm.getAscent() + 4 + catFm.getHeight() + 6;
        if (!bonus.isEmpty()) {
            h += effectFm.getHeight() + 2;
        }
        h += bodyLines.size() * (bodyFm.getHeight() + 1);
        h += 6 + catFm.getAscent() + 12 + footerFm.getHeight() + pad;

        Composite prev = g.getComposite();
        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.98f));
        g.setColor(BG);
        g.fillRoundRect(x, y, maxW, h, 4, 4);
        g.setColor(BORDER);
        g.drawRoundRect(x, y, maxW - 1, h - 1, 4, 4);
        g.drawRoundRect(x + 1, y + 1, maxW - 3, h - 3, 3, 3);

        int ty = y + pad + titleFm.getAscent();
        g.setFont(titleFont);
        g.setColor(TITLE);
        g.drawString(armour.getName().toUpperCase(Locale.ROOT), x + pad, ty);

        ty += 4 + catFm.getAscent();
        g.setFont(catFont);
        g.setColor(CATEGORY);
        g.drawString(slotLabel.toUpperCase(Locale.ROOT), x + pad, ty);

        ty += 6;
        if (!bonus.isEmpty()) {
            g.setFont(effectFont);
            g.setColor(EFFECT);
            ty += effectFm.getAscent();
            g.drawString(bonus, x + pad, ty);
            ty += effectFm.getDescent() + 2;
        }

        g.setFont(bodyFont);
        g.setColor(BODY);
        for (String line : bodyLines) {
            ty += bodyFm.getAscent();
            g.drawString(line, x + pad, ty);
            ty += bodyFm.getDescent() + 1;
        }

        ty += 4;
        g.setFont(catFont);
        g.setColor(RARITY);
        ty += catFm.getAscent();
        g.drawString(rarity, x + pad, ty);

        ty += 10;
        g.setFont(footerFont);
        g.setColor(FOOTER);
        ty += footerFm.getAscent();
        String footer = String.format("%.1f кг   ·   %d крон", armour.getWeight(), armour.getPrice());
        g.drawString(footer, x + pad, ty);

        g.setComposite(prev);
        if (interp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
        }
    }

    public static int preferredWidth() {
        return 152;
    }

    private static String slotLabel(Armour armour) {
        ShopEquipSlot slot = ShopEquipSlot.forArmour(armour);
        if (slot != null) {
            return slot.label;
        }
        return EquipmentArmourList.categoryFor(armour).label;
    }

    private static String rarityLabel(ArmourType type) {
        return switch (type) {
            case LIGHT -> "ЛЁГКАЯ БРОНЯ";
            case MEDIUM -> "СРЕДНЯЯ БРОНЯ";
            case HEAVY -> "ТЯЖЁЛАЯ БРОНЯ";
            case FULL_SET -> "КОМПЛЕКТ";
        };
    }

    private static List<String> wrapLines(String text, FontMetrics fm, int maxW) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return lines;
        }
        for (String paragraph : text.split("\n")) {
            String[] words = paragraph.trim().split("\\s+");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                if (word.isEmpty()) {
                    continue;
                }
                String candidate = line.isEmpty() ? word : line + " " + word;
                if (fm.stringWidth(candidate) > maxW && !line.isEmpty()) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    line = new StringBuilder(candidate);
                }
            }
            if (!line.isEmpty()) {
                lines.add(line.toString());
            }
        }
        return lines;
    }
}
