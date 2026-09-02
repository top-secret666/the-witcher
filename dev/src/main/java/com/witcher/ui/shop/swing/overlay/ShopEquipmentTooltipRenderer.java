package main.java.com.witcher.ui.shop.swing.overlay;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.enums.ArmourType;
import main.java.com.witcher.model.sets.ArmourSet;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.shop.EquipmentArmourList;
import main.java.com.witcher.shop.EquipSlot;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.swing.ShopCategoryGlow;

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

    private static final Color BG = new Color(12, 10, 8, 238);
    private static final Color EFFECT = new Color(238, 198, 58);
    private static final Color BODY = new Color(220, 210, 190);
    private static final Color FOOTER = new Color(170, 160, 140);

    private ShopEquipmentTooltipRenderer() {
    }

    public static void draw(Graphics2D g, int x, int y, int maxW, Armour armour, ShopModel model) {
        if (armour == null || maxW < 80) {
            return;
        }
        GameFonts.applyGothicHints(g);
        int pad = 8;
        int innerW = Math.max(24, maxW - pad * 2);

        ShopCategory category = EquipmentArmourList.categoryFor(armour);
        Color accent = ShopCategoryGlow.descriptionColor(category);
        Color border = ShopCategoryGlow.borderColor(category);
        Color categoryMute = new Color(
            Math.max(40, accent.getRed() - 40),
            Math.max(40, accent.getGreen() - 35),
            Math.max(40, accent.getBlue() - 30));

        Font titleFont = GameFonts.get().uiBold(10);
        Font catFont = GameFonts.get().uiPlain(8);
        Font effectFont = GameFonts.get().uiBold(9);
        Font bodyFont = GameFonts.get().uiPlain(8);
        Font footerFont = GameFonts.get().uiPlain(8);

        FontMetrics titleFm = g.getFontMetrics(titleFont);
        FontMetrics catFm = g.getFontMetrics(catFont);
        FontMetrics effectFm = g.getFontMetrics(effectFont);
        FontMetrics bodyFm = g.getFontMetrics(bodyFont);
        FontMetrics footerFm = g.getFontMetrics(footerFont);

        List<String> titleLines = wrapLines(armour.getName().toUpperCase(Locale.ROOT), titleFm, innerW);
        List<String> bodyLines = wrapLines(armour.getDescription(), bodyFm, innerW);
        String bonus = model.armourBonusLine(armour);
        List<String> bonusLines = bonus.isEmpty() ? List.of() : wrapLines(bonus, effectFm, innerW);
        String rarity = rarityLabel(armour.getType());
        String slotLabel = slotLabel(armour);

        int h = pad;
        h += titleLines.size() * titleFm.getHeight();
        h += 4 + catFm.getHeight();
        if (!bonusLines.isEmpty()) {
            h += 6 + bonusLines.size() * effectFm.getHeight();
        }
        h += bodyLines.size() * (bodyFm.getHeight() + 1);
        h += 6 + catFm.getAscent() + 12 + footerFm.getHeight() + pad;

        Composite prev = g.getComposite();
        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.98f));
        g.setColor(BG);
        g.fillRoundRect(x, y, maxW, h, 4, 4);
        g.setColor(border);
        g.drawRoundRect(x, y, maxW - 1, h - 1, 4, 4);
        g.drawRoundRect(x + 1, y + 1, maxW - 3, h - 3, 3, 3);

        int ty = y + pad;
        g.setFont(titleFont);
        g.setColor(accent);
        for (String line : titleLines) {
            ty += titleFm.getAscent();
            g.drawString(line, x + pad, ty);
            ty += titleFm.getDescent();
        }

        ty += 4;
        g.setFont(catFont);
        g.setColor(categoryMute);
        ty += catFm.getAscent();
        g.drawString(truncate(slotLabel.toUpperCase(Locale.ROOT), catFm, innerW), x + pad, ty);
        ty += catFm.getDescent();

        if (!bonusLines.isEmpty()) {
            ty += 6;
            g.setFont(effectFont);
            g.setColor(EFFECT);
            for (String line : bonusLines) {
                ty += effectFm.getAscent();
                g.drawString(line, x + pad, ty);
                ty += effectFm.getDescent();
            }
        }

        g.setFont(bodyFont);
        g.setColor(BODY);
        for (String line : bodyLines) {
            ty += bodyFm.getAscent() + 1;
            g.drawString(line, x + pad, ty);
            ty += bodyFm.getDescent();
        }

        ty += 4;
        g.setFont(catFont);
        g.setColor(accent);
        ty += catFm.getAscent();
        g.drawString(truncate(rarity, catFm, innerW), x + pad, ty);

        ty += 10;
        g.setFont(footerFont);
        g.setColor(FOOTER);
        ty += footerFm.getAscent();
        String footer = String.format("%.1f кг   ·   %d крон", armour.getWeight(), armour.getPrice());
        g.drawString(truncate(footer, footerFm, innerW), x + pad, ty);

        g.setComposite(prev);
        if (interp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
        }
    }

    public static void drawKit(Graphics2D g, int x, int y, int maxW, ArmourSet set) {
        if (set == null || maxW < 80) {
            return;
        }
        GameFonts.applyGothicHints(g);
        int pad = 8;
        int innerW = Math.max(24, maxW - pad * 2);
        Color accent = ShopCategoryGlow.descriptionColor(ShopCategory.SETS);
        Color border = ShopCategoryGlow.borderColor(ShopCategory.SETS);

        Font titleFont = GameFonts.get().uiBold(10);
        Font catFont = GameFonts.get().uiPlain(8);
        Font bodyFont = GameFonts.get().uiPlain(8);
        FontMetrics titleFm = g.getFontMetrics(titleFont);
        FontMetrics catFm = g.getFontMetrics(catFont);
        FontMetrics bodyFm = g.getFontMetrics(bodyFont);

        List<String> titleLines = wrapLines(set.getName().toUpperCase(Locale.ROOT), titleFm, innerW);
        List<String> bodyLines = wrapLines(
            "Эмблема комплекта. Клик — надеть все 4 части (кираса, штаны, перчатки, сапоги).",
            bodyFm, innerW);

        int h = pad + titleLines.size() * titleFm.getHeight() + 4 + catFm.getHeight()
            + bodyLines.size() * (bodyFm.getHeight() + 1) + pad;

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.96f));
        g.setColor(BG);
        g.fillRoundRect(x, y, maxW, h, 4, 4);
        g.setColor(border);
        g.drawRoundRect(x, y, maxW, h, 4, 4);

        int ty = y + pad;
        g.setFont(titleFont);
        g.setColor(accent);
        for (String line : titleLines) {
            ty += titleFm.getAscent();
            g.drawString(line, x + pad, ty);
            ty += titleFm.getDescent() + titleFm.getLeading();
        }
        ty += 4;
        g.setFont(catFont);
        g.setColor(new Color(180, 150, 210));
        ty += catFm.getAscent();
        g.drawString("КОМПЛЕКТ · 4 ЧАСТИ", x + pad, ty);
        ty += catFm.getDescent() + 4;
        g.setFont(bodyFont);
        g.setColor(BODY);
        for (String line : bodyLines) {
            ty += bodyFm.getAscent();
            g.drawString(line, x + pad, ty);
            ty += bodyFm.getDescent() + bodyFm.getLeading() + 1;
        }
        g.setComposite(prev);
    }

    public static int preferredWidth() {
        return 176;
    }

    private static String slotLabel(Armour armour) {
        EquipSlot slot = EquipSlot.forArmour(armour);
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

    private static String truncate(String text, FontMetrics fm, int maxW) {
        if (text == null) {
            return "";
        }
        if (fm.stringWidth(text) <= maxW) {
            return text;
        }
        String ellipsis = "…";
        for (int len = text.length() - 1; len > 0; len--) {
            String cut = text.substring(0, len) + ellipsis;
            if (fm.stringWidth(cut) <= maxW) {
                return cut;
            }
        }
        return ellipsis;
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
                    if (fm.stringWidth(word) > maxW) {
                        softBreakWord(lines, word, fm, maxW);
                        line = new StringBuilder();
                    } else {
                        line = new StringBuilder(word);
                    }
                } else if (fm.stringWidth(candidate) > maxW) {
                    softBreakWord(lines, word, fm, maxW);
                    line = new StringBuilder();
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

    private static void softBreakWord(List<String> lines, String word, FontMetrics fm, int maxW) {
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            cur.append(word.charAt(i));
            if (fm.stringWidth(cur.toString()) > maxW && cur.length() > 1) {
                cur.deleteCharAt(cur.length() - 1);
                lines.add(cur.toString());
                cur = new StringBuilder().append(word.charAt(i));
            }
        }
        if (!cur.isEmpty()) {
            lines.add(cur.toString());
        }
    }
}
