package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashScreen {
    private final Sprite logo;
    private final Sprite background;      // PNG-фон таверны
    private final AnimatedGif witcherGif;  // GIF-ведьмак (анимация)
    private final Sprite witcherSprite;    // PNG-ведьмак (fallback)
    private final AnimatedGif witcherBackGif; // GIF-спина ведьмака (на фоне за стойкой)
    private final Sprite witcherBackSprite;   // PNG-спина ведьмака (fallback)
    private BufferedImage logoScaled;
    private BufferedImage bgScaled;
    private BufferedImage witcherScaled;
    private BufferedImage witcherBackScaled;
    private int witcherBackScaledW;
    private int witcherBackScaledH;

    private float alpha = 0f;
    private int progress = 0;
    private boolean finished = false;
    private int timer = 0;
    private int tick = 0;

    // Позиция бегущего ведьмака
    private float witcherX = -120;

    // Атмосфера: дым, мерцание, зерно
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
        logo = Sprite.load("/assets/sprites/witcher_logo.png");
        background = Sprite.load("/assets/sprites/splash_bg.png");
        witcherGif = AnimatedGif.load("/assets/sprites/witcher_run.gif");
        witcherSprite = Sprite.load("/assets/sprites/witcher_run.png");

        // Спина ведьмака за барной стойкой (положи ресурс сюда):
        // /assets/sprites/witcher_back.gif  (или .png)
        witcherBackGif = AnimatedGif.load("/assets/sprites/witcher_back.gif");
        witcherBackSprite = Sprite.load("/assets/sprites/witcher_back.png");
    }

    public void update() {
        tick++;

        // Тёплое мерцание (имитация свечей/огня)
        float base = 0.86f + 0.10f * (float) Math.sin(tick * 0.12);
        float jitter = (rng.nextFloat() - 0.5f) * 0.10f;
        flicker = clamp(base + jitter, 0.72f, 1.0f);

        // Fade-in
        if (alpha < 1f) {
            alpha += 0.012f;
            if (alpha > 1f) alpha = 1f;
        } else if (progress < 100) {
            progress += 1;
        } else {
            timer++;
            if (timer > 80) finished = true;
        }

        // Ведьмак бежит справа налево → слева направо
        witcherX += 1.8f;
        if (witcherX > 520) witcherX = -120;

        // Частицы
        if (tick % 4 == 0 && alpha > 0.3f) {
            float px = 80 + rng.nextFloat() * 320;
            float py = 20 + rng.nextFloat() * 160;
            float vx = (rng.nextFloat() - 0.5f) * 0.6f;
            float vy = -0.2f - rng.nextFloat() * 0.5f;
            int life = 30 + rng.nextInt(50);
            Color c = rng.nextFloat() < 0.6f ? GOLD : GOLD_BRIGHT;
            particles.add(new Particle(px, py, vx, vy, life, c, 2 + rng.nextInt(2)));
        }
        particles.removeIf(p -> !p.alive);
        for (Particle p : particles) p.update();

        // Дым (медленные полупрозрачные "облака")
        if (tick % 7 == 0 && alpha > 0.25f) {
            float px = 35 + rng.nextFloat() * 410;
            float py = 150 + rng.nextFloat() * 120;
            float vx = (rng.nextFloat() - 0.5f) * 0.18f;
            float vy = -0.12f - rng.nextFloat() * 0.22f;
            int life = 120 + rng.nextInt(120);
            int r = 10 + rng.nextInt(18);
            smokePuffs.add(new SmokePuff(px, py, vx, vy, life, r));
        }
        smokePuffs.removeIf(p -> !p.alive);
        for (SmokePuff p : smokePuffs) p.update();
    }

    public void render(BufferedImage screen) {
        int sw = screen.getWidth();
        int sh = screen.getHeight();
        Graphics2D g = screen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // === ФОН: чёрный ===
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        // === Фон-картинка таверны (если есть) ===
        if (background != null) {
            if (bgScaled == null) {
                bgScaled = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB);
                Graphics2D bg = bgScaled.createGraphics();
                bg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                bg.drawImage(background.getImage(), 0, 0, sw, sh, null);
                bg.dispose();
            }

            // Фон с прозрачностью (чуть затемнённый, чтобы лого было видно)
            float bgAlpha = alpha * 0.35f;
            bgAlpha = clamp(bgAlpha, 0f, 1f);

            // Делаем "слои" фона: верхняя часть -> спина ведьмака -> нижняя часть (стойка перекрывает)
            int barOccludeY = (int) Math.round(sh * 0.58);
            barOccludeY = Math.max(1, Math.min(sh - 1, barOccludeY));

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha));

            // Верхняя часть (стена/полки)
            g.drawImage(bgScaled,
                    0, 0, sw, barOccludeY,
                    0, 0, sw, barOccludeY,
                    null);

            // === СПИНА ВЕДЬМАКА (за стойкой) ===
            // Рисуем между слоями фона, чтобы стойка могла перекрыть низ.
            float backA = clamp(alpha * 0.55f, 0f, 1f);
            int backTargetH = (int) Math.round(sh * 0.42);
            backTargetH = Math.max(60, Math.min(sh, backTargetH));
            int backY = barOccludeY - backTargetH + 26; // подгонка под стойку

            if (witcherBackGif != null) {
                float scale = backTargetH / (float) Math.max(1, witcherBackGif.getHeight());
                int backW = (int) Math.round(witcherBackGif.getWidth() * scale);
                int backX = (sw - backW) / 2;
                witcherBackGif.paint(g, backX, backY, scale, backA);
            } else if (witcherBackSprite != null) {
                float ratio = (float) witcherBackSprite.getWidth() / Math.max(1, witcherBackSprite.getHeight());
                int backW = Math.max(1, Math.round(backTargetH * ratio));
                int backX = (sw - backW) / 2;

                if (witcherBackScaled == null || witcherBackScaledW != backW || witcherBackScaledH != backTargetH) {
                    witcherBackScaledW = backW;
                    witcherBackScaledH = backTargetH;
                    witcherBackScaled = new BufferedImage(backW, backTargetH, BufferedImage.TYPE_INT_ARGB);
                    Graphics2D wg = witcherBackScaled.createGraphics();
                    wg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                    wg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                    wg.drawImage(witcherBackSprite.getImage(), 0, 0, backW, backTargetH, null);
                    wg.dispose();
                }

                Composite prev = g.getComposite();
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, backA));
                g.drawImage(witcherBackScaled, backX, backY, null);
                g.setComposite(prev);
            }

            // Нижняя часть (стойка/передний план) перекрывает нижнюю часть ведьмака
            g.drawImage(bgScaled,
                    0, barOccludeY, sw, sh,
                    0, barOccludeY, sw, sh,
                    null);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // === ТЁПЛОЕ МЕРЦАНИЕ СВЕТА (над фоном, под всем) ===
        if (alpha > 0.05f) {
            float a = alpha * 0.08f * flicker;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g.setColor(WARM_LIGHT);
            g.fillRect(0, 0, sw, sh);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // === ЧАСТИЦЫ (за логотипом) ===
        for (Particle p : particles) p.draw(g);

        // === ДЫМ (за логотипом) ===
        for (SmokePuff p : smokePuffs) p.draw(g);

        // === ЛОГОТИП (без свечения, чёрный фон) ===
        if (logo != null) {
            int maxW = sw - 60;
            int maxH = sh - 130;
            float s = Math.min((float) maxW / logo.getWidth(), (float) maxH / logo.getHeight());
            int drawW = Math.max(1, Math.round(logo.getWidth() * s));
            int drawH = Math.max(1, Math.round(logo.getHeight() * s));

            if (logoScaled == null) {
                logoScaled = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D lg = logoScaled.createGraphics();
                lg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                lg.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                lg.drawImage(logo.getImage(), 0, 0, drawW, drawH, null);
                lg.dispose();
            }

            int x = (sw - drawW) / 2;
            int y = (maxH - drawH) / 2 - 18; // чуть выше
            if (y < 8) y = 8;

            // Логотип с fade-in (БЕЗ свечения)
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.drawImage(logoScaled, x, y, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            g.setFont(new Font("Serif", Font.BOLD, 48));
            g.setColor(GOLD);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            String t = "WITCHER";
            int tw = g.getFontMetrics().stringWidth(t);
            g.drawString(t, (sw - tw) / 2, sh / 2);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // === БЕГУЩИЙ ВЕДЬМАК (GIF с движением; PNG fallback) ===
        int targetH = 80;
        int bob = (int) Math.round(Math.sin(tick * 0.22) * 2.0);
        int wy = sh - 55 - targetH + bob; // чуть выше прогресс-бара

        if (witcherGif != null) {
            float scale = targetH / (float) Math.max(1, witcherGif.getHeight());
            witcherGif.paint(g, (int) witcherX, wy, scale, 1f);
        } else if (witcherSprite != null) {
            float ratio = (float) witcherSprite.getWidth() / witcherSprite.getHeight();
            int wW = Math.round(targetH * ratio);

            if (witcherScaled == null) {
                witcherScaled = new BufferedImage(wW, targetH, BufferedImage.TYPE_INT_ARGB);
                Graphics2D wg = witcherScaled.createGraphics();
                wg.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                wg.drawImage(witcherSprite.getImage(), 0, 0, wW, targetH, null);
                wg.dispose();
            }
            g.drawImage(witcherScaled, (int) witcherX, wy, null);
        }

        // === ПРОГРЕСС-БАР ===
        int barW = (int) (sw * 0.55);
        int barH = 12;
        int barX = (sw - barW) / 2;
        int barY = sh - 30;

        // Декоративные уголки
        g.setColor(BAR_BORDER);
        g.fillRect(barX - 6, barY - 1, 4, barH + 2);
        g.fillRect(barX + barW + 2, barY - 1, 4, barH + 2);

        // Фон бара
        g.setColor(BAR_BG);
        g.fillRect(barX, barY, barW, barH);

        // Заполнение (золотой градиент)
        int fillW = (int) Math.round(barW * (progress / 100.0));
        if (fillW > 0) {
            GradientPaint barGrad = new GradientPaint(barX, barY, GOLD_DARK, barX + barW, barY, GOLD);
            g.setPaint(barGrad);
            g.fillRect(barX, barY, fillW, barH);

            // Блик
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
            g.setColor(Color.WHITE);
            g.fillRect(barX, barY, fillW, barH / 3);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // Рамка
        g.setColor(BAR_BORDER);
        g.setStroke(new BasicStroke(1.5f));
        g.drawRect(barX, barY, barW, barH);

        // Текст
        g.setFont(new Font("Serif", Font.BOLD, 14));
        g.setColor(GOLD);
        String loadText = "Загрузка... " + progress + "%";
        int textW = g.getFontMetrics().stringWidth(loadText);
        g.drawString(loadText, (sw - textW) / 2, barY - 8);

        // === ЗЕРНО (очень лёгкий шум) ===
        if (alpha > 0.1f) {
            if (grain == null) {
                grain = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            }
            if (tick % 5 == 0) {
                for (int y = 0; y < grain.getHeight(); y++) {
                    for (int x = 0; x < grain.getWidth(); x++) {
                        int n = 120 + rng.nextInt(110);
                        int a = 12 + rng.nextInt(14);
                        int argb = (a << 24) | (n << 16) | (n << 8) | n;
                        grain.setRGB(x, y, argb);
                    }
                }
            }

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.06f));
            for (int y = 0; y < sh; y += grain.getHeight()) {
                for (int x = 0; x < sw; x += grain.getWidth()) {
                    g.drawImage(grain, x, y, null);
                }
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }

        // === ВИНЬЕТКА (фокус на центре) ===
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Float(sw / 2f, sh / 2f),
                Math.max(sw, sh) * 0.70f,
                new float[]{0f, 1f},
                new Color[]{new Color(0, 0, 0, 0), new Color(0, 0, 0, 200)}
        );
        Paint prevPaint = g.getPaint();
        g.setPaint(vignette);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.65f));
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
