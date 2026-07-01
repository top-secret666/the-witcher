package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;
import org.lwjgl.glfw.GLFW;

/**
 * LibGDX desktop — окно 960×720 (480×360 ×2), integer-scale, без мыла.
 * Запуск: {@code run-gdx.bat}
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.opengl.Display.allowLegacyDXGIScaling", "false");
        try {
            float contentScale = queryPrimaryMonitorContentScale();

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(WitcherGame.WINDOW_W, WitcherGame.WINDOW_H);
            config.setResizable(false);
            config.setForegroundFPS(60);
            config.useVsync(true);

            System.out.println("[DesktopLauncher] okno=" + WitcherGame.WINDOW_W + 'x' + WitcherGame.WINDOW_H
                + " monitorScale=" + contentScale);

            new Lwjgl3Application(new WitcherGame(), config);
        } catch (Throwable error) {
            error.printStackTrace();
            System.err.println("LibGDX crash: " + error.getMessage());
            System.exit(1);
        }
    }

    /** Масштаб дисплея Windows (1.0 = 100%, 1.25 = 125%) — только для лога. */
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
}
