package main.java.com.witcher.ui.graphics;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Пиксельные значки мини-статов в строках каталога. */
public final class ShopStatGlyphs {

    public static final int ROW_ICON_SIZE = 9;
    public static final int LEGEND_ICON_SIZE = 13;
    public static final int ROW_GAP = 5;
    public static final int LEGEND_GAP = 12;

    private static final Color LABEL_COLOR = new Color(255, 238, 170);
    private static final Color POS_VALUE = new Color(168, 214, 148);
    private static final Color NEG_VALUE = new Color(214, 148, 128);
    private static final Color ZERO_VALUE = new Color(150, 142, 128);

    private static final Color[] FILL = {
        new Color(178, 58, 52),
        new Color(42, 168, 88),
        new Color(52, 118, 208)
    };
    private static final Color[] GLOW = {
        new Color(255, 148, 128),
        new Color(128, 240, 168),
        new Color(148, 188, 255)
    };
    private static final Color RIM = new Color(218, 178, 88);
    private static final Color SHADOW = new Color(28, 18, 10, 200);

    private static final String[] LEGEND_LABELS = {"Защита", "Выносливость", "Знаки"};

    private ShopStatGlyphs() {
    }

    public static boolean hasAnyDelta(int prot, int stamina, int signs) {
        return prot != 0 || stamina != 0 || signs != 0;
    }

    public static Rectangle legendBounds(FontMetrics fm) {
        int padX = 10;
        int padY = 4;
        int w = legendContentWidth(fm) + padX * 2;
        int h = Math.max(LEGEND_ICON_SIZE, fm.getHeight()) + padY * 2;
        return new Rectangle(0, 0, w, h);
    }

    public static int legendContentWidth(FontMetrics fm) {
        int width = 0;
        for (int i = 0; i < LEGEND_LABELS.length; i++) {
            if (i > 0) {
                width += LEGEND_GAP;
            }
            width += LEGEND_ICON_SIZE + 4 + fm.stringWidth(LEGEND_LABELS[i]);
        }
        return width;
    }

    public static void drawLegend(Graphics2D g, int x, int y, FontMetrics fm, float alpha) {
        Rectangle box = legendBounds(fm);
        box.x = x;
        box.y = y;
        ShopUiDraw.drawGoldHudChip(g, box, alpha);
        int cx = x + 10;
        int baselineY = (y + box.height - 5 + 1) & ~1;
        int iconY = baselineY - LEGEND_ICON_SIZE + 1;
        for (int i = 0; i < LEGEND_LABELS.length; i++) {
            if (i > 0) {
                cx += LEGEND_GAP;
            }
            drawIcon(g, i, cx, iconY, LEGEND_ICON_SIZE);
            cx += LEGEND_ICON_SIZE + 4;
            int labelX = (cx + 1) & ~1;
            ShopUiDraw.drawOutlinedText(g, LEGEND_LABELS[i], labelX, baselineY, LABEL_COLOR);
            cx += fm.stringWidth(LEGEND_LABELS[i]);
        }
    }

    public static int rowWidth(FontMetrics fm, int prot, int stamina, int signs) {
        if (!hasAnyDelta(prot, stamina, signs)) {
            return 0;
        }
        int width = 0;
        for (int value : new int[]{prot, stamina, signs}) {
            if (width > 0) {
                width += ROW_GAP;
            }
            width += ROW_ICON_SIZE + 1 + fm.stringWidth(formatValue(value));
        }
        return width;
    }

    public static void drawRow(Graphics2D g, int rightX, int baselineY, FontMetrics fm,
                               int prot, int stamina, int signs) {
        if (!hasAnyDelta(prot, stamina, signs)) {
            return;
        }
        int[] values = {prot, stamina, signs};
        int width = rowWidth(fm, prot, stamina, signs);
        int cx = rightX - width;
        int iconY = baselineY - ROW_ICON_SIZE + 1;
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                cx += ROW_GAP;
            }
            drawIcon(g, i, cx, iconY, ROW_ICON_SIZE);
            cx += ROW_ICON_SIZE + 1;
            String text = formatValue(values[i]);
            Color color = values[i] > 0 ? POS_VALUE : values[i] < 0 ? NEG_VALUE : ZERO_VALUE;
            ShopUiDraw.drawOutlinedText(g, text, cx, baselineY, color);
            cx += fm.stringWidth(text);
        }
    }

    private static String formatValue(int delta) {
        return delta > 0 ? "+" + delta : String.valueOf(delta);
    }

    private static void drawIcon(Graphics2D g, int kind, int x, int y, int size) {
        switch (kind) {
            case 0 -> drawShield(g, x, y, size);
            case 1 -> drawLightning(g, x, y, size);
            default -> drawSign(g, x, y, size);
        }
    }

    private static void drawShield(Graphics2D g, int x, int y, int size) {
        int[] xs = {x + size / 2, x + size - 1, x + size - 2, x + size / 2, x + 1, x};
        int[] ys = {y, y + 1, y + size - 1, y + size, y + size - 1, y + 1};
        fillGlyph(g, xs, ys, 0);
        g.setColor(RIM);
        for (int i = 0; i < xs.length; i++) {
            int j = (i + 1) % xs.length;
            g.drawLine(xs[i], ys[i], xs[j], ys[j]);
        }
        g.setColor(GLOW[0]);
        g.fillRect(x + size / 2, y + 2, 1, Math.max(2, size / 2));
    }

    private static void drawLightning(Graphics2D g, int x, int y, int size) {
        g.setColor(SHADOW);
        g.fillRect(x + size / 2, y + 1, size / 2, 2);
        g.fillRect(x + size / 2 - 1, y + size / 2, size / 2 + 1, 2);
        g.fillRect(x + 1, y + size - 3, size / 2 + 1, 2);
        g.setColor(FILL[1]);
        g.fillRect(x + size / 2 - 1, y, size / 2, 2);
        g.fillRect(x + size / 2 - 2, y + size / 2 - 1, size / 2, 2);
        g.fillRect(x, y + size - 4, size / 2, 2);
        g.setColor(RIM);
        g.drawLine(x + size / 2 - 1, y, x + size - 2, y + 1);
        g.drawLine(x + size - 2, y + 1, x + size / 2 - 2, y + size / 2);
        g.drawLine(x + size / 2 - 2, y + size / 2, x + size - 2, y + size / 2);
        g.drawLine(x + size - 2, y + size / 2, x, y + size - 2);
        g.setColor(GLOW[1]);
        g.fillRect(x + size / 2, y + 1, 1, 1);
    }

    private static void drawSign(Graphics2D g, int x, int y, int size) {
        int mid = x + size / 2;
        int midY = y + size / 2;
        int[] xs = {mid, x + size - 1, mid, x + 1};
        int[] ys = {y + 1, midY, y + size - 1, midY};
        fillGlyph(g, xs, ys, 2);
        g.setColor(RIM);
        for (int i = 0; i < xs.length; i++) {
            int j = (i + 1) % xs.length;
            g.drawLine(xs[i], ys[i], xs[j], ys[j]);
        }
        g.setColor(GLOW[2]);
        g.fillRect(mid, midY - 1, 1, 1);
        g.fillRect(mid - 1, midY, 1, 1);
    }

    private static void fillGlyph(Graphics2D g, int[] xs, int[] ys, int kind) {
        g.setColor(SHADOW);
        g.fillPolygon(offset(xs, 1), offset(ys, 1), xs.length);
        g.setColor(FILL[kind]);
        g.fillPolygon(xs, ys, xs.length);
    }

    private static int[] offset(int[] arr, int d) {
        int[] out = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            out[i] = arr[i] + d;
        }
        return out;
    }
}
