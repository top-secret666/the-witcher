package main.java.com.witcher.gdx.bridge;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.ui.shop.ArmourIconMap;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Запекание иконок товаров через LibGDX (nearest-neighbor) → {@link BufferedImage} для Swing.
 */
public final class GdxIconBaker {

    private GdxIconBaker() {
    }

    public static Map<String, BufferedImage> bakeDistinctIcons(int... sizes) {
        Set<String> files = ArmourIconMap.distinctIconFiles();
        Map<String, BufferedImage> out = new HashMap<>();
        SpriteBatch batch = new SpriteBatch();
        FrameBuffer fbo = null;
        try {
            for (int size : sizes) {
                if (size <= 0) {
                    continue;
                }
                if (fbo == null || fbo.getWidth() != size) {
                    if (fbo != null) {
                        fbo.dispose();
                    }
                    fbo = new FrameBuffer(Pixmap.Format.RGBA8888, size, size, false);
                }
                for (String file : files) {
                    BufferedImage baked = bakeFile(file, size, batch, fbo);
                    if (baked != null) {
                        out.put(cacheKey(file, size), baked);
                    }
                }
            }
        } finally {
            if (fbo != null) {
                fbo.dispose();
            }
            batch.dispose();
        }
        Gdx.app.log("GdxIconBaker", "Baked " + out.size() + " icons");
        return out;
    }

    public static String cacheKey(String fileName, int size) {
        return fileName + "@" + size;
    }

    private static BufferedImage bakeFile(String fileName, int size, SpriteBatch batch, FrameBuffer fbo) {
        PixelTextures.LoadedTexture loaded = PixelTextures.loadLavkaMeta("icons/items/" + fileName);
        if (loaded == null || loaded.texture == null) {
            return null;
        }
        Texture texture = loaded.texture;
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);

        int[] bounds = PixelTextures.computeOpaqueBounds("sprites/lavka/icons/items/" + fileName);
        TextureRegion region;
        if (bounds != null
            && bounds[0] >= 0 && bounds[1] >= 0
            && bounds[0] + bounds[2] <= texture.getWidth()
            && bounds[1] + bounds[3] <= texture.getHeight()) {
            region = new TextureRegion(texture, bounds[0], bounds[1], bounds[2], bounds[3]);
        } else {
            region = new TextureRegion(texture);
        }

        fbo.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();
        batch.draw(region, 0f, 0f, size, size);
        batch.end();
        fbo.end();

        Pixmap pixmap = Pixmap.createFromFrameBuffer(0, 0, size, size);
        try {
            PixelTextures.flipPixmapVertical(pixmap);
            return PixelTextures.pixmapToBufferedImage(pixmap);
        } finally {
            pixmap.dispose();
            PixelTextures.dispose(texture);
        }
    }
}
