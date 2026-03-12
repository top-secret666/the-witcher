package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashScreen {
    private final SpriteSheet logoAnim;     // анимированный логотип (6 кадров, мерцание искр)
    private final Sprite background;

    // Анимированные спрайт-листы
    private final SpriteSheet witcherBar;   // ведьмак за баром, 5 кадров (5×1)
    private final SpriteSheet griffinAnim;  // грифон выглядывает справа (6 кадров, 3×2, чёрный фон удалён)
    private final Sprite drownerSprite;     // утопец выглядывает слева (статичный, с прозрачным фоном)

    private BufferedImage logoScaled;
    private BufferedImage bgScaled;
    private int bgScaledForW, bgScaledForH;

    private float alpha = 0.05f;
    private int progress = 0;
    private boolean finished = false;
    private int timer = 0;
    private int tick = 0;

    // Атмосфера
    private final List<SmokePuff> smokePuffs = new ArrayList<>();
    private BufferedImage grain;
    private float flicker = 1f;

    // Частицы (золотые искры)
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    // Цвета
    private static final Color GOLD = new Color(218, 165, 32);
    private static final Color GOLD_BRIGHT = new Color(255, 210, 80);
    private static final Color GOLD_DARK = new Color(139, 90, 10);
    private static final Color BAR_BG = new Color(30, 22, 12);
    private static final Color BAR_BORDER = new Color(140, 100, 35);
    private static final Color WARM_LIGHT = new Color(255, 170, 85);
    private static final Color SMOKE = new Color(190, 180, 165);

    public SplashScreen() {
        background = Sprite.load("/assets/sprites/splash_bg.png");

        // Анимированный логотип — 2×3 сетка (6 вариантов с мерцающими искрами)
        SpriteSheet la = SpriteSheet.load("/assets/sprites/witcher_logo_new.png", 2, 3, 8);
        logoAnim = (la != null) ? la.setPingPong(true) : null;

        // Ведьмак за баром — 5 кадров в ряд, прозрачный фон
        SpriteSheet wb = SpriteSheet.load("/assets/sprites/witcher_bar.png", 5, 1, 15);
        witcherBar = (wb != null) ? wb.setPingPong(true) : null;

        // Грифон — 3×2 сетка (6 кадров), чёрный фон удаляется
        SpriteSheet gp = SpriteSheet.load("/assets/sprites/griffin_peek.png", 3, 2, 7, true);
        griffinAnim = (gp != null) ? gp.setPingPong(true) : null;

        // Утопец — статичный с прозрачным фоном
        drownerSprite = Sprite.load("/assets/sprites/drowner_single.png");
    }

    public void update() {
        tick++;

        // Тёплое мерцание (имитация свечей/огня)
        float base = 0.86f + 0.10f * (float) Math.sin(tick * 0.12);
        float jitter = (rng.nextFloat() - 0.5f) * 0.10f;
        flicker = clamp(base + jitter, 0.72f, 1.0f);

        // Fade-in (быстрый)
        if (alpha < 1f) {
            alpha += 0.06f;
            if (alpha > 1f) alpha = 1f;
        } else if (progress < 100) {
            progress += 1;
        } else {
            timer++;
            if (timer > 80) finished = true;
        }

        // Частицы (золотые искры)
        if (tick % 4 == 0 && alpha > 0.3f) {
            float px = 40 + rng.nextFloat() * 400;
            float py = 10 + rng.nextFloat() * 180;
            float vx = (rng.nextFloat() - 0.5f) * 0.6f;
            float vy = -0.2f - rng.nextFloat() * 0.5f;
            int life = 30 + rng.nextInt(50);
            Color c = rng.nextFloat() < 0.6f ? GOLD : GOLD_BRIGHT;
            particles.add(new Particle(px, py, vx, vy, life, c, 2 + rng.nextInt(2)));
        }
        particles.removeIf(p -> !p.alive);
        for (Particle p : particles) p.update();

        // Дым
        if (tick % 8 == 0 && alpha > 0.25f) {
            float px = 35 + rng.nextFloat() * 410;
            float py = 60 + rng.nextFloat() * 120;
            float vx = (rng.nextFloat() - 0.5f) * 0.18f;
            float vy = -0.12f - rng.nextFloat() * 0.22f;
            int life = 120 + rng.nextInt(120);
            int r = 10 + rng.nextInt(18);
            smokePuffs.add(new SmokePuff(px, py, vx, vy, life, r));
        }
        smokePuffs.removeIf(p -> !p.alive);
        for (SmokePuff p : smokePuffs) p.update();

        // Анимация спрайт-листов
        if (witcherBar != null) witcherBar.update();
        if (griffinAnim != null) griffinAnim.update();
        if (logoAnim != null) logoAnim.update();
    }

    public void render(BufferedImage screen) {
        int sw = screen.getWidth();
        int sh = screen.getHeight();
        Graphics2D g = screen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // Пиксельная подача: резкие края, без «мыла»
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

        // === ФОН: чёрный ===
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        // === ФОН ТАВЕРНЫ — ВПИСАН С ПОЛЯМИ (не в упор на весь экран) ===
        if (background != null) {
            if (bgScaled == null || bgScaledForW != sw || bgScaledForH != sh) {
                bgScaledForW = sw;
                bgScaledForH = sh;
                bgScaled = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D bg = bgScaled.createGraphics();
                bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                bg.setColor(Color.BLACK);
                bg.fillRect(0, 0, sw, sh);
                int srcW = background.getWidth();
                int srcH = background.getHeight();
                float contain = Math.min((float) sw / srcW, (float) sh / srcH);
                // Чуть уменьшаем размер, чтобы фон не был «в упор» к границам окна
                float scale = contain * 0.94f;
                int drawW = Math.round(srcW * scale);
                int drawH = Math.round(srcH * scale);
                bg.drawImage(background.getImage(), (sw - drawW) / 2, (sh - drawH) / 2, drawW, drawH, null);
                bg.dispose();
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, clamp(alpha * 0.88f, 0f, 1f)));
            g.drawImage(bgScaled, 0, 0, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // === ТЁПЛОЕ МЕРЦАНИЕ СВЕТА ===
        if (alpha > 0.05f) {
            float a = alpha * 0.05f * flicker;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g.setColor(WARM_LIGHT);
            g.fillRect(0, 0, sw, sh);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // === ЛОГОТИП СВЕРХУ ПО ЦЕНТРУ (анимированный) ===
        if (logoAnim != null && alpha > 0.05f) {
            // Тайл ~1024×682. Масштаб: ширина до 75% экрана, но не больше 28% высоты
            float s = Math.min((float)(sw * 0.75f) / logoAnim.getFrameWidth(), (float)(sh * 0.28f) / logoAnim.getFrameHeight());
            int drawW = Math.max(1, Math.round(logoAnim.getFrameWidth() * s));
            int drawH = Math.max(1, Math.round(logoAnim.getFrameHeight() * s));
            int lx = (sw - drawW) / 2;
            int ly = (int)(sh * 0.02f);
            // Тень под логотипом для контраста
            Composite prev = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.4f));
            g.setColor(Color.BLACK);
            g.fillRoundRect(lx - 4, ly - 2, drawW + 8, drawH + 4, 6, 6);
            g.setComposite(prev);
            // Анимированный логотип
            logoAnim.draw(g, lx, ly, drawW, drawH, alpha);
        }

        // === УТОПЕЦ СЛЕВА (выглядывает из-за края, покачивается) ===
        if (drownerSprite != null && alpha > 0.2f) {
            // Масштаб: 60% высоты экрана
            float dpScale = (sh * 0.60f) / drownerSprite.getHeight();
            int dpW = Math.round(drownerSprite.getWidth() * dpScale);
            int dpH = Math.round(drownerSprite.getHeight() * dpScale);
            // Покачивание
            float swayX = (float) Math.sin(tick * 0.035) * 4;
            float swayY = (float) Math.sin(tick * 0.025 + 1.0) * 3;
            // Спрятан на ~40% за левым краем, показывая правую часть (морду)
            int dpX = (int) (-dpW * 0.35f + swayX);
            int dpY = (int) (sh * 0.18f + swayY);
            Composite prev = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.92f));
            g.drawImage(drownerSprite.getImage(), dpX, dpY, dpW, dpH, null);
            g.setComposite(prev);
        }

        // === ГРИФОН СПРАВА (анимированный, выглядывает из-за края) ===
        if (griffinAnim != null && alpha > 0.2f) {
            // Масштаб: 60% высоты экрана
            float gpScale = (sh * 0.60f) / griffinAnim.getFrameHeight();
            int gpW = Math.round(griffinAnim.getFrameWidth() * gpScale);
            int gpH = Math.round(griffinAnim.getFrameHeight() * gpScale);
            // Покачивание
            float swayX = (float) Math.sin(tick * 0.03 + 2.0) * 4;
            float swayY = (float) Math.sin(tick * 0.04) * 3;
            // Спрятан на ~35% за правым краем
            int gpX = (int) (sw - gpW * 0.65f + swayX);
            int gpY = (int) (sh * 0.15f + swayY);
            griffinAnim.draw(g, gpX, gpY, gpW, gpH, alpha * 0.92f);
        }

        // === ВЕДЬМАК ЗА БАРОМ (по центру, крупный) ===
        if (witcherBar != null && alpha > 0.1f) {
            // Каждый кадр ~205×1024. Масштабируем по ширине — ~22% ширины экрана
            float wbScale = (sw * 0.22f) / witcherBar.getFrameWidth();
            int wbW = Math.round(witcherBar.getFrameWidth() * wbScale);
            int wbH = Math.round(witcherBar.getFrameHeight() * wbScale);
            int wbX = (sw - wbW) / 2;
            // Выровнять по низу области сцены (чтобы ноги не уходили за экран слишком сильно)
            int wbY = sh - wbH + (int)(wbH * 0.31f);
            witcherBar.draw(g, wbX, wbY, wbW, wbH, alpha * 0.95f);
        }

        // === ЧАСТИЦЫ ===
        for (Particle p : particles) p.draw(g);

        // === ДЫМ ===
        for (SmokePuff p : smokePuffs) p.draw(g);

        // === ТЁМНАЯ ПОЛОСА ВНИЗУ (только для прогресс-бара) ===
        int barZoneH = 28;
        GradientPaint fadeBottom = new GradientPaint(
                0, sh - barZoneH - 15, new Color(0, 0, 0, 0),
                0, sh - barZoneH, new Color(0, 0, 0, 180));
        g.setPaint(fadeBottom);
        g.fillRect(0, sh - barZoneH - 15, sw, 15);
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, sh - barZoneH, sw, barZoneH);

        // === ПРОГРЕСС-БАР (внизу экрана) ===
        int barW = (int) (sw * 0.55);
        int barH = 8;
        int barX = (sw - barW) / 2;
        int barY = sh - 20;

        g.setColor(BAR_BG);
        g.fillRect(barX, barY, barW, barH);

        int fillW = (int) Math.round(barW * (progress / 100.0));
        if (fillW > 0) {
            GradientPaint barGrad = new GradientPaint(barX, barY, GOLD_DARK, barX + barW, barY, GOLD);
            g.setPaint(barGrad);
            g.fillRect(barX, barY, fillW, barH);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
            g.setColor(Color.WHITE);
            g.fillRect(barX, barY, fillW, barH / 3);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
        g.setColor(BAR_BORDER);
        g.setStroke(new BasicStroke(1f));
        g.drawRect(barX, barY, barW, barH);

        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        g.setColor(GOLD);
        String loadText = "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430... " + progress + "%";
        int textW = g.getFontMetrics().stringWidth(loadText);
        g.drawString(loadText, (sw - textW) / 2, barY - 5);

        // === ЗЕРНО ===
        if (alpha > 0.1f) {
            if (grain == null) grain = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            if (tick % 5 == 0) {
                for (int y = 0; y < grain.getHeight(); y++) {
                    for (int x = 0; x < grain.getWidth(); x++) {
                        int n = 120 + rng.nextInt(110);
                        int a = 12 + rng.nextInt(14);
                        grain.setRGB(x, y, (a << 24) | (n << 16) | (n << 8) | n);
                    }
                }
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.04f));
            for (int y = 0; y < sh; y += grain.getHeight()) {
                for (int x = 0; x < sw; x += grain.getWidth()) {
                    g.drawImage(grain, x, y, null);
                }
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // === ВИНЬЕТКА ===
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(sw / 2f, sh * 0.35f),
                Math.max(sw, sh) * 0.75f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 160)}
        );
        Paint prevPaint = g.getPaint();
        g.setPaint(vignette);
        // Уменьшим силу виньетки — пусть фон не слишком затемняется по краям
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.30f));
        g.fillRect(0, 0, sw, sh);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        g.setPaint(prevPaint);

        g.dispose();
    }

    public boolean isFinished() {
        return finished;
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    // === Частица ===
    private static class Particle {
        float x, y, vx, vy;
        int life, maxLife, size;
        Color color;
        boolean alive = true;

        Particle(float x, float y, float vx, float vy, int life, Color color, int size) {
            this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life; this.color = color; this.size = size;
        }

        void update() {
            x += vx; y += vy; life--;
            if (life <= 0) alive = false;
        }

        void draw(Graphics2D g) {
            float a = (float) life / maxLife;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a * 0.7f));
            g.setColor(color);
            g.fillRect((int) x, (int) y, size, size);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }

    private class SmokePuff {
        float x, y, vx, vy;
        int life, maxLife;
        float r;
        boolean alive = true;

        SmokePuff(float x, float y, float vx, float vy, int life, float r) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.r = r;
        }

        void update() {
            x += vx;
            y += vy;
            r += 0.03f;
            life--;
            if (life <= 0) alive = false;
        }

        void draw(Graphics2D g) {
            float t = 1f - (life / (float) maxLife);
            float a = (float) (Math.sin(t * Math.PI) * 0.22f); // максимум в середине жизни
            if (a <= 0f) return;

            Composite prev = g.getComposite();
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g.setColor(SMOKE);

            int cx = (int) x;
            int cy = (int) y;
            int rr = (int) r;

            // "мягкость" без blur: несколько овалов
            g.fillOval(cx - rr, cy - rr, rr * 2, rr * 2);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a * 0.55f));
            g.fillOval(cx - rr - 4, cy - rr + 2, rr * 2, rr * 2);
            g.fillOval(cx - rr + 6, cy - rr - 1, rr * 2, rr * 2);
            g.setComposite(prev);
        }
    }
}
