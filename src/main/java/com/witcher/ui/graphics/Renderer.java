package main.java.com.witcher.ui.graphics;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Виртуальный кадр 480×360 → displayFrame (×2, без сглаживания) → панель 1:1.
 */
public class Renderer extends JPanel {

    private final int virtualW;
    private final int virtualH;
    private final int pixelScale;
    public final BufferedImage screen;
    private final BufferedImage displayFrame;

    private Sprite sprite;
    private int spriteX, spriteY;

    public Renderer(int virtualW, int virtualH, int scale) {
        if (scale < 1) {
            scale = 1;
        }
        this.virtualW = virtualW;
        this.virtualH = virtualH;
        this.pixelScale = scale;
        int dw = virtualW * pixelScale;
        int dh = virtualH * pixelScale;
        this.screen = new BufferedImage(virtualW, virtualH, BufferedImage.TYPE_INT_ARGB);
        this.displayFrame = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(dw, dh));
        setFocusable(true);
        setDoubleBuffered(false);
        setOpaque(true);
        setBackground(Color.BLACK);
    }

    public int getVirtualW() {
        return virtualW;
    }

    public int getVirtualH() {
        return virtualH;
    }

    public int getPixelScale() {
        return pixelScale;
    }

    public int getDisplayWidth() {
        return displayFrame.getWidth();
    }

    public int getDisplayHeight() {
        return displayFrame.getHeight();
    }

    public void setSprite(Sprite sprite) {
        this.sprite = sprite;
        this.spriteX = (virtualW - sprite.getWidth()) / 2;
        this.spriteY = (virtualH - sprite.getHeight()) / 2;
    }

    public void update() {
        Graphics2D g = screen.createGraphics();
        try {
            PixelDraw.applyNearest(g);
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, virtualW, virtualH);
            if (sprite != null) {
                sprite.draw(screen, spriteX, spriteY);
            }
        } finally {
            g.dispose();
        }
        present();
    }

    public void present() {
        if (pixelScale == 1) {
            Graphics2D g = displayFrame.createGraphics();
            try {
                PixelDraw.applyNearest(g);
                g.setColor(Color.BLACK);
                g.fillRect(0, 0, getDisplayWidth(), getDisplayHeight());
                g.drawImage(screen, 0, 0, null);
            } finally {
                g.dispose();
            }
        } else {
            PixelDraw.blitIntegerScale(screen, displayFrame, pixelScale);
        }
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        int pw = getWidth();
        int ph = getHeight();
        if (pw <= 0 || ph <= 0) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            PixelDraw.applyNearest(g2);
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, pw, ph);

            int fw = displayFrame.getWidth();
            int fh = displayFrame.getHeight();
            if (fw <= 0 || fh <= 0) {
                return;
            }

            if (pw == fw && ph == fh) {
                g2.drawImage(displayFrame, 0, 0, null);
            } else {
                int scale = Math.max(1, Math.min(pw / fw, ph / fh));
                int dw = fw * scale;
                int dh = fh * scale;
                int ox = (pw - dw) / 2;
                int oy = (ph - dh) / 2;
                if (scale == 1) {
                    g2.drawImage(displayFrame, ox, oy, null);
                } else {
                    BufferedImage up = new BufferedImage(dw, dh, BufferedImage.TYPE_INT_RGB);
                    PixelDraw.blitIntegerScale(displayFrame, up, scale);
                    g2.drawImage(up, ox, oy, null);
                }
            }
        } finally {
            g2.dispose();
        }
    }
}
