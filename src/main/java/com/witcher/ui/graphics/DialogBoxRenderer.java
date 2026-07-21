package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.intro.view.IntroSpeakerPlateLayout;

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
        /** Нижняя полоса под кнопки Назад / История / Авто. */
        public final int toolbarReserve;

        Layout(int sw, int sh) {
            this(sw, sh, 0.30f, 1.0f);
        }

        Layout(int sw, int sh, float heightRatio, float widthRatio) {
            int boxMargin = (int) (sw * 0.03f);
            boxH = Math.max(52, (int) (sh * heightRatio));
            boxW = Math.max(200, (int) (sw * widthRatio));
            boxX = (sw - boxW) / 2;
            boxY = sh - boxH - (int) (sh * 0.02f);
            toolbarReserve = heightRatio <= 0.11f ? 0 : Math.max(24, (int) (sh * 0.058f));
            if (heightRatio <= 0.11f) {
                fontSize = Math.max(13, (int) (sh * 0.040f));
                pad = Math.max(6, (int) (sw * 0.018f));
            } else {
                fontSize = Math.max(11, (int) (sh * 0.034f));
                pad = (int) (sw * 0.02f);
            }
            textX = boxX + pad;
            textY = boxY + pad + (heightRatio <= 0.11f ? 0 : Math.round(sh * 0.008f));
            textMaxW = boxW - pad * 2;
        }

        /** Y-координата строки VN-кнопок внутри нижней полосы окна. */
        public int toolbarRowY(int buttonHeight) {
            return boxY + boxH - toolbarReserve + Math.max(0, (toolbarReserve - buttonHeight) / 2);
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

        IntroSpeakerPlateLayout.Plate plate = IntroSpeakerPlateLayout.compute(
            boxX, boxY, pad, nameW, nameH, nfm.getAscent());

        Composite prevN = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.9f));
        g.setColor(BOX_BG);
        g.fillRect(plate.boxX, plate.boxY, plate.boxW, plate.boxH);
        g.setComposite(prevN);
        g.setColor(BOX_BORDER);
        g.drawRect(plate.boxX, plate.boxY, plate.boxW, plate.boxH);
        g.setColor(speakerColor);
        GameFonts.drawOutlined(g, speaker, plate.textX, plate.textBaselineY, speakerColor);
    }

    public static int drawSpeakerText(Graphics2D g, String speaker, String text, Color speakerColor,
                                      Layout layout, float alpha) {
        enableTextSmoothing(g);
        drawBox(g, layout.boxX, layout.boxY, layout.boxW, layout.boxH, alpha);
        drawSpeakerName(g, speaker, speakerColor, layout.boxX, layout.boxY, layout.pad, layout.fontSize, alpha);
        int lineY = drawCompactBodyLines(
            g, text, layout, speaker == null ? speakerColor : SPEECH_COLOR);
        disableTextSmoothing(g);
        return lineY;
    }

    public static int drawTypewriterText(Graphics2D g, String speaker, String visibleText,
                                         Color speakerColor, Layout layout, float alpha) {
        enableTextSmoothing(g);
        drawBox(g, layout.boxX, layout.boxY, layout.boxW, layout.boxH, alpha);
        drawSpeakerName(g, speaker, speakerColor, layout.boxX, layout.boxY, layout.pad, layout.fontSize, alpha);
        int lineY = drawCompactBodyLines(
            g, visibleText, layout, speaker == null ? speakerColor : SPEECH_COLOR);
        disableTextSmoothing(g);
        return lineY;
    }

    /** Плотный межстрочный интервал, выравнивание влево, зона над кнопками VN. */
    private static int drawCompactBodyLines(Graphics2D g, String text, Layout layout, Color textColor) {
        Font textFont = GameFonts.get().plain(layout.fontSize);
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        int lineStep = compactLineStep(fm, layout.fontSize);
        int bottomLimit = layout.boxY + layout.boxH - layout.pad - layout.toolbarReserve;
        int lineY = layout.textY + fm.getAscent();

        Shape prevClip = g.getClip();
        g.clipRect(layout.textX, layout.textY, layout.textMaxW, Math.max(0, bottomLimit - layout.textY));

        for (String rawLine : normalizeFlowText(text).split("\n", -1)) {
            if (rawLine.isBlank()) {
                lineY += Math.max(2, lineStep / 3);
                continue;
            }
            for (String wl : wrapLine(rawLine.trim(), fm, layout.textMaxW)) {
                if (lineY > bottomLimit) {
                    break;
                }
                GameFonts.drawShadowed(g, wl, layout.textX, lineY, textColor);
                lineY += lineStep;
            }
        }

        g.setClip(prevClip);
        return lineY;
    }

    /**
     * Склеивает одиночные переносы в пробел — реплика течёт по ширине окна, а не столбиком.
     * Двойной перенос оставляет абзац.
     */
    public static String normalizeFlowText(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        String s = text.replace("\r\n", "\n");
        s = s.replace("\n\n", "\u0001");
        s = s.replace('\n', ' ');
        s = s.replace("\u0001", "\n");
        s = s.replaceAll(" *\n *", "\n");
        s = s.replaceAll(" {2,}", " ");
        return s.trim();
    }

    private static int compactLineStep(FontMetrics fm, int fontSize) {
        return fm.getAscent() + Math.max(2, fontSize / 6);
    }

    /**
     * Компактная рамка внизу экрана — чёткий текст (лавка).
     */
    public static void drawCompactFramedSpeakerText(Graphics2D g, int sw, int sh, String speaker, String text,
                                                    Color speakerColor, float alpha) {
        disableTextSmoothing(g);
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int fontSize = Math.max(12, (int) (sh * 0.038f));
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
            GameFonts.drawOutlined(g, speakerLabel, textX, startY, speakerColor);
            if (!lines.isEmpty()) {
                String first = lines.get(0);
                g.setFont(textFont);
                GameFonts.drawShadowed(g, first, textX + speakerW, startY, SPEECH_COLOR);
                for (int i = 1; i < lines.size(); i++) {
                    int y = startY + lineH * i;
                    GameFonts.drawShadowed(g, lines.get(i), textX, y, SPEECH_COLOR);
                }
            }
        } else {
            for (int i = 0; i < lines.size(); i++) {
                GameFonts.drawShadowed(g, lines.get(i), textX, startY + lineH * i, SPEECH_COLOR);
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
        int hintY = layout.toolbarReserve > 0
            ? layout.boxY + layout.boxH - layout.toolbarReserve - 4
            : layout.boxY + layout.boxH - layout.pad + 2;
        g.drawString(hint, layout.boxX + layout.boxW - layout.pad - hw, hintY);
        disableTextSmoothing(g);
    }

    /**
     * Полоска внизу без золотой рамки — полупрозрачный чёрный фон, серый текст
     * (эпилог с осколком перед заставкой).
     */
    public static void drawShardEpilogueBar(Graphics2D g, int sw, int sh,
                                            String speaker, String text, float alpha) {
        GameFonts.applyGothicHints(g);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        int fontSize = Math.max(12, Math.round(sh * 0.036f));
        int padX = Math.round(sw * 0.06f);
        int barH = Math.round(sh * 0.30f);
        int barY = sh - barH;

        g.setColor(new Color(0, 0, 0, 185));
        g.fillRect(0, barY, sw, barH);

        Font textFont = GameFonts.get().plain(fontSize);
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        int lineH = fm.getHeight() + 4;
        int textMaxW = sw - padX * 2;
        Color bodyColor = new Color(178, 172, 164);
        Color nameColor = new Color(198, 192, 184);

        int y = barY + Math.round(barH * 0.26f);
        if (speaker != null && !speaker.isBlank()) {
            g.setFont(GameFonts.get().bold(fontSize));
            g.setColor(nameColor);
            g.drawString(speaker, padX, y);
            y += lineH;
            g.setFont(textFont);
            fm = g.getFontMetrics();
        }

        g.setColor(bodyColor);
        for (String rawLine : text.split("\n", -1)) {
            for (String wl : wrapLine(rawLine, fm, textMaxW)) {
                if (y > barY + barH - padX) {
                    break;
                }
                g.drawString(wl, padX, y);
                y += lineH;
            }
        }

        g.setComposite(prev);
    }

    public static String getLastVisibleLine(String text, FontMetrics fm, int maxW) {
        List<String> wrapped = new ArrayList<>();
        for (String rawLine : normalizeFlowText(text).split("\n", -1)) {
            if (rawLine.isBlank()) {
                continue;
            }
            wrapped.addAll(wrapLine(rawLine.trim(), fm, maxW));
        }
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
