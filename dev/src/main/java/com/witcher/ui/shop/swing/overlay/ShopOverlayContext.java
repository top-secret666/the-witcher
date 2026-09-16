package com.witcher.ui.shop.swing.overlay;



import com.witcher.ui.shop.ShopEntryIcons;

import com.witcher.ui.shop.ShopOverlayAssets;

import com.witcher.ui.shop.presenter.ShopPresenter;

import com.witcher.ui.shop.presenter.ShopSessionState;



/** Контекст оверлеев инвентаря / экипировки (логика в presenter, отрисовка в overlay-слоях). */

public record ShopOverlayContext(

    ShopPresenter presenter,

    ShopSessionState ui,

    ShopOverlayAssets assets,

    ShopEntryIcons armourIcons

) {

}

