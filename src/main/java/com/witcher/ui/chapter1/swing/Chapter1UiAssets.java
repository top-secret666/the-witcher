package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.chapter1.view.Chapter1AssetPaths;
import main.java.com.witcher.ui.graphics.PixelScaler;
import main.java.com.witcher.ui.graphics.Sprite;
import main.java.com.witcher.ui.graphics.SpriteSheet;

import java.awt.image.BufferedImage;

/** Ленивая загрузка UI-ассетов главы 1. Крупные PNG сразу режутся, чтобы не взрывать heap. */
public final class Chapter1UiAssets {

  private static final int MAX_MAP_EDGE = 640;
  private static final int MAX_PORTRAIT_EDGE = 512;
  private static final int MAX_ICON_EDGE = 256;

  private static BufferedImage terminalFrame;
  private static BufferedImage timerBar;
  private static BufferedImage bootBg;
  private static BufferedImage hiddenHint;

  private Chapter1UiAssets() {
  }

  public static BufferedImage terminalFrame() {
    if (terminalFrame == null) {
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.HACK_FRAME);
      terminalFrame = sprite != null ? sprite.getImage() : null;
    }
    return terminalFrame;
  }

  public static BufferedImage timerBar() {
    if (timerBar == null) {
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.HACK_TIMER);
      timerBar = sprite != null ? sprite.getImage() : null;
    }
    return timerBar;
  }

  public static BufferedImage bootBackground() {
    if (bootBg == null) {
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.BOOT_BG);
      bootBg = sprite != null ? sprite.getImage() : null;
    }
    return bootBg;
  }

  public static BufferedImage hiddenHint() {
    if (hiddenHint == null) {
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.HACK_HIDDEN_HINT);
      hiddenHint = sprite != null ? sprite.getImage() : null;
    }
    return hiddenHint;
  }

  private static BufferedImage cardClosed;
  private static BufferedImage cardIcon;

  public static BufferedImage cardClosed() {
    if (cardClosed == null) {
      cardClosed = loadCapped(Chapter1AssetPaths.CARD_CLOSED, MAX_MAP_EDGE);
    }
    return cardClosed;
  }

  public static BufferedImage cardIcon() {
    if (cardIcon == null) {
      cardIcon = loadCapped(Chapter1AssetPaths.CARD_ICON, MAX_ICON_EDGE);
    }
    return cardIcon;
  }

  private static BufferedImage bossMapOpen;
  private static BufferedImage bossMapClosed;
  private static BufferedImage bossDukeMapIcon;
  private static BufferedImage bossDukeMapHoverIcon;
  private static BufferedImage bossDukePortrait;
  private static BufferedImage bossWakeForest;
  private static SpriteSheet swordSlashSheetA;
  private static SpriteSheet swordSlashSheetB;
  private static boolean swordSlashTried;

  public static BufferedImage bossMapOpen() {
    if (bossMapOpen == null) {
      bossMapOpen = loadCapped(Chapter1AssetPaths.CARD_MAP_OPEN, MAX_MAP_EDGE);
    }
    return bossMapOpen;
  }

  public static BufferedImage bossMapClosed() {
    if (bossMapClosed == null) {
      bossMapClosed = loadCapped(Chapter1AssetPaths.CARD_CLOSED, MAX_MAP_EDGE);
    }
    return bossMapClosed;
  }

  public static BufferedImage bossMapIcon(String path) {
    if (path == null) {
      return null;
    }
    if (path.contains("boss_duke_map_hover") || path.equals(Chapter1AssetPaths.BOSS_DUKE_MAP_HOVER)) {
      if (bossDukeMapHoverIcon == null) {
        bossDukeMapHoverIcon = loadCappedCrisp(path, MAX_ICON_EDGE);
      }
      return bossDukeMapHoverIcon;
    }
    if (path.contains("boss_duke_map") || path.equals(Chapter1AssetPaths.BOSS_DUKE_MAP)) {
      if (bossDukeMapIcon == null) {
        bossDukeMapIcon = loadCappedCrisp(path, MAX_ICON_EDGE);
      }
      return bossDukeMapIcon;
    }
    return loadCappedCrisp(path, MAX_ICON_EDGE);
  }

  public static BufferedImage bossWakeForest() {
    if (bossWakeForest == null) {
      bossWakeForest = loadCapped(Chapter1AssetPaths.BOSS_WAKE_FOREST, MAX_MAP_EDGE);
    }
    return bossWakeForest;
  }

  public static BufferedImage bossPortrait(String path) {
    if (path != null && (path.contains("boss_duke_portrait")
        || path.equals(Chapter1AssetPaths.BOSS_DUKE_PORTRAIT))) {
      if (bossDukePortrait == null) {
        bossDukePortrait = loadCappedCrisp(path, MAX_PORTRAIT_EDGE);
      }
      return bossDukePortrait;
    }
    return loadCappedCrisp(path, MAX_PORTRAIT_EDGE);
  }

  /** Кадр проблеска меча: лист A (6×4) или B (2×4), чёрный фон вырезан. */
  public static BufferedImage swordSlashFrame(
      main.java.com.witcher.chapter1.battle.SwordSlashShowTimeline.SheetId sheet, int index) {
    ensureSwordSlashSheets();
    SpriteSheet frames = sheet == main.java.com.witcher.chapter1.battle.SwordSlashShowTimeline.SheetId.A
        ? swordSlashSheetA
        : swordSlashSheetB;
    return frames != null ? frames.getFrame(index) : null;
  }

  public static boolean swordSlashSheetsReady() {
    ensureSwordSlashSheets();
    return swordSlashSheetA != null || swordSlashSheetB != null;
  }

  private static void ensureSwordSlashSheets() {
    if (swordSlashTried) {
      return;
    }
    swordSlashTried = true;
    // чёрный → прозрачный, чтобы рисовать поверх чёрного кадра катсцены
    swordSlashSheetA = SpriteSheet.loadOptional(
        Chapter1AssetPaths.SWORD_SLASH_SHEET_A, 6, 4, 1, true);
    swordSlashSheetB = SpriteSheet.loadOptional(
        Chapter1AssetPaths.SWORD_SLASH_SHEET_B, 2, 4, 1, true);
  }

  private static BufferedImage loadCapped(String path, int maxEdge) {
    Sprite sprite = Sprite.loadOptional(path);
    if (sprite == null) {
      return null;
    }
    return capEdge(sprite.getImage(), maxEdge);
  }

  private static BufferedImage loadCappedCrisp(String path, int maxEdge) {
    Sprite sprite = Sprite.loadOptional(path);
    if (sprite == null) {
      return null;
    }
    return capEdgeCrisp(sprite.getImage(), maxEdge);
  }

  /** Ужимает оригинал один раз при загрузке — дальше ScaledImageCache не аллоцирует гигантские half. */
  static BufferedImage capEdge(BufferedImage src, int maxEdge) {
    if (src == null || maxEdge <= 0) {
      return src;
    }
    int w = src.getWidth();
    int h = src.getHeight();
    if (w <= maxEdge && h <= maxEdge) {
      return src;
    }
    float scale = Math.min((float) maxEdge / w, (float) maxEdge / h);
    int dw = Math.max(1, Math.round(w * scale));
    int dh = Math.max(1, Math.round(h * scale));
    return PixelScaler.smoothScale(src, dw, dh);
  }

  /** Иконки/портреты боссов — nearest, без размытия. */
  static BufferedImage capEdgeCrisp(BufferedImage src, int maxEdge) {
    if (src == null || maxEdge <= 0) {
      return src;
    }
    int w = src.getWidth();
    int h = src.getHeight();
    if (w <= maxEdge && h <= maxEdge) {
      return src;
    }
    float scale = Math.min((float) maxEdge / w, (float) maxEdge / h);
    int dw = Math.max(1, Math.round(w * scale));
    int dh = Math.max(1, Math.round(h * scale));
    return PixelScaler.crispScale(src, dw, dh);
  }
}
