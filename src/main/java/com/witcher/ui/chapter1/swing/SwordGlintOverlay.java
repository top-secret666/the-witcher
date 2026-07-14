package main.java.com.witcher.ui.chapter1.swing;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Процедурные блики меча на чёрном фоне + тряска в момент удара. */
public final class SwordGlintOverlay {

  private static final class Glint {
    float x;
    float y;
    float angle;
    long startTime;
    long duration;
    float length;
    boolean frozen;
  }

  private final List<Glint> glints = new ArrayList<>();
  private final Random rnd = new Random(41);
  private long nextGlintTime;
  private long lastGlintAt = -1;
  private boolean freezeLast;
  private long elapsedMs;

  public void reset() {
    glints.clear();
    nextGlintTime = 0;
    lastGlintAt = -1;
    freezeLast = false;
    elapsedMs = 0;
  }

  public void update(long deltaMs, int width, int height) {
    elapsedMs += deltaMs;
    if (!freezeLast) {
      glints.removeIf(g -> !g.frozen && elapsedMs - g.startTime > g.duration);
    }

    if (freezeLast || elapsedMs < nextGlintTime) {
      return;
    }

    Glint g = new Glint();
    g.x = rnd.nextFloat() * width;
    g.y = rnd.nextFloat() * height;
    g.angle = rnd.nextFloat() * (float) Math.PI;
    g.length = 40 + rnd.nextFloat() * 80;
    g.startTime = elapsedMs;
    g.duration = 120 + rnd.nextInt(80);
    glints.add(g);
    lastGlintAt = elapsedMs;
    nextGlintTime = elapsedMs + 300 + rnd.nextInt(400);
  }

  /** Заморозить последний блик для финального кадра. */
  public void freezeFinalGlint() {
    if (glints.isEmpty()) {
      return;
    }
    Glint last = glints.get(glints.size() - 1);
    last.frozen = true;
    last.duration = Long.MAX_VALUE / 2;
    freezeLast = true;
  }

  public long elapsedMs() {
    return elapsedMs;
  }

  public void render(Graphics2D g, int width, int height) {
    g.setColor(Color.BLACK);
    g.fillRect(0, 0, width, height);

    for (Glint glint : glints) {
      float t = (elapsedMs - glint.startTime) / (float) glint.duration;
      float alpha = t < 0.3f ? t / 0.3f : 1f - (t - 0.3f) / 0.7f;
      if (glint.frozen) {
        alpha = 1f;
      }
      alpha = Math.max(0f, Math.min(1f, alpha));

      g.setStroke(new BasicStroke(3f));
      g.setColor(new Color(1f, 1f, 1f, alpha));
      float dx = (float) Math.cos(glint.angle) * glint.length / 2f;
      float dy = (float) Math.sin(glint.angle) * glint.length / 2f;
      g.drawLine(
          (int) (glint.x - dx), (int) (glint.y - dy),
          (int) (glint.x + dx), (int) (glint.y + dy));
    }
  }

  public int getShakeOffsetX() {
    if (lastGlintAt < 0 || elapsedMs - lastGlintAt > 100) {
      return 0;
    }
    return rnd.nextInt(5) - 2;
  }

  public int getShakeOffsetY() {
    if (lastGlintAt < 0 || elapsedMs - lastGlintAt > 100) {
      return 0;
    }
    return rnd.nextInt(3) - 1;
  }
}
