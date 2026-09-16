package main.java.com.witcher.ui.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Чёрный экран перед интро: текст слева у края, гифка справа.
 */
public final class DisclaimerScreen {

    private static final Color TEXT = new Color(210, 205, 195);
    private static final Color HINT = new Color(170, 155, 120);
    private static final Color EMPHASIS = new Color(230, 200, 120);

    private static final String GIF_PATH = "/assets/sprites/ui/disclaimer_cat.gif";
    private static final int TEXT_LEFT = 14;
    private static final int TEXT_TOP = 18;
    private static final int GIF_RIGHT_MARGIN = 10;
    private static final int GIF_MAX_W = 200;
    private static final int GIF_MAX_H = 260;

    private static final String[] LINES = {
        "В первую очередь спасибо что вообще",
        "скачали эту маленькую игру",
        "и начали это читать.",
        "",
        "Это мой самый первый серьезный проект.",
        "Не ждите много от него.",
        "",
        "Прошу еще обратить внимание",
        "что это визуальная новелла,",
        "а не полноценный хоррор.",
        "",
        "Надеюсь что вы пройдете до конца...",
        "Я очень очень старался...",
    };

    private static final String HINT_LINE = "Нажмите ПРОБЕЛ, чтобы продолжить";

    private static final GifFrames CAT_GIF = GifFrames.load(GIF_PATH);

    private int tick;
    private int animMs;
    private boolean finished;

    public void update(boolean advanceRequested) {
        tick++;
        // ~30 FPS (GameWindow timer ~33ms)
        animMs += 33;
        if (advanceRequested && tick > 12) {
            finished = true;
        }
    }

    public boolean isFinished() {
        return finished;
    }

    public void render(BufferedImage screen) {
        Graphics2D g = screen.createGraphics();
        try {
            int w = screen.getWidth();
            int h = screen.getHeight();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, w, h);

            int gifAreaLeft = drawCatGif(g, w, h);

            GameFonts fonts = GameFonts.get();
            GameFonts.applyGothicHints(g);

            Font body = fonts.plain(12);
            Font emph = fonts.bold(12);
            g.setFont(body);
            FontMetrics fm = g.getFontMetrics();
            int lineH = fm.getHeight() + 1;

            // Текст у левого края, не шире зоны до гифки.
            int textMaxW = Math.max(120, gifAreaLeft - TEXT_LEFT - 10);
            int y = TEXT_TOP + fm.getAscent();

            for (String line : LINES) {
                if (line.isEmpty()) {
                    y += lineH / 2;
                    continue;
                }
                boolean shout = line.contains("визуальная новелла")
                    || line.contains("очень очень старался");
                g.setFont(shout ? emph : body);
                fm = g.getFontMetrics();
                for (String wrapped : wrapLine(line, fm, textMaxW)) {
                    GameFonts.drawShadowed(g, wrapped, TEXT_LEFT, y, shout ? EMPHASIS : TEXT);
                    y += lineH;
                }
            }

            float pulse = 0.55f + 0.45f * (float) Math.sin(tick * 0.12);
            int alpha = Math.max(40, Math.min(255, Math.round(255 * pulse)));
            g.setFont(fonts.plain(11));
            fm = g.getFontMetrics();
            int hx = TEXT_LEFT;
            int hy = h - 22;
            Color hint = new Color(HINT.getRed(), HINT.getGreen(), HINT.getBlue(), alpha);
            GameFonts.drawShadowed(g, HINT_LINE, hx, hy, hint);
        } finally {
            g.dispose();
        }
    }

    /** @return левая граница зоны гифки (для ширины текста). */
    private int drawCatGif(Graphics2D g, int sw, int sh) {
        if (CAT_GIF == null || CAT_GIF.frames.length == 0) {
            return sw;
        }
        BufferedImage frame = frameAt(animMs);
        if (frame == null) {
            return sw;
        }

        float scale = Math.min(
            GIF_MAX_W / (float) frame.getWidth(),
            GIF_MAX_H / (float) frame.getHeight());
        scale = Math.min(1f, scale);
        int dw = Math.max(1, Math.round(frame.getWidth() * scale));
        int dh = Math.max(1, Math.round(frame.getHeight() * scale));
        int x = sw - GIF_RIGHT_MARGIN - dw;
        int y = Math.max(8, (sh - dh) / 2 - 10);

        Object prev = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(frame, x, y, dw, dh, null);
        if (prev != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prev);
        }
        return x;
    }

    private static BufferedImage frameAt(int elapsedMs) {
        int total = 0;
        for (int d : CAT_GIF.delaysMs) {
            total += Math.max(20, d);
        }
        if (total <= 0) {
            return CAT_GIF.frames[0];
        }
        int t = elapsedMs % total;
        int acc = 0;
        for (int i = 0; i < CAT_GIF.frames.length; i++) {
            acc += Math.max(20, CAT_GIF.delaysMs[i]);
            if (t < acc) {
                return CAT_GIF.frames[i];
            }
        }
        return CAT_GIF.frames[CAT_GIF.frames.length - 1];
    }

    private static String[] wrapLine(String line, FontMetrics fm, int maxW) {
        if (fm.stringWidth(line) <= maxW) {
            return new String[]{line};
        }
        java.util.List<String> out = new java.util.ArrayList<>();
        String[] words = line.split(" ");
        StringBuilder cur = new StringBuilder();
        for (String word : words) {
            String next = cur.length() == 0 ? word : cur + " " + word;
            if (fm.stringWidth(next) <= maxW) {
                cur.setLength(0);
                cur.append(next);
            } else {
                if (cur.length() > 0) {
                    out.add(cur.toString());
                }
                cur.setLength(0);
                cur.append(word);
            }
        }
        if (cur.length() > 0) {
            out.add(cur.toString());
        }
        return out.toArray(new String[0]);
    }
}
