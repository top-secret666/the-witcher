package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.assets.Chapter1AssetPaths;
import main.java.com.witcher.ui.graphics.PixelScaler;
import main.java.com.witcher.ui.graphics.Sprite;
import main.java.com.witcher.ui.chapter1.swing.glitch.WitcherGlitchPalette;

import java.awt.image.BufferedImage;

/** Ленивая загрузка UI-ассетов главы 1. Крупные PNG сразу режутся, чтобы не взрывать heap. */
public final class Chapter1UiAssets {

  private static final int MAX_MAP_EDGE = 640;
  private static final int MAX_PORTRAIT_EDGE = 512;
  /** Полноростовые volk-спрайты — почти без ужимания, как портреты интро. */
  private static final int MAX_VOLK_EDGE = 1280;
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
  private static BufferedImage volkDukeMap;
  private static BufferedImage volkDukeMapAttack;
  private static BufferedImage volkDukeMapInterested;
  private static BufferedImage volkDukeMapLunge;
  private static BufferedImage bossBloodCorridor;
  private static BufferedImage bossGlitchAwakenSheet;
  private static BufferedImage wolfShardReveal;
  private static BufferedImage wolfShardAwaken;
  private static BufferedImage wolfForestEyes;
  private static BufferedImage wolfMistForest;
  private static BufferedImage bossWakeForest;
  private static BufferedImage bossQuestNotice;
  private static BufferedImage memoryArdCarraig;
  private static BufferedImage memoryKaerMorhen;

  /** Кадры воспоминаний — только fullscreen-эпилог. */
  public static BufferedImage encounterMemoryImage(String path) {
    if (path == null) {
      return null;
    }
    if (path.equals(Chapter1AssetPaths.MEMORY_ARD_CARRAIG)) {
      if (memoryArdCarraig == null) {
        memoryArdCarraig = loadCappedCrisp(Chapter1AssetPaths.MEMORY_ARD_CARRAIG, MAX_VOLK_EDGE);
      }
      return memoryArdCarraig;
    }
    if (path.equals(Chapter1AssetPaths.MEMORY_KAER_MORHEN)) {
      if (memoryKaerMorhen == null) {
        memoryKaerMorhen = loadCappedCrisp(Chapter1AssetPaths.MEMORY_KAER_MORHEN, MAX_VOLK_EDGE);
      }
      return memoryKaerMorhen;
    }
    return loadCappedCrisp(path, MAX_VOLK_EDGE);
  }

  public static BufferedImage bossQuestNotice() {
    if (bossQuestNotice == null) {
      bossQuestNotice = loadCappedCrisp(Chapter1AssetPaths.BOSS_QUEST_NOTICE, MAX_MAP_EDGE);
    }
    return bossQuestNotice;
  }

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

  public static BufferedImage bossBloodCorridor() {
    if (bossBloodCorridor == null) {
      bossBloodCorridor = loadCappedSharpPaletted(Chapter1AssetPaths.BOSS_BLOOD_CORRIDOR, MAX_VOLK_EDGE);
    }
    return bossBloodCorridor;
  }

  public static BufferedImage bossGlitchAwakenSheet() {
    if (bossGlitchAwakenSheet == null) {
      bossGlitchAwakenSheet = loadCappedSharpPaletted(Chapter1AssetPaths.BOSS_GLITCH_AWAKEN_SHEET, MAX_VOLK_EDGE);
    }
    return bossGlitchAwakenSheet;
  }

  public static BufferedImage wolfShardReveal() {
    if (wolfShardReveal == null) {
      wolfShardReveal = loadCappedSharpPaletted(Chapter1AssetPaths.WOLF_SHARD_REVEAL, MAX_VOLK_EDGE);
    }
    return wolfShardReveal;
  }

  public static BufferedImage wolfShardAwaken() {
    if (wolfShardAwaken == null) {
      // Палитру пока не трогаем — оригинальные цвета.
      wolfShardAwaken = loadCappedSharp(Chapter1AssetPaths.WOLF_SHARD_AWAKEN, MAX_VOLK_EDGE);
    }
    return wolfShardAwaken;
  }

  public static BufferedImage wolfForestEyes() {
    if (wolfForestEyes == null) {
      // Палитру пока не трогаем — оригинальные цвета.
      wolfForestEyes = loadCappedSharp(Chapter1AssetPaths.WOLF_FOREST_EYES, MAX_VOLK_EDGE);
    }
    return wolfForestEyes;
  }

  public static BufferedImage wolfMistForest() {
    if (wolfMistForest == null) {
      wolfMistForest = loadCappedSharp(Chapter1AssetPaths.WOLF_MIST_FOREST, MAX_VOLK_EDGE);
    }
    return wolfMistForest;
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

  /** Полноростовые спрайты герцога для VN пробуждения (эмоции). */
  public static BufferedImage volkDukeSprite(String path) {
    if (path == null) {
      return null;
    }
    if (path.contains("volk_duke_dialog_knife") || path.contains("volk_duke_map_attack")
        || path.equals(Chapter1AssetPaths.VOLK_DUKE_MAP_ATTACK)) {
      if (volkDukeMapAttack == null) {
        volkDukeMapAttack = loadCappedSharp(path, MAX_VOLK_EDGE);
      }
      return volkDukeMapAttack;
    }
    if (path.contains("volk_duke_dialog_lunge") || path.equals(Chapter1AssetPaths.VOLK_DUKE_MAP_LUNGE)) {
      if (volkDukeMapLunge == null) {
        volkDukeMapLunge = loadCappedSharp(path, MAX_VOLK_EDGE);
      }
      return volkDukeMapLunge;
    }
    if (path.contains("volk_duke_dialog_reach") || path.contains("volk_duke_map_interested")
        || path.equals(Chapter1AssetPaths.VOLK_DUKE_MAP_INTERESTED)) {
      if (volkDukeMapInterested == null) {
        volkDukeMapInterested = loadCappedSharp(path, MAX_VOLK_EDGE);
      }
      return volkDukeMapInterested;
    }
    if (path.contains("volk_duke_dialog_stand") || path.contains("volk_duke_map")
        || path.equals(Chapter1AssetPaths.VOLK_DUKE_MAP)) {
      if (volkDukeMap == null) {
        volkDukeMap = loadCappedSharp(path, MAX_VOLK_EDGE);
      }
      return volkDukeMap;
    }
    return loadCappedSharp(path, MAX_VOLK_EDGE);
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

  private static BufferedImage loadCappedSharp(String path, int maxEdge) {
    Sprite sprite = Sprite.loadOptional(path);
    if (sprite == null) {
      return null;
    }
    return capEdgeSharp(sprite.getImage(), maxEdge);
  }

  /** Глитч/horror-ассеты — после ужимания единая палитра главы 1. */
  private static BufferedImage loadCappedSharpPaletted(String path, int maxEdge) {
    BufferedImage capped = loadCappedSharp(path, maxEdge);
    return WitcherGlitchPalette.apply(capped);
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

  /** Крупные painted-спрайты (volk) — sharpScale, без лишнего ужима. */
  static BufferedImage capEdgeSharp(BufferedImage src, int maxEdge) {
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
    return PixelScaler.sharpScale(src, dw, dh);
  }
}
