package main.java.com.witcher.ui.intro;

import main.java.com.witcher.ui.graphics.DialogBoxRenderer;

/**
 * Раскладка кнопок VN-интерфейса (Назад, История, Авто).
 */
public final class IntroVnUi {

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

    public static final class ButtonLayout {
        public final Rect backButton;
        public final Rect historyButton;
        public final Rect autoButton;
        public final Rect historyPanel;
        public final Rect historyClose;

        public ButtonLayout() {
            backButton = new Rect();
            historyButton = new Rect();
            autoButton = new Rect();
            historyPanel = new Rect();
            historyClose = new Rect();
        }
    }

    private static final String LABEL_BACK = "Назад";
    private static final String LABEL_HISTORY = "История";
    private static final String LABEL_AUTO = "Авто";

    private IntroVnUi() {
    }

    public static ButtonLayout layoutVnButtons(int sw, int sh, int currentEntry) {
        DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
        int fontSize = Math.max(10, (int) (sh * 0.031f));
        int btnH = fontSize + 8;
        int gap = Math.max(28, (int) (sw * 0.09f));

        int backW = estimateTextWidth(LABEL_BACK, fontSize) + 10;
        int histW = estimateTextWidth(LABEL_HISTORY, fontSize) + 10;
        int autoW = estimateTextWidth(LABEL_AUTO, fontSize) + 10;
        int totalW = backW + histW + autoW + gap * 2;
        int startX = (sw - totalW) / 2;

        int rowY;
        if (currentEntry == IntroScript.SHOP_ANIMATION_ENTRY_INDEX) {
            rowY = sh - btnH - 6;
        } else {
            rowY = layout.toolbarRowY(btnH);
        }

        ButtonLayout result = new ButtonLayout();
        result.backButton.set(startX, rowY, backW, btnH);
        result.historyButton.set(startX + backW + gap, rowY, histW, btnH);
        result.autoButton.set(startX + backW + gap + histW + gap, rowY, autoW, btnH);

        int panelW = Math.max(280, (int) (sw * 0.82f));
        int panelH = Math.max(200, (int) (sh * 0.72f));
        result.historyPanel.set((sw - panelW) / 2f, (sh - panelH) / 2f, panelW, panelH);

        layoutHistoryClose(result.historyPanel, result.historyClose);
        return result;
    }

    public static boolean isVnButtonRowClick(ButtonLayout layout, float mouseX, float mouseY) {
        return layout.backButton.contains(mouseX, mouseY)
            || layout.historyButton.contains(mouseX, mouseY)
            || layout.autoButton.contains(mouseX, mouseY);
    }

    private static void layoutHistoryClose(Rect panel, Rect close) {
        int size = 22;
        int margin = 8;
        close.set(panel.x + panel.width - size - margin, panel.y + margin, size, size);
    }

    /** Приблизительная ширина кириллического текста без FontMetrics. */
    private static int estimateTextWidth(String text, int fontSize) {
        return Math.round(text.length() * fontSize * 0.55f);
    }
}
