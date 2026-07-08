package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.math.Rectangle;

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

    public void layoutButtons(float viewW, float viewH) {
        float logoBottom = viewH * 0.22f;
        float availableH = viewH - logoBottom - 16f;
        float gap = availableH * 0.04f;
        float slotH = (availableH - gap * (buttons.length - 1)) / buttons.length;
        float plankW = viewW * 0.62f;
        float plankH = plankW / 1.9f;
        if (plankH > slotH) {
            plankH = slotH;
            plankW = plankH * 1.9f;
        }
        float startX = (viewW - plankW) * 0.5f;
        float startY = logoBottom;
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
                    pressedTicks = 8;
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
