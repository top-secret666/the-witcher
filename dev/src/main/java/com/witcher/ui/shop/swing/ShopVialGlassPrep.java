package main.java.com.witcher.ui.shop.swing;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Лист прозрачных колб с чёрным фоном: фон ключуем в alpha без правки исходного PNG.
 * Жидкость рисуется под этим стеклом.
 */
public final class ShopVialGlassPrep {

    private static final int BLACK_KEY = 18;

    private ShopVialGlassPrep() {
    }

    /** Средняя колба с листа (или {@code null}, если файла нет). */
    public static BufferedImage loadMiddleGlass(BufferedImage sheet) {
        BufferedImage[] bands = extractBands(sheet);
        if (bands.length == 0) {
            return null;
        }
        return bands[Math.min(1, bands.length - 1)];
    }

    public static BufferedImage[] extractBands(BufferedImage sheet) {
        if (sheet == null) {
            return new BufferedImage[0];
        }
        int w = sheet.getWidth();
        int h = sheet.getHeight();
        boolean[] rowHit = new boolean[h];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x += 2) {
                if (!isBlackKey(sheet.getRGB(x, y))) {
                    rowHit[y] = true;
                    break;
                }
            }
        }
        List<int[]> yBands = new ArrayList<>();
        int start = -1;
        for (int y = 0; y < h; y++) {
            if (rowHit[y] && start < 0) {
                start = y;
            }
            if ((!rowHit[y] || y == h - 1) && start >= 0) {
                int end = rowHit[y] && y == h - 1 ? y : y - 1;
                if (end >= start) {
                    yBands.add(new int[]{start, end});
                }
                start = -1;
            }
        }
        List<BufferedImage> out = new ArrayList<>();
        for (int[] band : yBands) {
            int y0 = band[0];
            int y1 = band[1];
            int x0 = w;
            int x1 = 0;
            for (int y = y0; y <= y1; y++) {
                for (int x = 0; x < w; x++) {
                    if (!isBlackKey(sheet.getRGB(x, y))) {
                        x0 = Math.min(x0, x);
                        x1 = Math.max(x1, x);
                    }
                }
            }
            if (x1 < x0) {
                continue;
            }
            int bw = x1 - x0 + 1;
            int bh = y1 - y0 + 1;
            BufferedImage bandImg = new BufferedImage(bw, bh, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < bh; y++) {
                for (int x = 0; x < bw; x++) {
                    int rgb = sheet.getRGB(x0 + x, y0 + y);
                    if (isBlackKey(rgb)) {
                        bandImg.setRGB(x, y, 0);
                    } else {
                        bandImg.setRGB(x, y, 0xff000000 | (rgb & 0xffffff));
                    }
                }
            }
            out.add(bandImg);
        }
        return out.toArray(new BufferedImage[0]);
    }

    private static boolean isBlackKey(int argb) {
        int r = (argb >>> 16) & 0xff;
        int g = (argb >>> 8) & 0xff;
        int b = argb & 0xff;
        return r <= BLACK_KEY && g <= BLACK_KEY && b <= BLACK_KEY;
    }
}
