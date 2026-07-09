package main.java.com.witcher.gdx.intro;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.GdxUiChrome;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.graphics.SwingCoords;
import main.java.com.witcher.ui.intro.presenter.IntroController;
import main.java.com.witcher.ui.intro.view.IntroHistoryTheme;

import java.util.ArrayList;
import java.util.List;

/** Окно истории интро — визуал как {@code IntroScreen.drawHistoryOverlay} (Swing). */
public final class GdxHistoryPanelRenderer {

    private GdxHistoryPanelRenderer() {
    }

    public static void drawDim(ShapeRenderer shapes, int sw, int sh, float alpha) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, alpha * IntroHistoryTheme.DIM_ALPHA);
        shapes.rect(0f, 0f, sw, sh);
        shapes.end();
        PixelTextures.resetBlend();
    }

    public static void drawFrame(ShapeRenderer shapes, SwingCoords C, IntroController.IntroRect panel,
                                 float alpha) {
        GdxDialogBoxRenderer.drawBoxAt(shapes, C,
            Math.round(panel.x), Math.round(panel.y),
            Math.round(panel.width), Math.round(panel.height), alpha);
    }

    public static void drawDividers(ShapeRenderer shapes, SwingCoords C, IntroController controller,
                                    int sw, int sh, float fade) {
        IntroController.IntroRect panel = controller.getHistoryPanelBounds();
        int fontSize = IntroHistoryTheme.fontSize(sh);
        int titleSize = fontSize + IntroHistoryTheme.TITLE_SIZE_DELTA;
        int hintSize = Math.max(IntroHistoryTheme.HINT_SIZE_MIN, fontSize + IntroHistoryTheme.HINT_SIZE_DELTA);
        int pad = IntroHistoryTheme.pad(sw);
        float textX = panel.x + pad;
        float textMaxW = panel.width - pad * 2f;
        float titleBaseline = panel.y + pad + titleSize;
        float headerBottom = titleBaseline + IntroHistoryTheme.HEADER_GAP + 4f;
        float hintBaseline = panel.y + panel.height - pad;
        float footerTop = hintBaseline - hintSize - IntroHistoryTheme.FOOTER_GAP;

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(IntroHistoryTheme.DIVIDER_R / 255f, IntroHistoryTheme.DIVIDER_G / 255f,
            IntroHistoryTheme.DIVIDER_B / 255f, fade * (IntroHistoryTheme.DIVIDER_A / 255f));
        shapes.line(textX, C.textBaseline(headerBottom - 4f), textX + textMaxW,
            C.textBaseline(headerBottom - 4f));
        shapes.line(textX, C.textBaseline(footerTop), textX + textMaxW, C.textBaseline(footerTop));
        shapes.end();
        PixelTextures.resetBlend();
    }

    public static void drawContent(SpriteBatch batch, GameFonts fonts,
                                     GdxUiChrome chrome, GlyphLayout glyph,
                                     SwingCoords C, IntroController controller, int sw, int sh,
                                     float alpha) {
        float fade = alpha;
        IntroController.IntroRect panel = controller.getHistoryPanelBounds();
        int fontSize = IntroHistoryTheme.fontSize(sh);
        int titleSize = fontSize + IntroHistoryTheme.TITLE_SIZE_DELTA;
        int hintSize = Math.max(IntroHistoryTheme.HINT_SIZE_MIN, fontSize + IntroHistoryTheme.HINT_SIZE_DELTA);
        int pad = IntroHistoryTheme.pad(sw);
        float textX = panel.x + pad;
        float textMaxW = panel.width - pad * 2f;
        int lineH = fontSize + IntroHistoryTheme.LINE_GAP;

        BitmapFont titleFont = fonts.dialogBoldAt(titleSize);
        BitmapFont bodyFont = fonts.dialogAt(fontSize);
        BitmapFont hintFont = fonts.dialogAt(hintSize);
        titleFont.getData().setScale(1f);
        bodyFont.getData().setScale(1f);
        hintFont.getData().setScale(1f);

        float titleBaseline = panel.y + pad + titleFont.getCapHeight();
        float headerBottom = titleBaseline + titleFont.getDescent() + IntroHistoryTheme.HEADER_GAP;
        float hintBaseline = panel.y + panel.height - pad;
        float footerTop = hintBaseline - hintFont.getCapHeight() - IntroHistoryTheme.FOOTER_GAP;
        float contentTop = headerBottom;
        float contentBottom = footerTop;
        float contentH = Math.max(0f, contentBottom - contentTop);

        batch.setColor(1f, 1f, 1f, 1f);
        titleFont.setColor(IntroHistoryTheme.TITLE_R / 255f, IntroHistoryTheme.TITLE_G / 255f,
            IntroHistoryTheme.TITLE_B / 255f, fade);
        titleFont.draw(batch, "История", textX, C.textBaseline(titleBaseline));

        List<String> rendered = buildWrappedLines(controller.buildHistoryLogLines(), bodyFont, glyph, textMaxW);
        int totalHeight = rendered.size() * lineH;
        int maxScroll = Math.max(0, totalHeight - Math.round(contentH));
        int scroll = Math.min(controller.getHistoryScroll(), maxScroll);
        int firstLine = scroll / lineH;
        int scrollRemainder = scroll % lineH;

        float y = contentTop + bodyFont.getCapHeight() - scrollRemainder;
        for (int i = firstLine; i < rendered.size(); i++) {
            if (y > contentBottom) {
                break;
            }
            if (y + bodyFont.getDescent() >= contentTop) {
                String line = rendered.get(i);
                boolean speaker = line.startsWith("[") && line.endsWith("]");
                if (speaker) {
                    bodyFont.setColor(IntroHistoryTheme.SPEAKER_R / 255f, IntroHistoryTheme.SPEAKER_G / 255f,
                        IntroHistoryTheme.SPEAKER_B / 255f, fade);
                } else {
                    bodyFont.setColor(IntroHistoryTheme.BODY_R / 255f, IntroHistoryTheme.BODY_G / 255f,
                        IntroHistoryTheme.BODY_B / 255f, fade);
                }
                bodyFont.draw(batch, line, textX, C.textBaseline(y));
            }
            y += lineH;
        }

        hintFont.setColor(IntroHistoryTheme.HINT_R / 255f, IntroHistoryTheme.HINT_G / 255f,
            IntroHistoryTheme.HINT_B / 255f, fade * (IntroHistoryTheme.HINT_A / 255f));
        hintFont.draw(batch, "Колёсико — прокрутка", textX, C.textBaseline(hintBaseline));

        IntroController.IntroRect close = controller.getHistoryCloseBounds();
        if (chrome != null) {
            chrome.drawCloseButton(batch, C, close.x, close.y, close.width, close.height,
                controller.isHistoryCloseHovered(), fade);
        }
    }

    private static List<String> buildWrappedLines(List<String> rawLines, BitmapFont font,
                                                  GlyphLayout glyph, float maxW) {
        List<String> out = new ArrayList<>();
        for (String raw : rawLines) {
            if (raw == null || raw.isEmpty()) {
                out.add("");
                continue;
            }
            out.addAll(wrapLine(raw, font, glyph, maxW));
        }
        return out;
    }

    private static List<String> wrapLine(String line, BitmapFont font, GlyphLayout glyph, float maxW) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : line.split("(?<=\\s)")) {
            String trial = current.toString() + word;
            glyph.setText(font, trial);
            if (glyph.width > maxW && current.length() > 0) {
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
}
