package main.java.com.witcher.ui.chapter1.swing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Path2D;

/**
 * Процедурные веки от первого лица: квадратичные кривые Безье поверх катсцены.
 * openT=0 — закрыто, openT=1 — полностью открыто.
 */
public final class EyelidOverlay {

  private static final Color LID = new Color(6, 4, 3);
  private static final Color LID_EDGE = new Color(20, 12, 9);

  private EyelidOverlay() {
  }

  public static void render(Graphics2D g, int sw, int sh, float openT) {
    if (g == null || sw <= 0 || sh <= 0 || openT >= 0.995f) {
      return;
    }
    float closed = Math.max(0f, Math.min(1f, 1f - openT));
    double centerY = sh * 0.5;
    double curveY = centerY * closed;
    double curveDepth = Math.min(sw, sh) * 0.09 * closed;

    Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

    fillUpperLid(g, sw, curveY, curveDepth);
    fillLowerLid(g, sw, sh, curveY, curveDepth);
    strokeLidEdges(g, sw, sh, curveY, curveDepth);

    if (prevAa != null) {
      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
    }
  }

  private static void fillUpperLid(Graphics2D g, int sw, double curveY, double curveDepth) {
    if (curveY < 0.5) {
      return;
    }
    Path2D path = new Path2D.Double();
    path.moveTo(0, 0);
    path.lineTo(sw, 0);
    path.lineTo(sw, curveY);
    path.quadTo(sw * 0.5, curveY - curveDepth, 0, curveY);
    path.closePath();
    g.setColor(LID);
    g.fill(path);
  }

  private static void fillLowerLid(Graphics2D g, int sw, int sh, double curveY, double curveDepth) {
    if (curveY < 0.5) {
      return;
    }
    double bottomY = sh - curveY;
    Path2D path = new Path2D.Double();
    path.moveTo(0, sh);
    path.lineTo(sw, sh);
    path.lineTo(sw, bottomY);
    path.quadTo(sw * 0.5, bottomY + curveDepth, 0, bottomY);
    path.closePath();
    g.setColor(LID);
    g.fill(path);
  }

  /** Тонкая линия по краю века — читается на ретро-фильтре. */
  private static void strokeLidEdges(Graphics2D g, int sw, int sh, double curveY, double curveDepth) {
    if (curveY < 2) {
      return;
    }
    g.setColor(LID_EDGE);
    Path2D upperEdge = new Path2D.Double();
    upperEdge.moveTo(0, curveY);
    upperEdge.quadTo(sw * 0.5, curveY - curveDepth, sw, curveY);
    g.draw(upperEdge);

    double bottomY = sh - curveY;
    Path2D lowerEdge = new Path2D.Double();
    lowerEdge.moveTo(0, bottomY);
    lowerEdge.quadTo(sw * 0.5, bottomY + curveDepth, sw, bottomY);
    g.draw(lowerEdge);
  }
}
