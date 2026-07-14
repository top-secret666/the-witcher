package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Процедурные веки от первого лица.
 * openT=0 — полностью закрыто (сплошной чёрный), openT=1 — открыто.
 * Кривые смыкаются к центру, без щели при закрытии.
 */
public final class EyelidOverlay {

  private static final Color LID = new Color(6, 4, 3);
  private static final Color LID_EDGE = new Color(20, 12, 9);

  private EyelidOverlay() {
  }

  /** Закруглённые чёрные веки без контура. */
  public static void renderBlack(Graphics2D g, int sw, int sh, float openT) {
    renderLids(g, sw, sh, openT, Color.BLACK, false);
  }

  public static void render(Graphics2D g, int sw, int sh, float openT) {
    renderLids(g, sw, sh, openT, LID, true);
  }

  private static void renderLids(
      Graphics2D g, int sw, int sh, float openT, Color fill, boolean strokeEdges) {
    if (g == null || sw <= 0 || sh <= 0 || openT >= 0.995f) {
      return;
    }
    // Полностью закрыто — сплошной чёрный, без миндальной щели.
    if (openT <= 0.04f) {
      g.setColor(fill);
      g.fillRect(0, 0, sw, sh);
      return;
    }

    float closed = Math.max(0f, Math.min(1f, 1f - openT));
    double centerY = sh * 0.5;
    double curveY = centerY * closed;
    // Изгиб ВСТРЕЧНЫЙ (к центру) — при закрытии веки перекрываются, щели нет.
    double curveDepth = Math.min(sw, sh) * 0.08 * closed;

    Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

    fillUpperLid(g, sw, curveY, curveDepth, fill);
    fillLowerLid(g, sw, sh, curveY, curveDepth, fill);
    if (strokeEdges) {
      strokeLidEdges(g, sw, sh, curveY, curveDepth);
    }

    if (prevAa != null) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
    }
  }

  private static void fillUpperLid(
      Graphics2D g, int sw, double curveY, double curveDepth, Color fill) {
    if (curveY < 0.5) {
      return;
    }
    Path2D path = new Path2D.Double();
    path.moveTo(0, 0);
    path.lineTo(sw, 0);
    path.lineTo(sw, curveY);
    // Центр дуги ниже — верхнее веко закрывает середину.
    path.quadTo(sw * 0.5, curveY + curveDepth, 0, curveY);
    path.closePath();
    g.setColor(fill);
    g.fill(path);
  }

  private static void fillLowerLid(
      Graphics2D g, int sw, int sh, double curveY, double curveDepth, Color fill) {
    if (curveY < 0.5) {
      return;
    }
    double bottomY = sh - curveY;
    Path2D path = new Path2D.Double();
    path.moveTo(0, sh);
    path.lineTo(sw, sh);
    path.lineTo(sw, bottomY);
    // Центр дуги выше — нижнее веко закрывает середину.
    path.quadTo(sw * 0.5, bottomY - curveDepth, 0, bottomY);
    path.closePath();
    g.setColor(fill);
    g.fill(path);
  }

  private static void strokeLidEdges(Graphics2D g, int sw, int sh, double curveY, double curveDepth) {
    if (curveY < 2) {
      return;
    }
    g.setColor(LID_EDGE);
    Path2D upperEdge = new Path2D.Double();
    upperEdge.moveTo(0, curveY);
    upperEdge.quadTo(sw * 0.5, curveY + curveDepth, sw, curveY);
    g.draw(upperEdge);

    double bottomY = sh - curveY;
    Path2D lowerEdge = new Path2D.Double();
    lowerEdge.moveTo(0, bottomY);
    lowerEdge.quadTo(sw * 0.5, bottomY - curveDepth, sw, bottomY);
    g.draw(lowerEdge);
  }
}
