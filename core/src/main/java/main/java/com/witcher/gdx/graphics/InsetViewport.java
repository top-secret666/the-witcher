package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * Виртуальный мир рисуется только внутри прямоугольника кадра (между рамкой и шапкой).
 */
public class InsetViewport extends Viewport {

    private final float frameWorldW;
    private final float frameWorldH;
    private final float insetLeft;
    private final float insetBottom;
    private final float innerWorldW;
    private final float innerWorldH;

    public InsetViewport(
        float frameWorldW,
        float frameWorldH,
        float insetLeft,
        float insetBottom,
        float innerWorldW,
        float innerWorldH,
        Camera camera
    ) {
        this.frameWorldW = frameWorldW;
        this.frameWorldH = frameWorldH;
        this.insetLeft = insetLeft;
        this.insetBottom = insetBottom;
        this.innerWorldW = innerWorldW;
        this.innerWorldH = innerWorldH;
        setCamera(camera);
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        if (screenWidth <= 0 || screenHeight <= 0) {
            return;
        }
        float sx = screenWidth / frameWorldW;
        float sy = screenHeight / frameWorldH;

        int px = Math.round(insetLeft * sx);
        int py = Math.round(insetBottom * sy);
        int pw = Math.round(innerWorldW * sx);
        int ph = Math.round(innerWorldH * sy);

        setScreenBounds(px, py, pw, ph);
        setWorldSize(innerWorldW, innerWorldH);
        apply(centerCamera);

        Camera cam = getCamera();
        cam.position.set(innerWorldW * 0.5f, innerWorldH * 0.5f, 0f);
        cam.update();
    }
}
