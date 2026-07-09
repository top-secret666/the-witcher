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
import main.java.com.witcher.ui.intro.IntroHistoryText;
import main.java.com.witcher.ui.intro.presenter.IntroController;
import main.java.com.witcher.ui.intro.view.IntroHistoryLayout;
import main.java.com.witcher.ui.intro.view.IntroHistoryTheme;

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
        IntroHistoryLayout.Metrics m = IntroHistoryLayout.compute(sw, sh,
            panel.x, panel.y, panel.width, panel.height);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(IntroHistoryTheme.DIVIDER_R / 255f, IntroHistoryTheme.DIVIDER_G / 255f,
            IntroHistoryTheme.DIVIDER_B / 255f, fade * (IntroHistoryTheme.DIVIDER_A / 255f));
        shapes.line(m.textX, C.textBaseline(m.headerBottom - 4f), m.textX + m.textMaxW,
            C.textBaseline(m.headerBottom - 4f));
        shapes.line(m.textX, C.textBaseline(m.footerTop), m.textX + m.textMaxW,
            C.textBaseline(m.footerTop));
        shapes.end();
        PixelTextures.resetBlend();
    }

    public static void drawContent(SpriteBatch batch, GameFonts fonts,
                                   GdxUiChrome chrome, GlyphLayout glyph,
                                   SwingCoords C, IntroController controller, int sw, int sh,
                                   float alpha) {
        float fade = alpha;
        IntroController.IntroRect panel = controller.getHistoryPanelBounds();
        IntroHistoryLayout.Metrics m = IntroHistoryLayout.compute(sw, sh,
            panel.x, panel.y, panel.width, panel.height);

        BitmapFont titleFont = fonts.dialogBoldAt(m.titleSize);
        BitmapFont bodyFont = fonts.dialogAt(m.fontSize);
        BitmapFont hintFont = fonts.dialogAt(m.hintSize);
        titleFont.getData().setScale(1f);
        bodyFont.getData().setScale(1f);
        hintFont.getData().setScale(1f);

        batch.setColor(1f, 1f, 1f, 1f);
        titleFont.setColor(IntroHistoryTheme.TITLE_R / 255f, IntroHistoryTheme.TITLE_G / 255f,
            IntroHistoryTheme.TITLE_B / 255f, fade);
        titleFont.draw(batch, "История", m.textX, C.textBaseline(m.titleBaseline));

        List<String> rendered = IntroHistoryText.buildRenderedLines(
            controller.buildHistoryLogLines(), Math.round(m.textMaxW), m.fontSize);
        int maxScroll = IntroHistoryLayout.maxScroll(rendered.size(), m.lineH, m.contentH);
        int scroll = IntroHistoryLayout.clampScroll(controller.getHistoryScroll(), maxScroll);

        float y = m.contentTop + m.bodyAscent - scroll;
        for (String line : rendered) {
            if (y > m.contentBottom) {
                break;
            }
            if (y + m.bodyDescent >= m.contentTop) {
                if (IntroHistoryText.isSpeakerLine(line)) {
                    bodyFont.setColor(IntroHistoryTheme.SPEAKER_R / 255f, IntroHistoryTheme.SPEAKER_G / 255f,
                        IntroHistoryTheme.SPEAKER_B / 255f, fade);
                } else {
                    bodyFont.setColor(IntroHistoryTheme.BODY_R / 255f, IntroHistoryTheme.BODY_G / 255f,
                        IntroHistoryTheme.BODY_B / 255f, fade);
                }
                bodyFont.draw(batch, line, m.textX, C.textBaseline(y));
            }
            y += m.lineH;
        }

        hintFont.setColor(IntroHistoryTheme.HINT_R / 255f, IntroHistoryTheme.HINT_G / 255f,
            IntroHistoryTheme.HINT_B / 255f, fade * (IntroHistoryTheme.HINT_A / 255f));
        hintFont.draw(batch, "Колёсико — прокрутка", m.textX, C.textBaseline(m.hintBaseline));

        IntroController.IntroRect close = controller.getHistoryCloseBounds();
        if (chrome != null) {
            chrome.drawCloseButton(batch, C, close.x, close.y, close.width, close.height,
                controller.isHistoryCloseHovered(), fade);
        }
    }
}
