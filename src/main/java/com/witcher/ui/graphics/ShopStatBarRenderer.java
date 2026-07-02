package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Колбочки с жидкостью на обороте shop_card_back. */
final class ShopStatBarRenderer {

    private static final Color DELTA_YELLOW = new Color(175, 135, 28);
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
            new Color(140, 28, 32),
            new Color(28, 110, 62),
            new Color(32, 72, 145)
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
        applyWarmGlassTint(g, x, y, w, h);
        drawGothicVialTrim(g, x, y, w, h);

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

    /** Тёплый тон поверх PNG — гасит холодное сине-белое свечение, ближе к золотой рамке лавки. */
    private static void applyWarmGlassTint(Graphics2D g, int x, int y, int w, int h) {
        int arc = Math.max(4, h);
        Composite saved = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.34f));
        g.setColor(new Color(58, 38, 18));
        g.fillRoundRect(x, y, w, h, arc, arc);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.16f));
        g.setColor(new Color(175, 130, 55));
        g.fillRoundRect(x + 1, y + 1, w - 2, Math.max(2, h - 2), arc - 2, arc - 2);

        g.setComposite(saved);
    }

    /** Готическая латунная окантовка: зубцы сверху, угловые скобы, заклёпки по краям. */
    private static void drawGothicVialTrim(Graphics2D g, int x, int y, int w, int h) {
        if (w < 14 || h < 6) {
            drawVialRim(g, x, y, w, h);
            return;
        }

        int arc = Math.max(4, h);
        Color shadow = new Color(24, 14, 8);
        Color darkBrass = new Color(58, 38, 16);
        Color midBrass = new Color(118, 84, 34);
        Color brightBrass = new Color(188, 142, 52);
        Color highlight = new Color(232, 200, 118);

        Composite saved = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Базовый ободок колбы
        g.setColor(shadow);
        g.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
        g.setColor(darkBrass);
        g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, arc - 2, arc - 2);
        g.setColor(midBrass);
        g.drawRoundRect(x + 2, y + 2, w - 5, h - 5, arc - 3, arc - 3);
        g.setColor(brightBrass);
        g.drawRoundRect(x + 3, y + 3, w - 7, h - 7, arc - 4, arc - 4);

        int crestH = Math.min(4, Math.max(2, h / 3));
        int innerL = x + 5;
        int innerR = x + w - 6;
        int toothW = Math.max(3, Math.min(5, w / 9));
        int gap = Math.max(1, toothW / 3);

        // Готические зубцы сверху (мерлоны)
        for (int cx = innerL; cx <= innerR - toothW; cx += toothW + gap) {
            int bw = Math.min(toothW, innerR - cx);
            if (bw < 2) {
                break;
            }
            int ty = y - crestH + 1;
            g.setColor(shadow);
            g.fillRect(cx, ty, bw, crestH);
            g.setColor(darkBrass);
            g.fillRect(cx, ty + 1, bw, crestH - 1);
            g.setColor(highlight);
            g.fillRect(cx, ty, bw, 1);
            g.setColor(brightBrass);
            g.fillRect(cx + 1, ty + 1, Math.max(1, bw - 2), 1);
        }

        // Угловые скобы сверху
        drawGothicCornerBracket(g, x + 1, y, crestH, true, shadow, darkBrass, brightBrass, highlight);
        drawGothicCornerBracket(g, x + w - 2, y, crestH, false, shadow, darkBrass, brightBrass, highlight);

        // Нижний пояс — лёгкие зубчики поменьше
        int baseH = Math.max(1, crestH - 1);
        for (int cx = innerL + 2; cx <= innerR - toothW; cx += toothW + gap + 1) {
            int bw = Math.min(toothW - 1, innerR - cx);
            if (bw < 2) {
                break;
            }
            int by = y + h - 1;
            g.setColor(darkBrass);
            g.fillRect(cx, by, bw, baseH);
            g.setColor(new Color(72, 48, 22, 180));
            g.fillRect(cx, by, bw, 1);
        }

        // Заклёпки на торцах
        int rivetY = y + h / 2;
        drawEndRivet(g, x + 3, rivetY, h, shadow, midBrass, highlight);
        drawEndRivet(g, x + w - 4, rivetY, h, shadow, midBrass, highlight);

        // Тёплый блик по верхней кромке стекла
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.82f));
        g.setColor(highlight);
        g.drawLine(x + 6, y + 2, x + w - 7, y + 2);
        g.setColor(new Color(220, 185, 110, 150));
        g.drawLine(x + 7, y + 3, x + w - 8, y + 3);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
        g.setColor(new Color(40, 26, 12));
        g.drawLine(x + 6, y + h - 2, x + w - 7, y + h - 2);

        g.setComposite(saved);
    }

    private static void drawGothicCornerBracket(Graphics2D g, int cx, int cy, int rise, boolean left,
                                              Color shadow, Color dark, Color bright, Color highlight) {
        int top = cy - rise + 1;
        if (left) {
            g.setColor(shadow);
            g.fillRect(cx, top, 3, rise);
            g.fillRect(cx, top, 4, 2);
            g.setColor(dark);
            g.fillRect(cx, top + 1, 2, rise - 1);
            g.setColor(bright);
            g.fillRect(cx + 1, top + 1, 1, Math.max(1, rise - 2));
            g.setColor(highlight);
            g.fillRect(cx, top, 2, 1);
        } else {
            g.setColor(shadow);
            g.fillRect(cx - 2, top, 3, rise);
            g.fillRect(cx - 3, top, 4, 2);
            g.setColor(dark);
            g.fillRect(cx - 2, top + 1, 2, rise - 1);
            g.setColor(bright);
            g.fillRect(cx - 1, top + 1, 1, Math.max(1, rise - 2));
            g.setColor(highlight);
            g.fillRect(cx - 1, top, 2, 1);
        }
    }

    private static void drawEndRivet(Graphics2D g, int cx, int cy, int h, Color shadow, Color mid, Color hi) {
        int r = Math.max(2, Math.min(3, h / 4));
        g.setColor(shadow);
        g.fillOval(cx - r, cy - r, r * 2, r * 2);
        g.setColor(mid);
        g.fillOval(cx - r + 1, cy - r + 1, r * 2 - 2, r * 2 - 2);
        g.setColor(hi);
        g.fillRect(cx - 1, cy - r, 2, 1);
    }

    /** Ободок в палитре лавки: тёмная кожа/дерево + латунь + тёплый блик. */
    private static void drawVialRim(Graphics2D g, int x, int y, int w, int h) {
        int arc = Math.max(4, h);
        Composite saved = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setColor(new Color(32, 22, 12));
        g.drawRoundRect(x, y, w - 1, h - 1, arc, arc);
        g.drawRoundRect(x + 1, y, w - 1, h - 1, arc, arc);

        g.setColor(new Color(108, 78, 36));
        g.drawRoundRect(x + 1, y + 1, w - 3, h - 3, arc - 2, arc - 2);

        g.setColor(new Color(168, 128, 52));
        g.drawRoundRect(x + 2, y + 2, w - 5, h - 5, arc - 3, arc - 3);

        g.setColor(new Color(212, 175, 88));
        g.drawRoundRect(x + 3, y + 3, w - 7, h - 7, arc - 4, arc - 4);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
        g.setColor(new Color(238, 208, 138));
        g.drawLine(x + 5, y + 2, x + w - 6, y + 2);
        if (h > 7) {
            g.setColor(new Color(220, 185, 110, 180));
            g.drawLine(x + 6, y + 3, x + w - 7, y + 3);
        }

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(new Color(92, 58, 24));
        g.drawLine(x + 5, y + h - 2, x + w - 6, y + h - 2);

        g.setComposite(saved);
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

        Color deep = darken(base, 0.62f);
        Color mid = darken(base, 0.22f);
        Color body = darken(base, 0.05f);
        Color glow = brighten(base, 0.18f);
        Color core = brighten(base, 0.38f);

        Paint vert = new LinearGradientPaint(
            x, y, x, y + height,
            new float[]{0f, 0.22f, 0.55f, 0.82f, 1f},
            new Color[]{deep, mid, body, glow, core});
        Paint saved = g.getPaint();
        g.setPaint(vert);
        g.fillRoundRect(x, y, width, height, r, r);

        if (width > 4) {
            Color edgeDark = darken(base, 0.45f);
            Color edgeLight = brighten(base, 0.12f);
            g.setPaint(new LinearGradientPaint(
                x, y, x + width, y,
                new float[]{0f, 0.15f, 0.5f, 0.85f, 1f},
                new Color[]{edgeDark, body, glow, body, edgeDark}));
            g.fillRoundRect(x + 1, y + 1, Math.max(1, width - 2), Math.max(1, height - 2), r - 1, r - 1);
        }

        g.setPaint(saved);
        drawLiquidShimmers(g, x, y, width, height, brighten(base, 0.55f));
        drawLiquidMeniscus(g, x, y, width, height, core);
    }

    /** Мелкие блики внутри жидкости — как искры в зелье. */
    private static void drawLiquidShimmers(Graphics2D g, int x, int y, int w, int h, Color spark) {
        int sparks = Math.max(2, w / 6);
        for (int i = 0; i < sparks; i++) {
            int seed = x * 31 + y * 17 + w * 13 + i * 23;
            int sx = x + 2 + Math.floorMod(seed, Math.max(1, w - 3));
            int sy = y + 1 + Math.floorMod(seed / 7, Math.max(1, h - 2));
            int alpha = 70 + Math.floorMod(seed / 11, 110);
            g.setColor(new Color(spark.getRed(), spark.getGreen(), spark.getBlue(), alpha));
            g.fillRect(sx, sy, 1, 1);
            if (w > 10 && i % 3 == 0) {
                g.setColor(new Color(255, 255, 255, alpha / 2));
                g.fillRect(sx + 1, sy, 1, 1);
            }
        }
    }

    private static void drawLiquidMeniscus(Graphics2D g, int x, int y, int width, int height, Color edge) {
        g.setColor(new Color(0, 0, 0, 65));
        g.fillRect(x, y + height - 1, width, 1);
        if (width > 2) {
            g.setColor(new Color(edge.getRed(), edge.getGreen(), edge.getBlue(), 160));
            g.fillRect(x + width - 1, y + 1, 1, Math.max(1, height - 2));
            g.setColor(new Color(255, 255, 255, 35));
            g.fillRect(x + 1, y + 1, Math.max(1, width / 3), 1);
        }
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
