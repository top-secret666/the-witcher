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
import com.badlogic.gdx.utils.viewport.Viewport;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.IntegerScaleViewport;
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
    private static final Color SMOKE = new Color(190f / 255f, 180f / 255f, 165f / 255f, 1f);

    private final WitcherGame game;
    private Viewport viewport;
    private OrthographicCamera camera;
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

    private final Array<Particle> particles = new Array<>();
    private final Array<SmokePuff> smokePuffs = new Array<>();

    public SplashScreen(WitcherGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        viewport = new IntegerScaleViewport((int) VW, (int) VH, camera);
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

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        Gdx.app.log("SplashScreen", "assets bg=" + (background != null)
            + " logo=" + (logoAnim != null) + " bar=" + (witcherBar != null)
            + " viewport scale=" + ((IntegerScaleViewport) viewport).getScale());
    }

    @Override
    public void render(float delta) {
        tick++;
        updateLogic();

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        viewport.apply();

        game.batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        PixelTextures.resetBlend();

        game.batch.begin();
        drawSprites();
        game.batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawFilled();
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        drawLines();
        shapes.end();

        if (finished && !transitioning) {
            transitioning = true;
            game.setScreen(new BootScreen(game));
        }
    }

    private void updateLogic() {
        float base = 0.86f + 0.10f * (float) Math.sin(tick * 0.12);
        float jitter = (rng.nextFloat() - 0.5f) * 0.10f;
        flicker = clamp(base + jitter, 0.72f, 1f);

        if (alpha < 1f) {
            alpha += 0.06f;
            if (alpha > 1f) {
                alpha = 1f;
            }
        } else if (progress < 100) {
            progress++;
        } else {
            timer++;
            if (timer > 80) {
                finished = true;
            }
        }

        if (tick % 4 == 0 && alpha > 0.3f) {
            float px = 40 + rng.nextFloat() * 400;
            float py = 10 + rng.nextFloat() * 180;
            float vx = (rng.nextFloat() - 0.5f) * 0.6f;
            float vy = -0.2f - rng.nextFloat() * 0.5f;
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

        if (tick % 8 == 0 && alpha > 0.25f) {
            float px = 35 + rng.nextFloat() * 410;
            float py = 60 + rng.nextFloat() * 120;
            float vx = (rng.nextFloat() - 0.5f) * 0.18f;
            float vy = -0.12f - rng.nextFloat() * 0.22f;
            int life = 120 + rng.nextInt(120);
            float r = 10 + rng.nextInt(18);
            smokePuffs.add(new SmokePuff(px, py, vx, vy, life, r));
        }
        for (int i = smokePuffs.size - 1; i >= 0; i--) {
            SmokePuff p = smokePuffs.get(i);
            p.update();
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
            PixelTextures.drawContainInteger(game.batch, background, VW, VH, 0.94f, clamp(alpha * 0.88f, 0f, 1f));
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

        if (drownerSprite != null && alpha > 0.2f) {
            float dpScale = (VH * 0.60f) / drownerSprite.getHeight();
            int dpW = Math.round(drownerSprite.getWidth() * dpScale);
            int dpH = Math.round(drownerSprite.getHeight() * dpScale);
            float swayX = (float) Math.sin(tick * 0.035) * 4;
            float swayY = (float) Math.sin(tick * 0.025 + 1.0) * 3;
            float dpX = -dpW * 0.35f + swayX;
            float dpTop = VH * 0.18f + swayY;
            drawTexture(drownerSprite, dpX, topToBottomY(dpTop, dpH), dpW, dpH, alpha * 0.92f);
        }

        if (griffinAnim != null && alpha > 0.2f) {
            float gpScale = (VH * 0.60f) / griffinAnim.getFrameHeight();
            int gpW = Math.round(griffinAnim.getFrameWidth() * gpScale);
            int gpH = Math.round(griffinAnim.getFrameHeight() * gpScale);
            float swayX = (float) Math.sin(tick * 0.03 + 2.0) * 4;
            float swayY = (float) Math.sin(tick * 0.04) * 3;
            float gpX = VW - gpW * 0.65f + swayX;
            float gpTop = VH * 0.15f + swayY;
            griffinAnim.draw(game.batch, gpX, topToBottomY(gpTop, gpH), gpW, gpH, alpha * 0.92f);
        }

        if (witcherBar != null && alpha > 0.1f) {
            float wbScale = (VW * 0.22f) / witcherBar.getFrameWidth();
            int wbW = Math.round(witcherBar.getFrameWidth() * wbScale);
            int wbH = Math.round(witcherBar.getFrameHeight() * wbScale);
            float wbX = (VW - wbW) / 2f;
            float wbTop = VH - wbH + wbH * 0.31f;
            witcherBar.draw(game.batch, wbX, topToBottomY(wbTop, wbH), wbW, wbH, alpha * 0.95f);
        }
    }

    private void drawTexture(Texture texture, float x, float y, float w, float h, float a) {
        float prev = game.batch.getColor().a;
        game.batch.setColor(1f, 1f, 1f, a);
        game.batch.draw(texture, Math.round(x), Math.round(y), Math.round(w), Math.round(h));
        game.batch.setColor(1f, 1f, 1f, prev);
    }

    private void drawFilled() {
        if (alpha > 0.05f) {
            shapes.setColor(WARM_LIGHT.r, WARM_LIGHT.g, WARM_LIGHT.b, alpha * 0.05f * flicker);
            shapes.rect(0f, 0f, VW, VH);
        }

        for (Particle p : particles) {
            p.draw(shapes);
        }
        for (SmokePuff p : smokePuffs) {
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

        game.batch.begin();
        String loadText = "Загрузка... " + progress + "%";
        glyph.setText(fonts.uiSmall, loadText);
        fonts.uiSmall.setColor(GOLD);
        float textX = (VW - glyph.width) / 2f;
        float textY = barBottom + barH + glyph.height + 4f;
        fonts.uiSmall.draw(game.batch, loadText, textX, textY);
        game.batch.end();
    }

    private static float topToBottomY(float top, float height) {
        return VH - top - height;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
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
            float drawY = topToBottomY(y, size);
            shapes.rect(x, drawY, size, size);
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

        void update() {
            x += vx;
            y += vy;
            r += 0.03f;
            life--;
            if (life <= 0) {
                alive = false;
            }
        }

        void draw(ShapeRenderer shapes) {
            float t = 1f - (life / (float) maxLife);
            float a = (float) (Math.sin(t * Math.PI) * 0.22f);
            if (a <= 0f) {
                return;
            }
            shapes.setColor(SMOKE.r, SMOKE.g, SMOKE.b, a);
            float drawY = topToBottomY(y, r * 2f);
            shapes.circle(x, drawY + r, r);
            shapes.setColor(SMOKE.r, SMOKE.g, SMOKE.b, a * 0.55f);
            shapes.circle(x - 4f, drawY + r + 2f, r);
            shapes.circle(x + 6f, drawY + r - 1f, r);
        }
    }
}
