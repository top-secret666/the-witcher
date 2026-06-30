package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Загрузка и отрисовка пиксель-арт спрайтов без размытия.
 * Ищет файлы в рабочей папке (assets), в src/main/resources и в out/production.
 */
public final class PixelTextures {

    public static final class LoadedTexture {
        public final Texture texture;
        public final String logicalPath;
        public final String filePath;

        LoadedTexture(Texture texture, String logicalPath, String filePath) {
            this.texture = texture;
            this.logicalPath = logicalPath;
            this.filePath = filePath;
        }
    }

    private PixelTextures() {
    }

    public static Texture loadFirst(String... paths) {
        LoadedTexture loaded = loadFirstMeta(paths);
        return loaded != null ? loaded.texture : null;
    }

    public static LoadedTexture loadFirstMeta(String... paths) {
        for (String path : paths) {
            LoadedTexture loaded = loadOptionalMeta(path);
            if (loaded != null && isUsableImage(loaded.texture)) {
                return loaded;
            }
            dispose(loaded != null ? loaded.texture : null);
        }
        return null;
    }

    public static Texture loadOptional(String path) {
        LoadedTexture loaded = loadOptionalMeta(path);
        return loaded != null ? loaded.texture : null;
    }

    public static LoadedTexture loadOptionalMeta(String path) {
        FileHandle file = resolve(path);
        if (file == null) {
            return null;
        }
        try {
            Texture texture = new Texture(file);
            texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return new LoadedTexture(texture, path, file.path());
        } catch (Exception e) {
            Gdx.app.error("PixelTextures", "Ne udalos zagruzit: " + path + " -> " + file.path(), e);
            return null;
        }
    }

    private static boolean isUsableImage(Texture texture) {
        return texture != null && texture.getWidth() > 4 && texture.getHeight() > 4;
    }

    /** Запасной фон лавки, если PNG не найден или GPU не загрузил текстуру. */
    public static Texture createFallbackShopBg(int width, int height) {
        Pixmap pixmap = new Pixmap(width, height, Pixmap.Format.RGB888);
        for (int y = 0; y < height; y++) {
            float t = (float) y / Math.max(1, height - 1);
            int r = (int) (28 + t * 18 + Math.sin(y * 0.08f) * 4);
            int g = (int) (20 + t * 12 + Math.sin(y * 0.11f) * 3);
            int b = (int) (14 + t * 8);
            pixmap.setColor(r / 255f, g / 255f, b / 255f, 1f);
            pixmap.drawLine(0, y, width, y);
        }
        for (int i = 0; i < 120; i++) {
            int x = (int) (Math.abs(Math.sin(i * 17.3f)) * (width - 2));
            int y = (int) (Math.abs(Math.cos(i * 11.7f)) * (height - 2));
            pixmap.setColor(0.18f, 0.12f, 0.07f, 1f);
            pixmap.drawPixel(x, y);
        }
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        Gdx.app.log("PixelTextures", "Sozdan zapasnoj fon lavki " + width + "x" + height);
        return texture;
    }

    /**
     * LibGDX desktop: ассеты лежат в папке assets (cwd), не в classpath.
     * Порядок: local (cwd) → абсолютные пути проекта → internal.
     */
    public static FileHandle resolve(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String p = path.replace('\\', '/');
        if (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (p.startsWith("assets/")) {
            p = p.substring("assets/".length());
        }

        FileHandle local = Gdx.files.local(p);
        if (local.exists()) {
            return local;
        }

        String userDir = System.getProperty("user.dir", "").replace('\\', '/');
        String[] candidates = buildCandidates(userDir, p);
        for (String absolute : candidates) {
            FileHandle file = Gdx.files.absolute(absolute);
            if (file.exists()) {
                return file;
            }
        }

        FileHandle internal = Gdx.files.internal(p);
        if (internal.exists()) {
            return internal;
        }
        return null;
    }

    private static String[] buildCandidates(String userDir, String path) {
        Set<String> candidates = new LinkedHashSet<>();
        String assetsRoot = System.getProperty("witcher.assets", "").replace('\\', '/');
        if (!assetsRoot.isEmpty()) {
            if (assetsRoot.endsWith("/")) {
                assetsRoot = assetsRoot.substring(0, assetsRoot.length() - 1);
            }
            candidates.add(assetsRoot + "/" + path);
        }

        if (userDir.endsWith("/assets")) {
            candidates.add(userDir + "/" + path);
            candidates.add(userDir + "/../../../out/production/the-witcher/assets/" + path);
            candidates.add(userDir + "/../../../src/main/resources/assets/" + path);
        } else {
            candidates.add(userDir + "/src/main/resources/assets/" + path);
            candidates.add(userDir + "/assets/" + path);
            candidates.add(userDir + "/out/production/the-witcher/assets/" + path);
        }

        return candidates.toArray(new String[0]);
    }

    public static void drawCover(SpriteBatch batch, Texture texture, float viewW, float viewH, float alpha) {
        if (texture == null) {
            return;
        }
        float tw = texture.getWidth();
        float th = texture.getHeight();
        if (tw <= 0f || th <= 0f) {
            Gdx.app.error("PixelTextures", "drawCover: pustaya tekstura " + tw + "x" + th);
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

    /** Обрезанный спрайт; cropX/cropY — от верхнего левого угла PNG. */
    public static void drawCropped(SpriteBatch batch, Texture texture,
                                   int cropX, int cropY, int cropW, int cropH,
                                   float x, float y, float w, float h) {
        if (texture == null || cropW <= 0 || cropH <= 0 || w <= 0f || h <= 0f) {
            return;
        }
        int texH = texture.getHeight();
        int srcY = texH - cropY - cropH;
        batch.draw(texture, x, y, w, h, cropX, srcY, cropW, cropH);
    }

    public static int[] computeOpaqueBounds(String path) {
        FileHandle file = resolve(path);
        if (file == null) {
            return null;
        }
        Pixmap pixmap = new Pixmap(file);
        try {
            int w = pixmap.getWidth();
            int h = pixmap.getHeight();
            int minX = w;
            int minY = h;
            int maxX = 0;
            int maxY = 0;
            int step = Math.max(1, Math.min(w, h) / 256);
            for (int py = 0; py < h; py += step) {
                for (int px = 0; px < w; px += step) {
                    if ((pixmap.getPixel(px, py) >>> 24) > 20) {
                        minX = Math.min(minX, px);
                        minY = Math.min(minY, py);
                        maxX = Math.max(maxX, px);
                        maxY = Math.max(maxY, py);
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
