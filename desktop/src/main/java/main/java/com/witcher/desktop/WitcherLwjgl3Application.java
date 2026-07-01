package main.java.com.witcher.desktop;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.lwjgl.glfw.GLFW;

/**
 * GLFW без масштабирования окна монитором — 1 пиксель кадра = 1 пиксель на экране (как Swing).
 */
public class WitcherLwjgl3Application extends Lwjgl3Application {

    public WitcherLwjgl3Application(ApplicationListener listener, Lwjgl3ApplicationConfiguration config) {
        super(listener, config);
    }

    static void applyGlfwHints() {
        if (!GLFW.glfwInit()) {
            throw new IllegalStateException("glfwInit failed");
        }
        GLFW.glfwWindowHint(GLFW.GLFW_SCALE_TO_MONITOR, GLFW.GLFW_FALSE);
    }
}
