package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.intro.view.IntroLayout;

/**
 * Общая геометрия диалогового окна VN — Swing и GDX используют разные профили шрифта.
 */
public final class DialogBoxLayout {

  public record Metrics(
      int boxX,
      int boxY,
      int boxW,
      int boxH,
      int pad,
      int textX,
      int textY,
      int textMaxW,
      int fontSize,
      int toolbarReserve) {

    public int toolbarRowY(int buttonHeight) {
      return boxY + boxH - toolbarReserve + Math.max(0, (toolbarReserve - buttonHeight) / 2);
    }
  }

  /** Параметры размера шрифта/паддингов для конкретного рендерера. */
  public record FontProfile(
      int compactMinSize,
      int normalMinSize,
      float sizeRatio,
      float compactPadRatio,
      float normalPadRatio,
      int compactPadMin,
      boolean textTopInset,
      boolean toolbarStrip) {
  }

  public static final FontProfile SWING = new FontProfile(
      13, 11, 0.034f, 0.018f, 0.02f, 6, true, true);

  public static final FontProfile GDX = new FontProfile(
      16, 16, 0.045f, 0.018f, 0.02f, 8, false, false);

  private DialogBoxLayout() {
  }

  public static Metrics compute(int sw, int sh, float heightRatio, float widthRatio, FontProfile profile) {
    int boxH = Math.max((int) IntroLayout.DIALOG_MIN_HEIGHT, Math.round(sh * heightRatio));
    int boxW = Math.max((int) IntroLayout.DIALOG_MIN_WIDTH, Math.round(sw * widthRatio));
    int boxX = (sw - boxW) / 2;
    int boxY = sh - boxH - Math.round(sh * IntroLayout.DIALOG_BOTTOM_MARGIN_RATIO);
    boolean compact = heightRatio <= 0.11f;
    int toolbarReserve = profile.toolbarStrip() && !compact
        ? Math.max(24, Math.round(sh * 0.058f))
        : 0;
    int fontSize;
    int pad;
    if (compact) {
      fontSize = Math.max(profile.compactMinSize(), Math.round(sh * 0.040f));
      pad = Math.max(profile.compactPadMin(), Math.round(sw * profile.compactPadRatio()));
    } else {
      fontSize = Math.max(profile.normalMinSize(), Math.round(sh * profile.sizeRatio()));
      pad = Math.round(sw * profile.normalPadRatio());
    }
    int textX = boxX + pad;
    int textY = boxY + pad + (profile.textTopInset() && !compact ? Math.round(sh * 0.008f) : 0);
    int textMaxW = boxW - pad * 2;
    return new Metrics(boxX, boxY, boxW, boxH, pad, textX, textY, textMaxW, fontSize, toolbarReserve);
  }
}
