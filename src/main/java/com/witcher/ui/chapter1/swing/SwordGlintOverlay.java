package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Катсцена боя: 3 дискретных столкновения клинков на чёрном фоне.
 * Клинок — сужающийся силуэт, вспышка в точке контакта, искры из удара.
 */
public final class SwordGlintOverlay {

  private static final class Spark {
    float x;
    float y;
    float vx;
    float vy;
    long startTime;
    int duration;
  }

  private static final class ClashMoment {
    final long time;
    final float x;
    final float y;
    final float angleA;
    final float angleB;

    ClashMoment(long time, float x, float y, float angleA, float angleB) {
      this.time = time;
      this.x = x;
      this.y = y;
      this.angleA = angleA;
      this.angleB = angleB;
    }
  }

  private final List<ClashMoment> clashes = new ArrayList<>();
  private final List<Spark> sparks = new ArrayList<>();
  private final boolean[] sparkSpawned = new boolean[3];
  private final Random rnd = new Random(41);

  private long elapsedMs;
  private long renderMs;
  private boolean freezeFrame;
  private long frozenAtMs = -1;
  private int lastShakeClash = -1;
  private int layoutW;
  private int layoutH;

  public void reset() {
    clashes.clear();
    sparks.clear();
    for (int i = 0; i < sparkSpawned.length; i++) {
      sparkSpawned[i] = false;
    }
    elapsedMs = 0;
    renderMs = 0;
    freezeFrame = false;
    frozenAtMs = -1;
    lastShakeClash = -1;
    layoutW = 0;
    layoutH = 0;
  }

  public void update(long deltaMs, int width, int height) {
    ensureLayout(width, height);
    elapsedMs += deltaMs;
    renderMs = freezeFrame && frozenAtMs >= 0 ? frozenAtMs : elapsedMs;

    sparks.removeIf(s -> renderMs - s.startTime > s.duration);
    for (int i = 0; i < clashes.size(); i++) {
      long dt = renderMs - clashes.get(i).time;
      if (!sparkSpawned[i] && dt >= -10 && dt <= 10) {
        spawnSparks(clashes.get(i).x, clashes.get(i).y, renderMs);
        sparkSpawned[i] = true;
        lastShakeClash = i;
      }
    }
  }

  /** Заморозить кадр на финальном ударе (победа + глитч). */
  public void freezeFinalGlint() {
    if (freezeFrame) {
      return;
    }
    freezeFrame = true;
    frozenAtMs = elapsedMs;
    renderMs = frozenAtMs;
  }

  public long elapsedMs() {
    return elapsedMs;
  }

  public void render(Graphics2D g, int width, int height) {
    ensureLayout(width, height);
    Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, width, height);

    for (int i = 0; i < clashes.size(); i++) {
      ClashMoment c = clashes.get(i);
      long dt = renderMs - c.time;
      if (dt < -150 || dt > 350) {
        continue;
      }

      float approach = clamp((150f + dt) / 150f, 0f, 1f);
      drawBlade(g, c.x, c.y, c.angleA, approach);
      drawBlade(g, c.x, c.y, c.angleB, approach);

      if (dt >= -30 && dt <= 200) {
        float flashT = clamp(dt / 200f, 0f, 1f);
        float flashAlpha = flashT < 0.15f ? flashT / 0.15f : 1f - (flashT - 0.15f) / 0.85f;
        float radius = 15f + flashT * 70f;

        RadialGradientPaint glow = new RadialGradientPaint(
            new Point2D.Float(c.x, c.y),
            radius,
            new float[] {0f, 1f},
            new Color[] {
                new Color(1f, 1f, 0.92f, Math.max(0f, flashAlpha)),
                new Color(1f, 1f, 0.92f, 0f)
            });
        g.setPaint(glow);
        g.fillOval(
            (int) (c.x - radius),
            (int) (c.y - radius),
            (int) (radius * 2f),
            (int) (radius * 2f));
      }
    }

    renderSparks(g);

    if (prevAa != null) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
    }
  }

  public int getShakeOffsetX() {
    return shakeOffset(true);
  }

  public int getShakeOffsetY() {
    return shakeOffset(false);
  }

  private int shakeOffset(boolean xAxis) {
    if (lastShakeClash < 0 || lastShakeClash >= clashes.size()) {
      return 0;
    }
    long dt = renderMs - clashes.get(lastShakeClash).time;
    if (dt < 0 || dt > 90) {
      return 0;
    }
    int amp = xAxis ? 4 : 3;
    return rnd.nextInt(amp * 2 + 1) - amp;
  }

  private void ensureLayout(int width, int height) {
    if (width <= 0 || height <= 0) {
      return;
    }
    if (layoutW == width && layoutH == height && !clashes.isEmpty()) {
      return;
    }
    layoutW = width;
    layoutH = height;
    clashes.clear();
    for (int i = 0; i < sparkSpawned.length; i++) {
      sparkSpawned[i] = false;
    }
    sparks.clear();
    clashes.add(new ClashMoment(600, width * 0.4f, height * 0.5f, -0.6f, 0.6f));
    clashes.add(new ClashMoment(1400, width * 0.6f, height * 0.45f, 0.3f, -0.9f));
    clashes.add(new ClashMoment(2200, width * 0.5f, height * 0.55f, -0.2f, 0.8f));
  }

  private void drawBlade(Graphics2D g, float cx, float cy, float angle, float approach) {
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

  private void spawnSparks(float x, float y, long now) {
    for (int i = 0; i < 12; i++) {
      Spark s = new Spark();
      s.x = x;
      s.y = y;
      float angle = rnd.nextFloat() * (float) (Math.PI * 2);
      float speed = 1.5f + rnd.nextFloat() * 2.5f;
      s.vx = (float) Math.cos(angle) * speed;
      s.vy = (float) Math.sin(angle) * speed;
      s.startTime = now;
      s.duration = 200 + rnd.nextInt(150);
      sparks.add(s);
    }
  }

  private void renderSparks(Graphics2D g) {
    for (Spark s : sparks) {
      float t = (renderMs - s.startTime) / (float) s.duration;
      if (t < 0f || t > 1f) {
        continue;
      }
      float px = s.x + s.vx * t * 25f;
      float py = s.y + s.vy * t * 25f + t * t * 15f;
      float alpha = 1f - t;

      g.setColor(new Color(1f, 0.9f, 0.5f, Math.max(0f, alpha)));
      g.fillRect((int) px, (int) py, 2, 2);
    }
  }

  private static float clamp(float v, float min, float max) {
    return Math.max(min, Math.min(max, v));
  }
}
