package main.java.com.witcher.ui.graphics;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
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

    // ─── Состояния ───
    private boolean finished = false;
    private int tick = 0;
    private int finishTick = -1;
    private static final int FADE_OUT_TICKS = 90;

    // ─── Персонажи (спрайты) ───
    private final BufferedImage geraltSprite;
    private final BufferedImage dukeSprite;
    private final BufferedImage strangerSprite;
    private final BufferedImage geraltEmotionSprite;
    private final BufferedImage dukeLaughSprite;

    // Фон: кадры GIF (декодированы вручную) или статичный фон
    private final BufferedImage[] bgFrames;  // null если GIF не загрузился
    private final int[] bgFrameDelays;       // задержки кадров в мс
    private final BufferedImage staticBgImg; // fallback если GIF нет
    private int bgFrameIndex = 0;
    private long bgLastFrameTime = System.currentTimeMillis();

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

    // Вспышка при смене персонажа (0..1, затухает)
    private float switchFlash = 0f;
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

    // ─── Частицы (огненные искры от факелов) ───
    private final List<float[]> sparks = new ArrayList<>();
    private final Random rng = new Random();

    // ─── Частицы пепла/пыли (фоновая анимация) ───
    private final List<float[]> ashParticles = new ArrayList<>(); // [x,y,vx,vy,life,maxLife,size,alpha]

    // Эмоции для персонажей
    private BufferedImage leftEmotion = null;
    private BufferedImage rightEmotion = null;

    // ─── Цвета ───
    private static final Color NARRATOR_COLOR = new Color(180, 170, 150);
    private static final Color GERALT_COLOR = new Color(160, 205, 235);
    private static final Color STRANGER_COLOR = new Color(100, 130, 200);
    private static final Color DUKE_COLOR = new Color(218, 165, 32);
    private static final Color BOX_BG = new Color(10, 8, 4, 220);
    private static final Color BOX_BORDER = new Color(140, 100, 35);
    private static final Color BOX_BORDER_INNER = new Color(90, 65, 20);
    private static final Color HINT_COLOR = new Color(180, 160, 120, 180);

    private float fadeAlpha = 0f;

    public IntroScreen() {
        // ─── Загрузка спрайтов ───
        geraltSprite = loadTrimmed("/assets/sprites/screen saver/geralt_portrait.png");
        dukeSprite = loadTrimmed("/assets/sprites/screen saver/duke_portrait.png");
        strangerSprite = loadTrimmed("/assets/sprites/screen saver/stranger_shadow.png");

        // Эмоции для диалогов
        geraltEmotionSprite = loadTrimmed("/assets/sprites/screen saver/geralt_emotion.png");
        dukeLaughSprite = loadTrimmed("/assets/sprites/screen saver/duke_portrait_fun.png");

        // ─── Загрузка фона (статичная PNG картинка) ───
        bgFrames = null;
        bgFrameDelays = null;
        Sprite fb = Sprite.load("/assets/sprites/screen saver/kaer_morhen_bg.png");
        if (fb == null) fb = Sprite.load("/assets/sprites/menu/menu_bg_custom.jpg");
        staticBgImg = fb != null ? fb.getImage() : null;

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
                "ХО-ХО-ХО-ХА... Нет, нет, я обычный торговец.\nЯ не представился. Зовите меня Герцог.\nПриступим к делу. Броня, кирасы, шлемы, наколенники...\nОбеспечу вас всем, чего пожелаете.",
                DUKE_COLOR, "geralt", "duke", "right"));

        // 5: Нарратор — финал
        entries.add(new DialogEntry(null,
                "*Из стены замка начинает вырастать\nнастоящий торговый прилавок...*",
                NARRATOR_COLOR, "geralt", "duke", "none"));
    }

    // ─── Обновление ───
    public void update(boolean advancePressed) {
        tick++;

        if (fadeAlpha < 1f) fadeAlpha = Math.min(1f, fadeAlpha + 0.025f);

        // Fade out после завершения
        if (finishTick > 0 && tick - finishTick > FADE_OUT_TICKS) {
            finished = true;
            return;
        }

        if (currentEntry >= entries.size()) {
            if (finishTick < 0) finishTick = tick;
            return;
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

        // Детектим смену правого персонажа (stranger → duke)
        if (!newRight.equals(prevRightCharacter) && !"none".equals(newRight) && !"none".equals(prevRightCharacter)
                && !newRight.equals(prevRightCharacter)) {
            switchFlash = 1.0f;
            // Генерируем частицы-искры при переключении
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
            // Сетка шипящих частиц вокруг герцога
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

        // Slide анимации (отдельные для stranger и duke)
        float slideSpeed = 0.04f;
        geraltSlide = geraltVisible ? Math.min(1f, geraltSlide + slideSpeed) : Math.max(0f, geraltSlide - slideSpeed);

        boolean strangerWanted = "stranger".equals(rightCharacter);
        boolean dukeWanted = "duke".equals(rightCharacter);
        strangerSlide = strangerWanted ? Math.min(1f, strangerSlide + slideSpeed) : Math.max(0f, strangerSlide - slideSpeed * 1.5f);
        dukeSlide = dukeWanted ? Math.min(1f, dukeSlide + slideSpeed) : Math.max(0f, dukeSlide - slideSpeed * 1.5f);

        // Анимация активации (плавное нарастание/затухание)
        boolean leftActive = "left".equals(entry.activeSide);
        boolean rightActive = "right".equals(entry.activeSide);
        float activeSpeed = 0.06f;
        leftActiveAnim = leftActive ? Math.min(1f, leftActiveAnim + activeSpeed) : Math.max(0f, leftActiveAnim - activeSpeed * 0.7f);
        rightActiveAnim = rightActive ? Math.min(1f, rightActiveAnim + activeSpeed) : Math.max(0f, rightActiveAnim - activeSpeed * 0.7f);

        // Затухание вспышки при смене
        if (switchFlash > 0) switchFlash = Math.max(0f, switchFlash - 0.04f);

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

        if (waitingForAdvance) {
            if (advancePressed) {
                currentEntry++;
                charIndex = 0;
                typeTickCounter = 0;
                waitingForAdvance = false;
            }
        } else {
            if (advancePressed && charIndex < totalChars) {
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
            int srcW = bgImg.getWidth();
            int srcH = bgImg.getHeight();
            if (srcW > 0 && srcH > 0) {
                float scale = Math.max((float) sw / srcW, (float) sh / srcH);
                int w = Math.round(srcW * scale);
                int h = Math.round(srcH * scale);
                int x = (sw - w) / 2;
                int y = (sh - h) / 2;
                Composite prev = g.getComposite();
                // BILINEAR для фона — плавное масштабирование без пиксельных блоков
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.82f));
                g.drawImage(bgImg, x, y, w, h, null);
                // Восстанавливаем пиксельные настройки для остальных элементов
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                g.setComposite(prev);
            }
        }

        // ── Статический фон, без динамических эффектов ──
        // (убрана подсветка, виньетка, частицы пепла и искры)

        // ── Персонажи ──
        DialogEntry entry = currentEntry < entries.size() ? entries.get(currentEntry) : null;
        String activeSide = entry != null ? entry.activeSide : "none";

        // Рисуем Геральта (слева) - используем эмоциональный спрайт, если он активен и говорит
        BufferedImage leftSpriteToShow = (leftEmotion != null && "left".equals(activeSide)) ? leftEmotion : geraltSprite;
        drawCharacterEnhanced(g, sw, sh, leftSpriteToShow, geraltSlide, true,
                "left".equals(activeSide), leftActiveAnim);

        // Рисуем правых персонажей с кроссфейдом (stranger уходит, duke появляется)
        if (strangerSlide > 0.001f) {
            drawCharacterEnhanced(g, sw, sh, strangerSprite, strangerSlide, false,
                    "right".equals(activeSide) && "stranger".equals(rightCharacter), rightActiveAnim);
        }
        if (dukeSlide > 0.001f) {
            // Для герцога используем эмоциональный спрайт (смех), если он активен и говорит
            BufferedImage rightSpriteToShow = (rightEmotion != null && "right".equals(activeSide)) ? rightEmotion : dukeSprite;
            drawCharacterEnhanced(g, sw, sh, rightSpriteToShow, dukeSlide, false,
                    "right".equals(activeSide) && "duke".equals(rightCharacter), rightActiveAnim);
        }

        // ── Частицы смены персонажа ──
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

        // ── Красивая анимация появления герцога (золотые молнии + энергетические волны) ──
        if (switchFlash > 0.01f && rightCharacterBounds != null) {
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
        if (currentEntry < entries.size() && fadeAlpha > 0.2f) {
            drawDialogBox(g, sw, sh);
        }

        // ── Fade out ──
        if (finishTick > 0) {
            float outAlpha = Math.min(1f, (float) (tick - finishTick) / FADE_OUT_TICKS);
            g.setColor(new Color(0, 0, 0, (int) (outAlpha * 255)));
            g.fillRect(0, 0, sw, sh);
        }

        g.dispose();
    }

    private void drawCharacterEnhanced(Graphics2D g, int sw, int sh,
                                      BufferedImage sprite, float slide,
                                      boolean isLeft, boolean isActive, float activeAnim) {
        if (sprite == null || slide <= 0.001f) return;

        // Размер персонажа — примерно 85% высоты экрана
        float baseCharScale = (sh * 0.85f) / sprite.getHeight();

        // Активный персонаж чуть увеличивается (pop-эффект)
        float scaleBoost = 1.0f + activeAnim * 0.06f;
        float charScale = baseCharScale * scaleBoost;

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

        Composite prev = g.getComposite();

        // ── Спрайт персонажа (ЧЕТКИЙ - NEAREST_NEIGHBOR для пиксель-арта) ──
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha));
        g.drawImage(sprite, cx, cy, cw, ch, null);

        // Сохраняем рамку правого персонажа для эффекта смены
        if (!isLeft) {
            rightCharacterBounds = new Rectangle(cx, cy, cw, ch);
        }

        g.setComposite(prev);
    }

    private void drawDialogBox(Graphics2D g, int sw, int sh) {
        DialogEntry entry = entries.get(currentEntry);

        int boxMargin = (int) (sw * 0.03f);
        int boxH = (int) (sh * 0.30f);
        int boxW = sw - boxMargin * 2;
        int boxX = boxMargin;
        int boxY = sh - boxH - (int)(sh * 0.02f);

        // ── Фон диалогового окна с градиентом ──
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.90f));
        g.setColor(BOX_BG);
        g.fillRect(boxX, boxY, boxW, boxH);
        
        // Легкий градиент сверху вниз для объема
        GradientPaint bgGradient = new GradientPaint(
            boxX, boxY, new Color(20, 16, 8, 80),
            boxX, boxY + boxH / 3, new Color(5, 4, 2, 0)
        );
        g.setPaint(bgGradient);
        g.fillRect(boxX, boxY, boxW, boxH / 3);
        
        g.setComposite(prev);

        // ── Красивая многослойная рамка в стиле Ведьмака ──
        
        // 1. Внешняя толстая золотая рамка
        g.setColor(new Color(180, 140, 60, Math.max(0, Math.min(255, (int)(fadeAlpha * 255)))));
        g.fillRect(boxX - 2, boxY - 2, boxW + 4, 3);  // верх
        g.fillRect(boxX - 2, boxY + boxH - 1, boxW + 4, 3);  // низ
        g.fillRect(boxX - 2, boxY - 2, 3, boxH + 4);  // лево
        g.fillRect(boxX + boxW - 1, boxY - 2, 3, boxH + 4);  // право
        
        // 2. Основная яркая золотая рамка
        g.setColor(new Color(218, 165, 32, Math.max(0, Math.min(255, (int)(fadeAlpha * 255)))));
        g.fillRect(boxX, boxY, boxW, 2);  // верх
        g.fillRect(boxX, boxY + boxH - 2, boxW, 2);  // низ
        g.fillRect(boxX, boxY, 2, boxH);  // лево
        g.fillRect(boxX + boxW - 2, boxY, 2, boxH);  // право
        
        // 3. Внутренняя тонкая темная рамка для контраста
        g.setColor(new Color(60, 45, 15, Math.max(0, Math.min(255, (int)(fadeAlpha * 200)))));
        g.fillRect(boxX + 4, boxY + 4, boxW - 8, 1);  // верх
        g.fillRect(boxX + 4, boxY + boxH - 5, boxW - 8, 1);  // низ
        g.fillRect(boxX + 4, boxY + 4, 1, boxH - 8);  // лево
        g.fillRect(boxX + boxW - 5, boxY + 4, 1, boxH - 8);  // право
        
        // 4. Декоративные угловые акценты (золотые уголки)
        int cornerSize = 12;
        g.setColor(new Color(255, 215, 0, Math.max(0, Math.min(255, (int)(fadeAlpha * 220)))));
        // Верхний левый
        g.fillRect(boxX - 2, boxY - 2, cornerSize, 2);
        g.fillRect(boxX - 2, boxY - 2, 2, cornerSize);
        // Верхний правый
        g.fillRect(boxX + boxW - cornerSize + 2, boxY - 2, cornerSize, 2);
        g.fillRect(boxX + boxW, boxY - 2, 2, cornerSize);
        // Нижний левый
        g.fillRect(boxX - 2, boxY + boxH, cornerSize, 2);
        g.fillRect(boxX - 2, boxY + boxH - cornerSize + 2, 2, cornerSize);
        // Нижний правый
        g.fillRect(boxX + boxW - cornerSize + 2, boxY + boxH, cornerSize, 2);
        g.fillRect(boxX + boxW, boxY + boxH - cornerSize + 2, 2, cornerSize);
        
        // 5. Легкое свечение изнутри рамки
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.15f));
        g.setColor(new Color(255, 220, 120));
        g.drawRect(boxX + 1, boxY + 1, boxW - 2, boxH - 2);
        g.setComposite(prev);

        // ── Пад-отступы ──
        int pad = (int) (sw * 0.02f);
        int textX = boxX + pad;
        int textY = boxY + pad;
        int textMaxW = boxW - pad * 2;

        int fontSize = Math.max(12, (int) (sh * 0.040f));
        Font nameFont = new Font("Serif", Font.BOLD, fontSize);
        Font textFont = new Font("Serif", Font.PLAIN, fontSize);

        // Включаем сглаживание только для диалогов
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        int lineY = textY;

        // ── Имя говорящего (в рамке-плашке) ──
        if (entry.speaker != null) {
            g.setFont(nameFont);
            FontMetrics nfm = g.getFontMetrics();
            int nameW = nfm.stringWidth(entry.speaker);
            int nameH = nfm.getHeight();

            // Плашка с именем
            int nameBoxX = boxX + pad - 4;
            int nameBoxY = boxY - nameH - 2;
            int nameBoxW = nameW + 12;
            int nameBoxH = nameH + 4;

            Composite prevN = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.9f));
            g.setColor(BOX_BG);
            g.fillRect(nameBoxX, nameBoxY, nameBoxW, nameBoxH);
            g.setComposite(prevN);
            g.setColor(BOX_BORDER);
            g.drawRect(nameBoxX, nameBoxY, nameBoxW, nameBoxH);

            g.setColor(entry.speakerColor);
            g.drawString(entry.speaker, nameBoxX + 6, nameBoxY + nfm.getAscent() + 2);
        }

        // ── Текст реплики (typewriter) ──
        g.setFont(textFont);
        FontMetrics fm = g.getFontMetrics();
        int lineH = fm.getHeight();

        String visibleText = entry.text.substring(0, Math.min(charIndex, entry.text.length()));
        Color textColor = entry.speaker == null ? entry.speakerColor : new Color(255, 220, 80);

        String[] rawLines = visibleText.split("\n", -1);
        for (String rawLine : rawLines) {
            List<String> wrapped = wrapLine(rawLine, fm, textMaxW);
            for (String wl : wrapped) {
                lineY += lineH;
                if (lineY > boxY + boxH - pad) break;

                // Тень
                g.setColor(new Color(0, 0, 0, 160));
                g.drawString(wl, textX + 1, lineY + 1);
                // Основной
                g.setColor(textColor);
                g.drawString(wl, textX, lineY);
            }
        }

        // ── Мигающий курсор ──
        if (!waitingForAdvance && (tick / 8) % 2 == 0) {
            int cursorX = textX + fm.stringWidth(getLastVisibleLine(visibleText, fm, textMaxW));
            g.setColor(entry.speakerColor != null ? entry.speakerColor : NARRATOR_COLOR);
            g.fillRect(cursorX + 2, lineY - fm.getAscent() + 2, Math.max(2, fontSize / 5), fm.getAscent());
        }

        // ── Подсказка [ Enter ] ──
        if (waitingForAdvance && (tick / 15) % 2 == 0) {
            g.setFont(new Font("Serif", Font.BOLD, Math.max(10, fontSize - 2)));
            g.setColor(HINT_COLOR);
            String hint = "\u25B6 Enter";
            int hw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, boxX + boxW - pad - hw, boxY + boxH - pad + 2);
        }

        // Восстанавливаем пиксельные настройки
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
    }

    // ─── Утилиты ───

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

    private String getLastVisibleLine(String text, FontMetrics fm, int maxW) {
        String[] lines = text.split("\n", -1);
        String last = lines[lines.length - 1];
        List<String> wrapped = wrapLine(last, fm, maxW);
        return wrapped.isEmpty() ? "" : wrapped.get(wrapped.size() - 1);
    }

    private List<String> wrapLine(String line, FontMetrics fm, int maxW) {
        List<String> result = new ArrayList<>();
        if (line.isEmpty()) { result.add(""); return result; }
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

    public boolean isFinished() {
        return finished;
    }

    private static BufferedImage loadTrimmed(String path) {
        Sprite s = Sprite.load(path);
        if (s == null) return null;
        return s.getImage();
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
