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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Пиксельная заставка-интро в стиле визуальной новеллы.
 * Персонажи появляются слева/справа, активный подсвечен,
 * текст печатается посимвольно (typewriter).
 */
public class IntroScreen {

    private static final int SHOP_ANIMATION_ENTRY_INDEX = 5;
    /** Длительность смены stranger → duke: дым и золотые искры (~2.2 с при 30 FPS). */
    private static final int VN_RIGHT_MORPH_TICKS = 66;

    // ─── Состояния ───
    private boolean finished = false;
    private int tick = 0;

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
    private int shopFrameIndex = 0;
    private long shopLastFrameTime = System.currentTimeMillis();
    private float shopReveal = 0f;

    // Позиции персонажей (анимированные slide-in, 0.0 = за экраном, 1.0 = на месте)
    private float geraltSlide = 0f;
    private float strangerSlide = 0f;
    private float dukeSlide = 0f;

    // Анимация активации (0..1, используется для масштаба и подсветки)
    private float leftActiveAnim = 0f;
    private float rightActiveAnim = 0f;

    // Кто сейчас виден
    private boolean geraltVisible = false;
    private String rightCharacter = "none"; // "stranger" | "duke" | "none"
    private String prevRightCharacter = "none"; // для отслеживания смены
    private Rectangle rightCharacterBounds = null; // для эффекта смены

    // Вспышка при смене персонажа (0..1, затухает) — не используется для stranger→duke
    private float switchFlash = 0f;
    /** Кроссфейд stranger → duke: дымовая маскировка. */
    private boolean rightMorphActive = false;
    private float rightMorphT = 0f;
    private Rectangle morphAnchorBounds = null;
    /** [x, y, vx, vy, size, life, maxLife, r, g, b] — дым. */
    private final List<float[]> morphSmoke = new ArrayList<>();
    /** [x, y, vx, vy, life, maxLife, size] — яркие золотые искры. */
    private final List<float[]> morphSparks = new ArrayList<>();
    // Частицы-искры при смене персонажа
    private final List<float[]> switchParticles = new ArrayList<>(); // [x,y,vx,vy,life,maxLife,r,g,b]
    // Подсилка для эффекта нового правого персонажа
    private final List<float[]> rightSwitchParticles = new ArrayList<>(); // [x,y,vx,vy,life,maxLife,alpha]

    // ─── Записи диалога ───
    private static final class DialogEntry {
        final String speaker;        // null = повествование (нарратор)
        final String text;
        final Color speakerColor;
        final String leftChar;       // "geralt" | "none"
        final String rightChar;      // "stranger" | "duke" | "none"
        final String activeSide;     // "left" | "right" | "none" (кто подсвечен)

        DialogEntry(String speaker, String text, Color speakerColor,
                    String leftChar, String rightChar, String activeSide) {
            this.speaker = speaker;
            this.text = text;
            this.speakerColor = speakerColor;
            this.leftChar = leftChar;
            this.rightChar = rightChar;
            this.activeSide = activeSide;
        }
    }

    private final List<DialogEntry> entries = new ArrayList<>();
    private int currentEntry = 0;

    // ─── Typewriter ───
    private int charIndex = 0;
    private int typeTickCounter = 0;
    private static final int TICKS_PER_CHAR = 2;
    private boolean waitingForAdvance = false;

    // ─── VN UI: история, назад, авто ───
    private boolean historyOpen = false;
    private boolean autoMode = false;
    private int historyScroll = 0;
    private int autoWaitTicks = 0;
    private static final int AUTO_DELAY_TICKS = 50;
    private static final int AUTO_TICKS_PER_CHAR = 1;
    private Rectangle backButtonBounds = new Rectangle();
    private Rectangle historyButtonBounds = new Rectangle();
    private Rectangle autoButtonBounds = new Rectangle();
    private Rectangle historyPanelBounds = new Rectangle();
    private int shopAnimStartedForEntry = -1;

    private static final BufferedImage MENU_CURSOR = loadMenuCursor();

    // ─── Частицы (огненные искры от факелов) ───
    private final List<float[]> sparks = new ArrayList<>();
    private final Random rng = new Random();

    // ─── Частицы пепла/пыли (фоновая анимация) ───
    private final List<float[]> ashParticles = new ArrayList<>(); // [x,y,vx,vy,life,maxLife,size,alpha]

    // Эмоции для персонажей
    private BufferedImage leftEmotion = null;
    private BufferedImage rightEmotion = null;

    // ─── Цвета ───
    // Тёплый, более контрастный, но не ярко-белый цвет для описательных (нарратор) строк
    private static final Color NARRATOR_COLOR = new Color(160, 145, 120);
    private static final Color GERALT_COLOR = new Color(160, 205, 235);
    private static final Color STRANGER_COLOR = new Color(100, 130, 200);
    private static final Color DUKE_COLOR = new Color(218, 165, 32);

    private float fadeAlpha = 0f;

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

        // ─── Диалоги (в стиле визуальной новеллы) ───
        // 0: Нарратор — фон, нет персонажей
        entries.add(new DialogEntry(null,
                "*Ветер завывает между древних стен Каэр Морхена.\nГеральт неспешно поднимается по разрушенной лестнице...",
                NARRATOR_COLOR, "none", "none", "none"));

        // 1: Геральт появляется слева
        entries.add(new DialogEntry(null,
                "*Внезапно, прямо из стены замка вырастает\nужасающая фигура в богато украшенном камзоле...*",
                NARRATOR_COLOR, "geralt", "stranger", "right"));

        // 2: Незнакомец говорит (справа)
        entries.add(new DialogEntry("Незнакомое существо",
                "*хриплый смех* Вас-то я и ждал, Геральт из Ривии.\nЗдешний замок вам не кажется подозрительным?\n*незнакомец устремляет свой взгляд на Арнскрон*",
                STRANGER_COLOR, "geralt", "stranger", "right"));

        // 3: Геральт отвечает (слева)
        entries.add(new DialogEntry("Геральт",
                "...Ага, и вы тоже.",
                GERALT_COLOR, "geralt", "stranger", "left"));

        // 4: Герцог раскрывается (справа меняется на duke)
        entries.add(new DialogEntry("Герцог",
                "Я вас ждал, господин из Ривии.\nПростите манеры — зовите меня Герцог. Я скромный торговец.\nБроня, зелья, клинки — чего пожелаете, обеспечу.",
                DUKE_COLOR, "geralt", "duke", "right"));

        // 5: Нарратор — финал
        entries.add(new DialogEntry(null,
                "*Из стены замка начинает вырастать\nнастоящий торговый прилавок...*",
                NARRATOR_COLOR, "geralt", "duke", "none"));

        // 6: Геральт после появления прилавка
        entries.add(new DialogEntry("Геральт",
            "...Сгенерировал?",
            GERALT_COLOR, "geralt", "duke", "left"));

        // 7: Герцог отвечает
        entries.add(new DialogEntry("Герцог",
            "Ха-ха… Это называется ассортимент.\nВыбирайте с умом — покупатель всегда прав.",
            DUKE_COLOR, "geralt", "duke", "right"));

        // 8: Геральт ворчит
        entries.add(new DialogEntry("Геральт",
            "Хмм. Я бы попросил лучше спирт.",
            GERALT_COLOR, "geralt", "duke", "left"));
    }

    // ─── Обновление ───
    public void update(boolean advanceKey, int mouseX, int mouseY, boolean mouseClicked, int wheelNotches) {
        tick++;

        if (fadeAlpha < 1f) fadeAlpha = Math.min(1f, fadeAlpha + 0.025f);

        if (currentEntry >= entries.size()) {
            finished = true;
            return;
        }

        int sw = 480;
        int sh = 360;
        layoutVnButtons(sw, sh);

        if (historyOpen) {
            if (wheelNotches != 0) {
                historyScroll = Math.max(0, historyScroll + wheelNotches * 18);
            }
            if (mouseClicked) {
                if (!historyPanelBounds.contains(mouseX, mouseY)) {
                    historyOpen = false;
                }
            }
            return;
        }

        boolean advance = advanceKey;
        if (mouseClicked) {
            if (historyButtonBounds.contains(mouseX, mouseY)) {
                historyOpen = true;
                historyScroll = 0;
                return;
            }
            if (backButtonBounds.contains(mouseX, mouseY)) {
                if (currentEntry > 0) {
                    goToPreviousEntry();
                }
                return;
            }
            if (autoButtonBounds.contains(mouseX, mouseY)) {
                autoMode = !autoMode;
                autoWaitTicks = 0;
                return;
            }
            if (isVnButtonRowClick(mouseX, mouseY)) {
                return;
            }
            advance = true;
        }

        DialogEntry entry = entries.get(currentEntry);

        // Обновляем видимость персонажей
        geraltVisible = "geralt".equals(entry.leftChar);
        String newRight = entry.rightChar;

        // Выбираем эмоцию для текущей реплики (персонажу, а не диалоговой рамке)
        leftEmotion = null;
        rightEmotion = null;
        if ("Геральт".equals(entry.speaker)) {
            leftEmotion = geraltEmotionSprite != null ? geraltEmotionSprite : geraltSprite;
        } else if ("Герцог".equals(entry.speaker)) {
            rightEmotion = dukeLaughSprite != null ? dukeLaughSprite : dukeSprite;
        }

        // Фоллбэк, если нет эмо-спрайта
        if (leftEmotion == null && "Геральт".equals(entry.speaker)) {
            leftEmotion = geraltSprite;
        }
        if (rightEmotion == null && "Герцог".equals(entry.speaker)) {
            rightEmotion = dukeSprite;
        }

        // Смена правого персонажа: stranger → duke — дымовая маскировка
        if (!newRight.equals(prevRightCharacter) && isStrangerToDukeReveal(prevRightCharacter, newRight)) {
            beginStrangerToDukeMorph();
        } else if (!newRight.equals(prevRightCharacter) && !"none".equals(newRight) && !"none".equals(prevRightCharacter)) {
            switchFlash = 1.0f;
            for (int i = 0; i < 30; i++) {
                float px = 0.75f * 480 + (rng.nextFloat() - 0.5f) * 90;
                float py = 0.35f * 360 + (rng.nextFloat() - 0.5f) * 90;
                float vx = (rng.nextFloat() - 0.5f) * 2.2f;
                float vy = (rng.nextFloat() - 0.5f) * 2.2f;
                float cr = 210 + rng.nextInt(46);
                float cg = 140 + rng.nextInt(80);
                float cb = 30 + rng.nextInt(50);
                switchParticles.add(new float[]{px, py, vx, vy, 0, 28 + rng.nextInt(26), cr, cg, cb});
            }
            rightSwitchParticles.clear();
            if (rightCharacterBounds != null) {
                for (int i = 0; i < 28; i++) {
                    float angle = (float) (rng.nextFloat() * Math.PI * 2);
                    float radius = rightCharacterBounds.width * 0.5f + rng.nextFloat() * 16;
                    float px = rightCharacterBounds.x + rightCharacterBounds.width / 2 + (float) Math.cos(angle) * radius;
                    float py = rightCharacterBounds.y + rightCharacterBounds.height / 2 + (float) Math.sin(angle) * radius;
                    float vx = (float) Math.cos(angle) * (0.8f + rng.nextFloat() * 1.2f);
                    float vy = (float) Math.sin(angle) * (0.8f + rng.nextFloat() * 1.2f);
                    rightSwitchParticles.add(new float[]{px, py, vx, vy, 0, 25 + rng.nextInt(25), 1f});
                }
            }
        }
        prevRightCharacter = newRight;
        rightCharacter = newRight;

        if (rightMorphActive) {
            rightMorphT = Math.min(1f, rightMorphT + 1f / VN_RIGHT_MORPH_TICKS);
            updateMorphSmoke(rightMorphT);
            if (rightMorphT >= 1f) {
                rightMorphActive = false;
                strangerSlide = 0f;
                dukeSlide = 1f;
                morphSmoke.clear();
                morphSparks.clear();
                morphAnchorBounds = null;
            }
        }

        // Slide анимации (отдельные для stranger и duke)
        float slideSpeed = 0.04f;
        geraltSlide = geraltVisible ? Math.min(1f, geraltSlide + slideSpeed) : Math.max(0f, geraltSlide - slideSpeed);

        boolean strangerWanted = "stranger".equals(rightCharacter);
        boolean dukeWanted = "duke".equals(rightCharacter);
        if (!rightMorphActive) {
            strangerSlide = strangerWanted ? Math.min(1f, strangerSlide + slideSpeed) : Math.max(0f, strangerSlide - slideSpeed * 1.5f);
            dukeSlide = dukeWanted ? Math.min(1f, dukeSlide + slideSpeed) : Math.max(0f, dukeSlide - slideSpeed * 1.5f);
        }

        // Анимация активации (плавное нарастание/затухание)
        boolean leftActive = "left".equals(entry.activeSide);
        boolean rightActive = "right".equals(entry.activeSide);
        float activeSpeed = 0.06f;
        leftActiveAnim = leftActive ? Math.min(1f, leftActiveAnim + activeSpeed) : Math.max(0f, leftActiveAnim - activeSpeed * 0.7f);
        rightActiveAnim = rightActive ? Math.min(1f, rightActiveAnim + activeSpeed) : Math.max(0f, rightActiveAnim - activeSpeed * 0.7f);

        // Затухание вспышки при смене (медленнее для более эффектной анимации)
        if (switchFlash > 0) switchFlash = Math.max(0f, switchFlash - 0.015f);

        // Обновление частиц смены
        switchParticles.removeIf(p -> p[4] >= p[5]);
        for (float[] p : switchParticles) {
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.96f;
            p[3] *= 0.96f;
            p[4]++;
        }
        // Обновление правых спец-частиц герцога
        rightSwitchParticles.removeIf(p -> p[4] >= p[5]);
        for (float[] p : rightSwitchParticles) {
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.94f;
            p[3] *= 0.94f;
            p[4]++;
        }

        int totalChars = entry.text.length();

        boolean shopSceneReached = currentEntry >= SHOP_ANIMATION_ENTRY_INDEX;
        boolean finalShopScene = currentEntry == SHOP_ANIMATION_ENTRY_INDEX;

        if (currentEntry == SHOP_ANIMATION_ENTRY_INDEX && shopAnimStartedForEntry < SHOP_ANIMATION_ENTRY_INDEX) {
            shopFrameIndex = 0;
            shopLastFrameTime = System.currentTimeMillis();
            shopAnimStartedForEntry = SHOP_ANIMATION_ENTRY_INDEX;
        } else if (currentEntry < SHOP_ANIMATION_ENTRY_INDEX) {
            shopAnimStartedForEntry = -1;
        }

        float revealTarget = shopSceneReached ? 1f : 0f;
        if (shopReveal < revealTarget) {
            shopReveal = Math.min(revealTarget, shopReveal + 0.05f);
        } else {
            shopReveal = Math.max(revealTarget, shopReveal - 0.05f);
        }

        if (finalShopScene && shopMaterializeFrames != null && shopMaterializeFrames.length > 0) {
            long now = System.currentTimeMillis();
            int delayMs = shopMaterializeDelays[shopFrameIndex];
            if (delayMs < 20) delayMs = 70;
            if (now - shopLastFrameTime >= delayMs) {
                shopFrameIndex = Math.min(shopMaterializeFrames.length - 1, shopFrameIndex + 1);
                shopLastFrameTime = now;
            }
        } else if (!shopSceneReached) {
            shopFrameIndex = 0;
            shopLastFrameTime = System.currentTimeMillis();
        }

        boolean shopAnimationComplete = shopSceneReached
            && (currentEntry > SHOP_ANIMATION_ENTRY_INDEX
            || (shopReveal >= 0.995f
            && (shopMaterializeFrames == null
            || shopMaterializeFrames.length == 0
            || shopFrameIndex >= shopMaterializeFrames.length - 1)));

        if (shopSceneReached && shopReveal > 0.03f) {
            if (shopAnimationComplete) {
                geraltSlide = geraltVisible ? Math.min(1f, geraltSlide + slideSpeed * 1.2f) : Math.max(0f, geraltSlide - slideSpeed);
                strangerSlide = Math.max(0f, strangerSlide - slideSpeed * 1.8f);
                dukeSlide = dukeWanted ? Math.min(1f, dukeSlide + slideSpeed * 1.2f) : Math.max(0f, dukeSlide - slideSpeed);
            } else {
                geraltSlide = Math.max(0f, geraltSlide - slideSpeed * 1.8f);
                strangerSlide = Math.max(0f, strangerSlide - slideSpeed * 1.8f);
                dukeSlide = Math.max(0f, dukeSlide - slideSpeed * 1.8f);
            }
        }

        if (waitingForAdvance) {
            boolean morphBlocking = rightMorphActive;
            if (advance && !morphBlocking) {
                advanceDialogueEntry();
            } else if (autoMode && !morphBlocking) {
                autoWaitTicks++;
                if (autoWaitTicks >= AUTO_DELAY_TICKS) {
                    advanceDialogueEntry();
                }
            }
        } else {
            if (autoMode && !rightMorphActive) {
                typeTickCounter++;
                if (typeTickCounter >= AUTO_TICKS_PER_CHAR) {
                    typeTickCounter = 0;
                    charIndex++;
                    if (charIndex >= totalChars) {
                        charIndex = totalChars;
                        waitingForAdvance = true;
                        autoWaitTicks = 0;
                    }
                }
            } else if (advance && charIndex < totalChars) {
                charIndex = totalChars;
                waitingForAdvance = true;
            } else {
                typeTickCounter++;
                if (typeTickCounter >= TICKS_PER_CHAR) {
                    typeTickCounter = 0;
                    charIndex++;
                    if (charIndex >= totalChars) {
                        charIndex = totalChars;
                        waitingForAdvance = true;
                    }
                }
            }
        }

        // Без анимации пепла/искор на фоне: фиксированное статичное изображение.
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
            drawScaledBackground(g, bgImg, sw, sh, fadeAlpha * 0.82f, false);
        }

        boolean shopSceneReached = currentEntry >= SHOP_ANIMATION_ENTRY_INDEX;
        boolean finalShopScene = currentEntry == SHOP_ANIMATION_ENTRY_INDEX;
        boolean shopAnimationComplete = shopSceneReached
            && (currentEntry > SHOP_ANIMATION_ENTRY_INDEX
            || (shopReveal >= 0.995f
            && (shopMaterializeFrames == null
            || shopMaterializeFrames.length == 0
            || shopFrameIndex >= shopMaterializeFrames.length - 1)));
        boolean hideCharactersForShopScene = finalShopScene && shopReveal > 0.03f && !shopAnimationComplete;

        if (shopReveal > 0.001f) {
            BufferedImage shopFrame = null;
            if (shopAnimationComplete && merchantBgImg != null) {
                shopFrame = merchantBgImg;
            } else if (shopMaterializeFrames != null && shopMaterializeFrames.length > 0) {
                shopFrame = shopMaterializeFrames[Math.max(0, Math.min(shopFrameIndex, shopMaterializeFrames.length - 1))];
            } else {
                shopFrame = merchantBgImg;
            }

            if (shopFrame != null) {
                if (shopAnimationComplete) {
                    drawScaledBackground(g, shopFrame, sw, sh, fadeAlpha, false);
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

        // ── Статический фон, без динамических эффектов ──
        // (убрана подсветка, виньетка, частицы пепла и искры)

        // ── Персонажи ──
        DialogEntry entry = currentEntry < entries.size() ? entries.get(currentEntry) : null;
        String activeSide = entry != null ? entry.activeSide : "none";
        boolean leftForceOpaque = (leftEmotion != null) && "left".equals(activeSide);
        boolean rightForceOpaque = (rightEmotion != null) && "right".equals(activeSide);

        // Использовать альтернативные (shop) спрайты, если лавка проявлена
        boolean usingShopSprites = shopReveal > 0.03f && (shopMaterializeFrames != null || merchantBgImg != null);

        if (!hideCharactersForShopScene) {
                // Рисуем Геральта (слева) - при показе лавки используем альтернативные shop-спрайты
                BufferedImage geraltBase = usingShopSprites && geraltShopSprite != null ? geraltShopSprite : geraltSprite;
                BufferedImage geraltEmotionBase = usingShopSprites && geraltEmotionShopSprite != null ? geraltEmotionShopSprite : geraltEmotionSprite;
                BufferedImage leftSpriteToShow = ("left".equals(activeSide) && geraltEmotionBase != null) ? geraltEmotionBase : geraltBase;
                        drawCharacterEnhanced(g, sw, sh, leftSpriteToShow, geraltSlide, true,
                            "left".equals(activeSide), leftActiveAnim, leftForceOpaque, false, false, 1f);

            // Правый персонаж: дымовая маскировка stranger → duke
            if (rightMorphActive) {
                drawStrangerToDukeMorph(g, sw, sh, usingShopSprites, activeSide, rightActiveAnim, rightForceOpaque);
            } else {
                if (strangerSlide > 0.001f) {
                    drawCharacterEnhanced(g, sw, sh, strangerSprite, strangerSlide, false,
                        "right".equals(activeSide) && "stranger".equals(rightCharacter), rightActiveAnim, false, false, false, 1f);
                }
                if (dukeSlide > 0.001f) {
                    BufferedImage dukeBase = usingShopSprites && dukeShopSprite != null ? dukeShopSprite : dukeSprite;
                    BufferedImage dukeEmotionBase = usingShopSprites && dukeLaughShopSprite != null ? dukeLaughShopSprite : dukeLaughSprite;
                    BufferedImage rightSpriteToShow = ("right".equals(activeSide) && dukeEmotionBase != null) ? dukeEmotionBase : dukeBase;
                    boolean liftDuke = usingShopSprites && rightSpriteToShow == dukeLaughShopSprite;
                    boolean raiseDuke = usingShopSprites && rightSpriteToShow == dukeLaughShopSprite;
                    drawCharacterEnhanced(g, sw, sh, rightSpriteToShow, dukeSlide, false,
                        "right".equals(activeSide) && "duke".equals(rightCharacter), rightActiveAnim, rightForceOpaque, liftDuke, raiseDuke, 1f);
                }
            }

            // ── Частицы смены персонажа ── (пропускаем, если показываем непрозрачную эмоцию)
            if (!(leftForceOpaque || rightForceOpaque)) {
                for (float[] p : switchParticles) {
                    float life = p[4] / p[5];
                    float a = (1f - life) * fadeAlpha;
                    int pr = Math.min(255, (int) p[6]);
                    int pg = Math.min(255, (int) p[7]);
                    int pb = Math.min(255, (int) p[8]);
                    g.setColor(new Color(pr, pg, pb, Math.max(0, Math.min(255, (int) (a * 220)))));
                    int sz = life < 0.3f ? 3 : (life < 0.6f ? 2 : 1);
                    g.fillRect(Math.round(p[0] * sw / 480f), Math.round(p[1] * sh / 360f), sz, sz);
                }
            }
        }

        // ── Красивая анимация появления герцога (золотые молнии + энергетические волны) ──
        if (!hideCharactersForShopScene && !rightMorphActive && switchFlash > 0.01f && rightCharacterBounds != null && !rightForceOpaque) {
            Composite prevF = g.getComposite();
            int cx = rightCharacterBounds.x + rightCharacterBounds.width / 2;
            int cy = rightCharacterBounds.y + rightCharacterBounds.height / 2;
            
            // ── 1. Золотые расходящиеся волны энергии (4 слоя) ──
            for (int i = 0; i < 4; i++) {
                float wavePhase = (switchFlash + i * 0.15f) % 1f;
                float waveRadius = rightCharacterBounds.width * 0.6f * (1f + wavePhase * 2.5f);
                float waveAlpha = (1f - wavePhase) * switchFlash * 0.5f;
                
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, waveAlpha)));
                g.setColor(new Color(255, 215, 0, Math.max(0, Math.min(255, (int)(waveAlpha * 200)))));
                g.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int wr = Math.round(waveRadius);
                g.drawOval(cx - wr, cy - wr, 2 * wr, 2 * wr);
            }
            
            // ── 2. Яркая звезда-вспышка в центре ──
            float starAlpha = switchFlash * 0.8f;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, starAlpha));
            g.setColor(new Color(255, 245, 200, Math.max(0, Math.min(255, (int)(starAlpha * 255)))));
            int starSize = Math.round(rightCharacterBounds.width * 0.25f * (1f + switchFlash * 0.5f));
            // Рисуем крест-звезду (4 луча)
            g.setStroke(new BasicStroke(4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(cx - starSize, cy, cx + starSize, cy); // горизонталь
            g.drawLine(cx, cy - starSize, cx, cy + starSize); // вертикаль
            // Диагональные лучи
            int diag = Math.round(starSize * 0.7f);
            g.drawLine(cx - diag, cy - diag, cx + diag, cy + diag);
            g.drawLine(cx - diag, cy + diag, cx + diag, cy - diag);
            
            // ── 3. Золотое свечение вокруг фигуры (аура) ──
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, switchFlash * 0.25f));
            RadialGradientPaint aura = new RadialGradientPaint(
                cx, cy, 
                rightCharacterBounds.width * 0.65f,
                new float[]{0f, 0.5f, 1f},
                new Color[]{
                    new Color(255, 215, 0, 180),
                    new Color(255, 195, 50, 100),
                    new Color(255, 180, 0, 0)
                }
            );
            g.setPaint(aura);
            int auraSize = Math.round(rightCharacterBounds.width * 1.3f);
            g.fillOval(cx - auraSize/2, cy - auraSize/2, auraSize, auraSize);
            
            // ── 4. Золотые молнии (8 ярких вспышек от центра) ──
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, switchFlash * 0.7f));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < 8; i++) {
                float angle = (float)(i * Math.PI / 4 + tick * 0.05);
                int boltLen = Math.round(rightCharacterBounds.width * 0.4f * (0.8f + switchFlash * 0.4f));
                int ex = cx + (int)(Math.cos(angle) * boltLen);
                int ey = cy + (int)(Math.sin(angle) * boltLen);
                
                // Градиент от центра к краю
                float intensity = (i % 2 == 0) ? 1f : 0.7f;
                g.setColor(new Color(255, 235, 100, Math.max(0, Math.min(255, (int)(switchFlash * 255 * intensity)))));
                g.drawLine(cx, cy, ex, ey);
            }
            
            // ── 5. Яркие частицы-искры (золотые точки) ──
            for (float[] rp : rightSwitchParticles) {
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

        // ── Диалоговое окно ──
        if (!finalShopScene && currentEntry < entries.size() && fadeAlpha > 0.2f) {
            drawDialogBox(g, sw, sh);
        }

        if (fadeAlpha > 0.2f && !finished) {
            drawVnButtons(g, sw, sh, mouseX, mouseY);
        }

        if (historyOpen) {
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

        float easedSlide = easeOutBack(slide);
        int cx = offscreenX + (int) ((targetX - offscreenX) * easedSlide);

        // Покачивание вверх-вниз (idle breathing)
        float breathe = (float) Math.sin(tick * 0.04 + (isLeft ? 0 : 2)) * 2;
        // Активный персонаж качается ритмичнее
        if (isActive) {
            breathe += (float) Math.sin(tick * 0.08) * 0.8f;
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
        float characterAlpha = fadeAlpha * Math.min(1f, 0.2f + slide * 0.9f) * alphaMul;
        // If requested (emotion sprite while active), draw fully opaque for clear face
        if (forceOpaque && isActive) {
            characterAlpha = fadeAlpha * alphaMul;
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, characterAlpha));
        g.drawImage(sprite, cx, cy, cw, ch, null);
        // Восстанавливаем пиксельные настройки для остального интерфейса
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        
        // Никакого дополнительного перерисовывания — рисуем только один экземпляр (исключаем удвоение)

        // Сохраняем рамку правого персонажа для эффекта смены
        if (!isLeft) {
            rightCharacterBounds = new Rectangle(cx, cy, cw, ch);
        }

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
        DialogEntry entry = entries.get(currentEntry);
        DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);

        String visibleText = entry.text.substring(0, Math.min(charIndex, entry.text.length()));
        int lineY = DialogBoxRenderer.drawTypewriterText(
            g, entry.speaker, visibleText, entry.speakerColor, layout, fadeAlpha);

        if (!waitingForAdvance && (tick / 8) % 2 == 0) {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font textFont = new Font("Serif", Font.PLAIN, layout.fontSize);
            g.setFont(textFont);
            FontMetrics fm = g.getFontMetrics();
            int cursorX = layout.textX + fm.stringWidth(
                DialogBoxRenderer.getLastVisibleLine(visibleText, fm, layout.textMaxW));
            g.setColor(entry.speakerColor != null ? entry.speakerColor : NARRATOR_COLOR);
            g.fillRect(cursorX + 2, lineY - fm.getAscent() + 2,
                Math.max(2, layout.fontSize / 5), fm.getAscent());
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }

        if (waitingForAdvance && !autoMode && (tick / 15) % 2 == 0) {
            DialogBoxRenderer.drawHint(g, "\u25B6 Enter", layout, layout.fontSize, fadeAlpha);
        } else if (waitingForAdvance && autoMode && (tick / 12) % 2 == 0) {
            DialogBoxRenderer.drawHint(g, "Авто \u25B6", layout, layout.fontSize, fadeAlpha * 0.85f);
        }
    }

    // ─── Утилиты ───

    private static boolean isStrangerToDukeReveal(String from, String to) {
        return "stranger".equals(from) && "duke".equals(to);
    }

    private void advanceDialogueEntry() {
        currentEntry++;
        charIndex = 0;
        typeTickCounter = 0;
        waitingForAdvance = false;
        autoWaitTicks = 0;
    }

    private void beginStrangerToDukeMorph() {
        rightMorphActive = true;
        rightMorphT = 0f;
        strangerSlide = 1f;
        dukeSlide = 1f;
        switchFlash = 0f;
        switchParticles.clear();
        rightSwitchParticles.clear();
        morphSmoke.clear();
        morphSparks.clear();
        morphAnchorBounds = rightCharacterBounds != null
            ? new Rectangle(rightCharacterBounds)
            : estimateRightCharacterBounds(480, 360, strangerSprite);
        spawnMorphSmoke(morphAnchorBounds);
        spawnMorphSparks(morphAnchorBounds, 160);
    }

    private void spawnMorphSparks(Rectangle bounds, int count) {
        int cx = bounds.x + bounds.width / 2;
        int cy = bounds.y + bounds.height / 2;
        for (int i = 0; i < count; i++) {
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            float speed = 0.8f + rng.nextFloat() * 4.2f;
            float px = cx + (rng.nextFloat() - 0.5f) * bounds.width * 0.75f;
            float py = cy + (rng.nextFloat() - 0.5f) * bounds.height * 0.65f;
            morphSparks.add(new float[]{
                px, py,
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed - 1.2f,
                0,
                22 + rng.nextInt(28),
                1.5f + rng.nextFloat() * 2.5f
            });
        }
    }

    private void spawnMorphSparkBurst(Rectangle bounds, int count) {
        int cx = bounds.x + bounds.width / 2;
        int cy = bounds.y + bounds.height / 2;
        for (int i = 0; i < count; i++) {
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            float speed = 1.8f + rng.nextFloat() * 5f;
            morphSparks.add(new float[]{
                cx + (rng.nextFloat() - 0.5f) * bounds.width * 0.35f,
                cy + (rng.nextFloat() - 0.5f) * bounds.height * 0.25f,
                (float) Math.cos(angle) * speed,
                (float) Math.sin(angle) * speed - 2f,
                0,
                16 + rng.nextInt(20),
                2f + rng.nextFloat() * 3f
            });
        }
    }

    private Rectangle estimateRightCharacterBounds(int sw, int sh, BufferedImage sprite) {
        if (sprite == null) {
            return new Rectangle((int) (sw * 0.55f), (int) (sh * 0.12f), (int) (sw * 0.4f), (int) (sh * 0.75f));
        }
        return computeCharacterRect(sw, sh, sprite, 1f, false, false, 0f, false, false);
    }

    private void spawnMorphSmoke(Rectangle bounds) {
        int cx = bounds.x + bounds.width / 2;
        int cy = bounds.y + bounds.height / 2;
        for (int i = 0; i < 520; i++) {
            float px = bounds.x + rng.nextFloat() * bounds.width;
            float py = bounds.y + rng.nextFloat() * bounds.height;
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            float speed = 0.5f + rng.nextFloat() * 3.2f;
            float vx = (float) Math.cos(angle) * speed * 0.65f;
            float vy = -speed * (0.8f + rng.nextFloat() * 1.1f);
            float size = 2f + rng.nextFloat() * 5f;
            int maxLife = 45 + rng.nextInt(55);
            boolean warm = rng.nextFloat() < 0.22f;
            float r = warm ? 120 + rng.nextInt(80) : 38 + rng.nextInt(42);
            float g = warm ? 85 + rng.nextInt(55) : 32 + rng.nextInt(38);
            float b = warm ? 35 + rng.nextInt(35) : 48 + rng.nextInt(55);
            morphSmoke.add(new float[]{px, py, vx, vy, size, 0, maxLife, r, g, b});
        }
        for (int i = 0; i < 90; i++) {
            float angle = (float) (rng.nextFloat() * Math.PI * 2);
            float speed = 1.4f + rng.nextFloat() * 3.8f;
            float px = cx + (rng.nextFloat() - 0.5f) * bounds.width * 0.25f;
            float py = cy + (rng.nextFloat() - 0.5f) * bounds.height * 0.2f;
            morphSmoke.add(new float[]{
                px, py,
                (float) Math.cos(angle) * speed * 0.7f,
                (float) Math.sin(angle) * speed * 0.5f - 2f,
                3f + rng.nextFloat() * 4f,
                0, 38 + rng.nextInt(32),
                28 + rng.nextInt(30), 22 + rng.nextInt(24), 38 + rng.nextInt(35)
            });
        }
    }

    private void updateMorphSmoke(float morphT) {
        if (morphAnchorBounds == null) {
            return;
        }
        int cx = morphAnchorBounds.x + morphAnchorBounds.width / 2;
        int cy = morphAnchorBounds.y + morphAnchorBounds.height / 2;
        float converge = smoothstep(0.42f, 0.92f, morphT);
        morphSmoke.removeIf(p -> p[5] >= p[6]);
        for (float[] p : morphSmoke) {
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.96f;
            p[3] *= 0.96f;
            p[3] -= 0.045f;
            if (converge > 0.01f) {
                p[2] += (cx - p[0]) * converge * 0.04f;
                p[3] += (cy - p[1]) * converge * 0.032f;
            }
            p[5]++;
        }

        morphSparks.removeIf(p -> p[4] >= p[5]);
        for (float[] p : morphSparks) {
            p[0] += p[2];
            p[1] += p[3];
            p[2] *= 0.94f;
            p[3] = p[3] * 0.94f - 0.06f;
            p[4]++;
        }

        if (morphT > 0.1f && morphT < 0.82f && tick % 2 == 0 && morphSmoke.size() < 680) {
            Rectangle b = morphAnchorBounds;
            float px = b.x + rng.nextFloat() * b.width;
            float py = b.y + b.height * (0.45f + rng.nextFloat() * 0.4f);
            morphSmoke.add(new float[]{
                px, py,
                (rng.nextFloat() - 0.5f) * 1.8f,
                -1.8f - rng.nextFloat() * 2.2f,
                2.5f + rng.nextFloat() * 4f,
                0, 40 + rng.nextInt(35),
                32 + rng.nextInt(35), 26 + rng.nextInt(28), 42 + rng.nextInt(40)
            });
        }
        if (morphT > 0.18f && morphT < 0.78f && tick % 2 == 0 && morphSparks.size() < 320) {
            spawnMorphSparkBurst(morphAnchorBounds, 6 + rng.nextInt(6));
        }
    }

    private void drawStrangerToDukeMorph(Graphics2D g, int sw, int sh, boolean usingShopSprites,
                                         String activeSide, float rightActiveAnim, boolean rightForceOpaque) {
        float t = easeInOutCubic(rightMorphT);
        BufferedImage dukeBase = usingShopSprites && dukeShopSprite != null ? dukeShopSprite : dukeSprite;
        Rectangle bounds = morphAnchorBounds != null
            ? morphAnchorBounds
            : estimateRightCharacterBounds(sw, sh, strangerSprite);

        float dissolve = 1f - smoothstep(0f, 0.58f, t);
        float scatterOut = smoothstep(0f, 0.62f, t);
        float manifest = smoothstep(0.36f, 1f, t);
        float scatterIn = 1f - smoothstep(0.36f, 1f, t);

        drawMorphAura(g, bounds, t);
        drawMorphSmoke(g, t);

        if (strangerSprite != null && dissolve > 0.02f) {
            Rectangle strangerRect = computeCharacterRect(sw, sh, strangerSprite, 1f, false, false, 0f, false, false);
            drawSpriteDissolve(g, strangerSprite, strangerRect, dissolve, scatterOut, false);
        }
        if (dukeBase != null && manifest > 0.02f) {
            boolean dukeActive = "right".equals(activeSide);
            Rectangle dukeRect = computeCharacterRect(sw, sh, dukeBase, 1f, false, dukeActive, rightActiveAnim, false, false);
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
        for (float[] p : morphSmoke) {
            float life = p[5] / p[6];
            float alpha = (1f - life * 0.85f) * fadeAlpha * (0.48f + peak * 0.52f);
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
        for (float[] p : morphSparks) {
            float life = 1f - p[4] / p[5];
            float alpha = life * fadeAlpha * (0.55f + peak * 0.45f);
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

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, burst * 0.18f * fadeAlpha));
        g.setColor(new Color(255, 225, 120));
        int flashW = Math.round(bounds.width * 1.1f);
        int flashH = Math.round(bounds.height * 0.55f);
        g.fillOval(cx - flashW / 2, cy - flashH / 2, flashW, flashH);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, burst * 0.12f * fadeAlpha));
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

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, peak * 0.38f * fadeAlpha));
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

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, peak * 0.42f * fadeAlpha));
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

    private Rectangle computeCharacterRect(int sw, int sh, BufferedImage sprite, float slide,
                                           boolean isLeft, boolean isActive, float activeAnim,
                                           boolean liftForShop, boolean raiseAboveOthers) {
        float baseCharScale = (sh * 0.85f) / sprite.getHeight();
        float scaleBoost = 1.0f + activeAnim * 0.06f;
        float charScale = baseCharScale * scaleBoost;
        if (liftForShop) {
            charScale *= 0.92f;
        }
        int cw = Math.round(sprite.getWidth() * charScale);
        int ch = Math.round(sprite.getHeight() * charScale);
        int dialogZone = (int) (sh * 0.15f);
        int baseY = sh - dialogZone - ch + (int) (ch * 0.15f);
        int offscreenX = isLeft ? -cw : sw;
        int targetX = isLeft ? (int) (sw * 0.02f) : (int) (sw - cw - sw * 0.02f);
        int activeShift = (int) (sw * 0.03f * activeAnim);
        if (isLeft) {
            targetX += activeShift;
        } else {
            targetX -= activeShift;
        }
        float easedSlide = easeOutBack(slide);
        int cx = offscreenX + (int) ((targetX - offscreenX) * easedSlide);
        float breathe = (float) Math.sin(tick * 0.04 + (isLeft ? 0 : 2)) * 2;
        if (isActive) {
            breathe += (float) Math.sin(tick * 0.08) * 0.8f;
        }
        int cy = baseY + (int) breathe;
        if (liftForShop) {
            cy += Math.round(ch * 0.06f);
        }
        if (raiseAboveOthers) {
            cy -= Math.round(ch * 0.08f);
        }
        return new Rectangle(cx, cy, cw, ch);
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
                float blockAlpha = fadeAlpha * solid * (0.45f + (1f - scatter) * 0.55f);
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

    private static float smoothstep(float edge0, float edge1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - edge0) / (edge1 - edge0)));
        return t * t * (3f - 2f * t);
    }

    private void layoutVnButtons(int sw, int sh) {
        DialogBoxRenderer.Layout layout = DialogBoxRenderer.computeLayout(sw, sh);
        int fontSize = Math.max(10, (int) (sh * 0.031f));
        int btnH = fontSize + 8;
        int gap = Math.max(28, (int) (sw * 0.09f));

        Font measureFont = new Font("Serif", Font.PLAIN, fontSize);
        FontMetrics fm = Toolkit.getDefaultToolkit().getFontMetrics(measureFont);
        int backW = fm.stringWidth("Назад") + 10;
        int histW = fm.stringWidth("История") + 10;
        int autoW = fm.stringWidth("Авто") + 10;
        int totalW = backW + histW + autoW + gap * 2;
        int startX = (sw - totalW) / 2;

        int rowY;
        if (currentEntry == SHOP_ANIMATION_ENTRY_INDEX) {
            rowY = sh - btnH - 6;
        } else {
            rowY = layout.boxY + layout.boxH + 4;
            if (rowY + btnH > sh - 4) {
                rowY = sh - btnH - 4;
            }
        }

        backButtonBounds.setBounds(startX, rowY, backW, btnH);
        historyButtonBounds.setBounds(startX + backW + gap, rowY, histW, btnH);
        autoButtonBounds.setBounds(startX + backW + gap + histW + gap, rowY, autoW, btnH);

        int panelW = Math.max(280, (int) (sw * 0.82f));
        int panelH = Math.max(200, (int) (sh * 0.72f));
        historyPanelBounds.setBounds((sw - panelW) / 2, (sh - panelH) / 2, panelW, panelH);
    }

    private boolean isVnButtonRowClick(int mouseX, int mouseY) {
        return backButtonBounds.contains(mouseX, mouseY)
            || historyButtonBounds.contains(mouseX, mouseY)
            || autoButtonBounds.contains(mouseX, mouseY);
    }

    private void drawVnButtons(Graphics2D g, int sw, int sh, int mouseX, int mouseY) {
        layoutVnButtons(sw, sh);
        boolean backEnabled = currentEntry > 0;
        drawVnTextButton(g, backButtonBounds, "Назад", backEnabled, false,
            backEnabled && backButtonBounds.contains(mouseX, mouseY));
        drawVnTextButton(g, historyButtonBounds, "История", true, false,
            historyButtonBounds.contains(mouseX, mouseY));
        drawVnTextButton(g, autoButtonBounds, "Авто", true, autoMode,
            autoButtonBounds.contains(mouseX, mouseY));
    }

    private void drawVnTextButton(Graphics2D g, Rectangle r, String label, boolean enabled,
                                  boolean active, boolean hover) {
        int alpha255 = Math.max(0, Math.min(255, (int) (fadeAlpha * (enabled ? 255 : 130))));
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
        g.setFont(new Font("Serif", style, fontSize));
        FontMetrics fm = g.getFontMetrics();
        int tx = r.x + (r.width - fm.stringWidth(label)) / 2;
        int ty = r.y + (r.height + fm.getAscent() - fm.getDescent()) / 2;

        if (hover && enabled) {
            g.setColor(new Color(0, 0, 0, Math.max(0, Math.min(255, (int) (fadeAlpha * 100)))));
            g.drawString(label, tx + 1, ty + 1);
        }
        g.setColor(textColor);
        g.drawString(label, tx, ty);

        if ((hover || active) && enabled) {
            int ulY = ty + 2;
            g.setColor(new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(),
                Math.max(0, Math.min(255, (int) (fadeAlpha * 180)))));
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
        Sprite s = Sprite.loadOptional("/assets/sprites/menu/menu_cursor.png");
        return s != null ? s.getImage() : null;
    }

    private void drawHistoryOverlay(Graphics2D g, int sw, int sh) {
        layoutVnButtons(sw, sh);

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.62f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);

        Rectangle panel = historyPanelBounds;
        DialogBoxRenderer.drawBox(g, panel.x, panel.y, panel.width, panel.height, fadeAlpha);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        int fontSize = Math.max(11, (int) (sh * 0.034f));
        int lineH = fontSize + 4;
        int pad = Math.max(10, (int) (sw * 0.022f));
        int textX = panel.x + pad;
        int textMaxW = panel.width - pad * 2;

        int titleSize = fontSize + 1;
        Font titleFont = new Font("Serif", Font.BOLD, titleSize);
        g.setFont(titleFont);
        FontMetrics titleFm = g.getFontMetrics();
        int titleBaseline = panel.y + pad + titleFm.getAscent();
        int headerBottom = titleBaseline + titleFm.getDescent() + 8;

        int hintSize = Math.max(10, fontSize - 1);
        Font hintFont = new Font("Serif", Font.ITALIC, hintSize);
        g.setFont(hintFont);
        FontMetrics hintFm = g.getFontMetrics();
        int hintBaseline = panel.y + panel.height - pad;
        int footerTop = hintBaseline - hintFm.getHeight() - 6;

        int contentTop = headerBottom;
        int contentBottom = footerTop;
        int contentH = Math.max(0, contentBottom - contentTop);

        Font bodyFont = new Font("Serif", Font.PLAIN, fontSize);
        g.setFont(bodyFont);
        FontMetrics fm = g.getFontMetrics();
        List<String> renderedLines = buildHistoryRenderedLines(fm, textMaxW);
        int totalHeight = renderedLines.size() * lineH;
        int maxScroll = Math.max(0, totalHeight - contentH);
        historyScroll = Math.min(historyScroll, maxScroll);

        g.setFont(titleFont);
        g.setColor(new Color(218, 165, 32, Math.max(0, Math.min(255, (int) (fadeAlpha * 255)))));
        g.drawString("История", textX, titleBaseline);

        g.setColor(new Color(100, 80, 45, Math.max(0, Math.min(255, (int) (fadeAlpha * 160)))));
        g.drawLine(textX, headerBottom - 4, textX + textMaxW, headerBottom - 4);
        g.drawLine(textX, footerTop, textX + textMaxW, footerTop);

        Shape oldClip = g.getClip();
        g.clipRect(textX, contentTop, textMaxW, contentH);

        g.setFont(bodyFont);
        int y = contentTop + fm.getAscent() - historyScroll;
        for (String line : renderedLines) {
            if (y > contentBottom) {
                break;
            }
            if (y + fm.getDescent() >= contentTop) {
                boolean isSpeaker = line.startsWith("[") && line.endsWith("]");
                g.setColor(isSpeaker
                    ? new Color(180, 150, 90, Math.max(0, Math.min(255, (int) (fadeAlpha * 255))))
                    : new Color(210, 195, 155, Math.max(0, Math.min(255, (int) (fadeAlpha * 255)))));
                g.drawString(line, textX, y);
            }
            y += lineH;
        }
        g.setClip(oldClip);

        g.setFont(hintFont);
        g.setColor(new Color(150, 130, 95, Math.max(0, Math.min(255, (int) (fadeAlpha * 200)))));
        g.drawString("Колёсико — прокрутка  ·  клик снаружи — закрыть", textX, hintBaseline);

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    }

    private List<String> buildHistoryRenderedLines(FontMetrics fm, int textMaxW) {
        List<String> rendered = new ArrayList<>();
        for (String raw : buildHistoryLogLines()) {
            if (raw.isEmpty()) {
                rendered.add("");
                continue;
            }
            rendered.addAll(DialogBoxRenderer.wrapLine(raw, fm, textMaxW));
        }
        return rendered;
    }

    private List<String> buildHistoryLogLines() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i <= currentEntry && i < entries.size(); i++) {
            DialogEntry e = entries.get(i);
            String speakerLabel = e.speaker != null ? e.speaker : "Повествование";
            lines.add("[" + speakerLabel + "]");
            String text = i < currentEntry ? e.text : e.text.substring(0, Math.min(charIndex, e.text.length()));
            for (String part : text.split("\n", -1)) {
                lines.add(part);
            }
            if (i < currentEntry) {
                lines.add("");
            }
        }
        return lines;
    }

    private void goToPreviousEntry() {
        if (currentEntry <= 0) {
            return;
        }
        currentEntry--;
        charIndex = 0;
        typeTickCounter = 0;
        waitingForAdvance = false;
        historyOpen = false;
        historyScroll = 0;
        autoWaitTicks = 0;

        if (currentEntry < SHOP_ANIMATION_ENTRY_INDEX) {
            shopReveal = 0f;
            shopFrameIndex = 0;
            shopAnimStartedForEntry = -1;
        }

        DialogEntry entry = entries.get(currentEntry);
        geraltVisible = "geralt".equals(entry.leftChar);
        rightCharacter = entry.rightChar;
        prevRightCharacter = entry.rightChar;
        geraltSlide = geraltVisible ? 1f : 0f;
        strangerSlide = "stranger".equals(entry.rightChar) ? 1f : 0f;
        dukeSlide = "duke".equals(entry.rightChar) ? 1f : 0f;
        rightMorphActive = false;
        rightMorphT = 0f;
        morphSmoke.clear();
        morphSparks.clear();
        morphAnchorBounds = null;
        switchFlash = 0f;
        switchParticles.clear();
        rightSwitchParticles.clear();
        leftEmotion = null;
        rightEmotion = null;
    }

    private float easeInOutCubic(float t) {
        if (t >= 1f) {
            return 1f;
        }
        if (t <= 0f) {
            return 0f;
        }
        if (t < 0.5f) {
            return 4f * t * t * t;
        }
        return 1f - (float) Math.pow(-2f * t + 2f, 3) / 2f;
    }

    private float easeOut(float t) {
        return 1f - (1f - t) * (1f - t);
    }

    /** easeOutBack — небольшой инерционный перелёт за целевую позицию и возврат */
    private float easeOutBack(float t) {
        if (t >= 1f) return 1f;
        if (t <= 0f) return 0f;
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        return 1f + c3 * (float) Math.pow(t - 1, 3) + c1 * (float) Math.pow(t - 1, 2);
    }

    public boolean isFinished() {
        return finished;
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
