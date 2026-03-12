package main.java.com.witcher.ui.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Sprite {
    private final BufferedImage image;

    public Sprite(BufferedImage image) {
        this.image = image;
    }

    public static Sprite load(String resourcePath) {
        // 1) Try classpath (works when resources are on the runtime classpath)
        try (InputStream is = Sprite.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                BufferedImage img = ImageIO.read(is);
                if (img != null) return new Sprite(img);
            }
        } catch (IOException ignored) {
            // fallthrough
        }

        // 2) Fallback: load directly from source resources folder (useful during VS Code runs)
        // Example: /assets/sprites/witcher_logo.png -> <project>/src/main/resources/assets/sprites/witcher_logo.png
        String relative = resourcePath;
        if (relative.startsWith("/")) relative = relative.substring(1);

        Path filePath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources").resolve(relative);
        File file = filePath.toFile();
        if (!file.exists()) {
            System.out.println("Файл не найден: " + resourcePath);
            System.out.println("Пробовал classpath и путь: " + filePath);
            return null;
        }

        try {
            BufferedImage img = ImageIO.read(file);
            if (img == null) return null;
            return new Sprite(img);
        } catch (IOException e) {
            return null;
        }
    }

    public void draw(Graphics2D g, int x, int y) {
        g.drawImage(image, x, y, null);
    }

    public void draw(Graphics2D g, int x, int y, int w, int h) {
        g.drawImage(image, x, y, w, h, null);
    }

    public void draw(BufferedImage target, int x, int y) {
        Graphics2D g = target.createGraphics();
        draw(g, x, y);
        g.dispose();
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public BufferedImage getImage() {
        return image;
    }
}
