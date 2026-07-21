package main.java.com.witcher.ui.intro;

import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.intro.view.IntroDialogLayout;
import main.java.com.witcher.ui.intro.view.IntroDialogTheme;
import main.java.com.witcher.ui.intro.view.IntroLayout;

import java.util.ArrayList;
import java.util.List;

/** Перенос строк диалога интро — общий для Swing и LibGDX. */
public final class IntroDialogText {

    public static final int SPEECH_R = IntroDialogTheme.SPEECH_R;
    public static final int SPEECH_G = IntroDialogTheme.SPEECH_G;
    public static final int SPEECH_B = IntroDialogTheme.SPEECH_B;

    private IntroDialogText() {
    }

    public static int speechRgb() {
        return IntroDialogTheme.speechRgb();
    }

    public static int textColorRgb(String speaker, int speakerColorRgb) {
        return speaker == null ? speakerColorRgb : speechRgb();
    }

    public static List<String> buildVisibleLines(String visibleText, int maxWidthPx, int fontSize) {
        List<String> lines = new ArrayList<>();
        if (visibleText == null || visibleText.isEmpty()) {
            lines.add("");
            return lines;
        }
        for (String rawLine : DialogBoxRenderer.normalizeFlowText(visibleText).split("\n", -1)) {
            if (rawLine.isBlank()) {
                continue;
            }
            lines.addAll(wrapLine(rawLine.trim(), maxWidthPx, fontSize));
        }
        return lines;
    }

    public static List<String> wrapLine(String line, int maxWidthPx, int fontSize) {
        List<String> result = new ArrayList<>();
        if (line == null || line.isEmpty()) {
            result.add("");
            return result;
        }
        float charW = Math.max(4f, fontSize * IntroLayout.VN_TEXT_WIDTH_FACTOR);
        int maxChars = Math.max(8, Math.round(maxWidthPx / charW));
        StringBuilder current = new StringBuilder();
        for (String word : line.split("(?<=\\s)")) {
            if (current.length() > 0 && current.length() + word.length() > maxChars) {
                result.add(current.toString());
                current = new StringBuilder();
            }
            current.append(word);
        }
        if (current.length() > 0) {
            result.add(current.toString());
        }
        return result;
    }

    public static float lineHeight(int fontSize) {
        return fontSize + 5f;
    }

    public static float maxLineSwingY(IntroDialogLayout.Layout layout) {
        return layout.boxY + layout.boxH - layout.pad;
    }
}
