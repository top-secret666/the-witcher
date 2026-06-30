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
import com.badlogic.gdx.utils.viewport.FitViewport;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.shop.ShopAssets;
import main.java.com.witcher.gdx.shop.ShopItem;

import java.util.Random;

/**
 * LibGDX-порт {@code main.java.com.witcher.ui.graphics.ShopScreen} — та же логика и порядок отрисовки.
 */
public class ShopScreen implements Screen {

    private static final float DIALOG_ZONE = 54f;
    private static final Color DUKE_GOLD = new Color(218f / 255f, 165f / 255f, 32f / 255f, 1f);
    private static final Color SPEECH = new Color(220f / 255f, 190f / 255f, 100f / 255f, 1f);
    private static final Color WALLET = new Color(1f, 230f / 255f, 150f / 255f, 1f);
    private static final Color WALLET_SUFFIX = new Color(200f / 255f, 180f / 255f, 120f / 255f, 1f);

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
            Броня, кирасы, шлемы, наколенники — всё, что душе угодно.
            Только не забудьте кошелёк...""";

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
        items.add(new ShopItem("Кираса волчьей школы", "120",
            "Отличный выбор! Волчья сталь — как раз для таких, как вы.",
            new String[]{"Защита: 45", "Вес: 12", "Тип: кираса"}, assets.iconChest));
        items.add(new ShopItem("Укреплённые штаны", "45",
            "Штаны крепкие. Ноги целее — монстров больше.",
            new String[]{"Защита: 20", "Вес: 8", "Тип: поножи"}, assets.iconLegs));
        items.add(new ShopItem("Перчатки наездника", "30",
            "Рукам тепло, клинку — верно. Берите, не пожалеете.",
            new String[]{"Защита: 12", "Вес: 3", "Тип: перчатки"}, assets.iconGloves));
        items.add(new ShopItem("Сапоги стражника", "55",
            "В этих сапогах и по болоту пройдёте, и от удара отскочите.",
            new String[]{"Защита: 18", "Вес: 6", "Тип: сапоги"}, assets.iconBoots));
        items.add(new ShopItem("Зелье «Чёрный гриф»", "15",
            "Хм... Зелье? Ну что ж, ваш выбор, Белый Волк...",
            new String[]{"Эффект: яд", "Вес: 0.5", "⚠ без чекпоинта"}, assets.iconPotion));
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

        ShopLayout layout = new ShopLayout(sw, sh, items.size, assets.hud.drawX, assets.hud.drawW, assets.hud.drawH);

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
                cardFaceBack[hoveredIndex] = !cardFaceBack[hoveredIndex];
            }
        }
        if (!Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            clickLatch = false;
        }

        for (int i = 0; i < cardFlip.length; i++) {
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
        float panelH = layout.cardsTop + layout.cardH - layout.panelTop + 8f;
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

        float anchor = hudX + hudW * 0.68f;
        float textX = anchor - suffixW - walletW;
        float crownY = hudBottom + (hudH - crownSize) * 0.5f;
        if (assets.crownIcon != null) {
            textX -= crownSize + 4f;
            game.batch.draw(assets.crownIcon, textX, crownY, crownSize, crownSize);
            textX += crownSize + 4f;
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

        fonts.ui.setColor(DUKE_GOLD);
        drawCentered(fonts.ui, "— Товары —", layout.panelX + layout.panelW * 0.5f,
            textYFromTop(layout.panelTop + 16f, sh));

        for (int i = 0; i < items.size; i++) {
            ShopItem item = items.get(i);
            float x = layout.cardsStartX + i * (layout.cardW + layout.cardGap);
            float y = bottomFromTop(layout.cardsTop, layout.cardH, sh);
            item.bounds.set(x, y, layout.cardW, layout.cardH);
            drawItemCard(item, x, y, layout.cardW, layout.cardH,
                i == selectedIndex, i == hoveredIndex, cardFlip[i]);
        }
    }

    private void drawItemCard(ShopItem item, float x, float y, float w, float h,
                              boolean selected, boolean hovered, float flip) {
        boolean showBack = flip >= 0.5f;
        float fade = 1f;
        if (flip > 0.05f && flip < 0.95f) {
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
                cardRect = drawAspectFit(assets.cardBack, x, y, w, h);
            }
            drawCardBack(item, cardRect);
        } else {
            if (frame != null) {
                cardRect = drawAspectFit(frame, x, y, w, h);
            }
            drawCardFront(item, cardRect);
        }
        game.batch.setColor(1f, 1f, 1f, prevA);
    }

    private Rectangle drawAspectFit(Texture img, float x, float y, float w, float h) {
        float tw = img.getWidth();
        float th = img.getHeight();
        float srcAspect = tw / th;
        float dstAspect = w / h;
        float drawW;
        float drawH;
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

    private void drawCardFront(ShopItem item, Rectangle card) {
        Texture art = item.icon;
        if (art != null) {
            float artSize = Math.min(card.width - 10f, card.height - 32f);
            game.batch.draw(art,
                card.x + (card.width - artSize) * 0.5f, card.y + 6f, artSize, artSize);
        }
        BitmapFont small = fonts.uiSmall;
        small.setColor(235f / 255f, 215f / 255f, 155f / 255f, 1f);
        String name = truncateToWidth(small, item.name, card.width - 6f);
        drawCentered(small, name, card.x + card.width * 0.5f, card.y + card.height - 17f);

        float crownSize = 10f;
        glyph.setText(small, item.priceLabel);
        float priceW = glyph.width;
        if (assets.crownIcon != null) {
            priceW += crownSize + 2f;
        }
        float priceX = card.x + (card.width - priceW) * 0.5f;
        if (assets.crownIcon != null) {
            game.batch.draw(assets.crownIcon, priceX, card.y + card.height - 11f, crownSize, crownSize);
            priceX += crownSize + 2f;
        }
        small.setColor(255f / 255f, 230f / 255f, 120f / 255f, 1f);
        small.draw(game.batch, item.priceLabel, priceX, card.y + card.height - 3f);
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
        if (assets.btnBuyDisabled != null) {
            game.batch.draw(assets.btnBuyDisabled, layout.btnX, btnY, layout.btnW, layout.btnH);
        }
        fonts.ui.setColor(90f / 255f, 75f / 255f, 50f / 255f, 1f);
        drawCentered(fonts.ui, "Скоро", layout.btnX + layout.btnW * 0.5f,
            textYFromTop(layout.btnTop + 19f, sh));
    }

    /** Порт {@link main.java.com.witcher.ui.graphics.DialogBoxRenderer#drawCompactFramedSpeakerText}. */
    private void drawCompactDialog(float sh, String text) {
        PixelTextures.resetBlend();
        float sw = WitcherGame.VIRTUAL_W;
        float fontSize = Math.max(12f, sh * 0.036f);
        float boxMarginX = 10f;
        float boxMarginBottom = 5f;
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
        final float hudTop = 12f;
        final float hudX;
        final float hudW;
        final float hudH;
        final float panelTop;
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
        final float dialogTop;

        ShopLayout(float sw, float sh, int itemCount, int hudX, int hudW, int hudH) {
            this.hudX = hudX;
            this.hudW = hudW;
            this.hudH = hudH;
            dialogTop = sh - DIALOG_ZONE;
            float headerH = 22f;

            int cardsTotalW = itemCount * (int) cardW + (itemCount - 1) * (int) cardGap;
            panelW = Math.max(cardsTotalW + 28, Math.min(sw - 88f, 380f));
            panelX = (sw - panelW) * 0.5f;
            panelTop = hudTop + hudH + 6f;
            cardsTop = panelTop + headerH + 6f;
            float contentBottom = cardsTop + cardH;
            btnTop = contentBottom + 6f;
            cardsStartX = panelX + (panelW - cardsTotalW) * 0.5f;
            btnX = panelX + (panelW - btnW) * 0.5f;
        }
    }
}
