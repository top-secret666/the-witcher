package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.math.Vector2;
import main.java.com.witcher.gdx.WitcherGame;

/** Экранные координаты → виртуальный кадр (логика в Swing-Y). */
public final class GdxShopPointer {

    private static final SwingCoords COORDS = SwingCoords.forVirtualFrame();
    private final Vector2 tmp = new Vector2();

    /** LibGDX: origin снизу слева. */
    public void toVirtual(GameFrameLayout layout, int screenX, int screenY, Vector2 out) {
        float nx = (screenX - layout.gameX) / (float) layout.gameW;
        float ny = 1f - (screenY - layout.gameY) / (float) layout.gameH;
        out.x = nx * WitcherGame.VIRTUAL_W;
        out.y = ny * WitcherGame.VIRTUAL_H;
    }

    /** Swing: origin сверху слева — для {@link main.java.com.witcher.ui.shop.presenter.ShopPresenter}. */
    public void toSwing(GameFrameLayout layout, int screenX, int screenY, Vector2 outSwing) {
        toVirtual(layout, screenX, screenY, tmp);
        outSwing.x = tmp.x;
        outSwing.y = COORDS.gdxToSwingY(tmp.y);
    }

    public Vector2 toVirtual(GameFrameLayout layout, int screenX, int screenY) {
        toVirtual(layout, screenX, screenY, tmp);
        return tmp;
    }
}
