package main.java.com.witcher.ui.graphics;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Единый игровой шрифт для Swing UI (кириллица + латиница).
 * Приоритет: game.ttf → PixelifySans.ttf → системный SansSerif.
 */
public final class GameFonts {

    private static final GameFonts INSTANCE = new GameFonts();

    private final Font base;
    private final boolean custom;
    private final Map<String, Font> cache = new HashMap<>();

    private GameFonts() {
        Font loaded = tryLoad("/assets/fonts/game.ttf");
        if (loaded == null) {
            loaded = tryLoad("/assets/fonts/PixelifySans.ttf");
        }
        if (loaded != null) {
            base = loaded;
            custom = true;
        } else {
            base = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
            custom = false;
            System.err.println("[GameFonts] Ispolzuetsya zapasnoj shrift SansSerif");
        }
    }

    public static GameFonts get() {
        return INSTANCE;
    }

    public boolean isCustomLoaded() {
        return custom;
    }

    public Font plain(int size) {
        return derive(size, Font.PLAIN);
    }

    public Font bold(int size) {
        return derive(size, Font.BOLD);
    }

    public Font italic(int size) {
        return derive(size, Font.ITALIC);
    }

    /** Диалоги и длинный текст — чуть мягче. */
    public static void applyDialogHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    /** Карточки, HUD, мелкий UI — чётче. */
    public static void applyPixelHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    private Font derive(int size, int style) {
        int safeSize = Math.max(6, size);
        String key = safeSize + ":" + style;
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
