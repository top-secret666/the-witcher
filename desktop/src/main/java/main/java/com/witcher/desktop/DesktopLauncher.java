package main.java.com.witcher.desktop;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration.HdpiMode;
import main.java.com.witcher.gdx.WitcherGame;

/**
 * LibGDX desktop — полноэкранный режим, пиксельный viewport без чёрных полос.
 * Запуск: {@code run-gdx.bat}
 */
public class DesktopLauncher {

    public static void main(String[] args) {
        try {
            Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
            config.setTitle("The Witcher — LibGDX");
            Graphics.DisplayMode mode = Lwjgl3ApplicationConfiguration.getDisplayMode();
            config.setFullscreenMode(mode);
            config.setHdpiMode(HdpiMode.Pixels);
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
