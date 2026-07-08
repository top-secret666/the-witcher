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

    public static GdxIntroAssets load() {
        GdxIntroAssets a = new GdxIntroAssets();
        a.kaerMorhenBgTex = loadFirst(
            "sprites/screen saver/kaer_morhen_bg.png",
            "sprites/kaer_morhen_bg.png",
            "sprites/menu/menu_bg_custom.jpg",
            "sprites/menu/1x/menu_bg_custom.jpg");
        a.geraltTex = loadPortrait(
            "sprites/screen saver/geralt_portrait.png",
            "sprites/lavka/1x/geralt_portrait_shop.png",
            "sprites/lavka/geralt_portrait_shop.png");
        a.dukeTex = loadPortrait(
            "sprites/screen saver/duke_portrait.png",
            "sprites/lavka/1x/duke_portrait_shop.png",
            "sprites/lavka/duke_portrait_shop.png");
        a.strangerTex = loadPortrait(
            "sprites/screen saver/stranger_shadow.png",
            "sprites/lavka/1x/stranger.png",
            "sprites/lavka/stranger.png");
        a.geraltEmotionTex = loadPortrait(
            "sprites/screen saver/geralt_emotion.png",
            "sprites/lavka/1x/geralt_emotion_shop.png",
            "sprites/lavka/geralt_emotion_shop.png");
        a.dukeLaughTex = loadPortrait(
            "sprites/screen saver/duke_portrait_fun.png",
            "sprites/lavka/1x/duke_portrait_fun_shop.png",
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
            texW(strangerTex), texH(strangerTex),
            texW(geraltTex), texH(geraltTex),
            texW(dukeTex), texH(dukeTex),
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
