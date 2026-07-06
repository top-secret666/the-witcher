package main.java.com.witcher.ui.graphics;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Загружает спрайт-лист (PNG с кадрами) и воспроизводит анимацию.
 */
public class SpriteSheet {
    private final BufferedImage[] frames;
    private final int frameCount;
    private final int frameDelay;
    private int currentFrame = 0;
    private int tickCounter = 0;
    private boolean pingPong = false;
    private int direction = 1;
    private boolean crossfade = false;

    private SpriteSheet(BufferedImage[] frames, int frameDelay) {
        this.frames = frames;
        this.frameCount = frames.length;
        this.frameDelay = frameDelay;
    }

    public static SpriteSheet load(String resourcePath, int cols, int rows, int frameDelay) {
        return load(resourcePath, cols, rows, frameDelay, false);
    }

    /**
     * @param removeBlackBg если true, пиксели близкие к чёрному станут прозрачными
     */
    public static SpriteSheet load(String resourcePath, int cols, int rows, int frameDelay, boolean removeBlackBg) {
        return loadInternal(resourcePath, cols, rows, frameDelay, removeBlackBg, false, 0, true);
    }

    /**
     * Для плотных горизонтальных листов: отступ между кадрами, обрезка по альфе, общий холст.
     */
    public static SpriteSheet loadPacked(String resourcePath, int cols, int rows, int frameDelay,
                                         boolean removeBlackBg, int cellInset) {
        return loadInternal(resourcePath, cols, rows, frameDelay, removeBlackBg, true, cellInset, true);
    }

    public static SpriteSheet loadOptional(String resourcePath, int cols, int rows, int frameDelay, boolean removeBlackBg) {
        return loadInternal(resourcePath, cols, rows, frameDelay, removeBlackBg, false, 0, false);
    }

    private static SpriteSheet loadInternal(String resourcePath, int cols, int rows, int frameDelay,
                                            boolean removeBlackBg, boolean trimPacked, int cellInset,
                                            boolean logMissing) {
        Sprite sheet = logMissing ? Sprite.load(resourcePath) : Sprite.loadOptional(resourcePath);
        if (sheet == null) {
            return null;
        }

        BufferedImage src = sheet.getImage();
        int fw = src.getWidth() / cols;
        int fh = src.getHeight() / rows;
        int total = cols * rows;
        BufferedImage[] frames = new BufferedImage[total];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                BufferedImage frame = extractCell(src, cols, c, r, fw, fh, trimPacked ? cellInset : 0);
                if (removeBlackBg) {
                    frame = removeBlack(frame);
                }
                if (trimPacked) {
                    frame = trimToContent(frame, 1);
                }
                frames[r * cols + c] = frame;
            }
        }
        if (trimPacked) {
            frames = normalizeFrames(frames);
        }
        return new SpriteSheet(frames, frameDelay);
    }

    private static BufferedImage extractCell(BufferedImage src, int cols, int c, int r,
                                             int fw, int fh, int inset) {
        int x = c * fw;
        int y = r * fh;
        int left = inset > 0 && c > 0 ? inset : 0;
        int right = inset > 0 && c < cols - 1 ? inset : 0;
        int w = Math.max(1, fw - left - right);
        BufferedImage sub = src.getSubimage(x + left, y, w, fh);
        return copyImage(sub);
    }

    private static BufferedImage copyImage(BufferedImage src) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        return out;
    }

    private static BufferedImage trimToContent(BufferedImage src, int pad) {
        Rectangle box = computeContentBounds(src);
        if (box.width <= 0 || box.height <= 0) {
            return src;
        }
        int x = Math.max(0, box.x - pad);
        int y = Math.max(0, box.y - pad);
        int w = Math.min(src.getWidth() - x, box.width + pad * 2);
        int h = Math.min(src.getHeight() - y, box.height + pad * 2);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(src, 0, 0, w, h, x, y, x + w, y + h, null);
        g.dispose();
        return out;
    }

    private static BufferedImage[] normalizeFrames(BufferedImage[] raw) {
        int maxW = 0;
        int maxH = 0;
        for (BufferedImage frame : raw) {
            if (frame == null) {
                continue;
            }
            maxW = Math.max(maxW, frame.getWidth());
            maxH = Math.max(maxH, frame.getHeight());
        }
        if (maxW <= 0 || maxH <= 0) {
            return raw;
        }
        BufferedImage[] out = new BufferedImage[raw.length];
        for (int i = 0; i < raw.length; i++) {
            if (raw[i] == null) {
                continue;
            }
            out[i] = new BufferedImage(maxW, maxH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = out[i].createGraphics();
            int dx = (maxW - raw[i].getWidth()) / 2;
            int dy = maxH - raw[i].getHeight();
            g.drawImage(raw[i], dx, dy, null);
            g.dispose();
        }
        return out;
    }

    private static Rectangle computeContentBounds(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int minX = w;
        int minY = h;
        int maxX = 0;
        int maxY = 0;
        int step = Math.max(1, Math.min(w, h) / 256);
        for (int y = 0; y < h; y += step) {
            for (int x = 0; x < w; x += step) {
                int a = (img.getRGB(x, y) >>> 24) & 0xff;
                if (a > 12) {
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }
            }
        }
        if (maxX < minX || maxY < minY) {
            return new Rectangle(0, 0, w, h);
        }
        return new Rectangle(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    /** Удаляет пиксели близкие к чёрному, делая их прозрачными. */
    private static BufferedImage removeBlack(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = src.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                if (r < 30 && g < 30 && b < 30) {
                    out.setRGB(x, y, 0x00000000);
                } else {
                    out.setRGB(x, y, (0xFF << 24) | (r << 16) | (g << 8) | b);
                }
            }
        }
        return out;
    }

    /** Включить/выключить режим пинг-понг (вперёд-назад). */
    public SpriteSheet setPingPong(boolean pp) {
        this.pingPong = pp;
        return this;
    }

    /** Плавный переход между кадрами (без резкой смены). */
    public SpriteSheet setCrossfade(boolean enabled) {
        this.crossfade = enabled;
        return this;
    }

    /** Обновить анимацию (вызывать каждый тик). */
    public void update() {
        tickCounter++;
        if (tickCounter >= frameDelay) {
            tickCounter = 0;
            if (pingPong) {
                currentFrame += direction;
                if (currentFrame >= frameCount - 1) {
                    currentFrame = frameCount - 1;
                    direction = -1;
                } else if (currentFrame <= 0) {
                    currentFrame = 0;
                    direction = 1;
                }
            } else {
                currentFrame = (currentFrame + 1) % frameCount;
            }
        }
    }

    /** Отрисовать текущий кадр. */
    public void draw(Graphics2D g, int x, int y, int w, int h, float alpha) {
        if (frames == null || frameCount == 0) {
            return;
        }
        if (crossfade && frameDelay > 1 && frameCount > 1) {
            int fadeLen = Math.min(6, Math.max(2, frameDelay / 3));
            if (tickCounter >= frameDelay - fadeLen) {
                float blend = (tickCounter - (frameDelay - fadeLen)) / (float) fadeLen;
                blend = blend * blend * (3f - 2f * blend);
                int next = nextFrameIndex();
                drawFrame(g, currentFrame, x, y, w, h, alpha * (1f - blend));
                drawFrame(g, next, x, y, w, h, alpha * blend);
                return;
            }
        }
        drawFrame(g, currentFrame, x, y, w, h, alpha);
    }

    /** Отрисовать текущий кадр зеркально по горизонтали. */
    public void drawFlipped(Graphics2D g, int x, int y, int w, int h, float alpha) {
        if (frames == null || frameCount == 0) {
            return;
        }
        Composite prev = g.getComposite();
        applyDrawHints(g);
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, alpha))));
        }
        g.drawImage(frames[currentFrame], x + w, y, -w, h, null);
        g.setComposite(prev);
    }

    private int nextFrameIndex() {
        if (pingPong) {
            int next = currentFrame + direction;
            if (next >= frameCount) {
                return frameCount - 1;
            }
            if (next < 0) {
                return 0;
            }
            return next;
        }
        return (currentFrame + 1) % frameCount;
    }

    private void drawFrame(Graphics2D g, int frameIndex, int x, int y, int w, int h, float alpha) {
        if (frameIndex < 0 || frameIndex >= frameCount || frames[frameIndex] == null) {
            return;
        }
        Composite prev = g.getComposite();
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        applyDrawHints(g);
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, alpha))));
        }
        g.drawImage(frames[frameIndex], x, y, w, h, null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
        g.setComposite(prev);
    }

    private static void applyDrawHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    public int getFrameWidth() {
        return frames[0].getWidth();
    }

    public int getFrameHeight() {
        return frames[0].getHeight();
    }

    public int getFrameCount() {
        return frameCount;
    }

    /** Получить кадр по индексу (без анимации). */
    public BufferedImage getFrame(int idx) {
        if (frames == null || idx < 0 || idx >= frameCount) {
            return null;
        }
        return frames[idx];
    }
}
