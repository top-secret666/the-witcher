package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * LibGDX desktop — окно 960×720 (480×360 ×2), integer-scale, без мыла.
 * Запуск: {@code run-gdx.bat}
 */
public class DesktopLauncher {

    private static final int WINDOW_W = 960;
    private static final int WINDOW_H = 720;

    public static void main(String[] args) {
        try {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            config.setWindowedMode(WINDOW_W, WINDOW_H);
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
