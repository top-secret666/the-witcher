package main.java.com.witcher.ui.intro;

import java.util.ArrayList;
import java.util.List;

/** Перенос и подготовка строк окна истории — общий для Swing и LibGDX. */
public final class IntroHistoryText {

    private IntroHistoryText() {
    }

    public static List<String> buildRenderedLines(List<String> rawLines, int maxWidthPx, int fontSize) {
        List<String> rendered = new ArrayList<>();
        for (String raw : rawLines) {
            if (raw == null || raw.isEmpty()) {
                rendered.add("");
                continue;
            }
            rendered.addAll(IntroDialogText.wrapLine(raw, maxWidthPx, fontSize));
        }
        return rendered;
    }

    public static boolean isSpeakerLine(String line) {
        return line != null && line.startsWith("[") && line.endsWith("]");
    }
}
