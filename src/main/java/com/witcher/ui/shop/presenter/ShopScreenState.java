package main.java.com.witcher.ui.shop.presenter;

/** Состояния UI лавки (не путать с доменной моделью {@link main.java.com.witcher.ui.shop.ShopModel}). */
public enum ShopScreenState {
    REVEAL,
    IDLE,
    WALLET_REVEAL,
    PURCHASE_REVEAL,
    CATEGORY_OPENING,
    CATEGORY,
    CATEGORY_CLOSING,
    BATTLE_CARD_REVEAL
}
