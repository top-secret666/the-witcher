package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * Как Swing {@code Renderer}: виртуальный кадр {@code 480×360} растягивается на весь backbuffer
 * без чёрных полей. Окно должно быть {@link WitcherGame#WINDOW_W}×{@link WitcherGame#WINDOW_H}.
 */
public class IntegerScaleViewport extends Viewport {

    private final float worldWidth;
    private final float worldHeight;
    private int scaleX = 1;
    private int scaleY = 1;

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

        scaleX = Math.max(1, Math.round(screenWidth / worldWidth));
        scaleY = Math.max(1, Math.round(screenHeight / worldHeight));

        setScreenBounds(0, 0, screenWidth, screenHeight);
        setWorldSize(worldWidth, worldHeight);
        apply(centerCamera);

        Camera cam = getCamera();
        cam.position.set(worldWidth * 0.5f, worldHeight * 0.5f, 0);
        cam.update();
    }

    public int getScale() {
        return Math.min(scaleX, scaleY);
    }

    public int getScaleX() {
        return scaleX;
    }

    public int getScaleY() {
        return scaleY;
    }
}
