package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.DisplayMetrics;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.GameFrameLayout;
import main.java.com.witcher.gdx.graphics.GdxShopPointer;
import main.java.com.witcher.gdx.graphics.GdxWindowAlign;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.shop.GdxArmourIconRegistry;
import main.java.com.witcher.gdx.shop.GdxShopRuntimeAssets;
import main.java.com.witcher.gdx.shop.GdxShopView;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.presenter.ShopInput;
import main.java.com.witcher.ui.shop.presenter.ShopPresenter;

/**
 * LibGDX-экран лавки — тонкая обёртка над {@link ShopPresenter} + {@link GdxShopView}.
 */
public class ShopScreen implements Screen {

    private static final Color SHOP_BACKDROP = new Color(18f / 255f, 12f / 255f, 8f / 255f, 1f);
    private static final float SIM_STEP = 1f / 30f;

    private final WitcherGame game;
    private OrthographicCamera frameCamera;
    private OrthographicCamera gameCamera;
    private ShapeRenderer shapes;
    private GameFonts fonts;
    private GameFrameLayout layout;

    private ShopModel model;
    private GdxShopRuntimeAssets assets;
    private ShopPresenter presenter;
    private GdxShopView view;
    private final GdxShopPointer pointer = new GdxShopPointer();
    private final Vector2 virtualMouse = new Vector2();

    private float simAccumulator;
    private boolean clickLatch;

    public ShopScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        frameCamera = new OrthographicCamera();
        gameCamera = new OrthographicCamera();
        shapes = new ShapeRenderer();

        fonts = new GameFonts();
        fonts.load();

        assets = GdxShopRuntimeAssets.get();
        model = ShopModel.createNewSession();
        presenter = new ShopPresenter(model, assets, GdxArmourIconRegistry.get(assets.cardArtSize));
        view = new GdxShopView(game, presenter, assets, fonts);

        GdxWindowAlign.ensureFramebuffer(WitcherGame.FRAME_W, WitcherGame.FRAME_H);
        int bbw = GdxWindowAlign.backBufferW();
        int bbh = GdxWindowAlign.backBufferH();
        game.bindChromeFramebuffer(bbw, bbh);
        DisplayMetrics.log("shop-show");
        Gdx.app.log("ShopScreen", "presenter session started, assets hud=" + (assets.hudBar != null));
    }

    @Override
    public void render(float delta) {
        GdxWindowAlign.refreshFramebufferCache();
        int bbw = GdxWindowAlign.backBufferW();
        int bbh = GdxWindowAlign.backBufferH();
        game.bindChromeFramebuffer(bbw, bbh);
        layout = GameFrameLayout.fromFramebuffer(bbw, bbh);

        simAccumulator += delta;
        while (simAccumulator >= SIM_STEP) {
            simAccumulator -= SIM_STEP;
            updateInput();
        }

        layout.clearBackdrop(SHOP_BACKDROP.r, SHOP_BACKDROP.g, SHOP_BACKDROP.b);

        layout.bindFullFrame(frameCamera);
        game.batch.setProjectionMatrix(frameCamera.combined);
        shapes.setProjectionMatrix(frameCamera.combined);
        game.frameChrome.drawBackground(shapes);

        layout.bindGame(gameCamera);
        game.batch.setProjectionMatrix(gameCamera.combined);
        shapes.setProjectionMatrix(gameCamera.combined);
        PixelTextures.resetBlend();

        pointer.toVirtual(layout, Gdx.input.getX(), Gdx.input.getY(), virtualMouse);
        int mouseX = Math.round(virtualMouse.x);
        int mouseY = swingMouseY(virtualMouse.y);

        view.render(game.batch, shapes, layout, mouseX, mouseY);

        layout.bindFullFrame(frameCamera);
        game.batch.setProjectionMatrix(frameCamera.combined);
        shapes.setProjectionMatrix(frameCamera.combined);
        game.frameChrome.drawForeground(shapes, game.batch);

        if (presenter.exitRequested()) {
            presenter.clearExitRequest();
            game.setScreen(new BootScreen(game));
        }
    }

    /** Presenter использует Swing-координаты (Y сверху). */
    private int swingMouseY(float libgdxY) {
        return Math.round(WitcherGame.VIRTUAL_H - libgdxY);
    }

    private void updateInput() {
        pointer.toVirtual(layout, Gdx.input.getX(), Gdx.input.getY(), virtualMouse);
        int mouseX = Math.round(virtualMouse.x);
        int mouseY = swingMouseY(virtualMouse.y);

        boolean clicked = false;
        if (Gdx.input.isButtonJustPressed(Input.Buttons.LEFT) && !clickLatch) {
            clickLatch = true;
            clicked = true;
        }
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            clickLatch = false;
        }

        float scroll = Gdx.input.getDeltaY();
        int wheel = scroll > 0f ? 1 : (scroll < 0f ? -1 : 0);

        presenter.update(new ShopInput(mouseX, mouseY, clicked,
            Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE), wheel));
    }

    @Override
    public void resize(int width, int height) {
        GdxWindowAlign.refreshFramebufferCache();
        DisplayMetrics.log("shop-resize");
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
        if (view != null) {
            view.dispose();
            view = null;
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
        presenter = null;
        model = null;
    }
}
