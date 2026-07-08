package main.java.com.witcher.ui.intro;

import java.awt.Color;
import java.awt.image.BufferedImage;

/** Сборка {@link IntroAssetsInfo} из загруженных Swing-изображений. */
public final class IntroSwingBridge {

    private IntroSwingBridge() {
    }

    public static IntroAssetsInfo assetsInfo(
        BufferedImage strangerSprite,
        BufferedImage geraltSprite,
        BufferedImage dukeSprite,
        BufferedImage geraltEmotionSprite,
        BufferedImage dukeLaughSprite,
        BufferedImage geraltShopSprite,
        BufferedImage dukeShopSprite,
        BufferedImage geraltEmotionShopSprite,
        BufferedImage dukeLaughShopSprite,
        BufferedImage[] shopMaterializeFrames,
        int[] shopMaterializeDelays,
        BufferedImage merchantBgImg) {
        return new IntroAssetsInfo(
            sizeW(strangerSprite), sizeH(strangerSprite),
            sizeW(geraltSprite), sizeH(geraltSprite),
            sizeW(dukeSprite), sizeH(dukeSprite),
            shopMaterializeFrames != null ? shopMaterializeFrames.length : 0,
            shopMaterializeDelays,
            shopMaterializeFrames != null && shopMaterializeFrames.length > 0,
            merchantBgImg != null,
            geraltEmotionSprite != null,
            dukeLaughSprite != null,
            geraltShopSprite != null,
            dukeShopSprite != null,
            geraltEmotionShopSprite != null,
            dukeLaughShopSprite != null);
    }

    public static Color colorFromRgb(int rgb) {
        return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
    }

    private static int sizeW(BufferedImage img) {
        return img != null ? img.getWidth() : 0;
    }

    private static int sizeH(BufferedImage img) {
        return img != null ? img.getHeight() : 0;
    }
}
