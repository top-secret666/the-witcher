package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * Окно = FRAME_W×FRAME_H в пикселях (HdpiMode.Pixels): 1:1 с макетом, без уменьшения под scale ОС.
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

            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(WitcherGame.FRAME_W, WitcherGame.FRAME_H);
            config.setDecorated(false);
            config.setResizable(false);
            config.setForegroundFPS(60);
            config.useVsync(true);
            config.setWindowIcon("sprites/app_icon.png");

            System.out.println("[DesktopLauncher] framebufferCel="
                + WitcherGame.FRAME_W + 'x' + WitcherGame.FRAME_H);

            new WitcherLwjgl3Application(new WitcherGame(), config);
        } catch (Throwable error) {
            error.printStackTrace();
            System.err.println("LibGDX crash: " + error.getMessage());
            System.exit(1);
        }
    }
}
