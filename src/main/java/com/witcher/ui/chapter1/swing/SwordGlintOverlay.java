package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.SwordClashTimeline;

import java.awt.Graphics2D;

/**
 * Катсцена боя: тряска/тайминг из {@link SwordClashTimeline},
 * картинка — спрайты проблесков на чёрном фоне (fallback — procedural blades).
 */
public final class SwordGlintOverlay {

  private final SwordClashTimeline timeline = new SwordClashTimeline();

  public void reset() {
    timeline.reset();
  }

  public void update(long deltaMs, int width, int height) {
    timeline.update(deltaMs, width, height);
  }

  public void freezeFinalGlint() {
    timeline.freezeFinalGlint();
  }

  public long elapsedMs() {
    return timeline.elapsedMs();
  }

  public void render(Graphics2D g, int width, int height) {
    if (Chapter1UiAssets.swordSlashSheetsReady()) {
      SwordSlashSheetRenderer.paint(g, width, height, timeline.renderMs());
      return;
    }
    // Fallback, если листы не нашлись в resources.
    SwordGlintRenderer.paintProceduralFallback(g, width, height, timeline);
  }

  public int getShakeOffsetX() {
    return timeline.getShakeOffsetX();
  }

  public int getShakeOffsetY() {
    return timeline.getShakeOffsetY();
  }
}
