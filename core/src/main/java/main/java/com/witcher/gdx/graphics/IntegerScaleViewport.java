package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Пиксельный viewport: виртуальный кадр {@code worldW×worldH} растягивается на всё окно
 * (без чёрных полей). При совпадении пропорций 4:3 масштаб равномерный.
 * Текстуры — {@link com.badlogic.gdx.graphics.Texture.TextureFilter#Nearest}.
 */
public class IntegerScaleViewport extends Viewport {

    private final int worldWidth;
    private final int worldHeight;
    private int scale = 1;

    public IntegerScaleViewport(int worldWidth, int worldHeight, Camera camera) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        setCamera(camera);
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }

        float scaleX = screenWidth / (float) worldWidth;
        float scaleY = screenHeight / (float) worldHeight;
        float uniform = Math.max(scaleX, scaleY);
        scale = Math.max(1, Math.round(uniform));

        setScreenBounds(0, 0, screenWidth, screenHeight);

        float visibleW = screenWidth / uniform;
        float visibleH = screenHeight / uniform;
        setWorldSize(visibleW, visibleH);
        apply(centerCamera);

        float cx = worldWidth * 0.5f;
        float cy = worldHeight * 0.5f;
        if (visibleH < worldHeight) {
            cy = visibleH * 0.5f;
        }
        if (visibleW < worldWidth) {
            cx = visibleW * 0.5f;
        }

        Camera cam = getCamera();
        cam.position.set(cx, cy, 0);
        cam.update();
    }

    public int getScale() {
        return scale;
    }
}
