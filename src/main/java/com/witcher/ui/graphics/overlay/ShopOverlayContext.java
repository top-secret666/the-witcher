package main.java.com.witcher.ui.graphics.overlay;

import main.java.com.witcher.ui.graphics.GameFonts;
import main.java.com.witcher.ui.graphics.ShopAssetCache;
import main.java.com.witcher.ui.shop.ShopEntryIcons;
import main.java.com.witcher.ui.shop.presenter.ShopPresenter;
import main.java.com.witcher.ui.shop.presenter.ShopSessionState;

/** Контекст оверлеев инвентаря / экипировки (логика в presenter, отрисовка в overlay-слоях). */
public record ShopOverlayContext(
    ShopPresenter presenter,
    ShopSessionState ui,
    ShopAssetCache assets,
    ShopEntryIcons armourIcons
) {
}
