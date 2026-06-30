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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.IntegerScaleViewport;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.shop.ShopAssets;
import main.java.com.witcher.gdx.shop.ShopItem;

import java.util.Random;

/**
 * LibGDX-порт {@code main.java.com.witcher.ui.graphics.ShopScreen} — та же логика и порядок отрисовки.
 */
public class ShopScreen implements Screen {

    private static final float DIALOG_ZONE = 54f;
    /** Отступ диалога от низа экрана (больше = выше на экране). */
    private static final float DIALOG_BOTTOM_MARGIN = 18f;
    private static final Color DUKE_GOLD = new Color(218f / 255f, 165f / 255f, 32f / 255f, 1f);
    private static final Color SPEECH = new Color(220f / 255f, 190f / 255f, 100f / 255f, 1f);
    private static final Color WALLET = new Color(1f, 230f / 255f, 150f / 255f, 1f);
    private static final Color WALLET_SUFFIX = new Color(200f / 255f, 180f / 255f, 120f / 255f, 1f);

    private enum ShopState { WELCOME, BROWSE, IDLE }

    private final WitcherGame game;
    private Viewport viewport;
    private OrthographicCamera camera;
    private ShapeRenderer shapes;
    private final Vector2 mouse = new Vector2();
    private final GlyphLayout glyph = new GlyphLayout();
    private final Random rng = new Random();

    private GameFonts fonts;
    private ShopAssets assets;
    private final Array<ShopItem> items = new Array<>();
    private final float[] cardFlip = new float[8];
    private final boolean[] cardFaceBack = new boolean[8];
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
            Броня, кирасы, шлемы, наколенники — всё, что душе угодно.
            Только не забудьте кошелёк...""";

    private static final String IDLE = "Ну же, выбирайте. У меня нет вечности, а у вас — монстров полно.";

    public ShopScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new IntegerScaleViewport(
            (int) WitcherGame.VIRTUAL_W, (int) WitcherGame.VIRTUAL_H, camera);
        shapes = new ShapeRenderer();

        fonts = new GameFonts();
        fonts.load();

        assets = new ShopAssets();
        assets.load();
        buildItems();
        currentDialog = WELCOME;
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.app.log("ShopScreen", "cwd=" + System.getProperty("user.dir")
            + " assets=" + System.getProperty("witcher.assets", "")
            + " bg=" + (assets.merchantBg != null) + " src=" + assets.merchantBgSource
            + " hud=" + (assets.hudBar != null)
            + " panel=" + (assets.catalogPanel != null));
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
        items.add(new ShopItem("Кираса", "120", false,
            "Отличный выбор! Волчья сталь — как раз для таких, как вы.",
            new String[]{"Защ. 45", "Вес 12", "Кираса"}, assets.iconChest));
        items.add(new ShopItem("Штаны", "45", false,
            "Штаны крепкие. Ноги целее — монстров больше.",
            new String[]{"Защ. 20", "Вес 8", "Поножи"}, assets.iconLegs));
        items.add(new ShopItem("Перчатки", "30", false,
            "Рукам тепло, клинку — верно. Берите, не пожалеете.",
            new String[]{"Защ. 12", "Вес 3", "Руки"}, assets.iconGloves));
        items.add(new ShopItem("Сапоги", "55", false,
            "В этих сапогах и по болоту пройдёте, и от удара отскочите.",
            new String[]{"Защ. 18", "Вес 6", "Сапоги"}, assets.iconBoots));
        items.add(new ShopItem("Зелье", "15", false,
            "Хм... Зелье? Ну что ж, ваш выбор, Белый Волк...",
            new String[]{"Яд", "0.5 кг", "Осторожно"}, assets.iconPotion));
        items.add(new ShopItem("Комплекты", "···", true,
            "Ах, охотник на целые комплекты! Волчья, Кошачья, Грифонья — выбирайте.",
            new String[]{"Школьные", "Легендар.", "4 части"}, assets.setCatalogIcon));
    }

    @Override
    public void render(float delta) {
        float sw = WitcherGame.VIRTUAL_W;
        float sh = WitcherGame.VIRTUAL_H;
        tick++;
        welcomeTicks++;

        updateLogic(delta);
        updateAsh();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        ShopLayout layout = new ShopLayout(sw, sh, items.size,
            assets.hud.drawX, assets.hud.drawW, assets.hud.drawH);

        Texture dukeTex = (state == ShopState.BROWSE && selectedIndex >= 0 && assets.dukeLaughPortrait != null)
            ? assets.dukeLaughPortrait : assets.dukePortrait;

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();

        if (assets.merchantBg != null) {
            PixelTextures.drawCover(game.batch, assets.merchantBg, sw, sh, 0.75f);
        }

        game.batch.end();
        drawDarkOverlay(layout, sw, sh);
        PixelTextures.resetBlend();

        game.batch.begin();
        game.batch.setColor(1f, 1f, 1f, 1f);
        drawPortrait(assets.geraltPortrait, true, layout, sh);
        drawPortrait(dukeTex, false, layout, sh);
        drawHud(layout, sh);
        drawCards(layout, sh);
        drawBuyButton(layout, sh);
        game.batch.end();

        drawAshParticles();
        PixelTextures.resetBlend();
        drawCompactDialog(sh, currentDialog);

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
                selectedIndex = hoveredIndex;
                state = ShopState.BROWSE;
                currentDialog = items.get(hoveredIndex).dukeLine;
                if (!items.get(hoveredIndex).setCatalog) {
                    cardFaceBack[hoveredIndex] = !cardFaceBack[hoveredIndex];
                }
            }
        }
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            clickLatch = false;
        }

        for (int i = 0; i < items.size && i < cardFlip.length; i++) {
            if (items.get(i).setCatalog) {
                cardFlip[i] = 0f;
                continue;
            }
            float target = cardFaceBack[i] ? 1f : 0f;
            float diff = target - cardFlip[i];
            if (Math.abs(diff) > 0.02f) {
                cardFlip[i] += diff * Math.min(1f, delta * 19.2f);
            } else {
                cardFlip[i] = target;
            }
        }
    }

    /** Как в Swing: затемнение до UI, чтобы доска выдвигалась вперёд. */
    private void drawDarkOverlay(ShopLayout layout, float sw, float sh) {
        PixelTextures.resetBlend();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        shapes.setColor(0f, 0f, 0f, 0.45f);
        shapes.rect(0f, 0f, sw, sh);

        float pad = 6f;
        float contentBottom = layout.cardsY + layout.gridRows * layout.cardH
            + (layout.gridRows - 1) * layout.cardGap;
        float panelH = contentBottom - layout.panelTop + 8f;
        float overlayH = panelH + layout.btnH + pad * 2f + 10f;
        float overlayY = bottomFromTop(layout.panelTop - pad, overlayH, sh);
        shapes.setColor(0f, 0f, 0f, 0.55f);
        shapes.rect(layout.panelX - pad, overlayY, layout.panelW + pad * 2f, overlayH);

        shapes.setColor(0f, 0f, 0f, 0.35f);
        float sideH = layout.dialogTop;
        shapes.rect(0f, bottomFromTop(0f, sideH, sh), layout.panelX - 10f, sideH);
        shapes.rect(layout.panelX + layout.panelW + 10f, bottomFromTop(0f, sideH, sh),
            sw - layout.panelX - layout.panelW - 10f, sideH);

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
        float breathe = (float) Math.sin(tick * 0.04 + (left ? 0 : 2)) * 1.5f;
        game.batch.setColor(1f, 1f, 1f, 0.92f);
        game.batch.draw(sprite, cx, cy + breathe, cw, ch);
        game.batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawHud(ShopLayout layout, float sh) {
        float hudX = layout.hudX;
        float hudW = layout.hudW;
        float hudH = layout.hudH;
        float hudBottom = bottomFromTop(layout.hudTop, hudH, sh);

        if (assets.hudBar != null) {
            if (assets.hudBarRegion != null) {
                game.batch.draw(assets.hudBarRegion, hudX, hudBottom, hudW, hudH);
            } else {
                game.batch.draw(assets.hudBar, hudX, hudBottom, hudW, hudH);
            }
        } else {
            game.batch.end();
            shapes.setProjectionMatrix(camera.combined);
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(10f / 255f, 8f / 255f, 4f / 255f, 220f / 255f);
            shapes.rect(hudX, hudBottom, hudW, hudH);
            shapes.end();
            game.batch.begin();
        }

        BitmapFont titleFont = fonts.title;
        titleFont.setColor(DUKE_GOLD);
        glyph.setText(titleFont, "Лавка Герцога");
        float titleY = textYFromTop(layout.hudTop + (hudH + glyph.height) * 0.5f - 1f, sh);
        titleFont.draw(game.batch, "Лавка Герцога", hudX + 18f, titleY);

        String wallet = "???";
        int crownSize = 18;
        BitmapFont wFont = fonts.ui;
        glyph.setText(wFont, wallet);
        float walletW = glyph.width;
        glyph.setText(wFont, " крон");
        float suffixW = glyph.width;

        float blockW = walletW + suffixW;
        if (assets.crownIcon != null) {
            blockW += crownSize + 4f;
        }
        float blockX = hudX + (hudW - blockW) * 0.5f;
        float crownY = hudBottom + (hudH - crownSize) * 0.5f;
        float textX = blockX;
        if (assets.crownIcon != null) {
            game.batch.draw(assets.crownIcon, blockX, crownY, crownSize, crownSize);
            textX = blockX + crownSize + 4f;
        }
        glyph.setText(wFont, wallet);
        float walletY = textYFromTop(layout.hudTop + (hudH + glyph.height) * 0.5f - 2f, sh);
        wFont.setColor(WALLET);
        wFont.draw(game.batch, wallet, textX, walletY);
        wFont.setColor(WALLET_SUFFIX);
        wFont.draw(game.batch, " крон", textX + walletW, walletY);
    }

    private void drawCards(ShopLayout layout, float sh) {
        float panelBottom = layout.btnTop + layout.btnH + 4f;
        float panelDrawH = panelBottom - layout.panelTop;
        float panelY = bottomFromTop(layout.panelTop, panelDrawH, sh);

        if (assets.catalogPanel != null) {
            game.batch.draw(assets.catalogPanel, layout.panelX, panelY, layout.panelW, panelDrawH);
        }

        for (int i = 0; i < items.size; i++) {
            ShopItem item = items.get(i);
            float x = layout.cardX(i);
            float y = bottomFromTop(layout.cardY(i), layout.cardH, sh);
            item.bounds.set(x, y, layout.cardW, layout.cardH);
            drawItemCard(item, x, y, layout.cardW, layout.cardH,
                i == selectedIndex, i == hoveredIndex, cardFlip[i]);
        }
    }

    private void drawItemCard(ShopItem item, float x, float y, float w, float h,
                              boolean selected, boolean hovered, float flip) {
        boolean showBack = !item.setCatalog && flip >= 0.5f;
        float fade = 1f;
        if (!item.setCatalog && flip > 0.05f && flip < 0.95f) {
            fade = flip < 0.5f ? 1f - flip * 2f : (flip - 0.5f) * 2f;
            fade = 0.35f + fade * 0.65f;
        }

        float prevA = game.batch.getColor().a;
        game.batch.setColor(1f, 1f, 1f, fade);

        Texture frame = assets.cardFront;
        if (selected && assets.cardSelected != null) {
            frame = assets.cardSelected;
        } else if (hovered && assets.cardHover != null) {
            frame = assets.cardHover;
        }

        Rectangle cardRect = new Rectangle(x, y, w, h);
        if (showBack) {
            if (assets.cardBack != null) {
                game.batch.draw(assets.cardBack, x, y, w, h);
            }
            drawCardBack(item, cardRect);
        } else {
            if (frame != null) {
                game.batch.draw(frame, x, y, w, h);
            }
            drawCardFront(item, cardRect);
        }
        game.batch.setColor(1f, 1f, 1f, prevA);
    }

    private void drawCardFront(ShopItem item, Rectangle card) {
        Texture art = item.icon;
        if (art != null) {
            float artSize = 32f;
            game.batch.draw(art,
                card.x + (card.width - artSize) * 0.5f, card.y + 8f, artSize, artSize);
        }
        BitmapFont small = fonts.uiSmall;
        Color nameColor = item.setCatalog
            ? new Color(255f / 255f, 210f / 255f, 100f / 255f, 1f)
            : new Color(245f / 255f, 230f / 255f, 190f / 255f, 1f);
        small.setColor(nameColor);
        String name = truncateToWidth(small, item.name, card.width - 8f);
        drawCentered(small, name, card.x + card.width * 0.5f, card.y + card.height - 22f);

        Texture priceCrown = assets.crownIconSmall != null ? assets.crownIconSmall : assets.crownIcon;
        float crownSize = 10f;
        glyph.setText(small, item.priceLabel);
        float priceW = glyph.width;
        if (priceCrown != null) {
            priceW += crownSize + 2f;
        }
        float priceX = card.x + (card.width - priceW) * 0.5f;
        if (priceCrown != null) {
            game.batch.draw(priceCrown, priceX, card.y + card.height - 14f, crownSize, crownSize);
            priceX += crownSize + 2f;
        }
        small.setColor(255f / 255f, 230f / 255f, 120f / 255f, 1f);
        small.draw(game.batch, item.priceLabel, priceX, card.y + card.height - 4f);
    }

    private void drawCardBack(ShopItem item, Rectangle card) {
        BitmapFont small = fonts.uiSmall;
        small.setColor(255f / 255f, 220f / 255f, 130f / 255f, 1f);
        float lineY = card.y + 12f;
        for (String line : item.statLines) {
            drawCentered(small, truncateToWidth(small, line, card.width - 6f),
                card.x + card.width * 0.5f, lineY);
            lineY += 11f;
        }
    }

    private String truncateToWidth(BitmapFont font, String text, float maxW) {
        glyph.setText(font, text);
        if (glyph.width <= maxW) {
            return text;
        }
        String ellipsis = "…";
        for (int len = text.length() - 1; len > 0; len--) {
            String cut = text.substring(0, len) + ellipsis;
            glyph.setText(font, cut);
            if (glyph.width <= maxW) {
                return cut;
            }
        }
        return ellipsis;
    }

    private void drawBuyButton(ShopLayout layout, float sh) {
        float btnY = bottomFromTop(layout.btnTop, layout.btnH, sh);
        Texture btn = assets.btnBuyNormal != null ? assets.btnBuyNormal : assets.btnBuyDisabled;
        if (btn != null) {
            game.batch.draw(btn, layout.btnX, btnY, layout.btnW, layout.btnH);
        }
        fonts.ui.setColor(220f / 255f, 200f / 255f, 140f / 255f, 1f);
        drawCentered(fonts.ui, "Купить", layout.btnX + layout.btnW * 0.5f,
            textYFromTop(layout.btnTop + 19f, sh));
    }

    /** Порт {@link main.java.com.witcher.ui.graphics.DialogBoxRenderer#drawCompactFramedSpeakerText}. */
    private void drawCompactDialog(float sh, String text) {
        PixelTextures.resetBlend();
        float sw = WitcherGame.VIRTUAL_W;
        float fontSize = Math.max(12f, sh * 0.036f);
        float boxMarginX = 10f;
        float boxMarginBottom = DIALOG_BOTTOM_MARGIN;
        float pad = 8f;
        float boxW = sw - boxMarginX * 2f;
        float lineH = fontSize + 3f;

        BitmapFont textFont = fonts.dialog;
        BitmapFont speakerFont = fonts.dialog;
        String speakerLabel = "Герцог: ";
        glyph.setText(speakerFont, speakerLabel);
        float speakerW = glyph.width;

        String[] rawLines = text.split("\n", -1);
        int lineCount = Math.min(2, Math.max(1, rawLines.length));
        float boxH = lineH * lineCount + pad * 2f;
        float boxX = boxMarginX;
        float boxY = bottomFromTop(sh - boxMarginBottom - boxH, boxH, sh);

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(10f / 255f, 8f / 255f, 4f / 255f, 230f / 255f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(140f / 255f, 100f / 255f, 35f / 255f, 1f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.setColor(DUKE_GOLD.r, DUKE_GOLD.g, DUKE_GOLD.b, 160f / 255f);
        shapes.rect(boxX + 1f, boxY + 1f, boxW - 2f, boxH - 2f);
        shapes.end();
        PixelTextures.resetBlend();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        float textX = boxX + pad;
        float startY = textYFromTop(sh - boxMarginBottom - boxH + pad + fontSize, sh);

        speakerFont.setColor(DUKE_GOLD);
        speakerFont.draw(game.batch, speakerLabel, textX, startY);
        textFont.setColor(SPEECH);
        if (rawLines.length > 0) {
            textFont.draw(game.batch, rawLines[0].trim(), textX + speakerW, startY);
        }
        if (rawLines.length > 1) {
            textFont.draw(game.batch, rawLines[1].trim(), textX, startY - lineH);
        }
        game.batch.end();
    }

    private void drawCentered(BitmapFont font, String text, float centerX, float y) {
        glyph.setText(font, text);
        font.draw(game.batch, text, centerX - glyph.width * 0.5f, y);
    }

    private void updateAsh() {
        if (tick % 6 == 0 && ashParticles.size < 15) {
            ashParticles.add(new float[]{
                130f + rng.nextFloat() * 220f,
                50f + rng.nextFloat() * 140f,
                0f, -0.12f, 0f, 60 + rng.nextInt(60), 1f
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
        PixelTextures.resetBlend();
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (float[] p : ashParticles) {
            float life = 1f - p[4] / p[5];
            shapes.setColor(200f / 255f, 170f / 255f, 100f / 255f, life * 50f / 255f);
            shapes.rect(p[0], bottomFromTop(p[1], 1f, WitcherGame.VIRTUAL_H), 1f, 1f);
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
        final float hudTop = 4f;
        final float hudX;
        final float hudW;
        final float hudH;
        final float panelTop;
        final float cardsY;
        final float btnTop;
        final float panelX;
        final float panelW;
        final float btnX;
        final float btnW = 100f;
        final float btnH = 30f;
        final float cardW = ShopAssets.CARD_W;
        final float cardH = ShopAssets.CARD_H;
        final float cardGap = 6f;
        final float cardsStartX;
        final float dialogTop;
        final int gridCols;
        final int gridRows;

        ShopLayout(float sw, float sh, int itemCount, int hudX, int hudW, int hudH) {
            this.hudX = hudX;
            this.hudW = hudW;
            this.hudH = hudH;
            dialogTop = sh - DIALOG_ZONE;
            float headerH = 22f;

            gridCols = ShopAssets.GRID_COLS;
            gridRows = Math.max(1, (itemCount + gridCols - 1) / gridCols);
            int rowW = gridCols * (int) cardW + (gridCols - 1) * (int) cardGap;
            panelW = ShopAssets.PANEL_W;
            panelX = (sw - panelW) * 0.5f;
            panelTop = hudTop + hudH + 6f;
            cardsY = panelTop + headerH + 6f;
            float contentBottom = cardsY + gridRows * cardH + (gridRows - 1) * cardGap;
            btnTop = contentBottom + 6f;
            cardsStartX = panelX + (panelW - rowW) * 0.5f;
            btnX = panelX + (panelW - btnW) * 0.5f;
        }

        float cardX(int index) {
            int col = index % gridCols;
            return cardsStartX + col * (cardW + cardGap);
        }

        float cardY(int index) {
            int row = index / gridCols;
            return cardsY + row * (cardH + cardGap);
        }
    }
}
