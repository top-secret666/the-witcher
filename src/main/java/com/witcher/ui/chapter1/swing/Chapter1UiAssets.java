package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.ui.chapter1.view.Chapter1AssetPaths;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.image.BufferedImage;

/** Ленивая загрузка UI-ассетов главы 1. */
public final class Chapter1UiAssets {

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
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.CARD_CLOSED);
      cardClosed = sprite != null ? sprite.getImage() : null;
    }
    return cardClosed;
  }

  public static BufferedImage cardIcon() {
    if (cardIcon == null) {
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.CARD_ICON);
      cardIcon = sprite != null ? sprite.getImage() : null;
    }
    return cardIcon;
  }

  private static BufferedImage bossMapOpen;
  private static BufferedImage bossMapClosed;
  private static BufferedImage bossDukeMapIcon;
  private static BufferedImage bossDukePortrait;
  private static BufferedImage bossWakeForest;

  public static BufferedImage bossMapOpen() {
    if (bossMapOpen == null) {
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.CARD_MAP_OPEN);
      bossMapOpen = sprite != null ? sprite.getImage() : null;
    }
    return bossMapOpen;
  }

  public static BufferedImage bossMapClosed() {
    if (bossMapClosed == null) {
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.CARD_CLOSED);
      bossMapClosed = sprite != null ? sprite.getImage() : null;
    }
    return bossMapClosed;
  }

  public static BufferedImage bossMapIcon(String path) {
    if (path != null && path.contains("boss_duke_map")) {
      if (bossDukeMapIcon == null) {
        Sprite sprite = Sprite.loadOptional(path);
        bossDukeMapIcon = sprite != null ? sprite.getImage() : null;
      }
      return bossDukeMapIcon;
    }
    Sprite sprite = Sprite.loadOptional(path);
    return sprite != null ? sprite.getImage() : null;
  }

  public static BufferedImage bossWakeForest() {
    if (bossWakeForest == null) {
      Sprite sprite = Sprite.loadOptional(Chapter1AssetPaths.BOSS_WAKE_FOREST);
      bossWakeForest = sprite != null ? sprite.getImage() : null;
    }
    return bossWakeForest;
  }

  public static BufferedImage bossPortrait(String path) {
    if (path != null && path.contains("boss_duke_portrait")) {
      if (bossDukePortrait == null) {
        Sprite sprite = Sprite.loadOptional(path);
        bossDukePortrait = sprite != null ? sprite.getImage() : null;
      }
      return bossDukePortrait;
    }
    Sprite sprite = Sprite.loadOptional(path);
    return sprite != null ? sprite.getImage() : null;
  }
}
