package main.java.com.witcher.gdx.bridge;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import main.java.com.witcher.gdx.graphics.PixelTextures;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/** LibGDX-запекание HUD и оборота карточки лавки. */
public final class GdxShopUiBaker {

    public static final String KEY_CARD_BACK = "shop_card_back@166x249";
    public static final String KEY_HUD_CROWN = "hud.icon_crown@18";
    public static final String KEY_HUD_DUKE_SEAL = "hud.icon_duke_seal@32";

    public static final int HUD_CROWN_PX = 18;
    public static final int HUD_DUKE_SEAL_PX = 32;

    /** Размер карточки в режиме категории (как {@code ShopLayout#leftCategoryCardSlot}). */
    public static final int CATEGORY_CARD_W = 166;
    public static final int CATEGORY_CARD_H = 249;

    private GdxShopUiBaker() {
    }

    public static Map<String, BufferedImage> bake() {
        Map<String, BufferedImage> out = new HashMap<>();
        SpriteBatch batch = new SpriteBatch();
        try {
            putIfUsable(out, KEY_CARD_BACK,
                bakeUi(batch, "ui/shop_card_back.png", CATEGORY_CARD_W, CATEGORY_CARD_H));
            putIfUsable(out, KEY_HUD_CROWN, bakeIcon(batch, "icon_crown.png", HUD_CROWN_PX));
            putIfUsable(out, KEY_HUD_DUKE_SEAL, bakeIcon(batch, "icon_duke_seal.png", HUD_DUKE_SEAL_PX));
        } finally {
            batch.dispose();
        }
        return out;
    }

    private static void putIfUsable(Map<String, BufferedImage> out, String key, BufferedImage image) {
        if (GdxIconBaker.isUsable(image)) {
            out.put(key, image);
        }
    }

    private static BufferedImage bakeUi(SpriteBatch batch, String relativePath, int dstW, int dstH) {
        PixelTextures.LoadedTexture loaded = PixelTextures.loadFirstMeta("sprites/lavka/" + relativePath);
        if (loaded == null || loaded.texture == null) {
            return null;
        }
        try {
            TextureRegion region = new TextureRegion(loaded.texture);
            return GdxTextureDownscaler.bake(batch, region, dstW, dstH);
        } finally {
            PixelTextures.dispose(loaded.texture);
        }
    }

    private static BufferedImage bakeIcon(SpriteBatch batch, String fileName, int size) {
        PixelTextures.LoadedTexture loaded = PixelTextures.loadLavkaCategoryIconMeta(fileName);
        if (loaded == null || loaded.texture == null) {
            return null;
        }
        try {
            int[] bounds = PixelTextures.computeOpaqueBounds("sprites/lavka/icons/" + fileName);
            TextureRegion region;
            if (bounds != null
                && bounds[0] >= 0 && bounds[1] >= 0
                && bounds[0] + bounds[2] <= loaded.texture.getWidth()
                && bounds[1] + bounds[3] <= loaded.texture.getHeight()) {
                region = new TextureRegion(loaded.texture, bounds[0], bounds[1], bounds[2], bounds[3]);
            } else {
                region = new TextureRegion(loaded.texture);
            }
            return GdxTextureDownscaler.bake(batch, region, size, size);
        } finally {
            PixelTextures.dispose(loaded.texture);
        }
    }
}
