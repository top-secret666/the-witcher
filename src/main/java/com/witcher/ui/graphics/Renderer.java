package main.java.com.witcher.ui.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class Renderer extends JPanel {
    private final int virtualW;
    private final int virtualH;
    public final BufferedImage screen;
    private Sprite sprite;
    private int spriteX, spriteY;

    public Renderer(int virtualW, int virtualH, int scale) {
        this.virtualW = virtualW;
        this.virtualH = virtualH;
        this.screen = new BufferedImage(virtualW, virtualH, BufferedImage.TYPE_INT_ARGB);
        setPreferredSize(new Dimension(virtualW * scale, virtualH * scale));
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        this.spriteX = (virtualW - sprite.getWidth()) / 2;
        this.spriteY = (virtualH - sprite.getHeight()) / 2;
    }

    public void update() {
        Graphics2D g = screen.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, virtualW, virtualH);
        if (sprite != null) sprite.draw(screen, spriteX, spriteY);
        g.dispose();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Пиксельный апскейл виртуального экрана
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g2.drawImage(screen, 0, 0, getWidth(), getHeight(), null);
    }
}
