package main.java.com.witcher.ui.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GameWindow {
    private final JFrame frame;
    private final Renderer renderer;
    private Sprite sprite;
    private SplashScreen splashScreen;
    private boolean splashActive = true;

    public GameWindow() {
        frame = new JFrame("Witcher - Pixel Prototype");
        renderer = new Renderer(480, 360, 2); // virtual res 480x360 scaled x2 = окно 960x720
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(renderer, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
        splashScreen = new SplashScreen();
    }

    private void initSprite() {
        // После заставки пока показываем тот же логотип (позже заменим на меню/игру)
        sprite = Sprite.load("/assets/sprites/witcher_logo.png");
        if (sprite == null) {
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int col;
                    if (x == 0 || y == 0 || x == 15 || y == 15) {
                        col = 0xFF000000;
                    } else {
                        col = ((x + y) % 2 == 0) ? 0xFFFF00FF : 0xFF00FFFF;
                    }
                    img.setRGB(x, y, col);
                }
            }
            sprite = new Sprite(img);
        }
        renderer.setSprite(sprite);
    }

    public void start() {
        frame.setVisible(true);
        Timer timer = new Timer(16, e -> {
            if (splashActive) {
                splashScreen.update();
                splashScreen.render(renderer.screen);
                renderer.repaint();
                if (splashScreen.isFinished()) {
                    splashActive = false;
                    initSprite();
                }
            } else {
                renderer.update();
                renderer.repaint();
            }
        });
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GameWindow().start());
    }
}
