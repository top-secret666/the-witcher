package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import main.java.com.witcher.gdx.WitcherGame;
import org.lwjgl.glfw.GLFW;

/**
 * Пиксельная шапка и рамка — координаты в пикселях {@code FRAME_W}×{@code FRAME_H}.
 */
public final class PixelFrameChrome extends InputAdapter {

    private static final Color TITLE_BG = new Color(30f / 255f, 22f / 255f, 12f / 255f, 1f);
    private static final Color TITLE_FG = new Color(218f / 255f, 165f / 255f, 32f / 255f, 1f);
    private static final Color TITLE_BORDER = new Color(140f / 255f, 100f / 255f, 35f / 255f, 1f);
    private static final Color TITLE_HOVER = new Color(230f / 255f, 180f / 255f, 60f / 255f, 1f);

    private final GlyphLayout glyph = new GlyphLayout();

    private BitmapFont titleFont;
    private int fbW = WitcherGame.FRAME_W;
    private int fbH = WitcherGame.FRAME_H;
    private boolean hoverMinimize;
    private boolean hoverClose;
    private boolean pressedMinimize;
    private boolean pressedClose;
    private boolean dragging;
    private long dragHandle;
    private double dragCursorStartX;
    private double dragCursorStartY;
    private int dragWinX;
    private int dragWinY;

    public void loadFont() {
        if (titleFont == null) {
            titleFont = new BitmapFont();
            titleFont.getData().setScale(1.1f);
        }
    }

    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
    }

    public void bindFramebuffer(int width, int height) {
        fbW = Math.max(1, width);
        fbH = Math.max(1, height);
    }

    public void drawBackground(ShapeRenderer shapes) {
        float fw = WitcherGame.FRAME_W;
        float fh = WitcherGame.FRAME_H;
        float b = WitcherGame.BORDER;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(TITLE_BORDER);
        shapes.rect(0f, fh - b, fw, b);
        shapes.rect(0f, 0f, fw, b);
        shapes.rect(0f, 0f, b, fh);
        shapes.rect(fw - b, 0f, b, fh);

        shapes.setColor(TITLE_FG);
        shapes.rect(1f, fh - b + 1f, fw - 2f, 1f);
        shapes.rect(1f, b - 1f, fw - 2f, 1f);
        shapes.rect(1f, b, 1f, fh - 2f * b);
        shapes.rect(fw - 2f, b, 1f, fh - 2f * b);
        shapes.end();
    }

    public void drawForeground(ShapeRenderer shapes, SpriteBatch batch) {
        float fh = WitcherGame.FRAME_H;
        float b = WitcherGame.BORDER;
        float th = WitcherGame.TITLE_H;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(TITLE_BG);
        shapes.rect(b, fh - b - th, WitcherGame.WINDOW_W, th);
        shapes.setColor(TITLE_BORDER);
        shapes.rect(b, fh - b - th, WitcherGame.WINDOW_W, 1f);
        shapes.end();

        drawButton(shapes, minimizeBounds(), hoverMinimize, pressedMinimize, false);
        drawButton(shapes, closeBounds(), hoverClose, pressedClose, true);

        if (titleFont != null) {
            batch.begin();
            titleFont.setColor(TITLE_FG);
            String title = "The Witcher — LibGDX";
            glyph.setText(titleFont, title);
            float tx = b + 8f;
            float ty = fh - b - th + (th + glyph.height) * 0.5f - 2f;
            titleFont.draw(batch, title, tx, ty);
            batch.end();
        }
    }

    private void drawButton(ShapeRenderer shapes, float[] r, boolean hover, boolean pressed, boolean close) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        Color fill = pressed ? TITLE_FG : (hover ? TITLE_HOVER : TITLE_BG);
        shapes.setColor(fill);
        shapes.rect(r[0], r[1], r[2], r[3]);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(TITLE_FG);
        shapes.rect(r[0], r[1], r[2], r[3]);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(pressed ? TITLE_BG : TITLE_FG);
        if (close) {
            float pad = Math.max(2f, r[2] / 6f);
            float len = r[2] - pad * 2f;
            for (int i = 0; i < (int) len; i++) {
                shapes.rect(r[0] + pad + i, r[1] + pad + i, 1f, 1f);
                shapes.rect(r[0] + r[2] - pad - i, r[1] + pad + i, 1f, 1f);
            }
        } else {
            float barH = 5f;
            float barW = r[2] - 6f;
            shapes.rect(r[0] + (r[2] - barW) * 0.5f, r[1] + (r[3] - barH) * 0.5f, barW, barH);
        }
        shapes.end();
    }

    private float[] minimizeBounds() {
        float[] close = closeBounds();
        float btn = close[2];
        return new float[] { close[0] - 4f - btn, close[1], btn, btn };
    }

    private float[] closeBounds() {
        float fh = WitcherGame.FRAME_H;
        float b = WitcherGame.BORDER;
        float th = WitcherGame.TITLE_H;
        float btn = th - 4f;
        float x = WitcherGame.FRAME_W - b - 6f - btn;
        float y = fh - b - th + (th - btn) * 0.5f;
        return new float[] { x, y, btn, btn };
    }

    private float[] titleDragBounds() {
        float b = WitcherGame.BORDER;
        float th = WitcherGame.TITLE_H;
        float fh = WitcherGame.FRAME_H;
        return new float[] { b, fh - b - th, WitcherGame.WINDOW_W, th };
    }

    private boolean hit(float[] r, float wx, float wy) {
        return wx >= r[0] && wx <= r[0] + r[2] && wy >= r[1] && wy <= r[1] + r[3];
    }

    private float[] screenToFrame(int sx, int sy) {
        int bx = sx;
        int by = sy;
        if (Gdx.graphics instanceof Lwjgl3Graphics g) {
            long handle = g.getWindow().getWindowHandle();
            int[] winW = new int[1];
            int[] winH = new int[1];
            int[] fbW = new int[1];
            int[] fbH = new int[1];
            GLFW.glfwGetWindowSize(handle, winW, winH);
            GLFW.glfwGetFramebufferSize(handle, fbW, fbH);
            if (winW[0] > 0 && fbW[0] > 0 && winW[0] != fbW[0]) {
                bx = Math.round(sx * (fbW[0] / (float) winW[0]));
            }
            if (winH[0] > 0 && fbH[0] > 0 && winH[0] != fbH[0]) {
                by = Math.round(sy * (fbH[0] / (float) winH[0]));
            }
        }
        float wx = bx * (WitcherGame.FRAME_W / (float) fbW);
        float wy = by * (WitcherGame.FRAME_H / (float) fbH);
        return new float[] { wx, wy };
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            Gdx.app.exit();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT) {
            return false;
        }
        float[] w = screenToFrame(screenX, screenY);
        hoverMinimize = hit(minimizeBounds(), w[0], w[1]);
        hoverClose = hit(closeBounds(), w[0], w[1]);
        pressedMinimize = hoverMinimize;
        pressedClose = hoverClose;

        if (hoverMinimize || hoverClose) {
            return true;
        }
        if (hit(titleDragBounds(), w[0], w[1]) && Gdx.graphics instanceof Lwjgl3Graphics lwjgl) {
            dragging = true;
            dragHandle = lwjgl.getWindow().getWindowHandle();
            double[] cx = new double[1];
            double[] cy = new double[1];
            GLFW.glfwGetCursorPos(dragHandle, cx, cy);
            dragCursorStartX = cx[0];
            dragCursorStartY = cy[0];
            int[] wx = new int[1];
            int[] wy = new int[1];
            GLFW.glfwGetWindowPos(dragHandle, wx, wy);
            dragWinX = wx[0];
            dragWinY = wy[0];
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!dragging || dragHandle == 0) {
            return false;
        }
        double[] cx = new double[1];
        double[] cy = new double[1];
        GLFW.glfwGetCursorPos(dragHandle, cx, cy);
        int dx = (int) Math.round(cx[0] - dragCursorStartX);
        int dy = (int) Math.round(cy[0] - dragCursorStartY);
        GLFW.glfwSetWindowPos(dragHandle, dragWinX + dx, dragWinY + dy);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT) {
            return false;
        }
        boolean consumed = pressedMinimize || pressedClose || dragging;
        if (pressedClose && Gdx.graphics instanceof Lwjgl3Graphics lwjgl) {
            lwjgl.getWindow().closeWindow();
        } else if (pressedMinimize && Gdx.graphics instanceof Lwjgl3Graphics lwjgl) {
            lwjgl.getWindow().iconifyWindow();
        }
        pressedMinimize = false;
        pressedClose = false;
        dragging = false;
        return consumed;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        float[] w = screenToFrame(screenX, screenY);
        hoverMinimize = hit(minimizeBounds(), w[0], w[1]);
        hoverClose = hit(closeBounds(), w[0], w[1]);
        return hoverMinimize || hoverClose;
    }
}
