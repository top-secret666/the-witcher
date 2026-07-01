package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.utils.viewport.Viewport;
import main.java.com.witcher.gdx.WitcherGame;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

/**
 * Подробный лог размеров окна / backbuffer / viewport — чтобы не гадать про чёрные поля.
 */
public final class DisplayMetrics {

    private static int logCount;
    private static boolean viewportLoggedOnce;

    private DisplayMetrics() {
    }

    public static void logOnceViewport(String tag, Viewport viewport) {
        if (viewportLoggedOnce || viewport == null) {
            return;
        }
        viewportLoggedOnce = true;
        logViewport(tag, viewport);
    }

    public static void log(String tag) {
        logCount++;
        StringBuilder sb = new StringBuilder(512);
        sb.append("\n=== Display [").append(tag).append("] #").append(logCount).append(" ===\n");

        sb.append("ozhidaetsya (WitcherGame): ")
            .append(WitcherGame.WINDOW_W).append('x').append(WitcherGame.WINDOW_H)
            .append(" virtual=").append((int) WitcherGame.VIRTUAL_W)
            .append('x').append((int) WitcherGame.VIRTUAL_H)
            .append(" scale=").append(WitcherGame.PIXEL_SCALE).append('\n');

        sb.append("Gdx.graphics.getWidth x getHeight: ")
            .append(Gdx.graphics.getWidth()).append('x').append(Gdx.graphics.getHeight()).append('\n');
        sb.append("Gdx.graphics.getBackBufferWidth x Height: ")
            .append(Gdx.graphics.getBackBufferWidth()).append('x')
            .append(Gdx.graphics.getBackBufferHeight()).append('\n');
        sb.append("Gdx.graphics.getDensity: ").append(Gdx.graphics.getDensity()).append('\n');
        sb.append("Gdx.graphics.getPpiX x PpiY: ")
            .append(Gdx.graphics.getPpiX()).append('x').append(Gdx.graphics.getPpiY()).append('\n');
        sb.append("Gdx.graphics.getDisplayMode: ")
            .append(Gdx.graphics.getDisplayMode().width).append('x')
            .append(Gdx.graphics.getDisplayMode().height)
            .append('@').append(Gdx.graphics.getDisplayMode().refreshRate).append("Hz\n");

        if (Gdx.graphics instanceof Lwjgl3Graphics lwjgl) {
            appendLwjgl(sb, lwjgl);
        } else {
            sb.append("LWJGL3: net (graphics class ")
                .append(Gdx.graphics.getClass().getName()).append(")\n");
        }

        appendGlViewport(sb);
        appendAwtDpi(sb);
        appendDiagnosis(sb);
        sb.append("===\n");

        Gdx.app.log("DisplayMetrics", sb.toString());
    }

    public static void logViewport(String tag, Viewport viewport) {
        if (viewport == null) {
            return;
        }
        Gdx.app.log("DisplayMetrics",
            tag + " viewport: screen=(" + viewport.getScreenX() + ',' + viewport.getScreenY() + ") "
                + viewport.getScreenWidth() + 'x' + viewport.getScreenHeight()
                + " world=" + viewport.getWorldWidth() + 'x' + viewport.getWorldHeight()
                + " | Gdx.graphics=" + Gdx.graphics.getWidth() + 'x' + Gdx.graphics.getHeight());
    }

    /** Логирует размеры; авто-ресайз отключён — на HiDPI см. Diag в логе. */
    public static void tryFixWindowSizeMismatch() {
        if (!(Gdx.graphics instanceof Lwjgl3Graphics lwjgl)) {
            return;
        }
        long handle = lwjgl.getWindow().getWindowHandle();
        int[] winW = new int[1];
        int[] winH = new int[1];
        int[] fbW = new int[1];
        int[] fbH = new int[1];
        float[] sx = new float[1];
        float[] sy = new float[1];
        GLFW.glfwGetWindowSize(handle, winW, winH);
        GLFW.glfwGetFramebufferSize(handle, fbW, fbH);
        GLFW.glfwGetWindowContentScale(handle, sx, sy);

        int physW = Math.round(winW[0] * sx[0]);
        int physH = Math.round(winH[0] * sy[0]);
        if (physW != fbW[0] || physH != fbH[0]) {
            Gdx.app.log("DisplayMetrics", "HiDPI mismatch: physical~" + physW + 'x' + physH
                + " framebuffer=" + fbW[0] + 'x' + fbH[0]
                + " (scale=" + sx[0] + 'x' + sy[0] + ')');
        }
    }

    private static void appendLwjgl(StringBuilder sb, Lwjgl3Graphics lwjgl) {
        try {
            Lwjgl3Window window = lwjgl.getWindow();
            long handle = window.getWindowHandle();

            sb.append("Lwjgl3Window position: ")
                .append(window.getPositionX()).append(',').append(window.getPositionY()).append('\n');

            int[] winW = new int[1];
            int[] winH = new int[1];
            int[] fbW = new int[1];
            int[] fbH = new int[1];
            GLFW.glfwGetWindowSize(handle, winW, winH);
            GLFW.glfwGetFramebufferSize(handle, fbW, fbH);
            sb.append("GLFW glfwGetWindowSize: ").append(winW[0]).append('x').append(winH[0]).append('\n');
            sb.append("GLFW glfwGetFramebufferSize: ").append(fbW[0]).append('x').append(fbH[0]).append('\n');

            float[] scaleX = new float[1];
            float[] scaleY = new float[1];
            GLFW.glfwGetWindowContentScale(handle, scaleX, scaleY);
            sb.append("GLFW contentScale: ").append(scaleX[0]).append('x').append(scaleY[0]).append('\n');
            int physW = Math.round(winW[0] * scaleX[0]);
            int physH = Math.round(winH[0] * scaleY[0]);
            sb.append("GLFW physicalClient (window*scale): ").append(physW).append('x').append(physH).append('\n');

            int[] posX = new int[1];
            int[] posY = new int[1];
            GLFW.glfwGetWindowPos(handle, posX, posY);
            sb.append("GLFW windowPos: ").append(posX[0]).append(',').append(posY[0]).append('\n');
        } catch (Exception e) {
            sb.append("LWJGL error: ").append(e.getMessage()).append('\n');
        }
    }

    private static void appendGlViewport(StringBuilder sb) {
        try {
            int[] vp = new int[4];
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, vp);
            sb.append("OpenGL GL_VIEWPORT: x=").append(vp[0])
                .append(" y=").append(vp[1])
                .append(" w=").append(vp[2])
                .append(" h=").append(vp[3]).append('\n');
        } catch (Exception e) {
            sb.append("GL_VIEWPORT: ").append(e.getMessage()).append('\n');
        }
    }

    private static void appendAwtDpi(StringBuilder sb) {
        try {
            java.awt.Toolkit tk = java.awt.Toolkit.getDefaultToolkit();
            sb.append("AWT screenResolution: ").append(tk.getScreenResolution()).append(" DPI\n");
            java.awt.Dimension screen = tk.getScreenSize();
            sb.append("AWT screenSize: ").append(screen.width).append('x').append(screen.height).append('\n');
        } catch (Exception e) {
            sb.append("AWT DPI: ").append(e.getMessage()).append('\n');
        }
    }

    private static void appendDiagnosis(StringBuilder sb) {
        int gw = Gdx.graphics.getWidth();
        int gh = Gdx.graphics.getHeight();
        int ew = WitcherGame.WINDOW_W;
        int eh = WitcherGame.WINDOW_H;

        if (gw == ew && gh == eh) {
            sb.append("Diag: backbuffer sovpadaet s celom (").append(ew).append('x').append(eh).append(").\n");
        } else {
            sb.append("Diag: backbuffer ").append(gw).append('x').append(gh)
                .append(" (mozhet otlichatsya pri HiDPI; sm. physicalClient).\n");
        }

        if (Gdx.graphics instanceof Lwjgl3Graphics lwjgl) {
            try {
                long handle = lwjgl.getWindow().getWindowHandle();
                int[] winW = new int[1];
                int[] winH = new int[1];
                int[] fbW = new int[1];
                int[] fbH = new int[1];
                float[] sx = new float[1];
                float[] sy = new float[1];
                GLFW.glfwGetWindowSize(handle, winW, winH);
                GLFW.glfwGetFramebufferSize(handle, fbW, fbH);
                GLFW.glfwGetWindowContentScale(handle, sx, sy);

                int physW = Math.round(winW[0] * sx[0]);
                int physH = Math.round(winH[0] * sy[0]);

                if (Math.abs(physW - ew) <= 2 && Math.abs(physH - eh) <= 2) {
                    sb.append("Diag: fizicheskij klient ~").append(physW).append('x').append(physH)
                        .append(" — OK (cel ").append(ew).append('x').append(eh).append(").\n");
                } else if (physW != fbW[0] || physH != fbH[0]) {
                    sb.append("Diag: fizicheskij klient ~").append(physW).append('x').append(physH)
                        .append(", framebuffer ").append(fbW[0]).append('x').append(fbH[0])
                        .append(" — OS mozhet risovat chernye polya.\n");
                    sb.append("      Reshenie: DesktopLauncher.computeWindowSize(contentScale).\n");
                }
                if (winW[0] > fbW[0] + 4 || winH[0] > fbH[0] + 4) {
                    sb.append("Diag: okno krupnee framebuffer — masshtab Windows / HiDPI.\n");
                }
            } catch (Exception ignored) {
            }
        }
    }
}
