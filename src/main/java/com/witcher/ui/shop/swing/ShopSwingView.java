package main.java.com.witcher.ui.shop.swing;

import main.java.com.witcher.ui.graphics.DialogBoxRenderer;
import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.Sprite;
import main.java.com.witcher.ui.graphics.UiChrome;
import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.shop.ShopEntryIcons;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.presenter.ShopPresenter;
import main.java.com.witcher.ui.shop.presenter.ShopScreenState;
import main.java.com.witcher.ui.shop.presenter.ShopSessionState;
import main.java.com.witcher.ui.shop.swing.overlay.ShopEquipmentOverlay;
import main.java.com.witcher.ui.shop.swing.overlay.ShopInventoryOverlay;
import main.java.com.witcher.ui.shop.swing.overlay.ShopOverlayContext;
import main.java.com.witcher.ui.shop.view.ShopLayout;
import main.java.com.witcher.ui.shop.view.ShopShowcaseItem;
import main.java.com.witcher.ui.shop.view.ShopView;
import main.java.com.witcher.ui.shop.view.ShopViewConstants;
import main.java.com.witcher.ui.shop.view.anim.ShopCategoryAnimator;
import main.java.com.witcher.ui.shop.view.anim.ShopRevealAnimator;
import main.java.com.witcher.ui.shop.ShopInventoryKind;
import main.java.com.witcher.ui.shop.ShopInventorySlot;
import main.java.com.witcher.ui.chapter1.swing.BattleCardRevealView;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.*;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Swing-реализация {@link ShopView} — вся отрисовка лавки. */
public final class ShopSwingView implements ShopView {

    private static final boolean DEFER_UI_TEXT_TO_OVERLAY = ShopViewConstants.DEFER_UI_TEXT_TO_OVERLAY;
    private static boolean uiTextOverlayOnly;

    private static boolean shouldDrawUiTextInScene() {
        return !DEFER_UI_TEXT_TO_OVERLAY || uiTextOverlayOnly;
    }

    private static BufferedImage loadMenuCursor() {
        Sprite s = Sprite.loadOptional("/assets/sprites/menu/menu_cursor.png");
        return s != null ? s.getImage() : null;
    }

    private static final BufferedImage MENU_CURSOR = loadMenuCursor();

    private final ShopPresenter presenter;
    private final ShopSessionState ui;
    private final ShopAssetCache assets;
    private final ShopEntryIcons armourIcons;
    private final ShopOverlayContext overlayContext;

    public ShopSwingView(ShopPresenter presenter) {
        this.presenter = presenter;
        this.ui = presenter.ui();
        this.assets = ShopAssetCache.get();
        this.armourIcons = presenter.armourIcons();
        this.overlayContext = new ShopOverlayContext(presenter, ui, assets, armourIcons);
    }

    @Override
    public void renderScene(BufferedImage screen, int mouseX, int mouseY) {
        Graphics2D g = screen.createGraphics();
        int sw = screen.getWidth();
        int sh = screen.getHeight();
        applyCrispRendering(g);

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        ShopLayout layout = presenter.createLayout();
        ShopRevealAnimator reveal = presenter.revealAnimator();
        float brighten = reveal.sceneBrighten;

        drawScaledBackground(g, assets.merchantBgScaled, sw, sh, 0.75f * brighten);

        boolean categoryMode = ui.state == ShopScreenState.CATEGORY_OPENING
            || ui.state == ShopScreenState.CATEGORY || ui.state == ShopScreenState.CATEGORY_CLOSING;
        boolean walletScene = ui.state == ShopScreenState.WALLET_REVEAL;
        boolean purchaseScene = ui.state == ShopScreenState.PURCHASE_REVEAL;
        boolean battleCardScene = ui.state == ShopScreenState.BATTLE_CARD_REVEAL;

        if (walletScene) {
            drawWalletRevealScene(g, sw, sh, layout, mouseX, mouseY);
            if (shouldDrawUiTextInScene()) {
                DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", ui.currentDialog,
                    DialogBoxRenderer.DUKE_COLOR, 1f);
                drawCursor(g, mouseX, mouseY);
            }
            g.dispose();
            return;
        }

        if (battleCardScene) {
            drawBattleCardRevealScene(g, sw, sh, layout);
            if (shouldDrawUiTextInScene()) {
                DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", ui.currentDialog,
                    DialogBoxRenderer.DUKE_COLOR, 1f);
                drawCursor(g, mouseX, mouseY);
            }
            g.dispose();
            return;
        }

        if (purchaseScene) {
            drawPurchaseRevealScene(g, sw, sh, layout);
            if (shouldDrawUiTextInScene()) {
                DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", ui.currentDialog,
                    DialogBoxRenderer.DUKE_COLOR, 1f);
                drawCursor(g, mouseX, mouseY);
            }
            g.dispose();
            return;
        }

        if (!categoryMode) {
            drawDarkOverlay(g, sw, sh, layout, brighten * Math.max(0.25f, reveal.panelAlpha * 0.85f));
        } else {
            drawCategoryOverlay(g, sw, sh, layout, brighten);
        }

        if (!categoryMode) {
            float portraitAlpha = brighten;
            BufferedImage dukeDraw = assets.dukeScaled;
            drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, portraitAlpha);
            drawCharacter(g, sw, sh, dukeDraw, false, layout.dialogTop, portraitAlpha);
        }

        if (!categoryMode && !ui.equipmentOpen && !ui.inventoryOpen) {
            drawHud(g, layout, reveal.hudAlpha, reveal.hudSlideY);
        }

        if (categoryMode && ui.selectedIndex >= 0) {
            ShopCategoryAnimator catAnim = presenter.categoryAnimator(layout);
            drawCategoryView(g, layout, reveal, catAnim, mouseX, mouseY);
            drawCornerWallet(g, 1f);
        } else if (!ui.equipmentOpen && !ui.inventoryOpen) {
            drawCards(g, layout, reveal);
        }

        if (reveal.panelAlpha > 0.45f && !categoryMode) {
            drawAshParticles(g);
        }

        if (shouldDrawUiTextInScene() && !ui.equipmentOpen && !ui.inventoryOpen) {
            DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", ui.currentDialog,
                DialogBoxRenderer.DUKE_COLOR, 1f);
        }

        if (!presenter.model().needsWalletReveal() && !ui.equipmentOpen && !ui.inventoryOpen) {
            drawInventoryBag(g, 1f);
        }

        if (ui.equipmentOpen) {
            drawEquipmentOverlay(g, sw, sh);
        } else if (ui.inventoryOpen) {
            drawInventoryOverlay(g, sw, sh);
        }

        if (shouldDrawUiTextInScene()) {
            drawCursor(g, mouseX, mouseY);
        }

        g.dispose();
    }

    /** Чёткий UI-текст поверх пост-обработки (координаты виртуального кадра 480×360). */
    @Override
    public void renderTextOverlay(Graphics2D g, int mouseX, int mouseY) {
        if (!DEFER_UI_TEXT_TO_OVERLAY) {
            return;
        }
        uiTextOverlayOnly = true;
        try {
            int sw = VIRTUAL_W;
            int sh = VIRTUAL_H;
            ShopLayout layout = presenter.createLayout();
            ShopRevealAnimator reveal = presenter.revealAnimator();

            boolean categoryMode = ui.state == ShopScreenState.CATEGORY_OPENING
                || ui.state == ShopScreenState.CATEGORY || ui.state == ShopScreenState.CATEGORY_CLOSING;
            boolean walletScene = ui.state == ShopScreenState.WALLET_REVEAL;
            boolean purchaseScene = ui.state == ShopScreenState.PURCHASE_REVEAL;
            boolean battleCardScene = ui.state == ShopScreenState.BATTLE_CARD_REVEAL;

            if (walletScene || purchaseScene || battleCardScene) {
                DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", ui.currentDialog,
                    DialogBoxRenderer.DUKE_COLOR, 1f);
                drawCursor(g, mouseX, mouseY);
                return;
            }

            if (categoryMode && ui.selectedIndex >= 0) {
                ShopCategoryAnimator catAnim = presenter.categoryAnimator(layout);
                drawCategoryView(g, layout, reveal, catAnim, mouseX, mouseY);
                drawCornerWallet(g, 1f);
            } else if (!ui.equipmentOpen && !ui.inventoryOpen) {
                drawHud(g, layout, reveal.hudAlpha, reveal.hudSlideY);
                drawCards(g, layout, reveal);
            }

            if (!presenter.model().needsWalletReveal() && !ui.equipmentOpen && !ui.inventoryOpen) {
                int bagX = INVENTORY_BAG_MARGIN;
                int bagY = sh - INVENTORY_BAG_MARGIN - INVENTORY_BAG_SIZE;
                drawBagWalletAmount(g, bagX, bagY, INVENTORY_BAG_SIZE, 1f);
            }

            if (ui.equipmentOpen) {
                drawEquipmentOverlay(g, sw, sh);
            } else if (ui.inventoryOpen) {
                drawInventoryOverlay(g, sw, sh);
            }

            if (!ui.equipmentOpen && !ui.inventoryOpen) {
                DialogBoxRenderer.drawCompactFramedSpeakerText(g, sw, sh, "Герцог", ui.currentDialog,
                    DialogBoxRenderer.DUKE_COLOR, 1f);
            }
            drawCursor(g, mouseX, mouseY);
        } finally {
            uiTextOverlayOnly = false;
        }
    }

    private static void drawCursor(Graphics2D g, int mouseX, int mouseY) {
        if (MENU_CURSOR != null) {
            int cw = 28;
            int ch = Math.max(1, cw * MENU_CURSOR.getHeight() / MENU_CURSOR.getWidth());
            Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(MENU_CURSOR, mouseX - 4, mouseY - 4, cw, ch, null);
            if (prevInterp != null) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
            }
        }
    }

    public boolean isExitRequested() {
        return presenter.exitRequested();
    }

    public void clearExitRequest() {
        presenter.clearExitRequest();
    }

    private void drawWalletRevealScene(Graphics2D g, int sw, int sh, ShopLayout layout,
                                       int mouseX, int mouseY) {
        drawScaledBackground(g, assets.merchantBgScaled, sw, sh, 0.35f);

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);

        drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, 1f);
        drawCharacter(g, sw, sh, assets.dukeScaled, false, layout.dialogTop, 1f);

        drawWalletRevealBag(g, layout);
        drawWalletRevealPouch(g, layout);
    }

    private void drawPurchaseRevealScene(Graphics2D g, int sw, int sh, ShopLayout layout) {
        drawScaledBackground(g, assets.merchantBgScaled, sw, sh, 0.35f);

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);

        drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, 1f);
        drawCharacter(g, sw, sh, assets.dukeScaled, false, layout.dialogTop, 1f);

        drawPurchaseRevealBag(g, layout);
        drawPurchaseRevealItem(g, layout);
    }

    private void drawBattleCardRevealScene(Graphics2D g, int sw, int sh, ShopLayout layout) {
        drawScaledBackground(g, assets.merchantBgScaled, sw, sh, 0.35f);

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.78f));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);

        drawCharacter(g, sw, sh, assets.geraltScaled, true, layout.dialogTop, 1f);
        drawCharacter(g, sw, sh, assets.dukeScaled, false, layout.dialogTop, 1f);

        drawBattleCardRevealBag(g, layout);
        drawBattleCardFlying(g, sw, sh, layout);
    }

    private void drawBattleCardRevealBag(Graphics2D g, ShopLayout layout) {
        Point slot = presenter.inventoryBagSlot();
        float openT = bagOpenProgress(
            ui.battleCardRevealTicks,
            WALLET_APPEAR_TICKS,
            WALLET_FLY_TICKS,
            WALLET_FADE_TICKS,
            WALLET_CLOSE_TICKS);
        drawInventoryBagSprite(g, slot.x, slot.y, INVENTORY_BAG_SIZE, openT, false, 1f);
    }

    private void drawBattleCardFlying(Graphics2D g, int sw, int sh, ShopLayout layout) {
        BufferedImage card = main.java.com.witcher.ui.chapter1.swing.Chapter1UiAssets.cardClosed();
        if (card == null) {
            return;
        }

        int appearEnd = WALLET_APPEAR_TICKS;
        int flyEnd = appearEnd + WALLET_FLY_TICKS;
        int fadeEnd = flyEnd + WALLET_FADE_TICKS;
        int ticks = ui.battleCardRevealTicks;

        if (ticks > fadeEnd) {
            return;
        }

        float maxSize = 80f;
        float minSize = 13f;

        int centerX = VIRTUAL_W / 2;
        int centerY = layout.dialogTop / 2 + 6;
        Point bagSlot = presenter.inventoryBagSlot();
        float bagCenterX = bagSlot.x + INVENTORY_BAG_SIZE / 2f;
        float bagCenterY = bagSlot.y + INVENTORY_BAG_SIZE / 2f;

        float px;
        float py;
        float pw;
        float alpha;
        float flyT = 0f;
        float tuckT = 0f;

        if (ticks <= appearEnd) {
            float appearT = smoothstep(ticks / (float) appearEnd);
            pw = maxSize * (0.55f + appearT * 0.45f);
            px = centerX - pw / 2f;
            py = centerY - pw / 2f;
            alpha = 1f;
        } else {
            flyT = Math.min(1f, (ticks - appearEnd) / (float) WALLET_FLY_TICKS);
            float posT = smoothstep(flyT);
            float sizeT = posT * posT * (3f - 2f * posT);
            pw = maxSize + (minSize - maxSize) * sizeT;
            float cx = centerX + (bagCenterX - centerX) * posT;
            float cy = centerY + (bagCenterY - centerY) * posT;
            px = cx - pw / 2f;
            py = cy - pw / 2f;
            alpha = 1f;
            if (ticks > flyEnd) {
                tuckT = smoothstep((ticks - flyEnd) / (float) WALLET_FADE_TICKS);
                pw = minSize;
                px = bagCenterX - pw / 2f;
                py = bagCenterY - pw / 2f;
                alpha = Math.max(0f, 1f - tuckT);
            }
        }

        if (alpha <= 0.02f) {
            return;
        }

        int ipw = Math.round(pw);
        int iph = Math.round(pw * 1.33f);
        int ipx = Math.round(px);
        int ipy = Math.round(py);
        int cx = ipx + ipw / 2;
        int cy = ipy + iph / 2;
        float angle = ticks * 0.07f;

        int iconEnd = WALLET_ICON_IN_TICKS;
        int seedEnd = iconEnd + WALLET_SEED_GLOW_TICKS;
        int haloEnd = seedEnd + WALLET_HALO_IN_TICKS;

        float iconAlpha = alpha;
        if (ticks <= appearEnd) {
            iconAlpha = alpha * smoothstep(ticks / (float) Math.max(1, iconEnd));
        }

        // Эффекты поверх уже видимой иконки: glow → ореол.
        if (ticks > iconEnd) {
            float seedT = ticks <= seedEnd
                ? smoothstep((ticks - iconEnd) / (float) Math.max(1, WALLET_SEED_GLOW_TICKS))
                : 1f;
            float haloT = ticks <= seedEnd ? 0f
                : (ticks <= haloEnd
                    ? smoothstep((ticks - seedEnd) / (float) Math.max(1, WALLET_HALO_IN_TICKS))
                    : 1f);
            float seedAlpha = alpha * seedT * (ticks <= seedEnd ? 1f : (1f - haloT * 0.45f));
            if (seedAlpha > 0.02f) {
                ShopItemHaloDraw.drawSeedGlow(g, cx, cy, ipw, seedAlpha);
            }
            if (haloT > 0.02f) {
                ShopItemHaloDraw.drawOrbitingHalo(
                    g, assets.haloMap, cx, cy, ipw, alpha * haloT, flyT, tuckT, angle);
            }
        }

        if (iconAlpha <= 0.02f) {
            return;
        }

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, iconAlpha));
        g.drawImage(card, ipx, ipy, ipx + ipw, ipy + iph, 0, 0, card.getWidth(), card.getHeight(), null);
        g.setComposite(prev);
    }

    private void drawPurchaseRevealBag(Graphics2D g, ShopLayout layout) {
        Point slot = presenter.inventoryBagSlot();
        float openT = bagOpenProgress(
            ui.purchaseRevealTicks,
            PURCHASE_APPEAR_TICKS,
            PURCHASE_FLY_TICKS,
            PURCHASE_FADE_TICKS,
            PURCHASE_CLOSE_TICKS);
        drawInventoryBagSprite(g, slot.x, slot.y, INVENTORY_BAG_SIZE, openT, false, 1f);
    }

    /** Прогресс открытия сумки: закрыта → открыта на полёте → держится открытой → закрывается. */
    private float bagOpenProgress(int ticks, int appearTicks, int flyTicks, int fadeTicks, int closeTicks) {
        int bagShow = Math.max(0, appearTicks - 4);
        int flyEnd = appearTicks + flyTicks;
        int fadeEnd = flyEnd + fadeTicks;
        int closeEnd = fadeEnd + closeTicks;
        if (ticks < bagShow) {
            return 0f;
        }
        if (ticks < flyEnd) {
            return smoothstep((ticks - bagShow) / (float) Math.max(1, flyEnd - bagShow));
        }
        if (ticks < fadeEnd) {
            return 1f;
        }
        if (ticks < closeEnd) {
            float closeT = (ticks - fadeEnd) / (float) Math.max(1, closeTicks);
            return smoothstep(1f - closeT);
        }
        return 0f;
    }

    private void drawPurchaseRevealItem(Graphics2D g, ShopLayout layout) {
        if (ui.purchaseRevealIcon == null) {
            return;
        }

        int appearEnd = PURCHASE_APPEAR_TICKS;
        int flyEnd = appearEnd + PURCHASE_FLY_TICKS;
        int fadeEnd = flyEnd + PURCHASE_FADE_TICKS;
        int ticks = ui.purchaseRevealTicks;

        if (ticks > fadeEnd) {
            return;
        }

        float maxSize = 76f;
        float minSize = 14f;

        int centerX = VIRTUAL_W / 2;
        int centerY = layout.dialogTop / 2 + 4;
        Point bagSlot = presenter.inventoryBagSlot();
        float bagCenterX = bagSlot.x + INVENTORY_BAG_SIZE / 2f;
        float bagCenterY = bagSlot.y + INVENTORY_BAG_SIZE / 2f;

        float px;
        float py;
        float pw;
        float alpha;
        float flyT = 0f;
        float tuckT = 0f;
        float appearT = smoothstep(ticks / (float) appearEnd);

        if (ticks <= appearEnd) {
            pw = maxSize * (0.55f + appearT * 0.45f);
            px = centerX - pw / 2f;
            py = centerY - pw / 2f;
            alpha = 1f;
        } else {
            flyT = Math.min(1f, (ticks - appearEnd) / (float) PURCHASE_FLY_TICKS);
            float posT = smoothstep(flyT);
            float sizeT = posT * posT * (3f - 2f * posT);
            pw = maxSize + (minSize - maxSize) * sizeT;
            float cx = centerX + (bagCenterX - centerX) * posT;
            float cy = centerY + (bagCenterY - centerY) * posT;
            px = cx - pw / 2f;
            py = cy - pw / 2f;
            alpha = 1f;
            if (ticks > flyEnd) {
                tuckT = smoothstep((ticks - flyEnd) / (float) PURCHASE_FADE_TICKS);
                pw = minSize;
                px = bagCenterX - pw / 2f;
                py = bagCenterY - pw / 2f;
                alpha = Math.max(0f, 1f - tuckT);
            }
        }

        if (alpha <= 0.02f) {
            return;
        }

        int ipw = Math.round(pw);
        int ipx = Math.round(px);
        int ipy = Math.round(py);
        int cx = ipx + ipw / 2;
        int cy = ipy + ipw / 2;

        BufferedImage specialHalo = specialPurchaseHalo(ui.purchaseRevealCategory);
        if (specialHalo != null) {
            float angle = ticks * 0.07f;
            int iconEnd = WALLET_ICON_IN_TICKS;
            int seedEnd = iconEnd + WALLET_SEED_GLOW_TICKS;
            int haloEnd = seedEnd + Math.max(1, appearEnd - seedEnd);

            float iconAlpha = alpha;
            if (ticks <= appearEnd) {
                iconAlpha = alpha * smoothstep(ticks / (float) Math.max(1, iconEnd));
            }

            if (ticks > iconEnd) {
                float seedT = ticks <= seedEnd
                    ? smoothstep((ticks - iconEnd) / (float) Math.max(1, WALLET_SEED_GLOW_TICKS))
                    : 1f;
                float haloT = ticks <= seedEnd ? 0f
                    : (ticks <= haloEnd
                        ? smoothstep((ticks - seedEnd) / (float) Math.max(1, haloEnd - seedEnd))
                        : 1f);
                float seedAlpha = alpha * seedT * (ticks <= seedEnd ? 1f : (1f - haloT * 0.45f));
                if (seedAlpha > 0.02f) {
                    ShopItemHaloDraw.drawSeedGlow(g, cx, cy, ipw, seedAlpha);
                }
                if (haloT > 0.02f) {
                    ShopItemHaloDraw.drawOrbitingHalo(
                        g, specialHalo, cx, cy, ipw, alpha * haloT, flyT, tuckT, angle);
                }
            }

            if (iconAlpha > 0.02f) {
                drawPurchaseIcon(g, ipx, ipy, ipw, iconAlpha);
            }
            return;
        }

        // Обычная броня: сначала только иконка, пауза, потом отдельно набирает цветное свечение.
        int iconIn = PURCHASE_ICON_ONLY_TICKS;
        int glowStart = iconIn + PURCHASE_ICON_HOLD_TICKS;
        float iconAlpha = alpha;
        if (ticks < iconIn) {
            iconAlpha = alpha * smoothstep(ticks / (float) Math.max(1, iconIn));
        }

        // Свечение не трогаем до glowStart — даже на полёте в сумку.
        if (ticks >= glowStart) {
            float glowAppearT;
            if (ticks <= glowStart + PURCHASE_GLOW_IN_TICKS) {
                glowAppearT = smoothstep((ticks - glowStart) / (float) Math.max(1, PURCHASE_GLOW_IN_TICKS));
            } else {
                glowAppearT = 1f;
            }
            drawItemRevealGlow(g, ipx, ipy, ipw, alpha, glowAppearT, flyT, tuckT,
                ui.purchaseRevealCategory);
        }
        if (iconAlpha > 0.02f) {
            drawPurchaseIcon(g, ipx, ipy, ipw, iconAlpha);
        }
    }

    private BufferedImage specialPurchaseHalo(ShopCategory category) {
        if (category == null) {
            return null;
        }
        return switch (category) {
            case POTION -> assets.haloPotion;
            case WEAPON -> assets.haloWeapon;
            default -> null;
        };
    }

    private void drawPurchaseIcon(Graphics2D g, int ipx, int ipy, int ipw, float alpha) {
        Rectangle crop = ShopImageBounds.compute(ui.purchaseRevealIcon);
        Composite comp = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        if (crop.width > 0 && crop.height > 0) {
            g.drawImage(ui.purchaseRevealIcon, ipx, ipy, ipx + ipw, ipy + ipw,
                crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
        } else {
            g.drawImage(ui.purchaseRevealIcon, ipx, ipy, ipw, ipw, null);
        }
        g.setComposite(comp);
    }

    private void drawItemRevealGlow(Graphics2D g, int px, int py, int pw,
                                    float alpha, float appearT, float flyT, float tuckT,
                                    ShopCategory category) {
        // Без базовой яркости: при appearT=0 свечения нет (иначе иконка и glow стартуют вместе).
        float glow = alpha * appearT * 0.95f * (1f - flyT * 0.25f) * (1f - tuckT);
        if (glow <= 0.02f) {
            return;
        }
        ShopCategoryGlow.Tint tint = ShopCategoryGlow.forCategory(category);
        drawSoftItemGlow(g, px + pw / 2, py + pw / 2, pw, glow, 1.7f, 1.1f, 0.22f, 0.4f,
            tint.outer(), tint.inner());
    }

    private void drawWalletRevealBag(Graphics2D g, ShopLayout layout) {
        Point slot = presenter.inventoryBagSlot();
        int bagX = slot.x;
        int bagY = slot.y;
        int bagSize = INVENTORY_BAG_SIZE;

        int appearEnd = WALLET_APPEAR_TICKS;
        int flyEnd = appearEnd + WALLET_FLY_TICKS;
        int closeEnd = flyEnd + WALLET_BAG_CLOSE_TICKS;
        boolean bagVisible = ui.walletRevealTicks >= appearEnd - 4;

        if (!bagVisible) {
            return;
        }

        float openT = bagOpenProgress(
            ui.walletRevealTicks,
            WALLET_APPEAR_TICKS,
            WALLET_FLY_TICKS,
            WALLET_FADE_TICKS,
            WALLET_CLOSE_TICKS);
        float alpha = Math.min(1f, (ui.walletRevealTicks - (appearEnd - 4)) / 8f);
        drawInventoryBagSprite(g, bagX, bagY, bagSize, openT, false, alpha);

        int countStart = closeEnd;
        if (ui.walletRevealTicks >= countStart) {
            drawBagWalletAmount(g, bagX, bagY, bagSize, alpha);
        }
    }

    private void drawInventoryBag(Graphics2D g, float alpha) {
        Point slot = presenter.inventoryBagSlot();
        float openT = 0f;
        if (ui.inventoryOpen && assets.inventoryBagOpenFrames != null
            && assets.inventoryBagOpenFrames.length > 0) {
            openT = 1f;
        }
        drawInventoryBagSprite(g, slot.x, slot.y, INVENTORY_BAG_SIZE, openT, ui.inventoryBagHovered, alpha);
    }

    private void drawInventoryBagSprite(Graphics2D g, int x, int y, int size, float openT,
                                        boolean hovered, float alpha) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        BufferedImage sprite = pickBagSprite(openT, hovered);

        if (sprite != null) {
            drawScaledSprite(g, sprite, x, y, size, size, true);
        } else {
            g.setColor(new Color(72, 48, 28));
            g.fillRoundRect(x + 4, y + 10, size - 8, size - 14, 4, 4);
            g.setColor(new Color(110, 78, 42));
            g.drawRoundRect(x + 4, y + 10, size - 8, size - 14, 4, 4);
            if (openT > 0.2f) {
                g.setColor(new Color(30, 20, 12));
                g.fillOval(x + 8, y + 6, size - 16, 10);
            }
        }

        if (hovered && assets.inventoryBagHover == null && openT < 0.05f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.35f));
            g.setColor(new Color(255, 220, 120));
            g.drawRoundRect(x - 1, y - 1, size + 2, size + 2, 6, 6);
        }

        if (shouldShowPouchOnBag(openT) && assets.walletPouch != null) {
            int pouchSize = Math.max(12, size / 3);
            int pouchX = x + (size - pouchSize) / 2;
            int pouchY = y + size / 2 - pouchSize / 2 - 2;
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            g.drawImage(assets.walletPouch, pouchX, pouchY, pouchSize, pouchSize, null);
        }
        g.setComposite(prev);
    }

    private boolean shouldShowPouchOnBag(float openT) {
        if (presenter.model().needsWalletReveal()) {
            return false;
        }
        if (ui.state == ShopScreenState.PURCHASE_REVEAL || ui.state == ShopScreenState.WALLET_REVEAL
            || ui.state == ShopScreenState.BATTLE_CARD_REVEAL) {
            return false;
        }
        return openT < 0.001f;
    }

    private BufferedImage pickBagSprite(float openT, boolean hovered) {
        BufferedImage[] frames = assets.inventoryBagOpenFrames;
        if (frames != null && frames.length > 0) {
            if (openT > 0.001f) {
                int frame = Math.min(frames.length - 1,
                    Math.max(0, Math.round(openT * (frames.length - 1))));
                if (frames[frame] != null) {
                    return frames[frame];
                }
            } else if (ui.state == ShopScreenState.PURCHASE_REVEAL
                || ui.state == ShopScreenState.WALLET_REVEAL
                || ui.state == ShopScreenState.BATTLE_CARD_REVEAL) {
                return frames[0];
            }
        }
        if (hovered && openT < 0.05f && assets.inventoryBagHover != null) {
            return assets.inventoryBagHover;
        }
        if (assets.inventoryBagClosed != null) {
            return assets.inventoryBagClosed;
        }
        return assets.inventoryBagIcon;
    }

    private void drawBagWalletAmount(Graphics2D g, int bagX, int bagY, int bagSize, float alpha) {
        if (presenter.model().needsWalletReveal()) {
            return;
        }
        if (!shouldDrawUiTextInScene()) {
            return;
        }
        String wallet = presenter.walletHudAmountText();
        drawCrispText(g);
        g.setFont(GameFonts.get().uiBold( 11));
        FontMetrics fm = g.getFontMetrics();
        int textW = fm.stringWidth(wallet);
        int tx = bagX + (bagSize - textW) / 2;
        int ty = bagY + bagSize + 12;
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.setColor(new Color(20, 14, 6, 200));
        g.fillRoundRect(tx - 4, ty - fm.getAscent(), textW + 8, fm.getHeight() + 2, 4, 4);
        g.setColor(new Color(255, 230, 150));
        g.drawString(wallet, tx, ty);
        g.setComposite(prev);
    }

    /** Кошелёк в правом верхнем углу — экран списка товаров. */
    private Rectangle cornerWalletBounds(Graphics2D g) {
        String wallet = presenter.walletHudAmountText();
        String suffix = presenter.model().walletSuffix();
        int crownSize = 16;
        int crownGap = 4;
        int margin = 8;
        int padX = 7;
        int padY = 4;

        drawCrispText(g);
        g.setFont(GameFonts.get().uiBold(12));
        FontMetrics fm = g.getFontMetrics();
        int blockW = fm.stringWidth(wallet) + fm.stringWidth(suffix);
        if (assets.crownIconScaled != null) {
            blockW += crownSize + crownGap;
        }
        int blockH = Math.max(crownSize, fm.getHeight()) + padY * 2;
        int blockX = VIRTUAL_W - margin - blockW - padX * 2;
        int blockY = INVENTORY_BAG_MARGIN;
        return new Rectangle(blockX, blockY, blockW + padX * 2, blockH);
    }

    private void drawCornerWallet(Graphics2D g, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

        String wallet = presenter.walletHudAmountText();
        String suffix = presenter.model().walletSuffix();
        int crownSize = 16;
        int crownGap = 4;
        int padX = 7;

        Rectangle block = cornerWalletBounds(g);
        if (!uiTextOverlayOnly) {
            ShopUiDraw.drawGoldHudChip(g, block, alpha);
        }

        int blockX = block.x;
        int blockY = block.y;
        int blockH = block.height;

        if (!shouldDrawUiTextInScene()) {
            g.setComposite(prev);
            return;
        }

        FontMetrics fm = g.getFontMetrics();
        int textX = blockX + padX;
        if (!uiTextOverlayOnly && assets.crownIconScaled != null) {
            int crownY = blockY + (blockH - crownSize) / 2;
            drawHudPlaqueIcon(g, assets.crownIconScaled, textX, crownY, crownSize, crownSize);
            textX += crownSize + crownGap;
        } else if (assets.crownIconScaled != null) {
            int crownY = blockY + (blockH - crownSize) / 2;
            drawHudPlaqueIcon(g, assets.crownIconScaled, textX, crownY, crownSize, crownSize);
            textX += crownSize + crownGap;
        }
        int walletY = blockY + (blockH + fm.getAscent()) / 2 - 2;
        ShopUiDraw.drawOutlinedText(g, wallet, textX, walletY, new Color(255, 230, 150));
        ShopUiDraw.drawOutlinedText(g, suffix, textX + fm.stringWidth(wallet), walletY, new Color(200, 180, 120));

        g.setComposite(prev);
    }

    private void drawCategoryStatLegend(Graphics2D g, int panelX, int panelY, float alpha) {
        if (ui.selectedIndex < 0 || ui.selectedIndex >= ui.showcaseItems.size()) {
            return;
        }
        ShopShowcaseItem.Kind kind = ui.showcaseItems.get(ui.selectedIndex).kind;
        if (kind != ShopShowcaseItem.Kind.PIECE && kind != ShopShowcaseItem.Kind.SET_CATALOG) {
            return;
        }
        if (alpha <= 0.01f) {
            return;
        }

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        drawCrispText(g);
        g.setFont(GameFonts.get().uiBold(10));
        FontMetrics fm = g.getFontMetrics();
        Rectangle legendBox = ShopStatGlyphs.legendBounds(fm);
        int legendY = presenter.categoryStatLegendY(panelY);
        int legendX = panelX + (assets.detailPanelW - legendBox.width) / 2;
        ShopStatGlyphs.drawLegend(g, legendX, legendY, fm, alpha, assets.statIcons);
        g.setComposite(prev);
    }

    private void drawInventoryOverlay(Graphics2D g, int sw, int sh) {
        List<ShopInventorySlot> slots = presenter.inventorySlots();
        if (ui.inventoryFocusedIndex >= slots.size()) {
            ui.inventoryFocusedIndex = Math.max(0, slots.size() - 1);
        }
        ShopInventoryOverlay.draw(g, overlayContext, slots, ui.inventoryFocusedIndex,
            ui.inventoryHoveredIndex, ui.inventorySlotBounds,
            new ShopInventoryOverlay.SlotCallbacks() {
                @Override
                public void drawIcon(Graphics2D gg, ShopInventorySlot slot, int x, int y, int size,
                                     boolean focused, boolean hovered) {
                    drawInventorySlotIcon(gg, slot, x, y, size, focused, hovered);
                }

                @Override
                public int drawDetail(Graphics2D gg, ShopInventorySlot slot, int x, int y, int maxW, int maxH) {
                    return drawInventorySlotDetail(gg, slot, x, y, maxW, maxH);
                }
            }, sw, sh);
    }

    private void drawInventorySlotIcon(Graphics2D g, ShopInventorySlot slot, int x, int y, int size,
                                       boolean selected, boolean hovered) {
        Composite prev = g.getComposite();
        Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        if (selected || hovered) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f));
            g.setColor(selected ? new Color(120, 90, 40, 230) : new Color(80, 60, 30, 200));
            g.fillRoundRect(x, y, size, size, 6, 6);
        }
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
        int inset = (selected || hovered) ? 3 : 1;
        int ix = x + inset;
        int iy = y + inset;
        int isz = size - inset * 2;
        switch (slot.kind()) {
            case WALLET -> {
                if (assets.walletPouch != null) {
                    g.drawImage(assets.walletPouch, ix, iy, isz, isz, null);
                }
            }
            case BATTLE_CARD -> BattleCardRevealView.drawCardIcon(g, ix, iy, isz, hovered);
            case POTION, WEAPON -> {
                BufferedImage icon = productIcon(slot, isz);
                if (icon != null) {
                    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.drawImage(icon, ix, iy, isz, isz, null);
                    if (interp != null) {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                    }
                }
            }
            case ARMOUR -> {
                BufferedImage icon = armourIcons.iconForArmour(slot.armour(), slot.iconCategory(), isz);
                if (icon != null) {
                    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.drawImage(icon, ix, iy, isz, isz, null);
                    if (interp != null) {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                    }
                }
            }
            case SET -> {
                BufferedImage icon = armourIcons.iconForName(slot.title(), ShopCategory.SETS, isz);
                if (icon != null) {
                    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.drawImage(icon, ix, iy, isz, isz, null);
                    if (interp != null) {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                    }
                }
            }
        }
        if (prevAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
        }
        g.setComposite(prev);
    }

    /** Иконка самого товара (зелье/меч), иначе запасной значок категории. */
    private BufferedImage productIcon(ShopInventorySlot slot, int size) {
        if (slot == null || slot.title() == null) {
            return assets.iconForCategory(slot != null ? slot.iconCategory() : null);
        }
        BufferedImage icon = armourIcons.iconForName(slot.title(), slot.iconCategory(), size);
        if (icon != null) {
            return icon;
        }
        return assets.iconForCategory(slot.iconCategory());
    }

    private int drawInventorySlotDetail(Graphics2D g, ShopInventorySlot slot, int x, int y, int maxW, int maxH) {
        int large = Math.min(INVENTORY_POUCH_LARGE, Math.max(64, maxW - 8));
        int iconY = y;
        int iconX = x + (maxW - large) / 2;
        Shape prevClip = g.getClip();
        g.clipRect(x, y, maxW, Math.max(1, maxH));

        switch (slot.kind()) {
            case WALLET -> {
                if (assets.walletPouch != null) {
                    Rectangle crop = ShopImageBounds.compute(assets.walletPouch);
                    if (crop.width > 0 && crop.height > 0) {
                        g.drawImage(assets.walletPouch, iconX, iconY, iconX + large, iconY + large,
                            crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
                    } else {
                        g.drawImage(assets.walletPouch, iconX, iconY, large, large, null);
                    }
                }
            }
            case BATTLE_CARD -> BattleCardRevealView.drawCardIcon(g, iconX, iconY, large, false);
            case POTION, WEAPON -> {
                BufferedImage icon = productIcon(slot, large);
                if (icon != null) {
                    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.drawImage(icon, iconX, iconY, large, large, null);
                    if (interp != null) {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                    }
                }
            }
            case ARMOUR -> {
                BufferedImage icon = armourIcons.iconForArmour(slot.armour(), slot.iconCategory(), large);
                if (icon != null) {
                    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.drawImage(icon, iconX, iconY, large, large, null);
                    if (interp != null) {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                    }
                }
            }
            case SET -> {
                BufferedImage icon = armourIcons.iconForName(slot.title(), ShopCategory.SETS, large);
                if (icon != null) {
                    Object interp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g.drawImage(icon, iconX, iconY, large, large, null);
                    if (interp != null) {
                        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interp);
                    }
                }
            }
        }

        Color descColor = ShopCategoryGlow.descriptionColor(slot.iconCategory());
        if (slot.kind() == ShopInventoryKind.WALLET || slot.kind() == ShopInventoryKind.BATTLE_CARD) {
            descColor = ShopCategoryGlow.descriptionColor(null);
        }

        g.setFont(GameFonts.get().uiBold(11));
        FontMetrics titleFm = g.getFontMetrics();
        int textY = iconY + large + 14;
        int lineH = titleFm.getHeight();
        g.setColor(descColor);
        for (String line : wrapInventoryText(slot.title(), titleFm, maxW)) {
            if (textY > y + maxH - 4) {
                break;
            }
            g.drawString(line, x, textY);
            textY += lineH;
        }

        g.setFont(GameFonts.get().uiPlain(10));
        FontMetrics bodyFm = g.getFontMetrics();
        int bodyH = bodyFm.getHeight();
        Color bodyColor = new Color(
            Math.min(255, descColor.getRed() + 20),
            Math.min(255, descColor.getGreen() + 15),
            Math.min(255, descColor.getBlue() + 10));
        g.setColor(bodyColor);
        textY += 2;
        for (String detail : slot.detailLines()) {
            for (String line : wrapInventoryText(detail, bodyFm, maxW)) {
                if (textY > y + maxH - 4) {
                    g.setClip(prevClip);
                    return textY;
                }
                g.drawString(line, x, textY);
                textY += bodyH;
            }
        }
        g.setClip(prevClip);
        return textY;
    }

    private static List<String> wrapInventoryText(String text, FontMetrics fm, int maxW) {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isEmpty() || maxW <= 8) {
            return lines;
        }
        String[] words = text.trim().split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String candidate = line.isEmpty() ? word : line + " " + word;
            if (fm.stringWidth(candidate) > maxW && !line.isEmpty()) {
                lines.add(line.toString());
                if (fm.stringWidth(word) > maxW) {
                    lines.addAll(breakLongWord(word, fm, maxW));
                    line = new StringBuilder();
                } else {
                    line = new StringBuilder(word);
                }
            } else if (fm.stringWidth(candidate) > maxW) {
                lines.addAll(breakLongWord(word, fm, maxW));
                line = new StringBuilder();
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (!line.isEmpty()) {
            lines.add(line.toString());
        }
        return lines;
    }

    private static List<String> breakLongWord(String word, FontMetrics fm, int maxW) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < word.length(); i++) {
            cur.append(word.charAt(i));
            if (fm.stringWidth(cur.toString()) > maxW && cur.length() > 1) {
                cur.deleteCharAt(cur.length() - 1);
                parts.add(cur.toString());
                cur = new StringBuilder().append(word.charAt(i));
            }
        }
        if (!cur.isEmpty()) {
            parts.add(cur.toString());
        }
        return parts;
    }

    private void drawEquipmentOverlay(Graphics2D g, int sw, int sh) {
        ShopEquipmentOverlay.draw(g, overlayContext,
            this::drawScaledSprite, sw, sh);
    }

    private static float smoothstep(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    private void drawDarkOverlay(Graphics2D g, int sw, int sh, ShopLayout layout, float alpha) {
        Composite prev = g.getComposite();

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f * alpha));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * alpha));
        int pad = 6;
        g.fillRoundRect(layout.panelX - pad, layout.panelY - pad,
            layout.panelW + pad * 2, layout.panelH + pad * 2, 4, 4);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.35f * alpha));
        GradientPaint sides = new GradientPaint(0, 0, new Color(0, 0, 0, 200),
            layout.panelX - 20, 0, new Color(0, 0, 0, 0));
        g.setPaint(sides);
        g.fillRect(0, 0, layout.panelX - 10, layout.dialogTop);
        GradientPaint right = new GradientPaint(layout.panelX + layout.panelW + 20, 0,
            new Color(0, 0, 0, 0), sw, 0, new Color(0, 0, 0, 200));
        g.setPaint(right);
        g.fillRect(layout.panelX + layout.panelW + 10, 0, sw - layout.panelX - layout.panelW - 10, layout.dialogTop);

        g.setComposite(prev);
    }

    private void drawCategoryOverlay(Graphics2D g, int sw, int sh, ShopLayout layout, float alpha) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f * alpha));
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, sw, sh);
        g.setComposite(prev);
    }

    private void drawHud(Graphics2D g, ShopLayout layout, float alpha, float slideY) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int hudY = layout.hudY + Math.round(slideY);

        if (!uiTextOverlayOnly) {
            if (assets.hudBar != null) {
                g.drawImage(assets.hudBar, layout.hudX, hudY, null);
            } else {
                g.setColor(new Color(10, 8, 4, 220));
                g.fillRect(layout.hudX, hudY, layout.hudW, layout.hudH);
            }
        }

        if (!shouldDrawUiTextInScene()) {
            g.setComposite(prev);
            return;
        }

        if (assets.dukeSealIconScaled != null) {
            drawHudDukeSeals(g, layout, hudY, assets.dukeSealIconScaled);
        }

        String wallet = presenter.walletHudAmountText();
        String suffix = presenter.model().walletSuffix();
        int crownW = ShopViewConstants.HUD_CROWN_W;
        int crownH = ShopViewConstants.HUD_CROWN_H;
        int crownGap = 4;
        drawCrispText(g);
        g.setFont(GameFonts.get().uiBold( 13));
        FontMetrics fm = g.getFontMetrics();
        int blockW = fm.stringWidth(wallet) + fm.stringWidth(suffix);
        if (assets.crownIconScaled != null) {
            blockW += crownW + crownGap;
        }
        int blockX = layout.hudX + (layout.hudW - blockW) / 2;
        int textX = blockX;
        if (assets.crownIconScaled != null) {
            int crownY = (hudY + (layout.hudH - crownH) / 2) & ~1;
            int crownX = blockX & ~1;
            drawHudPlaqueIcon(g, assets.crownIconScaled, crownX, crownY, crownW, crownH);
            textX = blockX + crownW + crownGap;
        }
        int walletY = hudY + (layout.hudH + fm.getAscent()) / 2 - 2;
        ShopUiDraw.drawOutlinedText(g, wallet, textX, walletY, new Color(255, 230, 150));
        ShopUiDraw.drawOutlinedText(g, suffix, textX + fm.stringWidth(wallet), walletY, new Color(200, 180, 120));

        if (assets.dukeSealIconScaled != null) {
            drawHudDukeSealRight(g, layout, hudY, assets.dukeSealIconScaled);
        }
        g.setComposite(prev);
    }

    /** Печать герцога слева. */
    private static void drawHudDukeSeals(Graphics2D g, ShopLayout layout, int hudY, BufferedImage seal) {
        int sealW = ShopViewConstants.HUD_DUKE_SEAL_W;
        int sealH = ShopViewConstants.HUD_DUKE_SEAL_H;
        int margin = ShopViewConstants.HUD_SEAL_MARGIN;
        int sealY = (hudY + (layout.hudH - sealH) / 2) & ~1;
        int sealX = (layout.hudX + margin) & ~1;
        drawHudPlaqueIcon(g, seal, sealX, sealY, sealW, sealH);
    }

    /** Печать справа — перекрывает декоративную корону на арте плашки. */
    private static void drawHudDukeSealRight(Graphics2D g, ShopLayout layout, int hudY, BufferedImage seal) {
        int sealW = ShopViewConstants.HUD_DUKE_SEAL_W;
        int sealH = ShopViewConstants.HUD_DUKE_SEAL_H;
        int margin = ShopViewConstants.HUD_SEAL_MARGIN;
        int sealY = (hudY + (layout.hudH - sealH) / 2) & ~1;
        int sealX = (layout.hudX + layout.hudW - sealW - margin) & ~1;
        drawHudPlaqueIcon(g, seal, sealX, sealY, sealW, sealH);
    }

    private void drawWalletRevealPouch(Graphics2D g, ShopLayout layout) {
        if (assets.walletPouch == null) {
            return;
        }

        int appearEnd = WALLET_APPEAR_TICKS;
        int flyEnd = appearEnd + WALLET_FLY_TICKS;
        int fadeEnd = flyEnd + WALLET_FADE_TICKS;
        int ticks = ui.walletRevealTicks;

        if (ticks > fadeEnd) {
            return;
        }

        float maxSize = 80f;
        float minSize = 13f;

        int centerX = VIRTUAL_W / 2;
        int centerY = layout.dialogTop / 2 + 6;
        Point bagSlot = presenter.inventoryBagSlot();
        float bagCenterX = bagSlot.x + INVENTORY_BAG_SIZE / 2f;
        float bagCenterY = bagSlot.y + INVENTORY_BAG_SIZE / 2f;

        float px;
        float py;
        float pw;
        float alpha;
        float flyT = 0f;
        float tuckT = 0f;

        if (ticks <= appearEnd) {
            float appearT = smoothstep(ticks / (float) appearEnd);
            pw = maxSize * (0.55f + appearT * 0.45f);
            px = centerX - pw / 2f;
            py = centerY - pw / 2f;
            alpha = 1f;
        } else {
            flyT = Math.min(1f, (ticks - appearEnd) / (float) WALLET_FLY_TICKS);
            float posT = smoothstep(flyT);
            float sizeT = posT * posT * (3f - 2f * posT);
            pw = maxSize + (minSize - maxSize) * sizeT;
            float cx = centerX + (bagCenterX - centerX) * posT;
            float cy = centerY + (bagCenterY - centerY) * posT;
            px = cx - pw / 2f;
            py = cy - pw / 2f;
            alpha = 1f;
            if (ticks > flyEnd) {
                tuckT = smoothstep((ticks - flyEnd) / (float) WALLET_FADE_TICKS);
                pw = minSize;
                px = bagCenterX - pw / 2f;
                py = bagCenterY - pw / 2f;
                alpha = Math.max(0f, 1f - tuckT);
            }
        }

        if (alpha <= 0.02f) {
            return;
        }

        int ipw = Math.round(pw);
        int iph = ipw;
        int ipx = Math.round(px);
        int ipy = Math.round(py);
        int cx = ipx + ipw / 2;
        int cy = ipy + iph / 2;
        float angle = ticks * 0.07f;

        int iconEnd = WALLET_ICON_IN_TICKS;
        int seedEnd = iconEnd + WALLET_SEED_GLOW_TICKS;
        int haloEnd = seedEnd + WALLET_HALO_IN_TICKS;

        float iconAlpha = alpha;
        if (ticks <= appearEnd) {
            iconAlpha = alpha * smoothstep(ticks / (float) Math.max(1, iconEnd));
        }

        if (ticks > iconEnd) {
            float seedT = ticks <= seedEnd
                ? smoothstep((ticks - iconEnd) / (float) Math.max(1, WALLET_SEED_GLOW_TICKS))
                : 1f;
            float haloT = ticks <= seedEnd ? 0f
                : (ticks <= haloEnd
                    ? smoothstep((ticks - seedEnd) / (float) Math.max(1, WALLET_HALO_IN_TICKS))
                    : 1f);
            float seedAlpha = alpha * seedT * (ticks <= seedEnd ? 1f : (1f - haloT * 0.45f));
            if (seedAlpha > 0.02f) {
                ShopItemHaloDraw.drawSeedGlow(g, cx, cy, ipw, seedAlpha);
            }
            if (haloT > 0.02f) {
                ShopItemHaloDraw.drawOrbitingHalo(
                    g, assets.haloWallet, cx, cy, ipw, alpha * haloT, flyT, tuckT, angle);
            }
        }

        if (iconAlpha <= 0.02f) {
            return;
        }

        Rectangle crop = ShopImageBounds.compute(assets.walletPouch);
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, iconAlpha));
        if (crop.width > 0 && crop.height > 0) {
            g.drawImage(assets.walletPouch, ipx, ipy, ipx + ipw, ipy + iph,
                crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
        } else {
            g.drawImage(assets.walletPouch, ipx, ipy, ipw, iph, null);
        }
        g.setComposite(prev);
    }

    private void drawPouchGlow(Graphics2D g, int px, int py, int pw, int ph,
                               float alpha, float appearT, float flyT, float tuckT) {
        Composite prev = g.getComposite();
        int cx = px + pw / 2;
        int cy = py + ph / 2;
        float glow = alpha * (0.45f + appearT * 0.55f) * (1f - flyT * 0.25f) * (1f - tuckT);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.22f));
        g.setColor(new Color(255, 200, 60));
        int outer = Math.round(pw * 1.8f);
        g.fillOval(cx - outer / 2, cy - outer / 2, outer, outer);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glow * 0.45f));
        g.setColor(new Color(255, 230, 120));
        int mid = Math.round(pw * 1.15f);
        g.fillOval(cx - mid / 2, cy - mid / 2, mid, mid);

        if (flyT > 0.05f && flyT < 0.95f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha * 0.3f * (1f - flyT)));
            g.setColor(new Color(255, 215, 90));
            g.fillOval(cx - 4, cy - 4, 8, 8);
        }
        g.setComposite(prev);
    }

    private void drawCards(Graphics2D g, ShopLayout layout, ShopRevealAnimator reveal) {
        if (reveal.panelAlpha <= 0.01f) {
            for (int i = 0; i < ui.showcaseItems.size(); i++) {
                ui.showcaseItems.get(i).bounds.setBounds(0, 0, 0, 0);
            }
            return;
        }

        Composite layer = g.getComposite();
        float scale = reveal.panelScale;
        int panelCx = layout.panelX + layout.panelW / 2;
        int panelCy = layout.panelY + layout.panelH / 2 + Math.round(reveal.panelSlideY);

        if (assets.catalogPanelScaled != null && !uiTextOverlayOnly) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, reveal.panelAlpha));
            drawScaledCentered(g, assets.catalogPanelScaled,
                layout.panelX, layout.panelY + Math.round(reveal.panelSlideY),
                layout.panelW, layout.panelH, panelCx, panelCy, scale);
        }

        for (int i = 0; i < ui.showcaseItems.size(); i++) {
            ShopShowcaseItem item = ui.showcaseItems.get(i);
            Point slot = layout.cardSlot(i);
            float cardA = i < reveal.cardAlpha.length ? reveal.cardAlpha[i] : 1f;
            float cardS = i < reveal.cardScale.length ? reveal.cardScale[i] : 1f;
            float cardSlide = i < reveal.cardSlideY.length ? reveal.cardSlideY[i] : 0f;
            if (cardA <= 0.01f) {
                item.bounds.setBounds(0, 0, 0, 0);
                continue;
            }
            int cardW = Math.round(layout.cardW * cardS);
            int cardH = Math.round(layout.cardH * cardS);
            int cardX = slot.x + (layout.cardW - cardW) / 2;
            int cardY = slot.y + (layout.cardH - cardH) / 2 + Math.round(cardSlide);
            item.bounds.setBounds(cardX, cardY, cardW, cardH);
            drawItemCard(g, item, cardX, cardY, cardW, cardH, i,
                false, i == ui.hoveredIndex, cardA);
        }

        g.setComposite(layer);
    }

    private void drawCategoryView(Graphics2D g, ShopLayout layout, ShopRevealAnimator reveal,
                                  ShopCategoryAnimator cat, int mouseX, int mouseY) {
        Composite layer = g.getComposite();
        ShopShowcaseItem item = ui.showcaseItems.get(ui.selectedIndex);

        if (!uiTextOverlayOnly && assets.counterForeground != null && cat.counterAlpha > 0.02f) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cat.counterAlpha));
            int cy = layout.categoryCounterY();
            int ch = layout.categoryCounterH(layout.dialogTop);
            g.drawImage(assets.counterForeground, assets.counterX, cy, assets.counterW, ch, null);
        }

        for (int i = 0; i < ui.showcaseItems.size(); i++) {
            if (i == ui.selectedIndex) {
                continue;
            }
            if (cat.gridCardsAlpha <= 0.02f) {
                ui.showcaseItems.get(i).bounds.setBounds(0, 0, 0, 0);
                continue;
            }
            ShopShowcaseItem other = ui.showcaseItems.get(i);
            Point slot = layout.cardSlot(i);
            float cardA = (i < reveal.cardAlpha.length ? reveal.cardAlpha[i] : 1f) * cat.gridCardsAlpha;
            ui.showcaseItems.get(i).bounds.setBounds(0, 0, 0, 0);
            drawItemCard(g, other, slot.x, slot.y, layout.cardW, layout.cardH,
                i, false, false, cardA);
        }

        item.bounds.setBounds(cat.cardX, cat.cardY, cat.cardW, cat.cardH);
        drawFlippingCategoryCard(g, item, cat.cardX, cat.cardY, cat.cardW, cat.cardH, true);

        if (cat.detailPanelAlpha > 0.02f || uiTextOverlayOnly) {
            Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
            int px = panel.x + Math.round(cat.detailPanelSlideX);
            int py = panel.y;
            if (!uiTextOverlayOnly) {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cat.detailPanelAlpha));
                if (assets.catalogDetailPanel != null) {
                    g.drawImage(assets.catalogDetailPanel, px, py, null);
                }
            } else {
                g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, cat.detailPanelAlpha));
            }
            drawCatalogRows(g, px, py, cat.listInteractive);
            drawCategoryBackButton(g, px, cat.detailPanelAlpha, cat.listInteractive);
            drawCategoryBuyButton(g, px, py, cat.detailPanelAlpha, cat.listInteractive);
            drawCategoryStatLegend(g, px, py, cat.detailPanelAlpha);
        }

        g.setComposite(layer);
    }

    /** Переворот категории → товар при открытии и обратно при закрытии. */
    private void drawFlippingCategoryCard(Graphics2D g, ShopShowcaseItem item, int x, int y, int w, int h,
                                          boolean smoothIconGrowth) {
        float flipT = presenter.categoryAnimProgress();
        float scaleX = Math.abs((float) Math.cos(flipT * Math.PI));
        if (scaleX < 0.04f) {
            scaleX = 0.04f;
        }
        boolean itemFace = flipT >= 0.5f;

        AffineTransform saved = g.getTransform();
        int cx = x + w / 2;
        int cy = y + h / 2;
        AffineTransform flip = new AffineTransform(saved);
        flip.translate(cx, cy);
        flip.scale(scaleX, 1.0);
        flip.translate(-cx, -cy);
        g.setTransform(flip);

        ShopCatalogEntry statEntry = presenter.selectedCatalogEntry();
        String priceForCard = itemFace ? presenter.selectedCatalogPrice() : null;
        BufferedImage cardArt = itemFace ? presenter.itemArtForEntry(statEntry, item) : null;
        String nameOverride = itemFace && statEntry != null ? statEntry.name : null;
        BufferedImage frame = itemFace
            ? (assets.cardBackScaled != null ? assets.cardBackScaled : assets.cardFrontScaled)
            : assets.cardSelectedScaled;

        drawItemCard(g, item, x, y, w, h, ui.selectedIndex, true, false, 1f,
            priceForCard, smoothIconGrowth, cardArt, nameOverride, frame);

        g.setTransform(saved);
    }

    private void drawCategoryBackButton(Graphics2D g, int panelX, float alpha, boolean interactive) {
        if (alpha <= 0.01f) {
            ui.categoryBackBounds.setBounds(0, 0, 0, 0);
            return;
        }
        int size = UiChrome.BTN_SIZE;
        int backX = panelX + 4;
        int backY = INVENTORY_BAG_MARGIN;
        if (interactive) {
            ui.categoryBackBounds.setBounds(backX, backY, size, size);
        } else {
            ui.categoryBackBounds.setBounds(0, 0, 0, 0);
        }
        UiChrome.drawArrowBackButton(g, ui.categoryBackBounds, ui.categoryBackHovered, alpha);
    }

    private void drawCategoryBuyButton(Graphics2D g, int panelX, int panelY, float alpha, boolean interactive) {
        if (alpha <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        int btnW = assets.btnW;
        int btnH = assets.btnH;
        int btnX = panelX + (assets.detailPanelW - btnW) / 2;
        int btnY = presenter.categoryBuyButtonY(panelY);
        boolean enabled = presenter.isBuyButtonEnabled();
        if (interactive) {
            ui.categoryBuyBounds.setBounds(btnX, btnY, btnW, btnH);
        } else {
            ui.categoryBuyBounds.setBounds(0, 0, 0, 0);
        }
        BufferedImage btnImg = enabled ? assets.btnBuyNormal : assets.btnBuyDisabled;
        if (btnImg == null) {
            btnImg = assets.btnBuyDisabled;
        }
        if (btnImg != null && !uiTextOverlayOnly) {
            g.drawImage(btnImg, btnX, btnY, null);
        }
        if (!shouldDrawUiTextInScene()) {
            g.setComposite(prev);
            return;
        }
        drawCrispText(g);
        g.setFont(cardFont(9));
        String label = "Купить";
        FontMetrics fm = g.getFontMetrics();
        int tx = btnX + (btnW - fm.stringWidth(label)) / 2;
        Color labelColor = enabled
            ? (ui.categoryBuyHovered ? new Color(255, 240, 180) : new Color(220, 200, 140))
            : new Color(120, 105, 75);
        ShopUiDraw.drawOutlinedText(g, label, tx, btnY + 19, labelColor);
        g.setComposite(prev);
    }

    private void drawCatalogRows(Graphics2D g, int panelX, int panelY, boolean interactive) {
        int rowGap = 4;
        int rowStep = assets.rowH + rowGap;
        int listTop = presenter.catalogListTop(panelY);
        int listBottom = presenter.catalogListBottom(panelY);
        int rowW = presenter.catalogRowContentW();
        int x = presenter.catalogRowX(panelX);
        int clipX = x;
        int clipW = rowW;
        int clipH = Math.max(0, listBottom - listTop);

        Shape prevClip = g.getClip();
        g.clipRect(clipX, listTop, clipW, clipH);

        drawCardText(g);
        g.setFont(GameFonts.get().uiBold(9));

        for (int i = 0; i < ui.catalogEntries.size(); i++) {
            ShopCatalogEntry row = ui.catalogEntries.get(i);
            int y = listTop + i * rowStep - ui.catalogScrollOffset;
            if (y + assets.rowH < listTop || y > listBottom) {
                row.bounds.setBounds(0, 0, 0, 0);
                continue;
            }

            boolean hovered = interactive && i == ui.hoveredRowIndex;
            boolean selected = i == ui.selectedRowIndex;

            BufferedImage bg = assets.rowNormal;
            if (selected && assets.rowSelected != null) {
                bg = assets.rowSelected;
            } else if (hovered && assets.rowHover != null) {
                bg = assets.rowHover;
            }
            if (bg != null && !uiTextOverlayOnly) {
                g.drawImage(bg, x, y, rowW, assets.rowH, null);
            }

            row.bounds.setBounds(interactive ? x : 0, interactive ? y : 0, rowW, assets.rowH);

            if (!shouldDrawUiTextInScene()) {
                continue;
            }

            g.setFont(GameFonts.get().uiBold(9));
            FontMetrics fm = g.getFontMetrics();
            String price = row.priceLabel();
            int priceW = fm.stringWidth(price);
            if (assets.crownIconSmall != null && !price.equals("···")) {
                priceW += assets.catalogCoinSize + 2;
            }
            int priceX = x + rowW - priceW - 10;

            int[] deltas = presenter.catalogStatDeltas(row);
            g.setFont(GameFonts.get().uiBold(8));
            FontMetrics statsFm = g.getFontMetrics();
            int statsW = ShopStatGlyphs.rowWidth(statsFm, deltas[0], deltas[1], deltas[2]);
            if (statsW > 0) {
                statsW += 4;
            }
            int statsRight = priceX - 2;

            int nameMaxW = Math.max(20, statsRight - statsW - (x + 10));
            g.setFont(GameFonts.get().uiBold(9));
            fm = g.getFontMetrics();
            String label = truncateToWidth(row.name, fm, nameMaxW);
            int textY = y + (assets.rowH + fm.getAscent()) / 2 - 1;
            ShopUiDraw.drawOutlinedText(g, label, x + 10, textY, new Color(235, 215, 155));

            if (statsW > 0) {
                g.setFont(GameFonts.get().uiBold(8));
                statsFm = g.getFontMetrics();
                ShopStatGlyphs.drawRow(g, statsRight, textY, statsFm, deltas[0], deltas[1], deltas[2],
                    assets.statIcons);
            }

            if (assets.crownIconSmall != null && !price.equals("···")) {
                int coin = assets.catalogCoinSize;
                int coinY = (y + (assets.rowH - coin) / 2) & ~1;
                drawCatalogCoin(g, assets.crownIconSmall, priceX, coinY, coin);
                priceX += coin + 2;
            }
            ShopUiDraw.drawOutlinedText(g, price, priceX, textY, new Color(255, 220, 100));
        }

        g.setClip(prevClip);

        if (interactive && presenter.maxCatalogScroll(panelY) > 0) {
            drawCatalogScrollHint(g, clipX + clipW - 5, listTop, clipH - 8, panelY);
        }
    }

    private void drawCatalogScrollHint(Graphics2D g, int x, int trackTop, int trackH, int panelY) {
        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
        g.setColor(new Color(200, 170, 90));
        int thumbH = Math.max(12, trackH / 4);
        int max = presenter.maxCatalogScroll(panelY);
        float t = max > 0 ? ui.catalogScrollOffset / (float) max : 0f;
        int thumbY = trackTop + Math.round((trackH - thumbH) * t);
        g.fillRoundRect(x, thumbY, 3, thumbH, 2, 2);
        g.setComposite(prev);
    }

    private static void drawScaledCentered(Graphics2D g, BufferedImage img,
                                           int x, int y, int w, int h, int cx, int cy, float scale) {
        int sw = Math.round(w * scale);
        int sh = Math.round(h * scale);
        int sx = cx - sw / 2;
        int sy = cy - sh / 2;
        g.drawImage(img, sx, sy, sw, sh, null);
    }

    private void drawItemCard(Graphics2D g, ShopShowcaseItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered, revealAlpha, null);
    }

    private void drawItemCard(Graphics2D g, ShopShowcaseItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered, revealAlpha, priceOverride, false);
    }

    private void drawItemCard(Graphics2D g, ShopShowcaseItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride,
                              boolean smoothIconGrowth) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered,
            revealAlpha, priceOverride, smoothIconGrowth, null);
    }

    private void drawItemCard(Graphics2D g, ShopShowcaseItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride,
                              boolean smoothIconGrowth, BufferedImage cardArtOverride) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered,
            revealAlpha, priceOverride, smoothIconGrowth, cardArtOverride, null);
    }

    private void drawItemCard(Graphics2D g, ShopShowcaseItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride,
                              boolean smoothIconGrowth, BufferedImage cardArtOverride, String nameOverride) {
        drawItemCard(g, item, x, y, w, h, index, selected, hovered,
            revealAlpha, priceOverride, smoothIconGrowth, cardArtOverride, nameOverride, null);
    }

    private void drawItemCard(Graphics2D g, ShopShowcaseItem item, int x, int y, int w, int h, int index,
                              boolean selected, boolean hovered, float revealAlpha, String priceOverride,
                              boolean smoothIconGrowth, BufferedImage cardArtOverride, String nameOverride,
                              BufferedImage frameOverride) {
        Composite savedComposite = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, revealAlpha));

        BufferedImage frame = frameOverride;
        if (frame == null) {
            frame = assets.cardFrontScaled;
            if (selected && assets.cardSelectedScaled != null) {
                frame = assets.cardSelectedScaled;
            } else if (hovered && assets.cardHoverScaled != null) {
                frame = assets.cardHoverScaled;
            }
        }

        Rectangle cardRect;
        if (!uiTextOverlayOnly) {
            if (frame != null) {
                boolean cardBack = frame == assets.cardBackScaled;
                boolean nativeSize = Math.abs(w - frame.getWidth()) <= 1
                    && Math.abs(h - frame.getHeight()) <= 1;
                Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
                if (cardBack && nativeSize) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                } else if (w != frame.getWidth() || h != frame.getHeight()) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                } else {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                        RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                }
                g.drawImage(frame, x, y, w, h, null);
                if (prevInterp != null) {
                    g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
                }
                cardRect = new Rectangle(x, y, w, h);
            } else {
                drawFallbackCard(g, x, y, w, h, false);
                cardRect = new Rectangle(x, y, w, h);
            }
        } else {
            cardRect = new Rectangle(x, y, w, h);
        }
        drawCardFrontContent(g, item, cardRect, w, h, priceOverride, smoothIconGrowth, cardArtOverride, nameOverride);

        g.setComposite(savedComposite);
    }

    private void drawFallbackCard(Graphics2D g, int x, int y, int w, int h, boolean back) {
        g.setColor(back ? new Color(28, 22, 14, 235) : new Color(38, 28, 16, 235));
        g.fillRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
        g.setColor(new Color(180, 130, 45));
        g.drawRoundRect(x + 1, y + 1, w - 2, h - 2, 6, 6);
    }

    private void drawCardFrontContent(Graphics2D g, ShopShowcaseItem item, Rectangle card, int w, int h,
                                    String priceOverride, boolean smoothIconGrowth,
                                    BufferedImage cardArtOverride, String nameOverride) {
        int x = card.x;
        int y = card.y;
        boolean categoryGrid = priceOverride == null;

        drawCardText(g);
        String name = nameOverride != null ? nameOverride : item.displayName();
        Color nameColor = item.kind == ShopShowcaseItem.Kind.SET_CATALOG
            ? new Color(255, 210, 100) : new Color(245, 230, 190);

        int nameMaxW = categoryGrid ? w - 4 : categoryCardTextMaxW(w);
        int textZoneX = categoryGrid ? x : categoryCardTextX(x, w);
        int nameFontSize;
        FontMetrics nameFm;
        List<String> nameLines;
        if (categoryGrid) {
            nameFontSize = w >= 50 ? 11 : Math.max(9, Math.round(11f * w / 54f));
            g.setFont(fitUiFontToWidth(g, name, nameMaxW, nameFontSize, 8));
            nameFm = g.getFontMetrics();
            nameLines = wrapCardNameLines(nameFm, name, nameMaxW, 2);
            if (nameLines.size() == 1 && nameFm.stringWidth(nameLines.get(0)) > nameMaxW) {
                g.setFont(GameFonts.get().uiBold(9));
                nameFm = g.getFontMetrics();
                nameLines = wrapCardNameLines(nameFm, name, nameMaxW, 2);
            }
        } else {
            nameFontSize = h > 220 ? 9 : 8;
            nameLines = fitCardNameLines(g, name, nameMaxW, nameFontSize, 6, 2);
            nameFm = g.getFontMetrics();
        }

        int lineH = nameFm.getHeight();
        boolean showPrice = !categoryGrid && priceOverride != null
            && !(item.kind == ShopShowcaseItem.Kind.SET_CATALOG && "···".equals(priceOverride));
        int priceBlockH = 0;
        if (showPrice) {
            g.setFont(cardFont(Math.max(7, nameFontSize)));
            FontMetrics priceFmPreview = g.getFontMetrics();
            BufferedImage coinPreview = assets.crownIconSmall;
            int coinH = coinPreview != null ? assets.catalogCoinSize : priceFmPreview.getHeight();
            priceBlockH = Math.max(priceFmPreview.getHeight(), coinH) + 2;
        }

        int nameBlockH = nameLines.size() * lineH;
        int textBlockH = nameBlockH + (showPrice ? priceBlockH + PRODUCT_CARD_NAME_PRICE_GAP : 0);

        Rectangle artBounds = null;
        BufferedImage art = cardArtOverride != null
            ? cardArtOverride
            : item.cardArt != null ? item.cardArt : item.icon;
        if (art != null && !uiTextOverlayOnly) {
            int slotX;
            int slotW;
            int slotTop;
            int slotBottom;
            if (categoryGrid) {
                int nameBlockBottom = y + h - 6;
                int labelReserve = nameBlockH + 6;
                slotX = x + 4;
                slotW = w - 8;
                slotTop = y + Math.round(h * 0.12f);
                slotBottom = nameBlockBottom - labelReserve;
            } else {
                int innerBottom = y + h - categoryCardTextBottomInset(h);
                int labelTop = innerBottom - textBlockH;
                slotX = x + PRODUCT_CARD_INSET_X;
                slotW = w - PRODUCT_CARD_INSET_X * 2;
                slotTop = y + PRODUCT_CARD_INSET_TOP;
                slotBottom = labelTop - PRODUCT_CARD_ICON_TEXT_GAP;
            }
            int slotH = Math.max(1, slotBottom - slotTop);
            Rectangle crop = ShopImageBounds.compute(art);
            int maxArt = iconCapForCard(slotW, slotH, smoothIconGrowth);
            artBounds = aspectFitCroppedBounds(crop, slotX, slotTop, slotW, slotH, maxArt);
            if (!categoryGrid) {
                drawItemArtGoldContour(g, artBounds);
            }
            drawCroppedScaledSprite(g, art, crop, artBounds.x, artBounds.y,
                artBounds.width, artBounds.height, false);
        }

        if (categoryGrid) {
            int nameBlockBottom = y + h - 6;
            int baseline = nameBlockBottom;
            if (shouldDrawUiTextInScene()) {
                for (int i = nameLines.size() - 1; i >= 0; i--) {
                    String line = nameLines.get(i);
                    int lineX = x + (w - nameFm.stringWidth(line)) / 2;
                    drawCategoryLabel(g, line, lineX, baseline, nameColor);
                    baseline -= lineH;
                }
            }
        } else {
            int innerBottom = y + h - categoryCardTextBottomInset(h);
            int labelTop = innerBottom - textBlockH;
            if (shouldDrawUiTextInScene()) {
                int baseline = labelTop + nameFm.getAscent();
                for (String line : nameLines) {
                    int lineX = textZoneX + (nameMaxW - nameFm.stringWidth(line)) / 2;
                    drawCategoryLabel(g, line, lineX, baseline, nameColor);
                    baseline += lineH;
                }
                if (showPrice) {
                    drawProductCardPrice(g, textZoneX, nameMaxW, priceOverride,
                        labelTop + nameBlockH + PRODUCT_CARD_NAME_PRICE_GAP);
                }
            }
        }
    }

    private void drawProductCardPrice(Graphics2D g, int textX, int textW, String priceLabel,
                                      int priceTopY) {
        drawCardText(g);
        g.setFont(GameFonts.get().uiBold(10));
        FontMetrics priceFm = g.getFontMetrics();
        BufferedImage coin = assets.crownIconSmall;
        int coinSize = assets.catalogCoinSize;
        int priceW = priceFm.stringWidth(priceLabel);
        if (coin != null) {
            priceW += coinSize + 2;
        }
        int priceRowY = (priceTopY + priceFm.getAscent()) & ~1;
        int priceX = textX + (textW - priceW) / 2;
        if (coin != null) {
            int coinY = (priceRowY - coinSize + 1) & ~1;
            drawCatalogCoin(g, coin, priceX, coinY, coinSize);
            priceX += coinSize + 2;
        }
        drawCategoryLabel(g, priceLabel, priceX, priceRowY, new Color(255, 232, 120));
    }

    /** Мягкое золотое свечение под иконкой — как при покупке, но компактнее. */
    private static void drawItemArtGoldContour(Graphics2D g, Rectangle drawBounds) {
        if (drawBounds == null || drawBounds.width <= 0 || drawBounds.height <= 0) {
            return;
        }
        int cx = drawBounds.x + drawBounds.width / 2;
        int cy = drawBounds.y + drawBounds.height / 2;
        int base = Math.max(drawBounds.width, drawBounds.height);
        drawSoftGoldItemGlow(g, cx, cy, base, 0.88f, 1.28f, 0.90f, 0.16f, 0.30f);
    }

    private static void drawSoftGoldItemGlow(Graphics2D g, int cx, int cy, int baseSize, float strength,
                                             float outerMul, float midMul, float outerAlpha, float midAlpha) {
        drawSoftItemGlow(g, cx, cy, baseSize, strength, outerMul, midMul, outerAlpha, midAlpha,
            new Color(255, 210, 80), new Color(255, 235, 150));
    }

    private static void drawSoftItemGlow(Graphics2D g, int cx, int cy, int baseSize, float strength,
                                         float outerMul, float midMul, float outerAlpha, float midAlpha,
                                         Color outerColor, Color midColor) {
        if (strength <= 0.01f) {
            return;
        }
        Composite prev = g.getComposite();
        Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, strength * outerAlpha));
        g.setColor(outerColor);
        int outer = Math.round(baseSize * outerMul);
        g.fillOval(cx - outer / 2, cy - outer / 2, outer, outer);

        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, strength * midAlpha));
        g.setColor(midColor);
        int mid = Math.round(baseSize * midMul);
        g.fillOval(cx - mid / 2, cy - mid / 2, mid, mid);

        g.setComposite(prev);
        if (prevAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
        }
    }

    /** Иконка растёт вместе с слотом; при анимации — без скачка 32→96. */
    private int iconCapForCard(int slotW, int slotH, boolean smoothGrowth) {
        int base = assets.cardArtSize;
        int slotMax = Math.min(slotW, slotH);
        int target = Math.round(slotMax * 0.72f);
        if (smoothGrowth) {
            return Math.max(base, Math.min(slotMax, target));
        }
        if (slotMax <= base + 2) {
            return base;
        }
        int mult = Math.max(1, target / base);
        int cap = mult * base;
        if (cap > slotMax) {
            cap = Math.max(base, (slotMax / base) * base);
        }
        return cap;
    }

    private static Font cardFont(int size) {
        return GameFonts.get().uiBold(size);
    }

    private static void drawCardText(Graphics2D g) {
        if (uiTextOverlayOnly) {
            GameFonts.applyUiOverlayHints(g);
        } else {
            GameFonts.applyGothicHints(g);
        }
    }

    private static void drawCrispText(Graphics2D g) {
        drawCardText(g);
    }

    private static void drawCategoryGridLabel(Graphics2D g, String name, int x, int y, int w, int h,
                                              Color color, FontMetrics fm) {
        int nameY = y + h - 6;
        int nameX = x + (w - fm.stringWidth(name)) / 2;
        drawCategoryLabel(g, name, nameX, nameY, color);
    }

    private static Font fitUiFontToWidth(Graphics2D g, String text, int maxWidth, int startSize, int minSize) {
        for (int size = startSize; size >= minSize; size--) {
            Font font = GameFonts.get().uiBold(size);
            g.setFont(font);
            if (g.getFontMetrics().stringWidth(text) <= maxWidth) {
                return font;
            }
        }
        Font font = GameFonts.get().uiBold(minSize);
        g.setFont(font);
        return font;
    }

    /** До {@code maxLines} строк на shop_card_back — без ужимания в одну линию. */
    private static List<String> fitCardNameLines(Graphics2D g, String text, int maxW,
                                                 int startSize, int minSize, int maxLines) {
        for (int size = startSize; size >= minSize; size--) {
            g.setFont(GameFonts.get().uiBold(size));
            FontMetrics fm = g.getFontMetrics();
            List<String> lines = wrapCardNameLines(fm, text, maxW, maxLines);
            boolean fits = true;
            for (String line : lines) {
                if (fm.stringWidth(line) > maxW) {
                    fits = false;
                    break;
                }
            }
            if (fits) {
                return lines;
            }
        }
        g.setFont(GameFonts.get().uiBold(minSize));
        return wrapCardNameLines(g.getFontMetrics(), text, maxW, maxLines);
    }

    private static List<String> wrapCardNameLines(FontMetrics fm, String text, int maxW, int maxLines) {
        if (text == null || text.isBlank()) {
            return List.of("");
        }
        if (fm.stringWidth(text) <= maxW) {
            return List.of(text);
        }

        List<String> lines = new ArrayList<>();
        int index = 0;
        while (index < text.length() && lines.size() < maxLines) {
            int end = index + 1;
            while (end <= text.length() && fm.stringWidth(text.substring(index, end)) <= maxW) {
                end++;
            }
            end = Math.max(index + 1, end - 1);

            int breakAt = end;
            int lastSpace = -1;
            for (int i = index; i < breakAt; i++) {
                if (text.charAt(i) == ' ') {
                    lastSpace = i;
                }
            }
            if (lastSpace > index) {
                breakAt = lastSpace;
            }

            if (lines.size() == maxLines - 1 && breakAt < text.length()) {
                lines.add(truncateToWidth(text.substring(index).trim(), fm, maxW));
                return lines;
            }

            String line = text.substring(index, breakAt).trim();
            if (!line.isEmpty()) {
                lines.add(line);
            }
            index = breakAt;
            while (index < text.length() && text.charAt(index) == ' ') {
                index++;
            }
        }

        if (lines.isEmpty()) {
            lines.add(truncateToWidth(text, fm, maxW));
        }
        return lines;
    }

    /** Подпись на сетке категорий — крупнее и строго по пиксельной сетке. */
    private static void drawCategoryLabel(Graphics2D g, String text, int tx, int ty, Color fill) {
        tx = (tx + 1) & ~1;
        ty = (ty + 1) & ~1;
        g.setColor(new Color(8, 4, 2, 240));
        g.drawString(text, tx + 1, ty);
        g.drawString(text, tx - 1, ty);
        g.drawString(text, tx, ty + 1);
        g.setColor(fill);
        g.drawString(text, tx, ty);
    }

    /** Рисует спрайт в слот без искажения пропорций. */
    private Rectangle drawAspectFitSprite(Graphics2D g, BufferedImage img, int x, int y, int w, int h,
                                          boolean pixelArt) {
        if (img == null || w <= 0 || h <= 0) {
            return new Rectangle(x, y, w, h);
        }
        int srcW = img.getWidth();
        int srcH = img.getHeight();
        if (srcW <= 0 || srcH <= 0) {
            return new Rectangle(x, y, w, h);
        }

        float srcAspect = (float) srcW / srcH;
        float dstAspect = (float) w / h;
        int drawW;
        int drawH;
        if (srcAspect > dstAspect) {
            drawW = w;
            drawH = Math.max(1, Math.round(w / srcAspect));
        } else {
            drawH = h;
            drawW = Math.max(1, Math.round(h * srcAspect));
        }
        int drawX = x + (w - drawW) / 2;
        int drawY = y + (h - drawH) / 2;
        drawScaledSprite(g, img, drawX, drawY, drawW, drawH, pixelArt);
        return new Rectangle(drawX, drawY, drawW, drawH);
    }

    private static Rectangle aspectFitCroppedBounds(Rectangle crop, int x, int y, int w, int h,
                                                    int maxPixelSize) {
        if (crop == null || crop.width <= 0 || crop.height <= 0 || w <= 0 || h <= 0) {
            return new Rectangle(x, y, w, h);
        }
        float srcAspect = (float) crop.width / crop.height;
        float dstAspect = (float) w / h;
        int drawW;
        int drawH;
        if (srcAspect > dstAspect) {
            drawW = w;
            drawH = Math.max(1, Math.round(w / srcAspect));
        } else {
            drawH = h;
            drawW = Math.max(1, Math.round(h * srcAspect));
        }
        if (maxPixelSize > 0) {
            int cap = Math.min(maxPixelSize, Math.min(w, h));
            if (drawW > cap || drawH > cap) {
                if (drawW >= drawH) {
                    drawW = cap;
                    drawH = Math.max(1, Math.round(cap / srcAspect));
                } else {
                    drawH = cap;
                    drawW = Math.max(1, Math.round(cap * srcAspect));
                }
            }
        }
        int drawX = x + (w - drawW) / 2;
        int drawY = y + (h - drawH) / 2;
        return new Rectangle(drawX, drawY, drawW, drawH);
    }

    private Rectangle drawAspectFitCroppedSprite(Graphics2D g, BufferedImage img, Rectangle crop,
                                                 int x, int y, int w, int h, boolean pixelArt,
                                                 int maxPixelSize) {
        if (img == null || crop == null || crop.width <= 0 || crop.height <= 0 || w <= 0 || h <= 0) {
            return new Rectangle(x, y, w, h);
        }
        Rectangle bounds = aspectFitCroppedBounds(crop, x, y, w, h, pixelArt ? maxPixelSize : 0);
        drawCroppedScaledSprite(g, img, crop, bounds.x, bounds.y, bounds.width, bounds.height, pixelArt);
        return bounds;
    }

    /** Маленькая монетка у цены — LibGDX bake, aspect-fit. */
    private static void drawCatalogCoin(Graphics2D g, BufferedImage coin, int x, int y, int size) {
        drawHudPlaqueIcon(g, coin, x, y, size, size);
    }

    /** LibGDX-иконка HUD-плашки: aspect-fit в слот, без сплющивания. */
    private static void drawHudPlaqueIcon(Graphics2D g, BufferedImage icon, int x, int y, int w, int h) {
        if (icon == null || w <= 0 || h <= 0) {
            return;
        }
        Rectangle crop = ShopImageBounds.compute(icon);
        if (crop == null || crop.width <= 0 || crop.height <= 0) {
            crop = new Rectangle(0, 0, icon.getWidth(), icon.getHeight());
        }
        Rectangle dst = aspectFitCroppedBounds(crop, x, y, w, h, 0);
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object prevRender = g.getRenderingHint(RenderingHints.KEY_RENDERING);
        Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.drawImage(icon,
            dst.x, dst.y, dst.x + dst.width, dst.y + dst.height,
            crop.x, crop.y, crop.x + crop.width, crop.y + crop.height,
            null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
        if (prevRender != null) {
            g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRender);
        }
        if (prevAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
        }
    }

    /** Отрисовка иконки без размытия — nearest-neighbor, целые координаты. */
    private void drawCrispIcon(Graphics2D g, BufferedImage icon, int x, int y, int size) {
        if (icon == null || size <= 0) return;
        int ix = Math.round(x);
        int iy = Math.round(y);
        int iw = icon.getWidth();
        int ih = icon.getHeight();
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        Object prevRender = g.getRenderingHint(RenderingHints.KEY_RENDERING);
        Object prevAa = g.getRenderingHint(RenderingHints.KEY_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        if (iw == size && ih == size) {
            g.drawImage(icon, ix, iy, null);
        } else if (iw * 2 == size && ih * 2 == size) {
            g.drawImage(icon, ix, iy, ix + size, iy + size, 0, 0, iw, ih, null);
        } else if (iw == size * 2 && ih == size * 2) {
            g.drawImage(icon, ix, iy, ix + size, iy + size, 0, 0, iw, ih, null);
        } else {
            g.drawImage(icon, ix, iy, ix + size, iy + size, 0, 0, iw, ih, null);
        }
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
        if (prevRender != null) {
            g.setRenderingHint(RenderingHints.KEY_RENDERING, prevRender);
        }
        if (prevAa != null) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, prevAa);
        }
    }

    private static String truncateToWidth(String text, FontMetrics fm, int maxW) {
        if (fm.stringWidth(text) <= maxW) return text;
        String ellipsis = "…";
        for (int len = text.length() - 1; len > 0; len--) {
            String cut = text.substring(0, len) + ellipsis;
            if (fm.stringWidth(cut) <= maxW) return cut;
        }
        return ellipsis;
    }

    private static void applyCrispRendering(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    }

    private static void drawEquipText(Graphics2D g, Font font, String text, int x, int y, Color color) {
        drawCardText(g);
        g.setFont(font);
        int tx = (x + 1) & ~1;
        int ty = (y + 1) & ~1;
        GameFonts.drawOutlined(g, text, tx, ty, color);
    }

    private void drawCharacter(Graphics2D g, int sw, int sh, BufferedImage sprite,
                               boolean isLeft, int dialogTop, float alpha) {
        if (sprite == null) return;

        int cw = sprite.getWidth();
        int ch = sprite.getHeight();

        int baseY = dialogTop - ch + Math.round(ch * 0.12f);
        int cx = isLeft ? -Math.round(cw * 0.12f) : sw - cw + Math.round(cw * 0.12f);
        float breathe = (float) Math.sin(ui.tick * 0.04 + (isLeft ? 0 : 2)) * 1.5f;
        int cy = baseY + Math.round(breathe);

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.92f * alpha));
        g.drawImage(sprite, cx, cy, null);
        g.setComposite(prev);
    }

    private void drawScaledSprite(Graphics2D g, BufferedImage img, int x, int y, int w, int h, boolean pixelArt) {
        if (img == null || w <= 0 || h <= 0) return;
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            pixelArt ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                     : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, x, y, w, h, null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
    }

    private void drawCroppedScaledSprite(Graphics2D g, BufferedImage img, Rectangle crop,
                                         int x, int y, int w, int h, boolean pixelArt) {
        if (img == null || crop == null || crop.width <= 0 || crop.height <= 0 || w <= 0 || h <= 0) return;
        Object prevInterp = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
            pixelArt ? RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR
                     : RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(img, x, y, x + w, y + h,
            crop.x, crop.y, crop.x + crop.width, crop.y + crop.height, null);
        if (prevInterp != null) {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prevInterp);
        }
    }

    private void drawScaledBackground(Graphics2D g, BufferedImage img, int sw, int sh, float alpha) {
        if (img == null) return;

        int w = img.getWidth();
        int h = img.getHeight();
        if (w <= 0 || h <= 0) return;

        int x = (sw - w) / 2;
        int y = (sh - h) / 2;

        Composite prev = g.getComposite();
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(img, x, y, null);
        g.setComposite(prev);
    }

    private void drawAshParticles(Graphics2D g) {
        for (float[] p : ui.ashParticles) {
            float life = 1f - p[4] / p[5];
            int a = Math.max(0, Math.min(255, (int) (life * 50)));
            g.setColor(new Color(200, 170, 100, a));
            g.fillRect(Math.round(p[0]), Math.round(p[1]), 1, 1);
        }
    }
}
