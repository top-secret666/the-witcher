package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Колбочки с жидкостью на обороте shop_card_back. */
final class ShopStatBarRenderer {

    private static final Color DELTA_YELLOW = new Color(230, 195, 55);

    private ShopStatBarRenderer() {
    }

    static void draw(Graphics2D g, int x, int y, int w, int h, ShopModel.StatPreview preview,
                     BufferedImage vialEmpty, BufferedImage vialOverlay) {
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

        boolean useVials = vialEmpty != null;

        for (int i = 0; i < preview.rows().length; i++) {
            ShopModel.StatRow row = preview.rows()[i];
            int ry = startY + i * rowH;
            drawOutlinedText(g, labels[i], x + 8, ry + fm.getAscent(), new Color(200, 185, 150));
            int barY = ry + fm.getHeight() + 2;
            int baseValue = row.value() - row.delta();
            if (useVials) {
                int vialH = Math.max(8, Math.min(11, Math.round(barW * 0.24f)));
                drawVialComparison(g, x + 8, barY, barW, vialH, vialEmpty, vialOverlay,
                    colors[i], baseValue, row.value(), row.max());
            } else {
                drawComparisonBar(g, x + 8, barY, barW, Math.max(5, Math.min(8, rowH / 4)),
                    colors[i], baseValue, row.value(), row.max());
            }
            String delta = formatDelta(row.delta());
            Color deltaColor = row.delta() != 0 ? DELTA_YELLOW : new Color(160, 150, 130);
            String valueText = row.value() + (delta.isEmpty() ? "" : " " + delta);
            int valueY = barY + (useVials ? Math.max(8, Math.min(11, Math.round(barW * 0.24f))) : Math.max(5, Math.min(8, rowH / 4)))
                + fm.getAscent() + 2;
            drawOutlinedText(g, valueText, x + 8, valueY, delta.isEmpty() ? new Color(235, 225, 200) : deltaColor);
        }
    }

    /**
     * Слои (снизу вверх):
     * 1) тёмная полость;
     * 2) жидкость с вертикальным градиентом (основной цвет + жёлтая дельта);
     * 3) рамка колбы;
     * 4) блик стекла.
     */
    private static void drawVialComparison(Graphics2D g, int x, int y, int w, int h,
                                           BufferedImage vialEmpty, BufferedImage vialOverlay,
                                           Color main, int baseValue, int newValue, int max) {
        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int padX = Math.max(2, w / 14);
        int padY = Math.max(1, h / 5);
        int cavityX = x + padX;
        int cavityY = y + padY;
        int cavityW = Math.max(1, w - padX * 2);
        int cavityH = Math.max(1, h - padY * 2);

        g.setColor(new Color(14, 10, 8, 200));
        g.fillRoundRect(cavityX, cavityY, cavityW, cavityH, cavityH / 2, cavityH / 2);

        Shape savedClip = g.getClip();
        g.clipRect(cavityX, cavityY, cavityW, cavityH);
        drawLiquidComparison(g, cavityX, cavityY, cavityW, cavityH, main, baseValue, newValue, max);
        g.setClip(savedClip);

        g.drawImage(vialEmpty, x, y, w, h, null);
        if (vialOverlay != null) {
            g.drawImage(vialOverlay, x, y, w, h, null);
        }

        if (interp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
        }
    }

    private static void drawLiquidComparison(Graphics2D g, int x, int y, int w, int h,
                                             Color main, int baseValue, int newValue, int max) {
        if (w <= 0 || h <= 0 || max <= 0) {
            return;
        }

        int baseW = Math.round(w * Math.min(1f, Math.max(0, baseValue) / (float) max));
        int newW = Math.round(w * Math.min(1f, Math.max(0, newValue) / (float) max));
        int delta = newValue - baseValue;

        if (delta >= 0) {
            if (baseW > 0) {
                fillLiquid(g, x, y, baseW, h, main);
            }
            if (delta > 0 && newW > baseW) {
                fillLiquid(g, x + baseW, y, newW - baseW, h, DELTA_YELLOW);
            }
        } else {
            if (newW > 0) {
                fillLiquid(g, x, y, newW, h, main);
            }
            if (baseW > newW) {
                Color lossBlend = blendColors(main, DELTA_YELLOW, 0.55f);
                fillLiquid(g, x + newW, y, baseW - newW, h, lossBlend);
            }
        }
    }

    /** Вертикальный градиент + тонкая «мениск»-линия на правом крае заливки. */
    private static void fillLiquid(Graphics2D g, int x, int y, int width, int height, Color base) {
        if (width <= 0 || height <= 0) {
            return;
        }
        Color top = brighten(base, 0.38f);
        Color mid = base;
        Color bottom = darken(base, 0.22f);
        Paint paint = new LinearGradientPaint(
            x, y, x, y + height,
            new float[]{0f, 0.42f, 1f},
            new Color[]{top, mid, bottom});
        Paint saved = g.getPaint();
        g.setPaint(paint);
        g.fillRect(x, y, width, height);
        g.setPaint(saved);

        if (width > 1) {
            g.setColor(brighten(base, 0.55f));
            g.fillRect(x + width - 1, y + 1, 1, Math.max(1, height - 2));
        }
        g.setColor(new Color(0, 0, 0, 35));
        g.fillRect(x, y + height - 1, width, 1);
    }

    private static Color brighten(Color c, float amount) {
        return blendColors(c, Color.WHITE, amount);
    }

    private static Color darken(Color c, float amount) {
        return blendColors(c, Color.BLACK, amount);
    }

    private static void drawComparisonBar(Graphics2D g, int x, int y, int w, int h,
                                          Color main, int baseValue, int newValue, int max) {
        int tip = Math.max(2, h / 2);
        int ix = x + tip + 2;
        int iy = y + 1;
        int ih = h - 2;
        int trackW = w - tip * 2 - 4;
        if (trackW <= 0 || max <= 0) {
            return;
        }
        Shape savedClip = g.getClip();
        g.clipRect(ix, iy, trackW, ih);
        drawLiquidComparison(g, ix, iy, trackW, ih, main, baseValue, newValue, max);
        g.setClip(savedClip);
    }

    private static Color blendColors(Color base, Color overlay, float overlayWeight) {
        float t = Math.max(0f, Math.min(1f, overlayWeight));
        return new Color(
            Math.round(base.getRed() * (1f - t) + overlay.getRed() * t),
            Math.round(base.getGreen() * (1f - t) + overlay.getGreen() * t),
            Math.round(base.getBlue() * (1f - t) + overlay.getBlue() * t));
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
