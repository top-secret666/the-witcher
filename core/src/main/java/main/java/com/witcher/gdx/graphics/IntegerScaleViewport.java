package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Пиксельный viewport: один целый масштаб (×3, ×4…) — квадратные пиксели, без растягивания.
 * Экран заполняется за счёт обрезки краёв; при обрезке по высоте кадр прижат к низу (диалог виден).
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
        scale = Math.max(1, Math.max(
            (screenWidth + worldWidth - 1) / worldWidth,
            (screenHeight + worldHeight - 1) / worldHeight));

        int drawW = worldWidth * scale;
        int drawH = worldHeight * scale;
        setScreenBounds((screenWidth - drawW) / 2, (screenHeight - drawH) / 2, drawW, drawH);
        setWorldSize(worldWidth, worldHeight);
        apply(false);

        float visibleWorldH = screenHeight > 0 ? screenHeight / (float) scale : worldHeight;
        float cx = worldWidth * 0.5f;
        float cy = worldHeight * 0.5f;

        if (drawH > screenHeight) {
            cy = visibleWorldH * 0.5f;
        }

        Camera cam = getCamera();
        cam.position.set(cx, cy, 0);
        cam.update();
    }

    public int getScale() {
        return scale;
    }
}
