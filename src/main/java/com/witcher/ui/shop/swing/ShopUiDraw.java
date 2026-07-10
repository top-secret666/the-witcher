package main.java.com.witcher.ui.shop.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/** Общие примитивы отрисовки UI лавки (Swing). */
public final class ShopUiDraw {

    private ShopUiDraw() {
    }

    public static void drawGoldHudChip(Graphics2D g, Rectangle box, float alpha) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(10, 7, 3, 200));
        g.fillRoundRect(box.x, box.y, box.width, box.height, 6, 6);
        g.setColor(new Color(140, 105, 45, 200));
        g.drawRoundRect(box.x, box.y, box.width, box.height, 6, 6);
        g.setColor(new Color(255, 210, 90, 60));
        g.drawRoundRect(box.x + 1, box.y + 1, box.width - 2, box.height - 2, 5, 5);
        g.setComposite(prev);
    }

    /** Тёмная обводка — текст читается на золотой рамке карточки. */
    public static void drawOutlinedText(Graphics2D g, String text, int tx, int ty, Color fill) {
        tx = Math.round(tx);
        ty = Math.round(ty);
        g.setColor(new Color(20, 12, 4, 220));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) {
                    g.drawString(text, tx + dx, ty + dy);
                }
            }
        }
        g.setColor(fill);
        g.drawString(text, tx, ty);
    }
}
