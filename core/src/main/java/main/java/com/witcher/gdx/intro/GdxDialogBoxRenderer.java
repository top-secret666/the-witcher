package main.java.com.witcher.gdx.intro;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.graphics.SwingCoords;
import main.java.com.witcher.ui.intro.IntroDialogText;
import main.java.com.witcher.ui.intro.IntroScript;
import main.java.com.witcher.ui.intro.presenter.IntroController;
import main.java.com.witcher.ui.intro.view.IntroDialogLayout;
import main.java.com.witcher.ui.intro.view.IntroDialogTheme;
import main.java.com.witcher.ui.intro.view.IntroSpeakerPlateLayout;
import main.java.com.witcher.ui.intro.view.IntroTextLayout;

import java.util.List;

/**
 * Диалоговое окно интро LibGDX — визуал 1:1 с {@code DialogBoxRenderer} (Swing).
 */
public final class GdxDialogBoxRenderer {

    private GdxDialogBoxRenderer() {
    }

    public static void drawBox(ShapeRenderer shapes, SwingCoords C,
                               IntroDialogLayout.Layout layout, float alpha) {
        drawBoxAt(shapes, C, layout.boxX, layout.boxY, layout.boxW, layout.boxH, alpha);
    }

    public static void drawBoxAt(ShapeRenderer shapes, SwingCoords C,
                                 int bx, int by, int bw, int bh, float alpha) {
        float a = alpha;

        beginShapes(shapes);
        fillRect(shapes, C, bx, by, bw, bh,
            c(IntroDialogTheme.BOX_BG_R, IntroDialogTheme.BOX_BG_G, IntroDialogTheme.BOX_BG_B),
            a * IntroDialogTheme.BOX_FILL_ALPHA_MUL * (IntroDialogTheme.BOX_BG_A / 255f));

        int gradH = Math.max(1, bh / 3);
        fillRect(shapes, C, bx, by, bw, gradH,
            c(IntroDialogTheme.GRADIENT_TOP_R, IntroDialogTheme.GRADIENT_TOP_G, IntroDialogTheme.GRADIENT_TOP_B),
            a * (IntroDialogTheme.GRADIENT_TOP_A / 255f));

        int o = IntroDialogTheme.FRAME_OUTER_OFFSET;
        int ft = IntroDialogTheme.FRAME_INNER_THICKNESS;
        int di = IntroDialogTheme.FRAME_DARK_INSET;
        int cs = IntroDialogTheme.CORNER_SIZE;

        Color outer = c(IntroDialogTheme.FRAME_OUTER_R, IntroDialogTheme.FRAME_OUTER_G,
            IntroDialogTheme.FRAME_OUTER_B, a);
        fillRect(shapes, C, bx - o, by - o, bw + o * 2, 4, outer);
        fillRect(shapes, C, bx - o, by + bh - o, bw + o * 2, 4, outer);
        fillRect(shapes, C, bx - o, by - o, 4, bh + o * 2, outer);
        fillRect(shapes, C, bx + bw - o, by - o, 4, bh + o * 2, outer);

        Color inner = c(IntroDialogTheme.FRAME_INNER_R, IntroDialogTheme.FRAME_INNER_G,
            IntroDialogTheme.FRAME_INNER_B, a);
        fillRect(shapes, C, bx, by, bw, ft, inner);
        fillRect(shapes, C, bx, by + bh - ft, bw, ft, inner);
        fillRect(shapes, C, bx, by, ft, bh, inner);
        fillRect(shapes, C, bx + bw - ft, by, ft, bh, inner);

        Color dark = c(IntroDialogTheme.FRAME_DARK_R, IntroDialogTheme.FRAME_DARK_G,
            IntroDialogTheme.FRAME_DARK_B, a * (IntroDialogTheme.FRAME_DARK_A / 255f));
        fillRect(shapes, C, bx + di, by + di, bw - di * 2, 1, dark);
        fillRect(shapes, C, bx + di, by + bh - di - 1, bw - di * 2, 1, dark);
        fillRect(shapes, C, bx + di, by + di, 1, bh - di * 2, dark);
        fillRect(shapes, C, bx + bw - di - 1, by + di, 1, bh - di * 2, dark);

        Color corner = c(IntroDialogTheme.CORNER_GOLD_R, IntroDialogTheme.CORNER_GOLD_G,
            IntroDialogTheme.CORNER_GOLD_B, a * (IntroDialogTheme.CORNER_GOLD_A / 255f));
        fillRect(shapes, C, bx - o, by - o, cs, 2, corner);
        fillRect(shapes, C, bx - o, by - o, 2, cs, corner);
        fillRect(shapes, C, bx + bw - cs + o, by - o, cs, 2, corner);
        fillRect(shapes, C, bx + bw, by - o, 2, cs, corner);
        fillRect(shapes, C, bx - o, by + bh, cs, 2, corner);
        fillRect(shapes, C, bx - o, by + bh - cs + o, 2, cs, corner);
        fillRect(shapes, C, bx + bw - cs + o, by + bh, cs, 2, corner);
        fillRect(shapes, C, bx + bw, by + bh - cs + o, 2, cs, corner);

        shapes.end();

        beginLines(shapes);
        strokeRect(shapes, C, bx + 1, by + 1, bw - 2, bh - 2,
            c(IntroDialogTheme.INNER_STROKE_R, IntroDialogTheme.INNER_STROKE_G,
                IntroDialogTheme.INNER_STROKE_B, a * IntroDialogTheme.INNER_STROKE_ALPHA_1));
        strokeRect(shapes, C, bx + 2, by + 2, bw - 4, bh - 4,
            c(IntroDialogTheme.INNER_STROKE_R, IntroDialogTheme.INNER_STROKE_G,
                IntroDialogTheme.INNER_STROKE_B, a * IntroDialogTheme.INNER_STROKE_ALPHA_2));
        strokeRect(shapes, C, bx + 3, by + 3, bw - 6, bh - 6,
            c(IntroDialogTheme.INNER_STROKE_R, IntroDialogTheme.INNER_STROKE_G,
                IntroDialogTheme.INNER_STROKE_B, a * IntroDialogTheme.INNER_STROKE_ALPHA_3));
        shapes.end();
        PixelTextures.resetBlend();
    }

    public static void drawSpeakerPlate(ShapeRenderer shapes, SwingCoords C,
                                        IntroDialogLayout.Layout layout, String speaker,
                                        GlyphLayout glyph, BitmapFont nameFont, float alpha) {
        if (speaker == null || speaker.isEmpty()) {
            return;
        }
        glyph.setText(nameFont, speaker);
        float nameW = glyph.width;
        float nameH = glyph.height;
        IntroSpeakerPlateLayout.Plate plate = IntroSpeakerPlateLayout.compute(
            layout.boxX, layout.boxY, layout.pad, Math.round(nameW), Math.round(nameH),
            Math.round(nameFont.getCapHeight()));

        beginShapes(shapes);
        fillRect(shapes, C, plate.boxX, plate.boxY, plate.boxW, plate.boxH,
            c(IntroDialogTheme.BOX_BG_R, IntroDialogTheme.BOX_BG_G, IntroDialogTheme.BOX_BG_B),
            alpha * 0.9f * (IntroDialogTheme.BOX_BG_A / 255f));
        shapes.end();

        beginLines(shapes);
        strokeRect(shapes, C, plate.boxX, plate.boxY, plate.boxW, plate.boxH,
            c(IntroDialogTheme.BOX_BORDER_R, IntroDialogTheme.BOX_BORDER_G,
                IntroDialogTheme.BOX_BORDER_B, alpha));
        shapes.end();
        PixelTextures.resetBlend();
    }

    public static void drawFrame(ShapeRenderer shapes, SwingCoords C, GameFonts fonts,
                                 GlyphLayout glyph, IntroDialogLayout.Layout layout,
                                 IntroScript.DialogEntry entry, float alpha) {
        drawBox(shapes, C, layout, alpha);
        if (entry != null && entry.speaker() != null && !entry.speaker().isEmpty()) {
            BitmapFont nameFont = fonts.dialogBoldAt(layout.fontSize);
            nameFont.getData().setScale(1f);
            drawSpeakerPlate(shapes, C, layout, entry.speaker(), glyph, nameFont, alpha);
        }
    }

    public static void drawText(SpriteBatch batch, GameFonts fonts, GlyphLayout glyph,
                                SwingCoords C, IntroController controller,
                                IntroDialogLayout.Layout layout, float alpha) {
        IntroScript.DialogEntry entry = controller.getCurrentDialogEntry();
        if (entry == null) {
            return;
        }

        BitmapFont bodyFont = fonts.dialogAt(layout.fontSize);
        BitmapFont nameFont = fonts.dialogBoldAt(layout.fontSize);
        bodyFont.getData().setScale(1f);
        nameFont.getData().setScale(1f);

        batch.setColor(1f, 1f, 1f, 1f);
        if (entry.speaker() != null && !entry.speaker().isEmpty()) {
            drawSpeakerName(batch, glyph, nameFont, C, layout, entry.speaker(),
                speakerColor(entry), alpha);
        }

        String visible = entry.text().substring(0,
            Math.min(controller.getCharIndex(), entry.text().length()));
        Color bodyCol = bodyColor(entry, alpha);
        float lineY = layout.textY;
        float lineH = layout.fontSize + 4f;
        List<String> lines = IntroDialogText.buildVisibleLines(visible, layout.textMaxW, layout.fontSize);
        for (String line : lines) {
            lineY += lineH;
            if (lineY > layout.boxY + layout.boxH - layout.pad) {
                break;
            }
            float baseline = IntroTextLayout.dialogLineBaselineSwingY(lineY, bodyFont.getCapHeight());
            drawShadowed(batch, bodyFont, line, layout.textX, baseline, C, bodyCol);
        }

        if (controller.isWaitingForAdvance()) {
            String hint = controller.isAutoMode() ? "Авто ▶" : "▶ Enter";
            BitmapFont hintFont = fonts.dialogBoldAt(Math.max(10, layout.fontSize - 2));
            hintFont.getData().setScale(1f);
            Color hintColor = c(IntroDialogTheme.HINT_R, IntroDialogTheme.HINT_G,
                IntroDialogTheme.HINT_B, alpha * (IntroDialogTheme.HINT_A / 255f));
            glyph.setText(hintFont, hint);
            float hintX = layout.boxX + layout.boxW - layout.pad - glyph.width;
            float hintY = layout.boxY + layout.boxH - layout.pad + 2f;
            float baseline = IntroTextLayout.dialogLineBaselineSwingY(hintY, hintFont.getCapHeight());
            drawShadowed(batch, hintFont, hint, hintX, baseline, C, hintColor);
        }
    }

    /** Полный проход (frame + text) — для отладки. */
    public static void drawDialog(SpriteBatch batch, ShapeRenderer shapes, GameFonts fonts,
                                  GlyphLayout glyph, SwingCoords C, IntroController controller,
                                  int sw, int sh, float alpha) {
        IntroScript.DialogEntry entry = controller.getCurrentDialogEntry();
        if (entry == null) {
            return;
        }
        IntroDialogLayout.Layout layout = IntroDialogLayout.computeLayout(sw, sh);
        drawFrame(shapes, C, fonts, glyph, layout, entry, alpha);
        batch.setColor(1f, 1f, 1f, 1f);
        drawText(batch, fonts, glyph, C, controller, layout, alpha);
    }

    private static void drawSpeakerName(SpriteBatch batch, GlyphLayout glyph, BitmapFont font,
                                        SwingCoords C, IntroDialogLayout.Layout layout,
                                        String speaker, Color color, float alpha) {
        glyph.setText(font, speaker);
        IntroSpeakerPlateLayout.Plate plate = IntroSpeakerPlateLayout.compute(
            layout.boxX, layout.boxY, layout.pad, Math.round(glyph.width), Math.round(glyph.height),
            Math.round(font.getCapHeight()));
        float baseline = IntroTextLayout.dialogLineBaselineSwingY(plate.textBaselineY, font.getCapHeight());
        drawOutlined(batch, font, speaker, plate.textX, baseline, C, color);
    }

    private static Color speakerColor(IntroScript.DialogEntry entry) {
        int rgb = entry.speaker() == null
            ? IntroDialogTheme.narratorRgb()
            : entry.speakerColorRgb();
        return rgbColor(rgb, 1f);
    }

    private static Color bodyColor(IntroScript.DialogEntry entry, float alpha) {
        int rgb = IntroDialogText.textColorRgb(entry.speaker(), entry.speakerColorRgb());
        if (entry.speaker() == null) {
            rgb = IntroDialogTheme.narratorRgb();
        }
        return rgbColor(rgb, alpha);
    }

    private static void drawOutlined(SpriteBatch batch, BitmapFont font, String text,
                                     float x, float baselineSwingY, SwingCoords C, Color fill) {
        float tx = snapX(x);
        float by = snapY(C.textBaseline(baselineSwingY));
        Color outline = c(IntroDialogTheme.OUTLINE_R, IntroDialogTheme.OUTLINE_G,
            IntroDialogTheme.OUTLINE_B, IntroDialogTheme.OUTLINE_A / 255f);
        font.setColor(outline);
        font.draw(batch, text, tx + 1f, by);
        font.draw(batch, text, tx - 1f, by);
        font.draw(batch, text, tx, by - 1f);
        font.setColor(fill);
        font.draw(batch, text, tx, by);
    }

    private static void drawShadowed(SpriteBatch batch, BitmapFont font, String text,
                                     float x, float baselineSwingY, SwingCoords C, Color fill) {
        float tx = snapX(x);
        float by = snapY(C.textBaseline(baselineSwingY));
        font.setColor(c(IntroDialogTheme.SHADOW_R, IntroDialogTheme.SHADOW_G,
            IntroDialogTheme.SHADOW_B, IntroDialogTheme.SHADOW_A / 255f));
        font.draw(batch, text, tx + 1f, by - 1f);
        font.setColor(fill);
        font.draw(batch, text, tx, by);
    }

    private static float snapX(float x) {
        int ix = Math.round(x);
        if ((ix & 1) == 1) {
            ix++;
        }
        return ix;
    }

    private static float snapY(float y) {
        int iy = Math.round(y);
        if ((iy & 1) == 1) {
            iy++;
        }
        return iy;
    }

    private static Color rgbColor(int rgb, float alpha) {
        return c((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, alpha);
    }

    private static Color c(int r, int g, int b) {
        return c(r, g, b, 1f);
    }

    private static Color c(int r, int g, int b, float a) {
        return new Color(r / 255f, g / 255f, b / 255f, a);
    }

    private static void beginShapes(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
    }

    private static void beginLines(ShapeRenderer shapes) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Line);
    }

    private static void fillRect(ShapeRenderer shapes, SwingCoords C,
                                 int swingX, int swingTopY, int w, int h, Color color) {
        shapes.setColor(color);
        shapes.rect(swingX, C.rectY(swingTopY, h), w, h);
    }

    private static void fillRect(ShapeRenderer shapes, SwingCoords C,
                                 int swingX, int swingTopY, int w, int h,
                                 Color rgb, float alpha) {
        shapes.setColor(rgb.r, rgb.g, rgb.b, alpha);
        shapes.rect(swingX, C.rectY(swingTopY, h), w, h);
    }

    private static void strokeRect(ShapeRenderer shapes, SwingCoords C,
                                   int swingX, int swingTopY, int w, int h, Color color) {
        shapes.setColor(color);
        float x = swingX + 0.5f;
        float y = C.rectY(swingTopY, h) + 0.5f;
        shapes.rect(x, y, w - 1f, h - 1f);
    }
}
