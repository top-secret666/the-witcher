package main.java.com.witcher.gdx.graphics;

import com.badlogic.gdx.graphics.Pixmap;

import java.util.Random;

/** Порт Swing {@link main.java.com.witcher.ui.graphics.RetroPostProcessor} на Pixmap 480×360. */
public final class GdxRetroPostProcessor {

    private final Random rng = new Random();
    private int[] grainTile;
    private int grainTick;

    public void apply(Pixmap frame) {
        if (frame == null) {
            return;
        }
        int w = frame.getWidth();
        int h = frame.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }
        brighten(frame, w, h, 1.03f, 8);
        drawScanlines(frame, w, h, 0.03f, 3);
        drawVignette(frame, w, h, 0.12f);
        warmTint(frame, w, h, 0.02f);
        drawGrain(frame, w, h, 0.01f);
    }

    private static void warmTint(Pixmap frame, int w, int h, float amount) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = frame.getPixel(x, y);
                int a = argb & 0xff;
                if (a == 0) {
                    continue;
                }
                int r = (argb >>> 24) & 0xff;
                int g = (argb >>> 16) & 0xff;
                int b = (argb >>> 8) & 0xff;
                r = clamp((int) (r + 255 * amount));
                g = clamp((int) (g + 180 * amount));
                b = clamp((int) (b - 40 * amount));
                frame.drawPixel(x, y, rgba(r, g, b, a));
            }
        }
    }

    private static void drawScanlines(Pixmap frame, int w, int h, float strength, int step) {
        int darken = clamp((int) (255 * strength));
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x++) {
                int argb = frame.getPixel(x, y);
                int a = argb & 0xff;
                if (a == 0) {
                    continue;
                }
                int r = darkenChannel((argb >>> 24) & 0xff, darken);
                int g = darkenChannel((argb >>> 16) & 0xff, darken);
                int b = darkenChannel((argb >>> 8) & 0xff, darken);
                frame.drawPixel(x, y, rgba(r, g, b, a));
            }
        }
    }

    private static int darkenChannel(int c, int amount) {
        return Math.max(0, c - amount);
    }

    private static void drawVignette(Pixmap frame, int w, int h, float strength) {
        float cx = w * 0.5f;
        float cy = h * 0.42f;
        float maxR = (float) Math.hypot(cx, cy) * 1.05f;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = frame.getPixel(x, y);
                int a = argb & 0xff;
                if (a == 0) {
                    continue;
                }
                float t = (float) Math.hypot(x - cx, y - cy) / maxR;
                float v = Math.min(1f, t * t * strength);
                int r = dim((argb >>> 24) & 0xff, v);
                int g = dim((argb >>> 16) & 0xff, v);
                int b = dim((argb >>> 8) & 0xff, v);
                frame.drawPixel(x, y, rgba(r, g, b, a));
            }
        }
    }

    private static int dim(int c, float amount) {
        return clamp((int) (c * (1f - amount)));
    }

    private void drawGrain(Pixmap frame, int w, int h, float alpha) {
        if (alpha <= 0f) {
            return;
        }
        if (grainTile == null) {
            grainTile = new int[64 * 64];
        }
        if (grainTick++ % 6 == 0) {
            for (int i = 0; i < grainTile.length; i++) {
                int n = 110 + rng.nextInt(100);
                int a = 10 + rng.nextInt(18);
                grainTile[i] = (a << 24) | (n << 16) | (n << 8) | n;
            }
        }
        int strength = clamp((int) (alpha * 255));
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = frame.getPixel(x, y);
                int a = argb & 0xff;
                if (a == 0) {
                    continue;
                }
                int gn = grainTile[(y % 64) * 64 + (x % 64)] & 0xff;
                int delta = (gn - 128) * strength / 255;
                int r = clamp(((argb >>> 24) & 0xff) + delta);
                int g = clamp(((argb >>> 16) & 0xff) + delta);
                int b = clamp(((argb >>> 8) & 0xff) + delta);
                frame.drawPixel(x, y, rgba(r, g, b, a));
            }
        }
    }

    private static void brighten(Pixmap frame, int w, int h, float gain, int lift) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = frame.getPixel(x, y);
                int a = argb & 0xff;
                if (a == 0) {
                    continue;
                }
                int r = clamp((int) (((argb >>> 24) & 0xff) * gain + lift));
                int g = clamp((int) (((argb >>> 16) & 0xff) * gain + lift));
                int b = clamp((int) (((argb >>> 8) & 0xff) * gain + lift));
                frame.drawPixel(x, y, rgba(r, g, b, a));
            }
        }
    }

    private static int rgba(int r, int g, int b, int a) {
        return ((r & 0xff) << 24) | ((g & 0xff) << 16) | ((b & 0xff) << 8) | (a & 0xff);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
