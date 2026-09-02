package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.graphics.RenderQuality;
import main.java.com.witcher.ui.shop.ShopIcon;

import java.awt.image.BufferedImage;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * Кэш иконок лавки для LibGDX — hot-path отрисовки без {@code GdxTextureBridge}.
 */
public final class GdxShopIcons {

    private static final Map<BufferedImage, Texture> CACHE = new IdentityHashMap<>();

    private GdxShopIcons() {
    }

    public static Texture textureFor(ShopIcon icon) {
        return icon == null ? null : textureFor(icon.asBufferedImage());
    }

    public static Texture textureFor(BufferedImage image) {
        if (image == null) {
            return null;
        }
        Texture cached = CACHE.get(image);
        if (cached != null) {
            return cached;
        }
        int w = image.getWidth();
        int h = image.getHeight();
        if (w <= 0 || h <= 0) {
            return null;
        }
        Pixmap pixmap = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        try {
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int argb = image.getRGB(x, y);
                    int a = (argb >>> 24) & 0xff;
                    int r = (argb >>> 16) & 0xff;
                    int g = (argb >>> 8) & 0xff;
                    int b = argb & 0xff;
                    pixmap.setColor(r / 255f, g / 255f, b / 255f, a / 255f);
                    pixmap.drawPixel(x, h - 1 - y);
                }
            }
            Texture texture = new Texture(pixmap);
            texture.setFilter(RenderQuality.MIN, RenderQuality.MAG);
            CACHE.put(image, texture);
            return texture;
        } finally {
            pixmap.dispose();
        }
    }

    public static void disposeAll() {
        for (Texture texture : CACHE.values()) {
            PixelTextures.dispose(texture);
        }
        CACHE.clear();
    }
}
