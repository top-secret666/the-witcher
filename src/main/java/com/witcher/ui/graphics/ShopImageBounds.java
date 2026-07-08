package main.java.com.witcher.ui.graphics;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** Обрезка непрозрачного содержимого спрайта — без привязки к экрану лавки. */
public final class ShopImageBounds {

    private ShopImageBounds() {
    }

    public static Rectangle compute(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int minX = w;
        int minY = h;
        int maxX = 0;
        int maxY = 0;
        int step = Math.max(1, Math.min(w, h) / 256);
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                int argb = img.getRGB(x, y);
                int a = (argb >>> 24) & 0xff;
                if (a <= 20) {
                    continue;
                }
                int r = (argb >>> 16) & 0xff;
                int g = (argb >>> 8) & 0xff;
                int b = argb & 0xff;
                if (r < 24 && g < 24 && b < 24) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) {
            return new Rectangle(0, 0, w, h);
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }
}
