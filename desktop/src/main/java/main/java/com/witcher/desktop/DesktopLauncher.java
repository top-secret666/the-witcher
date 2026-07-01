package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;
import org.lwjgl.glfw.GLFW;

/**
 * Окно как Swing {@code Renderer(480, 360, 2)} — 960×720 на экране.
 * При 125% Windows GLFW-окно 768×576 (ОС растягивает до ~960×720 без полей).
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.opengl.Display.allowLegacyDXGIScaling", "false");
        try {
            float monitorScale = queryPrimaryMonitorContentScale();
            int[] glfw = glfwWindowSize(monitorScale);

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(glfw[0], glfw[1]);
            config.setResizable(false);
            config.setForegroundFPS(60);
            config.useVsync(true);

            System.out.println("[DesktopLauncher] glfw=" + glfw[0] + 'x' + glfw[1]
                + " monitorScale=" + monitorScale
                + " celNaEkrane=" + WitcherGame.WINDOW_W + 'x' + WitcherGame.WINDOW_H);

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

    static int[] glfwWindowSize(float monitorScale) {
        int targetW = WitcherGame.WINDOW_W;
        int targetH = WitcherGame.WINDOW_H;
        if (monitorScale <= 1.01f) {
            return new int[]{targetW, targetH};
        }
        return new int[]{
            Math.max(1, Math.round(targetW / monitorScale)),
            Math.max(1, Math.round(targetH / monitorScale))
        };
    }
}
