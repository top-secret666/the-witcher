package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Колбочки с жидкостью на обороте shop_card_back. */
final class ShopStatBarRenderer {

    private static final Color DELTA_YELLOW = new Color(230, 195, 55);
    private static Rectangle cachedEmptyCrop;
    private static Rectangle cachedOverlayCrop;

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
        int rowH = Math.max(30, Math.round(h * 0.21f));
        int startY = headerY + 10;
        String[] labels = {"Защита", "Выносл.", "Знаки"};
        Color[] colors = {
            new Color(190, 45, 45),
            new Color(55, 150, 85),
            new Color(55, 110, 190)
        };

        boolean useVials = vialEmpty != null;
        int vialH = vialHeight(barW, vialEmpty, h);

        for (int i = 0; i < preview.rows().length; i++) {
            ShopModel.StatRow row = preview.rows()[i];
            int ry = startY + i * rowH;
            drawOutlinedText(g, labels[i], x + 8, ry + fm.getAscent(), new Color(200, 185, 150));
            int barY = ry + fm.getHeight() + 2;
            int baseValue = row.value() - row.delta();
            if (useVials) {
                drawVialComparison(g, x + 8, barY, barW, vialH, vialEmpty, vialOverlay,
                    colors[i], baseValue, row.value(), row.max());
            } else {
                drawComparisonBar(g, x + 8, barY, barW, Math.max(5, Math.min(8, rowH / 4)),
                    colors[i], baseValue, row.value(), row.max());
            }
            String delta = formatDelta(row.delta());
            Color deltaColor = row.delta() != 0 ? DELTA_YELLOW : new Color(160, 150, 130);
            String valueText = row.value() + (delta.isEmpty() ? "" : " " + delta);
            int valueY = barY + vialH + fm.getAscent() + 3;
            drawOutlinedText(g, valueText, x + 8, valueY, delta.isEmpty() ? new Color(235, 225, 200) : deltaColor);
        }
    }

    private static int vialHeight(int barW, BufferedImage vialEmpty, int cardH) {
        Rectangle crop = cropOf(vialEmpty, true);
        if (crop.width <= 0 || crop.height <= 0) {
            return Math.max(12, Math.min(16, Math.round(barW * 0.34f)));
        }
        float aspect = crop.width / (float) crop.height;
        int fromAspect = Math.round(barW / aspect);
        int cap = cardH > 200 ? 16 : 14;
        return Math.max(12, Math.min(cap, fromAspect));
    }

    private static Rectangle cropOf(BufferedImage img, boolean empty) {
        if (img == null) {
            return new Rectangle(0, 0, 0, 0);
        }
        if (empty && cachedEmptyCrop != null) {
            return cachedEmptyCrop;
        }
        if (!empty && cachedOverlayCrop != null) {
            return cachedOverlayCrop;
        }
        Rectangle crop = ShopScreen.computeContentBoundsPublic(img);
        if (empty) {
            cachedEmptyCrop = crop;
        } else {
            cachedOverlayCrop = crop;
        }
        return crop;
    }

    private static void drawVialComparison(Graphics2D g, int x, int y, int w, int h,
                                           BufferedImage vialEmpty, BufferedImage vialOverlay,
                                           Color main, int baseValue, int newValue, int max) {
        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        Rectangle emptyCrop = cropOf(vialEmpty, true);
        int padX = Math.max(4, Math.round(w * 0.11f));
        int padY = Math.max(2, Math.round(h * 0.20f));
        int cavityX = x + padX;
        int cavityY = y + padY;
        int cavityW = Math.max(1, w - padX * 2);
        int cavityH = Math.max(1, h - padY * 2);

        g.setColor(new Color(18, 12, 8, 210));
        g.fillRoundRect(cavityX, cavityY, cavityW, cavityH, cavityH, cavityH);

        Shape savedClip = g.getClip();
        g.clipRect(cavityX, cavityY, cavityW, cavityH);
        drawLiquidComparison(g, cavityX, cavityY, cavityW, cavityH, main, baseValue, newValue, max);
        g.setClip(savedClip);

        drawCroppedSprite(g, vialEmpty, emptyCrop, x, y, w, h);
        if (vialOverlay != null) {
            drawCroppedSprite(g, vialOverlay, cropOf(vialOverlay, false), x, y, w, h);
        }

        if (interp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
        }
    }

    private static void drawCroppedSprite(Graphics2D g, BufferedImage img, Rectangle crop,
                                          int dx, int dy, int dw, int dh) {
        if (crop.width > 0 && crop.height > 0) {
            g.drawImage(img, dx, dy, dx + dw, dy + dh,
                crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
        } else {
            g.drawImage(img, dx, dy, dw, dh, null);
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

    private static void fillLiquid(Graphics2D g, int x, int y, int width, int height, Color base) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int r = Math.max(1, height / 2);
        Color top = brighten(base, 0.42f);
        Color mid = base;
        Color bottom = darken(base, 0.28f);
        Paint paint = new LinearGradientPaint(
            x, y, x, y + height,
            new float[]{0f, 0.38f, 1f},
            new Color[]{top, mid, bottom});
        Paint saved = g.getPaint();
        g.setPaint(paint);
        g.fillRoundRect(x, y, width, height, r, r);
        g.setPaint(saved);

        g.setColor(new Color(255, 255, 255, 45));
        g.fillRoundRect(x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height / 3), r, r);
        if (width > 2) {
            g.setColor(brighten(base, 0.5f));
            g.fillRect(x + width - 1, y + 1, 1, Math.max(1, height - 2));
        }
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(x, y + height - 1, width, 1);
    }

    private static void drawComparisonBar(Graphics2D g, int x, int y, int w, int h,
                                          Color main, int baseValue, int newValue, int max) {
        Shape savedClip = g.getClip();
        g.clipRect(x, y, w, h);
        drawLiquidComparison(g, x, y, w, h, main, baseValue, newValue, max);
        g.setClip(savedClip);
    }

    private static Color brighten(Color c, float amount) {
        return blendColors(c, Color.WHITE, amount);
    }

    private static Color darken(Color c, float amount) {
        return blendColors(c, Color.BLACK, amount);
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
