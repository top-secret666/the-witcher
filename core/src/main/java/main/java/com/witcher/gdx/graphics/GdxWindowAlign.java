package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import org.lwjgl.glfw.GLFW;

/**
 * Подгоняет GLFW-окно под HiDPI: framebuffer ≈ 960×720 при масштабе Windows 125%.
 * Viewport всегда рисует в реальный framebuffer (GLFW), не в «желаемый» размер.
 */
public final class GdxWindowAlign {

    private static int cachedFbW;
    private static int cachedFbH;

    private GdxWindowAlign() {
    }

    /**
     * Один раз при старте: glfw-окно = target/scale (на 125% → 768×576 → fb 960×720).
     * Нельзя вызывать glfwSetWindowSize(960,720) при 125% — framebuffer станет 1200×900 и появятся поля.
     */
    public static void ensureFramebuffer(int targetW, int targetH) {
        if (!(Gdx.graphics instanceof Lwjgl3Graphics lwjgl)) {
            return;
        }
        long handle = lwjgl.getWindow().getWindowHandle();
        if (handle == 0) {
            return;
        }

        float[] sx = new float[1];
        float[] sy = new float[1];
        GLFW.glfwGetWindowContentScale(handle, sx, sy);
        float scale = Math.max(1f, Math.max(sx[0], sy[0]));

        int wantWinW = Math.max(1, Math.round(targetW / scale));
        int wantWinH = Math.max(1, Math.round(targetH / scale));

        int[] winW = new int[1];
        int[] winH = new int[1];
        GLFW.glfwGetWindowSize(handle, winW, winH);
        if (winW[0] != wantWinW || winH[0] != wantWinH) {
            GLFW.glfwSetWindowSize(handle, wantWinW, wantWinH);
        }

        refreshFramebufferCache(handle);
        Gdx.app.log("GdxWindowAlign",
            "target=" + targetW + 'x' + targetH
                + " scale=" + scale
                + " glfw=" + wantWinW + 'x' + wantWinH
                + " framebuffer=" + cachedFbW + 'x' + cachedFbH);
    }

    public static void refreshFramebufferCache() {
        if (!(Gdx.graphics instanceof Lwjgl3Graphics lwjgl)) {
            cachedFbW = Gdx.graphics.getBackBufferWidth();
            cachedFbH = Gdx.graphics.getBackBufferHeight();
            return;
        }
        refreshFramebufferCache(lwjgl.getWindow().getWindowHandle());
    }

    private static void refreshFramebufferCache(long handle) {
        if (handle == 0) {
            cachedFbW = Gdx.graphics.getBackBufferWidth();
            cachedFbH = Gdx.graphics.getBackBufferHeight();
            return;
        }
        int[] fbW = new int[1];
        int[] fbH = new int[1];
        GLFW.glfwGetFramebufferSize(handle, fbW, fbH);
        cachedFbW = Math.max(1, fbW[0]);
        cachedFbH = Math.max(1, fbH[0]);
    }

    public static int backBufferW() {
        if (cachedFbW <= 0) {
            refreshFramebufferCache();
        }
        return cachedFbW;
    }

    public static int backBufferH() {
        if (cachedFbH <= 0) {
            refreshFramebufferCache();
        }
        return cachedFbH;
    }
}
