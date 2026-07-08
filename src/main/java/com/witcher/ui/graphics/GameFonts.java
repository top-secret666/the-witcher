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
 * Шрифты UI: готический TTF из assets/fonts (если есть) + чёткий рендер.
 */
public final class GameFonts {

    private static final String CYRILLIC_PROBE = "АбвГдеЁжЗийКлмнопрстуфхцчшщъыьэюя";

    private static final String[] GOTHIC_BOLD_PATHS = {
        "/assets/fonts/Philosopher-Bold.ttf",
        "/assets/fonts/gothic-bold.ttf"
    };

    private static final String[] GOTHIC_PATHS = {
        "/assets/fonts/Philosopher-Regular.ttf",
        "/assets/fonts/Philosopher-Bold.ttf",
        "/assets/fonts/lavka/MedievalSharp-Regular.ttf",
        "/assets/fonts/MedievalSharp-Regular.ttf",
        "/assets/fonts/gothic.ttf",
        "/assets/fonts/witcher_ui.ttf",
        "/assets/fonts/game.ttf"
    };

    private static final GameFonts INSTANCE = new GameFonts();

    private final Font dialogBase;
    private final Font dialogBoldBase;
    private final Font uiBase;
    private final Font uiBoldBase;
    private final Map<String, Font> cache = new HashMap<>();

    private GameFonts() {
        Font gothic = loadFirst(GOTHIC_PATHS);
        if (gothic == null) {
            gothic = pickSystemCyrillic();
        }
        Font gothicBold = loadFirst(GOTHIC_BOLD_PATHS);
        if (gothicBold == null) {
            gothicBold = gothic;
        }
        dialogBase = gothic;
        dialogBoldBase = gothicBold;
        uiBase = gothic;
        uiBoldBase = gothicBold;
        System.out.println("[GameFonts] UI: " + uiBase.getFontName()
            + (supportsCyrillic(uiBase) ? " (kirillica OK)" : " (NET kirillicy!)"));
    }

    public static GameFonts get() {
        return INSTANCE;
    }

    public Font plain(int size) {
        return derive(dialogBase, size, Font.PLAIN);
    }

    public Font bold(int size) {
        return derive(dialogBoldBase, size, Font.PLAIN);
    }

    public Font italic(int size) {
        return derive(dialogBase, size, Font.ITALIC);
    }

    public Font uiPlain(int size) {
        return derive(uiBase, size, Font.PLAIN);
    }

    public Font uiBold(int size) {
        return derive(uiBoldBase, size, Font.PLAIN);
    }

    public Font uiItalic(int size) {
        return derive(uiBase, size, Font.ITALIC);
    }

    /** Диалоги — тот же готический TTF. */
    public static void applyDialogHints(Graphics2D g) {
        applyGothicHints(g);
    }

    /** Мелкий пиксельный текст (иконки, счётчики). */
    public static void applyPixelHints(Graphics2D g) {
        applyGameHints(g);
    }

    /** Пиксель-арт / мелкие подписи на карточках. */
    public static void applyGameHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    /** Готический TTF — чёткие контуры без «мыла». */
    public static void applyGothicHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    /** Текст поверх CRT — рисуется уже в ×2, остаётся читаемым. */
    public static void applyUiOverlayHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
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

    /** Запасные системные шрифты — только с кириллицей. */
    private static Font pickSystemCyrillic() {
        String[] names = {
            "MedievalSharp",
            "Cinzel",
            "PT Serif",
            "Times New Roman",
            "Georgia",
            "Palatino Linotype",
            "Segoe UI"
        };
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] available = ge.getAvailableFontFamilyNames();
        for (String want : names) {
            for (String have : available) {
                if (have.equalsIgnoreCase(want)) {
                    Font candidate = new Font(have, Font.PLAIN, 12);
                    if (supportsCyrillic(candidate)) {
                        System.out.println("[GameFonts] Sistemnyj shrift: " + have);
                        return candidate;
                    }
                }
            }
        }
        System.out.println("[GameFonts] Zapasnoj: Times New Roman");
        return new Font("Times New Roman", Font.PLAIN, 12);
    }
}
