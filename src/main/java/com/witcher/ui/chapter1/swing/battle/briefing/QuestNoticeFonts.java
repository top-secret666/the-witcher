package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Читаемые шрифты листа заказа — Philosopher (кириллица, чёткие штрихи на пергаменте).
 */
public final class QuestNoticeFonts {

  private static final String REGULAR_PATH = "/assets/fonts/Philosopher-Regular.ttf";
  private static final String BOLD_PATH = "/assets/fonts/Philosopher-Bold.ttf";
  private static final String CYRILLIC_PROBE = "АбвГдеЁжЗийКлмнопрстуфхцчшщъыьэюя";

  private static final Font REGULAR = loadFont(REGULAR_PATH, Font.PLAIN);
  private static final Font BOLD = loadFont(BOLD_PATH, Font.BOLD);
  private static final Map<String, Font> CACHE = new HashMap<>();

  private QuestNoticeFonts() {
  }

  public static Font header(int size) {
    return sized(BOLD, size);
  }

  public static Font title(int size) {
    return sized(BOLD, size);
  }

  public static Font body(int size) {
    return sized(REGULAR, size);
  }

  private static Font sized(Font base, int size) {
    int px = Math.max(10, size);
    String key = base.getFontName() + ":" + px;
    return CACHE.computeIfAbsent(key, k -> base.deriveFont((float) px));
  }

  private static Font loadFont(String resourcePath, int fallbackStyle) {
    Font loaded = tryLoad(resourcePath);
    if (loaded != null && supportsCyrillic(loaded)) {
      return loaded;
    }
    return new Font(Font.SANS_SERIF, fallbackStyle, 12);
  }

  private static Font tryLoad(String resourcePath) {
    try (InputStream in = QuestNoticeFonts.class.getResourceAsStream(resourcePath)) {
      if (in == null) {
        return null;
      }
      Font font = Font.createFont(Font.TRUETYPE_FONT, in);
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
      return font;
    } catch (Exception e) {
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
}
