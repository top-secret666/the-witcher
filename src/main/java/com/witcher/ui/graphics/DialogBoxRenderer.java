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
            int boxMargin = (int) (sw * 0.03f);
            boxH = (int) (sh * 0.30f);
            boxW = sw - boxMargin * 2;
            boxX = boxMargin;
            boxY = sh - boxH - (int) (sh * 0.02f);
            pad = (int) (sw * 0.02f);
            textX = boxX + pad;
            textY = boxY + pad;
            textMaxW = boxW - pad * 2;
            fontSize = Math.max(12, (int) (sh * 0.040f));
        }
    }

    public static Layout computeLayout(int sw, int sh) {
        return new Layout(sw, sh);
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

        Font nameFont = new Font("Serif", Font.BOLD, fontSize);
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

        Font textFont = new Font("Serif", Font.PLAIN, layout.fontSize);
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

        Font textFont = new Font("Serif", Font.PLAIN, layout.fontSize);
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

    public static void drawHint(Graphics2D g, String hint, Layout layout, int fontSize, float alpha) {
        enableTextSmoothing(g);
        g.setFont(new Font("Serif", Font.BOLD, Math.max(10, fontSize - 2)));
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
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private static void disableTextSmoothing(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
    }
}
