package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Пиксельный бегущий ведьмак — 4 кадра анимации, 20x24 пикселя.
 * Каждый кадр хранится как массив строк, где символы означают цвета:
 *   '.' = прозрачный
 *   'H' = волосы (белые)
 *   'S' = кожа
 *   'A' = броня (тёмная)
 *   'B' = сапоги
 *   'W' = меч (серебро)
 *   'G' = свечение меча
 *   'C' = плащ
 *   'M' = медальон (золото)
 */
public class PixelWitcher {
    private final BufferedImage[] frames;
    private int currentFrame = 0;
    private int frameTimer = 0;
    private int x, y;
    private final float speed;
    private final int scale;

    // 4 кадра бегущего ведьмака (20 широких x 24 высоких)
    private static final String[][] FRAME_DATA = {
        // Кадр 1: правая нога впереди
        {
            ".......HHHH.........",
            "......HHHHHH........",
            "......HSSSHH........",
            "......SSSSS.........",
            ".......SSS..........",
            "......AAAAA.........",
            ".....AAAMAA.........",
            ".....AAAAAA.....W...",
            "....CCAAAAA....WW...",
            "....CCAAAAA...WW....",
            ".....AAAAAA..WW.....",
            ".....AAAAAA.WW......",
            ".....AAAAAWWG.......",
            "......AAAA..........",
            "......AAAA..........",
            "......A..A..........",
            ".....A....A.........",
            ".....B....B.........",
            "....BB....BB........",
            "....BB.....B........",
            "...BB......BB.......",
            "...B........B.......",
            "..BB................",
            "..B.................",
        },
        // Кадр 2: ноги вместе (фаза полёта)
        {
            ".......HHHH.........",
            "......HHHHHH........",
            "......HSSSHH........",
            "......SSSSS.........",
            ".......SSS..........",
            "......AAAAA.........",
            ".....AAAMAA.........",
            ".....AAAAAA.....W...",
            "...CCCAAAAA....WW...",
            "...CCCAAAAA...WW....",
            ".....AAAAAA..WW.....",
            ".....AAAAAA.WW......",
            ".....AAAAAWWG.......",
            "......AAAA..........",
            "......AAAA..........",
            "......A..A..........",
            "......A..A..........",
            "......B..B..........",
            ".....BB..BB.........",
            ".....BB..BB.........",
            ".....B....B.........",
            "................... ",
            "....................",
            "....................",
        },
        // Кадр 3: левая нога впереди
        {
            ".......HHHH.........",
            "......HHHHHH........",
            "......HSSSHH........",
            "......SSSSS.........",
            ".......SSS..........",
            "......AAAAA.........",
            ".....AAAMAA.........",
            ".....AAAAAA.....W...",
            "....CCAAAAA....WW...",
            "....CCAAAAA...WW....",
            ".....AAAAAA..WW.....",
            ".....AAAAAA.WW......",
            ".....AAAAAWWG.......",
            "......AAAA..........",
            "......AAAA..........",
            "......A..A..........",
            ".....A....A.........",
            ".....B....B.........",
            "....BB....BB........",
            ".....B....BB........",
            "....BB......BB......",
            ".....B........B.....",
            "..............BB....",
            "...............B....",
        },
        // Кадр 4: ноги скрещиваются
        {
            ".......HHHH.........",
            "......HHHHHH........",
            "......HSSSHH........",
            "......SSSSS.........",
            ".......SSS..........",
            "......AAAAA.........",
            ".....AAAMAA.........",
            ".....AAAAAA.....W...",
            "..CCCCAAAAA....WW...",
            "..CCCCAAAAA...WW....",
            ".....AAAAAA..WW.....",
            ".....AAAAAA.WW......",
            ".....AAAAAWWG.......",
            "......AAAA..........",
            "......AAAA..........",
            "......ABBA..........",
            "......ABBA..........",
            ".....BBBBB..........",
            ".....BB.BB..........",
            "......B.B...........",
            "....................",
            "....................",
            "....................",
            "....................",
        },
    };

    public PixelWitcher(int startX, int startY, float speed, int scale) {
        this.x = startX;
        this.y = startY;
        this.speed = speed;
        this.scale = scale;
        this.frames = new BufferedImage[FRAME_DATA.length];
        for (int f = 0; f < FRAME_DATA.length; f++) {
            frames[f] = buildFrame(FRAME_DATA[f]);
        }
    }

    private BufferedImage buildFrame(String[] data) {
        int h = data.length;
        int w = data[0].length();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                char c = col < data[row].length() ? data[row].charAt(col) : '.';
                int argb = colorFor(c);
                img.setRGB(col, row, argb);
            }
        }
        return img;
    }

    private int colorFor(char c) {
        return switch (c) {
            case 'H' -> 0xFFDDDDDD; // белые волосы
            case 'S' -> 0xFFDEB887; // кожа
            case 'A' -> 0xFF2A2A2A; // тёмная броня
            case 'B' -> 0xFF3B2816; // сапоги (коричневые)
            case 'W' -> 0xFFC0C0C0; // серебряный меч
            case 'G' -> 0xFF88CCFF; // свечение меча
            case 'C' -> 0xFF1A1A2E; // плащ (тёмно-синий)
            case 'M' -> 0xFFDAA520; // медальон (золото)
            default  -> 0x00000000; // прозрачный
        };
    }

    public void update(int screenWidth) {
        frameTimer++;
        if (frameTimer >= 6) {
            frameTimer = 0;
            currentFrame = (currentFrame + 1) % frames.length;
        }
        x += (int) speed;
        if (x > screenWidth + 40) {
            x = -60;
        }
    }

    public void draw(Graphics2D g) {
        BufferedImage frame = frames[currentFrame];
        int drawW = frame.getWidth() * scale;
        int drawH = frame.getHeight() * scale;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(frame, x, y, drawW, drawH, null);
    }

    public void draw(Graphics2D g, int scaleOverride) {
        BufferedImage frame = frames[currentFrame];
        int drawW = frame.getWidth() * scaleOverride;
        int drawH = frame.getHeight() * scaleOverride;
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(frame, x, y, drawW, drawH, null);
    }
}
