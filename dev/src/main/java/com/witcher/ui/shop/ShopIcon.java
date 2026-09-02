package main.java.com.witcher.ui.shop;

import java.awt.image.BufferedImage;
import java.util.Objects;

/**
 * Opaque handle for shop icons — Swing uses {@link #asBufferedImage()},
 * LibGDX resolves GPU texture via {@code GdxShopIcons}.
 */
public final class ShopIcon {

    private final BufferedImage image;

    private ShopIcon(BufferedImage image) {
        this.image = Objects.requireNonNull(image);
    }

    public static ShopIcon of(BufferedImage image) {
        return image == null ? null : new ShopIcon(image);
    }

    public BufferedImage asBufferedImage() {
        return image;
    }
}
