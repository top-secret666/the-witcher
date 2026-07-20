package main.java.com.witcher.ui.chapter1.view;

import main.java.com.witcher.chapter1.vn.VnChoice;

import java.util.ArrayList;
import java.util.List;

/** Раскладка кнопок выбора VN — блок по центру экрана. */
public final class VnChoiceLayout {

  private static final int ROW_H = 22;
  private static final int ROW_GAP = 6;
  private static final float BLOCK_WIDTH_RATIO = 0.88f;

  public record ChoiceRect(int index, float x, float y, float width, float height) {
    public boolean contains(float px, float py) {
      return px >= x && px < x + width && py >= y && py < y + height;
    }
  }

  private VnChoiceLayout() {
  }

  public static List<ChoiceRect> layout(int sw, int sh, List<VnChoice> choices) {
    List<ChoiceRect> rects = new ArrayList<>();
    if (choices == null || choices.isEmpty()) {
      return rects;
    }
    float w = sw * BLOCK_WIDTH_RATIO;
    float x0 = (sw - w) / 2f;
    int totalH = choices.size() * ROW_H + Math.max(0, choices.size() - 1) * ROW_GAP;
    float y0 = (sh - totalH) / 2f;
    for (int i = 0; i < choices.size(); i++) {
      float y = y0 + i * (ROW_H + ROW_GAP);
      rects.add(new ChoiceRect(i, x0, y, w, ROW_H));
    }
    return rects;
  }

  public static int hitIndex(List<ChoiceRect> rects, float mx, float my) {
    for (ChoiceRect rect : rects) {
      if (rect.contains(mx, my)) {
        return rect.index();
      }
    }
    return -1;
  }
}
