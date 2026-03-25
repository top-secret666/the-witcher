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

    // Вспышка при смене персонажа (0..1, затухает)
    private float switchFlash = 0f;
    // Частицы-искры при смене персонажа
    private final List<float[]> switchParticles = new ArrayList<>(); // [x,y,vx,vy,life,maxLife,r,g,b]

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

    // ─── Цвета ───
    private static final Color NARRATOR_COLOR = new Color(180, 170, 150);
    private static final Color GERALT_COLOR = new Color(220, 220, 220);
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

        // Детектим смену правого персонажа (stranger → duke)
        if (!newRight.equals(prevRightCharacter) && !"none".equals(newRight) && !"none".equals(prevRightCharacter)
                && !newRight.equals(prevRightCharacter)) {
            switchFlash = 1.0f;
            // Генерируем частицы-искры при переключении
            for (int i = 0; i < 25; i++) {
                float px = 0.75f * 480 + (rng.nextFloat() - 0.5f) * 80;
                float py = 0.35f * 360 + (rng.nextFloat() - 0.5f) * 100;
                float vx = (rng.nextFloat() - 0.5f) * 2.0f;
                float vy = (rng.nextFloat() - 0.5f) * 2.0f;
                float cr = 200 + rng.nextInt(56);
                float cg = 140 + rng.nextInt(80);
                float cb = 30 + rng.nextInt(50);
                switchParticles.add(new float[]{px, py, vx, vy, 0, 25 + rng.nextInt(25), cr, cg, cb});
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

        // Искры от факелов
        if (tick % 4 == 0 && fadeAlpha > 0.3f) {
            // Три точки-факела на фоне
            float[] torchX = {0.18f, 0.42f, 0.72f};
            float[] torchY = {0.25f, 0.20f, 0.22f};
            int idx = rng.nextInt(torchX.length);
            sparks.add(new float[]{
                    torchX[idx] * 480 + (rng.nextFloat() - 0.5f) * 10,
                    torchY[idx] * 360 + (rng.nextFloat() - 0.5f) * 6,
                    (rng.nextFloat() - 0.5f) * 0.3f,
                    -0.3f - rng.nextFloat() * 0.4f,
                    0, 30 + rng.nextInt(35)
            });
        }
        sparks.removeIf(s -> s[4] >= s[5]);
        for (float[] s : sparks) {
            s[0] += s[2];
            s[1] += s[3];
            s[4]++;
        }
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

        // ── Тёплый свет (мерцание факелов) ──
        float flicker = 0.86f + 0.08f * (float) Math.sin(tick * 0.1) + rng.nextFloat() * 0.04f;
        Composite prevC = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.04f * flicker));
        g.setColor(new Color(255, 170, 85));
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prevC);



        // ── Искры от факелов ──
        for (float[] s : sparks) {
            float life = s[4] / s[5];
            float a = (1f - life) * fadeAlpha;
            int bright = 200 + rng.nextInt(56);
            g.setColor(new Color(bright, bright / 2 + 40, 15, Math.max(0, Math.min(255, (int) (a * 240)))));
            int sz = life < 0.4f ? 2 : 1;
            g.fillRect(Math.round(s[0] * sw / 480f), Math.round(s[1] * sh / 360f), sz, sz);
        }

        // ── Персонажи ──
        DialogEntry entry = currentEntry < entries.size() ? entries.get(currentEntry) : null;
        String activeSide = entry != null ? entry.activeSide : "none";

        // Рисуем Геральта (слева)
        drawCharacterEnhanced(g, sw, sh, geraltSprite, geraltSlide, true,
                "left".equals(activeSide), leftActiveAnim);

        // Рисуем правых персонажей с кроссфейдом (stranger уходит, duke появляется)
        if (strangerSlide > 0.001f) {
            drawCharacterEnhanced(g, sw, sh, strangerSprite, strangerSlide, false,
                    "right".equals(activeSide) && "stranger".equals(rightCharacter), rightActiveAnim);
        }
        if (dukeSlide > 0.001f) {
            drawCharacterEnhanced(g, sw, sh, dukeSprite, dukeSlide, false,
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

        // ── Вспышка при смене персонажа ──
        if (switchFlash > 0.01f) {
            Composite prevF = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, switchFlash * 0.3f));
            g.setColor(new Color(255, 220, 150));
            // Вспышка только в правой части экрана
            g.fillRect(sw / 2, 0, sw / 2, sh);
            g.setComposite(prevF);
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

        // Размер персонажа — примерно 65% высоты экрана
        float baseCharScale = (sh * 0.65f) / sprite.getHeight();

        // Активный персонаж чуть увеличивается (pop-эффект)
        float scaleBoost = 1.0f + activeAnim * 0.06f;
        float charScale = baseCharScale * scaleBoost;

        int cw = Math.round(sprite.getWidth() * charScale);
        int ch = Math.round(sprite.getHeight() * charScale);

        // Диалоговое окно занимает ~32% снизу + margin
        int dialogZone = (int) (sh * 0.36f);
        int baseY = sh - dialogZone - ch + (int)(ch * 0.08f);

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

        // ── Спрайт персонажа ──
        float alpha = fadeAlpha * Math.min(1f, slide * 1.5f);
        if (!isActive) {
            alpha *= 0.55f;
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, alpha)));
        g.drawImage(sprite, cx, cy, cw, ch, null);

        g.setComposite(prev);
    }

    private void drawDialogBox(Graphics2D g, int sw, int sh) {
        DialogEntry entry = entries.get(currentEntry);

        int boxMargin = (int) (sw * 0.03f);
        int boxH = (int) (sh * 0.30f);
        int boxW = sw - boxMargin * 2;
        int boxX = boxMargin;
        int boxY = sh - boxH - (int)(sh * 0.02f);

        // ── Фон диалогового окна ──
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, fadeAlpha * 0.88f));
        g.setColor(BOX_BG);
        g.fillRect(boxX, boxY, boxW, boxH);
        g.setComposite(prev);

        // ── Рамка (двойная пиксельная) ──
        g.setColor(BOX_BORDER);
        g.fillRect(boxX, boxY, boxW, 2);
        g.fillRect(boxX, boxY + boxH - 2, boxW, 2);
        g.fillRect(boxX, boxY, 2, boxH);
        g.fillRect(boxX + boxW - 2, boxY, 2, boxH);
        // Внутренняя рамка
        g.setColor(BOX_BORDER_INNER);
        g.fillRect(boxX + 3, boxY + 3, boxW - 6, 1);
        g.fillRect(boxX + 3, boxY + boxH - 4, boxW - 6, 1);
        g.fillRect(boxX + 3, boxY + 3, 1, boxH - 6);
        g.fillRect(boxX + boxW - 4, boxY + 3, 1, boxH - 6);

        // ── Пад-отступы ──
        int pad = (int) (sw * 0.02f);
        int textX = boxX + pad;
        int textY = boxY + pad;
        int textMaxW = boxW - pad * 2;

        int fontSize = Math.max(10, (int) (sh * 0.036f));
        Font nameFont = new Font("Monospaced", Font.BOLD, fontSize);
        Font textFont = new Font("Monospaced", Font.PLAIN, Math.max(9, fontSize - 2));

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
        Color textColor = entry.speaker == null ? entry.speakerColor : new Color(220, 210, 190);

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
            g.setFont(new Font("Monospaced", Font.BOLD, Math.max(8, fontSize - 3)));
            g.setColor(HINT_COLOR);
            String hint = "\u25B6 Enter";
            int hw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, boxX + boxW - pad - hw, boxY + boxH - pad + 2);
        }
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
