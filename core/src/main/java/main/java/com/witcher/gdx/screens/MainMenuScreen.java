package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.GdxMenuAssets;
import main.java.com.witcher.gdx.graphics.GdxMenuCursor;
import main.java.com.witcher.gdx.graphics.GdxWindowAlign;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.graphics.SwingCoords;
import main.java.com.witcher.gdx.graphics.SwingViewport;
import main.java.com.witcher.gdx.layout.MenuLayout;

/**
 * LibGDX-меню — графика поверх {@link MainMenuController} (логика как в Swing).
 */
public class MainMenuScreen implements Screen {

    private static final float VW = WitcherGame.VIRTUAL_W;
    private static final float VH = WitcherGame.VIRTUAL_H;
    private static final float TITLE_FONT_BASE = 15f;

    private final WitcherGame game;
    private final MainMenuController controller = new MainMenuController();
    private final SwingViewport viewport = new SwingViewport();
    private final SwingCoords C = viewport.coords();
    private final Vector2 swingMouse = new Vector2();
    private final GlyphLayout glyph = new GlyphLayout();

    private OrthographicCamera camera;
    private ShapeRenderer shapes;
    private GameFonts fonts;
    private GdxMenuAssets assets;

    public MainMenuScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        shapes = new ShapeRenderer();
        fonts = new GameFonts();
        fonts.load();
        assets = GdxMenuAssets.load();
        game.frameChrome.setVisible(false);
        GdxMenuCursor.hideForMenu();
        relayoutButtons();
    }

    private void relayoutButtons() {
        float buttonAspect = 1.9f;
        float logoSignAspect = 0f;
        float titleLogoAspect = 0f;
        TextureRegion ref = assets != null ? assets.buttonFrame(0, 0) : null;
        if (ref != null && ref.getRegionHeight() > 0) {
            buttonAspect = (float) ref.getRegionWidth() / ref.getRegionHeight();
        }
        if (assets != null && assets.logoSignTex != null && assets.logoSignTex.getWidth() > 0) {
            logoSignAspect = (float) assets.logoSignTex.getHeight() / assets.logoSignTex.getWidth();
        }
        if (assets != null && assets.titleLogoFrame != null && assets.titleLogoFrame.getRegionWidth() > 0) {
            titleLogoAspect = (float) assets.titleLogoFrame.getRegionHeight()
                / assets.titleLogoFrame.getRegionWidth();
        }
        controller.layoutButtons(VW, VH, buttonAspect, logoSignAspect, titleLogoAspect,
            assets != null && assets.logoSignTex != null,
            assets != null && assets.titleLogoFrame != null);
    }

    @Override
    public void render(float delta) {
        GdxWindowAlign.refreshFramebufferCache();
        int bbw = GdxWindowAlign.backBufferW();
        int bbh = GdxWindowAlign.backBufferH();
        game.bindChromeFramebuffer(bbw, bbh);

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.bindStretch(camera, bbw, bbh);
        game.batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        PixelTextures.resetBlend();

        viewport.screenToSwing(bbw, bbh, Gdx.input.getX(), Gdx.input.getY(), swingMouse);
        int mouseX = Math.round(swingMouse.x);
        int mouseY = Math.round(swingMouse.y);

        int navDir = 0;
        if (Gdx.input.isKeyJustPressed(Input.Keys.UP) || Gdx.input.isKeyJustPressed(Input.Keys.W)) {
            navDir = -1;
        } else if (Gdx.input.isKeyJustPressed(Input.Keys.DOWN) || Gdx.input.isKeyJustPressed(Input.Keys.S)) {
            navDir = 1;
        }
        boolean activate = Gdx.input.isKeyJustPressed(Input.Keys.ENTER)
            || Gdx.input.isKeyJustPressed(Input.Keys.SPACE);

        controller.update(VW, VH, mouseX, mouseY,
            Gdx.input.isButtonJustPressed(Input.Buttons.LEFT), navDir, activate);

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            controller.requestExit();
        }

        game.batch.begin();
        drawBackground();
        game.batch.end();
        drawEmbers();
        game.batch.begin();
        drawTitle();
        drawButtons();
        drawCursor(mouseX, mouseY);
        game.batch.end();

        MainMenuController.Action action = controller.consumeAction();
        if (action == MainMenuController.Action.START) {
            leaveMenu();
            game.setScreen(new IntroScreen(game));
        } else if (action == MainMenuController.Action.EXIT) {
            Gdx.app.exit();
        }
    }

    private void leaveMenu() {
        GdxMenuCursor.restoreAfterMenu();
        game.frameChrome.setVisible(true);
    }

    private void drawBackground() {
        if (assets != null && assets.backgroundTex != null) {
            PixelTextures.drawCover(game.batch, assets.backgroundTex, VW, VH, 1f);
        }
    }

    private void drawEmbers() {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (float[] e : controller.embers()) {
            float life = e[4] / e[5];
            float alpha;
            if (life < 0.15f) {
                alpha = life / 0.15f;
            } else if (life > 0.7f) {
                alpha = (1f - life) / 0.3f;
            } else {
                alpha = 1f;
            }
            alpha = Math.max(0f, Math.min(1f, alpha)) * 0.8f;

            float sz = e[6] * (1f - life * 0.4f);
            float radius = Math.max(0.5f, sz * 0.5f);
            float cx = e[0] + radius;
            float cy = C.centerY(e[1], radius * 2f);

            if (sz > 1.2f && alpha > 0.3f) {
                shapes.setColor(1f, 0.63f, 0.24f, alpha * 0.15f);
                shapes.circle(cx, cy, radius * 3f);
            }

            shapes.setColor(e[7] / 255f, e[8] / 255f, e[9] / 255f, alpha * 0.78f);
            shapes.circle(cx, cy, radius);
        }
        shapes.end();
        PixelTextures.resetBlend();
    }

    private void drawTitle() {
        if (assets == null) {
            return;
        }
        float logoY = MenuLayout.logoY(VH);
        if (assets.logoSignTex != null) {
            float signW = MenuLayout.signW(VW);
            float signH = signW * assets.logoSignTex.getHeight() / assets.logoSignTex.getWidth();
            float signX = (VW - signW) * 0.5f;
            game.batch.draw(assets.logoSignTex, signX, C.rectY(logoY, signH), signW, signH);
            if (assets.titleLogoFrame != null) {
                float logoW = signW * MenuLayout.INNER_LOGO_W_OF_SIGN;
                float logoH = logoW * assets.titleLogoFrame.getRegionHeight()
                    / assets.titleLogoFrame.getRegionWidth();
                float logoX = (VW - logoW) * 0.5f;
                float innerTop = logoY + (signH - logoH) * 0.5f + signH * MenuLayout.INNER_LOGO_OFFSET_Y_OF_SIGN;
                game.batch.draw(assets.titleLogoFrame, logoX, C.rectY(innerTop, logoH), logoW, logoH);
            }
        } else if (assets.titleLogoFrame != null) {
            float logoW = VW * MenuLayout.TITLE_LOGO_W_RATIO;
            float logoH = logoW * assets.titleLogoFrame.getRegionHeight()
                / assets.titleLogoFrame.getRegionWidth();
            game.batch.draw(assets.titleLogoFrame, (VW - logoW) * 0.5f, C.rectY(logoY, logoH), logoW, logoH);
        }
    }

    private void drawButtons() {
        if (assets == null) {
            return;
        }
        BitmapFont font = fonts.title;
        for (int i = 0; i < controller.buttonCount(); i++) {
            Rectangle r = controller.buttonRect(i);
            int state = controller.buttonState(i);
            TextureRegion frame = assets.buttonFrame(i, state);
            if (frame != null) {
                game.batch.draw(frame, r.x, C.rectY(r.y, r.height), r.width, r.height);
            }
            String label = controller.buttonLabel(i);
            if (label.isEmpty()) {
                continue;
            }
            float anchorX = i < MenuLayout.TEXT_ANCHOR_X.length ? MenuLayout.TEXT_ANCHOR_X[i] : 0.47f;
            float fontSize = Math.max(MenuLayout.TEXT_FONT_MIN, r.height * MenuLayout.TEXT_FONT_HEIGHT_RATIO);
            float fontScale = fontSize / TITLE_FONT_BASE;
            font.getData().setScale(fontScale);
            glyph.setText(font, label);
            float tx = r.x + r.width * anchorX - glyph.width * 0.5f;
            float ty = C.textBaseline(r.y + r.height * MenuLayout.TEXT_ANCHOR_Y);
            font.setColor(0f, 0f, 0f, 0.7f);
            font.draw(game.batch, label, tx + 1f, ty - 1f);
            font.setColor(state == 2 ? 0.78f : 0.96f, state == 2 ? 0.67f : 0.86f, state == 2 ? 0.35f : 0.47f, 1f);
            font.draw(game.batch, label, tx, ty);
            font.getData().setScale(1f);
        }
    }

    private void drawCursor(int mouseX, int mouseY) {
        if (assets == null || assets.cursorTex == null) {
            return;
        }
        float cw = MenuLayout.CURSOR_W;
        float ch = cw * assets.cursorTex.getHeight() / assets.cursorTex.getWidth();
        float topY = mouseY - MenuLayout.CURSOR_HOTSPOT_Y;
        game.batch.draw(assets.cursorTex, mouseX - MenuLayout.CURSOR_HOTSPOT_X,
            C.rectY(topY, ch), cw, ch);
    }

    @Override
    public void resize(int width, int height) {
        GdxWindowAlign.refreshFramebufferCache();
        relayoutButtons();
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        leaveMenu();
    }

    @Override
    public void dispose() {
        leaveMenu();
        if (assets != null) {
            assets.dispose();
            assets = null;
        }
        if (fonts != null) {
            fonts.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
    }
}
