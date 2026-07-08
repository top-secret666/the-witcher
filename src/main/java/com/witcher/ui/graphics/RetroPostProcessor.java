package main.java.com.witcher.ui.graphics;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Random;

/**
 * Пост-обработка кадра 480×360 «как фильтр сверху» — без LibGDX.
 * Вызывается из {@link Renderer#present()} после отрисовки сцены.
 */
public final class RetroPostProcessor {

    public enum Preset {
        OFF,
        /** Мягко: виньетка + лёгкое зерно + чуть теплее. */
        SNES,
        /** PS1-вайб: меньше цветов, дизеринг, хроматика, виньетка. */
        PS1,
        /** CRT: резкость → сканлайны → виньетка → зерно. */
        CRT,
        /** Только пиксельная резкость, без сканлайнов (для сравнения). */
        CRISP
    }

    private Preset preset = Preset.OFF;
    private final Random rng = new Random();
    private BufferedImage grainTile;
    private int grainTick;

    public Preset preset() {
        return preset;
    }

    public void setPreset(Preset preset) {
        this.preset = preset != null ? preset : Preset.OFF;
    }

    public void cyclePreset() {
        Preset[] all = Preset.values();
        int next = (preset.ordinal() + 1) % all.length;
        preset = all[next];
        System.out.println("Retro filter: " + preset);
    }

    public boolean isEnabled() {
        return preset != Preset.OFF;
    }

    public void apply(BufferedImage frame) {
        if (frame == null || preset == Preset.OFF) {
            return;
        }
        int w = frame.getWidth();
        int h = frame.getHeight();
        if (w <= 0 || h <= 0) {
            return;
        }

        switch (preset) {
            case SNES -> applySnes(frame, w, h);
            case PS1 -> applyPs1(frame, w, h);
            case CRT -> applyCrt(frame, w, h);
            case CRISP -> applyCrisp(frame, w, h);
            default -> { }
        }
    }

    private void applySnes(BufferedImage frame, int w, int h) {
        warmTint(frame, w, h, 0.06f);
        quantizeRgb(frame, w, h, 6);
        drawVignette(frame, w, h, 0.22f);
        drawGrain(frame, w, h, 0.035f);
    }

    private void applyPs1(BufferedImage frame, int w, int h) {
        quantizeRgb(frame, w, h, 5);
        applyBayerDither(frame, w, h, 5);
        chromaticAberration(frame, w, h, 1);
        drawVignette(frame, w, h, 0.28f);
        drawGrain(frame, w, h, 0.05f);
    }

    private void applyCrt(BufferedImage frame, int w, int h) {
        pixelSharpen(frame, w, h, 0.42f);
        drawScanlines(frame, w, h, 0.18f, 2);
        drawVignette(frame, w, h, 0.35f);
        warmTint(frame, w, h, 0.04f);
        drawGrain(frame, w, h, 0.06f);
    }

    private void applyCrisp(BufferedImage frame, int w, int h) {
        pixelSharpen(frame, w, h, 0.55f);
        localContrast(frame, w, h, 1.06f);
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

    private static void quantizeRgb(BufferedImage frame, int w, int h, int bits) {
        int levels = Math.max(2, 1 << bits);
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        for (int i = 0; i < w * h; i++) {
            int argb = buf[i];
            int a = (argb >>> 24) & 0xFF;
            if (a == 0) {
                continue;
            }
            int r = quantizeChannel((argb >>> 16) & 0xFF, levels);
            int g = quantizeChannel((argb >>> 8) & 0xFF, levels);
            int b = quantizeChannel(argb & 0xFF, levels);
            buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    private static int quantizeChannel(int c, int levels) {
        float t = c / 255f;
        int q = Math.round(t * (levels - 1));
        return clamp(q * 255 / (levels - 1));
    }

    private static final int[] BAYER_4X4 = {
        0, 8, 2, 10,
        12, 4, 14, 6,
        3, 11, 1, 9,
        15, 7, 13, 5
    };

    private static void applyBayerDither(BufferedImage frame, int w, int h, int bits) {
        int levels = Math.max(2, 1 << bits);
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                int i = row + x;
                int argb = buf[i];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int threshold = BAYER_4X4[(y & 3) * 4 + (x & 3)] * 255 / 16;
                int r = ditherChannel((argb >>> 16) & 0xFF, levels, threshold);
                int g = ditherChannel((argb >>> 8) & 0xFF, levels, threshold);
                int b = ditherChannel(argb & 0xFF, levels, threshold);
                buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    private static int ditherChannel(int c, int levels, int threshold) {
        float t = (c + threshold - 128) / 255f;
        t = Math.max(0f, Math.min(1f, t));
        int q = Math.round(t * (levels - 1));
        return clamp(q * 255 / (levels - 1));
    }

    private static void chromaticAberration(BufferedImage frame, int w, int h, int shift) {
        if (shift <= 0) {
            return;
        }
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        int[] copy = buf.clone();
        for (int y = 0; y < h; y++) {
            int row = y * w;
            for (int x = 0; x < w; x++) {
                int i = row + x;
                int argb = copy[i];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int r = sampleChannel(copy, w, h, x - shift, y, 16);
                int g = (argb >>> 8) & 0xFF;
                int b = sampleChannel(copy, w, h, x + shift, y, 0);
                buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    private static int sampleChannel(int[] buf, int w, int h, int x, int y, int shift) {
        if (x < 0 || y < 0 || x >= w || y >= h) {
            return 0;
        }
        return (buf[y * w + x] >>> shift) & 0xFF;
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
            int gy = y % gh;
            int grainRow = gy * gw;
            for (int x = 0; x < w; x++) {
                int i = row + x;
                int argb = buf[i];
                int a = (argb >>> 24) & 0xFF;
                if (a == 0) {
                    continue;
                }
                int gn = grain[grainRow + (x % gw)] & 0xFF;
                int delta = gn - 128;
                int mix = delta * strength / 255;
                int r = clamp(((argb >>> 16) & 0xFF) + mix);
                int g = clamp(((argb >>> 8) & 0xFF) + mix);
                int b = clamp((argb & 0xFF) + mix);
                buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(255, v));
    }

    /**
     * Unsharp 1px — подчёркивает границы пикселей без размытия всего кадра.
     */
    private static void pixelSharpen(BufferedImage frame, int w, int h, float amount) {
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
                int r = sharpenChannel(src, i, w, 16, amount);
                int g = sharpenChannel(src, i, w, 8, amount);
                int b = sharpenChannel(src, i, w, 0, amount);
                buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
            }
        }
    }

    private static int sharpenChannel(int[] src, int i, int w, int shift, float amount) {
        int c = (src[i] >>> shift) & 0xFF;
        int blur = ((src[i - 1] >>> shift) & 0xFF)
            + ((src[i + 1] >>> shift) & 0xFF)
            + ((src[i - w] >>> shift) & 0xFF)
            + ((src[i + w] >>> shift) & 0xFF);
        blur /= 4;
        return clamp((int) (c + (c - blur) * amount));
    }

    /** Лёгкий контраст по центру — пиксели «собраннее», без мыла. */
    private static void localContrast(BufferedImage frame, int w, int h, float gain) {
        int[] buf = ((DataBufferInt) frame.getRaster().getDataBuffer()).getData();
        float mid = 128f;
        for (int i = 0; i < w * h; i++) {
            int argb = buf[i];
            int a = (argb >>> 24) & 0xFF;
            if (a == 0) {
                continue;
            }
            int r = contrastChannel((argb >>> 16) & 0xFF, mid, gain);
            int g = contrastChannel((argb >>> 8) & 0xFF, mid, gain);
            int b = contrastChannel(argb & 0xFF, mid, gain);
            buf[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }
    }

    private static int contrastChannel(int c, float mid, float gain) {
        return clamp((int) (mid + (c - mid) * gain));
    }
}
