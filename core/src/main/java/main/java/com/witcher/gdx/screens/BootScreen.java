package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.viewport.FitViewport;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.screens.ShopScreen;

/**
 * Первый тестовый экран — проверяем, что viewport 480×360 и запуск работают.
 */
public class BootScreen implements Screen {

    private final WitcherGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private BitmapFont font;

    public BootScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WitcherGame.VIRTUAL_W, WitcherGame.VIRTUAL_H, camera);
        font = new BitmapFont();
        font.getData().setScale(1.1f);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.06f, 0.05f, 0.04f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        font.draw(game.batch, "The Witcher — LibGDX", 108, 210);
        font.draw(game.batch, "Shag 2: lavka (karkas)", 118, 180);
        font.draw(game.batch, "ENTER — otkryt lavku", 118, 150);
        font.draw(game.batch, "ESC — vyhod", 168, 90);
        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new ShopScreen(game));
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (font != null) {
            font.dispose();
        }
    }
}
