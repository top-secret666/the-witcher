package main.java.com.witcher.gdx.intro;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.GdxIntroAssets;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.gdx.graphics.SwingCoords;
import main.java.com.witcher.ui.intro.IntroAssetsInfo;
import main.java.com.witcher.ui.intro.IntroDialogText;
import main.java.com.witcher.ui.intro.IntroEasing;
import main.java.com.witcher.ui.intro.IntroMorphAnimation;
import main.java.com.witcher.ui.intro.IntroScript;
import main.java.com.witcher.ui.intro.IntroTheme;
import main.java.com.witcher.ui.intro.presenter.IntroController;
import main.java.com.witcher.ui.intro.view.IntroCharacterLayout;
import main.java.com.witcher.ui.intro.view.IntroDialogLayout;
import main.java.com.witcher.ui.intro.view.IntroLayout;
import main.java.com.witcher.ui.intro.view.IntroTextLayout;

import java.util.List;

/**
 * Отрисовка интро-сцены LibGDX по состоянию {@link IntroController}.
 */
public final class GdxIntroView {

    private static final float DIALOG_FONT_BASE = 12f;

    private final GlyphLayout glyph = new GlyphLayout();

    public void render(SpriteBatch batch, ShapeRenderer shapes, GameFonts fonts,
                       GdxIntroAssets assets, IntroController controller,
                       SwingCoords C, int sw, int sh, int mouseX, int mouseY) {
        float fade = controller.getFadeAlpha();
        IntroAssetsInfo info = controller.getAssets();

        batch.begin();
        drawBackground(batch, assets, controller, sw, sh, fade);
        batch.end();

        if (controller.isRightMorphActive()) {
            drawMorphAura(shapes, controller, C, fade);
        }
        drawMorphParticles(shapes, controller, C, fade);
        drawSwitchParticles(shapes, controller, C, fade);

        batch.begin();
        drawCharacters(batch, assets, info, controller, C, sw, sh, fade);
        batch.end();

        if (controller.shouldShowDialogBox()) {
            drawDialogBoxShapes(shapes, controller, C, sw, sh, fade);
        }
        if (controller.isHistoryOpen()) {
            drawHistoryOverlayShapes(shapes, controller, C, sw, sh, fade);
        }

        batch.begin();
        if (controller.shouldShowDialogBox()) {
            drawDialogBoxText(batch, fonts, controller, C, sw, sh, fade);
        }
        if (controller.shouldShowVnButtons()) {
            drawVnButtons(batch, fonts.dialog, controller, C, sh, mouseX, mouseY, fade);
        }
        if (controller.isHistoryOpen()) {
            drawHistoryOverlayText(batch, fonts, controller, C, fade);
        }
        drawCursor(batch, assets, C, mouseX, mouseY);
        batch.end();

        if (controller.isRightMorphActive()) {
            drawMorphGoldenBurst(shapes, controller, C, fade);
        }
    }

    private void drawBackground(SpriteBatch batch, GdxIntroAssets assets,
                                IntroController controller, int sw, int sh, float fade) {
        if (assets.kaerMorhenBgTex != null) {
            PixelTextures.drawCover(batch, assets.kaerMorhenBgTex, sw, sh, fade * IntroLayout.BG_FADE_MUL);
        }

        float shopReveal = controller.getShopReveal();
        if (shopReveal > 0.001f) {
            Texture shopFrame = null;
            if (controller.isShopAnimationComplete() && assets.merchantBgTex != null) {
                shopFrame = assets.merchantBgTex;
            } else if (assets.shopMaterializeFrames != null && assets.shopMaterializeFrames.length > 0) {
                int idx = Math.max(0, Math.min(controller.getShopFrameIndex(),
                    assets.shopMaterializeFrames.length - 1));
                shopFrame = assets.shopMaterializeFrames[idx];
            } else {
                shopFrame = assets.merchantBgTex;
            }
            if (shopFrame != null) {
                float alpha = controller.isShopAnimationComplete()
                    ? fade
                    : Math.min(1f, 0.55f + shopReveal * 0.45f);
                PixelTextures.drawCover(batch, shopFrame, sw, sh, alpha);
            }
        }
    }

    private void drawCharacters(SpriteBatch batch, GdxIntroAssets assets, IntroAssetsInfo info,
                                IntroController controller, SwingCoords C, int sw, int sh, float fade) {
        if (controller.shouldHideCharactersForShopScene()) {
            return;
        }

        IntroScript.DialogEntry entry = controller.getCurrentDialogEntry();
        String activeSide = entry != null ? entry.activeSide() : "none";
        boolean usingShop = controller.isUsingShopSprites();
        int tick = controller.getTick();

        Texture geraltBase = pick(usingShop, assets.geraltShopTex, assets.geraltTex);
        Texture geraltEmotion = pick(usingShop, assets.geraltEmotionShopTex, assets.geraltEmotionTex);
        Texture leftTex = ("left".equals(activeSide) && geraltEmotion != null) ? geraltEmotion : geraltBase;
        boolean leftForce = controller.isLeftForceOpaque();
        drawCharacter(batch, assets, info, C, leftTex, controller.getGeraltSlide(), true,
            "left".equals(activeSide), controller.getLeftActiveAnim(),
            leftForce, false, false, fade, tick, sw, sh);

        if (controller.isRightMorphActive()) {
            drawMorphCharacters(batch, assets, info, controller, C, sw, sh, fade, activeSide);
        } else {
            if (controller.getStrangerSlide() > 0.001f && assets.strangerTex != null) {
                drawCharacter(batch, assets, info, C, assets.strangerTex, controller.getStrangerSlide(), false,
                    "right".equals(activeSide) && "stranger".equals(controller.getRightCharacter()),
                    controller.getRightActiveAnim(), false, false, false, fade, tick, sw, sh);
            }
            if (controller.getDukeSlide() > 0.001f) {
                Texture dukeBase = pick(usingShop, assets.dukeShopTex, assets.dukeTex);
                Texture dukeEmotion = pick(usingShop, assets.dukeLaughShopTex, assets.dukeLaughTex);
                Texture rightTex = ("right".equals(activeSide) && dukeEmotion != null) ? dukeEmotion : dukeBase;
                drawCharacter(batch, assets, info, C, rightTex, controller.getDukeSlide(), false,
                    "right".equals(activeSide) && "duke".equals(controller.getRightCharacter()),
                    controller.getRightActiveAnim(), controller.isRightForceOpaque(),
                    controller.shouldLiftDukeForShop(), controller.shouldRaiseDukeForShop(),
                    fade, tick, sw, sh);
            }
        }
    }

    private void drawMorphCharacters(SpriteBatch batch, GdxIntroAssets assets, IntroAssetsInfo info,
                                     IntroController controller, SwingCoords C, int sw, int sh,
                                     float fade, String activeSide) {
        float t = IntroEasing.easeInOutCubic(controller.getRightMorphT());
        boolean usingShop = controller.isUsingShopSprites();

        float dissolve = 1f - IntroEasing.smoothstep(0f, 0.58f, t);
        float manifest = IntroEasing.smoothstep(0.36f, 1f, t);

        int[] strangerSize = logicalSize(assets.strangerTex, assets, info);
        if (assets.strangerTex != null && dissolve > 0.02f) {
            IntroCharacterLayout.Rect strangerRect = IntroCharacterLayout.computeCharacterRect(
                sw, sh, strangerSize[0], strangerSize[1],
                1f, false, false, 0f, controller.getTick(), false, false);
            drawSpriteAt(batch, C, assets.strangerTex, strangerRect, fade * dissolve);
        }

        Texture dukeBase = pick(usingShop, assets.dukeShopTex, assets.dukeTex);
        if (dukeBase != null && manifest > 0.02f) {
            boolean dukeActive = "right".equals(activeSide);
            int[] dukeSize = logicalSize(dukeBase, assets, info);
            IntroCharacterLayout.Rect dukeRect = IntroCharacterLayout.computeCharacterRect(
                sw, sh, dukeSize[0], dukeSize[1],
                1f, false, dukeActive, controller.getRightActiveAnim(),
                controller.getTick(), false, false);
            drawSpriteAt(batch, C, dukeBase, dukeRect, fade * manifest);
            if (controller.isRightForceOpaque() && dukeActive && manifest > 0.85f) {
                drawSpriteAt(batch, C, dukeBase, dukeRect, fade * manifest);
            }
        }
    }

    private void drawCharacter(SpriteBatch batch, GdxIntroAssets assets, IntroAssetsInfo info,
                               SwingCoords C, Texture sprite, float slide,
                               boolean isLeft, boolean isActive, float activeAnim,
                               boolean forceOpaque, boolean liftForShop, boolean raiseAbove,
                               float fadeAlpha, int tick, int sw, int sh) {
        if (sprite == null || slide <= 0.001f) {
            return;
        }
        int[] size = logicalSize(sprite, assets, info);
        IntroCharacterLayout.Rect rect = IntroCharacterLayout.computeCharacterRect(
            sw, sh, size[0], size[1], slide, isLeft, isActive, activeAnim,
            tick, liftForShop, raiseAbove);
        float alpha = fadeAlpha * Math.min(1f, 0.2f + slide * 0.9f);
        if (forceOpaque && isActive) {
            alpha = fadeAlpha;
        }
        drawSpriteAt(batch, C, sprite, rect, alpha);
    }

    private void drawSpriteAt(SpriteBatch batch, SwingCoords C, Texture sprite,
                              IntroCharacterLayout.Rect rect, float alpha) {
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(sprite, rect.x, C.rectY(rect.y, rect.height), rect.width, rect.height);
        batch.setColor(1f, 1f, 1f, 1f);
    }

    private void drawMorphAura(ShapeRenderer shapes, IntroController controller,
                               SwingCoords C, float fade) {
        IntroMorphAnimation.IntroRect anchor = controller.getMorphAnchorBounds();
        if (anchor == null) {
            return;
        }
        float t = controller.getRightMorphT();
        float peak = (float) Math.sin(t * Math.PI);
        if (peak <= 0.08f) {
            return;
        }
        float cx = anchor.x + anchor.width * 0.5f;
        float cy = C.centerY(anchor.y + anchor.height * 0.5f, 0f);
        float w = anchor.width * 1.05f;
        float h = anchor.height * 0.72f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.16f, 0.1f, 0.06f, peak * 0.22f * fade);
        shapes.ellipse(cx - w * 0.5f, cy - h * 0.5f, w, h);
        shapes.setColor(0.55f, 0.38f, 0.12f, peak * 0.14f * fade);
        shapes.ellipse(cx - w * 0.42f, cy - h * 0.38f, w * 0.84f, h * 0.76f);
        shapes.end();
        PixelTextures.resetBlend();
    }

    private void drawMorphGoldenBurst(ShapeRenderer shapes, IntroController controller,
                                      SwingCoords C, float fade) {
        IntroMorphAnimation.IntroRect anchor = controller.getMorphAnchorBounds();
        if (anchor == null) {
            return;
        }
        float morphT = controller.getRightMorphT();
        float peak = (float) Math.sin(morphT * Math.PI);
        if (peak <= 0.35f) {
            return;
        }
        float burst = (peak - 0.35f) / 0.65f;
        float cx = anchor.x + anchor.width * 0.5f;
        float cy = C.centerY(anchor.y + anchor.height * 0.5f, 0f);
        float flashW = anchor.width * 1.1f;
        float flashH = anchor.height * 0.55f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(1f, 0.88f, 0.47f, burst * 0.18f * fade);
        shapes.ellipse(cx - flashW * 0.5f, cy - flashH * 0.5f, flashW, flashH);
        shapes.setColor(0.16f, 0.11f, 0.07f, burst * 0.12f * fade);
        shapes.ellipse(cx - flashW * 0.5f, cy - flashH * 0.25f, flashW, flashH);
        shapes.end();
        PixelTextures.resetBlend();
    }

    private void drawMorphParticles(ShapeRenderer shapes, IntroController controller,
                                    SwingCoords C, float fade) {
        if (!controller.isRightMorphActive()) {
            return;
        }
        float morphT = controller.getRightMorphT();
        float peak = (float) Math.sin(morphT * Math.PI);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (float[] p : controller.getMorphSmoke()) {
            float life = p[5] / p[6];
            float alpha = (1f - life * 0.85f) * fade * (0.48f + peak * 0.52f);
            if (alpha <= 0.01f) {
                continue;
            }
            float sz = Math.max(2f, p[4]);
            float cx = p[0];
            float cy = C.centerY(p[1], sz);
            shapes.setColor(p[7] / 255f, p[8] / 255f, p[9] / 255f, alpha * 0.35f);
            shapes.circle(cx, cy, sz * 0.5f + 1f);
            shapes.setColor(p[7] / 255f, p[8] / 255f, p[9] / 255f, alpha);
            shapes.circle(cx, cy, Math.max(0.5f, sz * 0.5f));
        }

        for (float[] p : controller.getMorphSparks()) {
            float life = 1f - p[4] / p[5];
            float alpha = life * fade * (0.55f + peak * 0.45f);
            if (alpha <= 0.02f) {
                continue;
            }
            float sz = Math.max(1f, p[6] * (0.6f + life * 0.5f));
            float cx = p[0];
            float cy = C.centerY(p[1], sz);
            shapes.setColor(1f, 0.75f, 0.16f, alpha * 0.28f);
            shapes.circle(cx, cy, sz + 2f);
            shapes.setColor(1f, 0.86f, 0.31f, alpha);
            shapes.circle(cx, cy, sz * 0.5f);
        }
        shapes.end();
        PixelTextures.resetBlend();
    }

    private void drawSwitchParticles(ShapeRenderer shapes, IntroController controller,
                                     SwingCoords C, float fade) {
        float switchFlash = controller.getSwitchFlash();
        if (switchFlash <= 0.01f && controller.getSwitchParticles().isEmpty()) {
            return;
        }
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        for (float[] p : controller.getSwitchParticles()) {
            float life = p[4] / p[5];
            float a = (1f - life) * fade;
            float sz = life < 0.3f ? 3f : (life < 0.6f ? 2f : 1f);
            shapes.setColor(p[6] / 255f, p[7] / 255f, p[8] / 255f, a * 0.86f);
            shapes.circle(p[0], C.centerY(p[1], sz), sz * 0.5f);
        }

        IntroCharacterLayout.Rect bounds = controller.getRightCharacterBounds();
        if (switchFlash > 0.01f && bounds.width > 0 && !controller.isRightForceOpaque()) {
            float cx = bounds.x + bounds.width * 0.5f;
            float cy = C.centerY(bounds.y + bounds.height * 0.5f, 0f);
            for (float[] rp : controller.getRightSwitchParticles()) {
                float life = 1f - rp[4] / rp[5];
                float alpha = life * switchFlash * 0.9f;
                float s = Math.max(2f, 3f + (0.5f - life) * 4f);
                shapes.setColor(1f, 0.78f, 0.08f, alpha);
                shapes.circle(rp[0], C.centerY(rp[1], s), s * 0.5f);
            }
            shapes.setColor(1f, 0.84f, 0f, switchFlash * 0.25f * fade);
            shapes.circle(cx, cy, bounds.width * 0.65f);
        }
        shapes.end();
        PixelTextures.resetBlend();
    }

    private void drawDialogBoxShapes(ShapeRenderer shapes, IntroController controller,
                                     SwingCoords C, int sw, int sh, float fade) {
        if (controller.getCurrentDialogEntry() == null) {
            return;
        }
        IntroDialogLayout.Layout layout = IntroDialogLayout.computeLayout(sw, sh);
        float boxAlpha = fade * 0.92f;
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.06f, 0.05f, 0.04f, boxAlpha * 0.88f);
        shapes.rect(layout.boxX, C.rectY(layout.boxY, layout.boxH), layout.boxW, layout.boxH);
        shapes.setColor(0.45f, 0.35f, 0.18f, boxAlpha * 0.6f);
        shapes.rect(layout.boxX, C.rectY(layout.boxY, 2), layout.boxW, 2);
        shapes.end();
        PixelTextures.resetBlend();
    }

    private void drawDialogBoxText(SpriteBatch batch, GameFonts fonts, IntroController controller,
                                   SwingCoords C, int sw, int sh, float fade) {
        IntroScript.DialogEntry entry = controller.getCurrentDialogEntry();
        if (entry == null) {
            return;
        }
        IntroDialogLayout.Layout layout = IntroDialogLayout.computeLayout(sw, sh);
        BitmapFont font = fonts.dialog;
        float scale = IntroTextLayout.dialogFontScale(layout.fontSize, DIALOG_FONT_BASE);
        font.getData().setScale(scale);

        String visible = entry.text().substring(0,
            Math.min(controller.getCharIndex(), entry.text().length()));
        Color speakerColor = speakerGdxColor(entry);
        font.setColor(speakerColor.r, speakerColor.g, speakerColor.b, fade);

        float lineY = layout.textY;
        if (entry.speaker() != null) {
            float baseline = IntroTextLayout.dialogLineBaselineSwingY(lineY, font.getCapHeight());
            font.draw(batch, entry.speaker(), layout.textX, C.textBaseline(baseline));
            lineY += layout.fontSize + 4;
        }
        List<String> lines = IntroDialogText.buildVisibleLines(visible, layout.textMaxW, layout.fontSize);
        for (String line : lines) {
            float baseline = IntroTextLayout.dialogLineBaselineSwingY(lineY, font.getCapHeight());
            font.draw(batch, line, layout.textX, C.textBaseline(baseline));
            lineY += IntroDialogText.lineHeight(layout.fontSize);
        }

        if (controller.isWaitingForAdvance()) {
            String hint = controller.isAutoMode() ? "Авто ▶" : "▶ Enter";
            font.setColor(speakerColor.r, speakerColor.g, speakerColor.b, fade * 0.85f);
            float hintY = layout.boxY + layout.boxH - layout.pad;
            float baseline = IntroTextLayout.dialogLineBaselineSwingY(hintY, font.getCapHeight());
            font.draw(batch, hint, layout.textX + layout.textMaxW - 60f, C.textBaseline(baseline));
        }
        font.getData().setScale(1f);
    }

    private void drawVnButtons(SpriteBatch batch, BitmapFont font, IntroController controller,
                               SwingCoords C, int sh, int mouseX, int mouseY, float fade) {
        float vnSize = IntroTextLayout.vnFontSize(sh);
        float scale = IntroTextLayout.dialogFontScale(Math.round(vnSize), DIALOG_FONT_BASE);
        font.getData().setScale(scale);
        drawVnButton(batch, font, controller.getBackButtonBounds(), "Назад",
            controller.isBackEnabled(), false,
            controller.isBackEnabled() && controller.getBackButtonBounds().contains(mouseX, mouseY),
            C, fade);
        drawVnButton(batch, font, controller.getHistoryButtonBounds(), "История",
            true, false,
            controller.getHistoryButtonBounds().contains(mouseX, mouseY), C, fade);
        drawVnButton(batch, font, controller.getAutoButtonBounds(), "Авто",
            true, controller.isAutoMode(),
            controller.getAutoButtonBounds().contains(mouseX, mouseY), C, fade);
        font.getData().setScale(1f);
    }

    private void drawVnButton(SpriteBatch batch, BitmapFont font,
                              IntroController.IntroRect r, String label,
                              boolean enabled, boolean active, boolean hover,
                              SwingCoords C, float fade) {
        float alpha = fade * (enabled ? 1f : 0.5f);
        if (!enabled) {
            font.setColor(0.37f, 0.31f, 0.23f, alpha);
        } else if (active) {
            font.setColor(1f, 0.88f, 0.51f, alpha);
        } else if (hover) {
            font.setColor(1f, 0.92f, 0.67f, alpha);
        } else {
            font.setColor(0.8f, 0.71f, 0.45f, alpha);
        }
        glyph.setText(font, label);
        float tx = r.x + (r.width - glyph.width) * 0.5f;
        float baselineY = IntroTextLayout.vnLabelBaselineSwingY(toVnRect(r), font.getCapHeight());
        font.draw(batch, label, tx, C.textBaseline(baselineY));
    }

    private void drawHistoryOverlayShapes(ShapeRenderer shapes, IntroController controller,
                                          SwingCoords C, int sw, int sh, float fade) {
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, fade * 0.62f);
        shapes.rect(0f, 0f, sw, sh);
        IntroController.IntroRect panel = controller.getHistoryPanelBounds();
        shapes.setColor(0.08f, 0.06f, 0.04f, fade * 0.95f);
        shapes.rect(panel.x, C.rectY(panel.y, panel.height), panel.width, panel.height);
        shapes.end();
        PixelTextures.resetBlend();
    }

    private void drawHistoryOverlayText(SpriteBatch batch, GameFonts fonts, IntroController controller,
                                        SwingCoords C, float fade) {
        IntroController.IntroRect panel = controller.getHistoryPanelBounds();
        BitmapFont font = fonts.dialog;
        font.setColor(0.85f, 0.65f, 0.12f, fade);
        font.draw(batch, "История", panel.x + 12f, C.textBaseline(panel.y + 20f));

        float lineY = panel.y + 36f;
        for (String line : controller.buildHistoryLogLines()) {
            boolean speaker = line.startsWith("[") && line.endsWith("]");
            if (speaker) {
                font.setColor(0.71f, 0.59f, 0.35f, fade);
            } else {
                font.setColor(0.82f, 0.76f, 0.61f, fade);
            }
            font.draw(batch, line, panel.x + 12f, C.textBaseline(lineY));
            lineY += 16f;
        }
    }

    private void drawCursor(SpriteBatch batch, GdxIntroAssets assets, SwingCoords C,
                            int mouseX, int mouseY) {
        if (assets.cursorTex == null) {
            return;
        }
        float cw = IntroLayout.CURSOR_W;
        float ch = cw * assets.cursorTex.getHeight() / assets.cursorTex.getWidth();
        float topY = mouseY - IntroLayout.CURSOR_HOTSPOT_Y;
        batch.draw(assets.cursorTex, mouseX - IntroLayout.CURSOR_HOTSPOT_X,
            C.rectY(topY, ch), cw, ch);
    }

    private static int[] logicalSize(Texture tex, GdxIntroAssets assets, IntroAssetsInfo info) {
        if (tex == null) {
            return new int[]{0, 0};
        }
        if (info != null) {
            if (tex == assets.strangerTex) {
                return new int[]{info.strangerW, info.strangerH};
            }
            if (tex == assets.geraltTex || tex == assets.geraltEmotionTex) {
                return new int[]{info.geraltW, info.geraltH};
            }
            if (tex == assets.dukeTex || tex == assets.dukeLaughTex) {
                return new int[]{info.dukeW, info.dukeH};
            }
        }
        return new int[]{tex.getWidth(), tex.getHeight()};
    }

    private static Color speakerGdxColor(IntroScript.DialogEntry entry) {
        int rgb = entry.speaker() == null ? IntroTheme.narratorRgb() : entry.speakerColorRgb();
        float r = ((rgb >> 16) & 0xFF) / 255f;
        float g = ((rgb >> 8) & 0xFF) / 255f;
        float b = (rgb & 0xFF) / 255f;
        return new Color(r, g, b, 1f);
    }

    private static Texture pick(boolean shop, Texture shopTex, Texture normalTex) {
        return shop && shopTex != null ? shopTex : normalTex;
    }

    private static main.java.com.witcher.ui.intro.IntroVnUi.Rect toVnRect(IntroController.IntroRect r) {
        main.java.com.witcher.ui.intro.IntroVnUi.Rect out = new main.java.com.witcher.ui.intro.IntroVnUi.Rect();
        out.x = r.x;
        out.y = r.y;
        out.width = r.width;
        out.height = r.height;
        return out;
    }
}
