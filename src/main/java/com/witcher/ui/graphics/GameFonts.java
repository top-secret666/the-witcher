package main.java.com.witcher.ui.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Шрифты Swing UI: диалоги (Serif) и HUD/лавка (SansSerif).
 * Опционально: положи свой game.ttf в assets/fonts/.
 */
public final class GameFonts {

    private static final GameFonts INSTANCE = new GameFonts();

    private final Font dialogBase;
    private final Font uiBase;
    private final Map<String, Font> cache = new HashMap<>();

    private GameFonts() {
        Font custom = tryLoad("/assets/fonts/game.ttf");
        if (custom != null) {
            dialogBase = custom;
            uiBase = custom;
        } else {
            dialogBase = new Font("Serif", Font.PLAIN, 12);
            uiBase = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
        }
    }

    public static GameFonts get() {
        return INSTANCE;
    }

    /** Диалоги, интро, VN. */
    public Font plain(int size) {
        return derive(dialogBase, size, Font.PLAIN);
    }

    public Font bold(int size) {
        return derive(dialogBase, size, Font.BOLD);
    }

    public Font italic(int size) {
        return derive(dialogBase, size, Font.ITALIC);
    }

    /** Лавка, карточки, HUD. */
    public Font uiPlain(int size) {
        return derive(uiBase, size, Font.PLAIN);
    }

    public Font uiBold(int size) {
        return derive(uiBase, size, Font.BOLD);
    }

    public Font uiItalic(int size) {
        return derive(uiBase, size, Font.ITALIC);
    }

    public static void applyGameHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    /** @deprecated use {@link #applyGameHints(Graphics2D)} */
    @Deprecated
    public static void applyDialogHints(Graphics2D g) {
        applyGameHints(g);
    }

    /** @deprecated use {@link #applyGameHints(Graphics2D)} */
    @Deprecated
    public static void applyPixelHints(Graphics2D g) {
        applyGameHints(g);
    }

    /** Тёмная обводка — текст читается на любом фоне. */
    public static void drawOutlined(Graphics2D g, String text, int x, int y, Color fill) {
        g.setColor(new Color(12, 8, 4, 200));
        g.drawString(text, x + 1, y);
        g.drawString(text, x - 1, y);
        g.drawString(text, x, y + 1);
        g.drawString(text, x, y - 1);
        g.setColor(fill);
        g.drawString(text, x, y);
    }

    /** Мягкая тень для длинных диалогов. */
    public static void drawShadowed(Graphics2D g, String text, int x, int y, Color fill) {
        g.setColor(new Color(0, 0, 0, 140));
        g.drawString(text, x + 1, y + 1);
        g.setColor(fill);
        g.drawString(text, x, y);
    }

    private Font derive(Font base, int size, int style) {
        int safeSize = Math.max(6, size);
        String key = base.getFamily() + ":" + safeSize + ":" + style;
        return cache.computeIfAbsent(key, k -> base.deriveFont(style, (float) safeSize));
    }

    private static Font tryLoad(String resourcePath) {
        try (InputStream in = GameFonts.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                return null;
            }
            Font font = Font.createFont(Font.TRUETYPE_FONT, in);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
            System.out.println("[GameFonts] Zagruzhen: " + resourcePath);
            return font;
        } catch (Exception e) {
            System.err.println("[GameFonts] Oshibka " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }
}
