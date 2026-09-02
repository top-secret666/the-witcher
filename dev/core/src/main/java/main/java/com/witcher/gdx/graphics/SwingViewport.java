package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * Виртуальный кадр Swing (480×360) на весь framebuffer — для меню и полноэкранных экранов.
 */
public final class SwingViewport {

    private final float designW;
    private final float designH;
    private final SwingCoords coords;

    public SwingViewport() {
        this(WitcherGame.VIRTUAL_W, WitcherGame.VIRTUAL_H);
    }

    public SwingViewport(float designW, float designH) {
        this.designW = designW;
        this.designH = designH;
        this.coords = new SwingCoords(designW, designH);
    }

    public SwingCoords coords() {
        return coords;
    }

    /** Растягивает дизайн на весь framebuffer (без inset game viewport). */
    public void bindStretch(OrthographicCamera camera, int fbW, int fbH) {
        com.badlogic.gdx.Gdx.gl.glViewport(0, 0, fbW, fbH);
        camera.setToOrtho(false, designW, designH);
        camera.position.set(designW * 0.5f, designH * 0.5f, 0f);
        camera.update();
    }

    /** Экранные пиксели → Swing-Y (логика / hit-test). */
    public void screenToSwing(int fbW, int fbH, int screenX, int screenY, Vector2 outSwing) {
        float nx = screenX / (float) Math.max(1, fbW);
        float ny = 1f - screenY / (float) Math.max(1, fbH);
        outSwing.x = nx * designW;
        outSwing.y = coords.gdxToSwingY(ny * designH);
    }

    public float uniformScale(int fbW, int fbH) {
        return Math.min(fbW / designW, fbH / designH);
    }
}
