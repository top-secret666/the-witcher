package main.java.com.witcher.ui.chapter1.swing.battle.briefing;

import main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;

import java.awt.image.BufferedImage;

/** Чистая геометрия пергамента заказа (без Graphics2D). */
public final class QuestNoticeLayout {

  public record Metrics(int x, int y, int w, int h, int textX, int textY, int textMaxW, int textMaxH) {
    public int centerX() {
      return x + w / 2;
    }

    public QuestNoticeRenderer.Layout toRendererLayout() {
      return new QuestNoticeRenderer.Layout(x, y, w, h, textX, textY, textMaxW, textMaxH);
    }
  }

  private QuestNoticeLayout() {
  }

  public static Metrics compute(int sw, int sh) {
    BufferedImage src = Chapter1UiAssets.bossQuestNotice();
    float aspect = src != null && src.getHeight() > 0
        ? src.getWidth() / (float) src.getHeight()
        : 1.5f;

    DialogBoxRenderer.Layout dialog = DialogBoxRenderer.computeLayout(sw, sh);
    int reserveBottom = sh - dialog.boxY + Math.round(sh * 0.05f);

    int maxW = Math.round(sw * 0.92f);
    int maxH = sh - reserveBottom - Math.round(sh * 0.03f);
    int preferredH = Math.round(sh * 0.42f);

    int w = maxW;
    int h = Math.round(w / aspect);
    if (h > Math.min(maxH, preferredH)) {
      h = Math.min(maxH, preferredH);
      w = Math.round(h * aspect);
    }

    int x = (sw - w) / 2;
    int y = Math.max(Math.round(sh * 0.05f), (sh - reserveBottom - h) / 2);
    if (y + h > sh - reserveBottom) {
      y = Math.max(Math.round(sh * 0.02f), sh - reserveBottom - h);
    }

    int padLeft = Math.round(w * 0.10f);
    int padRightSeal = Math.round(w * 0.24f);
    int padTop = Math.round(h * 0.13f);
    int padBottom = Math.round(h * 0.14f);
    return new Metrics(
        x, y, w, h,
        x + padLeft,
        y + padTop,
        w - padLeft - padRightSeal,
        h - padTop - padBottom);
  }
}
