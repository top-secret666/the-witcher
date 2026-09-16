package com.witcher.ui.pause;

import java.awt.Rectangle;

/** Пауза: Продолжить / Настройки / В главное меню (+ подтверждение без сохранений). */
public final class PauseMenuController {

  public enum Action {
    NONE, CONTINUE, SETTINGS, MAIN_MENU
  }

  public enum Mode {
    MENU, CONFIRM_MAIN
  }

  private static final String[] MENU_LABELS = {
      "Продолжить",
      "Настройки",
      "В главное меню"
  };

  private static final String[] CONFIRM_LABELS = {
      "Нет",
      "Да"
  };

  private final Rectangle[] menuButtons = new Rectangle[MENU_LABELS.length];
  private final Rectangle[] confirmButtons = new Rectangle[CONFIRM_LABELS.length];
  private Action pending = Action.NONE;
  private Mode mode = Mode.MENU;
  private int selectedIndex;
  private int keyboardFocus = -1;

  public PauseMenuController() {
    for (int i = 0; i < menuButtons.length; i++) {
      menuButtons[i] = new Rectangle();
    }
    for (int i = 0; i < confirmButtons.length; i++) {
      confirmButtons[i] = new Rectangle();
    }
  }

  public Mode mode() {
    return mode;
  }

  public void layout(int sw, int sh) {
    layoutButtons(sw, sh, menuButtons, MENU_LABELS.length, panelHeight(MENU_LABELS.length));
    layoutButtons(sw, sh, confirmButtons, CONFIRM_LABELS.length,
        panelHeight(CONFIRM_LABELS.length) + confirmExtraHeight());
  }

  private void layoutButtons(int sw, int sh, Rectangle[] buttons, int count, int panelH) {
    int panelW = panelWidth();
    int btnW = 168;
    int btnH = 28;
    int gap = 8;
    int titleH = 22;
    int pad = 16;
    int panelX = (sw - panelW) / 2;
    int panelY = (sh - panelH) / 2;
    int btnX = panelX + (panelW - btnW) / 2;
    int btnY = panelY + pad + titleH + 10 + (mode == Mode.CONFIRM_MAIN ? confirmExtraHeight() : 0);
    for (int i = 0; i < count; i++) {
      buttons[i].setBounds(btnX, btnY + i * (btnH + gap), btnW, btnH);
    }
  }

  public Rectangle panelBounds(int sw, int sh) {
    int count = mode == Mode.CONFIRM_MAIN ? CONFIRM_LABELS.length : MENU_LABELS.length;
    int panelH = panelHeight(count);
    if (mode == Mode.CONFIRM_MAIN) {
      panelH += confirmExtraHeight();
    }
    int panelW = panelWidth();
    return new Rectangle((sw - panelW) / 2, (sh - panelH) / 2, panelW, panelH);
  }

  private static int panelWidth() {
    return 260;
  }

  private static int panelHeight(int buttonCount) {
    int btnH = 28;
    int gap = 8;
    int titleH = 22;
    int pad = 16;
    return pad + titleH + 10 + buttonCount * btnH + (buttonCount - 1) * gap + pad;
  }

  private static int confirmExtraHeight() {
    return 44;
  }

  public void reset() {
    pending = Action.NONE;
    mode = Mode.MENU;
    selectedIndex = 0;
    keyboardFocus = 0;
  }

  public void update(int mouseX, int mouseY, boolean clicked, int navDir, boolean activate, boolean esc) {
    if (esc) {
      if (mode == Mode.CONFIRM_MAIN) {
        mode = Mode.MENU;
        selectedIndex = 2;
        keyboardFocus = 2;
        return;
      }
      pending = Action.CONTINUE;
      return;
    }

    Rectangle[] buttons = mode == Mode.CONFIRM_MAIN ? confirmButtons : menuButtons;
    int count = buttons.length;

    int hovered = -1;
    for (int i = 0; i < count; i++) {
      if (buttons[i].contains(mouseX, mouseY)) {
        hovered = i;
        break;
      }
    }
    if (hovered >= 0) {
      selectedIndex = hovered;
      keyboardFocus = -1;
    } else if (keyboardFocus >= 0) {
      selectedIndex = Math.min(keyboardFocus, count - 1);
    }

    if (navDir != 0) {
      selectedIndex = (selectedIndex + navDir) % count;
      if (selectedIndex < 0) {
        selectedIndex += count;
      }
      keyboardFocus = selectedIndex;
    }

    if (clicked && hovered >= 0) {
      activateIndex(hovered);
    } else if (activate && selectedIndex >= 0 && selectedIndex < count) {
      activateIndex(selectedIndex);
    }
  }

  private void activateIndex(int index) {
    if (mode == Mode.CONFIRM_MAIN) {
      if (index == 0) {
        mode = Mode.MENU;
        selectedIndex = 2;
        keyboardFocus = 2;
      } else {
        pending = Action.MAIN_MENU;
      }
      return;
    }
    switch (index) {
      case 0 -> pending = Action.CONTINUE;
      case 1 -> pending = Action.SETTINGS;
      default -> {
        mode = Mode.CONFIRM_MAIN;
        selectedIndex = 0;
        keyboardFocus = 0;
      }
    }
  }

  public Action consumeAction() {
    Action a = pending;
    pending = Action.NONE;
    return a;
  }

  public int buttonCount() {
    return mode == Mode.CONFIRM_MAIN ? CONFIRM_LABELS.length : MENU_LABELS.length;
  }

  public Rectangle button(int index) {
    return mode == Mode.CONFIRM_MAIN ? confirmButtons[index] : menuButtons[index];
  }

  public String label(int index) {
    return mode == Mode.CONFIRM_MAIN ? CONFIRM_LABELS[index] : MENU_LABELS[index];
  }

  public boolean selected(int index) {
    return selectedIndex == index;
  }

  public String title() {
    return mode == Mode.CONFIRM_MAIN ? "Сохранений нет" : "Пауза";
  }

  public String confirmBody() {
    return "Прогресс не сохранится.\nВыйти в меню?";
  }
}
