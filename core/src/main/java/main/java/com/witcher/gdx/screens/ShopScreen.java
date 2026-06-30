package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.screens.BootScreen;
import main.java.com.witcher.gdx.shop.ShopAssets;
import main.java.com.witcher.gdx.shop.ShopItem;

import java.util.Random;

/**
 * Полный визуал лавки Герцога (LibGDX). Логика покупки — позже.
 */
public class ShopScreen implements Screen {

    private static final float DIALOG_ZONE = 54f;
    private static final Color DUKE_GOLD = new Color(0.85f, 0.65f, 0.13f, 1f);
    private static final Color SPEECH = new Color(0.86f, 0.75f, 0.39f, 1f);
    private static final Color WALLET = new Color(1f, 0.9f, 0.59f, 1f);
    private static final Color WALLET_SUFFIX = new Color(0.78f, 0.7f, 0.47f, 1f);

    private enum ShopState { WELCOME, BROWSE, IDLE }

    private final WitcherGame game;
    private FitViewport viewport;
    private OrthographicCamera camera;
    private ShapeRenderer shapes;
    private final Vector2 mouse = new Vector2();
    private final GlyphLayout glyph = new GlyphLayout();
    private final Random rng = new Random();

    private GameFonts fonts;
    private ShopAssets assets;
    private final Array<ShopItem> items = new Array<>();
    private final float[] cardFlip = new float[5];
    private final boolean[] cardFaceBack = new boolean[5];
    private final Array<float[]> ashParticles = new Array<>();

    private ShopState state = ShopState.WELCOME;
    private String currentDialog;
    private int selectedIndex = -1;
    private int hoveredIndex = -1;
    private int tick;
    private int welcomeTicks;
    private boolean clickLatch;

    private static final String WELCOME = """
            ХО-ХО-ХО-ХА... Приступим к делу, Белый Волк.
            Броня, кирасы, шлемы, наколенники — всё, что душе угодно.""";

    private static final String IDLE = "Ну же, выбирайте. У меня нет вечности, а у вас — монстров полно.";

    public ShopScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new FitViewport(WitcherGame.VIRTUAL_W, WitcherGame.VIRTUAL_H, camera);
        shapes = new ShapeRenderer();

        fonts = new GameFonts();
        fonts.load();

        assets = new ShopAssets();
        assets.load();
        buildItems();
        currentDialog = WELCOME;
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.app.log("ShopScreen", "assets: bg=" + (assets.merchantBg != null)
            + " hud=" + (assets.hudBar != null)
            + " panel=" + (assets.catalogPanel != null)
            + " bgSrc=" + assets.merchantBgSource
            + " bgSize=" + (assets.merchantBg != null
                ? assets.merchantBg.getWidth() + "x" + assets.merchantBg.getHeight() : "?"));
    }

    /** Swing (Y сверху) → нижний край спрайта в LibGDX. */
    private static float bottomFromTop(float top, float height, float sh) {
        return sh - top - height;
    }

    /** Swing (Y сверху) → базовая линия текста в LibGDX. */
    private static float textYFromTop(float top, float sh) {
        return sh - top;
    }

    private void buildItems() {
        items.add(new ShopItem("Кираса", "120",
            "Отличный выбор! Волчья сталь — как раз для таких, как вы.",
            new String[]{"Защита: 45", "Вес: 12", "Тип: кираса"}, assets.iconChest));
        items.add(new ShopItem("Штаны", "45",
            "Штаны крепкие. Ноги целее — монстров больше.",
            new String[]{"Защита: 20", "Вес: 8", "Тип: поножи"}, assets.iconLegs));
        items.add(new ShopItem("Перчатки", "30",
            "Рукам тепло, клинку — верно. Берите, не пожалеете.",
            new String[]{"Защита: 12", "Вес: 3", "Тип: перчатки"}, assets.iconGloves));
        items.add(new ShopItem("Сапоги", "55",
            "В этих сапогах и по болоту пройдёте.",
            new String[]{"Защита: 18", "Вес: 6", "Тип: сапоги"}, assets.iconBoots));
        items.add(new ShopItem("Зелье", "15",
            "Хм... Зелье? Ну что ж, ваш выбор, Белый Волк...",
            new String[]{"Эффект: яд", "Вес: 0.5", "⚠ ловушка"}, assets.iconPotion));
    }

    @Override
    public void render(float delta) {
        float sw = WitcherGame.VIRTUAL_W;
        float sh = WitcherGame.VIRTUAL_H;
        tick++;
        welcomeTicks++;

        updateLogic(delta);
        updateAsh(sh);

        Gdx.gl.glClearColor(0.05f, 0.04f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        ShopLayout layout = new ShopLayout(sw, sh, items.size, assets.hud.drawH);

        Texture dukeTex = (state == ShopState.BROWSE && selectedIndex >= 0 && assets.dukeLaughPortrait != null)
            ? assets.dukeLaughPortrait : assets.dukePortrait;

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        if (assets.merchantBg != null) {
            PixelTextures.drawCover(game.batch, assets.merchantBg, sw, sh, 0.75f);
        }

        drawPortrait(assets.geraltPortrait, true, layout, sh);
        drawPortrait(dukeTex, false, layout, sh);
        drawHud(layout, sh);
        drawCatalog(layout, sh);
        drawCards(layout, sh);
        drawBuyButton(layout, sh);
        game.batch.end();

        drawDarkOverlay(layout, sw, sh);

        drawAshParticles();
        drawDialogFrame();
        drawDialogText();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new BootScreen(game));
        }
    }

    private void updateLogic(float delta) {
        if (state == ShopState.WELCOME && welcomeTicks > 120) {
            state = ShopState.IDLE;
            currentDialog = IDLE;
        }

        mouse.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(mouse);

        hoveredIndex = -1;
        for (int i = 0; i < items.size; i++) {
            if (items.get(i).bounds.contains(mouse)) {
                hoveredIndex = i;
                break;
            }
        }

        boolean clicked = Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (clicked && !clickLatch) {
            clickLatch = true;
            if (hoveredIndex >= 0) {
                if (selectedIndex == hoveredIndex) {
                    cardFaceBack[hoveredIndex] = !cardFaceBack[hoveredIndex];
                    cardFlip[hoveredIndex] = 0f;
                } else {
                    selectedIndex = hoveredIndex;
                    state = ShopState.BROWSE;
                    currentDialog = items.get(selectedIndex).dukeLine;
                }
            }
        }
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            clickLatch = false;
        }

        for (int i = 0; i < cardFlip.length; i++) {
            float target = cardFaceBack[i] ? 1f : 0f;
            if (Math.abs(cardFlip[i] - target) > 0.02f) {
                cardFlip[i] = MathUtils.lerp(cardFlip[i], target, delta * 8f);
            } else {
                cardFlip[i] = target;
            }
        }
    }

    private void drawDarkOverlay(ShopLayout layout, float sw, float sh) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0, 0, 0, 0.45f);
        shapes.rect(0, 0, sw, sh);

        shapes.setColor(0, 0, 0, 0.55f);
        float pad = 6f;
        float overlayH = layout.panelBottom - layout.panelTop + pad * 2f;
        float overlayY = bottomFromTop(layout.panelTop - pad, overlayH, sh);
        shapes.rect(layout.panelX - pad, overlayY, layout.panelW + pad * 2, overlayH);

        shapes.setColor(0, 0, 0, 0.35f);
        shapes.rect(0, bottomFromTop(0, layout.dialogTop, sh), layout.panelX - 10, layout.dialogTop);
        shapes.rect(layout.panelX + layout.panelW + 10, bottomFromTop(0, layout.dialogTop, sh),
            sw - layout.panelX - layout.panelW - 10, layout.dialogTop);
        shapes.end();
    }

    private void drawPortrait(Texture sprite, boolean left, ShopLayout layout, float sh) {
        if (sprite == null) {
            return;
        }
        float charScale = (sh * 0.7f) / sprite.getHeight();
        float cw = sprite.getWidth() * charScale;
        float ch = sprite.getHeight() * charScale;
        float spriteTop = layout.dialogTop - ch + ch * 0.12f;
        float cy = bottomFromTop(spriteTop, ch, sh);
        float cx = left ? -cw * 0.12f : WitcherGame.VIRTUAL_W - cw + cw * 0.12f;
        float breathe = MathUtils.sin(tick * 0.04f + (left ? 0f : 2f)) * 1.5f;
        game.batch.setColor(1, 1, 1, 0.92f);
        game.batch.draw(sprite, cx, cy + breathe, cw, ch);
        game.batch.setColor(1, 1, 1, 1);
    }

    private void drawHud(ShopLayout layout, float sh) {
        ShopAssets.HudLayout hud = assets.hud;
        float hudX = hud.drawX;
        float hudW = hud.drawW;
        float hudH = hud.drawH;
        float hudBottom = bottomFromTop(layout.hudTop, hudH, sh);

        if (assets.hudBar != null) {
            if (hud.cropped) {
                PixelTextures.drawCropped(game.batch, assets.hudBar,
                    hud.cropX, hud.cropY, hud.cropW, hud.cropH,
                    hudX, hudBottom, hudW, hudH);
            } else {
                game.batch.draw(assets.hudBar, hudX, hudBottom, hudW, hudH);
            }
        } else {
            game.batch.end();
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.04f, 0.03f, 0.02f, 0.9f);
            shapes.rect(hudX, hudBottom, hudW, hudH);
            shapes.end();
            game.batch.begin();
        }

        BitmapFont titleFont = fonts.title;
        titleFont.setColor(DUKE_GOLD);
        titleFont.draw(game.batch, "Лавка Герцога", hudX + 18, textYFromTop(layout.hudTop + 18, sh));

        String wallet = "???";
        int crownSize = 18;
        BitmapFont wFont = fonts.ui;
        glyph.setText(wFont, wallet);
        float walletW = glyph.width;
        glyph.setText(wFont, " крон");
        float suffixW = glyph.width;

        float anchor = hudX + hudW * 0.68f;
        float textX = anchor - suffixW - walletW;
        float crownY = hudBottom + (hudH - crownSize) * 0.5f;
        if (assets.crownIcon != null) {
            textX -= crownSize + 4;
            game.batch.draw(assets.crownIcon, textX, crownY, crownSize, crownSize);
            textX += crownSize + 4;
        }
        wFont.setColor(WALLET);
        wFont.draw(game.batch, wallet, textX, textYFromTop(layout.hudTop + 20, sh));
        wFont.setColor(WALLET_SUFFIX);
        wFont.draw(game.batch, " крон", textX + walletW, textYFromTop(layout.hudTop + 20, sh));
    }

    private void drawCatalog(ShopLayout layout, float sh) {
        if (assets.catalogPanel != null) {
            float panelDrawH = layout.panelBottom - layout.panelTop;
            float panelY = bottomFromTop(layout.panelTop, panelDrawH, sh);
            game.batch.draw(assets.catalogPanel, layout.panelX, panelY, layout.panelW, panelDrawH);
        }
        fonts.ui.setColor(DUKE_GOLD);
        drawCentered(fonts.ui, "— Товары —", layout.panelX + layout.panelW * 0.5f,
            textYFromTop(layout.panelTop + 16, sh));
    }

    private void drawCards(ShopLayout layout, float sh) {
        for (int i = 0; i < items.size; i++) {
            ShopItem item = items.get(i);
            float x = layout.cardsStartX + i * (layout.cardW + layout.cardGap);
            float y = bottomFromTop(layout.cardsTop, layout.cardH, sh);
            item.bounds.set(x, y, layout.cardW, layout.cardH);
            drawItemCard(item, x, y, layout.cardW, layout.cardH,
                i == selectedIndex, i == hoveredIndex, cardFlip[i], sh);
        }
    }

    private void drawItemCard(ShopItem item, float x, float y, float w, float h,
                              boolean selected, boolean hovered, float flip, float sh) {
        boolean showBack = flip >= 0.5f;
        float fade = 1f;
        if (flip > 0.05f && flip < 0.95f) {
            fade = flip < 0.5f ? 1f - flip * 2f : (flip - 0.5f) * 2f;
            fade = 0.35f + fade * 0.65f;
        }

        float prevA = game.batch.getColor().a;
        game.batch.setColor(1, 1, 1, fade);

        Texture frame = assets.cardFront;
        if (selected && assets.cardSelected != null) {
            frame = assets.cardSelected;
        } else if (hovered && assets.cardHover != null) {
            frame = assets.cardHover;
        }

        Rectangle cardRect = new Rectangle(x, y, w, h);
        if (showBack) {
            if (assets.cardBack != null) {
                cardRect = drawAspectFit(assets.cardBack, x, y, w, h);
            }
            drawCardBack(item, cardRect, sh);
        } else {
            if (frame != null) {
                cardRect = drawAspectFit(frame, x, y, w, h);
            }
            drawCardFront(item, cardRect, sh);
        }
        game.batch.setColor(1, 1, 1, prevA);
    }

    private Rectangle drawAspectFit(Texture img, float x, float y, float w, float h) {
        float tw = img.getWidth();
        float th = img.getHeight();
        float srcAspect = tw / th;
        float dstAspect = w / h;
        float drawW, drawH;
        if (srcAspect > dstAspect) {
            drawW = w;
            drawH = w / srcAspect;
        } else {
            drawH = h;
            drawW = h * srcAspect;
        }
        float drawX = x + (w - drawW) * 0.5f;
        float drawY = y + (h - drawH) * 0.5f;
        game.batch.draw(img, drawX, drawY, drawW, drawH);
        return new Rectangle(drawX, drawY, drawW, drawH);
    }

    private void drawCardFront(ShopItem item, Rectangle card, float sh) {
        Texture art = item.icon;
        if (art != null) {
            float artSize = Math.min(card.width - 10, card.height - 32);
            game.batch.draw(art,
                card.x + (card.width - artSize) * 0.5f, card.y + 6, artSize, artSize);
        }
        BitmapFont small = fonts.uiSmall;
        small.setColor(0.92f, 0.84f, 0.61f, 1f);
        drawCentered(small, item.name, card.x + card.width * 0.5f, card.y + card.height - 14);

        float crownSize = 10f;
        glyph.setText(small, item.priceLabel);
        float priceW = glyph.width;
        if (assets.crownIcon != null) {
            priceW += crownSize + 2;
        }
        float priceX = card.x + (card.width - priceW) * 0.5f;
        if (assets.crownIcon != null) {
            game.batch.draw(assets.crownIcon, priceX, card.y + 8, crownSize, crownSize);
            priceX += crownSize + 2;
        }
        small.setColor(1f, 0.9f, 0.47f, 1f);
        small.draw(game.batch, item.priceLabel, priceX, card.y + 22);
    }

    private void drawCardBack(ShopItem item, Rectangle card, float sh) {
        BitmapFont small = fonts.uiSmall;
        small.setColor(1f, 0.86f, 0.51f, 1f);
        float lineY = card.y + card.height - 12;
        for (int i = item.statLines.length - 1; i >= 0; i--) {
            drawCentered(small, item.statLines[i], card.x + card.width * 0.5f, lineY);
            lineY -= 11;
        }
    }

    private void drawBuyButton(ShopLayout layout, float sh) {
        float btnY = bottomFromTop(layout.btnTop, layout.btnH, sh);
        if (assets.btnBuyDisabled != null) {
            game.batch.draw(assets.btnBuyDisabled, layout.btnX, btnY, layout.btnW, layout.btnH);
        }
        fonts.ui.setColor(0.35f, 0.29f, 0.2f, 1f);
        drawCentered(fonts.ui, "Скоро", layout.btnX + layout.btnW * 0.5f,
            textYFromTop(layout.btnTop + 20, sh));
    }

    private void drawDialogFrame() {
        float boxX = 10f;
        float boxW = WitcherGame.VIRTUAL_W - 20f;
        float boxH = DIALOG_ZONE - 8f;
        float boxY = 6f;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.04f, 0.03f, 0.015f, 0.92f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.55f, 0.39f, 0.14f, 1f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.setColor(0.85f, 0.65f, 0.13f, 0.75f);
        shapes.rect(boxX + 1, boxY + 1, boxW - 2, boxH - 2);
        shapes.end();
    }

    private void drawDialogText() {
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        BitmapFont df = fonts.dialog;
        String speaker = "Герцог: ";
        df.setColor(DUKE_GOLD);
        df.draw(game.batch, speaker, 18, 38);

        glyph.setText(df, speaker);
        float sx = 18 + glyph.width;

        String[] lines = currentDialog.split("\n", 2);
        df.setColor(SPEECH);
        df.draw(game.batch, lines[0].trim(), sx, 38);
        if (lines.length > 1) {
            df.draw(game.batch, lines[1].trim(), 18, 22);
        }
        game.batch.end();
    }

    private void drawCentered(BitmapFont font, String text, float centerX, float y) {
        glyph.setText(font, text);
        font.draw(game.batch, text, centerX - glyph.width * 0.5f, y);
    }

    private void updateAsh(float sh) {
        if (tick % 6 == 0 && ashParticles.size < 15) {
            float top = 50 + rng.nextFloat() * 140;
            ashParticles.add(new float[]{
                130 + rng.nextFloat() * 220, bottomFromTop(top, 0, sh),
                0, 0.12f, 0, 60 + rng.nextInt(60), 1
            });
        }
        for (int i = ashParticles.size - 1; i >= 0; i--) {
            float[] p = ashParticles.get(i);
            p[4]++;
            p[1] += p[3];
            if (p[4] >= p[5]) {
                ashParticles.removeIndex(i);
            }
        }
    }

    private void drawAshParticles() {
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (float[] p : ashParticles) {
            float life = 1f - p[4] / p[5];
            shapes.setColor(0.55f, 0.45f, 0.3f, life * 0.4f);
            shapes.rect(p[0], p[1], 1.5f, 1.5f);
        }
        shapes.end();
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
    }

    private static final class ShopLayout {
        /** Координаты от верха экрана (как в Swing). */
        final float hudTop = 4f;
        final float panelTop;
        final float panelBottom;
        final float cardsTop;
        final float btnTop;
        final float panelX;
        final float panelW;
        final float btnX;
        final float btnW = 100f;
        final float btnH = 30f;
        final float cardW = 54f;
        final float cardH = 81f;
        final float cardGap = 6f;
        final float cardsStartX;
        /** Линия «верх диалога» — от верха экрана. */
        final float dialogTop;

        ShopLayout(float sw, float sh, int itemCount, int hudDrawH) {
            dialogTop = sh - DIALOG_ZONE;
            float headerH = 22f;
            int cardsTotalW = itemCount * (int) cardW + (itemCount - 1) * (int) cardGap;
            panelW = Math.max(cardsTotalW + 28, Math.min(sw - 88f, 380f));
            panelX = (sw - panelW) * 0.5f;
            panelTop = hudTop + hudDrawH + 6f;
            cardsTop = panelTop + headerH + 6f;
            float contentBottom = cardsTop + cardH;
            btnTop = contentBottom + 6f;
            panelBottom = btnTop + btnH + 4f;
            cardsStartX = panelX + (panelW - cardsTotalW) * 0.5f;
            btnX = panelX + (panelW - btnW) * 0.5f;
        }
    }
}
