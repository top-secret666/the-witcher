package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;
import org.lwjgl.glfw.GLFW;

/**
 * LibGDX desktop — 960×720 (480×360 ×2).
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.opengl.Display.allowLegacyDXGIScaling", "false");
        try {
            float monitorScale = queryPrimaryMonitorContentScale();
            int[] size = renderSize(monitorScale);

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(size[0], size[1]);
            config.setResizable(false);
            config.setForegroundFPS(60);
            config.useVsync(true);

            System.out.println("[DesktopLauncher] framebuffer=" + size[0] + 'x' + size[1]
                + " monitorScale=" + monitorScale
                + " (cel " + WitcherGame.WINDOW_W + 'x' + WitcherGame.WINDOW_H + " na ekrane)");

            new Lwjgl3Application(new WitcherGame(), config);
        } catch (Throwable error) {
            error.printStackTrace();
            System.err.println("LibGDX crash: " + error.getMessage());
            System.exit(1);
        }
    }

    static float queryPrimaryMonitorContentScale() {
        try {
            if (!GLFW.glfwInit()) {
                return 1f;
            }
            long monitor = GLFW.glfwGetPrimaryMonitor();
            float[] sx = new float[1];
            float[] sy = new float[1];
            GLFW.glfwGetMonitorContentScale(monitor, sx, sy);
            GLFW.glfwTerminate();
            return Math.max(1f, Math.max(sx[0], sy[0]));
        } catch (Throwable ignored) {
            return 1f;
        }
    }

    /**
     * При 125% Windows рисуем 1200×900 — тот же физический размер, что занимает окно.
     * Игра всё равно растягивает 480×360 на весь backbuffer.
     */
    static int[] renderSize(float monitorScale) {
        if (monitorScale <= 1.01f) {
            return new int[]{WitcherGame.WINDOW_W, WitcherGame.WINDOW_H};
        }
        return new int[]{
            Math.round(WitcherGame.WINDOW_W * monitorScale),
            Math.round(WitcherGame.WINDOW_H * monitorScale)
        };
    }
}
