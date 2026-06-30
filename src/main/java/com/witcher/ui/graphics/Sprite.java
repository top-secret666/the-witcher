package main.java.com.witcher.ui.graphics;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Sprite {
    private final BufferedImage image;

    public Sprite(BufferedImage image) {
        this.image = image;
    }

    public static Sprite load(String resourcePath) {
        return load(resourcePath, true);
    }

    /** Загрузка без сообщения в консоль — для опциональных ассетов и цепочек fallback. */
    public static Sprite loadOptional(String resourcePath) {
        return load(resourcePath, false);
    }

    private static Sprite load(String resourcePath, boolean logMissing) {
        String relative = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;

        BufferedImage fromClasspath = readClasspath("/" + relative);
        if (fromClasspath != null) {
            return new Sprite(fromClasspath);
        }

        for (Path root : resourceRoots()) {
            File file = root.resolve(relative).toFile();
            if (!file.isFile()) {
                continue;
            }
            try {
                BufferedImage img = ImageIO.read(file);
                if (img != null) {
                    return new Sprite(img);
                }
            } catch (IOException ignored) {
                // try next root
            }
        }

        if (logMissing) {
            System.err.println("Файл не найден: " + resourcePath);
            System.err.println("Проверен classpath и каталоги: src/main/resources, output/bin");
        }
        return null;
    }

    private static BufferedImage readClasspath(String resourcePath) {
        try (InputStream is = Sprite.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static List<Path> resourceRoots() {
        List<Path> roots = new ArrayList<>();
        roots.add(Paths.get(System.getProperty("user.dir"), "src", "main", "resources"));

        try {
            URL codeSource = Sprite.class.getProtectionDomain().getCodeSource().getLocation();
            if (codeSource != null) {
                Path outputDir = Paths.get(codeSource.toURI()).toAbsolutePath().normalize();
                if (outputDir.toFile().isDirectory()) {
                    roots.add(outputDir);
                }
                Path projectResources = outputDir.getParent().resolve("src").resolve("main").resolve("resources");
                roots.add(projectResources.normalize());
            }
        } catch (URISyntaxException | SecurityException ignored) {
            // ignore
        }

        return roots;
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
