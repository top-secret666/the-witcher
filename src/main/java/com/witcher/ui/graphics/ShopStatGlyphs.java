package main.java.com.witcher.ui.graphics;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Пиксельные значки мини-статов в строках каталога. */
public final class ShopStatGlyphs {

    public static final int ROW_ICON_SIZE = 8;
    public static final int LEGEND_ICON_SIZE = 10;
    public static final int ROW_GAP = 5;
    public static final int LEGEND_GAP = 10;

    private static final Color ICON_COLOR = new Color(220, 205, 175);
    private static final Color LABEL_COLOR = new Color(255, 230, 155);
    private static final Color POS_VALUE = new Color(168, 214, 148);
    private static final Color NEG_VALUE = new Color(214, 148, 128);
    private static final Color ZERO_VALUE = new Color(150, 142, 128);

    private static final String[] LEGEND_LABELS = {"Защита", "Выносливость", "Знаки"};

    private ShopStatGlyphs() {
    }

    public static boolean hasAnyDelta(int prot, int stamina, int signs) {
        return prot != 0 || stamina != 0 || signs != 0;
    }

    public static Rectangle legendBounds(FontMetrics fm) {
        int padX = 8;
        int padY = 3;
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
            width += LEGEND_ICON_SIZE + 3 + fm.stringWidth(LEGEND_LABELS[i]);
        }
        return width;
    }

    public static void drawLegend(Graphics2D g, int x, int y, FontMetrics fm, float alpha) {
        Rectangle box = legendBounds(fm);
        box.x = x;
        box.y = y;
        ShopScreen.drawGoldHudChip(g, box, alpha);
        int cx = x + 8;
        int baselineY = y + box.height - 4;
        int iconY = baselineY - LEGEND_ICON_SIZE + 1;
        for (int i = 0; i < LEGEND_LABELS.length; i++) {
            if (i > 0) {
                cx += LEGEND_GAP;
            }
            drawIcon(g, i, cx, iconY, LEGEND_ICON_SIZE, ICON_COLOR);
            cx += LEGEND_ICON_SIZE + 3;
            ShopScreen.drawOutlinedText(g, LEGEND_LABELS[i], cx, baselineY, LABEL_COLOR);
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
            drawIcon(g, i, cx, iconY, ROW_ICON_SIZE, ICON_COLOR);
            cx += ROW_ICON_SIZE + 1;
            String text = formatValue(values[i]);
            Color color = values[i] > 0 ? POS_VALUE : values[i] < 0 ? NEG_VALUE : ZERO_VALUE;
            ShopScreen.drawOutlinedText(g, text, cx, baselineY, color);
            cx += fm.stringWidth(text);
        }
    }

    private static String formatValue(int delta) {
        return delta > 0 ? "+" + delta : String.valueOf(delta);
    }

    private static void drawIcon(Graphics2D g, int kind, int x, int y, int size, Color color) {
        g.setColor(color);
        switch (kind) {
            case 0 -> drawShield(g, x, y, size);
            case 1 -> drawLightning(g, x, y, size);
            default -> drawSign(g, x, y, size);
        }
    }

    private static void drawShield(Graphics2D g, int x, int y, int size) {
        int s = size - 1;
        g.drawLine(x + s / 2, y, x + s, y + 1);
        g.drawLine(x + s, y + 1, x + s - 1, y + s);
        g.drawLine(x + s - 1, y + s, x + s / 2, y + s);
        g.drawLine(x + s / 2, y + s, x + 1, y + s);
        g.drawLine(x + 1, y + s, x, y + 1);
        g.drawLine(x, y + 1, x + s / 2, y);
        g.drawLine(x + s / 2, y + 2, x + s / 2, y + s - 1);
    }

    private static void drawLightning(Graphics2D g, int x, int y, int size) {
        g.drawLine(x + size - 2, y, x + size / 2, y + size / 2 - 1);
        g.drawLine(x + size / 2, y + size / 2 - 1, x + size - 2, y + size / 2 - 1);
        g.drawLine(x + size - 2, y + size / 2 - 1, x + 2, y + size - 1);
        g.drawLine(x + size - 3, y + 2, x + size / 2 + 1, y + 2);
    }

    private static void drawSign(Graphics2D g, int x, int y, int size) {
        int mid = x + size / 2;
        int midY = y + size / 2;
        g.drawLine(mid, y + 1, x + size - 1, midY);
        g.drawLine(x + size - 1, midY, mid, y + size - 1);
        g.drawLine(mid, y + size - 1, x + 1, midY);
        g.drawLine(x + 1, midY, mid, y + 1);
    }
}
