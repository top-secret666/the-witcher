package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.math.Vector2;
import main.java.com.witcher.gdx.WitcherGame;

/** Экранные координаты → виртуальный кадр 480×360 внутри game viewport. */
public final class GdxShopPointer {

    private final Vector2 tmp = new Vector2();

    public void toVirtual(GameFrameLayout layout, int screenX, int screenY, Vector2 out) {
        float nx = (screenX - layout.gameX) / (float) layout.gameW;
        float ny = 1f - (screenY - layout.gameY) / (float) layout.gameH;
        out.x = nx * WitcherGame.VIRTUAL_W;
        out.y = ny * WitcherGame.VIRTUAL_H;
    }

    public Vector2 toVirtual(GameFrameLayout layout, int screenX, int screenY) {
        toVirtual(layout, screenX, screenY, tmp);
        return tmp;
    }
}
