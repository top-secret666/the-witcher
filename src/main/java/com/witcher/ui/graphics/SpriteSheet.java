package main.java.com.witcher.ui.graphics;

import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Graphics2D;
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
        return loadInternal(resourcePath, cols, rows, frameDelay, removeBlackBg, false, true);
    }

    /** @param trimToContent обрезает пустые поля вокруг спрайта в каждом кадре */
    public static SpriteSheet load(String resourcePath, int cols, int rows, int frameDelay,
                                   boolean removeBlackBg, boolean trimToContent) {
        return loadInternal(resourcePath, cols, rows, frameDelay, removeBlackBg, trimToContent, true);
    }

    public static SpriteSheet loadOptional(String resourcePath, int cols, int rows, int frameDelay, boolean removeBlackBg) {
        return loadInternal(resourcePath, cols, rows, frameDelay, removeBlackBg, false, false);
    }

    private static SpriteSheet loadInternal(String resourcePath, int cols, int rows, int frameDelay,
                                            boolean removeBlackBg, boolean trimToContent, boolean logMissing) {
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
                BufferedImage frame = src.getSubimage(c * fw, r * fh, fw, fh);
                if (removeBlackBg) {
                    frame = removeBlack(frame);
                }
                if (trimToContent) {
                    frame = trimToContent(frame);
                }
                frames[r * cols + c] = frame;
            }
        }
        return new SpriteSheet(frames, frameDelay);
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

    /** Обрезает прозрачные/чёрные поля — для кадров внизу большого листа. */
    private static BufferedImage trimToContent(BufferedImage src) {
        int w = src.getWidth();
        int h = src.getHeight();
        int minX = w;
        int minY = h;
        int maxX = 0;
        int maxY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int argb = src.getRGB(x, y);
                int a = (argb >>> 24) & 0xff;
                int r = (argb >>> 16) & 0xff;
                int g = (argb >>> 8) & 0xff;
                int b = argb & 0xff;
                boolean visible = a > 20 && !(r < 30 && g < 30 && b < 30);
                if (!visible) {
                    continue;
                }
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        if (maxX < minX || maxY < minY) {
            return src;
        }
        return src.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
    }

    /** Включить/выключить режим пинг-понг (вперёд-назад). */
    public SpriteSheet setPingPong(boolean pp) {
        this.pingPong = pp;
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
        Composite prev = g.getComposite();
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, alpha))));
        }
        g.drawImage(frames[currentFrame], x, y, w, h, null);
        g.setComposite(prev);
    }

    /** Отрисовать текущий кадр зеркально по горизонтали. */
    public void drawFlipped(Graphics2D g, int x, int y, int w, int h, float alpha) {
        if (frames == null || frameCount == 0) {
            return;
        }
        Composite prev = g.getComposite();
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                Math.max(0f, Math.min(1f, alpha))));
        }
        g.drawImage(frames[currentFrame], x + w, y, -w, h, null);
        g.setComposite(prev);
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
