package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

/**
 * Пиксельный спрайт-лист — кадры с {@link Texture.TextureFilter#Nearest}.
 */
public final class PixelSpriteSheet implements Disposable {

    private final Texture texture;
    private final TextureRegion[] frames;
    private final int frameDelay;
    private int currentFrame;
    private int tickCounter;
    private boolean pingPong;
    private int direction = 1;
    private final int frameWidth;
    private final int frameHeight;
    private boolean ownedTexture;

    private PixelSpriteSheet(Texture texture, TextureRegion[] frames, int frameDelay, boolean ownedTexture) {
        this.texture = texture;
        this.frames = frames;
        this.frameDelay = frameDelay;
        this.ownedTexture = ownedTexture;
        frameWidth = frames.length > 0 ? frames[0].getRegionWidth() : 0;
        frameHeight = frames.length > 0 ? frames[0].getRegionHeight() : 0;
    }

    public static PixelSpriteSheet load(String path, int cols, int rows, int frameDelay) {
        return load(path, cols, rows, frameDelay, false);
    }

    public static PixelSpriteSheet load(String path, int cols, int rows, int frameDelay, boolean removeBlackBg) {
        Texture texture = PixelTextures.loadOptional(path);
        if (texture == null) {
            return null;
        }
        if (removeBlackBg) {
            Texture processed = stripNearBlack(texture);
            if (processed != texture) {
                texture.dispose();
                texture = processed;
            }
        }
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        int fw = texture.getWidth() / cols;
        int fh = texture.getHeight() / rows;
        int total = cols * rows;
        TextureRegion[] regions = new TextureRegion[total];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                regions[r * cols + c] = new TextureRegion(texture, c * fw, r * fh, fw, fh);
            }
        }
        return new PixelSpriteSheet(texture, regions, frameDelay, true);
    }

    private static Texture stripNearBlack(Texture source) {
        if (!source.getTextureData().isPrepared()) {
            source.getTextureData().prepare();
        }
        Pixmap pixmap = source.getTextureData().consumePixmap();
        try {
            for (int y = 0; y < pixmap.getHeight(); y++) {
                for (int x = 0; x < pixmap.getWidth(); x++) {
                    int rgba = pixmap.getPixel(x, y);
                    int a = rgba & 0xff;
                    if (a <= 20) {
                        continue;
                    }
                    int r = (rgba >>> 24) & 0xff;
                    int g = (rgba >>> 16) & 0xff;
                    int b = (rgba >>> 8) & 0xff;
                    if (r < 30 && g < 30 && b < 30) {
                        pixmap.drawPixel(x, y, 0);
                    }
                }
            }
            Texture out = new Texture(pixmap);
            out.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
            return out;
        } finally {
            pixmap.dispose();
        }
    }

    public PixelSpriteSheet setPingPong(boolean value) {
        pingPong = value;
        return this;
    }

    public void update() {
        tickCounter++;
        if (tickCounter < frameDelay) {
            return;
        }
        tickCounter = 0;
        if (pingPong) {
            currentFrame += direction;
            if (currentFrame >= frames.length - 1) {
                currentFrame = frames.length - 1;
                direction = -1;
            } else if (currentFrame <= 0) {
                currentFrame = 0;
                direction = 1;
            }
        } else {
            currentFrame = (currentFrame + 1) % frames.length;
        }
    }

    public void draw(SpriteBatch batch, float x, float y, float w, float h, float alpha) {
        if (frames.length == 0) {
            return;
        }
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(frames[currentFrame], Math.round(x), Math.round(y), Math.round(w), Math.round(h));
        batch.setColor(1f, 1f, 1f, prev);
    }

    public int getFrameWidth() {
        return frameWidth;
    }

    public int getFrameHeight() {
        return frameHeight;
    }

    @Override
    public void dispose() {
        if (ownedTexture && texture != null) {
            texture.dispose();
            ownedTexture = false;
        }
    }
}
