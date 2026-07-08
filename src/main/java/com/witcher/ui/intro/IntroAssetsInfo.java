package main.java.com.witcher.ui.intro;

/**
 * Размеры спрайтов и метаданные GIF появления лавки — задаёт слой отрисовки.
 */
public final class IntroAssetsInfo {

    public final int strangerW;
    public final int strangerH;
    public final int geraltW;
    public final int geraltH;
    public final int dukeW;
    public final int dukeH;
    public final int shopMaterializeFrameCount;
    public final int[] shopMaterializeDelaysMs;
    public final boolean hasShopMaterializeFrames;
    public final boolean hasMerchantBg;
    public final boolean hasGeraltEmotion;
    public final boolean hasDukeLaugh;
    public final boolean hasGeraltShopSprite;
    public final boolean hasDukeShopSprite;
    public final boolean hasGeraltEmotionShop;
    public final boolean hasDukeLaughShop;

    public IntroAssetsInfo(int strangerW, int strangerH,
                           int geraltW, int geraltH,
                           int dukeW, int dukeH,
                           int shopMaterializeFrameCount,
                           int[] shopMaterializeDelaysMs,
                           boolean hasShopMaterializeFrames,
                           boolean hasMerchantBg,
                           boolean hasGeraltEmotion,
                           boolean hasDukeLaugh,
                           boolean hasGeraltShopSprite,
                           boolean hasDukeShopSprite,
                           boolean hasGeraltEmotionShop,
                           boolean hasDukeLaughShop) {
        this.strangerW = strangerW;
        this.strangerH = strangerH;
        this.geraltW = geraltW;
        this.geraltH = geraltH;
        this.dukeW = dukeW;
        this.dukeH = dukeH;
        this.shopMaterializeFrameCount = shopMaterializeFrameCount;
        this.shopMaterializeDelaysMs = shopMaterializeDelaysMs;
        this.hasShopMaterializeFrames = hasShopMaterializeFrames;
        this.hasMerchantBg = hasMerchantBg;
        this.hasGeraltEmotion = hasGeraltEmotion;
        this.hasDukeLaugh = hasDukeLaugh;
        this.hasGeraltShopSprite = hasGeraltShopSprite;
        this.hasDukeShopSprite = hasDukeShopSprite;
        this.hasGeraltEmotionShop = hasGeraltEmotionShop;
        this.hasDukeLaughShop = hasDukeLaughShop;
    }

    public static IntroAssetsInfo empty() {
        return new IntroAssetsInfo(0, 0, 0, 0, 0, 0, 0, null, false, false,
            false, false, false, false, false, false);
    }
}
