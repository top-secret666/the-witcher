package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.battle.SwordClashTimeline;
import main.java.com.witcher.chapter1.battle.SwordClashTimeline.ClashMoment;
import main.java.com.witcher.chapter1.battle.SwordClashTimeline.Spark;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/** Только отрисовка столкновений клинков — без тайминга/симуляции. */
public final class SwordGlintRenderer {

  public record ClashDraw(long time, float x, float y, float angleA, float angleB) {
  }

  public record SparkDraw(float x, float y, float vx, float vy, long startTime, int duration) {
  }

  private SwordGlintRenderer() {
  }

  public static void paintProceduralFallback(
      Graphics2D g, int width, int height, SwordClashTimeline timeline) {
    List<ClashDraw> clashes = new ArrayList<>();
    for (ClashMoment c : timeline.clashes()) {
      clashes.add(new ClashDraw(c.time(), c.x(), c.y(), c.angleA(), c.angleB()));
    }
    List<SparkDraw> sparks = new ArrayList<>();
    for (Spark s : timeline.sparks()) {
      sparks.add(new SparkDraw(s.x(), s.y(), s.vx(), s.vy(), s.startTime(), s.duration()));
    }
    paint(g, width, height, timeline.renderMs(), clashes, sparks);
  }

  public static void paint(
      Graphics2D g,
      int width,
      int height,
      long renderMs,
      List<ClashDraw> clashes,
      List<SparkDraw> sparks) {
    Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, width, height);

    for (ClashDraw c : clashes) {
      long dt = renderMs - c.time();
      if (dt < -150 || dt > 350) {
        continue;
      }

      float approach = clamp((150f + dt) / 150f, 0f, 1f);
      drawBlade(g, c.x(), c.y(), c.angleA(), approach);
      drawBlade(g, c.x(), c.y(), c.angleB(), approach);

      if (dt >= -30 && dt <= 200) {
        float flashT = clamp(dt / 200f, 0f, 1f);
        float flashAlpha = flashT < 0.15f ? flashT / 0.15f : 1f - (flashT - 0.15f) / 0.85f;
        float radius = 15f + flashT * 70f;

        RadialGradientPaint glow = new RadialGradientPaint(
            new Point2D.Float(c.x(), c.y()),
            radius,
            new float[] {0f, 1f},
            new Color[] {
                new Color(1f, 1f, 0.92f, Math.max(0f, flashAlpha)),
                new Color(1f, 1f, 0.92f, 0f)
            });
        g.setPaint(glow);
        g.fillOval(
            (int) (c.x() - radius),
            (int) (c.y() - radius),
            (int) (radius * 2f),
            (int) (radius * 2f));
      }
    }

    paintSparks(g, renderMs, sparks);

    if (prevAa != null) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
    }
  }

  private static void drawBlade(Graphics2D g, float cx, float cy, float angle, float approach) {
    float len = 90f;
    float tipDist = len * (0.4f + approach * 0.6f);
    float dx = (float) Math.cos(angle);
    float dy = (float) Math.sin(angle);

    float tipX = cx + dx * tipDist;
    float tipY = cy + dy * tipDist;
    float baseX = cx + dx * (tipDist - len);
    float baseY = cy + dy * (tipDist - len);

    float perpX = -dy;
    float perpY = dx;
    float baseWidth = 6f;
    float tipWidth = 1f;

    Path2D blade = new Path2D.Float();
    blade.moveTo(baseX + perpX * baseWidth, baseY + perpY * baseWidth);
    blade.lineTo(tipX + perpX * tipWidth, tipY + perpY * tipWidth);
    blade.lineTo(tipX - perpX * tipWidth, tipY - perpY * tipWidth);
    blade.lineTo(baseX - perpX * baseWidth, baseY - perpY * baseWidth);
    blade.closePath();

    g.setColor(new Color(0.85f, 0.9f, 1f, 0.9f * approach));
    g.fill(blade);
  }

  private static void paintSparks(Graphics2D g, long renderMs, List<SparkDraw> sparks) {
    for (SparkDraw s : sparks) {
      float t = (renderMs - s.startTime()) / (float) s.duration();
      if (t < 0f || t > 1f) {
        continue;
      }
      float px = s.x() + s.vx() * t * 25f;
      float py = s.y() + s.vy() * t * 25f + t * t * 15f;
      float alpha = 1f - t;

      g.setColor(new Color(1f, 0.9f, 0.5f, Math.max(0f, alpha)));
      g.fillRect((int) px, (int) py, 2, 2);
    }
  }

  private static float clamp(float v, float min, float max) {
    return Math.max(min, Math.min(max, v));
  }
}
