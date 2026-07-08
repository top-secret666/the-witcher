package main.java.com.witcher.ui.intro;

/**
 * Анимация появления лавки: reveal-лерп и кадры GIF materialize.
 */
public final class IntroShopAnimation {

    private final IntroAssetsInfo assets;

    private float shopReveal;
    private int shopFrameIndex;
    private long shopLastFrameTime = System.currentTimeMillis();
    private int shopAnimStartedForEntry = -1;

    public IntroShopAnimation(IntroAssetsInfo assets) {
        this.assets = assets;
    }

    public float getShopReveal() {
        return shopReveal;
    }

    public int getShopFrameIndex() {
        return shopFrameIndex;
    }

    public int getShopAnimStartedForEntry() {
        return shopAnimStartedForEntry;
    }

    public void setShopReveal(float shopReveal) {
        this.shopReveal = shopReveal;
    }

    public void resetShopState() {
        shopReveal = 0f;
        shopFrameIndex = 0;
        shopAnimStartedForEntry = -1;
        shopLastFrameTime = System.currentTimeMillis();
    }

    public void tick(int currentEntry) {
        boolean shopSceneReached = currentEntry >= IntroScript.SHOP_ANIMATION_ENTRY_INDEX;
        boolean finalShopScene = currentEntry == IntroScript.SHOP_ANIMATION_ENTRY_INDEX;

        if (currentEntry == IntroScript.SHOP_ANIMATION_ENTRY_INDEX
            && shopAnimStartedForEntry < IntroScript.SHOP_ANIMATION_ENTRY_INDEX) {
            shopFrameIndex = 0;
            shopLastFrameTime = System.currentTimeMillis();
            shopAnimStartedForEntry = IntroScript.SHOP_ANIMATION_ENTRY_INDEX;
        } else if (currentEntry < IntroScript.SHOP_ANIMATION_ENTRY_INDEX) {
            shopAnimStartedForEntry = -1;
        }

        float revealTarget = shopSceneReached ? 1f : 0f;
        if (shopReveal < revealTarget) {
            shopReveal = Math.min(revealTarget, shopReveal + 0.05f);
        } else {
            shopReveal = Math.max(revealTarget, shopReveal - 0.05f);
        }

        if (finalShopScene && assets.hasShopMaterializeFrames && assets.shopMaterializeFrameCount > 0) {
            long now = System.currentTimeMillis();
            int delayMs = frameDelayMs(shopFrameIndex);
            if (now - shopLastFrameTime >= delayMs) {
                shopFrameIndex = Math.min(assets.shopMaterializeFrameCount - 1, shopFrameIndex + 1);
                shopLastFrameTime = now;
            }
        } else if (!shopSceneReached) {
            shopFrameIndex = 0;
            shopLastFrameTime = System.currentTimeMillis();
        }
    }

    public boolean isShopAnimationComplete(int currentEntry) {
        boolean shopSceneReached = currentEntry >= IntroScript.SHOP_ANIMATION_ENTRY_INDEX;
        return shopSceneReached
            && (currentEntry > IntroScript.SHOP_ANIMATION_ENTRY_INDEX
            || (shopReveal >= 0.995f
            && (!assets.hasShopMaterializeFrames
            || assets.shopMaterializeFrameCount == 0
            || shopFrameIndex >= assets.shopMaterializeFrameCount - 1)));
    }

    public boolean isShopMaterializePlaying(int currentEntry) {
        return currentEntry == IntroScript.SHOP_ANIMATION_ENTRY_INDEX
            && shopReveal > 0.03f
            && !isShopAnimationComplete(currentEntry);
    }

    public boolean shouldHideCharactersForShopScene(int currentEntry) {
        boolean finalShopScene = currentEntry == IntroScript.SHOP_ANIMATION_ENTRY_INDEX;
        return finalShopScene && shopReveal > 0.03f && !isShopAnimationComplete(currentEntry);
    }

    private int frameDelayMs(int frameIndex) {
        if (assets.shopMaterializeDelaysMs == null || frameIndex < 0
            || frameIndex >= assets.shopMaterializeDelaysMs.length) {
            return 70;
        }
        int delayMs = assets.shopMaterializeDelaysMs[frameIndex];
        return delayMs < 20 ? 70 : delayMs;
    }
}
