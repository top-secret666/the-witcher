package main.java.com.witcher.ui.intro;

import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.intro.view.IntroLayout;

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

    private IntroVnUi() {
    }

    public static void copyButtonLayout(ButtonLayout src, ButtonLayout dst) {
        dst.backButton.set(src.backButton.x, src.backButton.y, src.backButton.width, src.backButton.height);
        dst.historyButton.set(src.historyButton.x, src.historyButton.y,
            src.historyButton.width, src.historyButton.height);
        dst.autoButton.set(src.autoButton.x, src.autoButton.y, src.autoButton.width, src.autoButton.height);
        dst.historyPanel.set(src.historyPanel.x, src.historyPanel.y,
            src.historyPanel.width, src.historyPanel.height);
        dst.historyClose.set(src.historyClose.x, src.historyClose.y,
            src.historyClose.width, src.historyClose.height);
    }

    public static ButtonLayout layoutVnButtons(int sw, int sh, int currentEntry) {
        DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
        int fontSize = Math.max(Math.round(IntroLayout.VN_FONT_MIN),
            Math.round(sh * IntroLayout.VN_FONT_SIZE_RATIO));
        int btnH = fontSize + Math.round(IntroLayout.VN_BUTTON_PAD_V);
        int gap = Math.max(Math.round(IntroLayout.VN_BUTTON_GAP_MIN),
            Math.round(sw * IntroLayout.VN_BUTTON_GAP_RATIO));

        int backW = estimateTextWidth(VnButtonLabels.BACK, fontSize) + 10;
        int histW = estimateTextWidth(VnButtonLabels.HISTORY, fontSize) + 10;
        int autoW = estimateTextWidth(VnButtonLabels.AUTO, fontSize) + 10;
        int totalW = backW + histW + autoW + gap * 2;
        int startX = (sw - totalW) / 2;

        int rowY;
        if (currentEntry == IntroScript.SHOP_ANIMATION_ENTRY_INDEX) {
            rowY = sh - btnH - Math.round(IntroLayout.VN_ROW_BOTTOM_MARGIN);
        } else {
            rowY = layout.toolbarRowY(btnH);
        }

        ButtonLayout result = new ButtonLayout();
        result.backButton.set(startX, rowY, backW, btnH);
        result.historyButton.set(startX + backW + gap, rowY, histW, btnH);
        result.autoButton.set(startX + backW + gap + histW + gap, rowY, autoW, btnH);

        int panelW = Math.max(Math.round(IntroLayout.VN_HISTORY_PANEL_MIN_W),
            Math.round(sw * IntroLayout.VN_HISTORY_PANEL_W_RATIO));
        int panelH = Math.max(Math.round(IntroLayout.VN_HISTORY_PANEL_MIN_H),
            Math.round(sh * IntroLayout.VN_HISTORY_PANEL_H_RATIO));
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
        return Math.round(text.length() * fontSize * IntroLayout.VN_TEXT_WIDTH_FACTOR);
    }
}
