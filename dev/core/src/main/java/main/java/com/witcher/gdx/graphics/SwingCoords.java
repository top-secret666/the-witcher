package main.java.com.witcher.gdx.graphics;

import main.java.com.witcher.gdx.WitcherGame;

/**
 * Единый перевод координат: логика и {@link main.java.com.witcher.ui.shop.view.ShopLayout}
 * используют Swing (Y сверху вниз, 480×360); LibGDX рисует с Y снизу вверх.
 */
public final class SwingCoords {

    private final float viewW;
    private final float viewH;

    public static SwingCoords forVirtualFrame() {
        return new SwingCoords(WitcherGame.VIRTUAL_W, WitcherGame.VIRTUAL_H);
    }

    public SwingCoords(float viewW, float viewH) {
        this.viewW = viewW;
        this.viewH = viewH;
    }

    public float viewWidth() {
        return viewW;
    }

    public float viewHeight() {
        return viewH;
    }

    /** Прямоугольник: верхний левый угол в Swing → нижний левый Y в LibGDX. */
    public float rectY(float topY, float height) {
        return viewH - topY - height;
    }

    public float rectX(float swingX) {
        return swingX;
    }

    /** Baseline текста BitmapFont при offset от верха кадра (Swing). */
    public float textBaseline(float topY) {
        return viewH - topY;
    }

    /** Центр по вертикали: swing top + height/2 → gdx Y центра. */
    public float centerY(float topY, float height) {
        return viewH - topY - height * 0.5f;
    }

    public float swingToGdxY(float swingY) {
        return viewH - swingY;
    }

    public float gdxToSwingY(float gdxY) {
        return viewH - gdxY;
    }
}
