package main.java.com.witcher.ui.graphics;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** Пиксельные значки мини-статов в строках каталога. */
public final class ShopStatGlyphs {

    public static final int ICON_SIZE = 8;
    public static final int GROUP_GAP = 5;

    private static final Color ICON_COLOR = new Color(198, 188, 168);
    private static final Color LABEL_COLOR = new Color(128, 112, 82);
    private static final Color POS_VALUE = new Color(168, 214, 148);
    private static final Color NEG_VALUE = new Color(214, 148, 128);
    private static final Color ZERO_VALUE = new Color(150, 142, 128);

    private ShopStatGlyphs() {
    }

    public static boolean hasAnyDelta(int prot, int stamina, int signs) {
        return prot != 0 || stamina != 0 || signs != 0;
    }

    public static int rowWidth(FontMetrics fm, int prot, int stamina, int signs) {
        if (!hasAnyDelta(prot, stamina, signs)) {
            return 0;
        }
        int width = 0;
        for (int value : new int[]{prot, stamina, signs}) {
            if (width > 0) {
                width += GROUP_GAP;
            }
            width += ICON_SIZE + 1 + fm.stringWidth(formatValue(value));
        }
        return width;
    }

    public static int legendWidth(FontMetrics fm) {
        int width = 0;
        String[] labels = {"Защ.", "Вын.", "Знак."};
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) {
                width += GROUP_GAP + 2;
            }
            width += ICON_SIZE + 2 + fm.stringWidth(labels[i]);
        }
        return width;
    }

    public static void drawLegend(Graphics2D g, int x, int baselineY, FontMetrics fm) {
        String[] labels = {"Защ.", "Вын.", "Знак."};
        int cx = x;
        int iconY = baselineY - ICON_SIZE + 1;
        for (int i = 0; i < labels.length; i++) {
            if (i > 0) {
                cx += GROUP_GAP + 2;
            }
            drawIcon(g, i, cx, iconY, ICON_COLOR);
            cx += ICON_SIZE + 2;
            ShopScreen.drawOutlinedText(g, labels[i], cx, baselineY, LABEL_COLOR);
            cx += fm.stringWidth(labels[i]);
        }
    }

    public static void drawRow(Graphics2D g, int rightX, int baselineY, FontMetrics fm,
                               int prot, int stamina, int signs) {
        if (!hasAnyDelta(prot, stamina, signs)) {
            return;
        }
        int[] values = {prot, stamina, signs};
        int width = rowWidth(fm, prot, stamina, signs);
        int cx = rightX - width;
        int iconY = baselineY - ICON_SIZE + 1;
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                cx += GROUP_GAP;
            }
            drawIcon(g, i, cx, iconY, ICON_COLOR);
            cx += ICON_SIZE + 1;
            String text = formatValue(values[i]);
            Color color = values[i] > 0 ? POS_VALUE : values[i] < 0 ? NEG_VALUE : ZERO_VALUE;
            ShopScreen.drawOutlinedText(g, text, cx, baselineY, color);
            cx += fm.stringWidth(text);
        }
    }

    private static String formatValue(int delta) {
        return delta > 0 ? "+" + delta : String.valueOf(delta);
    }

    private static void drawIcon(Graphics2D g, int kind, int x, int y, Color color) {
        g.setColor(color);
        switch (kind) {
            case 0 -> drawShield(g, x, y);
            case 1 -> drawLightning(g, x, y);
            default -> drawSign(g, x, y);
        }
    }

    private static void drawShield(Graphics2D g, int x, int y) {
        g.drawLine(x + 3, y, x + 7, y + 1);
        g.drawLine(x + 7, y + 1, x + 6, y + 6);
        g.drawLine(x + 6, y + 6, x + 3, y + 7);
        g.drawLine(x + 3, y + 7, x + 1, y + 6);
        g.drawLine(x + 1, y + 6, x, y + 1);
        g.drawLine(x, y + 1, x + 3, y);
        g.drawLine(x + 3, y + 2, x + 3, y + 6);
    }

    private static void drawLightning(Graphics2D g, int x, int y) {
        g.drawLine(x + 5, y, x + 2, y + 4);
        g.drawLine(x + 2, y + 4, x + 4, y + 4);
        g.drawLine(x + 4, y + 4, x + 1, y + 7);
        g.drawLine(x + 5, y + 2, x + 3, y + 2);
        g.drawLine(x + 3, y + 2, x + 6, y);
    }

    private static void drawSign(Graphics2D g, int x, int y) {
        g.drawLine(x + 3, y, x + 6, y + 3);
        g.drawLine(x + 6, y + 3, x + 3, y + 6);
        g.drawLine(x + 3, y + 6, x, y + 3);
        g.drawLine(x, y + 3, x + 3, y);
        g.drawLine(x + 3, y + 1, x + 3, y + 5);
        g.drawLine(x + 1, y + 3, x + 5, y + 3);
    }
}
