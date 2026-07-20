package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.ui.graphics.GameFonts;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Рукописный, но читаемый шрифт для листа заказа — Forum (OFL, полная кириллица).
 */
public final class QuestNoticeFonts {

  private static final String FORUM_PATH = "/assets/fonts/Forum-Regular.ttf";
  private static final String CYRILLIC_PROBE = "АбвГдеЁжЗийКлмнопрстуфхцчшщъыьэюя";

  private static final Font BASE = loadForum();
  private static final Map<String, Font> CACHE = new HashMap<>();

  private QuestNoticeFonts() {
  }

  /** Заголовок «ЗАКАЗ НА МОНСТРА». */
  public static Font header(int size) {
    return derive(Math.max(10, size), Font.BOLD);
  }

  /** Имя цели — крупнее, чуть жирнее. */
  public static Font title(int size) {
    return derive(Math.max(11, size), Font.BOLD);
  }

  /** Основной текст контракта. */
  public static Font body(int size) {
    return derive(Math.max(8, size), Font.PLAIN);
  }

  /** Печать / подпись внизу. */
  public static Font seal(int size) {
    return derive(Math.max(7, size), Font.ITALIC);
  }

  public static void applyInkHints(Graphics2D g) {
    GameFonts.applyGothicHints(g);
  }

  private static Font derive(int size, int style) {
    String key = size + ":" + style;
    return CACHE.computeIfAbsent(key, k -> BASE.deriveFont(style, (float) size));
  }

  private static Font loadForum() {
    Font loaded = tryLoad(FORUM_PATH);
    if (loaded != null && supportsCyrillic(loaded)) {
      return loaded;
    }
    return GameFonts.get().plain(12);
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
