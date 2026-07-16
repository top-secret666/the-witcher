package main.java.com.witcher.ui.chapter1.swing;

import main.java.com.witcher.chapter1.loop.ForestWalkTimeline;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Процедурный переход «сквозь чащу»: Ken Burns по туманному лесу, ветки, пыль, виньетка.
 * Старый путь с веками ({@link EyesBlinkEffect}) не трогаем — он для других осколков.
 */
public final class ForestWalkScene {

  private static final long KEN_BURNS_MS = ForestWalkTimeline.TOTAL_MS;
  private static final int BRANCH_SEED = 42;

  private static final class Branch {
    float edgeX;
    float edgeY;
    float angle;
    float length;
    float thickness;
    float curveOffset;
    long partDelay;
    float partDirX;
    float partDirY;
  }

  private final List<Branch> farBranches = new ArrayList<>();
  private final List<Branch> nearBranches = new ArrayList<>();
  private int branchesForW;
  private int branchesForH;

  public void ensureBranches(int w, int h) {
    if (w == branchesForW && h == branchesForH && !farBranches.isEmpty()) {
      return;
    }
    farBranches.clear();
    nearBranches.clear();
    generateLayer(farBranches, w, h, 4, 0.55f, 6f, 11f, new Random(BRANCH_SEED));
    generateLayer(nearBranches, w, h, 7, 0.85f, 10f, 20f, new Random(BRANCH_SEED + 17));
    branchesForW = w;
    branchesForH = h;
  }

  public void render(Graphics2D g, int w, int h, int elapsedMs) {
    ensureBranches(w, h);
    BufferedImage photo = Chapter1UiAssets.forestWalkMist();

    g.setColor(Color.BLACK);
    g.fillRect(0, 0, w, h);

    float bobX = (float) Math.sin(elapsedMs / 260.0) * 4f;
    float bobY = (float) Math.abs(Math.sin(elapsedMs / 180.0)) * 5f;

    drawKenBurns(g, photo, elapsedMs, w, h, bobX, -bobY);
    drawFogDrift(g, elapsedMs, w, h);
    drawDustMotes(g, elapsedMs, w, h);

    long partStart = ForestWalkTimeline.partStartMs();
    drawBranchLayer(g, farBranches, elapsedMs, partStart, bobX * 0.6f, -bobY * 0.6f, w, h);
    drawBranchLayer(g, nearBranches, elapsedMs, partStart, bobX * 1.4f, -bobY * 1.4f, w, h);

    drawVignette(g, w, h);
  }

  private void generateLayer(
      List<Branch> out,
      int w,
      int h,
      int count,
      float lengthScale,
      float thickMin,
      float thickMax,
      Random rnd) {
    for (int i = 0; i < count; i++) {
      Branch b = new Branch();
      int edge = rnd.nextInt(4);
      switch (edge) {
        case 0 -> {
          b.edgeX = rnd.nextFloat() * w;
          b.edgeY = -24f;
        }
        case 1 -> {
          b.edgeX = -24f;
          b.edgeY = rnd.nextFloat() * h;
        }
        case 2 -> {
          b.edgeX = w + 24f;
          b.edgeY = rnd.nextFloat() * h;
        }
        default -> {
          b.edgeX = rnd.nextFloat() * w;
          b.edgeY = h + 24f;
        }
      }
      float toCenterX = w / 2f - b.edgeX;
      float toCenterY = h / 2f - b.edgeY;
      b.angle = (float) Math.atan2(toCenterY, toCenterX);
      b.length = w * (0.28f + rnd.nextFloat() * 0.32f) * lengthScale;
      b.thickness = thickMin + rnd.nextFloat() * (thickMax - thickMin);
      b.curveOffset = (rnd.nextFloat() - 0.5f) * 72f;
      b.partDelay = (long) (rnd.nextFloat() * 520);
      b.partDirX = (float) Math.cos(b.angle) * -1.7f;
      b.partDirY = (float) Math.sin(b.angle) * -1.7f;
      out.add(b);
    }
  }

  private void drawKenBurns(
      Graphics2D g,
      BufferedImage image,
      int elapsedMs,
      int viewW,
      int viewH,
      float bobX,
      float bobY) {
    if (image == null) {
      g.setColor(new Color(28, 32, 38));
      g.fillRect(0, 0, viewW, viewH);
      return;
    }

    float t = clamp(elapsedMs / (float) KEN_BURNS_MS, 0f, 1f);
    float eased = ForestWalkTimeline.easeOutQuad(t);
    float scale = lerp(1.0f, 1.28f, eased);
    float focusX = lerp(0.5f, 0.44f, eased) * image.getWidth();
    float focusY = lerp(0.55f, 0.48f, eased) * image.getHeight();

    AffineTransform tx = new AffineTransform();
    tx.translate(viewW / 2.0 + bobX, viewH / 2.0 + bobY);
    tx.scale(scale, scale);
    tx.translate(-focusX, -focusY);

    Object oldInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    g.drawImage(image, tx, null);
    if (oldInterp != null) {
      g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
    }
  }

  private void drawFogDrift(Graphics2D g, int elapsedMs, int w, int h) {
    float drift = (elapsedMs * 0.018f) % (w + 80f);
    g.setColor(new Color(0.75f, 0.78f, 0.82f, 0.12f));
    g.fillRect((int) drift - 40, 0, w / 2 + 40, h);
    g.setColor(new Color(0.7f, 0.74f, 0.8f, 0.08f));
    g.fillRect((int) (drift * 0.6f) - 20, h / 4, w / 3, h / 2);
  }

  private void drawDustMotes(Graphics2D g, long elapsedMs, int w, int h) {
    for (int i = 0; i < 22; i++) {
      float seed = i * 137.5f;
      float x = (seed * 3.1f + elapsedMs * 0.01f) % w;
      float y = (seed * 2.3f - elapsedMs * 0.015f) % h;
      if (y < 0) {
        y += h;
      }
      float flicker = 0.3f + 0.3f * (float) Math.sin(elapsedMs / 400.0 + seed);
      g.setColor(new Color(0.8f, 0.85f, 0.9f, flicker * 0.4f));
      g.fillOval((int) x, (int) y, 2, 2);
    }
  }

  private void drawBranchLayer(
      Graphics2D g,
      List<Branch> branches,
      int elapsedMs,
      long partStart,
      float layerBobX,
      float layerBobY,
      int w,
      int h) {
    g.translate(layerBobX, layerBobY);
    try {
      for (Branch b : branches) {
        long localPartStart = partStart + b.partDelay;
        float partT = clamp((elapsedMs - localPartStart) / (float) ForestWalkTimeline.PART_MS, 0f, 1f);
        float partEase = ForestWalkTimeline.easeInBack(partT);

        float shiftX = b.partDirX * partEase * w;
        float shiftY = b.partDirY * partEase * h;
        float alpha = 1f - partEase;
        if (alpha <= 0.01f) {
          continue;
        }
        drawBranch(g, b, shiftX, shiftY, alpha);
      }
    } finally {
      g.translate(-layerBobX, -layerBobY);
    }
  }

  private void drawBranch(Graphics2D g, Branch b, float shiftX, float shiftY, float alpha) {
    float midX = b.edgeX + (float) Math.cos(b.angle) * b.length * 0.5f + b.curveOffset;
    float midY = b.edgeY + (float) Math.sin(b.angle) * b.length * 0.5f;
    float tipX = b.edgeX + (float) Math.cos(b.angle) * b.length;
    float tipY = b.edgeY + (float) Math.sin(b.angle) * b.length;

    Path2D branch = new Path2D.Float();
    branch.moveTo(b.edgeX + shiftX, b.edgeY + shiftY);
    branch.quadTo(midX + shiftX, midY + shiftY, tipX + shiftX, tipY + shiftY);

    g.setStroke(new BasicStroke(b.thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
    g.setColor(new Color(0.05f, 0.05f, 0.06f, alpha));
    g.draw(branch);
  }

  private void drawVignette(Graphics2D g, int w, int h) {
    RadialGradientPaint vignette = new RadialGradientPaint(
        new Point2D.Float(w / 2f, h / 2f),
        w * 0.72f,
        new float[] {0f, 0.55f, 1f},
        new Color[] {
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 0),
            new Color(0, 0, 0, 175)
        });
    g.setPaint(vignette);
    g.fillRect(0, 0, w, h);
  }

  private static float lerp(float a, float b, float t) {
    return a + (b - a) * t;
  }

  private static float clamp(float v, float min, float max) {
    return Math.max(min, Math.min(max, v));
  }
}
