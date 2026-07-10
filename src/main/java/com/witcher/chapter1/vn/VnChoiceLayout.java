package main.java.com.witcher.chapter1.vn;

import java.util.ArrayList;
import java.util.List;

/** Раскладка кнопок выбора VN (низ экрана). */
public final class VnChoiceLayout {

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
    int rowH = 18;
    int pad = 8;
    int totalH = choices.size() * rowH + pad;
    float y0 = sh - totalH - 12;
    float w = sw - 24;
    for (int i = 0; i < choices.size(); i++) {
      rects.add(new ChoiceRect(i, 12, y0 + i * rowH, w, rowH));
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
