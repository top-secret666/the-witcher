package main.java.com.witcher.ui.graphics;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Пиксель-арт даунскейл без размытия: повторное /2 и nearest-neighbor (как Nearest в LibGDX).
 */
public final class PixelScaler {

    private PixelScaler() {
    }

    public static BufferedImage crispScale(BufferedImage src, int dstW, int dstH) {
        if (src == null || dstW <= 0 || dstH <= 0) {
            return src;
        }
        if (src.getWidth() == dstW && src.getHeight() == dstH) {
            return src;
        }

        BufferedImage work = src;
        while (work.getWidth() > dstW * 2 && work.getHeight() > dstH * 2) {
            work = half(work);
        }

        BufferedImage out = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        applyNearest(g);
        g.drawImage(work, 0, 0, dstW, dstH, null);
        g.dispose();
        return out;
    }

    public static BufferedImage crispScaleRegion(BufferedImage src, Rectangle crop, int dstW, int dstH) {
        if (src == null || crop == null || crop.width <= 0 || crop.height <= 0) {
            return crispScale(src, dstW, dstH);
        }
        BufferedImage region = src.getSubimage(crop.x, crop.y, crop.width, crop.height);
        return crispScale(region, dstW, dstH);
    }

    /** Сглаженный даунскейл оригиналов (bilinear) — для иконок товаров, не пиксель-арт UI. */
    public static BufferedImage smoothScale(BufferedImage src, int dstW, int dstH) {
        if (src == null || dstW <= 0 || dstH <= 0) {
            return src;
        }
        if (src.getWidth() == dstW && src.getHeight() == dstH) {
            return src;
        }
        BufferedImage out = new BufferedImage(dstW, dstH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        applyBilinear(g);
        g.drawImage(src, 0, 0, dstW, dstH, null);
        g.dispose();
        return out;
    }

    public static BufferedImage smoothScaleRegion(BufferedImage src, Rectangle crop, int dstW, int dstH) {
        if (src == null || crop == null || crop.width <= 0 || crop.height <= 0) {
            return smoothScale(src, dstW, dstH);
        }
        BufferedImage region = src.getSubimage(crop.x, crop.y, crop.width, crop.height);
        return smoothScale(region, dstW, dstH);
    }

    /** Вписать в квадрат maxSize×maxSize с сохранением пропорций. */
    public static BufferedImage smoothScaleUniform(BufferedImage src, int maxSize) {
        if (src == null || maxSize <= 0) {
            return src;
        }
        int w = src.getWidth();
        int h = src.getHeight();
        if (w <= maxSize && h <= maxSize) {
            return src;
        }
        float scale = Math.min((float) maxSize / w, (float) maxSize / h);
        int dstW = Math.max(1, Math.round(w * scale));
        int dstH = Math.max(1, Math.round(h * scale));
        return smoothScale(src, dstW, dstH);
    }

    /** Целочисленный даунскейл: crop / scaleFactor. */
    public static BufferedImage crispScaleInteger(BufferedImage src, Rectangle crop, int scaleFactor) {
        if (src == null || crop == null || scaleFactor < 1) {
            return src;
        }
        int dstW = crop.width / scaleFactor;
        int dstH = crop.height / scaleFactor;
        if (dstW < 1 || dstH < 1) {
            return crispScaleRegion(src, crop, Math.max(1, dstW), Math.max(1, dstH));
        }
        return crispScaleRegion(src, crop, dstW, dstH);
    }

    public static int bestIntegerScale(int srcSize, int maxDst) {
        if (srcSize <= 0 || maxDst <= 0) {
            return 1;
        }
        int scale = Math.max(1, (int) Math.ceil((float) srcSize / maxDst));
        while (srcSize / scale > maxDst) {
            scale++;
        }
        return scale;
    }

    private static BufferedImage half(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int nw = Math.max(1, w / 2);
        int nh = Math.max(1, h / 2);
        BufferedImage out = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        applyNearest(g);
        g.drawImage(src, 0, 0, nw, nh, 0, 0, w, h, null);
        g.dispose();
        return out;
    }

    private static void applyNearest(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    private static void applyBilinear(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }
}
