package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.BossCatalog;
import main.java.com.witcher.chapter1.cutscene.CutsceneId;
import main.java.com.witcher.ui.chapter1.view.BossMapLayout;
import main.java.com.witcher.ui.chapter1.view.Chapter1ViewConstants;

import java.awt.image.BufferedImage;

/** Фоновый прогрев тяжёлых ассетов — не блокирует клик по карте в сумке. */
public final class Chapter1AssetPrewarm {

  private static final int PORTRAIT_SIZE = 56;

  private static volatile boolean cutscenesWarming;
  private static volatile boolean cutscenesReady;
  private static volatile boolean mapDrawablesReady;

  private Chapter1AssetPrewarm() {
  }

  /** Быстрый синхронный прогрев карты (PNG + даунскейл). */
  public static void warmBossMapDrawables() {
    if (mapDrawablesReady) {
      return;
    }
    int sw = Chapter1ViewConstants.VIRTUAL_W;
    int sh = Chapter1ViewConstants.VIRTUAL_H;
    BufferedImage map = Chapter1UiAssets.bossMapOpen();
    if (map == null) {
      map = Chapter1UiAssets.bossMapClosed();
    }
    ScaledImageCache.get(map, sw, sh);
    int icon = BossMapLayout.BOSS_ICON;
    for (var boss : BossCatalog.all()) {
      ScaledImageCache.get(Chapter1UiAssets.bossMapIcon(boss.mapIconPath()), icon, icon);
      ScaledImageCache.get(Chapter1UiAssets.bossPortrait(boss.portraitPath()), PORTRAIT_SIZE, PORTRAIT_SIZE);
    }
    mapDrawablesReady = true;
  }

  /** Декод GIF и предмасштабирование — в фоне, один раз. */
  public static void warmCutscenesAsync() {
    if (cutscenesReady || cutscenesWarming) {
      return;
    }
    cutscenesWarming = true;
    int sw = Chapter1ViewConstants.VIRTUAL_W;
    int sh = Chapter1ViewConstants.VIRTUAL_H;
    Thread worker = new Thread(() -> {
      try {
        CutsceneCache.warm(CutsceneId.LOOP_WAKE, CutsceneId.ILLUSION_WRONG);
        CutsceneCache.prewarmScaled(CutsceneId.LOOP_WAKE, sw, sh);
        CutsceneCache.prewarmScaled(CutsceneId.ILLUSION_WRONG, sw, sh);
        cutscenesReady = true;
      } finally {
        cutscenesWarming = false;
      }
    }, "ch1-cutscene-prewarm");
    worker.setDaemon(true);
    worker.start();
  }

  public static void warmAllAsync() {
    warmBossMapDrawables();
    warmCutscenesAsync();
  }
}
