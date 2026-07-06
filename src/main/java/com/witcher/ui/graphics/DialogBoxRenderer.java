package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Переиспользуемый рендерер диалоговых окон в стиле Ведьмака.
 */
public final class DialogBoxRenderer {

    public static final Color NARRATOR_COLOR = new Color(160, 145, 120);
    public static final Color GERALT_COLOR = new Color(160, 205, 235);
    public static final Color DUKE_COLOR = new Color(218, 165, 32);
    public static final Color BOX_BG = new Color(10, 8, 4, 220);
    public static final Color BOX_BORDER = new Color(140, 100, 35);
    public static final Color HINT_COLOR = new Color(180, 160, 120, 180);
    public static final Color SPEECH_COLOR = new Color(220, 190, 100);

    private DialogBoxRenderer() {
    }

    public static final class Layout {
        public final int boxX;
        public final int boxY;
        public final int boxW;
        public final int boxH;
        public final int pad;
        public final int textX;
        public final int textY;
        public final int textMaxW;
        public final int fontSize;

        Layout(int sw, int sh) {
            this(sw, sh, 0.30f, 1.0f);
        }

        Layout(int sw, int sh, float heightRatio, float widthRatio) {
            int boxMargin = (int) (sw * 0.03f);
            boxH = Math.max(52, (int) (sh * heightRatio));
            boxW = Math.max(200, (int) (sw * widthRatio));
            boxX = (sw - boxW) / 2;
            boxY = sh - boxH - (int) (sh * 0.02f);
            if (heightRatio <= 0.11f) {
                fontSize = Math.max(13, (int) (sh * 0.042f));
                pad = Math.max(6, (int) (sw * 0.018f));
            } else {
                fontSize = Math.max(11, (int) (sh * 0.038f));
                pad = (int) (sw * 0.02f);
            }
            textX = boxX + pad;
            textY = boxY + pad;
            textMaxW = boxW - pad * 2;
        }
    }

    public static Layout computeLayout(int sw, int sh) {
        return new Layout(sw, sh);
    }

    /** Компактное окно для лавки — низкая полоска, крупный текст. */
    public static Layout computeCompactLayout(int sw, int sh) {
        return new Layout(sw, sh, 0.10f, 0.78f);
    }

    public static Layout computeLayout(int sw, int sh, float heightRatio, float widthRatio) {
        return new Layout(sw, sh, heightRatio, widthRatio);
    }

    public static void drawBox(Graphics2D g, int boxX, int boxY, int boxW, int boxH, float alpha) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.90f));
        g.setColor(BOX_BG);
        g.fillRect(boxX, boxY, boxW, boxH);

        GradientPaint bgGradient = new GradientPaint(
            boxX, boxY, new Color(20, 16, 8, 80),
            boxX, boxY + boxH / 3, new Color(5, 4, 2, 0)
        );
        g.setPaint(bgGradient);
        g.fillRect(boxX, boxY, boxW, boxH / 3);
        g.setComposite(prev);

        int alpha255 = Math.max(0, Math.min(255, (int) (alpha * 255)));

        g.setColor(new Color(235, 200, 110, alpha255));
        g.fillRect(boxX - 2, boxY - 2, boxW + 4, 4);
        g.fillRect(boxX - 2, boxY + boxH - 2, boxW + 4, 4);
        g.fillRect(boxX - 2, boxY - 2, 4, boxH + 4);
        g.fillRect(boxX + boxW - 2, boxY - 2, 4, boxH + 4);

        g.setColor(new Color(218, 165, 32, alpha255));
        g.fillRect(boxX, boxY, boxW, 2);
        g.fillRect(boxX, boxY + boxH - 2, boxW, 2);
        g.fillRect(boxX, boxY, 2, boxH);
        g.fillRect(boxX + boxW - 2, boxY, 2, boxH);

        g.setColor(new Color(60, 45, 15, Math.max(0, Math.min(255, (int) (alpha * 200)))));
        g.fillRect(boxX + 4, boxY + 4, boxW - 8, 1);
        g.fillRect(boxX + 4, boxY + boxH - 5, boxW - 8, 1);
        g.fillRect(boxX + 4, boxY + 4, 1, boxH - 8);
        g.fillRect(boxX + boxW - 5, boxY + 4, 1, boxH - 8);

        int cornerSize = 12;
        g.setColor(new Color(255, 215, 0, Math.max(0, Math.min(255, (int) (alpha * 220)))));
        g.fillRect(boxX - 2, boxY - 2, cornerSize, 2);
        g.fillRect(boxX - 2, boxY - 2, 2, cornerSize);
        g.fillRect(boxX + boxW - cornerSize + 2, boxY - 2, cornerSize, 2);
        g.fillRect(boxX + boxW, boxY - 2, 2, cornerSize);
        g.fillRect(boxX - 2, boxY + boxH, cornerSize, 2);
        g.fillRect(boxX - 2, boxY + boxH - cornerSize + 2, 2, cornerSize);
        g.fillRect(boxX + boxW - cornerSize + 2, boxY + boxH, cornerSize, 2);
        g.fillRect(boxX + boxW, boxY + boxH - cornerSize + 2, 2, cornerSize);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.65f));
        g.setColor(new Color(255, 245, 160));
        g.drawRect(boxX + 1, boxY + 1, boxW - 2, boxH - 2);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.45f));
        g.drawRect(boxX + 2, boxY + 2, boxW - 4, boxH - 4);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.25f));
        g.drawRect(boxX + 3, boxY + 3, boxW - 6, boxH - 6);
        g.setComposite(prev);
    }

    public static void drawSpeakerName(Graphics2D g, String speaker, Color speakerColor,
                                       int boxX, int boxY, int pad, int fontSize, float alpha) {
        if (speaker == null || speaker.isEmpty()) return;

        Font nameFont = GameFonts.get().bold(fontSize);
        g.setFont(nameFont);
        FontMetrics nfm = g.getFontMetrics();
        int nameW = nfm.stringWidth(speaker);
        int nameH = nfm.getHeight();

        int nameBoxX = boxX + pad - 4;
        int nameBoxY = boxY - nameH - 2;
        int nameBoxW = nameW + 12;
        int nameBoxH = nameH + 4;

        Composite prevN = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.9f));
        g.setColor(BOX_BG);
        g.fillRect(nameBoxX, nameBoxY, nameBoxW, nameBoxH);
        g.setComposite(prevN);
        g.setColor(BOX_BORDER);
        g.drawRect(nameBoxX, nameBoxY, nameBoxW, nameBoxH);
        g.setColor(speakerColor);
        g.drawString(speaker, nameBoxX + 6, nameBoxY + nfm.getAscent() + 2);
    }

    public static int drawSpeakerText(Graphics2D g, String speaker, String text, Color speakerColor,
                                      Layout layout, float alpha) {
        enableTextSmoothing(g);
        drawBox(g, layout.boxX, layout.boxY, layout.boxW, layout.boxH, alpha);
        drawSpeakerName(g, speaker, speakerColor, layout.boxX, layout.boxY, layout.pad, layout.fontSize, alpha);

        Font textFont = GameFonts.get().plain(layout.fontSize);
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        int lineH = fm.getHeight();
        int lineY = layout.textY;
        Color textColor = speaker == null ? speakerColor : SPEECH_COLOR;

        String[] rawLines = text.split("\n", -1);
        for (String rawLine : rawLines) {
            for (String wl : wrapLine(rawLine, fm, layout.textMaxW)) {
                lineY += lineH;
                if (lineY > layout.boxY + layout.boxH - layout.pad) break;
                g.setColor(new Color(0, 0, 0, 160));
                g.drawString(wl, layout.textX + 1, lineY + 1);
                g.setColor(textColor);
                g.drawString(wl, layout.textX, lineY);
            }
        }

        disableTextSmoothing(g);
        return lineY;
    }

    public static int drawTypewriterText(Graphics2D g, String speaker, String visibleText,
                                         Color speakerColor, Layout layout, float alpha) {
        enableTextSmoothing(g);
        drawBox(g, layout.boxX, layout.boxY, layout.boxW, layout.boxH, alpha);
        drawSpeakerName(g, speaker, speakerColor, layout.boxX, layout.boxY, layout.pad, layout.fontSize, alpha);

        Font textFont = GameFonts.get().plain(layout.fontSize);
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        int lineH = fm.getHeight();
        int lineY = layout.textY;
        Color textColor = speaker == null ? speakerColor : SPEECH_COLOR;

        String[] rawLines = visibleText.split("\n", -1);
        for (String rawLine : rawLines) {
            for (String wl : wrapLine(rawLine, fm, layout.textMaxW)) {
                lineY += lineH;
                if (lineY > layout.boxY + layout.boxH - layout.pad) break;
                g.setColor(new Color(0, 0, 0, 160));
                g.drawString(wl, layout.textX + 1, lineY + 1);
                g.setColor(textColor);
                g.drawString(wl, layout.textX, lineY);
            }
        }

        disableTextSmoothing(g);
        return lineY;
    }

    /**
     * Компактная рамка внизу экрана — чёткий текст без сглаживания (лавка и т.п.).
     */
    public static void drawCompactFramedSpeakerText(Graphics2D g, int sw, int sh, String speaker, String text,
                                                    Color speakerColor, float alpha) {
        disableTextSmoothing(g);
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int fontSize = Math.max(12, (int) (sh * 0.036f));
        int boxMarginX = 10;
        int boxMarginBottom = 5;
        int pad = 8;
        int boxW = sw - boxMarginX * 2;
        int textMaxW = boxW - pad * 2;
        int lineH = fontSize + 3;
        int maxLines = 2;

        Font textFont = GameFonts.get().plain(fontSize);
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();

        String speakerLabel = (speaker != null && !speaker.isEmpty()) ? speaker + ": " : "";
        int speakerW = speakerLabel.isEmpty() ? 0 : g.getFontMetrics(
            GameFonts.get().bold(fontSize)).stringWidth(speakerLabel);

        List<String> lines = new ArrayList<>();
        for (String rawLine : text.split("\n", -1)) {
            lines.addAll(wrapLine(rawLine, fm, textMaxW - (lines.isEmpty() ? speakerW : 0)));
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        if (lines.size() > maxLines) {
            lines = lines.subList(0, maxLines);
        }

        int blockH = lineH * lines.size();
        int boxH = blockH + pad * 2;
        int boxX = boxMarginX;
        int boxY = sh - boxMarginBottom - boxH;

        int alpha255 = Math.max(0, Math.min(255, (int) (alpha * 255)));
        g.setColor(new Color(10, 8, 4, Math.min(230, alpha255)));
        g.fillRoundRect(boxX, boxY, boxW, boxH, 5, 5);
        g.setColor(new Color(140, 100, 35, alpha255));
        g.drawRoundRect(boxX, boxY, boxW, boxH, 5, 5);
        g.setColor(new Color(218, 165, 32, Math.max(0, Math.min(255, (int) (alpha * 160)))));
        g.drawRoundRect(boxX + 1, boxY + 1, boxW - 2, boxH - 2, 4, 4);

        int textX = boxX + pad;
        int startY = boxY + pad + fm.getAscent();

        if (!speakerLabel.isEmpty()) {
            g.setFont(GameFonts.get().bold(fontSize));
            g.setColor(speakerColor);
            g.drawString(speakerLabel, textX, startY);
            if (!lines.isEmpty()) {
                String first = lines.get(0);
                g.setFont(textFont);
                g.setColor(SPEECH_COLOR);
                g.drawString(first, textX + speakerW, startY);
                for (int i = 1; i < lines.size(); i++) {
                    int y = startY + lineH * i;
                    g.drawString(lines.get(i), textX, y);
                }
            }
        } else {
            for (int i = 0; i < lines.size(); i++) {
                g.setColor(SPEECH_COLOR);
                g.drawString(lines.get(i), textX, startY + lineH * i);
            }
        }

        g.setComposite(prev);
    }

    public static void drawPlainSpeakerText(Graphics2D g, int sw, int sh, String speaker, String text,
                                            Color speakerColor, float alpha) {
        enableTextSmoothing(g);
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int fontSize = Math.max(13, (int) (sh * 0.040f));
        int marginX = 14;
        int textMaxW = sw - marginX * 2;
        int lineH = fontSize + 4;
        int maxLines = 2;
        int bottomPad = 10;

        Font textFont = GameFonts.get().plain(fontSize);
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();

        List<String> lines = new ArrayList<>();
        for (String rawLine : text.split("\n", -1)) {
            lines.addAll(wrapLine(rawLine, fm, textMaxW));
        }
        if (lines.size() > maxLines) {
            lines = lines.subList(0, maxLines);
        }

        int blockH = lineH * lines.size();
        int startY = sh - bottomPad - blockH + fm.getAscent();

        if (speaker != null && !speaker.isEmpty()) {
            g.setFont(GameFonts.get().bold(fontSize));
            g.setColor(speakerColor);
            String label = speaker + ": ";
            g.drawString(label, marginX, startY);
            int labelW = g.getFontMetrics().stringWidth(label);
            if (!lines.isEmpty()) {
                String first = lines.get(0);
                g.setFont(textFont);
                g.setColor(new Color(0, 0, 0, 140));
                g.drawString(first, marginX + labelW + 1, startY + 1);
                g.setColor(SPEECH_COLOR);
                g.drawString(first, marginX + labelW, startY);
                for (int i = 1; i < lines.size(); i++) {
                    int y = startY + lineH * i;
                    g.setColor(new Color(0, 0, 0, 140));
                    g.drawString(lines.get(i), marginX + 1, y + 1);
                    g.setColor(SPEECH_COLOR);
                    g.drawString(lines.get(i), marginX, y);
                }
            }
        } else {
            for (int i = 0; i < lines.size(); i++) {
                int y = startY + lineH * i;
                g.setColor(new Color(0, 0, 0, 140));
                g.drawString(lines.get(i), marginX + 1, y + 1);
                g.setColor(SPEECH_COLOR);
                g.drawString(lines.get(i), marginX, y);
            }
        }

        g.setComposite(prev);
        disableTextSmoothing(g);
    }

    public static void drawHint(Graphics2D g, String hint, Layout layout, int fontSize, float alpha) {
        enableTextSmoothing(g);
        g.setFont(GameFonts.get().bold(Math.max(10, fontSize - 2)));
        g.setColor(HINT_COLOR);
        int hw = g.getFontMetrics().stringWidth(hint);
        g.drawString(hint, layout.boxX + layout.boxW - layout.pad - hw, layout.boxY + layout.boxH - layout.pad + 2);
        disableTextSmoothing(g);
    }

    public static String getLastVisibleLine(String text, FontMetrics fm, int maxW) {
        String[] lines = text.split("\n", -1);
        String last = lines[lines.length - 1];
        List<String> wrapped = wrapLine(last, fm, maxW);
        return wrapped.isEmpty() ? "" : wrapped.get(wrapped.size() - 1);
    }

    public static List<String> wrapLine(String line, FontMetrics fm, int maxW) {
        List<String> result = new ArrayList<>();
        if (line.isEmpty()) {
            result.add("");
            return result;
        }
        StringBuilder current = new StringBuilder();
        for (String word : line.split("(?<=\\s)")) {
            if (fm.stringWidth(current.toString() + word) > maxW && current.length() > 0) {
                result.add(current.toString());
                current = new StringBuilder();
            }
            current.append(word);
        }
        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    private static void enableTextSmoothing(Graphics2D g) {
        GameFonts.applyDialogHints(g);
    }

    private static void disableTextSmoothing(Graphics2D g) {
        GameFonts.applyPixelHints(g);
    }
}
