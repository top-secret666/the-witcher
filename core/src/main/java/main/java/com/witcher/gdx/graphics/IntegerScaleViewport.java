package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Пиксельный viewport: заполняет весь экран без чёрных полос.
 * Масштаб по X и Y — отдельные целые числа (×3, ×4…), фильтр Nearest на текстурах.
 * Лишнее за краями экрана обрезается, не размывается.
 */
public class IntegerScaleViewport extends Viewport {

    private final int worldWidth;
    private final int worldHeight;
    private int scaleX = 1;
    private int scaleY = 1;

    public IntegerScaleViewport(int worldWidth, int worldHeight, Camera camera) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        setCamera(camera);
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        scaleX = Math.max(1, (screenWidth + worldWidth - 1) / worldWidth);
        scaleY = Math.max(1, (screenHeight + worldHeight - 1) / worldHeight);
        int drawW = worldWidth * scaleX;
        int drawH = worldHeight * scaleY;
        setScreenBounds((screenWidth - drawW) / 2, (screenHeight - drawH) / 2, drawW, drawH);
        setWorldSize(worldWidth, worldHeight);
        apply(centerCamera);
    }

    public int getScaleX() {
        return scaleX;
    }

    public int getScaleY() {
        return scaleY;
    }

    /** Единый масштаб, если оси совпадают (квадратные пиксели). */
    public int getScale() {
        return Math.min(scaleX, scaleY);
    }
}
