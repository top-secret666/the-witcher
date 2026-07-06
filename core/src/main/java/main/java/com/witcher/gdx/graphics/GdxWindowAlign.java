package main.java.com.witcher.gdx.graphics;



import com.badlogic.gdx.Gdx;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;

import org.lwjgl.glfw.GLFW;



/**

 * Кэш framebuffer. Рисуем во весь {@code glfwGetFramebufferSize} — не меняем размер окна после старта.

 */

public final class GdxWindowAlign {



    private static int cachedFbW;

    private static int cachedFbH;



    private GdxWindowAlign() {

    }



    public static void ensureFramebuffer(int targetW, int targetH) {

        refreshFramebufferCache();

        if (!(Gdx.graphics instanceof Lwjgl3Graphics lwjgl)) {

            return;

        }

        long handle = lwjgl.getWindow().getWindowHandle();

        if (handle == 0) {

            return;

        }



        float[] sx = new float[1];

        float[] sy = new float[1];

        GLFW.glfwGetWindowContentScale(handle, sx, sy);

        int[] winW = new int[1];

        int[] winH = new int[1];

        GLFW.glfwGetWindowSize(handle, winW, winH);



        Gdx.app.log("GdxWindowAlign",

            "cel=" + targetW + 'x' + targetH

                + " scale=" + Math.max(sx[0], sy[0])

                + " glfw=" + winW[0] + 'x' + winH[0]

                + " framebuffer=" + cachedFbW + 'x' + cachedFbH);

    }



    public static void refreshFramebufferCache() {

        if (!(Gdx.graphics instanceof Lwjgl3Graphics lwjgl)) {

            cachedFbW = Gdx.graphics.getBackBufferWidth();

            cachedFbH = Gdx.graphics.getBackBufferHeight();

            return;

        }

        refreshFramebufferCache(lwjgl.getWindow().getWindowHandle());

    }



    private static void refreshFramebufferCache(long handle) {

        if (handle == 0) {

            cachedFbW = Gdx.graphics.getBackBufferWidth();

            cachedFbH = Gdx.graphics.getBackBufferHeight();

            return;

        }

        int[] fbW = new int[1];

        int[] fbH = new int[1];

        GLFW.glfwGetFramebufferSize(handle, fbW, fbH);

        cachedFbW = Math.max(1, fbW[0]);

        cachedFbH = Math.max(1, fbH[0]);

    }



    public static int backBufferW() {

        if (cachedFbW <= 0) {

            refreshFramebufferCache();

        }

        return cachedFbW;

    }



    public static int backBufferH() {

        if (cachedFbH <= 0) {

            refreshFramebufferCache();

        }

        return cachedFbH;

    }

}

