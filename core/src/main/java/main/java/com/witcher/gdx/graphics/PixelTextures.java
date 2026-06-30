package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Загрузка и отрисовка пиксель-арт спрайтов без размытия.
 */
public final class PixelTextures {

    private PixelTextures() {
    }

    /** Пробует пути по порядку; возвращает null, если файла нет. */
    public static Texture loadFirst(String... paths) {
        for (String path : paths) {
            Texture texture = loadOptional(path);
            if (texture != null) {
                return texture;
            }
        }
        return null;
    }

    public static Texture loadOptional(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        if (!Gdx.files.internal(path).exists()) {
            return null;
        }
        try {
            Texture texture = new Texture(Gdx.files.internal(path));
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return texture;
        } catch (Exception e) {
            Gdx.app.error("PixelTextures", "Ne udalos zagruzit: " + path, e);
            return null;
        }
    }

    /** Заполняет весь экран с сохранением пропорций (как cover в CSS). */
    public static void drawCover(SpriteBatch batch, Texture texture, float viewW, float viewH, float alpha) {
        if (texture == null) {
            return;
        }
        float tw = texture.getWidth();
        float th = texture.getHeight();
        if (tw <= 0f || th <= 0f) {
            return;
        }
        float scale = Math.max(viewW / tw, viewH / th);
        float drawW = tw * scale;
        float drawH = th * scale;
        float x = (viewW - drawW) * 0.5f;
        float y = (viewH - drawH) * 0.5f;
        float a = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, x, y, drawW, drawH);
        batch.setColor(1f, 1f, 1f, a);
    }

    /** Рисует спрайт в прямоугольник без искажения пропорций. */
    public static void drawFit(SpriteBatch batch, Texture texture, float x, float y, float w, float h) {
        if (texture == null || w <= 0f || h <= 0f) {
            return;
        }
        float tw = texture.getWidth();
        float th = texture.getHeight();
        if (tw <= 0f || th <= 0f) {
            return;
        }
        float srcAspect = tw / th;
        float dstAspect = w / h;
        float drawW;
        float drawH;
        if (srcAspect > dstAspect) {
            drawW = w;
            drawH = w / srcAspect;
        } else {
            drawH = h;
            drawW = h * srcAspect;
        }
        float drawX = x + (w - drawW) * 0.5f;
        float drawY = y + (h - drawH) * 0.5f;
        batch.draw(texture, drawX, drawY, drawW, drawH);
    }

    public static void drawRegion(SpriteBatch batch, TextureRegion region, float x, float y, float w, float h) {
        if (region == null || w <= 0f || h <= 0f) {
            return;
        }
        batch.draw(region, x, y, w, h);
    }

    /** Обрезанный спрайт (для HUD-плашки с пустыми полями в PNG). */
    public static void drawCropped(SpriteBatch batch, Texture texture,
                                   int srcX, int srcY, int srcW, int srcH,
                                   float x, float y, float w, float h) {
        if (texture == null || srcW <= 0 || srcH <= 0 || w <= 0f || h <= 0f) {
            return;
        }
        batch.draw(texture, x, y, w, h, srcX, srcY, srcW, srcH);
    }

    /**
     * Находит плотную область непрозрачных пикселей в PNG.
     * @return int[]{x, y, w, h} или null
     */
    public static int[] computeOpaqueBounds(String path) {
        if (path == null || path.isEmpty() || !Gdx.files.internal(path).exists()) {
            return null;
        }
        FileHandle file = Gdx.files.internal(path);
        Pixmap pixmap = new Pixmap(file);
        try {
            int w = pixmap.getWidth();
            int h = pixmap.getHeight();
            int minX = w;
            int minY = h;
            int maxX = 0;
            int maxY = 0;
            int step = Math.max(1, Math.min(w, h) / 256);
            for (int y = 0; y < h; y += step) {
                for (int x = 0; x < w; x += step) {
                    if ((pixmap.getPixel(x, y) >>> 24) > 20) {
                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }
                }
            }
            if (maxX < minX || maxY < minY) {
                return null;
            }
            return new int[]{minX, minY, maxX - minX + 1, maxY - minY + 1};
        } finally {
            pixmap.dispose();
        }
    }

    public static void dispose(Texture texture) {
        if (texture != null) {
            texture.dispose();
        }
    }
}
