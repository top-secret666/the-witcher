package main.java.com.witcher.ui.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AnimatedGif {
    private final ImageIcon icon;
    private final JComponent observer;

    private AnimatedGif(ImageIcon icon) {
        this.icon = icon;
        this.observer = new JLabel();
    }

    public static AnimatedGif load(String resourcePath) {
        // 1) Classpath
        URL url = AnimatedGif.class.getResource(resourcePath);
        if (url != null) {
            ImageIcon icon = new ImageIcon(url);
            if (icon.getIconWidth() > 0 && icon.getIconHeight() > 0) return new AnimatedGif(icon);
        }

        // 2) Fallback: <project>/src/main/resources/<resourcePath>
        String relative = resourcePath;
        if (relative.startsWith("/")) relative = relative.substring(1);
        Path filePath = Paths.get(System.getProperty("user.dir"), "src", "main", "resources").resolve(relative);
        File file = filePath.toFile();
        if (!file.exists()) {
            return null;
        }

        ImageIcon icon = new ImageIcon(file.getAbsolutePath());
        if (icon.getIconWidth() <= 0 || icon.getIconHeight() <= 0) return null;
        return new AnimatedGif(icon);
    }

    public int getWidth() {
        return icon.getIconWidth();
    }

    public int getHeight() {
        return icon.getIconHeight();
    }

    public void paint(Graphics2D g, int x, int y, float scale, float alpha) {
        Composite prev = g.getComposite();
        if (alpha < 1f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0f, Math.min(1f, alpha))));
        }

        AffineTransform oldTx = g.getTransform();
        g.translate(x, y);
        g.scale(scale, scale);
        icon.paintIcon(observer, g, 0, 0);
        g.setTransform(oldTx);
        g.setComposite(prev);
    }
}
