package main.java.com.witcher.ui.shop.swing.overlay;

import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** Общие текстовые хелперы оверлеев лавки. */
final class ShopOverlayText {

    private ShopOverlayText() {
    }

    static void drawEquipText(Graphics2D g, Font font, String text, int x, int y, Color color) {
        GameFonts.applyGothicHints(g);
        g.setFont(font);
        int tx = (x + 1) & ~1;
        int ty = (y + 1) & ~1;
        GameFonts.drawOutlined(g, text, tx, ty, color);
    }

    static String truncateToWidth(String text, FontMetrics fm, int maxW) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (fm.stringWidth(text) <= maxW) {
            return text;
        }
        String ellipsis = "…";
        if (fm.stringWidth(ellipsis) > maxW) {
            return "";
        }
        for (int i = text.length() - 1; i > 0; i--) {
            String cut = text.substring(0, i) + ellipsis;
            if (fm.stringWidth(cut) <= maxW) {
                return cut;
            }
        }
        return ellipsis;
    }
}
