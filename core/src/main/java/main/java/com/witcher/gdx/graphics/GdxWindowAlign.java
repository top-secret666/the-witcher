package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import org.lwjgl.glfw.GLFW;

/**
 * Подгоняет GLFW-окно так, чтобы framebuffer = целевому кадру (960×720).
 * Без этого на Windows 125% клиент окна больше framebuffer → чёрные поля вокруг GL.
 */
public final class GdxWindowAlign {

    private static boolean done;

    private GdxWindowAlign() {
    }

    public static void ensureFramebuffer(int targetW, int targetH) {
        if (done || !(Gdx.graphics instanceof Lwjgl3Graphics lwjgl)) {
            return;
        }
        long handle = lwjgl.getWindow().getWindowHandle();
        if (handle == 0) {
            return;
        }

        int[] fbW = new int[1];
        int[] fbH = new int[1];
        GLFW.glfwGetFramebufferSize(handle, fbW, fbH);

        if (fbW[0] == targetW && fbH[0] == targetH) {
            Gdx.app.log("GdxWindowAlign", "OK framebuffer " + targetW + 'x' + targetH);
            done = true;
            return;
        }

        float[] sx = new float[1];
        float[] sy = new float[1];
        GLFW.glfwGetWindowContentScale(handle, sx, sy);
        float scale = Math.max(1f, Math.max(sx[0], sy[0]));

        int winW = Math.max(1, Math.round(targetW / scale));
        int winH = Math.max(1, Math.round(targetH / scale));
        GLFW.glfwSetWindowSize(handle, winW, winH);

        GLFW.glfwGetFramebufferSize(handle, fbW, fbH);
        Gdx.app.log("GdxWindowAlign",
            "target=" + targetW + 'x' + targetH
                + " scale=" + scale
                + " glfw=" + winW + 'x' + winH
                + " framebuffer=" + fbW[0] + 'x' + fbH[0]);

        if (fbW[0] != targetW || fbH[0] != targetH) {
            GLFW.glfwSetWindowSize(handle, targetW, targetH);
            GLFW.glfwGetFramebufferSize(handle, fbW, fbH);
            Gdx.app.log("GdxWindowAlign", "retry direct " + targetW + 'x' + targetH
                + " -> framebuffer=" + fbW[0] + 'x' + fbH[0]);
        }

        done = true;
    }

    public static int backBufferW() {
        return Gdx.graphics.getBackBufferWidth();
    }

    public static int backBufferH() {
        return Gdx.graphics.getBackBufferHeight();
    }
}
