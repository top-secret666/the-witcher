package main.java.com.witcher.ui.graphics;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SplashScreen {
    private final Sprite logo;
    private float alpha = 0f; // прозрачность для fade-in
    private int progress = 0; // прогресс загрузки
    private boolean finished = false;
    private int timer = 0;

    public SplashScreen() {
        // Подключаем логотип
        logo = Sprite.load("/assets/sprites/witcher_logo.png");
    }

    public void update() {
        // Fade-in логотипа
        if (alpha < 1f) {
            alpha += 0.02f;
            if (alpha > 1f) alpha = 1f;
        } else if (progress < 100) {
            progress += 1;
        } else {
            timer++;
            if (timer > 60) finished = true; // задержка после загрузки
        }
    }

    public void render(BufferedImage screen) {
        Graphics2D g = screen.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screen.getWidth(), screen.getHeight());
        if (logo != null) {
            // Впишем логотип в экран, оставив место под прогресс-бар
            int maxW = screen.getWidth() - 16;
            int maxH = screen.getHeight() - 56;
            float s = Math.min((float) maxW / logo.getWidth(), (float) maxH / logo.getHeight());
            if (s > 1f) s = 1f;
            int drawW = Math.max(1, Math.round(logo.getWidth() * s));
            int drawH = Math.max(1, Math.round(logo.getHeight() * s));

            int x = (screen.getWidth() - drawW) / 2;
            int y = (maxH - drawH) / 2;

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            logo.draw(g, x, y, drawW, drawH);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        } else {
            // Если логотип не найден — покажем текст, чтобы было понятно, что экран работает
            g.setColor(Color.WHITE);
            g.drawString("WITCHER", 10, 20);
        }
        // Прогресс-бар
        int barW = 100, barH = 8;
        int barX = (screen.getWidth() - barW) / 2;
        int barY = screen.getHeight() - 40;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX, barY, barW, barH);
        g.setColor(Color.YELLOW);
        g.fillRect(barX, barY, progress, barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX, barY, barW, barH);
        g.drawString("Загрузка... " + progress + "%", barX, barY - 4);
        g.dispose();
    }

    public boolean isFinished() {
        return finished;
    }
}
