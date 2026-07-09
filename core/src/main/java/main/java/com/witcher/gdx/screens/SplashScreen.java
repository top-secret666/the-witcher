package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.DisplayMetrics;
import main.java.com.witcher.gdx.graphics.GameFrameLayout;
import main.java.com.witcher.gdx.graphics.GdxWindowAlign;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.PixelSpriteSheet;
import main.java.com.witcher.gdx.graphics.PixelTextures;

import java.util.Random;

/**
 * LibGDX-порт {@link main.java.com.witcher.ui.graphics.SplashScreen} — пиксельная чёткость, integer-scale.
 */
public class SplashScreen implements Screen {

    private static final float VW = WitcherGame.VIRTUAL_W;
    private static final float VH = WitcherGame.VIRTUAL_H;

    private static final Color GOLD = new Color(218f / 255f, 165f / 255f, 32f / 255f, 1f);
    private static final Color GOLD_BRIGHT = new Color(1f, 210f / 255f, 80f / 255f, 1f);
    private static final Color GOLD_DARK = new Color(139f / 255f, 90f / 255f, 10f / 255f, 1f);
    private static final Color BAR_BG = new Color(30f / 255f, 22f / 255f, 12f / 255f, 1f);
    private static final Color BAR_BORDER = new Color(140f / 255f, 100f / 255f, 35f / 255f, 1f);
    private static final Color WARM_LIGHT = new Color(1f, 170f / 255f, 85f / 255f, 1f);
    private static final Color SPLASH_BACKDROP = new Color(18f / 255f, 12f / 255f, 8f / 255f, 1f);
    private static final Color SMOKE = new Color(210f / 255f, 205f / 255f, 195f / 255f, 1f);
    private static final boolean DRAW_SMOKE = true;

    /** Скорость загрузки (медленнее, чем было). */
    private static final float ALPHA_STEP = 0.022f;
    private static final int PROGRESS_EVERY_STEPS = 5;
    private static final int END_HOLD_TICKS = 100;

    private final WitcherGame game;
    private OrthographicCamera frameCamera;
    private OrthographicCamera gameCamera;
    private ShapeRenderer shapes;
    private GameFonts fonts;
    private final GlyphLayout glyph = new GlyphLayout();
    private final Random rng = new Random();

    private Texture background;
    private Texture drownerSprite;
    private PixelSpriteSheet logoAnim;
    private PixelSpriteSheet witcherBar;
    private PixelSpriteSheet griffinAnim;

    private float alpha = 0.05f;
    private int progress;
    private boolean finished;
    private boolean transitioning;
    private int timer;
    private int tick;
    private float flicker = 1f;
    /** Как Swing Timer(33 ms) — ~30 кадров/с на логику загрузки. */
    private static final float SIM_STEP = 1f / 30f;
    private float simAccumulator;
    private int progressCooldown;

    private final Array<Particle> particles = new Array<>();
    private final Array<SmokePuff> smokePuffs = new Array<>();
    private BgGutters smokeGutters = BgGutters.none();

    private int logoDrawW;
    private int logoDrawH;
    private float logoTopY;

    private boolean metricsLoggedAfterDraw;

    public SplashScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        frameCamera = new OrthographicCamera();
        gameCamera = new OrthographicCamera();
        shapes = new ShapeRenderer();
        fonts = new GameFonts();
        fonts.load();

        background = PixelTextures.loadOptional("sprites/splash_bg.png");
        drownerSprite = PixelTextures.loadOptional("sprites/drowner_single.png");
        logoAnim = PixelSpriteSheet.load("sprites/witcher_logo_new.png", 2, 3, 8);
        if (logoAnim != null) {
            logoAnim.setPingPong(true);
        }
        witcherBar = PixelSpriteSheet.load("sprites/witcher_bar.png", 5, 1, 15);
        if (witcherBar != null) {
            witcherBar.setPingPong(true);
        }
        griffinAnim = PixelSpriteSheet.load("sprites/griffin_peek.png", 3, 2, 7, true);
        if (griffinAnim != null) {
            griffinAnim.setPingPong(true);
        }

        GdxWindowAlign.ensureFramebuffer(WitcherGame.FRAME_W, WitcherGame.FRAME_H);
        int bbw = GdxWindowAlign.backBufferW();
        int bbh = GdxWindowAlign.backBufferH();
        game.bindChromeFramebuffer(bbw, bbh);
        DisplayMetrics.log("splash-show");
        DisplayMetrics.tryFixWindowSizeMismatch();
        Gdx.app.log("SplashScreen", "assets bg=" + (background != null)
            + " logo=" + (logoAnim != null) + " bar=" + (witcherBar != null)
            + " griffin=" + (griffinAnim != null)
            + " backbuffer=" + GdxWindowAlign.backBufferW() + "x" + GdxWindowAlign.backBufferH()
            + (logoAnim != null ? " logoFrame=" + logoAnim.getFrameWidth() + "x" + logoAnim.getFrameHeight() : "")
            + (witcherBar != null ? " barFrame=" + witcherBar.getFrameWidth() + "x" + witcherBar.getFrameHeight() : ""));
    }

    @Override
    public void render(float delta) {
        simAccumulator += delta;
        while (simAccumulator >= SIM_STEP) {
            simAccumulator -= SIM_STEP;
            simulateStep();
        }

        GdxWindowAlign.refreshFramebufferCache();
        int bbw = GdxWindowAlign.backBufferW();
        int bbh = GdxWindowAlign.backBufferH();
        game.bindChromeFramebuffer(bbw, bbh);
        GameFrameLayout layout = GameFrameLayout.fromFramebuffer(bbw, bbh);

        layout.clearBackdrop(SPLASH_BACKDROP.r, SPLASH_BACKDROP.g, SPLASH_BACKDROP.b);

        layout.bindFullFrame(frameCamera);
        game.batch.setProjectionMatrix(frameCamera.combined);
        shapes.setProjectionMatrix(frameCamera.combined);
        game.frameChrome.drawBackground(shapes);

        layout.bindGame(gameCamera);
        game.batch.setProjectionMatrix(gameCamera.combined);
        shapes.setProjectionMatrix(gameCamera.combined);
        PixelTextures.resetBlend();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(SPLASH_BACKDROP);
        shapes.rect(0f, 0f, VW, VH);
        if (alpha > 0.05f) {
            shapes.setColor(WARM_LIGHT.r, WARM_LIGHT.g, WARM_LIGHT.b, alpha * 0.05f * flicker);
            shapes.rect(0f, 0f, VW, VH);
        }
        shapes.end();

        game.batch.begin();
        drawSprites();
        game.batch.end();

        drawSmokeOverlay();

        game.batch.begin();
        drawSideSprites();
        game.batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawFilledOverlay();
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        drawLines();
        shapes.end();

        game.batch.begin();
        drawLoadText();
        game.batch.end();

        layout.bindFullFrame(frameCamera);
        game.batch.setProjectionMatrix(frameCamera.combined);
        shapes.setProjectionMatrix(frameCamera.combined);
        game.frameChrome.drawForeground(shapes, game.batch);

        if (!metricsLoggedAfterDraw) {
            metricsLoggedAfterDraw = true;
            DisplayMetrics.log("splash-first-frame");
            Gdx.app.log("SplashScreen", "gameRect=" + layout.gameX + ',' + layout.gameY
                + ' ' + layout.gameW + 'x' + layout.gameH + " fb=" + bbw + 'x' + bbh);
        }

        if (finished && !transitioning) {
            transitioning = true;
            game.setScreen(new ShopScreen(game));
        }
    }

    private void simulateStep() {
        tick++;
        float base = 0.86f + 0.10f * (float) Math.sin(tick * 0.12);
        float jitter = (rng.nextFloat() - 0.5f) * 0.10f;
        flicker = clamp(base + jitter, 0.72f, 1f);

        if (alpha < 1f) {
            alpha += ALPHA_STEP;
            if (alpha > 1f) {
                alpha = 1f;
            }
        } else if (progress < 100) {
            progressCooldown++;
            if (progressCooldown >= PROGRESS_EVERY_STEPS) {
                progressCooldown = 0;
                progress++;
            }
        } else {
            timer++;
            if (timer > END_HOLD_TICKS) {
                finished = true;
            }
        }

        if (tick % 4 == 0 && alpha > 0.3f) {
            float px = 40 + rng.nextFloat() * 400;
            float py = VH - (200 + rng.nextFloat() * 120);
            float vx = (rng.nextFloat() - 0.5f) * 0.6f;
            float vy = -(0.2f + rng.nextFloat() * 0.5f);
            int life = 30 + rng.nextInt(50);
            Color c = rng.nextFloat() < 0.6f ? GOLD : GOLD_BRIGHT;
            particles.add(new Particle(px, py, vx, vy, life, c, 2 + rng.nextInt(2)));
        }
        for (int i = particles.size - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (!p.alive) {
                particles.removeIndex(i);
            }
        }

        updateLogoLayout();
        smokeGutters = buildSmokeZone();

        if (tick % 2 == 0 && DRAW_SMOKE && alpha > 0.15f && smokeGutters.hasSides && smokePuffs.size < 20) {
            smokePuffs.add(createSmokePuff());
        }
        for (int i = smokePuffs.size - 1; i >= 0; i--) {
            SmokePuff p = smokePuffs.get(i);
            p.update(smokeGutters);
            if (!p.alive) {
                smokePuffs.removeIndex(i);
            }
        }

        if (witcherBar != null) {
            witcherBar.update();
        }
        if (griffinAnim != null) {
            griffinAnim.update();
        }
        if (logoAnim != null) {
            logoAnim.update();
        }
    }

    private void drawSprites() {
        if (background != null) {
            float a = clamp(alpha * 0.88f, 0f, 1f);
            PixelTextures.drawCover(game.batch, background, VW, VH, a);
        }

        if (logoAnim != null && alpha > 0.05f) {
            float s = Math.min((VW * 0.75f) / logoAnim.getFrameWidth(), (VH * 0.28f) / logoAnim.getFrameHeight());
            int drawW = Math.max(1, Math.round(logoAnim.getFrameWidth() * s));
            int drawH = Math.max(1, Math.round(logoAnim.getFrameHeight() * s));
            float lx = (VW - drawW) / 2f;
            float lyTop = VH * 0.02f;
            float ly = topToBottomY(lyTop, drawH);
            logoAnim.draw(game.batch, lx, ly, drawW, drawH, alpha);
        }
    }

    private void drawSmokeOverlay() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (SmokePuff p : smokePuffs) {
            if (DRAW_SMOKE && smokeGutters.contains(p.x, VH - p.y)) {
                p.draw(shapes);
            }
        }
        shapes.end();
    }

    private void drawSideSprites() {
        if (drownerSprite != null && alpha > 0.2f) {
            int[] bounds = PixelTextures.computeOpaqueBounds("sprites/drowner_single.png");
            float artW = bounds != null ? bounds[2] : drownerSprite.getWidth();
            float artH = bounds != null ? bounds[3] : drownerSprite.getHeight();
            float dpScale = (VH * 0.60f) / artH;
            float dpW = artW * dpScale;
            float dpH = artH * dpScale;
            float swayX = (float) Math.sin(tick * 0.035) * 4;
            float swayY = (float) Math.sin(tick * 0.025 + 1.0) * 3;
            float dpX = -dpW * 0.35f + swayX;
            float dpTop = VH * 0.18f + swayY;
            float dpY = topToBottomY(dpTop, dpH);
            float prev = game.batch.getColor().a;
            game.batch.setColor(1f, 1f, 1f, alpha * 0.92f);
            if (bounds != null) {
                PixelTextures.drawCropped(game.batch, drownerSprite,
                    bounds[0], bounds[1], bounds[2], bounds[3], dpX, dpY, dpW, dpH);
            } else {
                game.batch.draw(drownerSprite, dpX, dpY, dpW, dpH);
            }
            game.batch.setColor(1f, 1f, 1f, prev);
        }

        if (griffinAnim != null && alpha > 0.2f) {
            float gpScale = (VH * 0.60f) / griffinAnim.getFrameHeight();
            float gpW = griffinAnim.getFrameWidth() * gpScale;
            float gpH = griffinAnim.getFrameHeight() * gpScale;
            float swayX = (float) Math.sin(tick * 0.03 + 2.0) * 4;
            float swayY = (float) Math.sin(tick * 0.04) * 3;
            float gpX = VW - gpW * 0.65f + swayX;
            float gpTop = VH * 0.15f + swayY;
            griffinAnim.draw(game.batch, gpX, topToBottomY(gpTop, gpH), gpW, gpH, alpha * 0.92f);
        }

        if (witcherBar != null && alpha > 0.1f) {
            float wbScale = (VW * 0.22f) / witcherBar.getFrameWidth();
            float wbW = witcherBar.getFrameWidth() * wbScale;
            float wbH = witcherBar.getFrameHeight() * wbScale;
            float wbX = (VW - wbW) / 2f;
            float wbTop = VH - wbH + wbH * 0.31f;
            witcherBar.draw(game.batch, wbX, topToBottomY(wbTop, wbH), wbW, wbH, alpha * 0.95f);
        }
    }

    private void updateLogoLayout() {
        if (logoAnim == null) {
            logoDrawW = 0;
            logoDrawH = 0;
            logoTopY = VH * 0.02f;
            return;
        }
        float s = Math.min((VW * 0.75f) / logoAnim.getFrameWidth(), (VH * 0.28f) / logoAnim.getFrameHeight());
        logoDrawW = Math.max(1, Math.round(logoAnim.getFrameWidth() * s));
        logoDrawH = Math.max(1, Math.round(logoAnim.getFrameHeight() * s));
        logoTopY = VH * 0.02f;
    }

    private BgGutters buildSmokeZone() {
        if (background == null) {
            return BgGutters.none();
        }
        BgGutters layout = BgGutters.fromCover(VW, VH, background.getWidth(), background.getHeight());
        float bandTop = Math.max(0f, logoTopY - 2f);
        float bandBottom = logoAnim != null
            ? Math.min(VH * 0.15f, logoTopY + logoDrawH + 6f)
            : VH * 0.12f;
        float bandLeft = layout.bandLeft + 8f;
        float bandRight = layout.bandRight - 8f;
        return layout.asTopBand(bandLeft, bandRight, bandTop, bandBottom);
    }

    private SmokePuff createSmokePuff() {
        float px = smokeGutters.randomX(rng);
        float visualY = smokeGutters.randomVisualY(rng);
        float py = VH - visualY;
        float vx = (rng.nextFloat() - 0.5f) * 0.10f;
        float vy = (rng.nextFloat() - 0.5f) * 0.006f;
        int life = 80 + rng.nextInt(60);
        float r = 4f + rng.nextFloat() * 6f;
        return new SmokePuff(px, py, vx, vy, life, r);
    }

    /** Горизонтальная полоса дыма сверху по центру (не на боковых анимациях). */
    private static final class BgGutters {
        final float bandLeft;
        final float bandRight;
        final float visualTop;
        final float visualBottom;
        final boolean hasSides;

        private BgGutters(float bandLeft, float bandRight, float visualTop, float visualBottom,
                          boolean hasSides) {
            this.bandLeft = bandLeft;
            this.bandRight = bandRight;
            this.visualTop = visualTop;
            this.visualBottom = visualBottom;
            this.hasSides = hasSides;
        }

        static BgGutters none() {
            return new BgGutters(0f, 0f, 0f, 0f, false);
        }

        BgGutters asTopBand(float left, float right, float top, float bottom) {
            boolean active = (right - left) > 24f && (bottom - top) > 4f;
            return new BgGutters(left, right, top, bottom, active);
        }

        static BgGutters fromCover(float sw, float sh, float srcW, float srcH) {
            float cover = Math.max(sw / srcW, sh / srcH);
            float drawW = srcW * cover;
            float dx = (sw - drawW) * 0.5f;
            return new BgGutters(dx, dx + drawW, 0f, sh, true);
        }

        boolean contains(float x, float visualY) {
            return hasSides
                && x >= bandLeft
                && x <= bandRight
                && visualY >= visualTop
                && visualY <= visualBottom;
        }

        float randomX(Random rng) {
            return bandLeft + rng.nextFloat() * Math.max(4f, bandRight - bandLeft);
        }

        float randomVisualY(Random rng) {
            return visualTop + rng.nextFloat() * Math.max(2f, visualBottom - visualTop);
        }
    }

    private void drawTexture(Texture texture, float x, float y, float w, float h, float a) {
        float prev = game.batch.getColor().a;
        game.batch.setColor(1f, 1f, 1f, a);
        game.batch.draw(texture, x, y, w, h);
        game.batch.setColor(1f, 1f, 1f, prev);
    }

    private void drawFilledOverlay() {
        for (Particle p : particles) {
            p.draw(shapes);
        }

        float barZoneH = 28f;
        shapes.setColor(0f, 0f, 0f, 0.7f);
        shapes.rect(0f, 0f, VW, barZoneH);

        int barW = Math.round(VW * 0.55f);
        int barH = 8;
        float barX = (VW - barW) / 2f;
        float barBottom = 20f - barH;

        shapes.setColor(BAR_BG);
        shapes.rect(barX, barBottom, barW, barH);

        int fillW = Math.round(barW * (progress / 100f));
        if (fillW > 0) {
            shapes.setColor(GOLD_DARK);
            shapes.rect(barX, barBottom, fillW, barH);
            shapes.setColor(1f, 1f, 1f, 0.3f);
            shapes.rect(barX, barBottom + barH - 3f, fillW, 3f);
        }

        shapes.setColor(0f, 0f, 0f, alpha * 0.30f);
        shapes.rect(0f, 0f, VW * 0.12f, VH);
        shapes.rect(VW * 0.88f, 0f, VW * 0.12f, VH);
        shapes.rect(0f, VH * 0.72f, VW, VH * 0.28f);
    }

    private void drawLines() {
        int barW = Math.round(VW * 0.55f);
        int barH = 8;
        float barX = (VW - barW) / 2f;
        float barBottom = 20f - barH;
        shapes.setColor(BAR_BORDER);
        shapes.rect(barX, barBottom, barW, barH);

        if (logoAnim != null && alpha > 0.05f) {
            float s = Math.min((VW * 0.75f) / logoAnim.getFrameWidth(), (VH * 0.28f) / logoAnim.getFrameHeight());
            int drawW = Math.max(1, Math.round(logoAnim.getFrameWidth() * s));
            int drawH = Math.max(1, Math.round(logoAnim.getFrameHeight() * s));
            float lx = (VW - drawW) / 2f;
            float ly = topToBottomY(VH * 0.02f, drawH);
            shapes.setColor(0f, 0f, 0f, alpha * 0.4f);
            shapes.rect(lx - 4f, ly - 2f, drawW + 8f, drawH + 4f);
        }
    }

    private void drawLoadText() {
        int barH = 8;
        float barBottom = 20f - barH;
        String loadText = "Загрузка... " + progress + "%";
        glyph.setText(fonts.uiSmall, loadText);
        fonts.uiSmall.setColor(GOLD);
        float textX = (VW - glyph.width) / 2f;
        float textY = barBottom + barH + glyph.height + 4f;
        fonts.uiSmall.draw(game.batch, loadText, textX, textY);
    }

    private static float topToBottomY(float top, float height) {
        return VH - top - height;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public void resize(int width, int height) {
        GdxWindowAlign.refreshFramebufferCache();
        DisplayMetrics.log("splash-resize");
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
        PixelTextures.dispose(background);
        PixelTextures.dispose(drownerSprite);
        if (logoAnim != null) {
            logoAnim.dispose();
        }
        if (witcherBar != null) {
            witcherBar.dispose();
        }
        if (griffinAnim != null) {
            griffinAnim.dispose();
        }
        if (fonts != null) {
            fonts.dispose();
        }
        if (shapes != null) {
            shapes.dispose();
        }
        background = null;
        drownerSprite = null;
        logoAnim = null;
        witcherBar = null;
        griffinAnim = null;
    }

    private static final class Particle {
        float x;
        float y;
        float vx;
        float vy;
        int life;
        int maxLife;
        int size;
        Color color;
        boolean alive = true;

        Particle(float x, float y, float vx, float vy, int life, Color color, int size) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.color = color;
            this.size = size;
        }

        void update() {
            x += vx;
            y += vy;
            life--;
            if (life <= 0) {
                alive = false;
            }
        }

        void draw(ShapeRenderer shapes) {
            float a = (life / (float) maxLife) * 0.7f;
            shapes.setColor(color.r, color.g, color.b, a);
            shapes.rect(x, y, size, size);
        }
    }

    private static final class SmokePuff {
        float x;
        float y;
        float vx;
        float vy;
        int life;
        int maxLife;
        float r;
        boolean alive = true;

        SmokePuff(float x, float y, float vx, float vy, int life, float r) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.r = r;
        }

        void update(BgGutters gutters) {
            x += vx;
            y += vy;
            r += 0.04f;
            if (r > 22f) {
                r = 22f;
            }
            if (gutters.hasSides && !gutters.contains(x, VH - y)) {
                alive = false;
            }
            life--;
            if (life <= 0) {
                alive = false;
            }
        }

        void draw(ShapeRenderer shapes) {
            float t = 1f - (life / (float) maxLife);
            float a = (float) (Math.sin(t * Math.PI) * 0.20f);
            if (a <= 0f) {
                return;
            }
            shapes.setColor(SMOKE.r, SMOKE.g, SMOKE.b, a * 0.32f);
            shapes.circle(x, y, r);
            shapes.setColor(SMOKE.r, SMOKE.g, SMOKE.b, a * 0.18f);
            shapes.circle(x - 6f, y + 3f, r * 0.9f);
            shapes.circle(x + 8f, y - 2f, r * 0.85f);
        }
    }
}
