package main.java.com.witcher.gdx;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import main.java.com.witcher.gdx.screens.ShopScreen;

/**
 * Корневой класс LibGDX-версии. Постепенно заменит Swing {@code GameWindow}.
 */
public class WitcherGame extends Game {

    public static final float VIRTUAL_W = 480f;
    public static final float VIRTUAL_H = 360f;

    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch(2048);
        setScreen(new ShopScreen(this));
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }
    }
}
