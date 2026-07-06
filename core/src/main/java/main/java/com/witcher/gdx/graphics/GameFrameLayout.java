package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * Прямоугольники кадра в пикселях framebuffer — без виртуального мира для рамки.
 */
public final class GameFrameLayout {

    public final int fbW;
    public final int fbH;
    public final int gameX;
    public final int gameY;
    public final int gameW;
    public final int gameH;

    private GameFrameLayout(int fbW, int fbH, int gameX, int gameY, int gameW, int gameH) {
        this.fbW = fbW;
        this.fbH = fbH;
        this.gameX = gameX;
        this.gameY = gameY;
        this.gameW = gameW;
        this.gameH = gameH;
    }

    public static GameFrameLayout fromFramebuffer(int fbW, int fbH) {
        float ux = fbW / (float) WitcherGame.FRAME_W;
        float uy = fbH / (float) WitcherGame.FRAME_H;
        int gx = Math.round(WitcherGame.BORDER * ux);
        int gy = Math.round(WitcherGame.BORDER * uy);
        int gw = Math.round(WitcherGame.WINDOW_W * ux);
        int gh = Math.round(WitcherGame.WINDOW_H * uy);
        return new GameFrameLayout(fbW, fbH, gx, gy, gw, gh);
    }

    public void bindFullFrame(OrthographicCamera camera) {
        Gdx.gl.glViewport(0, 0, fbW, fbH);
        camera.setToOrtho(false, WitcherGame.FRAME_W, WitcherGame.FRAME_H);
        camera.position.set(WitcherGame.FRAME_W * 0.5f, WitcherGame.FRAME_H * 0.5f, 0f);
        camera.update();
    }

    public void bindGame(OrthographicCamera camera) {
        Gdx.gl.glViewport(gameX, gameY, gameW, gameH);
        camera.setToOrtho(false, WitcherGame.VIRTUAL_W, WitcherGame.VIRTUAL_H);
        camera.position.set(WitcherGame.VIRTUAL_W * 0.5f, WitcherGame.VIRTUAL_H * 0.5f, 0f);
        camera.update();
    }

    public void clearBackdrop(float r, float g, float b) {
        Gdx.gl.glViewport(0, 0, fbW, fbH);
        Gdx.gl.glClearColor(r, g, b, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }
}
