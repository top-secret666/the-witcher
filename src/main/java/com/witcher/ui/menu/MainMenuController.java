package main.java.com.witcher.ui.menu;

import main.java.com.witcher.ui.menu.view.MenuLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Логика главного меню: hover, клавиатура (W/S, стрелки, Enter), эмберы, действия.
 * Общая для Swing и LibGDX — только отрисовка различается.
 */
public final class MainMenuController {

    public enum Action {
        NONE, START, SETTINGS, EXIT
    }

    public static final class Rect {
        public float x;
        public float y;
        public float width;
        public float height;

        public void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean contains(float px, float py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }

    private static final String[] LABELS = {"Играть", "Настройки", "Выход"};

    private final Rect[] buttons = new Rect[3];
    private final List<float[]> embers = new ArrayList<>();
    private final Random rng = new Random();
    private Action pending = Action.NONE;
    private int selectedIndex = -1;
    private int keyboardFocus = -1;
    private int pressedIndex = -1;
    private int pressedTicks;
    private int tick;

    public MainMenuController() {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new Rect();
        }
    }

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

    /**
     * @param navDir -1 вверх, 0 нет, 1 вниз (W/S, стрелки)
     * @param activate Enter / Space
     */
    public void update(float viewW, float viewH, float mouseX, float mouseY,
                       boolean mouseClicked, int navDir, boolean activate) {
        tick++;
        updateEmbers(viewW, viewH);

        if (pressedTicks > 0) {
            pressedTicks--;
        }

        int hoveredIndex = -1;
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].contains(mouseX, mouseY)) {
                hoveredIndex = i;
                break;
            }
        }
        if (hoveredIndex != -1) {
            selectedIndex = hoveredIndex;
            keyboardFocus = -1;
        } else if (keyboardFocus >= 0) {
            selectedIndex = keyboardFocus;
        } else {
            selectedIndex = -1;
        }

        if (navDir != 0) {
            if (selectedIndex < 0) {
                selectedIndex = 0;
            }
            selectedIndex = (selectedIndex + navDir) % buttons.length;
            if (selectedIndex < 0) {
                selectedIndex += buttons.length;
            }
            keyboardFocus = selectedIndex;
        }

        if (mouseClicked && selectedIndex >= 0 && buttons[selectedIndex].contains(mouseX, mouseY)) {
            pressSelected();
        } else if (activate && selectedIndex >= 0) {
            pressSelected();
        }

        if (pressedTicks == 0) {
            pressedIndex = -1;
        }
    }

    public void requestExit() {
        pending = Action.EXIT;
    }

    private void pressSelected() {
        pressedIndex = selectedIndex;
        pressedTicks = 6;
        pending = switch (selectedIndex) {
            case 0 -> Action.START;
            case 1 -> Action.SETTINGS;
            default -> Action.EXIT;
        };
    }

    private void updateEmbers(float viewW, float viewH) {
        if (tick % 3 == 0 && embers.size() < 40) {
            float x = rng.nextFloat() * viewW;
            float y = viewH + rng.nextFloat() * 20f;
            float vx = (rng.nextFloat() - 0.5f) * 0.3f;
            float vy = -0.4f - rng.nextFloat() * 0.6f;
            float maxAge = 120 + rng.nextInt(180);
            float sz = 1f + rng.nextFloat() * 2f;
            float r = 200 + rng.nextInt(56);
            float g = 80 + rng.nextInt(80);
            float b = 10 + rng.nextInt(30);
            embers.add(new float[] {x, y, vx, vy, 0f, maxAge, sz, r, g, b});
        }
        Iterator<float[]> it = embers.iterator();
        while (it.hasNext()) {
            float[] e = it.next();
            e[0] += e[2] + (float) Math.sin(e[4] * 0.03) * 0.15f;
            e[1] += e[3];
            e[2] *= 0.995f;
            e[4]++;
            if (e[4] >= e[5] || e[1] < -10f) {
                it.remove();
            }
        }
    }

    public List<float[]> embers() {
        return embers;
    }

    public Action consumeAction() {
        Action a = pending;
        pending = Action.NONE;
        return a;
    }

    public int buttonCount() {
        return buttons.length;
    }

    public Rect buttonRect(int index) {
        return buttons[index];
    }

    public String buttonLabel(int index) {
        return LABELS[index];
    }

    /** 0 normal, 1 hover/selected, 2 pressed */
    public int buttonState(int index) {
        if (pressedIndex == index && pressedTicks > 0) {
            return 2;
        }
        if (selectedIndex == index) {
            return 1;
        }
        return 0;
    }
}
