package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.GdxIntroAssets;
import main.java.com.witcher.gdx.graphics.GdxMenuCursor;
import main.java.com.witcher.gdx.graphics.GdxWindowAlign;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.graphics.SwingCoords;
import main.java.com.witcher.gdx.graphics.SwingViewport;
import main.java.com.witcher.gdx.intro.GdxIntroView;
import main.java.com.witcher.ui.intro.presenter.IntroController;

/**
 * Интро-заставка LibGDX — общая логика в {@link IntroController}.
 */
public class IntroScreen implements Screen {

    private static final float VW = WitcherGame.VIRTUAL_W;
    private static final float VH = WitcherGame.VIRTUAL_H;
    private static final int FB_W = (int) (VW * WitcherGame.PIXEL_SCALE);
    private static final int FB_H = (int) (VH * WitcherGame.PIXEL_SCALE);

    private final WitcherGame game;
    private final SwingViewport viewport = new SwingViewport();
    private final SwingCoords C = viewport.coords();
    private final Vector2 swingMouse = new Vector2();
    private final GdxIntroView introView = new GdxIntroView();

    private OrthographicCamera camera;
    private ShapeRenderer shapes;
    private GameFonts fonts;
    private GdxIntroAssets assets;
    private IntroController controller;
    private FrameBuffer fbo;
    private TextureRegion sceneRegion;

    public IntroScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        shapes = new ShapeRenderer();
        fonts = new GameFonts();
        fonts.load();
        assets = GdxIntroAssets.load();
        controller = new IntroController(assets.buildAssetsInfo());
        fbo = new FrameBuffer(com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888, FB_W, FB_H, false);
        com.badlogic.gdx.graphics.Texture fboTex = fbo.getColorBufferTexture();
        fboTex.setFilter(com.badlogic.gdx.graphics.Texture.TextureFilter.Linear,
            com.badlogic.gdx.graphics.Texture.TextureFilter.Linear);
        sceneRegion = new TextureRegion(fboTex);
        sceneRegion.flip(false, true);
        game.frameChrome.setVisible(false);
        GdxMenuCursor.hideForMenu();
        GdxWindowAlign.ensureFramebuffer(WitcherGame.FRAME_W, WitcherGame.FRAME_H);
    }

    @Override
    public void render(float delta) {
        GdxWindowAlign.refreshFramebufferCache();
        int bbw = GdxWindowAlign.backBufferW();
        int bbh = GdxWindowAlign.backBufferH();

        viewport.screenToSwing(bbw, bbh, Gdx.input.getX(), Gdx.input.getY(), swingMouse);
        int mouseX = Math.round(swingMouse.x);
        int mouseY = Math.round(swingMouse.y);

        boolean advance = Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);
        boolean clicked = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        int wheelNotches = (int) Gdx.input.getDeltaY();

        controller.update(advance, mouseX, mouseY, clicked, wheelNotches);

        fbo.begin();
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.setToOrtho(false, VW, VH);
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        PixelTextures.resetBlend();

        introView.render(game.batch, shapes, fonts, assets, controller, C,
            Math.round(VW), Math.round(VH), mouseX, mouseY);
        fbo.end();

        game.bindChromeFramebuffer(bbw, bbh);
        Gdx.gl.glViewport(0, 0, bbw, bbh);
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.bindStretch(camera, bbw, bbh);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        game.batch.draw(sceneRegion, 0f, 0f, VW, VH);
        game.batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            leaveIntro();
            game.setScreen(new MainMenuScreen(game));
            return;
        }
        if (controller.isFinished()) {
            leaveIntro();
            game.setScreen(new ShopScreen(game));
        }
    }

    private void leaveIntro() {
        GdxMenuCursor.restoreAfterMenu();
        game.frameChrome.setVisible(true);
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
        if (fbo != null) {
            fbo.dispose();
            fbo = null;
        }
        if (assets != null) {
            assets.dispose();
            assets = null;
        }
        if (fonts != null) {
            fonts.dispose();
            fonts = null;
        }
        if (shapes != null) {
            shapes.dispose();
            shapes = null;
        }
        sceneRegion = null;
        controller = null;
    }
}
