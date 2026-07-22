package main.java.com.witcher.ui.graphics;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Paths;
import main.java.com.witcher.ui.intro.IntroEasing;
import main.java.com.witcher.ui.intro.IntroMorphAnimation;
import main.java.com.witcher.ui.intro.IntroScript;
import main.java.com.witcher.ui.intro.IntroSwingBridge;
import main.java.com.witcher.ui.intro.IntroTheme;
import main.java.com.witcher.ui.intro.presenter.IntroController;
import main.java.com.witcher.ui.intro.IntroHistoryText;
import main.java.com.witcher.ui.intro.view.IntroCharacterLayout;
import main.java.com.witcher.ui.intro.view.IntroHistoryLayout;
import main.java.com.witcher.ui.intro.view.IntroHistoryTheme;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Пиксельная заставка-интро в стиле визуальной новеллы.
 * Персонажи появляются слева/справа, активный подсвечен,
 * текст печатается посимвольно (typewriter).
 */
public class IntroScreen {

    private final IntroController controller;

    // ─── Персонажи (спрайты) ───
    private final BufferedImage geraltSprite;
    private final BufferedImage dukeSprite;
    private final BufferedImage strangerSprite;
    private final BufferedImage geraltEmotionSprite;
    private final BufferedImage dukeLaughSprite;
    // Спрайты для сцены лавки (альтернативные варианты)
    private final BufferedImage geraltShopSprite;
    private final BufferedImage dukeShopSprite;
    private final BufferedImage geraltEmotionShopSprite;
    private final BufferedImage dukeLaughShopSprite;

    // Фон: кадры GIF (декодированы вручную) или статичный фон
    private final BufferedImage[] bgFrames;  // null если GIF не загрузился
    private final int[] bgFrameDelays;       // задержки кадров в мс
    private final BufferedImage staticBgImg; // fallback если GIF нет
    private final BufferedImage merchantBgImg;
    private final BufferedImage[] shopMaterializeFrames;
    private final int[] shopMaterializeDelays;
    private int bgFrameIndex = 0;
    private long bgLastFrameTime = System.currentTimeMillis();

    private static final BufferedImage MENU_CURSOR = loadMenuCursor();

    public IntroScreen() {
        // ─── Загрузка спрайтов ───
        geraltSprite = loadTrimmed("/assets/sprites/screen saver/geralt_portrait.png");
        dukeSprite = loadTrimmed("/assets/sprites/screen saver/duke_portrait.png");
        strangerSprite = loadTrimmed("/assets/sprites/screen saver/stranger_shadow.png");

        // Эмоции для диалогов
        geraltEmotionSprite = loadTrimmed("/assets/sprites/screen saver/geralt_emotion.png");
        dukeLaughSprite = loadTrimmed("/assets/sprites/screen saver/duke_portrait_fun.png");

        // Попытка загрузить альтернативные спрайты для сцены лавки (фоллбэки к основным)
        geraltShopSprite = loadTrimmed("/assets/sprites/lavka/geralt_portrait_shop.png");
        dukeShopSprite = loadTrimmed("/assets/sprites/lavka/duke_portrait_shop.png");
        geraltEmotionShopSprite = loadTrimmed("/assets/sprites/lavka/geralt_emotion_shop.png");
        dukeLaughShopSprite = loadTrimmed("/assets/sprites/lavka/duke_portrait_fun_shop.png");

        // ─── Загрузка фона (статичная PNG картинка) ───
        bgFrames = null;
        bgFrameDelays = null;
        Sprite fb = tryLoad("/assets/sprites/screen saver/kaer_morhen_bg.png",
            "/assets/sprites/kaer_morhen_bg.png",
            "/assets/sprites/menu/menu_bg_custom.jpg");
        staticBgImg = fb != null ? fb.getImage() : null;

        GifData shopGif = null;
        // try several likely locations for the shop materialize gif
        String[] gifCandidates = new String[]{
            "/assets/sprites/screen saver/shop_materialize.gif",
            "/assets/sprites/screen saver/59f8bef1-2321-427a-80b3-56655d3e1e4b.gif",
            "/assets/sprites/lavka/shop_materialize.gif",
            "/assets/sprites/lavka/shop_materialize_v2.gif"
        };
        for (String c : gifCandidates) {
            shopGif = loadGifFrames(c);
            if (shopGif != null) break;
        }
        shopMaterializeFrames = shopGif != null ? shopGif.frames : null;
        shopMaterializeDelays = shopGif != null ? shopGif.delays : null;

        Sprite merchantBg = tryLoadOptional(
            "/assets/sprites/lavka/merchant_bg_lavka.png",
            "/assets/sprites/screen saver/lavka.png",
            "/assets/sprites/lavka/lavka.png",
            "/assets/sprites/menu/menu_bg_custom.jpg"
        );
        merchantBgImg = merchantBg != null ? merchantBg.getImage() : null;

        controller = new IntroController(IntroSwingBridge.assetsInfo(
            strangerSprite, geraltSprite, dukeSprite,
            geraltEmotionSprite, dukeLaughSprite,
            geraltShopSprite, dukeShopSprite,
            geraltEmotionShopSprite, dukeLaughShopSprite,
            shopMaterializeFrames, shopMaterializeDelays, merchantBgImg));
    }

    // ─── Обновление ───
    public void update(boolean advanceKey, int mouseX, int mouseY, boolean mouseClicked, int wheelNotches) {
        controller.update(advanceKey, mouseX, mouseY, mouseClicked, wheelNotches);
    }

    // ─── Рендер ───
    public void render(BufferedImage screen, int mouseX, int mouseY) {
        int sw = screen.getWidth();
        int sh = screen.getHeight();

        Graphics2D g = screen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // ── Чёрный фон ──
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        // ── Фон Каэр Морхена (GIF или статичный) ──
        BufferedImage bgImg = null;
        if (bgFrames != null) {
            long now = System.currentTimeMillis();
            int delayMs = bgFrameDelays[bgFrameIndex];
            if (delayMs < 20) delayMs = 80; // защита от нулевых задержек
            if (now - bgLastFrameTime >= delayMs) {
                bgFrameIndex = (bgFrameIndex + 1) % bgFrames.length;
                bgLastFrameTime = now;
            }
            bgImg = bgFrames[bgFrameIndex];
        } else {
            bgImg = staticBgImg;
        }
        if (bgImg != null) {
            drawScaledBackground(g, bgImg, sw, sh, controller.getFadeAlpha() * 0.82f, false);
        }

        float shopReveal = controller.getShopReveal();
        boolean shopAnimationComplete = controller.isShopAnimationComplete();
        boolean hideCharactersForShopScene = controller.shouldHideCharactersForShopScene();

        if (shopReveal > 0.001f) {
            BufferedImage shopFrame = null;
            if (shopAnimationComplete && merchantBgImg != null) {
                shopFrame = merchantBgImg;
            } else if (shopMaterializeFrames != null && shopMaterializeFrames.length > 0) {
                shopFrame = shopMaterializeFrames[Math.max(0, Math.min(
                    controller.getShopFrameIndex(), shopMaterializeFrames.length - 1))];
            } else {
                shopFrame = merchantBgImg;
            }

            if (shopFrame != null) {
                if (shopAnimationComplete) {
                    drawScaledBackground(g, shopFrame, sw, sh, controller.getFadeAlpha(), false);
                } else {
                    float overlayAlpha = 0.55f + shopReveal * 0.45f;
                    drawScaledBackground(g, shopFrame, sw, sh, Math.min(1f, overlayAlpha), false);

                    Composite prev = g.getComposite();
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shopReveal * 0.08f));
                    g.setColor(new Color(255, 195, 110));
                    g.fillRect(0, 0, sw, sh);
                    g.setComposite(prev);
                }
            }
        }

        IntroScript.DialogEntry entry = controller.getCurrentDialogEntry();
        String activeSide = entry != null ? entry.activeSide() : "none";
        boolean leftForceOpaque = controller.isLeftForceOpaque();
        boolean rightForceOpaque = controller.isRightForceOpaque();
        boolean usingShopSprites = controller.isUsingShopSprites();

        if (!hideCharactersForShopScene) {
            BufferedImage geraltBase = usingShopSprites && geraltShopSprite != null ? geraltShopSprite : geraltSprite;
            BufferedImage geraltEmotionBase = usingShopSprites && geraltEmotionShopSprite != null
                ? geraltEmotionShopSprite : geraltEmotionSprite;
            BufferedImage leftSpriteToShow = ("left".equals(activeSide) && geraltEmotionBase != null)
                ? geraltEmotionBase : geraltBase;
            drawCharacterEnhanced(g, sw, sh, leftSpriteToShow, controller.getGeraltSlide(), true,
                "left".equals(activeSide), controller.getLeftActiveAnim(), leftForceOpaque, false, false, 1f);

            if (controller.isRightMorphActive()) {
                drawStrangerToDukeMorph(g, sw, sh, usingShopSprites, activeSide,
                    controller.getRightActiveAnim(), rightForceOpaque);
            } else {
                if (controller.getStrangerSlide() > 0.001f) {
                    drawCharacterEnhanced(g, sw, sh, strangerSprite, controller.getStrangerSlide(), false,
                        "right".equals(activeSide) && "stranger".equals(controller.getRightCharacter()),
                        controller.getRightActiveAnim(), false, false, false, 1f);
                }
                if (controller.getDukeSlide() > 0.001f) {
                    BufferedImage dukeBase = usingShopSprites && dukeShopSprite != null ? dukeShopSprite : dukeSprite;
                    BufferedImage dukeEmotionBase = usingShopSprites && dukeLaughShopSprite != null
                        ? dukeLaughShopSprite : dukeLaughSprite;
                    BufferedImage rightSpriteToShow = ("right".equals(activeSide) && dukeEmotionBase != null)
                        ? dukeEmotionBase : dukeBase;
                    boolean shopPose = controller.shouldLiftDukeForShop();
                    drawCharacterEnhanced(g, sw, sh, rightSpriteToShow, controller.getDukeSlide(), false,
                        "right".equals(activeSide) && "duke".equals(controller.getRightCharacter()),
                        controller.getRightActiveAnim(), rightForceOpaque, shopPose, shopPose, 1f);
                }
            }

            if (!(leftForceOpaque || rightForceOpaque)) {
                for (float[] p : controller.getSwitchParticles()) {
                    float life = p[4] / p[5];
                    float a = (1f - life) * controller.getFadeAlpha();
                    int pr = Math.min(255, (int) p[6]);
                    int pg = Math.min(255, (int) p[7]);
                    int pb = Math.min(255, (int) p[8]);
                    g.setColor(new Color(pr, pg, pb, Math.max(0, Math.min(255, (int) (a * 220)))));
                    int sz = life < 0.3f ? 3 : (life < 0.6f ? 2 : 1);
                    g.fillRect(Math.round(p[0] * sw / 480f), Math.round(p[1] * sh / 360f), sz, sz);
                }
            }
        }

        IntroCharacterLayout.Rect rightBounds = controller.getRightCharacterBounds();
        float switchFlash = controller.getSwitchFlash();
        if (!hideCharactersForShopScene && !controller.isRightMorphActive()
            && switchFlash > 0.01f && rightBounds.width > 0 && !rightForceOpaque) {
            Composite prevF = g.getComposite();
            int cx = rightBounds.x + rightBounds.width / 2;
            int cy = rightBounds.y + rightBounds.height / 2;
            
            for (int i = 0; i < 4; i++) {
                float wavePhase = (switchFlash + i * 0.15f) % 1f;
                float waveRadius = rightBounds.width * 0.6f * (1f + wavePhase * 2.5f);
                float waveAlpha = (1f - wavePhase) * switchFlash * 0.5f;
                
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, waveAlpha)));
                g.setColor(new Color(255, 215, 0, Math.max(0, Math.min(255, (int)(waveAlpha * 200)))));
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int wr = Math.round(waveRadius);
                g.drawOval(cx - wr, cy - wr, 2 * wr, 2 * wr);
            }
            
            float starAlpha = switchFlash * 0.8f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, starAlpha));
            g.setColor(new Color(255, 245, 200, Math.max(0, Math.min(255, (int)(starAlpha * 255)))));
            int starSize = Math.round(rightBounds.width * 0.25f * (1f + switchFlash * 0.5f));
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - starSize, cy, cx + starSize, cy);
            g.drawLine(cx, cy - starSize, cx, cy + starSize);
            int diag = Math.round(starSize * 0.7f);
            g.drawLine(cx - diag, cy - diag, cx + diag, cy + diag);
            g.drawLine(cx - diag, cy + diag, cx + diag, cy - diag);
            
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, switchFlash * 0.25f));
            RadialGradientPaint aura = new RadialGradientPaint(
                cx, cy, 
                rightBounds.width * 0.65f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{
                    new Color(255, 215, 0, 180),
                    new Color(255, 195, 50, 100),
                    new Color(255, 180, 0, 0)
                }
            );
            g.setPaint(aura);
            int auraSize = Math.round(rightBounds.width * 1.3f);
            g.fillOval(cx - auraSize/2, cy - auraSize/2, auraSize, auraSize);
            
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, switchFlash * 0.7f));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 8; i++) {
                float angle = (float)(i * Math.PI / 4 + controller.getTick() * 0.05);
                int boltLen = Math.round(rightBounds.width * 0.4f * (0.8f + switchFlash * 0.4f));
                int ex = cx + (int)(Math.cos(angle) * boltLen);
                int ey = cy + (int)(Math.sin(angle) * boltLen);
                float intensity = (i % 2 == 0) ? 1f : 0.7f;
                g.setColor(new Color(255, 235, 100, Math.max(0, Math.min(255, (int)(switchFlash * 255 * intensity)))));
                g.drawLine(cx, cy, ex, ey);
            }
            
            for (float[] rp : controller.getRightSwitchParticles()) {
                float life = rp[4] / rp[5];
                float alpha = (1f - life) * switchFlash * 0.9f;
                int s = Math.max(2, Math.round(3 + (0.5f - life) * 4));
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, alpha)));
                
                // Золотой цвет с легким мерцанием
                int rc = Math.max(0, Math.min(255, (int) (255 - life * 30)));
                int gc = Math.max(0, Math.min(255, (int) (200 + life * 55)));
                int bc = Math.max(0, Math.min(255, (int) (20 + life * 30)));
                
                g.setColor(new Color(rc, gc, bc, Math.max(0, Math.min(255, (int)(alpha*255)))));
                g.fillOval(Math.round(rp[0] * sw / 480f) - s/2, Math.round(rp[1] * sh / 360f) - s/2, s, s);
                
                // Легкое свечение вокруг частицы
                if (life < 0.5f) {
                    g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.3f));
                    g.setColor(new Color(255, 220, 80, Math.max(0, Math.min(255, (int)(alpha * 100)))));
                    g.fillOval(Math.round(rp[0] * sw / 480f) - s, Math.round(rp[1] * sh / 360f) - s, s*2, s*2);
                }
            }
            
            g.setComposite(prevF);
            g.setStroke(new BasicStroke(1f));
        }

        if (controller.shouldShowDialogBox()) {
            drawDialogBox(g, sw, sh);
        }

        if (controller.shouldShowVnButtons()) {
            drawVnButtons(g, sw, sh, mouseX, mouseY);
        }

        if (controller.isHistoryOpen()) {
            drawHistoryOverlay(g, sw, sh);
        }

        drawCursor(g, mouseX, mouseY);

        g.dispose();
    }

    private void drawCharacterEnhanced(Graphics2D g, int sw, int sh,
                                      BufferedImage sprite, float slide,
                                      boolean isLeft, boolean isActive, float activeAnim,
                                      boolean forceOpaque, boolean liftForShop, boolean raiseAboveOthers,
                                      float alphaMul) {
        if (sprite == null || slide <= 0.001f || alphaMul <= 0.001f) return;

        // Размер персонажа — примерно 85% высоты экрана
        float baseCharScale = (sh * 0.85f) / sprite.getHeight();

        // Активный персонаж чуть увеличивается (pop-эффект)
        float scaleBoost = 1.0f + activeAnim * 0.06f;
        float charScale = baseCharScale * scaleBoost;
        
        // Если передан флаг liftForShop — слегка уменьшим масштаб спрайта (визуально для прилавка)
        if (liftForShop) {
            charScale *= 0.92f; // уменьшение на ~8%
        }

        int cw = Math.round(sprite.getWidth() * charScale);
        int ch = Math.round(sprite.getHeight() * charScale);

        // Диалоговое окно занимает ~25% снизу и персонаж опущен ниже,
        // чтобы текст перекрывал ~половину тела
        int dialogZone = (int) (sh * 0.15f);
        int baseY = sh - dialogZone - ch + (int)(ch * 0.15f);

        // X позиция с анимацией slide-in (bounce overshoot)
        int offscreenX = isLeft ? -cw : sw;
        int targetX = isLeft ? (int) (sw * 0.02f) : (int) (sw - cw - sw * 0.02f);

        // Активный персонаж сдвигается к центру
        int activeShift = (int) (sw * 0.03f * activeAnim);
        if (isLeft) {
            targetX += activeShift;
        } else {
            targetX -= activeShift;
        }

        float easedSlide = IntroEasing.easeOutBack(slide);
        int cx = offscreenX + (int) ((targetX - offscreenX) * easedSlide);

        float breathe = (float) Math.sin(controller.getTick() * 0.04 + (isLeft ? 0 : 2)) * 2;
        if (isActive) {
            breathe += (float) Math.sin(controller.getTick() * 0.08) * 0.8f;
        }
        int cy = baseY + (int) breathe;

        // Если передан флаг liftForShop — слегка сдвинем персонажа вниз (корректировка для прилавка)
        if (liftForShop) {
            int down = Math.round(ch * 0.06f);
            cy = cy + down;
        }

        // Если передан флаг raiseAboveOthers — приподнимем изображение (используется для конкретного shop-герцога)
        if (raiseAboveOthers) {
            int up = Math.round(ch * 0.08f);
            cy = cy - up;
        }

        Composite prev = g.getComposite();

        // ── Спрайт персонажа (УЛУЧШЕННОЕ КАЧЕСТВО - комбинированная интерполяция) ──
        // Используем более высокое качество интерполяции для лучшего вида спрайтов
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        float characterAlpha = controller.getFadeAlpha() * Math.min(1f, 0.2f + slide * 0.9f) * alphaMul;
        if (forceOpaque && isActive) {
            characterAlpha = controller.getFadeAlpha() * alphaMul;
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, characterAlpha));
        g.drawImage(sprite, cx, cy, cw, ch, null);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);

        g.setComposite(prev);
    }

    private void drawScaledBackground(Graphics2D g, BufferedImage img, int sw, int sh, float alpha, boolean pixelate) {
        drawScaledBackground(g, img, sw, sh, alpha, pixelate, 1f);
    }

    private void drawScaledBackground(Graphics2D g, BufferedImage img, int sw, int sh, float alpha,
                                      boolean pixelate, float pixelSize) {
        if (img == null || alpha <= 0f) return;

        int srcW = img.getWidth();
        int srcH = img.getHeight();
        if (srcW <= 0 || srcH <= 0) return;

        float scale = Math.max((float) sw / srcW, (float) sh / srcH);
        int w = Math.round(srcW * scale);
        int h = Math.round(srcH * scale);
        int x = (sw - w) / 2;
        int y = (sh - h) / 2;

        Composite prevComposite = g.getComposite();
        Object prevInterpolation = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object prevRendering = g.getRenderingHint(RenderingHints.KEY_RENDERING);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        if (pixelate && pixelSize > 1.5f) {
            int smallW = Math.max(1, Math.round(w / pixelSize));
            int smallH = Math.max(1, Math.round(h / pixelSize));
            BufferedImage reduced = new BufferedImage(smallW, smallH, BufferedImage.TYPE_INT_ARGB);
            Graphics2D rg = reduced.createGraphics();
            rg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            rg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            rg.drawImage(img, 0, 0, smallW, smallH, null);
            rg.dispose();

            BufferedImage boosted = boostBackgroundImage(reduced,
                new float[]{1.12f, 1.10f, 1.06f, 1f},
                new float[]{8f, 6f, 4f, 0f});

            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
            g.drawImage(boosted, x, y, w, h, null);
        } else {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            BufferedImage boosted = boostBackgroundImage(img,
                new float[]{1.08f, 1.06f, 1.03f, 1f},
                new float[]{6f, 4f, 2f, 0f});
            g.drawImage(boosted, x, y, w, h, null);
        }

        if (prevInterpolation != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterpolation);
        }
        if (prevRendering != null) {
            g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRendering);
        }
        g.setComposite(prevComposite);
    }

    private BufferedImage boostBackgroundImage(BufferedImage source, float[] scales, float[] offsets) {
        BufferedImage compatibleSource = ensureArgbImage(source);
        int components = compatibleSource.getColorModel().hasAlpha() ? 4 : 3;
        float[] appliedScales = adaptComponents(scales, components, 1f);
        float[] appliedOffsets = adaptComponents(offsets, components, 0f);
        BufferedImage boosted = new BufferedImage(
            compatibleSource.getWidth(),
            compatibleSource.getHeight(),
            BufferedImage.TYPE_INT_ARGB
        );
        RescaleOp vividOp = new RescaleOp(appliedScales, appliedOffsets, null);
        vividOp.filter(compatibleSource, boosted);
        return boosted;
    }

    private BufferedImage ensureArgbImage(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }

        BufferedImage converted = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D cg = converted.createGraphics();
        cg.drawImage(source, 0, 0, null);
        cg.dispose();
        return converted;
    }

    private float[] adaptComponents(float[] values, int count, float fallback) {
        float[] adapted = new float[count];
        for (int index = 0; index < count; index++) {
            adapted[index] = index < values.length ? values[index] : fallback;
        }
        return adapted;
    }

    private void drawDialogBox(Graphics2D g, int sw, int sh) {
        IntroScript.DialogEntry entry = controller.getCurrentDialogEntry();
        if (entry == null) {
            return;
        }
        DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
        Color speakerColor = entry.speaker() == null
            ? IntroSwingBridge.colorFromRgb(IntroTheme.narratorRgb())
            : IntroSwingBridge.colorFromRgb(entry.speakerColorRgb());

        String visibleText = entry.text().substring(0, Math.min(controller.getCharIndex(), entry.text().length()));
        int lineY = DialogBoxRenderer.drawTypewriterText(
            g, entry.speaker(), visibleText, speakerColor, layout, controller.getFadeAlpha());

        if (!controller.isWaitingForAdvance() && (controller.getTick() / 8) % 2 == 0) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font textFont = GameFonts.get().plain(layout.fontSize);
            g.setFont(textFont);
            FontMetrics fm = g.getFontMetrics();
            int cursorX = layout.textX + fm.stringWidth(
                DialogBoxRenderer.getLastVisibleLine(visibleText, fm, layout.textMaxW));
            g.setColor(speakerColor);
            g.fillRect(cursorX + 2, lineY - fm.getAscent() + 2,
                Math.max(2, layout.fontSize / 5), fm.getAscent());
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }

        if (controller.isWaitingForAdvance() && !controller.isAutoMode()
            && (controller.getTick() / 15) % 2 == 0) {
            DialogBoxRenderer.drawHint(g, "\u25B6 Enter", layout, layout.fontSize, controller.getFadeAlpha());
        } else if (controller.isWaitingForAdvance() && controller.isAutoMode()
            && (controller.getTick() / 12) % 2 == 0) {
            DialogBoxRenderer.drawHint(g, "Авто \u25B6", layout, layout.fontSize, controller.getFadeAlpha() * 0.85f);
        }
    }

    // ─── Утилиты ───

    private static Rectangle toRectangle(IntroController.IntroRect r) {
        return new Rectangle(Math.round(r.x), Math.round(r.y), Math.round(r.width), Math.round(r.height));
    }

    private static Rectangle morphAnchorToRectangle(IntroMorphAnimation.IntroRect r) {
        return new Rectangle(Math.round(r.x), Math.round(r.y), Math.round(r.width), Math.round(r.height));
    }

    private static Rectangle layoutRectToAwt(IntroCharacterLayout.Rect r) {
        return new Rectangle(r.x, r.y, r.width, r.height);
    }

    private void drawStrangerToDukeMorph(Graphics2D g, int sw, int sh, boolean usingShopSprites,
                                         String activeSide, float rightActiveAnim, boolean rightForceOpaque) {
        float t = IntroEasing.easeInOutCubic(controller.getRightMorphT());
        BufferedImage dukeBase = usingShopSprites && dukeShopSprite != null ? dukeShopSprite : dukeSprite;
        IntroMorphAnimation.IntroRect anchor = controller.getMorphAnchorBounds();
        Rectangle bounds = anchor != null
            ? morphAnchorToRectangle(anchor)
            : layoutRectToAwt(IntroCharacterLayout.estimateRightCharacterBounds(sw, sh,
                strangerSprite != null ? strangerSprite.getWidth() : 0,
                strangerSprite != null ? strangerSprite.getHeight() : 0));

        float dissolve = 1f - IntroEasing.smoothstep(0f, 0.58f, t);
        float scatterOut = IntroEasing.smoothstep(0f, 0.62f, t);
        float manifest = IntroEasing.smoothstep(0.36f, 1f, t);
        float scatterIn = 1f - IntroEasing.smoothstep(0.36f, 1f, t);

        drawMorphAura(g, bounds, t);
        drawMorphSmoke(g, t);

        if (strangerSprite != null && dissolve > 0.02f) {
            Rectangle strangerRect = layoutRectToAwt(IntroCharacterLayout.computeCharacterRect(
                sw, sh, strangerSprite.getWidth(), strangerSprite.getHeight(),
                1f, false, false, 0f, controller.getTick(), false, false));
            drawSpriteDissolve(g, strangerSprite, strangerRect, dissolve, scatterOut, false);
        }
        if (dukeBase != null && manifest > 0.02f) {
            boolean dukeActive = "right".equals(activeSide);
            Rectangle dukeRect = layoutRectToAwt(IntroCharacterLayout.computeCharacterRect(
                sw, sh, dukeBase.getWidth(), dukeBase.getHeight(),
                1f, false, dukeActive, rightActiveAnim, controller.getTick(), false, false));
            drawSpriteDissolve(g, dukeBase, dukeRect, manifest, scatterIn, true);
            if (rightForceOpaque && dukeActive && manifest > 0.85f) {
                drawCharacterEnhanced(g, sw, sh, dukeBase, 1f, false,
                    true, rightActiveAnim, true, false, false, manifest);
            }
        }

        drawMorphSparks(g, t);
        drawMorphGoldenBurst(g, bounds, t);
    }

    private void drawMorphSmoke(Graphics2D g, float morphT) {
        Composite prev = g.getComposite();
        float peak = (float) Math.sin(morphT * Math.PI);
        for (float[] p : controller.getMorphSmoke()) {
            float life = p[5] / p[6];
            float alpha = (1f - life * 0.85f) * controller.getFadeAlpha() * (0.48f + peak * 0.52f);
            if (alpha <= 0.01f) {
                continue;
            }
            int sz = Math.max(2, Math.round(p[4]));
            int x = Math.round(p[0]);
            int y = Math.round(p[1]);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha * 0.35f)));
            g.setColor(new Color(
                Math.min(255, (int) p[7]),
                Math.min(255, (int) p[8]),
                Math.min(255, (int) p[9])));
            g.fillOval(x - sz / 2, y - sz / 2, sz + 2, sz + 2);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
            g.fillRect(x, y, Math.max(1, sz - 1), Math.max(1, sz - 1));
        }
        g.setComposite(prev);
    }

    private void drawMorphSparks(Graphics2D g, float morphT) {
        Composite prev = g.getComposite();
        float peak = (float) Math.sin(morphT * Math.PI);
        for (float[] p : controller.getMorphSparks()) {
            float life = 1f - p[4] / p[5];
            float alpha = life * controller.getFadeAlpha() * (0.55f + peak * 0.45f);
            if (alpha <= 0.02f) {
                continue;
            }
            int x = Math.round(p[0]);
            int y = Math.round(p[1]);
            int sz = Math.max(1, Math.round(p[6] * (0.6f + life * 0.5f)));

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.28f));
            g.setColor(new Color(255, 190, 40));
            int glow = sz + 4;
            g.fillOval(x - glow / 2, y - glow / 2, glow, glow);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
            g.setColor(new Color(255, 220, 80));
            g.fillRect(x, y, sz, sz);

            if (life > 0.45f) {
                g.setColor(new Color(255, 248, 210, Math.max(0, Math.min(255, (int) (alpha * 220)))));
                g.fillRect(x, y, Math.max(1, sz - 1), 1);
            }
        }
        g.setComposite(prev);
    }

    private void drawMorphGoldenBurst(Graphics2D g, Rectangle bounds, float morphT) {
        if (bounds == null) {
            return;
        }
        float peak = (float) Math.sin(morphT * Math.PI);
        if (peak <= 0.35f) {
            return;
        }
        float burst = (peak - 0.35f) / 0.65f;
        int cx = bounds.x + bounds.width / 2;
        int cy = bounds.y + bounds.height / 2;
        Composite prev = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, burst * 0.18f * controller.getFadeAlpha()));
        g.setColor(new Color(255, 225, 120));
        int flashW = Math.round(bounds.width * 1.1f);
        int flashH = Math.round(bounds.height * 0.55f);
        g.fillOval(cx - flashW / 2, cy - flashH / 2, flashW, flashH);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, burst * 0.12f * controller.getFadeAlpha()));
        g.setColor(new Color(40, 28, 18));
        g.fillOval(cx - flashW / 2, cy - flashH / 4, flashW, flashH);

        g.setComposite(prev);
    }

    private void drawMorphAura(Graphics2D g, Rectangle bounds, float morphT) {
        float peak = (float) Math.sin(morphT * Math.PI);
        if (peak <= 0.05f || bounds == null) {
            return;
        }
        int cx = bounds.x + bounds.width / 2;
        int cy = bounds.y + bounds.height / 2;
        Composite prev = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, peak * 0.38f * controller.getFadeAlpha()));
        RadialGradientPaint smoke = new RadialGradientPaint(
            cx, cy,
            bounds.width * 0.62f,
            new float[]{0f, 0.45f, 1f},
            new Color[]{
                new Color(22, 16, 28, 200),
                new Color(48, 32, 42, 120),
                new Color(8, 6, 12, 0)
            }
        );
        g.setPaint(smoke);
        int smokeSize = Math.round(bounds.width * 1.25f);
        g.fillOval(cx - smokeSize / 2, cy - smokeSize / 2, smokeSize, smokeSize);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, peak * 0.42f * controller.getFadeAlpha()));
        RadialGradientPaint gold = new RadialGradientPaint(
            cx, cy - bounds.height * 0.08f,
            bounds.width * 0.5f,
            new float[]{0f, 0.4f, 1f},
            new Color[]{
                new Color(255, 230, 130, 200),
                new Color(255, 185, 55, 100),
                new Color(255, 160, 30, 0)
            }
        );
        g.setPaint(gold);
        int goldSize = Math.round(bounds.width * 0.95f);
        g.fillOval(cx - goldSize / 2, cy - goldSize / 2 - 8, goldSize, goldSize);

        g.setComposite(prev);
    }

    private void drawSpriteDissolve(Graphics2D g, BufferedImage sprite, Rectangle rect,
                                    float solid, float scatter, boolean manifesting) {
        if (sprite == null || rect == null || solid <= 0.01f) {
            return;
        }
        int cx = rect.x;
        int cy = rect.y;
        int cw = rect.width;
        int ch = rect.height;
        int block = 7;
        Composite prev = g.getComposite();
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        for (int y = 0; y < ch; y += block) {
            for (int x = 0; x < cw; x += block) {
                int hash = (x * 73 + y * 991 + (manifesting ? 17 : 0)) & 0xFFFF;
                float threshold = hash / 65535f;
                if (threshold > solid) {
                    continue;
                }
                float fx = ((hash >> 3) & 0xFF) / 255f;
                float fy = ((hash >> 11) & 0xFF) / 255f;
                int ox = Math.round((fx - 0.5f) * scatter * 28f);
                int oy = Math.round((fy - 0.5f) * scatter * -34f - scatter * 12f);
                int bw = Math.min(block, cw - x);
                int bh = Math.min(block, ch - y);
                int sx = Math.min(sprite.getWidth() - 1, (int) ((float) x / cw * sprite.getWidth()));
                int sy = Math.min(sprite.getHeight() - 1, (int) ((float) y / ch * sprite.getHeight()));
                int sw = Math.max(1, Math.min(sprite.getWidth() - sx, (int) ((float) bw / cw * sprite.getWidth())));
                int sh = Math.max(1, Math.min(sprite.getHeight() - sy, (int) ((float) bh / ch * sprite.getHeight())));
                float blockAlpha = controller.getFadeAlpha() * solid * (0.45f + (1f - scatter) * 0.55f);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, blockAlpha));
                g.drawImage(sprite, cx + x + ox, cy + y + oy, cx + x + ox + bw, cy + y + oy + bh,
                    sx, sy, sx + sw, sy + sh, null);
            }
        }
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
        g.setComposite(prev);
    }

    private void drawVnButtons(Graphics2D g, int sw, int sh, int mouseX, int mouseY) {
        drawVnTextButton(g, toRectangle(controller.getBackButtonBounds()), "Назад",
            controller.isBackEnabled(), false,
            controller.isBackEnabled() && controller.getBackButtonBounds().contains(mouseX, mouseY));
        drawVnTextButton(g, toRectangle(controller.getHistoryButtonBounds()), "История",
            true, false,
            controller.getHistoryButtonBounds().contains(mouseX, mouseY));
        drawVnTextButton(g, toRectangle(controller.getAutoButtonBounds()), "Авто",
            true, controller.isAutoMode(),
            controller.getAutoButtonBounds().contains(mouseX, mouseY));
    }

    private void drawVnTextButton(Graphics2D g, Rectangle r, String label, boolean enabled,
                                  boolean active, boolean hover) {
        int alpha255 = Math.max(0, Math.min(255, (int) (controller.getFadeAlpha() * (enabled ? 255 : 130))));
        Color textColor;
        if (!enabled) {
            textColor = new Color(95, 80, 58, alpha255);
        } else if (active) {
            textColor = new Color(255, 225, 130, alpha255);
        } else if (hover) {
            textColor = new Color(255, 235, 170, alpha255);
        } else {
            textColor = new Color(205, 180, 115, alpha255);
        }

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int fontSize = Math.max(10, r.height - 8);
        int style = active ? Font.BOLD : Font.PLAIN;
        g.setFont(style == Font.BOLD ? GameFonts.get().bold(fontSize)
            : style == Font.ITALIC ? GameFonts.get().italic(fontSize)
            : GameFonts.get().plain(fontSize));
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
        int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;

        if (hover && enabled) {
            g.setColor(new Color(0, 0, 0, Math.max(0, Math.min(255, (int) (controller.getFadeAlpha() * 100)))));
            g.drawString(label, tx + 1, ty + 1);
        }
        g.setColor(textColor);
        g.drawString(label, tx, ty);

        if ((hover || active) && enabled) {
            int ulY = ty + 2;
            g.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(),
                Math.max(0, Math.min(255, (int) (controller.getFadeAlpha() * 180)))));
            g.drawLine(tx, ulY, tx + fm.stringWidth(label), ulY);
        }
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    private void drawCursor(Graphics2D g, int mouseX, int mouseY) {
        if (MENU_CURSOR != null) {
            int cw = 16;
            int ch = Math.max(1, cw * MENU_CURSOR.getHeight() / MENU_CURSOR.getWidth());
            g.drawImage(MENU_CURSOR, mouseX - 4, mouseY - 4, cw, ch, null);
        } else {
            g.setColor(new Color(255, 220, 100));
            g.drawLine(mouseX, mouseY, mouseX + 8, mouseY + 8);
            g.drawLine(mouseX, mouseY, mouseX + 6, mouseY);
            g.drawLine(mouseX, mouseY, mouseX, mouseY + 6);
        }
    }

    private static BufferedImage loadMenuCursor() {
        Sprite s = Sprite.loadOptional(MenuCursorPaths.MENU_CURSOR);
        return s != null ? s.getImage() : null;
    }

    private void drawHistoryOverlay(Graphics2D g, int sw, int sh) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, controller.getFadeAlpha() * 0.62f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);

        Rectangle panel = toRectangle(controller.getHistoryPanelBounds());
        DialogBoxRenderer.drawBox(g, panel.x, panel.y, panel.width, panel.height, controller.getFadeAlpha());

        Rectangle closeBounds = toRectangle(controller.getHistoryCloseBounds());
        UiChrome.drawCloseButton(g, closeBounds, controller.isHistoryCloseHovered(), controller.getFadeAlpha());

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        IntroHistoryLayout.Metrics m = IntroHistoryLayout.compute(sw, sh, panel.x, panel.y, panel.width, panel.height);

        Font titleFont = GameFonts.get().bold(m.titleSize);
        Font hintFont = GameFonts.get().italic(m.hintSize);
        Font bodyFont = GameFonts.get().plain(m.fontSize);
        g.setFont(bodyFont);
        FontMetrics fm = g.getFontMetrics();
        List<String> renderedLines = IntroHistoryText.buildRenderedLines(
            controller.buildHistoryLogLines(), Math.round(m.textMaxW), m.fontSize);
        int maxScroll = IntroHistoryLayout.maxScroll(renderedLines.size(), m.lineH, m.contentH);
        int historyScroll = IntroHistoryLayout.clampScroll(controller.getHistoryScroll(), maxScroll);
        float fadeAlpha = controller.getFadeAlpha();

        g.setFont(titleFont);
        g.setColor(new Color(IntroHistoryTheme.TITLE_R, IntroHistoryTheme.TITLE_G, IntroHistoryTheme.TITLE_B,
            Math.max(0, Math.min(255, (int) (fadeAlpha * 255)))));
        g.drawString("История", Math.round(m.textX), m.titleBaseline);

        g.setColor(new Color(IntroHistoryTheme.DIVIDER_R, IntroHistoryTheme.DIVIDER_G, IntroHistoryTheme.DIVIDER_B,
            Math.max(0, Math.min(255, (int) (fadeAlpha * IntroHistoryTheme.DIVIDER_A / 255f)))));
        g.drawLine(Math.round(m.textX), m.headerBottom - 4, Math.round(m.textX + m.textMaxW), m.headerBottom - 4);
        g.drawLine(Math.round(m.textX), m.footerTop, Math.round(m.textX + m.textMaxW), m.footerTop);

        Shape oldClip = g.getClip();
        g.clipRect(Math.round(m.textX), m.contentTop, Math.round(m.textMaxW), m.contentH);

        g.setFont(bodyFont);
        int y = m.contentTop + fm.getAscent() - historyScroll;
        for (String line : renderedLines) {
            if (y > m.contentBottom) {
                break;
            }
            if (y + fm.getDescent() >= m.contentTop) {
                boolean isSpeaker = IntroHistoryText.isSpeakerLine(line);
                g.setColor(isSpeaker
                    ? new Color(IntroHistoryTheme.SPEAKER_R, IntroHistoryTheme.SPEAKER_G, IntroHistoryTheme.SPEAKER_B,
                        Math.max(0, Math.min(255, (int) (fadeAlpha * 255))))
                    : new Color(IntroHistoryTheme.BODY_R, IntroHistoryTheme.BODY_G, IntroHistoryTheme.BODY_B,
                        Math.max(0, Math.min(255, (int) (fadeAlpha * 255)))));
                g.drawString(line, Math.round(m.textX), y);
            }
            y += m.lineH;
        }
        g.setClip(oldClip);

        g.setFont(hintFont);
        g.setColor(new Color(IntroHistoryTheme.HINT_R, IntroHistoryTheme.HINT_G, IntroHistoryTheme.HINT_B,
            Math.max(0, Math.min(255, (int) (fadeAlpha * IntroHistoryTheme.HINT_A / 255f)))));
        g.drawString("Колёсико — прокрутка", Math.round(m.textX), m.hintBaseline);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    public boolean isFinished() {
        return controller.isFinished();
    }

    private static BufferedImage loadTrimmed(String path) {
        Sprite s = Sprite.load(path);
        if (s == null) return null;
        return s.getImage();
    }

    private static Sprite tryLoadOptional(String... paths) {
        for (String p : paths) {
            Sprite s = Sprite.loadOptional(p);
            if (s != null) return s;
        }
        return null;
    }

    private static Sprite tryLoad(String... paths) {
        for (String p : paths) {
            Sprite s = Sprite.load(p);
            if (s != null) return s;
        }
        return null;
    }

    private static BufferedImage[] loadFrameStrip(String path, int cols, int rows) {
        Sprite s = Sprite.load(path);
        if (s == null) return new BufferedImage[0];
        BufferedImage src = s.getImage();
        int cw = src.getWidth() / cols;
        int ch = src.getHeight() / rows;
        BufferedImage[] out = new BufferedImage[cols * rows];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                BufferedImage frame = src.getSubimage(c * cw, r * ch, cw, ch);
                BufferedImage copy = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = copy.createGraphics();
                g.drawImage(frame, 0, 0, null);
                g.dispose();
                out[r * cols + c] = copy;
            }
        }
        return out;
    }

    // ─── Декодирование GIF кадров ───

    private static final class GifData {
        final BufferedImage[] frames;
        final int[] delays; // в миллисекундах
        GifData(BufferedImage[] frames, int[] delays) {
            this.frames = frames;
            this.delays = delays;
        }
    }

    private static GifData loadGifFrames(String resourcePath) {
        try {
            InputStream is = IntroScreen.class.getResourceAsStream(resourcePath);
            if (is == null) {
                // fallback: filesystem
                String relative = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
                File file = Paths.get(System.getProperty("user.dir"), "src", "main", "resources")
                        .resolve(relative).toFile();
                if (!file.exists()) return null;
                is = new java.io.FileInputStream(file);
            }

            ImageInputStream iis = ImageIO.createImageInputStream(is);
            Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("gif");
            if (!readers.hasNext()) return null;

            ImageReader reader = readers.next();
            reader.setInput(iis);
            int count = reader.getNumImages(true);
            if (count == 0) return null;

            List<BufferedImage> frames = new ArrayList<>();
            List<Integer> delays = new ArrayList<>();

            // Читаем первый кадр для размеров холста
            int canvasW = reader.getWidth(0);
            int canvasH = reader.getHeight(0);

            // Накопительный холст (для GIF с disposal method)
            BufferedImage canvas = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);

            for (int i = 0; i < count; i++) {
                BufferedImage rawFrame = reader.read(i);
                javax.imageio.metadata.IIOMetadata meta = reader.getImageMetadata(i);

                // Задержка из метаданных GIF
                int delayMs = 80; // default
                try {
                    String metaFmt = meta.getNativeMetadataFormatName();
                    org.w3c.dom.Node tree = meta.getAsTree(metaFmt);
                    org.w3c.dom.NodeList children = tree.getChildNodes();
                    for (int j = 0; j < children.getLength(); j++) {
                        org.w3c.dom.Node child = children.item(j);
                        if ("GraphicControlExtension".equals(child.getNodeName())) {
                            org.w3c.dom.NamedNodeMap attrs = child.getAttributes();
                            org.w3c.dom.Node delayAttr = attrs.getNamedItem("delayTime");
                            if (delayAttr != null) {
                                delayMs = Integer.parseInt(delayAttr.getNodeValue()) * 10;
                            }
                        }
                    }
                } catch (Exception ignored) {}

                // Рисуем кадр на холст
                Graphics2D cg = canvas.createGraphics();
                cg.drawImage(rawFrame, 0, 0, null);
                cg.dispose();

                // Копируем снимок холста
                BufferedImage snapshot = new BufferedImage(canvasW, canvasH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D sg = snapshot.createGraphics();
                sg.drawImage(canvas, 0, 0, null);
                sg.dispose();

                frames.add(snapshot);
                delays.add(delayMs);
            }
            reader.dispose();

            return new GifData(
                    frames.toArray(new BufferedImage[0]),
                    delays.stream().mapToInt(Integer::intValue).toArray()
            );
        } catch (Exception e) {
            return null;
        }
    }
}
