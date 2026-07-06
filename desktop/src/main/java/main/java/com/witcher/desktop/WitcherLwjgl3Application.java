package main.java.com.witcher.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.lwjgl.glfw.GLFW;

/**
 * GLFW: не масштабировать окно монитором. Hint ставим до {@code glfwInit} (вызовет LibGDX).
 */
public class WitcherLwjgl3Application extends Lwjgl3Application {

    public WitcherLwjgl3Application(ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
        super(listener, config);
    }

    static void applyGlfwHints() {
        GLFW.glfwInitHint(GLFW.GLFW_SCALE_TO_MONITOR, GLFW.GLFW_FALSE);
    }
}
