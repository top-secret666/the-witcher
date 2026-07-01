package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.Viewport;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * Пиксельная шапка и рамка окна — как Swing {@code GameWindow.PixelTitleBar} / {@code PixelBorder}.
 */
public final class PixelFrameChrome extends InputAdapter {

    private static final Color TITLE_BG = new Color(30f / 255f, 22f / 255f, 12f / 255f, 1f);
    private static final Color TITLE_FG = new Color(218f / 255f, 165f / 255f, 32f / 255f, 1f);
    private static final Color TITLE_BORDER = new Color(140f / 255f, 100f / 255f, 35f / 255f, 1f);
    private static final Color TITLE_HOVER = new Color(230f / 255f, 180f / 255f, 60f / 255f, 1f);

    private final GlyphLayout glyph = new GlyphLayout();
    private final Vector3 tmp = new Vector3();
    private final Matrix4 gameMatrix = new Matrix4();

    private BitmapFont titleFont;
    private boolean hoverMinimize;
    private boolean hoverClose;
    private boolean pressedMinimize;
    private boolean pressedClose;
    private boolean dragging;
    private int dragStartX;
    private int dragStartY;
    private int dragWinX;
    private int dragWinY;

    public void loadFont() {
        if (titleFont == null) {
            titleFont = new BitmapFont();
            titleFont.getData().setScale(0.55f);
        }
    }

    public void dispose() {
        if (titleFont != null) {
            titleFont.dispose();
            titleFont = null;
        }
    }

    public Matrix4 gameContentMatrix(Matrix4 frameMatrix) {
        gameMatrix.set(frameMatrix);
        gameMatrix.translate(WitcherGame.GAME_VX, WitcherGame.GAME_VY, 0f);
        return gameMatrix;
    }

    public void drawBackground(ShapeRenderer shapes) {
        float fw = WitcherGame.FRAME_VW;
        float fh = WitcherGame.FRAME_VH;
        float b = WitcherGame.BORDER_V;

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

    public void drawForeground(ShapeRenderer shapes, SpriteBatch batch, Viewport viewport) {
        float fh = WitcherGame.FRAME_VH;
        float b = WitcherGame.BORDER_V;
        float th = WitcherGame.TITLE_V;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(TITLE_BG);
        shapes.rect(b, fh - b - th, WitcherGame.VIRTUAL_W, th);
        shapes.setColor(TITLE_BORDER);
        shapes.rect(b, fh - b - th, WitcherGame.VIRTUAL_W, 1f);
        shapes.end();

        drawButton(shapes, minimizeBounds(viewport), hoverMinimize, pressedMinimize, false);
        drawButton(shapes, closeBounds(viewport), hoverClose, pressedClose, true);

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
            float barH = 2.5f;
            float barW = r[2] - 6f;
            shapes.rect(r[0] + (r[2] - barW) * 0.5f, r[1] + (r[3] - barH) * 0.5f, barW, barH);
        }
        shapes.end();
    }

    private float[] minimizeBounds(Viewport viewport) {
        float[] close = closeBounds(viewport);
        float btn = close[2];
        return new float[] { close[0] - 4f - btn, close[1], btn, btn };
    }

    private float[] closeBounds(Viewport viewport) {
        float fh = WitcherGame.FRAME_VH;
        float b = WitcherGame.BORDER_V;
        float th = WitcherGame.TITLE_V;
        float btn = th - 4f;
        float x = WitcherGame.FRAME_VW - b - 6f - btn;
        float y = fh - b - th + (th - btn) * 0.5f;
        return new float[] { x, y, btn, btn };
    }

    private float[] titleDragBounds() {
        float b = WitcherGame.BORDER_V;
        float th = WitcherGame.TITLE_V;
        float fh = WitcherGame.FRAME_VH;
        return new float[] { b, fh - b - th, WitcherGame.VIRTUAL_W, th };
    }

    private boolean hit(float[] r, float wx, float wy) {
        return wx >= r[0] && wx <= r[0] + r[2] && wy >= r[1] && wy <= r[1] + r[3];
    }

    private float[] screenToWorld(Viewport viewport, int sx, int sy) {
        tmp.set(sx, sy, 0f);
        viewport.unproject(tmp);
        return new float[] { tmp.x, tmp.y };
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (button != Input.Buttons.LEFT || viewport == null) {
            return false;
        }
        float[] w = screenToWorld(viewport, screenX, screenY);
        hoverMinimize = hit(minimizeBounds(viewport), w[0], w[1]);
        hoverClose = hit(closeBounds(viewport), w[0], w[1]);
        pressedMinimize = hoverMinimize;
        pressedClose = hoverClose;

        if (hoverMinimize || hoverClose) {
            return true;
        }
        if (hit(titleDragBounds(), w[0], w[1]) && Gdx.graphics instanceof Lwjgl3Graphics lwjgl) {
            dragging = true;
            dragStartX = screenX;
            dragStartY = screenY;
            dragWinX = lwjgl.getWindow().getPositionX();
            dragWinY = lwjgl.getWindow().getPositionY();
            return true;
        }
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (!dragging || !(Gdx.graphics instanceof Lwjgl3Graphics lwjgl)) {
            return false;
        }
        int dx = screenX - dragStartX;
        int dy = screenY - dragStartY;
        lwjgl.getWindow().setPosition(dragWinX + dx, dragWinY - dy);
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
        if (viewport == null) {
            return false;
        }
        float[] w = screenToWorld(viewport, screenX, screenY);
        hoverMinimize = hit(minimizeBounds(viewport), w[0], w[1]);
        hoverClose = hit(closeBounds(viewport), w[0], w[1]);
        return hoverMinimize || hoverClose;
    }

    private Viewport viewport;

    public void setViewport(Viewport viewport) {
        this.viewport = viewport;
    }
}
