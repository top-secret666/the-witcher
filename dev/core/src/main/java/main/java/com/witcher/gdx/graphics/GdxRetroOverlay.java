package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/** Лёгкий ретро-оверлей на GPU (без readback framebuffer). */
public final class GdxRetroOverlay {

    private GdxRetroOverlay() {
    }

    public static void drawVignette(ShapeRenderer shapes, float viewW, float viewH) {
        float side = viewW * 0.14f;
        float top = viewH * 0.1f;
        shapes.setColor(0f, 0f, 0f, 0.14f);
        shapes.rect(0f, 0f, side, viewH);
        shapes.rect(viewW - side, 0f, side, viewH);
        shapes.rect(0f, viewH - top, viewW, top);
        shapes.rect(0f, 0f, viewW, top * 0.65f);
    }

    public static void drawScanlines(ShapeRenderer shapes, float viewW, float viewH) {
        shapes.setColor(0f, 0f, 0f, 0.035f);
        for (float y = 0f; y < viewH; y += 3f) {
            shapes.rect(0f, y, viewW, 1f);
        }
    }
}
