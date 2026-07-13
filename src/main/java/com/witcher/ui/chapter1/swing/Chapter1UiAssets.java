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
}
