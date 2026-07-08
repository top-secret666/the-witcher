package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Загрузка и отрисовка спрайтов — сглаженный масштаб ({@link RenderQuality}).
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

    /**
     * Сначала полный PNG ({@code sprites/lavka/…}), затем 1x-запасник — для сглаженного масштаба.
     */
    public static Texture loadLavka(String relativePath, String... extraFallbacks) {
        String full = "sprites/lavka/" + relativePath;
        String baked = "sprites/lavka/1x/" + relativePath;
        if (extraFallbacks == null || extraFallbacks.length == 0) {
            return loadFirst(full, baked);
        }
        String[] all = new String[2 + extraFallbacks.length];
        all[0] = full;
        all[1] = baked;
        System.arraycopy(extraFallbacks, 0, all, 2, extraFallbacks.length);
        return loadFirst(all);
    }

    /** Меню: сначала baked 1x (чёткие размеры под 480×360×scale), затем исходник. */
    public static Texture loadMenu(String relativePath) {
        return loadFirst(
            "sprites/menu/1x/" + relativePath,
            "sprites/menu/" + relativePath);
    }

    public static BufferedImage loadLavkaBufferedImage(String relativePath, String... extraFallbacks) {
        String full = "sprites/lavka/" + relativePath;
        String baked = "sprites/lavka/1x/" + relativePath;
        if (extraFallbacks == null || extraFallbacks.length == 0) {
            return loadFirstBufferedImage(full, baked);
        }
        String[] all = new String[2 + extraFallbacks.length];
        all[0] = full;
        all[1] = baked;
        System.arraycopy(extraFallbacks, 0, all, 2, extraFallbacks.length);
        return loadFirstBufferedImage(all);
    }

    public static BufferedImage loadFirstBufferedImage(String... paths) {
        for (String path : paths) {
            BufferedImage image = loadBufferedImageOptional(path);
            if (image != null && image.getWidth() > 4 && image.getHeight() > 4) {
                return image;
            }
        }
        return null;
    }

    public static BufferedImage loadBufferedImageOptional(String path) {
        FileHandle file = resolve(path);
        if (file == null) {
            return null;
        }
        Pixmap pixmap = new Pixmap(file);
        try {
            return pixmapToBufferedImage(pixmap);
        } catch (Exception e) {
            Gdx.app.error("PixelTextures", "Ne udalos prochitat PNG: " + path, e);
            return null;
        } finally {
            pixmap.dispose();
        }
    }

    public static BufferedImage pixmapToBufferedImage(Pixmap pixmap) {
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
    }

    public static LoadedTexture loadLavkaMeta(String relativePath, String... extraFallbacks) {
        String full = "sprites/lavka/" + relativePath;
        String baked = "sprites/lavka/1x/" + relativePath;
        if (extraFallbacks == null || extraFallbacks.length == 0) {
            return loadFirstMeta(full, baked);
        }
        String[] all = new String[2 + extraFallbacks.length];
        all[0] = full;
        all[1] = baked;
        System.arraycopy(extraFallbacks, 0, all, 2, extraFallbacks.length);
        return loadFirstMeta(all);
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
            Gdx.app.error("PixelTextures", "Fajl ne najden: " + path);
            return null;
        }
        try {
            Texture texture = loadTextureFromFile(file);
            RenderQuality.apply(texture);
            Gdx.app.log("PixelTextures", "OK " + path + " -> " + file.path()
                + " (" + texture.getWidth() + "x" + texture.getHeight() + ")");
            return new LoadedTexture(texture, path, file.path());
        } catch (Exception e) {
            Gdx.app.error("PixelTextures", "Ne udalos zagruzit: " + path + " -> " + file.path(), e);
            return null;
        }
    }

    private static Texture loadTextureFromFile(FileHandle file) {
        Pixmap pixmap = new Pixmap(file);
        try {
            if (pixmap.getFormat() != Pixmap.Format.RGBA8888) {
                Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
                rgba.drawPixmap(pixmap, 0, 0);
                pixmap.dispose();
                pixmap = rgba;
            }
            Texture texture = new Texture(pixmap);
            RenderQuality.apply(texture);
            return texture;
        } finally {
            pixmap.dispose();
        }
    }

    public static void resetBlend() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
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
        RenderQuality.apply(texture);
        Gdx.app.log("PixelTextures", "Sozdan zapasnoj fon lavki " + width + "x" + height);
        return texture;
    }

    /**
     * LibGDX desktop: ассеты в {@code src/main/resources/assets}.
     * Порядок: witcher.assets → cwd (если это папка assets) → src/main/resources/assets → internal.
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

        String userDir = System.getProperty("user.dir", "").replace('\\', '/');
        String assetsRoot = System.getProperty("witcher.assets", "").replace('\\', '/').trim();
        if (assetsRoot.startsWith("\"") && assetsRoot.endsWith("\"") && assetsRoot.length() > 1) {
            assetsRoot = assetsRoot.substring(1, assetsRoot.length() - 1);
        }
        if (assetsRoot.endsWith("/")) {
            assetsRoot = assetsRoot.substring(0, assetsRoot.length() - 1);
        }

        if (!assetsRoot.isEmpty()) {
            FileHandle fromProp = Gdx.files.absolute(assetsRoot + "/" + p);
            if (fromProp.exists()) {
                return fromProp;
            }
        }

        if (userDir.endsWith("/assets")) {
            FileHandle fromCwd = Gdx.files.absolute(userDir + "/" + p);
            if (fromCwd.exists()) {
                return fromCwd;
            }
        }

        FileHandle local = Gdx.files.local(p);
        if (local.exists()) {
            return local;
        }

        for (String absolute : buildCandidates(userDir, p, assetsRoot)) {
            FileHandle file = Gdx.files.absolute(absolute);
            if (file.exists()) {
                return file;
            }
        }

        // Запасной путь: мастер-копии в sprites/_source/ (пока не скопировали в sprites/)
        String sourcePath = sourceFallbackPath(p);
        if (sourcePath != null) {
            for (String absolute : buildCandidates(userDir, sourcePath, assetsRoot)) {
                FileHandle file = Gdx.files.absolute(absolute);
                if (file.exists()) {
                    Gdx.app.log("PixelTextures", "Iz _source: " + path + " -> " + file.path());
                    return file;
                }
            }
            FileHandle localSource = Gdx.files.local(sourcePath);
            if (localSource.exists()) {
                Gdx.app.log("PixelTextures", "Iz _source: " + path + " -> " + localSource.path());
                return localSource;
            }
        }

        FileHandle internal = Gdx.files.internal(p);
        if (internal.exists()) {
            return internal;
        }
        FileHandle internalAssets = Gdx.files.internal("assets/" + p);
        if (internalAssets.exists()) {
            return internalAssets;
        }
        return null;
    }

    private static String[] buildCandidates(String userDir, String path, String assetsRoot) {
        Set<String> candidates = new LinkedHashSet<>();
        if (!assetsRoot.isEmpty()) {
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

    private static String sourceFallbackPath(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }
        String p = path.replace('\\', '/');
        if (p.startsWith("sprites/")) {
            return "sprites/_source/" + p.substring("sprites/".length());
        }
        return null;
    }

    /**
     * Вписать текстуру (допускается дробный масштаб) — для крупных фонов PNG.
     */
    public static void drawContainFit(SpriteBatch batch, Texture texture, float viewW, float viewH,
                                    float sizeFactor, float alpha) {
        if (texture == null) {
            return;
        }
        float tw = texture.getWidth();
        float th = texture.getHeight();
        if (tw <= 0f || th <= 0f) {
            return;
        }
        float contain = Math.min(viewW / tw, viewH / th) * sizeFactor;
        float drawW = tw * contain;
        float drawH = th * contain;
        float x = (viewW - drawW) * 0.5f;
        float y = (viewH - drawH) * 0.5f;
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, x, y, drawW, drawH);
        batch.setColor(1f, 1f, 1f, prev);
    }

    /**
     * Вписать текстуру с целочисленным масштабом (чёткие пиксели), как Swing-сплэш.
     */
    public static void drawContainInteger(SpriteBatch batch, Texture texture, float viewW, float viewH,
                                          float sizeFactor, float alpha) {
        if (texture == null) {
            return;
        }
        float tw = texture.getWidth();
        float th = texture.getHeight();
        if (tw <= 0f || th <= 0f) {
            return;
        }
        float contain = Math.min(viewW / tw, viewH / th) * sizeFactor;
        int scale = Math.max(1, Math.round(contain));
        float drawW = tw * scale;
        float drawH = th * scale;
        float x = (viewW - drawW) * 0.5f;
        float y = (viewH - drawH) * 0.5f;
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(texture, Math.round(x), Math.round(y), Math.round(drawW), Math.round(drawH));
        batch.setColor(1f, 1f, 1f, prev);
    }

    /** Как Swing: вписать фон с полями, не cover на весь экран. */
    public static void drawContain(SpriteBatch batch, Texture texture, float viewW, float viewH, float alpha, float margin) {
        if (texture == null) {
            return;
        }
        float tw = texture.getWidth();
        float th = texture.getHeight();
        if (tw <= 0f || th <= 0f) {
            return;
        }
        float a = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        float contain = Math.min(viewW / tw, viewH / th) * margin;
        float drawW = tw * contain;
        float drawH = th * contain;
        float x = (viewW - drawW) * 0.5f;
        float y = (viewH - drawH) * 0.5f;
        batch.draw(texture, x, y, drawW, drawH);
        batch.setColor(1f, 1f, 1f, a);
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
        float a = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        float cover = Math.max(viewW / tw, viewH / th);
        float drawW = tw * cover;
        float drawH = th * cover;
        float x = (viewW - drawW) * 0.5f;
        float y = (viewH - drawH) * 0.5f;
        batch.draw(texture, x, y, drawW, drawH);
        batch.setColor(1f, 1f, 1f, a);
    }

    /** Cover, но нижний край картинки прижат к низу экрана (splash_bg с пустым верхом в PNG). */
    public static void drawCoverBottom(SpriteBatch batch, Texture texture, float viewW, float viewH, float alpha) {
        if (texture == null) {
            return;
        }
        float tw = texture.getWidth();
        float th = texture.getHeight();
        if (tw <= 0f || th <= 0f) {
            return;
        }
        float a = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        float cover = Math.max(viewW / tw, viewH / th);
        float drawW = tw * cover;
        float drawH = th * cover;
        float x = (viewW - drawW) * 0.5f;
        float y = 0f;
        batch.draw(texture, x, y, drawW, drawH);
        batch.setColor(1f, 1f, 1f, a);
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
        TextureRegion region = new TextureRegion(texture, cropX, srcY, cropW, cropH);
        batch.draw(region, x, y, w, h);
    }

    /** Игнорирует прозрачные и почти чёрные пиксели (чёрный фон PNG). */
    public static int[] computeVisibleBounds(String path) {
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
                    int rgba = pixmap.getPixel(px, py);
                    int a = rgba & 0xff;
                    if (a <= 20) {
                        continue;
                    }
                    int r = (rgba >>> 24) & 0xff;
                    int g = (rgba >>> 16) & 0xff;
                    int b = (rgba >>> 8) & 0xff;
                    if (r < 24 && g < 24 && b < 24) {
                        continue;
                    }
                    minX = Math.min(minX, px);
                    minY = Math.min(minY, py);
                    maxX = Math.max(maxX, px);
                    maxY = Math.max(maxY, py);
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

    /** Как Swing {@code computeContentBounds} — по непрозрачным пикселям. */
    public static int[] computeContentBounds(String path) {
        return computeOpaqueBounds(path);
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
                    int rgba = pixmap.getPixel(px, py);
                    if ((rgba & 0xff) > 20) {
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
