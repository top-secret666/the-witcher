package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * LibGDX desktop. Окно 960×720 = 480×360 ×2 (целый масштаб, как GameWindow).
 * Запуск: {@code run-gdx.bat}
 */
public class DesktopLauncher {

    public static final int PIXEL_SCALE = 2;

    public static void main(String[] args) {
        try {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            int w = Math.round(WitcherGame.VIRTUAL_W * PIXEL_SCALE);
            int h = Math.round(WitcherGame.VIRTUAL_H * PIXEL_SCALE);
            config.setWindowedMode(w, h);
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
