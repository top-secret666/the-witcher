package main.java.com.witcher.ui.menu.view;

import main.java.com.witcher.ui.menu.MainMenuController;

/**
 * Позиция подписи на кнопке меню — якоря из {@link MenuLayout} (скрипт swing_to_gdx_layout.py).
 */
public final class MenuTextLayout {

    private MenuTextLayout() {
    }

    public static float fontSize(float buttonHeight) {
        return MenuLayout.buttonFontSize(buttonHeight);
    }

    public static float anchorX(MainMenuController.Rect button, int buttonIndex) {
        return button.x + button.width * MenuLayout.textAnchorX(buttonIndex);
    }

    public static float anchorY(MainMenuController.Rect button, int buttonIndex) {
        return button.y + button.height * MenuLayout.textAnchorY(buttonIndex);
    }
}
