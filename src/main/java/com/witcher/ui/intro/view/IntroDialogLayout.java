package main.java.com.witcher.ui.intro.view;

import main.java.com.witcher.ui.graphics.DialogBoxLayout;

/**
 * Геометрия диалогового окна интро — без Graphics2D.
 */
public final class IntroDialogLayout {

  public static final class Layout {
    public final int boxX;
    public final int boxY;
    public final int boxW;
    public final int boxH;
    public final int pad;
    public final int textX;
    public final int textY;
    public final int textMaxW;
    public final int fontSize;

    Layout(int sw, int sh, float heightRatio, float widthRatio) {
      DialogBoxLayout.Metrics m = DialogBoxLayout.compute(sw, sh, heightRatio, widthRatio, DialogBoxLayout.GDX);
      boxX = m.boxX();
      boxY = m.boxY();
      boxW = m.boxW();
      boxH = m.boxH();
      pad = m.pad();
      textX = m.textX();
      textY = m.textY();
      textMaxW = m.textMaxW();
      fontSize = m.fontSize();
    }
  }

  private IntroDialogLayout() {
  }

  public static Layout computeLayout(int sw, int sh) {
    return new Layout(sw, sh, 0.30f, 1.0f);
  }

  public static Layout computeLayout(int sw, int sh, float heightRatio, float widthRatio) {
    return new Layout(sw, sh, heightRatio, widthRatio);
  }
}
