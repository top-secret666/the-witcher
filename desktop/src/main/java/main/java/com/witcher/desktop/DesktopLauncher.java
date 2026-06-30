package main.java.com.witcher.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import main.java.com.witcher.gdx.WitcherGame;

/** Точка входа LibGDX (desktop). Запуск: {@code gradlew desktop:run} */
public class DesktopLauncher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("The Witcher");
        config.setWindowedMode(960, 720);
        config.setForegroundFPS(60);
        config.useVsync(true);
        new Lwjgl3Application(new WitcherGame(), config);
    }
}
