package main.java.com.witcher.ui.chapter1.view;

import main.java.com.witcher.chapter1.view.Chapter1Layout;

/**
 * Swing-фасад над {@link Chapter1Layout} — старые импорты UI не ломаются.
 */
public final class Chapter1ViewConstants {

  public static final int VIRTUAL_W = Chapter1Layout.VIRTUAL_W;
  public static final int VIRTUAL_H = Chapter1Layout.VIRTUAL_H;

  public static final int CARD_ICON_X = Chapter1Layout.CARD_ICON_X;
  public static final int CARD_ICON_Y = Chapter1Layout.CARD_ICON_Y;
  public static final int CARD_ICON_SIZE = Chapter1Layout.CARD_ICON_SIZE;
  public static final int CARD_ICON_BAG_OFFSET_X = Chapter1Layout.CARD_ICON_BAG_OFFSET_X;
  public static final int CARD_ICON_BAG_OFFSET_Y = Chapter1Layout.CARD_ICON_BAG_OFFSET_Y;

  private Chapter1ViewConstants() {
  }
}
