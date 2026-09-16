package com.witcher.ui.shop.swing;

/**
 * Мигание подсказки: чередование обычного спрайта и hover (без отдельной рамки).
 */
public final class ShopAttentionPulse {

  /** Тиков на половину цикла (~0.4 с при 30 fps). */
  private static final int HALF_CYCLE = 12;

  private ShopAttentionPulse() {
  }

  /** true → показывать hover-вариант иконки. */
  public static boolean hoverPhase(int tick) {
    return ((Math.floorDiv(Math.max(0, tick), HALF_CYCLE)) % 2) == 0;
  }
}
