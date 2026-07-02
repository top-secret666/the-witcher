package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.*;

/** Полоски характеристик на обороте shop_card_back. */
final class ShopStatBarRenderer {

    private ShopStatBarRenderer() {
    }

    static void draw(Graphics2D g, int x, int y, int w, int h, ShopModel.StatPreview preview) {
        drawCardText(g);
        int fontSize = w < 90 ? 7 : (h > 200 ? 10 : 8);
        g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, fontSize));
        FontMetrics fm = g.getFontMetrics();

        int topInset = Math.max(18, Math.round(h * 0.16f));
        int headerY = y + topInset + fm.getAscent();
        String header = "ХАРАКТЕРИСТИКИ";
        int headerX = x + (w - fm.stringWidth(header)) / 2;
        g.setColor(new Color(220, 200, 140));
        g.drawString(header, headerX, headerY);

        int barW = w - 16;
        int rowH = Math.max(28, Math.round(h * 0.20f));
        int startY = headerY + 10;
        String[] labels = {"Защита", "Выносл.", "Знаки"};
        Color[] colors = {
            new Color(190, 45, 45),
            new Color(55, 150, 85),
            new Color(55, 110, 190)
        };

        for (int i = 0; i < preview.rows().length; i++) {
            ShopModel.StatRow row = preview.rows()[i];
            int ry = startY + i * rowH;
            drawOutlinedText(g, labels[i], x + 8, ry + fm.getAscent(), new Color(200, 185, 150));
            int barY = ry + fm.getHeight() + 2;
            int barH = Math.max(5, Math.min(8, rowH / 4));
            drawSkyrimBar(g, x + 8, barY, barW, barH, colors[i], row.value(), row.max());
            String delta = formatDelta(row.delta());
            Color deltaColor = row.delta() > 0 ? new Color(120, 220, 120)
                : row.delta() < 0 ? new Color(220, 100, 100) : new Color(160, 150, 130);
            String valueText = row.value() + (delta.isEmpty() ? "" : " " + delta);
            int valueY = barY + barH + fm.getAscent() + 2;
            drawOutlinedText(g, valueText, x + 8, valueY, delta.isEmpty() ? new Color(235, 225, 200) : deltaColor);
        }
    }

    private static void drawSkyrimBar(Graphics2D g, int x, int y, int w, int h,
                                      Color fill, int value, int max) {
        int tip = Math.max(2, h / 2);
        int[] xs = {x + tip, x + w - tip, x + w, x + w - tip, x + tip, x, x + tip};
        int[] ys = {y, y, y + h / 2, y + h, y + h, y + h / 2, y};
        g.setColor(new Color(20, 16, 12, 220));
        g.fillPolygon(xs, ys, xs.length);
        g.setColor(new Color(60, 48, 30));
        g.drawPolygon(xs, ys, xs.length);

        float ratio = max <= 0 ? 0f : Math.min(1f, value / (float) max);
        int innerW = Math.max(0, Math.round((w - tip * 2 - 4) * ratio));
        if (innerW > 0) {
            int ix = x + tip + 2;
            int iy = y + 1;
            int ih = h - 2;
            g.setColor(fill.darker());
            g.fillRect(ix, iy, innerW, ih);
            g.setColor(fill);
            g.fillRect(ix, iy, Math.max(1, innerW - 1), Math.max(1, ih - 1));
        }
    }

    private static String formatDelta(int delta) {
        if (delta > 0) {
            return "(+" + delta + ")";
        }
        if (delta < 0) {
            return "(" + delta + ")";
        }
        return "";
    }

    private static void drawOutlinedText(Graphics2D g, String text, int tx, int ty, Color fill) {
        g.setColor(new Color(12, 8, 4, 200));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(text, tx + dx, ty + dy);
                }
            }
        }
        g.setColor(fill);
        g.drawString(text, tx, ty);
    }

    private static void drawCardText(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }
}
