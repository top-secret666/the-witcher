package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import main.java.com.witcher.gdx.WitcherGame;
import main.java.com.witcher.gdx.graphics.GameFonts;
import main.java.com.witcher.gdx.graphics.GameFrameLayout;
import main.java.com.witcher.gdx.graphics.GdxTextureBridge;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.presenter.ShopPresenter;
import main.java.com.witcher.ui.shop.presenter.ShopScreenState;
import main.java.com.witcher.ui.shop.presenter.ShopSessionState;
import main.java.com.witcher.ui.shop.view.ShopLayout;
import main.java.com.witcher.ui.shop.view.ShopShowcaseItem;
import main.java.com.witcher.ui.shop.view.anim.ShopCategoryAnimator;
import main.java.com.witcher.ui.shop.view.anim.ShopRevealAnimator;

import java.awt.Point;
import java.awt.image.BufferedImage;

import static main.java.com.witcher.ui.shop.view.ShopViewConstants.*;

/**
 * LibGDX-отрисовка лавки — порт логики {@link main.java.com.witcher.ui.graphics.ShopSwingView}.
 */
public final class GdxShopView {

    private static final float VW = WitcherGame.VIRTUAL_W;
    private static final float VH = WitcherGame.VIRTUAL_H;
    private static final float DIALOG_BOTTOM_MARGIN = 18f;

    private static final Color DUKE_GOLD = new Color(218f / 255f, 165f / 255f, 32f / 255f, 1f);
    private static final Color SPEECH = new Color(220f / 255f, 190f / 255f, 100f / 255f, 1f);
    private static final Color WALLET = new Color(1f, 230f / 255f, 150f / 255f, 1f);
    private static final Color WALLET_SUFFIX = new Color(200f / 255f, 180f / 255f, 120f / 255f, 1f);
    private static final Color CARD_NAME = new Color(245f / 255f, 230f / 255f, 190f / 255f, 1f);
    private static final Color CARD_NAME_SET = new Color(255f / 255f, 210f / 255f, 100f / 255f, 1f);
    private static final Color CARD_PRICE = new Color(255f / 255f, 230f / 255f, 120f / 255f, 1f);
    private static final Color ROW_TEXT = new Color(235f / 255f, 215f / 255f, 155f / 255f, 1f);
    private static final Color ROW_PRICE = new Color(255f / 255f, 220f / 255f, 100f / 255f, 1f);

    private final WitcherGame game;
    private final ShopPresenter presenter;
    private final GdxShopRuntimeAssets assets;
    private final GameFonts fonts;
    private final ShopSessionState ui;
    private final GlyphLayout glyph = new GlyphLayout();

    public GdxShopView(WitcherGame game, ShopPresenter presenter,
                       GdxShopRuntimeAssets assets, GameFonts fonts) {
        this.game = game;
        this.presenter = presenter;
        this.assets = assets;
        this.fonts = fonts;
        this.ui = presenter.ui();
    }

    public void dispose() {
        GdxTextureBridge.disposeCachedTextures();
    }

    public void render(SpriteBatch batch, ShapeRenderer shapes, GameFrameLayout layout,
                       int mouseX, int mouseY) {
        ShopLayout shopLayout = presenter.createLayout();
        ShopRevealAnimator reveal = presenter.revealAnimator();

        boolean categoryMode = presenter.isCategoryMode();
        boolean walletScene = ui.state == ShopScreenState.WALLET_REVEAL;
        boolean purchaseScene = ui.state == ShopScreenState.PURCHASE_REVEAL;

        batch.begin();

        if (walletScene) {
            drawBackground(batch, 0.35f);
            batch.end();
            drawFullscreenDim(shapes, 0.78f);
            batch.begin();
            drawPortraits(batch, shopLayout, 1f);
            drawWalletRevealScene(batch, shapes, shopLayout);
            batch.end();
            drawDialog(batch, shapes, ui.currentDialog);
            return;
        }

        if (purchaseScene) {
            drawBackground(batch, 0.35f);
            batch.end();
            drawFullscreenDim(shapes, 0.78f);
            batch.begin();
            drawPortraits(batch, shopLayout, 1f);
            drawPurchaseRevealScene(batch, shopLayout);
            batch.end();
            drawDialog(batch, shapes, ui.currentDialog);
            return;
        }

        float brighten = reveal.sceneBrighten;
        drawBackground(batch, 0.75f * brighten);
        batch.end();

        if (!categoryMode) {
            drawDarkOverlay(shapes, shopLayout, brighten * Math.max(0.25f, reveal.panelAlpha * 0.85f));
        } else {
            drawCategoryOverlay(shapes, brighten);
        }

        batch.begin();
        if (!categoryMode) {
            drawPortraits(batch, shopLayout, brighten);
            drawHud(batch, shopLayout, reveal.hudAlpha, reveal.hudSlideY);
        }

        if (categoryMode && ui.selectedIndex >= 0) {
            ShopCategoryAnimator catAnim = presenter.categoryAnimator(shopLayout);
            drawCategoryView(batch, shapes, shopLayout, reveal, catAnim);
            drawCornerWallet(batch, shapes, 1f);
        } else {
            drawCards(batch, shopLayout, reveal);
        }

        if (reveal.panelAlpha > 0.45f && !categoryMode) {
            batch.end();
            drawAshParticles(shapes);
            batch.begin();
        }

        if (!presenter.model().needsWalletReveal()) {
            drawInventoryBag(batch, 1f);
        }

        batch.end();
        drawDialog(batch, shapes, ui.currentDialog);
    }

    private static float bottomFromTop(float top, float h) {
        return VH - top - h;
    }

    private static float textYFromTop(float top) {
        return VH - top;
    }

    private static float smoothstep(float t) {
        t = Math.max(0f, Math.min(1f, t));
        return t * t * (3f - 2f * t);
    }

    private void drawBackground(SpriteBatch batch, float alpha) {
        if (assets.merchantBgScaled != null) {
            PixelTextures.drawCoverBottom(batch, assets.merchantBgScaled, VW, VH, alpha);
        }
    }

    private void drawFullscreenDim(ShapeRenderer shapes, float alpha) {
        PixelTextures.resetBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, alpha);
        shapes.rect(0f, 0f, VW, VH);
        shapes.end();
    }

    private void drawDarkOverlay(ShapeRenderer shapes, ShopLayout layout, float alpha) {
        PixelTextures.resetBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.45f * alpha);
        shapes.rect(0f, 0f, VW, VH);
        shapes.setColor(0f, 0f, 0f, 0.55f * alpha);
        int pad = 6;
        float panelY = bottomFromTop(layout.panelY, layout.panelH);
        shapes.rect(layout.panelX - pad, panelY - pad,
            layout.panelW + pad * 2f, layout.panelH + pad * 2f);
        shapes.setColor(0f, 0f, 0f, 0.35f * alpha);
        float sideH = bottomFromTop(0f, layout.dialogTop);
        shapes.rect(0f, sideH, layout.panelX - 10f, layout.dialogTop);
        shapes.rect(layout.panelX + layout.panelW + 10f, sideH,
            VW - layout.panelX - layout.panelW - 10f, layout.dialogTop);
        shapes.end();
    }

    private void drawCategoryOverlay(ShapeRenderer shapes, float alpha) {
        PixelTextures.resetBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0.5f * alpha);
        shapes.rect(0f, 0f, VW, VH);
        shapes.end();
    }

    private void drawPortraits(SpriteBatch batch, ShopLayout layout, float alpha) {
        Texture duke = assets.dukeScaled;
        if (presenter.isCategoryMode() && assets.dukeLaughScaled != null) {
            duke = assets.dukeLaughScaled;
        }
        drawPortrait(batch, assets.geraltScaled, true, layout.dialogTop, alpha);
        drawPortrait(batch, duke, false, layout.dialogTop, alpha);
    }

    private void drawPortrait(SpriteBatch batch, Texture sprite, boolean left, int dialogTop, float alpha) {
        if (sprite == null) {
            return;
        }
        int targetH = Math.round(VH * 0.82f);
        int scale = Math.max(1, targetH / sprite.getHeight());
        int cw = sprite.getWidth() * scale;
        int ch = sprite.getHeight() * scale;
        float breathe = (float) Math.sin(ui.tick * 0.04 + (left ? 0 : 2)) * 1.5f;
        float top = dialogTop - ch + ch * 0.12f + breathe;
        float x = left ? -cw * 0.12f : VW - cw + cw * 0.12f;
        float y = bottomFromTop(top, ch);
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, 0.92f * alpha);
        batch.draw(sprite, Math.round(x), Math.round(y), cw, ch);
        batch.setColor(1f, 1f, 1f, prev);
    }

    private void drawHud(SpriteBatch batch, ShopLayout layout, float alpha, float slideY) {
        if (alpha <= 0.01f) {
            return;
        }
        int hudY = layout.hudY + Math.round(slideY);
        float hudBottom = bottomFromTop(hudY, layout.hudH);
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);

        if (assets.hudBar != null) {
            batch.draw(assets.hudBar, Math.round(layout.hudX), Math.round(hudBottom),
                assets.hudBar.getWidth(), assets.hudBar.getHeight());
        }

        if (assets.dukeSealIconScaled != null) {
            int seal = assets.dukeSealSize;
            int sealX = layout.hudX + 12;
            float sealY = bottomFromTop(hudY + (layout.hudH - seal) / 2f, seal);
            batch.draw(assets.dukeSealIconScaled, sealX, Math.round(sealY), seal, seal);
        }

        String wallet = presenter.walletHudAmountText();
        String suffix = presenter.model().walletSuffix();
        int crownSize = 18;
        BitmapFont wFont = fonts.ui;
        glyph.setText(wFont, wallet);
        float walletW = glyph.width;
        glyph.setText(wFont, suffix);
        float suffixW = glyph.width;
        float blockW = walletW + suffixW;
        if (assets.crownIconScaled != null) {
            blockW += crownSize + 4f;
        }
        float blockX = layout.hudX + (layout.hudW - blockW) * 0.5f;
        float textX = blockX;
        if (assets.crownIconScaled != null) {
            float crownY = bottomFromTop(hudY + (layout.hudH - crownSize) * 0.5f, crownSize);
            batch.draw(assets.crownIconScaled, blockX, Math.round(crownY), crownSize, crownSize);
            textX = blockX + crownSize + 4f;
        }
        float walletY = textYFromTop(hudY + (layout.hudH + glyph.height) * 0.5f - 2f);
        wFont.setColor(WALLET.r, WALLET.g, WALLET.b, alpha);
        wFont.draw(batch, wallet, textX, walletY);
        wFont.setColor(WALLET_SUFFIX.r, WALLET_SUFFIX.g, WALLET_SUFFIX.b, alpha);
        wFont.draw(batch, suffix, textX + walletW, walletY);
        batch.setColor(1f, 1f, 1f, prev);
    }

    private void drawCards(SpriteBatch batch, ShopLayout layout, ShopRevealAnimator reveal) {
        if (reveal.panelAlpha <= 0.01f) {
            for (ShopShowcaseItem item : ui.showcaseItems) {
                item.bounds.setBounds(0, 0, 0, 0);
            }
            return;
        }

        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, reveal.panelAlpha);
        if (assets.catalogPanelScaled != null) {
            float panelY = bottomFromTop(layout.panelY + Math.round(reveal.panelSlideY), layout.panelH);
            batch.draw(assets.catalogPanelScaled,
                Math.round(layout.panelX), Math.round(panelY),
                assets.catalogPanelScaled.getWidth(), assets.catalogPanelScaled.getHeight());
        }
        batch.setColor(1f, 1f, 1f, prev);

        for (int i = 0; i < ui.showcaseItems.size(); i++) {
            ShopShowcaseItem item = ui.showcaseItems.get(i);
            Point slot = layout.cardSlot(i);
            float cardA = i < reveal.cardAlpha.length ? reveal.cardAlpha[i] : 1f;
            if (cardA <= 0.01f) {
                item.bounds.setBounds(0, 0, 0, 0);
                continue;
            }
            int cardW = assets.cardW;
            int cardH = assets.cardH;
            int cardX = slot.x;
            int cardY = slot.y + Math.round(i < reveal.cardSlideY.length ? reveal.cardSlideY[i] : 0f);
            item.bounds.setBounds(cardX, cardY, cardW, cardH);
            drawItemCard(batch, item, cardX, cardY, cardW, cardH, i == ui.hoveredIndex, false, cardA,
                null, null, null, null);
        }
    }

    private void drawCategoryView(SpriteBatch batch, ShapeRenderer shapes, ShopLayout layout,
                                  ShopRevealAnimator reveal, ShopCategoryAnimator cat) {
        ShopShowcaseItem item = ui.showcaseItems.get(ui.selectedIndex);

        if (assets.counterForeground != null && cat.counterAlpha > 0.02f) {
            float prev = batch.getColor().a;
            batch.setColor(1f, 1f, 1f, cat.counterAlpha);
            int cy = layout.categoryCounterY();
            int ch = layout.categoryCounterH(layout.dialogTop);
            float drawY = bottomFromTop(cy, ch);
            batch.draw(assets.counterForeground, assets.counterX, Math.round(drawY),
                assets.counterW, ch);
            batch.setColor(1f, 1f, 1f, prev);
        }

        for (int i = 0; i < ui.showcaseItems.size(); i++) {
            if (i == ui.selectedIndex || cat.gridCardsAlpha <= 0.02f) {
                if (i != ui.selectedIndex) {
                    ui.showcaseItems.get(i).bounds.setBounds(0, 0, 0, 0);
                }
                continue;
            }
            ShopShowcaseItem other = ui.showcaseItems.get(i);
            Point slot = layout.cardSlot(i);
            float cardA = (i < reveal.cardAlpha.length ? reveal.cardAlpha[i] : 1f) * cat.gridCardsAlpha;
            drawItemCard(batch, other, slot.x, slot.y, layout.cardW, layout.cardH,
                false, false, cardA, null, null, null, null);
        }

        item.bounds.setBounds(cat.cardX, cat.cardY, cat.cardW, cat.cardH);
        drawFlippingCategoryCard(batch, item, cat.cardX, cat.cardY, cat.cardW, cat.cardH);

        if (cat.detailPanelAlpha > 0.02f) {
            java.awt.Rectangle panel = layout.detailListPanelSlot(assets.detailPanelW, assets.detailPanelH);
            int px = panel.x + Math.round(cat.detailPanelSlideX);
            int py = panel.y;
            float drawY = bottomFromTop(py, assets.detailPanelH);
            float prev = batch.getColor().a;
            batch.setColor(1f, 1f, 1f, cat.detailPanelAlpha);
            if (assets.catalogDetailPanel != null) {
                batch.draw(assets.catalogDetailPanel, Math.round(px), Math.round(drawY),
                    assets.catalogDetailPanel.getWidth(), assets.catalogDetailPanel.getHeight());
            }
            batch.setColor(1f, 1f, 1f, prev);
            drawCatalogRows(batch, shapes, px, py, cat.listInteractive);
            drawCategoryBackButton(batch, shapes, px, cat.detailPanelAlpha, cat.listInteractive);
            drawCategoryBuyButton(batch, px, py, cat.detailPanelAlpha, cat.listInteractive);
        }
    }

    private void drawFlippingCategoryCard(SpriteBatch batch, ShopShowcaseItem item,
                                          int x, int y, int w, int h) {
        float flipT = presenter.categoryAnimProgress();
        float scaleX = Math.abs((float) Math.cos(flipT * Math.PI));
        if (scaleX < 0.04f) {
            scaleX = 0.04f;
        }
        boolean itemFace = flipT >= 0.5f;
        ShopCatalogEntry statEntry = presenter.selectedCatalogEntry();
        String priceForCard = itemFace ? presenter.selectedCatalogPrice() : null;
        BufferedImage cardArt = itemFace ? presenter.itemArtForEntry(statEntry, item) : null;
        String nameOverride = itemFace && statEntry != null ? statEntry.name : null;
        Texture frame = itemFace
            ? (assets.cardBackScaled != null ? assets.cardBackScaled : assets.cardFrontScaled)
            : assets.cardSelectedScaled;

        int cx = x + w / 2;
        int drawW = Math.round(w * scaleX);
        int drawX = cx - drawW / 2;
        float drawY = bottomFromTop(y, h);
        drawItemCard(batch, item, drawX, (int) drawY, drawW, h, false, false, 1f,
            priceForCard, cardArt, nameOverride, frame);
    }

    private void drawItemCard(SpriteBatch batch, ShopShowcaseItem item,
                              int x, int topY, int w, int h,
                              boolean hovered, boolean selected, float alpha,
                              String priceOverride, BufferedImage cardArtOverride,
                              String nameOverride, Texture frameOverride) {
        float drawY = bottomFromTop(topY, h);
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);

        Texture frame = frameOverride;
        if (frame == null) {
            frame = assets.cardFrontScaled;
            if (selected && assets.cardSelectedScaled != null) {
                frame = assets.cardSelectedScaled;
            } else if (hovered && assets.cardHoverScaled != null) {
                frame = assets.cardHoverScaled;
            }
        }
        if (frame != null) {
            batch.draw(frame, Math.round(x), Math.round(drawY), w, h);
        }

        BufferedImage art = cardArtOverride != null ? cardArtOverride : item.icon;
        if (art != null) {
            Texture artTex = GdxTextureBridge.toTexture(art);
            int artSize = assets.cardArtSize;
            float artX = x + (w - artSize) * 0.5f;
            float artTop = topY + 8;
            float artDrawY = bottomFromTop(artTop, artSize);
            batch.draw(artTex, Math.round(artX), Math.round(artDrawY), artSize, artSize);
        }

        BitmapFont small = fonts.uiSmall;
        String name = nameOverride != null ? nameOverride : item.displayName();
        Color nameColor = item.kind == ShopShowcaseItem.Kind.SET_CATALOG ? CARD_NAME_SET : CARD_NAME;
        small.setColor(nameColor.r, nameColor.g, nameColor.b, alpha);
        String truncName = truncateToWidth(small, name, w - 8f);
        drawCentered(batch, small, truncName, x + w * 0.5f, bottomFromTop(topY + h - 22, small.getLineHeight()));

        String price = priceOverride != null ? priceOverride : item.priceLabel;
        Texture priceCrown = assets.crownIconSmall != null ? assets.crownIconSmall : assets.crownIconScaled;
        int crownSize = 10;
        glyph.setText(small, price);
        float priceW = glyph.width;
        if (priceCrown != null && !"···".equals(price)) {
            priceW += crownSize + 2f;
        }
        float priceX = x + (w - priceW) * 0.5f;
        if (priceCrown != null && !"···".equals(price)) {
            float crownY = bottomFromTop(topY + h - 14, crownSize);
            batch.draw(priceCrown, Math.round(priceX), Math.round(crownY), crownSize, crownSize);
            priceX += crownSize + 2f;
        }
        small.setColor(CARD_PRICE.r, CARD_PRICE.g, CARD_PRICE.b, alpha);
        small.draw(batch, price, priceX, bottomFromTop(topY + h - 4, small.getCapHeight()));
        batch.setColor(1f, 1f, 1f, prev);
    }

    private void drawCategoryBackButton(SpriteBatch batch, ShapeRenderer shapes,
                                        int panelX, float alpha, boolean interactive) {
        if (alpha <= 0.01f) {
            ui.categoryBackBounds.setBounds(0, 0, 0, 0);
            return;
        }
        int size = 28;
        int backX = panelX + 4;
        int backY = INVENTORY_BAG_MARGIN;
        if (interactive) {
            ui.categoryBackBounds.setBounds(backX, backY, size, size);
        } else {
            ui.categoryBackBounds.setBounds(0, 0, 0, 0);
        }
        batch.end();
        PixelTextures.resetBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.12f, 0.08f, 0.04f, alpha * 0.9f);
        float drawY = bottomFromTop(backY, size);
        shapes.rect(backX, drawY, size, size);
        shapes.setColor(DUKE_GOLD.r, DUKE_GOLD.g, DUKE_GOLD.b, alpha);
        shapes.triangle(backX + size * 0.62f, drawY + size * 0.5f,
            backX + size * 0.32f, drawY + size * 0.28f,
            backX + size * 0.32f, drawY + size * 0.72f);
        shapes.end();
        batch.begin();
    }

    private void drawCategoryBuyButton(SpriteBatch batch, int panelX, int panelY,
                                       float alpha, boolean interactive) {
        if (alpha <= 0.01f) {
            return;
        }
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
        Texture btnImg = enabled ? assets.btnBuyNormal : assets.btnBuyDisabled;
        if (btnImg == null) {
            btnImg = assets.btnBuyDisabled;
        }
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        if (btnImg != null) {
            batch.draw(btnImg, Math.round(btnX), Math.round(bottomFromTop(btnY, btnH)),
                btnW, btnH);
        }
        fonts.ui.setColor(enabled ? 220f / 255f : 120f / 255f,
            enabled ? 200f / 255f : 105f / 255f,
            enabled ? 140f / 255f : 75f / 255f, alpha);
        drawCentered(batch, fonts.ui, "Купить", btnX + btnW * 0.5f,
            textYFromTop(btnY + 19f));
        batch.setColor(1f, 1f, 1f, prev);
    }

    private void drawCatalogRows(SpriteBatch batch, ShapeRenderer shapes, int panelX, int panelY,
                                 boolean interactive) {
        int rowGap = 4;
        int rowStep = assets.rowH + rowGap;
        int listTop = presenter.catalogListTop(panelY);
        int listBottom = presenter.catalogListBottom(panelY);
        int rowW = presenter.catalogRowContentW();
        int x = presenter.catalogRowX(panelX);

        batch.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0f, 0f, 0f, 0f);
        shapes.end();
        batch.begin();

        BitmapFont rowFont = fonts.uiSmall;
        for (int i = 0; i < ui.catalogEntries.size(); i++) {
            ShopCatalogEntry row = ui.catalogEntries.get(i);
            int y = listTop + i * rowStep - ui.catalogScrollOffset;
            if (y + assets.rowH < listTop || y > listBottom) {
                row.bounds.setBounds(0, 0, 0, 0);
                continue;
            }

            boolean hovered = interactive && i == ui.hoveredRowIndex;
            boolean selected = i == ui.selectedRowIndex;
            Texture bg = assets.rowNormal;
            if (selected && assets.rowSelected != null) {
                bg = assets.rowSelected;
            } else if (hovered && assets.rowHover != null) {
                bg = assets.rowHover;
            }
            float drawY = bottomFromTop(y, assets.rowH);
            if (bg != null) {
                batch.draw(bg, Math.round(x), Math.round(drawY), rowW, assets.rowH);
            }
            row.bounds.setBounds(interactive ? x : 0, interactive ? y : 0, rowW, assets.rowH);

            String price = row.priceLabel();
            glyph.setText(rowFont, price);
            float priceW = glyph.width;
            if (assets.crownIconSmall != null && !"···".equals(price)) {
                priceW += assets.crownIconSmall.getWidth() + 2f;
            }
            float priceX = x + rowW - priceW - 6f;

            String label = truncateToWidth(rowFont, row.name, priceX - (x + 8f));
            float textY = textYFromTop(y + (assets.rowH + rowFont.getCapHeight()) * 0.5f - 1f);
            rowFont.setColor(ROW_TEXT);
            rowFont.draw(batch, label, x + 8f, textY);

            if (assets.crownIconSmall != null && !"···".equals(price)) {
                float crownY = bottomFromTop(y + 6, assets.crownIconSmall.getHeight());
                batch.draw(assets.crownIconSmall, Math.round(priceX), Math.round(crownY),
                    assets.crownIconSmall.getWidth(), assets.crownIconSmall.getHeight());
                priceX += assets.crownIconSmall.getWidth() + 2f;
            }
            rowFont.setColor(ROW_PRICE);
            rowFont.draw(batch, price, priceX, textY);
        }

        if (interactive && presenter.maxCatalogScroll(panelY) > 0) {
            batch.end();
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(200f / 255f, 170f / 255f, 90f / 255f, 0.45f);
            int trackTop = listTop;
            int trackH = Math.max(0, listBottom - listTop) - 8;
            int thumbH = Math.max(12, trackH / 4);
            int max = presenter.maxCatalogScroll(panelY);
            float t = max > 0 ? ui.catalogScrollOffset / (float) max : 0f;
            int thumbY = trackTop + Math.round((trackH - thumbH) * t);
            shapes.rect(x + rowW - 5, bottomFromTop(thumbY, thumbH), 3, thumbH);
            shapes.end();
            batch.begin();
        }
    }

    private void drawCornerWallet(SpriteBatch batch, ShapeRenderer shapes, float alpha) {
        if (alpha <= 0.01f) {
            return;
        }
        String wallet = presenter.walletHudAmountText();
        String suffix = presenter.model().walletSuffix();
        int crownSize = 16;
        int crownGap = 4;
        int padX = 7;
        int margin = 8;

        BitmapFont font = fonts.ui;
        glyph.setText(font, wallet);
        float walletW = glyph.width;
        glyph.setText(font, suffix);
        float suffixW = glyph.width;
        float blockW = walletW + suffixW;
        if (assets.crownIconScaled != null) {
            blockW += crownSize + crownGap;
        }
        float blockH = Math.max(crownSize, font.getLineHeight()) + 8;
        float blockX = VW - margin - blockW - padX * 2f;
        float blockTop = INVENTORY_BAG_MARGIN;

        batch.end();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.1f, 0.07f, 0.03f, alpha * 0.85f);
        shapes.rect(blockX, bottomFromTop(blockTop, blockH), blockW + padX * 2f, blockH);
        shapes.end();
        batch.begin();

        float textX = blockX + padX;
        if (assets.crownIconScaled != null) {
            float crownY = bottomFromTop(blockTop + (blockH - crownSize) * 0.5f, crownSize);
            batch.draw(assets.crownIconScaled, textX, Math.round(crownY), crownSize, crownSize);
            textX += crownSize + crownGap;
        }
        float walletY = textYFromTop(blockTop + (blockH + font.getCapHeight()) * 0.5f - 2f);
        font.setColor(WALLET.r, WALLET.g, WALLET.b, alpha);
        font.draw(batch, wallet, textX, walletY);
        font.setColor(WALLET_SUFFIX.r, WALLET_SUFFIX.g, WALLET_SUFFIX.b, alpha);
        font.draw(batch, suffix, textX + walletW, walletY);
    }

    private void drawInventoryBag(SpriteBatch batch, float alpha) {
        Point slot = presenter.inventoryBagSlot();
        float openT = ui.inventoryOpen ? 1f : 0f;
        drawInventoryBagSprite(batch, slot.x, slot.y, INVENTORY_BAG_SIZE, openT, ui.inventoryBagHovered, alpha);
    }

    private void drawInventoryBagSprite(SpriteBatch batch, int x, int topY, int size,
                                        float openT, boolean hovered, float alpha) {
        Texture sprite = pickBagSprite(openT, hovered);
        float drawY = bottomFromTop(topY, size);
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        if (sprite != null) {
            batch.draw(sprite, Math.round(x), Math.round(drawY), size, size);
        }
        if (shouldShowPouchOnBag(openT) && assets.walletPouch != null) {
            int pouchSize = Math.max(12, size / 3);
            int pouchX = x + (size - pouchSize) / 2;
            float pouchTop = topY + size / 2 - pouchSize / 2 - 2;
            batch.draw(assets.walletPouch, pouchX, Math.round(bottomFromTop(pouchTop, pouchSize)),
                pouchSize, pouchSize);
        }
        batch.setColor(1f, 1f, 1f, prev);
    }

    private Texture pickBagSprite(float openT, boolean hovered) {
        Texture[] frames = assets.inventoryBagOpenFrames;
        if (frames != null && frames.length > 0) {
            if (openT > 0.001f) {
                int frame = Math.min(frames.length - 1,
                    Math.max(0, Math.round(openT * (frames.length - 1))));
                if (frames[frame] != null) {
                    return frames[frame];
                }
            } else if (ui.state == ShopScreenState.PURCHASE_REVEAL
                || ui.state == ShopScreenState.WALLET_REVEAL) {
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

    private boolean shouldShowPouchOnBag(float openT) {
        if (presenter.model().needsWalletReveal()) {
            return false;
        }
        if (ui.state == ShopScreenState.PURCHASE_REVEAL || ui.state == ShopScreenState.WALLET_REVEAL) {
            return false;
        }
        return openT < 0.001f;
    }

    private void drawWalletRevealScene(SpriteBatch batch, ShapeRenderer shapes, ShopLayout layout) {
        drawWalletRevealBag(batch, layout);
        drawWalletRevealPouch(batch, shapes, layout);
    }

    private void drawWalletRevealBag(SpriteBatch batch, ShopLayout layout) {
        Point slot = presenter.inventoryBagSlot();
        int appearEnd = WALLET_APPEAR_TICKS;
        if (ui.walletRevealTicks < appearEnd - 4) {
            return;
        }
        float openT = bagOpenProgress(ui.walletRevealTicks, WALLET_APPEAR_TICKS, WALLET_FLY_TICKS,
            WALLET_FADE_TICKS, WALLET_CLOSE_TICKS);
        float alpha = Math.min(1f, (ui.walletRevealTicks - (appearEnd - 4)) / 8f);
        drawInventoryBagSprite(batch, slot.x, slot.y, INVENTORY_BAG_SIZE, openT, false, alpha);
    }

    private void drawWalletRevealPouch(SpriteBatch batch, ShapeRenderer shapes, ShopLayout layout) {
        if (assets.walletPouch == null || ui.walletRevealTicks > WALLET_APPEAR_TICKS + WALLET_FLY_TICKS + WALLET_FADE_TICKS) {
            return;
        }
        float appearT = smoothstep(ui.walletRevealTicks / (float) WALLET_APPEAR_TICKS);
        float maxSize = 80f;
        float minSize = 13f;
        int centerX = (int) (VW / 2f);
        int centerY = layout.dialogTop / 2 + 6;
        Point bagSlot = presenter.inventoryBagSlot();
        float bagCenterX = bagSlot.x + INVENTORY_BAG_SIZE / 2f;
        float bagCenterY = bagSlot.y + INVENTORY_BAG_SIZE / 2f;

        float px;
        float py;
        float pw;
        float alpha;

        if (ui.walletRevealTicks <= WALLET_APPEAR_TICKS) {
            pw = maxSize * (0.32f + appearT * 0.68f);
            px = centerX - pw / 2f;
            py = centerY - pw / 2f;
            alpha = Math.min(1f, appearT * 1.15f);
        } else {
            float posT = smoothstep((ui.walletRevealTicks - WALLET_APPEAR_TICKS) / (float) WALLET_FLY_TICKS);
            float sizeT = smoothstep(posT);
            pw = maxSize + (minSize - maxSize) * sizeT;
            float cx = centerX + (bagCenterX - centerX) * posT;
            float cy = centerY + (bagCenterY - centerY) * posT;
            px = cx - pw / 2f;
            py = cy - pw / 2f;
            alpha = 1f;
            int flyEnd = WALLET_APPEAR_TICKS + WALLET_FLY_TICKS;
            if (ui.walletRevealTicks > flyEnd) {
                float fadeT = smoothstep((ui.walletRevealTicks - flyEnd) / (float) WALLET_FADE_TICKS);
                pw = minSize;
                px = bagCenterX - pw / 2f;
                py = bagCenterY - pw / 2f;
                alpha = Math.max(0f, 1f - fadeT);
            }
        }

        if (alpha <= 0.02f) {
            return;
        }
        int ipw = Math.round(pw);
        float drawY = bottomFromTop(py, ipw);
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(assets.walletPouch, Math.round(px), Math.round(drawY), ipw, ipw);
        batch.setColor(1f, 1f, 1f, prev);
    }

    private void drawPurchaseRevealScene(SpriteBatch batch, ShopLayout layout) {
        Point slot = presenter.inventoryBagSlot();
        float openT = bagOpenProgress(ui.purchaseRevealTicks, PURCHASE_APPEAR_TICKS, PURCHASE_FLY_TICKS,
            PURCHASE_FADE_TICKS, PURCHASE_CLOSE_TICKS);
        drawInventoryBagSprite(batch, slot.x, slot.y, INVENTORY_BAG_SIZE, openT, false, 1f);
        drawPurchaseRevealItem(batch, layout);
    }

    private void drawPurchaseRevealItem(SpriteBatch batch, ShopLayout layout) {
        if (ui.purchaseRevealIcon == null) {
            return;
        }
        int appearEnd = PURCHASE_APPEAR_TICKS;
        int flyEnd = appearEnd + PURCHASE_FLY_TICKS;
        int fadeEnd = flyEnd + PURCHASE_FADE_TICKS;
        if (ui.purchaseRevealTicks > fadeEnd) {
            return;
        }

        float appearT = smoothstep(ui.purchaseRevealTicks / (float) appearEnd);
        float maxSize = 76f;
        float minSize = 14f;
        int centerX = (int) (VW / 2f);
        int centerY = layout.dialogTop / 2 + 4;
        Point bagSlot = presenter.inventoryBagSlot();
        float bagCenterX = bagSlot.x + INVENTORY_BAG_SIZE / 2f;
        float bagCenterY = bagSlot.y + INVENTORY_BAG_SIZE / 2f;

        float px;
        float py;
        float pw;
        float alpha;

        if (ui.purchaseRevealTicks <= appearEnd) {
            pw = maxSize * (0.28f + appearT * 0.72f);
            px = centerX - pw / 2f;
            py = centerY - pw / 2f;
            alpha = Math.min(1f, appearT * 1.1f);
        } else {
            float posT = smoothstep((ui.purchaseRevealTicks - appearEnd) / (float) PURCHASE_FLY_TICKS);
            float sizeT = smoothstep(posT);
            pw = maxSize + (minSize - maxSize) * sizeT;
            float cx = centerX + (bagCenterX - centerX) * posT;
            float cy = centerY + (bagCenterY - centerY) * posT;
            px = cx - pw / 2f;
            py = cy - pw / 2f;
            alpha = 1f;
            if (ui.purchaseRevealTicks > flyEnd) {
                float fadeT = smoothstep((ui.purchaseRevealTicks - flyEnd) / (float) PURCHASE_FADE_TICKS);
                pw = minSize;
                px = bagCenterX - pw / 2f;
                py = bagCenterY - pw / 2f;
                alpha = Math.max(0f, 1f - fadeT);
            }
        }

        if (alpha <= 0.02f) {
            return;
        }
        Texture icon = GdxTextureBridge.toTexture(ui.purchaseRevealIcon);
        int ipw = Math.round(pw);
        float drawY = bottomFromTop(py, ipw);
        float prev = batch.getColor().a;
        batch.setColor(1f, 1f, 1f, alpha);
        batch.draw(icon, Math.round(px), Math.round(drawY), ipw, ipw);
        batch.setColor(1f, 1f, 1f, prev);
    }

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

    private void drawAshParticles(ShapeRenderer shapes) {
        PixelTextures.resetBlend();
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (float[] p : ui.ashParticles) {
            float life = 1f - p[4] / p[5];
            shapes.setColor(200f / 255f, 170f / 255f, 100f / 255f, life * 50f / 255f);
            shapes.rect(p[0], bottomFromTop(p[1], 1f), 1f, 1f);
        }
        shapes.end();
    }

    private void drawDialog(SpriteBatch batch, ShapeRenderer shapes, String text) {
        if (text == null) {
            return;
        }
        PixelTextures.resetBlend();
        float boxMarginX = 10f;
        float pad = 8f;
        float boxW = VW - boxMarginX * 2f;
        BitmapFont textFont = fonts.dialog;
        String speakerLabel = "Герцог: ";
        glyph.setText(textFont, speakerLabel);
        float speakerW = glyph.width;
        String[] rawLines = text.split("\n", -1);
        int lineCount = Math.min(2, Math.max(1, rawLines.length));
        float lineH = textFont.getLineHeight() + 3f;
        float boxH = lineH * lineCount + pad * 2f;
        float boxX = boxMarginX;
        float boxTop = VH - DIALOG_BOTTOM_MARGIN - boxH;
        float boxY = bottomFromTop(boxTop, boxH);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(10f / 255f, 8f / 255f, 4f / 255f, 230f / 255f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(140f / 255f, 100f / 255f, 35f / 255f, 1f);
        shapes.rect(boxX, boxY, boxW, boxH);
        shapes.setColor(DUKE_GOLD.r, DUKE_GOLD.g, DUKE_GOLD.b, 160f / 255f);
        shapes.rect(boxX + 1f, boxY + 1f, boxW - 2f, boxH - 2f);
        shapes.end();
        PixelTextures.resetBlend();

        batch.begin();
        float textX = boxX + pad;
        float startY = textYFromTop(boxTop + pad + textFont.getCapHeight());
        textFont.setColor(DUKE_GOLD);
        textFont.draw(batch, speakerLabel, textX, startY);
        textFont.setColor(SPEECH);
        if (rawLines.length > 0) {
            textFont.draw(batch, rawLines[0].trim(), textX + speakerW, startY);
        }
        if (rawLines.length > 1) {
            textFont.draw(batch, rawLines[1].trim(), textX, startY - lineH);
        }
        batch.end();
    }

    private void drawCentered(SpriteBatch batch, BitmapFont font, String text, float centerX, float y) {
        glyph.setText(font, text);
        font.draw(batch, text, centerX - glyph.width * 0.5f, y);
    }

    private String truncateToWidth(BitmapFont font, String text, float maxW) {
        glyph.setText(font, text);
        if (glyph.width <= maxW) {
            return text;
        }
        String ellipsis = "…";
        for (int len = text.length() - 1; len > 0; len--) {
            String cut = text.substring(0, len) + ellipsis;
            glyph.setText(font, cut);
            if (glyph.width <= maxW) {
                return cut;
            }
        }
        return ellipsis;
    }
}
