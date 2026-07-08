package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.math.Rectangle;
import main.java.com.witcher.gdx.layout.MenuLayout;

/**
 * Логика главного меню (без отрисовки) — аналог Swing {@code MainMenuScreen.update}.
 */
public final class MainMenuController {

    public enum Action {
        NONE, START, SETTINGS, EXIT
    }

    private static final String[] LABELS = {"Играть", "Настройки", "Выход"};

    private final Rectangle[] buttons = new Rectangle[3];
    private Action pending = Action.NONE;
    private int hovered = -1;
    private int pressed = -1;
    private int pressedTicks;

    public MainMenuController() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Rectangle();
        }
    }

    /**
     * Раскладка как в Swing {@code layoutButtons} — с реальным aspect таблички.
     */
    public void layoutButtons(float viewW, float viewH, float buttonAspect,
                              float logoSignAspect, float titleLogoAspect,
                              boolean hasLogoSign, boolean hasTitleLogo) {
        float logoY = MenuLayout.logoY(viewH);
        float logoReservedBottom = logoY;
        if (hasLogoSign && logoSignAspect > 0f) {
            float signW = MenuLayout.signW(viewW);
            float signH = signW * logoSignAspect;
            logoReservedBottom = logoY + signH;
        } else if (hasTitleLogo && titleLogoAspect > 0f) {
            float logoW = viewW * MenuLayout.TITLE_LOGO_W_RATIO;
            float logoH = logoW * titleLogoAspect;
            logoReservedBottom = logoY + logoH;
        }
        logoReservedBottom += MenuLayout.LOGO_MARGIN_BOTTOM;

        float availableH = viewH - logoReservedBottom - MenuLayout.CONTENT_MARGIN_BOTTOM;
        float gap = availableH * MenuLayout.BUTTON_GAP_OF_AVAILABLE;
        float slotH = (availableH - gap * (buttons.length - 1)) / buttons.length;

        float plankW = viewW * MenuLayout.BUTTON_W_RATIO;
        float plankH = plankW / buttonAspect;
        if (plankH > slotH) {
            plankH = slotH;
            plankW = plankH * buttonAspect;
        }

        float startX = (viewW - plankW) * 0.5f;
        float startY = logoReservedBottom;

        for (int i = 0; i < buttons.length; i++) {
            float slotY = startY + i * (slotH + gap);
            float plankY = slotY + (slotH - plankH) * 0.5f;
            buttons[i].set(startX, plankY, plankW, plankH);
        }
    }

    public void update(int mouseX, int mouseY, boolean clicked) {
        if (pressedTicks > 0) {
            pressedTicks--;
        }
        hovered = -1;
        for (int i = 0; i < buttons.length; i++) {
            Rectangle r = buttons[i];
            if (mouseX >= r.x && mouseX < r.x + r.width
                && mouseY >= r.y && mouseY < r.y + r.height) {
                hovered = i;
                if (clicked) {
                    pressed = i;
                    pressedTicks = 6;
                    pending = switch (i) {
                        case 0 -> Action.START;
                        case 1 -> Action.SETTINGS;
                        default -> Action.EXIT;
                    };
                }
                break;
            }
        }
        if (!clicked && pressedTicks == 0) {
            pressed = -1;
        }
    }

    public Action consumeAction() {
        Action a = pending;
        pending = Action.NONE;
        return a;
    }

    public int buttonCount() {
        return buttons.length;
    }

    public Rectangle buttonRect(int index) {
        return buttons[index];
    }

    public String buttonLabel(int index) {
        return LABELS[index];
    }

    /** 0 normal, 1 hover, 2 pressed */
    public int buttonState(int index) {
        if (pressed == index && pressedTicks > 0) {
            return 2;
        }
        if (hovered == index) {
            return 1;
        }
        return 0;
    }
}
