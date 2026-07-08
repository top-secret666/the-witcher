package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.TextureData;
import com.badlogic.gdx.graphics.Texture.TextureFilter;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.IdentityHashMap;
import java.util.Map;

/** Конвертация LibGDX-текстур ↔ BufferedImage для общего presenter. */
public final class GdxTextureBridge {

    private static final Map<BufferedImage, Texture> IMAGE_TO_TEXTURE = new IdentityHashMap<>();

    private GdxTextureBridge() {
    }

    public static BufferedImage toBufferedImage(Texture texture) {
        if (texture == null) {
            return null;
        }
        TextureData data = texture.getTextureData();
        if (!data.isPrepared()) {
            data.prepare();
        }
        Pixmap pixmap = data.consumePixmap();
        try {
            int w = pixmap.getWidth();
            int h = pixmap.getHeight();
            BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    int rgba = pixmap.getPixel(x, y);
                    int a = (rgba >>> 24) & 0xff;
                    int r = (rgba >>> 16) & 0xff;
                    int g = (rgba >>> 8) & 0xff;
                    int b = rgba & 0xff;
                    pixels[y * w + x] = (a << 24) | (r << 16) | (g << 8) | b;
                }
            }
            return image;
        } finally {
            if (texture.getTextureData().disposePixmap()) {
                pixmap.dispose();
            }
        }
    }

    /** Кэш по identity — для отрисовки BufferedImage из presenter. */
    public static Texture toTexture(BufferedImage image) {
        if (image == null) {
            return null;
        }
        Texture cached = IMAGE_TO_TEXTURE.get(image);
        if (cached != null) {
            return cached;
        }
        int w = image.getWidth();
        int h = image.getHeight();
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
            IMAGE_TO_TEXTURE.put(image, texture);
            return texture;
        } finally {
            pixmap.dispose();
        }
    }

    public static void disposeCachedTextures() {
        for (Texture texture : IMAGE_TO_TEXTURE.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }
        IMAGE_TO_TEXTURE.clear();
    }
}
