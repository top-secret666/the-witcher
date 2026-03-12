package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SplashScreen {
    private final Sprite logo;
    private BufferedImage logoScaled;
    private float alpha = 0f;
    private int progress = 0;
    private boolean finished = false;
    private int timer = 0;
    private int tick = 0;

    // Бегущий ведьмак
    private final PixelWitcher witcher;

    // Магические частицы
    private final List<Particle> particles = new ArrayList<>();
    private final Random rng = new Random();

    // Цвета в стиле Witcher
    private static final Color BG_TOP = new Color(10, 8, 5);
    private static final Color BG_BOTTOM = new Color(30, 20, 10);
    private static final Color GOLD = new Color(218, 165, 32);
    private static final Color GOLD_BRIGHT = new Color(255, 210, 80);
    private static final Color GOLD_DARK = new Color(139, 90, 10);
    private static final Color BAR_BG = new Color(30, 22, 12);
    private static final Color BAR_BORDER = new Color(140, 100, 35);
    private static final Color GROUND = new Color(25, 18, 8);

    public SplashScreen() {
        logo = Sprite.load("/assets/sprites/witcher_logo.png");
        // Ведьмак бежит внизу экрана, масштаб спрайта x3
        witcher = new PixelWitcher(-50, 280, 1.8f, 3);
    }

    public void update() {
        tick++;

        // Fade-in логотипа
        if (alpha < 1f) {
            alpha += 0.012f;
            if (alpha > 1f) alpha = 1f;
        } else if (progress < 100) {
            progress += 1;
        } else {
            timer++;
            if (timer > 80) finished = true;
        }

        // Обновляем ведьмака
        witcher.update(500);

        // Генерируем новые частицы (золотые искры вокруг логотипа)
        if (tick % 3 == 0 && alpha > 0.2f) {
            float px = 100 + rng.nextFloat() * 280;
            float py = 40 + rng.nextFloat() * 180;
            float vx = (rng.nextFloat() - 0.5f) * 0.8f;
            float vy = -0.3f - rng.nextFloat() * 0.6f;
            int life = 40 + rng.nextInt(60);
            Color c;
            float r = rng.nextFloat();
            if (r < 0.5f) c = GOLD;
            else if (r < 0.8f) c = GOLD_BRIGHT;
            else c = new Color(255, 140, 40);  // оранжевая искра
            particles.add(new Particle(px, py, vx, vy, life, c, 2 + rng.nextInt(3)));
        }

        // Обновляем частицы
        particles.removeIf(p -> !p.alive);
        for (Particle p : particles) p.update();
    }

    public void render(BufferedImage screen) {
        int sw = screen.getWidth();
        int sh = screen.getHeight();
        Graphics2D g = screen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // === ФОН: тёмный градиент ===
        GradientPaint bgGrad = new GradientPaint(0, 0, BG_TOP, 0, sh, BG_BOTTOM);
        g.setPaint(bgGrad);
        g.fillRect(0, 0, sw, sh);

        // === ЗЕМЛЯ: полоска внизу для ведьмака ===
        g.setColor(GROUND);
        g.fillRect(0, sh - 50, sw, 50);
        // Линия горизонта
        g.setColor(new Color(60, 40, 15));
        g.drawLine(0, sh - 50, sw, sh - 50);

        // === ЧАСТИЦЫ (за логотипом) ===
        for (Particle p : particles) p.draw(g);

        // === ЛОГОТИП ===
        if (logo != null) {
            int maxW = sw - 40;
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
            int y = (maxH - drawH) / 2 + 5;

            // Золотое свечение (пульсирующее)
            if (alpha > 0.3f) {
                float pulse = (float) (0.08f + 0.04f * Math.sin(tick * 0.05));
                float glowAlpha = Math.min(pulse, (alpha - 0.3f) * 0.3f);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha));
                int glow = 35;
                g.setColor(GOLD);
                g.fillRoundRect(x - glow, y - glow, drawW + glow * 2, drawH + glow * 2, 30, 30);
            }

            // Логотип
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

        // === БЕГУЩИЙ ВЕДЬМАК ===
        witcher.draw(g);

        // === ПРОГРЕСС-БАР ===
        int barW = (int) (sw * 0.55);
        int barH = 12;
        int barX = (sw - barW) / 2;
        int barY = sh - 30;

        // Декоративные уголки
        g.setColor(BAR_BORDER);
        int corner = 4;
        g.fillRect(barX - corner - 2, barY - 1, corner, barH + 2);
        g.fillRect(barX + barW + 2, barY - 1, corner, barH + 2);

        // Фон бара
        g.setColor(BAR_BG);
        g.fillRect(barX, barY, barW, barH);

        // Заполнение
        int fillW = (int) Math.round(barW * (progress / 100.0));
        if (fillW > 0) {
            GradientPaint barGrad = new GradientPaint(barX, barY, GOLD_DARK, barX + barW, barY, GOLD);
            g.setPaint(barGrad);
            g.fillRect(barX, barY, fillW, barH);

            // Блик на заполненной части
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
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

        g.dispose();
    }

    public boolean isFinished() {
        return finished;
    }

    // === Внутренний класс: магическая частица ===
    private static class Particle {
        float x, y, vx, vy;
        int life, maxLife, size;
        Color color;
        boolean alive = true;

        Particle(float x, float y, float vx, float vy, int life, Color color, int size) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.life = life;
            this.maxLife = life;
            this.color = color;
            this.size = size;
        }

        void update() {
            x += vx;
            y += vy;
            life--;
            if (life <= 0) alive = false;
        }

        void draw(Graphics2D g) {
            float a = (float) life / maxLife;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a * 0.8f));
            g.setColor(color);
            g.fillRect((int) x, (int) y, size, size);
            // Яркий центр
            if (size > 2) {
                g.setColor(Color.WHITE);
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a * 0.5f));
                g.fillRect((int) x + 1, (int) y + 1, size - 2, size - 2);
            }
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        }
    }
}
