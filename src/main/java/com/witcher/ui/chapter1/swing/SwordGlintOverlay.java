package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.SwordClashTimeline;
import main.java.com.witcher.chapter1.battle.SwordClashTimeline.ClashMoment;
import main.java.com.witcher.chapter1.battle.SwordClashTimeline.Spark;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/** Swing-обёртка над {@link SwordClashTimeline}: симуляция в домене, paint в renderer. */
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
    List<SwordGlintRenderer.ClashDraw> clashDraws = new ArrayList<>();
    for (ClashMoment c : timeline.clashes()) {
      clashDraws.add(new SwordGlintRenderer.ClashDraw(
          c.time(), c.x(), c.y(), c.angleA(), c.angleB()));
    }
    List<SwordGlintRenderer.SparkDraw> sparkDraws = new ArrayList<>();
    for (Spark s : timeline.sparks()) {
      sparkDraws.add(new SwordGlintRenderer.SparkDraw(
          s.x(), s.y(), s.vx(), s.vy(), s.startTime(), s.duration()));
    }
    SwordGlintRenderer.paint(g, width, height, timeline.renderMs(), clashDraws, sparkDraws);
  }

  public int getShakeOffsetX() {
    return timeline.getShakeOffsetX();
  }

  public int getShakeOffsetY() {
    return timeline.getShakeOffsetY();
  }
}
