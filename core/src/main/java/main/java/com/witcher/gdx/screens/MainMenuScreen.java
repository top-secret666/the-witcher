package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.GameFrameLayout;
import main.java.com.witcher.gdx.graphics.GdxShopPointer;
import main.java.com.witcher.gdx.graphics.SwingCoords;
import main.java.com.witcher.gdx.graphics.GdxWindowAlign;
import main.java.com.witcher.gdx.graphics.PixelTextures;

/**
 * LibGDX-меню — графика; логика в {@link MainMenuController}.
 */
public class MainMenuScreen implements Screen {

    private static final float VW = WitcherGame.VIRTUAL_W;
    private static final float VH = WitcherGame.VIRTUAL_H;
    private static final Color BACKDROP = new Color(12f / 255f, 8f / 255f, 5f / 255f, 1f);

    private final WitcherGame game;
    private final MainMenuController controller = new MainMenuController();
    private static final SwingCoords C = SwingCoords.forVirtualFrame();
    private final GdxShopPointer pointer = new GdxShopPointer();
    private final Vector2 virtualMouse = new Vector2();

    private OrthographicCamera frameCamera;
    private OrthographicCamera gameCamera;
    private ShapeRenderer shapes;
    private GameFonts fonts;
    private GameFrameLayout layout;

    private Texture background;
    private Texture logoSign;
    private Texture titleLogo;

    public MainMenuScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        frameCamera = new OrthographicCamera();
        gameCamera = new OrthographicCamera();
        shapes = new ShapeRenderer();
        fonts = new GameFonts();
        fonts.load();

        background = PixelTextures.loadOptional("sprites/menu/menu_bg_custom.jpg");
        logoSign = PixelTextures.loadOptional("sprites/menu/menu_logo_sign.png");
        titleLogo = PixelTextures.loadFirst("sprites/witcher_logo_new.png");

        controller.layoutButtons(VW, VH);
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
        game.batch.setProjectionMatrix(frameCamera.combined);
        shapes.setProjectionMatrix(frameCamera.combined);
        game.frameChrome.drawBackground(shapes);

        layout.bindGame(gameCamera);
        game.batch.setProjectionMatrix(gameCamera.combined);
        shapes.setProjectionMatrix(gameCamera.combined);
        PixelTextures.resetBlend();

        pointer.toVirtual(layout, Gdx.input.getX(), Gdx.input.getY(), virtualMouse);
        int mouseX = Math.round(virtualMouse.x);
        int mouseY = Math.round(VH - virtualMouse.y);
        controller.update(mouseX, mouseY, Gdx.input.isButtonJustPressed(Input.Buttons.LEFT));

        game.batch.begin();
        drawBackground();
        drawTitle();
        game.batch.end();

        drawButtonPlanks();
        game.batch.begin();
        drawButtonLabels();
        game.batch.end();

        layout.bindFullFrame(frameCamera);
        game.batch.setProjectionMatrix(frameCamera.combined);
        shapes.setProjectionMatrix(frameCamera.combined);
        game.frameChrome.drawForeground(shapes, game.batch);

        MainMenuController.Action action = controller.consumeAction();
        if (action == MainMenuController.Action.START) {
            game.setScreen(new IntroScreen(game));
        } else if (action == MainMenuController.Action.EXIT) {
            Gdx.app.exit();
        }
    }

    private void drawBackground() {
        if (background != null) {
            PixelTextures.drawCover(game.batch, background, VW, VH, 1f);
        }
    }

    private void drawTitle() {
        float logoY = VH * 0.035f;
        if (logoSign != null) {
            float signW = VW * 0.45f;
            float signH = signW * logoSign.getHeight() / logoSign.getWidth();
            float signX = (VW - signW) * 0.5f;
            float drawY = VH - logoY - signH;
            game.batch.draw(logoSign, signX, drawY, signW, signH);
            if (titleLogo != null) {
                float logoW = signW * 0.7f;
                float logoH = logoW * titleLogo.getHeight() / titleLogo.getWidth();
                float logoX = (VW - logoW) * 0.5f;
                float innerY = drawY + (signH - logoH) * 0.5f - signH * 0.05f;
                game.batch.draw(titleLogo, logoX, innerY, logoW, logoH);
            }
        } else if (titleLogo != null) {
            float logoW = VW * 0.31f;
            float logoH = logoW * titleLogo.getHeight() / titleLogo.getWidth();
            game.batch.draw(titleLogo, (VW - logoW) * 0.5f, VH - logoY - logoH, logoW, logoH);
        }
    }

    private void drawButtonPlanks() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int i = 0; i < controller.buttonCount(); i++) {
            Rectangle r = controller.buttonRect(i);
            int state = controller.buttonState(i);
            if (state == 2) {
                shapes.setColor(0.45f, 0.32f, 0.12f, 0.95f);
            } else if (state == 1) {
                shapes.setColor(0.55f, 0.40f, 0.16f, 0.92f);
            } else {
                shapes.setColor(0.38f, 0.26f, 0.10f, 0.88f);
            }
            shapes.rect(r.x, C.rectY(r.y, r.height), r.width, r.height);
        }
        shapes.end();
    }

    private void drawButtonLabels() {
        BitmapFont font = fonts.title;
        for (int i = 0; i < controller.buttonCount(); i++) {
            Rectangle r = controller.buttonRect(i);
            font.setColor(0.96f, 0.86f, 0.47f, 1f);
            float textY = C.textBaseline(r.y + r.height * 0.62f);
            font.draw(game.batch, controller.buttonLabel(i), r.x + r.width * 0.34f, textY);
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
        disposeTex(background);
        disposeTex(logoSign);
        disposeTex(titleLogo);
        if (fonts != null) {
            fonts.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
    }

    private static void disposeTex(Texture t) {
        if (t != null) {
            t.dispose();
        }
    }
}
