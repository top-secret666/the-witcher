package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.ShopModel;

import java.awt.*;
import java.awt.image.BufferedImage;

/** Колбочки с жидкостью на обороте shop_card_back и в экипировке. */
public final class ShopStatBarRenderer {

    private static final Color DELTA_YELLOW = new Color(255, 215, 48);
    private static Rectangle cachedEmptyCrop;
    private static Rectangle cachedOverlayCrop;
    private static Rectangle cachedEndCapCrop;

    private ShopStatBarRenderer() {
    }

    public static void draw(Graphics2D g, int x, int y, int w, int h, ShopModel.StatPreview preview,
                     BufferedImage vialEmpty, BufferedImage vialOverlay, BufferedImage vialEndCap) {
        draw(g, x, y, w, h, preview, vialEmpty, vialOverlay, vialEndCap, 0);
    }

    public static void draw(Graphics2D g, int x, int y, int w, int h, ShopModel.StatPreview preview,
                     BufferedImage vialEmpty, BufferedImage vialOverlay, BufferedImage vialEndCap,
                     int animTick) {
        drawCardText(g);
        int fontSize = w < 90 ? 7 : (h > 200 ? 10 : 8);
        g.setFont(GameFonts.get().uiBold(fontSize));
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
            new Color(210, 52, 58),
            new Color(48, 195, 108),
            new Color(62, 138, 235)
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
                drawVialComparison(g, x + 8, barY, barW, vialH, vialEmpty, vialOverlay, vialEndCap,
                    colors[i], baseValue, row.value(), row.max(), animTick, i);
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
        Rectangle crop = ShopImageBounds.compute(img);
        if (empty) {
            cachedEmptyCrop = crop;
        } else {
            cachedOverlayCrop = crop;
        }
        return crop;
    }

    private static void drawVialComparison(Graphics2D g, int x, int y, int w, int h,
                                           BufferedImage vialEmpty, BufferedImage vialOverlay,
                                           BufferedImage vialEndCap,
                                           Color main, int baseValue, int newValue, int max,
                                           int animTick, int rowIndex) {
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
        drawLiquidComparison(g, cavityX, cavityY, cavityW, cavityH, main, baseValue, newValue, max,
            animTick, rowIndex);
        g.setClip(savedClip);

        drawCroppedSprite(g, vialEmpty, emptyCrop, x, y, w, h);
        if (vialOverlay != null) {
            drawCroppedSprite(g, vialOverlay, cropOf(vialOverlay, false), x, y, w, h);
        }
        applyWarmGlassTint(g, x, y, w, h);
        if (vialEndCap != null) {
            drawVialEndCaps(g, x, y, w, h, vialEndCap);
        } else {
            drawGothicVialTrim(g, x, y, w, h);
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

    /** Тёплый тон поверх PNG — гасит холодное сине-белое свечение, ближе к золотой рамке лавки. */
    private static void applyWarmGlassTint(Graphics2D g, int x, int y, int w, int h) {
        int arc = Math.max(4, h);
        Composite saved = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.12f));
        g.setColor(new Color(58, 38, 18));
        g.fillRoundRect(x, y, w, h, arc, arc);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.08f));
        g.setColor(new Color(200, 155, 70));
        g.fillRoundRect(x + 1, y + 1, w - 2, Math.max(2, h - 2), arc - 2, arc - 2);

        g.setComposite(saved);
    }

    private static Rectangle endCapCropOf(BufferedImage img) {
        if (img == null) {
            return new Rectangle(0, 0, 0, 0);
        }
        if (cachedEndCapCrop != null) {
            return cachedEndCapCrop;
        }
        Rectangle full = ShopImageBounds.compute(img);
        int bakedCap = img.getWidth();
        int bakedH = img.getHeight();
        // Уже нарезанный bake (узкий торец) — берём целиком
        if (bakedCap <= 24 && bakedH <= 24 && bakedCap < bakedH * 2) {
            cachedEndCapCrop = new Rectangle(0, 0, bakedCap, bakedH);
            return cachedEndCapCrop;
        }
        int capW = Math.max(1, Math.round(full.width * 0.24f));
        cachedEndCapCrop = new Rectangle(full.x, full.y, capW, full.height);
        return cachedEndCapCrop;
    }

    private static void drawVialEndCaps(Graphics2D g, int x, int y, int w, int h, BufferedImage vialEndCap) {
        Rectangle crop = endCapCropOf(vialEndCap);
        if (crop.width <= 0 || crop.height <= 0) {
            drawGothicVialTrim(g, x, y, w, h);
            return;
        }

        Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int capW = Math.round(h * (crop.width / (float) crop.height));
        capW = Math.max(8, Math.min(capW, w / 3));

        drawCroppedSprite(g, vialEndCap, crop, x, y, capW, h);
        int rx = x + w - capW;
        int sx0 = crop.x;
        int sx1 = crop.x + crop.width;
        g.drawImage(vialEndCap, rx, y, rx + capW, y + h, sx1, crop.y, sx0, crop.y + crop.height, null);

        if (interp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
        }
    }

    /** Готическая обмотка только на торцах колбы — fallback без PNG. */
    private static void drawGothicVialTrim(Graphics2D g, int x, int y, int w, int h) {
        if (w < 14 || h < 6) {
            drawVialRim(g, x, y, w, h);
            return;
        }

        Color shadow = new Color(24, 14, 8);
        Color darkBrass = new Color(58, 38, 16);
        Color highlight = new Color(232, 200, 118);

        int capW = Math.max(7, Math.min(12, w / 8));
        int midX = x + capW;
        int midW = Math.max(1, w - capW * 2);
        int arc = Math.max(4, h);

        Composite saved = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        // Тонкое стекло по центру — без рамки по всей длине
        g.setColor(new Color(108, 78, 36, 200));
        g.drawRoundRect(midX, y + 1, midW, h - 2, arc - 2, arc - 2);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
        g.setColor(highlight);
        g.drawLine(midX + 2, y + 2, midX + midW - 3, y + 2);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(shadow);
        g.drawLine(midX + 2, y + h - 2, midX + midW - 3, y + h - 2);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));

        drawGothicEndWrap(g, x, y, capW, h, true);
        drawGothicEndWrap(g, x + w - capW, y, capW, h, false);

        g.setComposite(saved);
    }

    /** Торцевая накладка: металл + диагональная «обмотка» + мини-зубцы с краю. */
    private static void drawGothicEndWrap(Graphics2D g, int x, int y, int capW, int h, boolean left) {
        Color shadow = new Color(24, 14, 8);
        Color darkBrass = new Color(58, 38, 16);
        Color midBrass = new Color(118, 84, 34);
        Color brightBrass = new Color(188, 142, 52);
        Color highlight = new Color(232, 200, 118);
        int arc = Math.max(4, h);

        g.setColor(shadow);
        g.fillRoundRect(x, y, capW, h, arc, arc);
        g.setColor(darkBrass);
        g.fillRoundRect(x + 1, y + 1, capW - 2, h - 2, arc - 2, arc - 2);
        g.setColor(midBrass);
        g.fillRoundRect(x + 2, y + 2, capW - 4, h - 4, arc - 3, arc - 3);

        // «Намотка» — диагональные витки по торцу
        g.setColor(brightBrass);
        for (int i = -h; i < capW + h; i += 3) {
            int x0 = left ? x + i : x + capW - i;
            int y0 = y + 2;
            int x1 = left ? x + i + h - 4 : x + capW - i - h + 4;
            int y1 = y + h - 3;
            if (left) {
                if (x0 < x + capW && x1 > x) {
                    g.drawLine(Math.max(x + 1, x0), y0, Math.min(x + capW - 2, x1), y1);
                }
            } else {
                if (x0 > x && x1 < x + capW) {
                    g.drawLine(Math.min(x + capW - 2, x0), y0, Math.max(x + 1, x1), y1);
                }
            }
        }

        // Мини-зубцы только на внешнем крае торца
        int edgeX = left ? x : x + capW - 2;
        int toothH = Math.min(3, Math.max(2, h / 4));
        for (int ty = y + 1; ty < y + h - toothH; ty += toothH + 1) {
            g.setColor(shadow);
            g.fillRect(edgeX, ty, 2, toothH);
            g.setColor(highlight);
            g.fillRect(edgeX, ty, 2, 1);
        }

        int rivetX = left ? x + capW / 2 + 1 : x + capW / 2 - 1;
        drawEndRivet(g, rivetX, y + h / 2, h, shadow, midBrass, highlight);

        g.setColor(highlight);
        if (left) {
            g.drawLine(x + 2, y + 2, x + capW - 2, y + 2);
        } else {
            g.drawLine(x + 1, y + 2, x + capW - 3, y + 2);
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
                                             Color main, int baseValue, int newValue, int max,
                                             int animTick, int rowIndex) {
        if (w <= 0 || h <= 0 || max <= 0) {
            return;
        }

        int baseW = Math.round(w * Math.min(1f, Math.max(0, baseValue) / (float) max));
        int newW = Math.round(w * Math.min(1f, Math.max(0, newValue) / (float) max));
        int delta = newValue - baseValue;

        if (delta >= 0) {
            if (baseW > 0) {
                fillLiquid(g, x, y, baseW, h, main, animTick, rowIndex);
            }
            if (delta > 0 && newW > baseW) {
                fillLiquid(g, x + baseW, y, newW - baseW, h, DELTA_YELLOW, animTick + 7, rowIndex + 3);
            }
        } else {
            if (newW > 0) {
                fillLiquid(g, x, y, newW, h, main, animTick, rowIndex);
            }
            if (baseW > newW) {
                Color lossBlend = blendColors(main, DELTA_YELLOW, 0.55f);
                fillLiquid(g, x + newW, y, baseW - newW, h, lossBlend, animTick + 4, rowIndex + 1);
            }
        }
    }

    private static void fillLiquid(Graphics2D g, int x, int y, int width, int height, Color base,
                                   int animTick, int rowIndex) {
        if (width <= 0 || height <= 0) {
            return;
        }
        int r = Math.max(1, height / 2);

        Color deep = darken(base, 0.48f);
        Color mid = darken(base, 0.12f);
        Color body = brighten(base, 0.08f);
        Color glow = brighten(base, 0.32f);
        Color core = brighten(base, 0.52f);

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
        drawLiquidShimmers(g, x, y, width, height, brighten(base, 0.55f), animTick, rowIndex);
        drawLiquidBubbles(g, x, y, width, height, brighten(base, 0.35f), animTick, rowIndex);
        drawLiquidMeniscus(g, x, y, width, height, core, animTick);
    }

    /** Мелкие блики внутри жидкости — двигаются со временем. */
    private static void drawLiquidShimmers(Graphics2D g, int x, int y, int w, int h, Color spark,
                                           int animTick, int rowIndex) {
        int sparks = Math.max(2, w / 6);
        for (int i = 0; i < sparks; i++) {
            int seed = x * 31 + y * 17 + w * 13 + i * 23 + rowIndex * 41;
            int drift = animTick / 3 + i * 5;
            int sx = x + 2 + Math.floorMod(seed + drift, Math.max(1, w - 3));
            int sy = y + 1 + Math.floorMod(seed / 7 + animTick / 5, Math.max(1, h - 2));
            int alpha = 70 + Math.floorMod(seed / 11, 110);
            g.setColor(new Color(spark.getRed(), spark.getGreen(), spark.getBlue(), alpha));
            g.fillRect(sx, sy, 1, 1);
            if (w > 10 && i % 3 == 0) {
                g.setColor(new Color(255, 255, 255, alpha / 2));
                g.fillRect(sx + 1, sy, 1, 1);
            }
        }
    }

    /** Пузырьки внутри колбы — поднимаются вверх. */
    private static void drawLiquidBubbles(Graphics2D g, int x, int y, int w, int h, Color tint,
                                          int animTick, int rowIndex) {
        if (w < 8 || h < 6) {
            return;
        }
        for (int b = 0; b < 2; b++) {
            int period = 42 + b * 19 + rowIndex * 13;
            int phaseTick = Math.floorMod(animTick + b * 24 + rowIndex * 17, period);
            float phase = phaseTick / (float) period;
            if (phase > 0.92f) {
                continue;
            }
            int bx = x + 3 + Math.floorMod(animTick * 2 + b * 11 + rowIndex * 9, Math.max(1, w - 6));
            int by = y + h - 2 - Math.round(phase * (h - 3));
            int alpha = 90 + Math.round((1f - phase) * 100);
            int size = phase < 0.15f ? 2 : 1;
            g.setColor(new Color(255, 255, 255, alpha));
            g.fillOval(bx, by, size, size);
            g.setColor(new Color(tint.getRed(), tint.getGreen(), tint.getBlue(), alpha / 2));
            g.fillOval(bx, by + 1, size, size);
        }
    }

    private static void drawLiquidMeniscus(Graphics2D g, int x, int y, int width, int height, Color edge,
                                           int animTick) {
        g.setColor(new Color(0, 0, 0, 65));
        g.fillRect(x, y + height - 1, width, 1);
        if (width > 2) {
            g.setColor(new Color(edge.getRed(), edge.getGreen(), edge.getBlue(), 160));
            g.fillRect(x + width - 1, y + 1, 1, Math.max(1, height - 2));
            int surfaceY = y + 1;
            for (int i = 0; i < width; i++) {
                int wave = (int) (Math.sin((i + animTick * 0.22) * 0.42) * 1.1);
                int alpha = 55 + (i % 3) * 12;
                g.setColor(new Color(255, 255, 255, alpha));
                g.fillRect(x + i, surfaceY + wave, 1, 1);
            }
            g.setColor(new Color(edge.getRed(), edge.getGreen(), edge.getBlue(), 120));
            g.fillRect(x + 1, surfaceY, Math.max(1, width / 4), 1);
        }
    }

    private static void drawComparisonBar(Graphics2D g, int x, int y, int w, int h,
                                          Color main, int baseValue, int newValue, int max) {
        Shape savedClip = g.getClip();
        g.clipRect(x, y, w, h);
        drawLiquidComparison(g, x, y, w, h, main, baseValue, newValue, max, 0, 0);
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
        GameFonts.applyGothicHints(g);
    }
}
