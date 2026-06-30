package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

/** Товар в витрине лавки (пока мок-данные, как в Swing). */
public final class ShopItem {

    public final String name;
    public final String priceLabel;
    public final String dukeLine;
    public final String[] statLines;
    public final Texture icon;
    public final Rectangle bounds = new Rectangle();

    public ShopItem(String name, String priceLabel, String dukeLine, String[] statLines, Texture icon) {
        this.name = name;
        this.priceLabel = priceLabel;
        this.dukeLine = dukeLine;
        this.statLines = statLines;
        this.icon = icon;
    }
}
