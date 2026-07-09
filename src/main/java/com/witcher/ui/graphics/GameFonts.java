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
 * Шрифты UI (Swing): готический TTF с кириллицей.
 * <p>
 * Основной — MedievalSharp (острый средневековый готический, Google Fonts OFL).
 */
public final class GameFonts {

    private static final String CYRILLIC_PROBE = "АбвГдеЁжЗийКлмнопрстуфхцчшщъыьэюя";

    private static final String[] GOTHIC_PATHS = {
        "/assets/fonts/MedievalSharp-Regular.ttf",
        "/assets/fonts/lavka/MedievalSharp-Regular.ttf",
        "/assets/fonts/Philosopher-Bold.ttf",
        "/assets/fonts/Philosopher-Regular.ttf"
    };

    private static final GameFonts INSTANCE = new GameFonts();

    private final Font gothicBase;
    private final Map<String, Font> cache = new HashMap<>();

    private GameFonts() {
        Font loaded = loadFirst(GOTHIC_PATHS);
        if (loaded == null) {
            loaded = pickSystemGothic();
        }
        gothicBase = loaded;
        System.out.println("[GameFonts] Gothic: " + gothicBase.getFontName()
            + (supportsCyrillic(gothicBase) ? " (kirillica OK)" : " (NET kirillicy!)"));
    }

    public static GameFonts get() {
        return INSTANCE;
    }

    public Font plain(int size) {
        return derive(gothicBase, size, Font.PLAIN);
    }

    public Font bold(int size) {
        return derive(gothicBase, size, Font.BOLD);
    }

    public Font italic(int size) {
        return derive(gothicBase, size, Font.ITALIC);
    }

    public Font uiPlain(int size) {
        return plain(size);
    }

    public Font uiBold(int size) {
        return bold(size);
    }

    public Font uiItalic(int size) {
        return italic(size);
    }

    public static void applyDialogHints(Graphics2D g) {
        applyGothicHints(g);
    }

    public static void applyPixelHints(Graphics2D g) {
        applyGameHints(g);
    }

    public static void applyGameHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    /** Готический TTF — чёткие штрихи, без размытия. */
    public static void applyGothicHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    public static void applyUiOverlayHints(Graphics2D g) {
        applyGothicHints(g);
    }

    public static void drawOutlined(Graphics2D g, String text, int x, int y, Color fill) {
        int tx = (x + 1) & ~1;
        int ty = (y + 1) & ~1;
        g.setColor(new Color(12, 8, 4, 220));
        g.drawString(text, tx + 1, ty);
        g.drawString(text, tx - 1, ty);
        g.drawString(text, tx, ty + 1);
        g.setColor(fill);
        g.drawString(text, tx, ty);
    }

    public static void drawShadowed(Graphics2D g, String text, int x, int y, Color fill) {
        int tx = (x + 1) & ~1;
        int ty = (y + 1) & ~1;
        g.setColor(new Color(0, 0, 0, 140));
        g.drawString(text, tx + 1, ty + 1);
        g.setColor(fill);
        g.drawString(text, tx, ty);
    }

    private Font derive(Font base, int size, int style) {
        int safeSize = Math.max(6, size);
        String key = base.getFontName() + ":" + safeSize + ":" + style;
        return cache.computeIfAbsent(key, k -> base.deriveFont(style, (float) safeSize));
    }

    private static Font loadFirst(String[] paths) {
        for (String path : paths) {
            Font font = tryLoad(path);
            if (font != null && supportsCyrillic(font)) {
                return font;
            }
            if (font != null) {
                System.err.println("[GameFonts] Propusk (net kirillicy): " + path);
            }
        }
        return null;
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

    private static boolean supportsCyrillic(Font font) {
        Font probe = font.deriveFont(Font.PLAIN, 12f);
        for (int i = 0; i < CYRILLIC_PROBE.length(); i++) {
            if (!probe.canDisplay(CYRILLIC_PROBE.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private static Font pickSystemGothic() {
        String[] names = {
            "MedievalSharp",
            "UnifrakturMaguntia",
            "UnifrakturCook",
            "Forum",
            "Cinzel",
            "Times New Roman"
        };
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        for (String want : names) {
            for (String have : available) {
                if (have.equalsIgnoreCase(want)) {
                    Font candidate = new Font(have, Font.PLAIN, 12);
                    if (supportsCyrillic(candidate)) {
                        System.out.println("[GameFonts] Sistemnyj gothic: " + have);
                        return candidate;
                    }
                }
            }
        }
        System.out.println("[GameFonts] Zapasnoj: Times New Roman");
        return new Font("Times New Roman", Font.PLAIN, 12);
    }
}
