package main.java.com.witcher.ui.intro.presenter;

import main.java.com.witcher.ui.intro.IntroAssetsInfo;
import main.java.com.witcher.ui.intro.IntroMorphAnimation;
import main.java.com.witcher.ui.intro.IntroScript;
import main.java.com.witcher.ui.intro.IntroShopAnimation;
import main.java.com.witcher.ui.intro.IntroSwitchAnimation;
import main.java.com.witcher.ui.intro.IntroVnUi;
import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.intro.view.IntroHistoryLayout;
import main.java.com.witcher.ui.intro.view.IntroCharacterLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Логика интро-заставки: диалог, анимации персонажей, лавка, VN-кнопки.
 * Общая для Swing и LibGDX — только отрисовка различается.
 */
public final class IntroController {

    public static final int REF_W = 480;
    public static final int REF_H = 360;
    private static final int TICKS_PER_CHAR = 2;
    private static final int AUTO_DELAY_TICKS = 50;
    private static final int AUTO_TICKS_PER_CHAR = 1;
    private static final float SLIDE_SPEED = 0.04f;
    private static final float ACTIVE_SPEED = 0.06f;

    public static final class IntroRect {
        public float x;
        public float y;
        public float width;
        public float height;

        public void set(float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public boolean contains(float px, float py) {
            return px >= x && px < x + width && py >= y && py < y + height;
        }
    }

    private final IntroAssetsInfo assets;
    private final IntroMorphAnimation morph = new IntroMorphAnimation();
    private final IntroSwitchAnimation switchAnim = new IntroSwitchAnimation();
    private final IntroShopAnimation shop;

    private final List<IntroScript.DialogEntry> entries = IntroScript.entries();

    private boolean finished;
    private int tick;
    private float fadeAlpha;

    private float geraltSlide;
    private float strangerSlide;
    private float dukeSlide;
    private float leftActiveAnim;
    private float rightActiveAnim;

    private boolean geraltVisible;
    private String rightCharacter = "none";
    private String prevRightCharacter = "none";
    private final IntroCharacterLayout.Rect rightCharacterBounds = new IntroCharacterLayout.Rect();

    private int currentEntry;
    private int charIndex;
    private int typeTickCounter;
    private boolean waitingForAdvance;

    private boolean historyOpen;
    private boolean autoMode;
    private int historyScroll;
    private int autoWaitTicks;
    private boolean historyCloseHovered;

    private final IntroRect backButtonBounds = new IntroRect();
    private final IntroRect historyButtonBounds = new IntroRect();
    private final IntroRect autoButtonBounds = new IntroRect();
    private final IntroRect historyCloseBounds = new IntroRect();
    private final IntroRect historyPanelBounds = new IntroRect();

    private boolean leftEmotionSpeaker;
    private boolean rightEmotionSpeaker;

    public IntroController(IntroAssetsInfo assets) {
        this.assets = assets != null ? assets : IntroAssetsInfo.empty();
        this.shop = new IntroShopAnimation(this.assets);
    }

    public void update(boolean advanceKey, int mouseX, int mouseY, boolean mouseClicked, int wheelNotches) {
        tick++;

        if (fadeAlpha < 1f) {
            fadeAlpha = Math.min(1f, fadeAlpha + 0.025f);
        }

        if (currentEntry >= entries.size()) {
            finished = true;
            return;
        }

        applyButtonLayout(REF_W, REF_H);

        if (historyOpen) {
            if (shop.isShopMaterializePlaying(currentEntry)) {
                historyOpen = false;
            } else {
                historyCloseHovered = historyCloseBounds.contains(mouseX, mouseY);
                if (wheelNotches != 0) {
                    historyScroll = Math.max(0, historyScroll + wheelNotches * IntroHistoryLayout.SCROLL_STEP_PX);
                }
                if (mouseClicked) {
                    if (historyCloseBounds.contains(mouseX, mouseY)) {
                        historyOpen = false;
                    } else if (!historyPanelBounds.contains(mouseX, mouseY)) {
                        historyOpen = false;
                    }
                }
                return;
            }
        }

        boolean advance = advanceKey;
        if (mouseClicked && shop.isShopMaterializePlaying(currentEntry)) {
            if (IntroVnUi.isVnButtonRowClick(currentButtonLayout(), mouseX, mouseY)) {
                return;
            }
        }
        if (mouseClicked) {
            if (historyButtonBounds.contains(mouseX, mouseY)) {
                historyOpen = true;
                historyScroll = 0;
                return;
            }
            if (backButtonBounds.contains(mouseX, mouseY)) {
                if (currentEntry > 0) {
                    goToPreviousEntry();
                }
                return;
            }
            if (autoButtonBounds.contains(mouseX, mouseY)) {
                autoMode = !autoMode;
                autoWaitTicks = 0;
                return;
            }
            if (IntroVnUi.isVnButtonRowClick(currentButtonLayout(), mouseX, mouseY)) {
                return;
            }
            advance = true;
        }

        IntroScript.DialogEntry entry = entries.get(currentEntry);

        geraltVisible = "geralt".equals(entry.leftChar());
        String newRight = entry.rightChar();

        leftEmotionSpeaker = "Геральт".equals(entry.speaker());
        rightEmotionSpeaker = "Герцог".equals(entry.speaker());

        if (!newRight.equals(prevRightCharacter) && IntroMorphAnimation.isStrangerToDukeReveal(prevRightCharacter, newRight)) {
            beginStrangerToDukeMorph();
        } else if (!newRight.equals(prevRightCharacter) && !"none".equals(newRight) && !"none".equals(prevRightCharacter)) {
            switchAnim.spawnSwitchEffect(rightCharacterBounds, REF_W, REF_H);
        }
        prevRightCharacter = newRight;
        rightCharacter = newRight;

        if (morph.isActive()) {
            boolean morphFinished = morph.tick(tick);
            if (morphFinished) {
                strangerSlide = 0f;
                dukeSlide = 1f;
            }
        }

        geraltSlide = geraltVisible
            ? Math.min(1f, geraltSlide + SLIDE_SPEED)
            : Math.max(0f, geraltSlide - SLIDE_SPEED);

        boolean strangerWanted = "stranger".equals(rightCharacter);
        boolean dukeWanted = "duke".equals(rightCharacter);
        if (!morph.isActive()) {
            strangerSlide = strangerWanted
                ? Math.min(1f, strangerSlide + SLIDE_SPEED)
                : Math.max(0f, strangerSlide - SLIDE_SPEED * 1.5f);
            dukeSlide = dukeWanted
                ? Math.min(1f, dukeSlide + SLIDE_SPEED)
                : Math.max(0f, dukeSlide - SLIDE_SPEED * 1.5f);
        }

        boolean leftActive = "left".equals(entry.activeSide());
        boolean rightActive = "right".equals(entry.activeSide());
        leftActiveAnim = leftActive
            ? Math.min(1f, leftActiveAnim + ACTIVE_SPEED)
            : Math.max(0f, leftActiveAnim - ACTIVE_SPEED * 0.7f);
        rightActiveAnim = rightActive
            ? Math.min(1f, rightActiveAnim + ACTIVE_SPEED)
            : Math.max(0f, rightActiveAnim - ACTIVE_SPEED * 0.7f);

        switchAnim.tick();

        shop.tick(currentEntry);
        boolean shopSceneReached = currentEntry >= IntroScript.SHOP_ANIMATION_ENTRY_INDEX;
        boolean shopAnimationComplete = shop.isShopAnimationComplete(currentEntry);

        if (shopSceneReached && shop.getShopReveal() > 0.03f) {
            if (shopAnimationComplete) {
                geraltSlide = geraltVisible
                    ? Math.min(1f, geraltSlide + SLIDE_SPEED * 1.2f)
                    : Math.max(0f, geraltSlide - SLIDE_SPEED);
                strangerSlide = Math.max(0f, strangerSlide - SLIDE_SPEED * 1.8f);
                dukeSlide = dukeWanted
                    ? Math.min(1f, dukeSlide + SLIDE_SPEED * 1.2f)
                    : Math.max(0f, dukeSlide - SLIDE_SPEED);
            } else {
                geraltSlide = Math.max(0f, geraltSlide - SLIDE_SPEED * 1.8f);
                strangerSlide = Math.max(0f, strangerSlide - SLIDE_SPEED * 1.8f);
                dukeSlide = Math.max(0f, dukeSlide - SLIDE_SPEED * 1.8f);
            }
        }

        updateRightCharacterBounds(entry);

        int totalChars = entry.text().length();

        if (waitingForAdvance) {
            boolean morphBlocking = morph.isActive();
            if (advance && !morphBlocking) {
                advanceDialogueEntry();
            } else if (autoMode && !morphBlocking) {
                autoWaitTicks++;
                if (autoWaitTicks >= AUTO_DELAY_TICKS) {
                    advanceDialogueEntry();
                }
            }
        } else {
            if (autoMode && !morph.isActive()) {
                typeTickCounter++;
                if (typeTickCounter >= AUTO_TICKS_PER_CHAR) {
                    typeTickCounter = 0;
                    charIndex++;
                    if (charIndex >= totalChars) {
                        charIndex = totalChars;
                        waitingForAdvance = true;
                        autoWaitTicks = 0;
                    }
                }
            } else if (advance && charIndex < totalChars) {
                charIndex = totalChars;
                waitingForAdvance = true;
            } else {
                typeTickCounter++;
                if (typeTickCounter >= TICKS_PER_CHAR) {
                    typeTickCounter = 0;
                    charIndex++;
                    if (charIndex >= totalChars) {
                        charIndex = totalChars;
                        waitingForAdvance = true;
                    }
                }
            }
        }
    }

    public void goToPreviousEntry() {
        if (currentEntry <= 0) {
            return;
        }
        currentEntry--;
        charIndex = 0;
        typeTickCounter = 0;
        waitingForAdvance = false;
        historyOpen = false;
        historyScroll = 0;
        autoWaitTicks = 0;

        if (currentEntry < IntroScript.SHOP_ANIMATION_ENTRY_INDEX) {
            shop.resetShopState();
        }

        IntroScript.DialogEntry entry = entries.get(currentEntry);
        geraltVisible = "geralt".equals(entry.leftChar());
        rightCharacter = entry.rightChar();
        prevRightCharacter = entry.rightChar();
        geraltSlide = geraltVisible ? 1f : 0f;
        strangerSlide = "stranger".equals(entry.rightChar()) ? 1f : 0f;
        dukeSlide = "duke".equals(entry.rightChar()) ? 1f : 0f;

        morph.reset();
        switchAnim.reset();
        leftEmotionSpeaker = false;
        rightEmotionSpeaker = false;
        updateRightCharacterBounds(entry);
    }

    public void advanceDialogueEntry() {
        currentEntry++;
        charIndex = 0;
        typeTickCounter = 0;
        waitingForAdvance = false;
        autoWaitTicks = 0;
    }

    public void beginStrangerToDukeMorph() {
        strangerSlide = 1f;
        dukeSlide = 1f;
        switchAnim.clearFlashAndParticles();

        IntroMorphAnimation.IntroRect anchor = null;
        if (rightCharacterBounds.width > 0 && rightCharacterBounds.height > 0) {
            anchor = new IntroMorphAnimation.IntroRect();
            anchor.copyFrom(rightCharacterBounds);
        }
        morph.begin(anchor, REF_W, REF_H, assets.strangerW, assets.strangerH);
    }

    private void updateRightCharacterBounds(IntroScript.DialogEntry entry) {
        if ("stranger".equals(rightCharacter) && strangerSlide > 0.001f) {
            IntroCharacterLayout.Rect rect = IntroCharacterLayout.computeCharacterRect(
                REF_W, REF_H, assets.strangerW, assets.strangerH,
                strangerSlide, false, "right".equals(entry.activeSide()), rightActiveAnim,
                tick, false, false);
            rightCharacterBounds.copyFrom(rect);
        } else if ("duke".equals(rightCharacter) && dukeSlide > 0.001f) {
            boolean lift = isUsingShopSprites() && rightEmotionSpeaker && assets.hasDukeLaughShop;
            boolean raise = lift;
            IntroCharacterLayout.Rect rect = IntroCharacterLayout.computeCharacterRect(
                REF_W, REF_H, assets.dukeW, assets.dukeH,
                dukeSlide, false, "right".equals(entry.activeSide()), rightActiveAnim,
                tick, lift, raise);
            rightCharacterBounds.copyFrom(rect);
        }
    }

    private void applyButtonLayout(int sw, int sh) {
        IntroVnUi.ButtonLayout layout = IntroVnUi.layoutVnButtons(sw, sh, currentEntry);
        backButtonBounds.set(layout.backButton.x, layout.backButton.y,
            layout.backButton.width, layout.backButton.height);
        historyButtonBounds.set(layout.historyButton.x, layout.historyButton.y,
            layout.historyButton.width, layout.historyButton.height);
        autoButtonBounds.set(layout.autoButton.x, layout.autoButton.y,
            layout.autoButton.width, layout.autoButton.height);
        historyPanelBounds.set(layout.historyPanel.x, layout.historyPanel.y,
            layout.historyPanel.width, layout.historyPanel.height);
        historyCloseBounds.set(layout.historyClose.x, layout.historyClose.y,
            layout.historyClose.width, layout.historyClose.height);
    }

    private IntroVnUi.ButtonLayout currentButtonLayout() {
        IntroVnUi.ButtonLayout layout = new IntroVnUi.ButtonLayout();
        layout.backButton.set(backButtonBounds.x, backButtonBounds.y,
            backButtonBounds.width, backButtonBounds.height);
        layout.historyButton.set(historyButtonBounds.x, historyButtonBounds.y,
            historyButtonBounds.width, historyButtonBounds.height);
        layout.autoButton.set(autoButtonBounds.x, autoButtonBounds.y,
            autoButtonBounds.width, autoButtonBounds.height);
        return layout;
    }

    public List<String> buildHistoryLogLines() {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i <= currentEntry && i < entries.size(); i++) {
            IntroScript.DialogEntry e = entries.get(i);
            String speakerLabel = e.speaker() != null ? e.speaker() : "Повествование";
            lines.add("[" + speakerLabel + "]");
            String text = DialogBoxRenderer.normalizeFlowText(i < currentEntry
                ? e.text()
                : e.text().substring(0, Math.min(charIndex, e.text().length())));
            if (!text.isBlank()) {
                lines.add(text);
            }
            if (i < currentEntry) {
                lines.add("");
            }
        }
        return lines;
    }

    // ─── Getters для отрисовки ───

    public int getTick() {
        return tick;
    }

    public float getFadeAlpha() {
        return fadeAlpha;
    }

    public boolean isFinished() {
        return finished;
    }

    public int getCurrentEntry() {
        return currentEntry;
    }

    public IntroScript.DialogEntry getCurrentDialogEntry() {
        return currentEntry < entries.size() ? entries.get(currentEntry) : null;
    }

    public List<IntroScript.DialogEntry> getEntries() {
        return entries;
    }

    public int getCharIndex() {
        return charIndex;
    }

    public boolean isWaitingForAdvance() {
        return waitingForAdvance;
    }

    public boolean isAutoMode() {
        return autoMode;
    }

    public float getGeraltSlide() {
        return geraltSlide;
    }

    public float getStrangerSlide() {
        return strangerSlide;
    }

    public float getDukeSlide() {
        return dukeSlide;
    }

    public float getLeftActiveAnim() {
        return leftActiveAnim;
    }

    public float getRightActiveAnim() {
        return rightActiveAnim;
    }

    public boolean isGeraltVisible() {
        return geraltVisible;
    }

    public String getRightCharacter() {
        return rightCharacter;
    }

    public String getPrevRightCharacter() {
        return prevRightCharacter;
    }

    public IntroCharacterLayout.Rect getRightCharacterBounds() {
        return rightCharacterBounds;
    }

    public boolean isRightMorphActive() {
        return morph.isActive();
    }

    public float getRightMorphT() {
        return morph.getMorphT();
    }

    public IntroMorphAnimation.IntroRect getMorphAnchorBounds() {
        return morph.getMorphAnchorBounds();
    }

    public List<float[]> getMorphSmoke() {
        return morph.getMorphSmoke();
    }

    public List<float[]> getMorphSparks() {
        return morph.getMorphSparks();
    }

    public float getSwitchFlash() {
        return switchAnim.getSwitchFlash();
    }

    public List<float[]> getSwitchParticles() {
        return switchAnim.getSwitchParticles();
    }

    public List<float[]> getRightSwitchParticles() {
        return switchAnim.getRightSwitchParticles();
    }

    public float getShopReveal() {
        return shop.getShopReveal();
    }

    public int getShopFrameIndex() {
        return shop.getShopFrameIndex();
    }

    public boolean isShopAnimationComplete() {
        return shop.isShopAnimationComplete(currentEntry);
    }

    public boolean isShopMaterializePlaying() {
        return shop.isShopMaterializePlaying(currentEntry);
    }

    public boolean shouldHideCharactersForShopScene() {
        return shop.shouldHideCharactersForShopScene(currentEntry);
    }

    public boolean isUsingShopSprites() {
        return shop.getShopReveal() > 0.03f
            && (assets.hasShopMaterializeFrames || assets.hasMerchantBg);
    }

    public boolean isLeftEmotionSpeaker() {
        return leftEmotionSpeaker;
    }

    public boolean isRightEmotionSpeaker() {
        return rightEmotionSpeaker;
    }

    public boolean isLeftForceOpaque() {
        IntroScript.DialogEntry entry = getCurrentDialogEntry();
        return leftEmotionSpeaker && entry != null && "left".equals(entry.activeSide());
    }

    public boolean isRightForceOpaque() {
        IntroScript.DialogEntry entry = getCurrentDialogEntry();
        return rightEmotionSpeaker && entry != null && "right".equals(entry.activeSide());
    }

    public boolean shouldLiftDukeForShop() {
        return isUsingShopSprites() && isRightEmotionSpeaker() && assets.hasDukeLaughShop;
    }

    public boolean isHistoryOpen() {
        return historyOpen;
    }

    public int getHistoryScroll() {
        return historyScroll;
    }

    public boolean isHistoryCloseHovered() {
        return historyCloseHovered;
    }

    public IntroRect getBackButtonBounds() {
        return backButtonBounds;
    }

    public IntroRect getHistoryButtonBounds() {
        return historyButtonBounds;
    }

    public IntroRect getAutoButtonBounds() {
        return autoButtonBounds;
    }

    public IntroRect getHistoryCloseBounds() {
        return historyCloseBounds;
    }

    public IntroRect getHistoryPanelBounds() {
        return historyPanelBounds;
    }

    public IntroAssetsInfo getAssets() {
        return assets;
    }

    public boolean isBackEnabled() {
        return currentEntry > 0;
    }

    public boolean isFinalShopScene() {
        return currentEntry == IntroScript.SHOP_ANIMATION_ENTRY_INDEX;
    }

    public boolean shouldShowDialogBox() {
        return !historyOpen && !isFinalShopScene() && currentEntry < entries.size() && fadeAlpha > 0.2f;
    }

    public boolean shouldShowVnButtons() {
        return fadeAlpha > 0.2f && !finished && !isShopMaterializePlaying();
    }
}
