package main.java.com.witcher.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import main.java.com.witcher.gdx.graphics.DisplayMetrics;
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

    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch(2048);
        DisplayMetrics.log("game-create");
        setScreen(new SplashScreen(this));
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
    }
}
