package main.java.com.witcher.gdx.bridge;

import java.awt.image.BufferedImage;

/** GDX-запечённые HUD-ассеты лавки для Swing. */
public final class HybridShopUiAssets {

    private HybridShopUiAssets() {
    }

    public static BufferedImage get(String key) {
        if (!GdxIconBakeSession.isReady()) {
            return null;
        }
        BufferedImage image = GdxIconBakeSession.cache().get(key);
        return GdxIconBaker.isUsable(image) ? image : null;
    }
}
