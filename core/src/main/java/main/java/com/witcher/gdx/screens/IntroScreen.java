package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.GameFrameLayout;
import main.java.com.witcher.gdx.graphics.GdxWindowAlign;
import main.java.com.witcher.gdx.graphics.PixelTextures;

/**
 * Заглушка интро — полный порт {@link main.java.com.witcher.ui.graphics.IntroScreen} в работе.
 * ENTER / клик — переход в лавку.
 */
public class IntroScreen implements Screen {

    private static final float VW = WitcherGame.VIRTUAL_W;
    private static final float VH = WitcherGame.VIRTUAL_H;
    private static final Color BACKDROP = new Color(8f / 255f, 6f / 255f, 4f / 255f, 1f);

    private final WitcherGame game;
    private OrthographicCamera frameCamera;
    private OrthographicCamera gameCamera;
    private ShapeRenderer shapes;
    private GameFonts fonts;
    private GameFrameLayout layout;
    private Texture merchantBg;

    public IntroScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        frameCamera = new OrthographicCamera();
        gameCamera = new OrthographicCamera();
        shapes = new ShapeRenderer();
        fonts = new GameFonts();
        fonts.load();
        merchantBg = PixelTextures.loadLavka("merchant_bg_lavka.png", "lavka.png");
        GdxWindowAlign.ensureFramebuffer(WitcherGame.FRAME_W, WitcherGame.FRAME_H);
    }

    @Override
    public void render(float delta) {
        GdxWindowAlign.refreshFramebufferCache();
        int bbw = GdxWindowAlign.backBufferW();
        int bbh = GdxWindowAlign.backBufferH();
        game.bindChromeFramebuffer(bbw, bbh);
        layout = GameFrameLayout.fromFramebuffer(bbw, bbh);
        layout.clearBackdrop(BACKDROP.r, BACKDROP.g, BACKDROP.b);

        layout.bindFullFrame(frameCamera);
        shapes.setProjectionMatrix(frameCamera.combined);
        game.batch.setProjectionMatrix(frameCamera.combined);
        game.frameChrome.drawBackground(shapes);

        layout.bindGame(gameCamera);
        game.batch.setProjectionMatrix(gameCamera.combined);
        shapes.setProjectionMatrix(gameCamera.combined);
        PixelTextures.resetBlend();

        game.batch.begin();
        if (merchantBg != null) {
            PixelTextures.drawCover(game.batch, merchantBg, VW, VH, 0.85f);
        }
        BitmapFont font = fonts.dialog;
        font.setColor(0.92f, 0.82f, 0.55f, 1f);
        font.draw(game.batch, "Интро (порт в LibGDX)...", 24f, VH - 48f);
        font.draw(game.batch, "ENTER — лавка", 24f, VH - 72f);
        game.batch.end();

        layout.bindFullFrame(frameCamera);
        game.frameChrome.drawForeground(shapes, game.batch);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
            || Gdx.input.isButtonJustPressed(Input.Buttons.LEFT)) {
            game.setScreen(new ShopScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new MainMenuScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        GdxWindowAlign.refreshFramebufferCache();
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
        if (merchantBg != null) {
            merchantBg.dispose();
        }
        if (fonts != null) {
            fonts.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
    }
}
