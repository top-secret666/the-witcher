package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Fullscreen / большой экран с целочисленным масштабом (×2, ×3, ×4…).
 * Чёрные поля по краям, картинка без дробного zoom — как Nearest в LibGDX.
 */
public class IntegerScaleViewport extends Viewport {

    private final int worldWidth;
    private final int worldHeight;

    public IntegerScaleViewport(int worldWidth, int worldHeight, Camera camera) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        setCamera(camera);
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        int scale = Math.max(1, Math.min(screenWidth / worldWidth, screenHeight / worldHeight));
        int bw = worldWidth * scale;
        int bh = worldHeight * scale;
        setScreenBounds((screenWidth - bw) / 2, (screenHeight - bh) / 2, bw, bh);
        setWorldSize(worldWidth, worldHeight);
        apply(centerCamera);
    }

    public int getScale() {
        int sw = getScreenWidth();
        if (sw <= 0) {
            return 1;
        }
        return sw / worldWidth;
    }
}
