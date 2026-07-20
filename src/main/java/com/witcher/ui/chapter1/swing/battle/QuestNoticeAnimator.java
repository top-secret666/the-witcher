package main.java.com.witcher.ui.chapter1.swing.battle;

/**
 * Появление листа заказа — как открытие категории в лавке: рост, сдвиг и лёгкий flip.
 */
public final class QuestNoticeAnimator {

  public final float progress;
  public final int x;
  public final int y;
  public final int w;
  public final int h;
  public final float paperAlpha;
  public final float textAlpha;
  public final float flipScaleX;

  private QuestNoticeAnimator(float progress, int x, int y, int w, int h,
                              float paperAlpha, float textAlpha, float flipScaleX) {
    this.progress = progress;
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
    this.paperAlpha = paperAlpha;
    this.textAlpha = textAlpha;
    this.flipScaleX = flipScaleX;
  }

  public static QuestNoticeAnimator opening(float t, QuestNoticeRenderer.Layout target) {
    float p = clamp01(t);
    float move = easeInOutCubic(p);

    int fromW = Math.round(target.w() * 0.55f);
    int fromH = Math.round(target.h() * 0.55f);
    int fromX = target.x() + (target.w() - fromW) / 2;
    int fromY = target.y() + target.h() - fromH;

    int x = Math.round(lerp(fromX, target.x(), move));
    int y = Math.round(lerp(fromY, target.y(), move));
    int w = Math.round(lerp(fromW, target.w(), move));
    int h = Math.round(lerp(fromH, target.h(), move));

    float flipScaleX = Math.abs((float) Math.cos(p * Math.PI));
    if (flipScaleX < 0.04f) {
      flipScaleX = 0.04f;
    }

    float paperAlpha = easeOutCubic(Math.min(p * 1.25f, 1f));
    float textAlpha = easeOutCubic(segment(p, 0.42f, 0.92f));

    return new QuestNoticeAnimator(p, x, y, w, h, paperAlpha, textAlpha, flipScaleX);
  }

  public static QuestNoticeAnimator settled(QuestNoticeRenderer.Layout target) {
    return new QuestNoticeAnimator(1f, target.x(), target.y(), target.w(), target.h(),
        1f, 1f, 1f);
  }

  private static float segment(float t, float start, float end) {
    if (t <= start) {
      return 0f;
    }
    if (t >= end) {
      return 1f;
    }
    return (t - start) / (end - start);
  }

  private static float clamp01(float v) {
    return Math.max(0f, Math.min(1f, v));
  }

  private static float lerp(float a, float b, float t) {
    return a + (b - a) * clamp01(t);
  }

  private static float easeOutCubic(float t) {
    float x = clamp01(t);
    return 1f - (float) Math.pow(1f - x, 3);
  }

  private static float easeInOutCubic(float t) {
    float x = clamp01(t);
    if (x < 0.5f) {
      return 4f * x * x * x;
    }
    return 1f - (float) Math.pow(-2f * x + 2f, 3) / 2f;
  }
}
