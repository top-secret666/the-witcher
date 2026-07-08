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
        brighten(frame, w, h, 1.02f, 4);
        drawScanlines(frame, w, h, 0.02f, 3);
        drawVignette(frame, w, h, 0.08f);
        warmTint(frame, w, h, 0.012f);
        drawGrain(frame, w, h, 0.006f);
    }

    private static void warmTint(Pixmap frame, int w, int h, float amount) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int[] c = read(frame, x, y);
                if (c == null) {
                    continue;
                }
                c[0] = clamp((int) (c[0] + 255 * amount));
                c[1] = clamp((int) (c[1] + 180 * amount));
                c[2] = clamp((int) (c[2] - 40 * amount));
                write(frame, x, y, c);
            }
        }
    }

    private static void drawScanlines(Pixmap frame, int w, int h, float strength, int step) {
        int darken = clamp((int) (255 * strength));
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x++) {
                int[] c = read(frame, x, y);
                if (c == null) {
                    continue;
                }
                c[0] = darkenChannel(c[0], darken);
                c[1] = darkenChannel(c[1], darken);
                c[2] = darkenChannel(c[2], darken);
                write(frame, x, y, c);
            }
        }
    }

    private static int darkenChannel(int ch, int amount) {
        return Math.max(0, ch - amount);
    }

    private static void drawVignette(Pixmap frame, int w, int h, float strength) {
        float cx = w * 0.5f;
        float cy = h * 0.42f;
        float maxR = (float) Math.hypot(cx, cy) * 1.05f;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int[] c = read(frame, x, y);
                if (c == null) {
                    continue;
                }
                float t = (float) Math.hypot(x - cx, y - cy) / maxR;
                float v = Math.min(1f, t * t * strength);
                c[0] = dim(c[0], v);
                c[1] = dim(c[1], v);
                c[2] = dim(c[2], v);
                write(frame, x, y, c);
            }
        }
    }

    private static int dim(int ch, float amount) {
        return clamp((int) (ch * (1f - amount)));
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
                grainTile[i] = 110 + rng.nextInt(100);
            }
        }
        int strength = clamp((int) (alpha * 255));
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int[] c = read(frame, x, y);
                if (c == null) {
                    continue;
                }
                int gn = grainTile[(y % 64) * 64 + (x % 64)];
                int delta = (gn - 128) * strength / 255;
                c[0] = clamp(c[0] + delta);
                c[1] = clamp(c[1] + delta);
                c[2] = clamp(c[2] + delta);
                write(frame, x, y, c);
            }
        }
    }

    private static void brighten(Pixmap frame, int w, int h, float gain, int lift) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int[] c = read(frame, x, y);
                if (c == null) {
                    continue;
                }
                c[0] = clamp((int) (c[0] * gain + lift));
                c[1] = clamp((int) (c[1] * gain + lift));
                c[2] = clamp((int) (c[2] * gain + lift));
                write(frame, x, y, c);
            }
        }
    }

    /** @return {r,g,b,a} или null для пустого пикселя */
    private static int[] read(Pixmap frame, int x, int y) {
        int rgba = frame.getPixel(x, y);
        if (!visible(rgba)) {
            return null;
        }
        int a = rgba & 0xff;
        if (a < 4) {
            a = 255;
        }
        return new int[] {
            (rgba >>> 24) & 0xff,
            (rgba >>> 16) & 0xff,
            (rgba >>> 8) & 0xff,
            a
        };
    }

    private static void write(Pixmap frame, int x, int y, int[] c) {
        frame.drawPixel(x, y, rgba(c[0], c[1], c[2], c[3]));
    }

    private static boolean visible(int rgba) {
        if ((rgba & 0xff) > 4) {
            return true;
        }
        return (rgba >>> 8) != 0;
    }

    private static int rgba(int r, int g, int b, int a) {
        return ((r & 0xff) << 24) | ((g & 0xff) << 16) | ((b & 0xff) << 8) | (a & 0xff);
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }
}
