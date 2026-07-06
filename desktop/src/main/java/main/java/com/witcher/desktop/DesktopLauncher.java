package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * При Windows 125%: GLFW-окно = FRAME/scale (784×616), ОС растягивает до ~980×770 без чёрных полей.
 */
public class DesktopLauncher {

    static {
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        System.setProperty("awt.useSystemAAFontSettings", "off");
        System.setProperty("org.lwjgl.opengl.Display.allowLegacyDXGIScaling", "false");
    }

    public static void main(String[] args) {
        try {
            WitcherLwjgl3Application.applyGlfwHints();

            float scale = desktopScale();
            int[] glfw = glfwWindowSize(scale);

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(glfw[0], glfw[1]);
            config.setDecorated(false);
            config.setResizable(false);
            config.setForegroundFPS(60);
            config.useVsync(true);
            config.setWindowIcon("sprites/app_icon.png");

            System.out.println("[DesktopLauncher] celNaEkrane=" + WitcherGame.FRAME_W + 'x' + WitcherGame.FRAME_H
                + " glfw=" + glfw[0] + 'x' + glfw[1]
                + " scale=" + scale);

            new WitcherLwjgl3Application(new WitcherGame(), config);
        } catch (Throwable error) {
            error.printStackTrace();
            System.err.println("LibGDX crash: " + error.getMessage());
            System.exit(1);
        }
    }

    static float desktopScale() {
        try {
            return Math.max(1f, java.awt.Toolkit.getDefaultToolkit().getScreenResolution() / 96f);
        } catch (Throwable ignored) {
            return 1f;
        }
    }

    static int[] glfwWindowSize(float scale) {
        if (scale <= 1.01f) {
            return new int[] { WitcherGame.FRAME_W, WitcherGame.FRAME_H };
        }
        return new int[] {
            Math.max(1, Math.round(WitcherGame.FRAME_W / scale)),
            Math.max(1, Math.round(WitcherGame.FRAME_H / scale))
        };
    }
}
