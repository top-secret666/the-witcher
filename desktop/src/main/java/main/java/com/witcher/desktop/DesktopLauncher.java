package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;
import org.lwjgl.glfw.GLFW;

/**
 * LibGDX: пиксельная рамка, кадр 960×720. Размер GLFW-окна = FRAME/scale (как Swing при 125%).
 */
public class DesktopLauncher {

    static {
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        System.setProperty("awt.useSystemAAFontSettings", "off");
        System.setProperty("org.lwjgl.opengl.Display.allowLegacyDXGIScaling", "false");
    }

    public static void main(String[] args) {
        try {
            float contentScale = queryPrimaryMonitorContentScale();
            int[] glfw = computeGlfwWindowSize(contentScale);

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(glfw[0], glfw[1]);
            config.setDecorated(false);
            config.setResizable(false);
            config.setForegroundFPS(60);
            config.useVsync(true);
            config.setWindowIcon("sprites/app_icon.png");

            System.out.println("[DesktopLauncher] cel=" + WitcherGame.FRAME_W + 'x' + WitcherGame.FRAME_H
                + " glfw=" + glfw[0] + 'x' + glfw[1]
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

    static int[] computeGlfwWindowSize(float contentScale) {
        int targetW = WitcherGame.FRAME_W;
        int targetH = WitcherGame.FRAME_H;
        if (contentScale <= 1.01f) {
            return new int[] { targetW, targetH };
        }
        return new int[] {
            Math.max(1, Math.round(targetW / contentScale)),
            Math.max(1, Math.round(targetH / contentScale))
        };
    }
}
