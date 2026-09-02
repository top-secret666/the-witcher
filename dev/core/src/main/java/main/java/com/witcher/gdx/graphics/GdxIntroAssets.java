package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;
import main.java.com.witcher.ui.intro.IntroAssetsInfo;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Ассеты интро-заставки для LibGDX.
 */
public final class GdxIntroAssets implements Disposable {

    public Texture kaerMorhenBgTex;
    public Texture geraltTex;
    public Texture dukeTex;
    public Texture strangerTex;
    public Texture geraltEmotionTex;
    public Texture dukeLaughTex;
    public Texture geraltShopTex;
    public Texture dukeShopTex;
    public Texture geraltEmotionShopTex;
    public Texture dukeLaughShopTex;
    public Texture merchantBgTex;
    public Texture cursorTex;
    public Texture[] shopMaterializeFrames;
    public int[] shopMaterializeDelaysMs;

    public int geraltLogicalW;
    public int geraltLogicalH;
    public int dukeLogicalW;
    public int dukeLogicalH;
    public int strangerLogicalW;
    public int strangerLogicalH;

    public static GdxIntroAssets load() {
        GdxIntroAssets a = new GdxIntroAssets();
        a.kaerMorhenBgTex = loadFirst(
            "sprites/screen saver/kaer_morhen_bg.png",
            "sprites/kaer_morhen_bg.png",
            "sprites/menu/menu_bg_custom.jpg",
            "sprites/menu/1x/menu_bg_custom.jpg");
        PortraitLoad geralt = loadIntroPortrait(
            "sprites/screen saver/geralt_portrait.png",
            "sprites/lavka/geralt_portrait_shop.png");
        a.geraltTex = geralt.texture;
        a.geraltLogicalW = geralt.w;
        a.geraltLogicalH = geralt.h;
        PortraitLoad duke = loadIntroPortrait(
            "sprites/screen saver/duke_portrait.png",
            "sprites/lavka/duke_portrait_shop.png");
        a.dukeTex = duke.texture;
        a.dukeLogicalW = duke.w;
        a.dukeLogicalH = duke.h;
        PortraitLoad stranger = loadIntroPortrait(
            "sprites/screen saver/stranger_shadow.png");
        a.strangerTex = stranger.texture;
        a.strangerLogicalW = stranger.w;
        a.strangerLogicalH = stranger.h;
        a.geraltEmotionTex = loadPortrait(
            "sprites/screen saver/geralt_emotion.png",
            "sprites/lavka/geralt_emotion_shop.png");
        a.dukeLaughTex = loadPortrait(
            "sprites/screen saver/duke_portrait_fun.png",
            "sprites/lavka/duke_portrait_fun_shop.png");
        a.geraltShopTex = PixelTextures.loadLavka("geralt_portrait_shop.png");
        a.dukeShopTex = PixelTextures.loadLavka("duke_portrait_shop.png");
        a.geraltEmotionShopTex = PixelTextures.loadLavka("geralt_emotion_shop.png");
        a.dukeLaughShopTex = PixelTextures.loadLavka("duke_portrait_fun_shop.png");
        a.merchantBgTex = PixelTextures.loadLavka("merchant_bg_lavka.png", "lavka.png");
        a.cursorTex = PixelTextures.loadMenu("menu_cursor.png");
        loadShopMaterializeGif(a);
        return a;
    }

    private static final class PortraitLoad {
        final Texture texture;
        final int w;
        final int h;

        PortraitLoad(Texture texture, int w, int h) {
            this.texture = texture;
            this.w = w;
            this.h = h;
        }
    }

    private static PortraitLoad loadIntroPortrait(String... paths) {
        for (String path : paths) {
            FileHandle file = PixelTextures.resolve(path);
            if (file == null || !file.exists()) {
                continue;
            }
            Pixmap source = new Pixmap(file);
            try {
                source = ensureRgba(source);
                stripNearBlack(source);
                Pixmap trimmed = trimTransparent(source);
                if (trimmed != source) {
                    source.dispose();
                }
                Texture texture = new Texture(trimmed);
                RenderQuality.apply(texture);
                trimmed.dispose();
                com.badlogic.gdx.Gdx.app.log("GdxIntroAssets", "Portrait OK " + path
                    + " -> " + texture.getWidth() + "x" + texture.getHeight());
                return new PortraitLoad(texture, texture.getWidth(), texture.getHeight());
            } catch (RuntimeException e) {
                source.dispose();
                com.badlogic.gdx.Gdx.app.error("GdxIntroAssets", "Portrait fail " + path, e);
            }
        }
        return new PortraitLoad(null, 0, 0);
    }

    private static Pixmap ensureRgba(Pixmap pixmap) {
        if (pixmap.getFormat() == Pixmap.Format.RGBA8888) {
            return pixmap;
        }
        Pixmap rgba = new Pixmap(pixmap.getWidth(), pixmap.getHeight(), Pixmap.Format.RGBA8888);
        rgba.drawPixmap(pixmap, 0, 0);
        pixmap.dispose();
        return rgba;
    }

    /** LibGDX RGBA8888: R в старшем байте, A в младшем — как в {@link GdxMenuAssets}. */
    private static void stripNearBlack(Pixmap pixmap) {
        int w = pixmap.getWidth();
        int h = pixmap.getHeight();
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgba = pixmap.getPixel(x, y);
                int a = rgba & 0xFF;
                int r = (rgba >>> 24) & 0xFF;
                int g = (rgba >>> 16) & 0xFF;
                int b = (rgba >>> 8) & 0xFF;
                if (a == 0 || (r < 18 && g < 18 && b < 18)) {
                    pixmap.drawPixel(x, y, 0);
                }
            }
        }
    }

    private static Pixmap trimTransparent(Pixmap src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int minX = w;
        int minY = h;
        int maxX = -1;
        int maxY = -1;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int a = src.getPixel(x, y) & 0xFF;
                if (a > 4) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < minX || maxY < minY) {
            return src;
        }
        int tw = maxX - minX + 1;
        int th = maxY - minY + 1;
        Pixmap out = new Pixmap(tw, th, Pixmap.Format.RGBA8888);
        out.drawPixmap(src, 0, 0, minX, minY, tw, th);
        return out;
    }

    private static Texture loadFirst(String... paths) {
        for (String path : paths) {
            Texture tex = PixelTextures.loadOptional(path);
            if (tex != null) {
                return tex;
            }
        }
        return null;
    }

    private static Texture loadPortrait(String... paths) {
        return loadFirst(paths);
    }

    private static void loadShopMaterializeGif(GdxIntroAssets a) {
        String[] candidates = {
            "sprites/screen saver/shop_materialize.gif",
            "sprites/screen saver/59f8bef1-2321-427a-80b3-56655d3e1e4b.gif",
            "sprites/lavka/shop_materialize.gif",
            "sprites/lavka/shop_materialize_v2.gif"
        };
        for (String path : candidates) {
            GifData gif = loadGifFrames(path);
            if (gif != null && gif.frames.length > 0) {
                a.shopMaterializeFrames = gif.frames;
                a.shopMaterializeDelaysMs = gif.delays;
                return;
            }
        }
        a.shopMaterializeFrames = new Texture[0];
        a.shopMaterializeDelaysMs = new int[0];
    }

    private static final class GifData {
        final Texture[] frames;
        final int[] delays;

        GifData(Texture[] frames, int[] delays) {
            this.frames = frames;
            this.delays = delays;
        }
    }

    private static GifData loadGifFrames(String path) {
        FileHandle file = PixelTextures.resolve(path);
        if (file == null || !file.exists()) {
            return null;
        }
        try {
            ImageInputStream iis = ImageIO.createImageInputStream(file.read());
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) {
                return null;
            }
            ImageReader reader = readers.next();
            reader.setInput(iis);
            int count = reader.getNumImages(true);
            if (count == 0) {
                return null;
            }
            List<Texture> frames = new ArrayList<>();
            List<Integer> delays = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                BufferedImage raw = reader.read(i);
                if (raw == null) {
                    continue;
                }
                Texture tex = bufferedImageToTexture(raw);
                if (tex != null) {
                    frames.add(tex);
                    delays.add(extractGifDelay(reader, i));
                }
            }
            reader.dispose();
            iis.close();
            if (frames.isEmpty()) {
                return null;
            }
            Texture[] texArr = frames.toArray(new Texture[0]);
            int[] delayArr = new int[delays.size()];
            for (int i = 0; i < delays.size(); i++) {
                delayArr[i] = delays.get(i);
            }
            return new GifData(texArr, delayArr);
        } catch (Exception e) {
            com.badlogic.gdx.Gdx.app.error("GdxIntroAssets", "GIF load failed: " + path, e);
            return null;
        }
    }

    private static int extractGifDelay(ImageReader reader, int frameIndex) {
        int delayMs = 80;
        try {
            javax.imageio.metadata.IIOMetadata meta = reader.getImageMetadata(frameIndex);
            String metaFmt = meta.getNativeMetadataFormatName();
            org.w3c.dom.Node tree = meta.getAsTree(metaFmt);
            org.w3c.dom.NodeList children = tree.getChildNodes();
            for (int n = 0; n < children.getLength(); n++) {
                org.w3c.dom.Node node = children.item(n);
                if ("GraphicControlExtension".equals(node.getNodeName())) {
                    org.w3c.dom.NamedNodeMap attrs = node.getAttributes();
                    org.w3c.dom.Node delayNode = attrs.getNamedItem("delayTime");
                    if (delayNode != null) {
                        delayMs = Integer.parseInt(delayNode.getNodeValue()) * 10;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return delayMs < 20 ? 80 : delayMs;
    }

    private static Texture bufferedImageToTexture(BufferedImage image) {
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
            RenderQuality.apply(texture);
            return texture;
        } finally {
            pixmap.dispose();
        }
    }

    public IntroAssetsInfo buildAssetsInfo() {
        return new IntroAssetsInfo(
            strangerLogicalW, strangerLogicalH,
            geraltLogicalW, geraltLogicalH,
            dukeLogicalW, dukeLogicalH,
            shopMaterializeFrames != null ? shopMaterializeFrames.length : 0,
            shopMaterializeDelaysMs,
            shopMaterializeFrames != null && shopMaterializeFrames.length > 0,
            merchantBgTex != null,
            geraltEmotionTex != null,
            dukeLaughTex != null,
            geraltShopTex != null,
            dukeShopTex != null,
            geraltEmotionShopTex != null,
            dukeLaughShopTex != null);
    }

    private static int texW(Texture t) {
        return t != null ? t.getWidth() : 0;
    }

    private static int texH(Texture t) {
        return t != null ? t.getHeight() : 0;
    }

    @Override
    public void dispose() {
        disposeTex(kaerMorhenBgTex);
        disposeTex(geraltTex);
        disposeTex(dukeTex);
        disposeTex(strangerTex);
        disposeTex(geraltEmotionTex);
        disposeTex(dukeLaughTex);
        disposeTex(geraltShopTex);
        disposeTex(dukeShopTex);
        disposeTex(geraltEmotionShopTex);
        disposeTex(dukeLaughShopTex);
        disposeTex(merchantBgTex);
        disposeTex(cursorTex);
        if (shopMaterializeFrames != null) {
            for (Texture t : shopMaterializeFrames) {
                disposeTex(t);
            }
        }
        kaerMorhenBgTex = null;
        geraltTex = null;
        dukeTex = null;
        strangerTex = null;
        geraltEmotionTex = null;
        dukeLaughTex = null;
        geraltShopTex = null;
        dukeShopTex = null;
        geraltEmotionShopTex = null;
        dukeLaughShopTex = null;
        merchantBgTex = null;
        cursorTex = null;
        shopMaterializeFrames = null;
        shopMaterializeDelaysMs = null;
    }

    private static void disposeTex(Texture t) {
        if (t != null) {
            t.dispose();
        }
    }
}
