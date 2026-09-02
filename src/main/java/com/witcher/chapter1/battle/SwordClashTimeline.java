package main.java.com.witcher.chapter1.battle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Симуляция столкновений мечей (без AWT) — общее для Swing и движка. */
public final class SwordClashTimeline {

  public record ClashMoment(long time, float x, float y, float angleA, float angleB) {
  }

  public record Spark(
      float x, float y, float vx, float vy, long startTime, int duration) {
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

    sparks.removeIf(s -> renderMs - s.startTime() > s.duration());
    for (int i = 0; i < clashes.size(); i++) {
      long dt = renderMs - clashes.get(i).time();
      if (!sparkSpawned[i] && dt >= -10 && dt <= 10) {
        spawnSparks(clashes.get(i).x(), clashes.get(i).y(), renderMs);
        sparkSpawned[i] = true;
        lastShakeClash = i;
      }
    }
  }

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

  public long renderMs() {
    return renderMs;
  }

  public List<ClashMoment> clashes() {
    return Collections.unmodifiableList(clashes);
  }

  public List<Spark> sparks() {
    return Collections.unmodifiableList(sparks);
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
    long dt = renderMs - clashes.get(lastShakeClash).time();
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
    // Согласовано с rush-листом (~80…920 мс) и fallback A/B клипами.
    clashes.add(new ClashMoment(220, width * 0.4f, height * 0.5f, -0.6f, 0.6f));
    clashes.add(new ClashMoment(480, width * 0.6f, height * 0.45f, 0.3f, -0.9f));
    clashes.add(new ClashMoment(760, width * 0.5f, height * 0.55f, -0.2f, 0.8f));
  }

  private void spawnSparks(float x, float y, long now) {
    for (int i = 0; i < 12; i++) {
      float angle = rnd.nextFloat() * (float) (Math.PI * 2);
      float speed = 1.5f + rnd.nextFloat() * 2.5f;
      sparks.add(new Spark(
          x, y,
          (float) Math.cos(angle) * speed,
          (float) Math.sin(angle) * speed,
          now,
          200 + rnd.nextInt(150)));
    }
  }
}
