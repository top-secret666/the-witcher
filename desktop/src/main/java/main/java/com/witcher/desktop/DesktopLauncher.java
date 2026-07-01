package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * LibGDX desktop — окно 960×720 (480×360 ×2), integer-scale, без мыла.
 * Запуск: {@code run-gdx.bat}
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        System.setProperty("org.lwjgl.opengl.Display.allowLegacyDXGIScaling", "false");
        try {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setHdpiMode(HdpiMode.Pixels);
            config.setWindowedMode(WitcherGame.WINDOW_W, WitcherGame.WINDOW_H);
            config.setResizable(false);
            config.setForegroundFPS(60);
            config.useVsync(true);
            new Lwjgl3Application(new WitcherGame(), config);
        } catch (Throwable error) {
            error.printStackTrace();
            System.err.println("LibGDX crash: " + error.getMessage());
            System.exit(1);
        }
    }
}
