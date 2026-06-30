package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.PixelTextures;

/**
 * Шаг 2: каркас лавки — фон, HUD, панель каталога, диалог внизу.
 * Логика покупки и карточки — следующие шаги.
 */
public class ShopScreen implements Screen {

    private static final float DIALOG_ZONE = 54f;
    private static final int ITEM_COUNT = 5;

    private final WitcherGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private BitmapFont font;
    private ShapeRenderer shapes;

    private Texture merchantBg;
    private Texture hudBar;
    private Texture catalogPanel;

    private final int hudX = 2;
    private final int hudW = 476;
    private final int hudH = 56;

    public ShopScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WitcherGame.VIRTUAL_W, WitcherGame.VIRTUAL_H, camera);
        font = new BitmapFont();
        font.getData().setScale(1.05f);
        shapes = new ShapeRenderer();

        merchantBg = PixelTextures.loadFirst(
            "sprites/lavka/merchant_bg_lavka.png",
            "sprites/lavka/lavka.png"
        );
        hudBar = PixelTextures.loadOptional("sprites/lavka/ui/shop_hud_bar.png");
        catalogPanel = PixelTextures.loadOptional("sprites/lavka/ui/shop_catalog_panel.png");
    }

    @Override
    public void render(float delta) {
        float sw = WitcherGame.VIRTUAL_W;
        float sh = WitcherGame.VIRTUAL_H;

        Gdx.gl.glClearColor(0.05f, 0.04f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
        if (merchantBg != null) {
            PixelTextures.drawCover(game.batch, merchantBg, sw, sh, 0.85f);
        }

        ShopLayout layout = new ShopLayout(sw, sh);
        if (catalogPanel != null) {
            int panelDrawH = layout.btnY + layout.btnH + 4 - layout.panelY;
            game.batch.draw(catalogPanel, layout.panelX, layout.panelY, layout.panelW, panelDrawH);
        }

        if (hudBar != null) {
            game.batch.draw(hudBar, hudX, layout.hudY, hudW, hudH);
        } else {
            game.batch.end();
            drawFallbackHud(layout);
            game.batch.begin();
        }

        font.draw(game.batch, "Lavka Gertsoga", hudX + 18, layout.hudY + 36);
        font.draw(game.batch, "??? kron", hudX + hudW - 90, layout.hudY + 34);
        font.draw(game.batch, "- Tovary -", layout.panelX + layout.panelW * 0.5f - 36, layout.panelY + 16);
        font.draw(game.batch, "[ kartochki - skoro ]", layout.panelX + layout.panelW * 0.5f - 62, layout.cardsY + 40);
        game.batch.end();

        drawDialogBox();
        drawDialogText();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new BootScreen(game));
        }
    }

    private void drawFallbackHud(ShopLayout layout) {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.04f, 0.03f, 0.02f, 0.86f);
        shapes.rect(hudX, layout.hudY, hudW, hudH);
        shapes.end();
    }

    private void drawDialogBox() {
        float boxX = 10f;
        float boxW = WitcherGame.VIRTUAL_W - 20f;
        float boxH = DIALOG_ZONE - 8f;
        float boxY = 6f;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.04f, 0.03f, 0.015f, 0.9f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.55f, 0.39f, 0.14f, 1f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.setColor(0.85f, 0.65f, 0.13f, 0.7f);
        shapes.rect(boxX + 1f, boxY + 1f, boxW - 2f, boxH - 2f);
        shapes.end();
    }

    private void drawDialogText() {
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        font.draw(game.batch, "Gertsog: HO-HO-HO-HA...", 18, 38);
        font.draw(game.batch, "Bronya, kirasy, shlemy - vse chto dushe ugodno.", 18, 22);
        game.batch.end();
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
        PixelTextures.dispose(merchantBg);
        PixelTextures.dispose(hudBar);
        PixelTextures.dispose(catalogPanel);
        merchantBg = null;
        hudBar = null;
        catalogPanel = null;
        if (font != null) {
            font.dispose();
            font = null;
        }
        if (shapes != null) {
            shapes.dispose();
            shapes = null;
        }
    }

    private static final class ShopLayout {
        final float hudY;
        final float panelX;
        final float panelY;
        final float panelW;
        final float panelH;
        final float btnX;
        final float btnY;
        final float btnW;
        final float btnH;
        final float cardsY;
        final float dialogTop;

        ShopLayout(float sw, float sh) {
            hudY = 4f;
            dialogTop = sh - DIALOG_ZONE;
            btnH = 30f;
            btnW = 100f;
            float headerH = 22f;
            float cardW = 54f;
            float cardH = 81f;
            float cardGap = 6f;

            int cardsTotalW = ITEM_COUNT * (int) cardW + (ITEM_COUNT - 1) * (int) cardGap;
            panelW = Math.max(cardsTotalW + 28, Math.min(sw - 88f, 380f));
            panelX = (sw - panelW) * 0.5f;
            panelY = hudY + 56f + 6f;
            cardsY = panelY + headerH + 6f;
            float contentBottom = cardsY + cardH;
            panelH = contentBottom - panelY + 8f;
            btnX = panelX + (panelW - btnW) * 0.5f;
            btnY = contentBottom + 6f;
        }
    }
}
