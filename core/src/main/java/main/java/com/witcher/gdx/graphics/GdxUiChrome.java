package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/** Кнопки chrome (крестик закрытия) — аналог {@code UiChrome} (Swing). */
public final class GdxUiChrome implements Disposable {

    public static final int BTN_SIZE = 18;

    private Texture closeNormal;
    private Texture closeHover;

    public static GdxUiChrome load() {
        GdxUiChrome chrome = new GdxUiChrome();
        chrome.closeNormal = PixelTextures.loadLavka("ui/ui_btn_close.png");
        chrome.closeHover = PixelTextures.loadLavka("ui/ui_btn_close_hover.png");
        if (chrome.closeNormal == null) {
            chrome.closeNormal = PixelTextures.loadLavka("1x/ui/ui_btn_close.png");
        }
        if (chrome.closeHover == null) {
            chrome.closeHover = PixelTextures.loadLavka("1x/ui/ui_btn_close_hover.png");
        }
        return chrome;
    }

    public void drawCloseButton(SpriteBatch batch, SwingCoords C, float swingX, float swingTopY,
                                float width, float height, boolean hovered, float alpha) {
        Texture tex = hovered && closeHover != null ? closeHover : closeNormal;
        if (tex == null) {
            return;
        }
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        float drawW = BTN_SIZE;
        float drawH = BTN_SIZE;
        float dx = swingX + (width - drawW) * 0.5f;
        float dy = swingTopY + (height - drawH) * 0.5f;
        batch.draw(tex, dx, C.rectY(dy, drawH), drawW, drawH);
        batch.setColor(1f, 1f, 1f, prev);
    }

    @Override
    public void dispose() {
        disposeTex(closeNormal);
        disposeTex(closeHover);
        closeNormal = null;
        closeHover = null;
    }

    private static void disposeTex(Texture t) {
        if (t != null) {
            t.dispose();
        }
    }
}
