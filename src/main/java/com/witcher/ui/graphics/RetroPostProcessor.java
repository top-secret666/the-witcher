package main.java.com.witcher.ui.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/**
 * Фиксированная пост-обработка всей игры: CRT + мягкая чёткость пикселей.
 * Вызывается из {@link Renderer#present()} после каждого кадра 480×360.
 */
public final class RetroPostProcessor {

    private final Random rng = new Random();
    private BufferedImage grainTile;
    private int grainTick;

    public void apply(BufferedImage frame) {
        if (frame == null) {
            return;
        }
        int w = frame.getWidth();
        int h = frame.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        // Мягкий CRT — без «дробления» текста сканлайнами.
        brighten(frame, w, h, 1.03f, 8);
        drawScanlines(frame, w, h, 0.03f, 3);
        drawVignette(frame, w, h, 0.12f);
        warmTint(frame, w, h, 0.02f);
        drawGrain(frame, w, h, 0.01f);
    }

    private static void warmTint(BufferedImage frame, int w, int h, float amount) {
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < w * h; i++) {
            int argb = buf[i];
            int a = (argb >>> 24) & 0xFF;
            if (a == 0) {
                continue;
            }
            int r = (argb >>> 16) & 0xFF;
            int g = (argb >>> 8) & 0xFF;
            int b = argb & 0xFF;
            r = clamp((int) (r + 255 * amount));
            g = clamp((int) (g + 180 * amount));
            b = clamp((int) (b - 40 * amount));
            buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    private static void drawScanlines(BufferedImage frame, int w, int h, float strength, int step) {
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        int darken = clamp((int) (255 * strength));
        for (int y = 0; y < h; y += step) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                int i = row + x;
                int argb = buf[i];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int r = darkenChannel((argb >>> 16) & 0xFF, darken);
                int g = darkenChannel((argb >>> 8) & 0xFF, darken);
                int b = darkenChannel(argb & 0xFF, darken);
                buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    private static int darkenChannel(int c, int amount) {
        return Math.max(0, c - amount);
    }

    private static void drawVignette(BufferedImage frame, int w, int h, float strength) {
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        float cx = w * 0.5f;
        float cy = h * 0.42f;
        float maxR = (float) Math.hypot(cx, cy) * 1.05f;
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                int i = row + x;
                int argb = buf[i];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                float dx = x - cx;
                float dy = y - cy;
                float t = (float) Math.hypot(dx, dy) / maxR;
                float v = Math.min(1f, t * t * strength);
                int r = dim((argb >>> 16) & 0xFF, v);
                int g = dim((argb >>> 8) & 0xFF, v);
                int b = dim(argb & 0xFF, v);
                buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    private static int dim(int c, float amount) {
        return clamp((int) (c * (1f - amount)));
    }

    private void drawGrain(BufferedImage frame, int w, int h, float alpha) {
        if (alpha <= 0f) {
            return;
        }
        if (grainTile == null) {
            grainTile = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        }
        if (grainTick++ % 6 == 0) {
            for (int y = 0; y < grainTile.getHeight(); y++) {
                for (int x = 0; x < grainTile.getWidth(); x++) {
                    int n = 110 + rng.nextInt(100);
                    int a = 10 + rng.nextInt(18);
                    grainTile.setRGB(x, y, (a << 24) | (n << 16) | (n << 8) | n);
                }
            }
        }
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        int gw = grainTile.getWidth();
        int gh = grainTile.getHeight();
        int[] grain = ((DataBufferInt) grainTile.getRaster().getDataBuffer()).getData();
        int strength = clamp((int) (alpha * 255));
        for (int y = 0; y < h; y++) {
            int row = y * w;
            int grainRow = (y % gh) * gw;
            for (int x = 0; x < w; x++) {
                int i = row + x;
                int argb = buf[i];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int gn = grain[grainRow + (x % gw)] & 0xFF;
                int delta = (gn - 128) * strength / 255;
                int r = clamp(((argb >>> 16) & 0xFF) + delta);
                int g = clamp(((argb >>> 8) & 0xFF) + delta);
                int b = clamp((argb & 0xFF) + delta);
                buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    /** Лёгкий подъём яркости — кадр не «давит» по краям и в тенях. */
    private static void brighten(BufferedImage frame, int w, int h, float gain, int lift) {
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < w * h; i++) {
            int argb = buf[i];
            int a = (argb >>> 24) & 0xFF;
            if (a == 0) {
                continue;
            }
            int r = clamp((int) (((argb >>> 16) & 0xFF) * gain + lift));
            int g = clamp((int) (((argb >>> 8) & 0xFF) * gain + lift));
            int b = clamp((int) ((argb & 0xFF) * gain + lift));
            buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    /**
     * Чёткость пиксель-арта: слегка подчёркивает границы, но с лимитом — без ореолов.
     */
    private static void pixelClarity(BufferedImage frame, int w, int h, float amount) {
        if (amount <= 0f) {
            return;
        }
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        int[] src = buf.clone();
        for (int y = 1; y < h - 1; y++) {
            int row = y * w;
            for (int x = 1; x < w - 1; x++) {
                int i = row + x;
                int argb = src[i];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int r = clarityChannel(src, i, w, 16, amount);
                int g = clarityChannel(src, i, w, 8, amount);
                int b = clarityChannel(src, i, w, 0, amount);
                buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    private static int clarityChannel(int[] src, int i, int w, int shift, float amount) {
        int c = (src[i] >>> shift) & 0xFF;
        int blur = ((src[i - 1] >>> shift) & 0xFF)
            + ((src[i + 1] >>> shift) & 0xFF)
            + ((src[i - w] >>> shift) & 0xFF)
            + ((src[i + w] >>> shift) & 0xFF);
        blur /= 4;
        int edge = c - blur;
        if (Math.abs(edge) < 4) {
            return c;
        }
        int delta = (int) (edge * amount);
        delta = Math.max(-6, Math.min(6, delta));
        return clamp(c + delta);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
