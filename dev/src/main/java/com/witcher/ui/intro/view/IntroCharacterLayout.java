package main.java.com.witcher.ui.intro.view;

import main.java.com.witcher.ui.intro.IntroEasing;

/**
 * Позиция и размер спрайта персонажа на экране интро.
 */
public final class IntroCharacterLayout {

    public static final class Rect {
        public int x;
        public int y;
        public int width;
        public int height;

        public void set(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public void copyFrom(Rect other) {
            set(other.x, other.y, other.width, other.height);
        }
    }

    private IntroCharacterLayout() {
    }

    public static Rect computeCharacterRect(int sw, int sh, int spriteW, int spriteH, float slide,
                                            boolean isLeft, boolean isActive, float activeAnim,
                                            int tick, boolean liftForShop, boolean raiseAboveOthers) {
        if (spriteW <= 0 || spriteH <= 0) {
            Rect fallback = new Rect();
            fallback.set((int) (sw * 0.55f), (int) (sh * 0.12f), (int) (sw * 0.4f), (int) (sh * 0.75f));
            return fallback;
        }

        float baseCharScale = (sh * 0.85f) / spriteH;
        float scaleBoost = 1.0f + activeAnim * 0.06f;
        float charScale = baseCharScale * scaleBoost;
        if (liftForShop) {
            charScale *= 0.92f;
        }

        int cw = Math.round(spriteW * charScale);
        int ch = Math.round(spriteH * charScale);

        int dialogZone = (int) (sh * 0.15f);
        int baseY = sh - dialogZone - ch + (int) (ch * 0.15f);

        int offscreenX = isLeft ? -cw : sw;
        int targetX = isLeft ? (int) (sw * 0.02f) : (int) (sw - cw - sw * 0.02f);

        int activeShift = (int) (sw * 0.03f * activeAnim);
        if (isLeft) {
            targetX += activeShift;
        } else {
            targetX -= activeShift;
        }

        float easedSlide = IntroEasing.easeOutBack(slide);
        int cx = offscreenX + (int) ((targetX - offscreenX) * easedSlide);

        float breathe = (float) Math.sin(tick * 0.04 + (isLeft ? 0 : 2)) * 2;
        if (isActive) {
            breathe += (float) Math.sin(tick * 0.08) * 0.8f;
        }
        int cy = baseY + (int) breathe;

        if (liftForShop) {
            cy += Math.round(ch * 0.06f);
        }
        if (raiseAboveOthers) {
            cy -= Math.round(ch * 0.08f);
        }

        Rect rect = new Rect();
        rect.set(cx, cy, cw, ch);
        return rect;
    }

    public static Rect estimateRightCharacterBounds(int sw, int sh, int strangerW, int strangerH) {
        if (strangerW <= 0 || strangerH <= 0) {
            Rect fallback = new Rect();
            fallback.set((int) (sw * 0.55f), (int) (sh * 0.12f), (int) (sw * 0.4f), (int) (sh * 0.75f));
            return fallback;
        }
        return computeCharacterRect(sw, sh, strangerW, strangerH, 1f, false, false, 0f, 0, false, false);
    }
}
