package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;
import org.lwjgl.glfw.GLFW;

/**
 * Окно LibGDX — целевой кадр 960×720 (480×360 ×2).
 * <p>
 * На Windows 125% нельзя вызывать {@code setWindowedMode(960, 720)} напрямую:
 * framebuffer и физический клиент расходятся → чёрные поля и «крошечная» картинка.
 * Решение: GLFW-окно меньше на contentScale (как раньше в Swing+OS).
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.opengl.Display.allowLegacyDXGIScaling", "false");
        try {
            float contentScale = queryPrimaryMonitorContentScale();
            int[] glfw = computeGlfwWindowSize(contentScale);

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(glfw[0], glfw[1]);
            config.setResizable(false);
            config.setForegroundFPS(60);
            config.useVsync(true);

            System.out.println("[DesktopLauncher] celNaEkrane=" + WitcherGame.WINDOW_W + 'x' + WitcherGame.WINDOW_H
                + " glfwOkno=" + glfw[0] + 'x' + glfw[1]
                + " monitorScale=" + contentScale);

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
     * Размер окна для GLFW: при 125% → 768×576, ОС растягивает до ~960×720 без полей.
     */
    static int[] computeGlfwWindowSize(float contentScale) {
        int targetW = WitcherGame.WINDOW_W;
        int targetH = WitcherGame.WINDOW_H;
        if (contentScale <= 1.01f) {
            return new int[] { targetW, targetH };
        }
        return new int[] {
            Math.max(1, Math.round(targetW / contentScale)),
            Math.max(1, Math.round(targetH / contentScale))
        };
    }
}
