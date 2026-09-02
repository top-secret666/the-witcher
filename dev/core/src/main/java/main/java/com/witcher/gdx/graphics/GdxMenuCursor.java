package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import org.lwjgl.glfw.GLFW;

/** Скрытие системного курсора через GLFW (без чёрного квадрата custom-cursor). */
public final class GdxMenuCursor {

    private static boolean menuHidden;

    private GdxMenuCursor() {
    }

    public static void hideForMenu() {
        long handle = windowHandle();
        if (handle != 0) {
            GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
            menuHidden = true;
        }
    }

    /** Повторно скрыть системный курсор (Windows иногда сбрасывает режим). */
    public static void ensureHidden() {
        if (!menuHidden) {
            return;
        }
        long handle = windowHandle();
        if (handle != 0) {
            GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_HIDDEN);
        }
    }

    public static void restoreAfterMenu() {
        if (!menuHidden) {
            return;
        }
        long handle = windowHandle();
        if (handle != 0) {
            GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
        }
        menuHidden = false;
    }

    public static void dispose() {
        restoreAfterMenu();
    }

    private static long windowHandle() {
        if (Gdx.graphics instanceof Lwjgl3Graphics g) {
            return g.getWindow().getWindowHandle();
        }
        return 0;
    }
}
