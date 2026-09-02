package main.java.com.witcher.ui.shop.swing;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Вращающийся ореол вокруг особых предметов (кошелёк, зелье, карта, оружие)
 * и мягкое стартовое свечение перед появлением ореола.
 */
public final class ShopItemHaloDraw {

    private ShopItemHaloDraw() {
    }

    /** Маленькое свечение до появления спрайта-ореола. */
    public static void drawSeedGlow(Graphics2D g, int cx, int cy, int itemSize, float alpha) {
        if (alpha <= 0.02f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.28f));
        g.setColor(new Color(255, 210, 90));
        int outer = Math.round(itemSize * 1.35f);
        g.fillOval(cx - outer / 2, cy - outer / 2, outer, outer);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.5f));
        g.setColor(new Color(255, 235, 140));
        int mid = Math.round(itemSize * 0.75f);
        g.fillOval(cx - mid / 2, cy - mid / 2, mid, mid);
        g.setComposite(prev);
    }

    /**
     * Ореол вокруг центра предмета. {@code flyT} 0..1 — чем дальше к сумке, тем сильнее тускнеет.
     */
    public static void drawOrbitingHalo(Graphics2D g, BufferedImage halo, int cx, int cy, int itemSize,
                                        float alpha, float flyT, float tuckT, float angleRad) {
        if (halo == null || alpha <= 0.02f) {
            return;
        }
        float distanceFade = (1f - flyT) * (1f - flyT);
        float haloAlpha = alpha * (0.95f - tuckT) * distanceFade;
        if (haloAlpha <= 0.02f) {
            return;
        }

        int haloSize = Math.max(24, Math.round(itemSize * 2.45f));
        Composite prev = g.getComposite();
        AffineTransform prevTx = g.getTransform();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1f, haloAlpha)));
        g.translate(cx, cy);
        g.rotate(angleRad);
        g.drawImage(halo, -haloSize / 2, -haloSize / 2, haloSize, haloSize, null);
        g.setTransform(prevTx);
        g.setComposite(prev);
    }
}
