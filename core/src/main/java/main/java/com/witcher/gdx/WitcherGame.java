package main.java.com.witcher.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import main.java.com.witcher.gdx.graphics.DisplayMetrics;
import main.java.com.witcher.gdx.graphics.GdxWindowAlign;
import main.java.com.witcher.gdx.graphics.PixelFrameChrome;
import main.java.com.witcher.gdx.screens.SplashScreen;

/**
 * Корневой класс LibGDX-версии. Постепенно заменит Swing {@code GameWindow}.
 */
public class WitcherGame extends Game {

    public static final float VIRTUAL_W = 480f;
    public static final float VIRTUAL_H = 360f;
    public static final int PIXEL_SCALE = 2;
    public static final int WINDOW_W = (int) (VIRTUAL_W * PIXEL_SCALE);
    public static final int WINDOW_H = (int) (VIRTUAL_H * PIXEL_SCALE);

    /** Как Swing {@code PixelTitleBar} + {@code PixelBorder}. */
    public static final int TITLE_H = 30;
    public static final int BORDER = 10;
    public static final int FRAME_W = WINDOW_W + BORDER * 2;
    public static final int FRAME_H = WINDOW_H + TITLE_H + BORDER * 2;
    public static final float FRAME_VW = FRAME_W / (float) PIXEL_SCALE;
    public static final float FRAME_VH = FRAME_H / (float) PIXEL_SCALE;
    public static final float BORDER_V = BORDER / (float) PIXEL_SCALE;
    public static final float TITLE_V = TITLE_H / (float) PIXEL_SCALE;
    public static final float GAME_VX = BORDER_V;
    public static final float GAME_VY = BORDER_V;

    public SpriteBatch batch;
    public final PixelFrameChrome frameChrome = new PixelFrameChrome();

    @Override
    public void create() {
        batch = new SpriteBatch(2048);
        frameChrome.loadFont();
        GdxWindowAlign.ensureFramebuffer(FRAME_W, FRAME_H);
        DisplayMetrics.log("game-create");
        Gdx.input.setInputProcessor(frameChrome);
        setScreen(new SplashScreen(this));
    }

    public void bindChromeFramebuffer(int width, int height) {
        frameChrome.bindFramebuffer(width, height);
    }

    @Override
    public void dispose() {
        frameChrome.dispose();
        if (batch != null) {
            batch.dispose();
        }
    }
}
