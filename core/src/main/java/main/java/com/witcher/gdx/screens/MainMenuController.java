package main.java.com.witcher.gdx.screens;

import com.badlogic.gdx.math.Rectangle;
import main.java.com.witcher.gdx.layout.MenuLayout;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Логика главного меню (без отрисовки) — аналог Swing {@code MainMenuScreen.update}.
 */
public final class MainMenuController {

    public enum Action {
        NONE, START, SETTINGS, EXIT
    }

    private static final String[] LABELS = {"Играть", "Настройки", "Выход"};

    private final Rectangle[] buttons = new Rectangle[3];
    private final List<float[]> embers = new ArrayList<>();
    private final Random rng = new Random();
    private Action pending = Action.NONE;
    private int hovered = -1;
    private int pressed = -1;
    private int pressedTicks;
    private int tick;

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

    public void update(float viewW, float viewH, int mouseX, int mouseY, boolean clicked) {
        tick++;
        updateEmbers(viewW, viewH);

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

    /** Эмберы — порт Swing {@code MainMenuScreen.update} / {@code drawAtmosphere}. */
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
