package main.java.com.witcher.ui.graphics;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

/** Общие подсказки Java2D для пиксель-арта (аналог Nearest в LibGDX). */
final class PixelDraw {

    private PixelDraw() {
    }

    static void applyNearest(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
    }

    /**
     * Целочисленный апскейл без drawImage — Java2D на Windows иногда всё равно сглаживает.
     */
    static void blitIntegerScale(BufferedImage src, BufferedImage dst, int scale) {
        if (src == null || dst == null || scale < 1) {
            return;
        }
        int sw = src.getWidth();
        int sh = src.getHeight();
        int dw = dst.getWidth();
        int[] sBuf = ((DataBufferInt) src.getRaster().getDataBuffer()).getData();
        int[] dBuf = ((DataBufferInt) dst.getRaster().getDataBuffer()).getData();
        int bg = 0xFF000000;
        java.util.Arrays.fill(dBuf, bg);

        for (int y = 0; y < sh; y++) {
            int srcRow = y * sw;
            int dy0 = y * scale;
            for (int x = 0; x < sw; x++) {
                int argb = sBuf[srcRow + x];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int rgb = a == 255 ? argb : blendOnBlack(argb, a);
                int dx0 = x * scale;
                for (int oy = 0; oy < scale; oy++) {
                    int dRow = (dy0 + oy) * dw + dx0;
                    for (int ox = 0; ox < scale; ox++) {
                        dBuf[dRow + ox] = rgb;
                    }
                }
            }
        }
    }

    private static int blendOnBlack(int argb, int a) {
        int r = ((argb >>> 16) & 0xFF) * a / 255;
        int g = ((argb >>> 8) & 0xFF) * a / 255;
        int b = (argb & 0xFF) * a / 255;
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
