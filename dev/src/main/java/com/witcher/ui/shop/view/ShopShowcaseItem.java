package main.java.com.witcher.ui.shop.view;

import main.java.com.witcher.ui.shop.ShopCategory;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/** Элемент сетки категорий на витрине (только отображение). */
public final class ShopShowcaseItem {

    public enum Kind {
        PIECE,
        SET_CATALOG
    }

    public final Kind kind;
    public final ShopCategory category;
    public String priceLabel;
    public final String dukeLine;
    public final String[] statLines;
    public final BufferedImage icon;
    public final BufferedImage cardArt;
    public Rectangle bounds = new Rectangle();

    public ShopShowcaseItem(Kind kind, ShopCategory category, String priceLabel, String dukeLine,
                            String[] statLines, BufferedImage icon, BufferedImage cardArt) {
        this.kind = kind;
        this.category = category;
        this.priceLabel = priceLabel;
        this.dukeLine = dukeLine;
        this.statLines = statLines;
        this.icon = icon;
        this.cardArt = cardArt;
    }

    public String displayName() {
        return category.label;
    }
}
