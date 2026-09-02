package main.java.com.witcher.ui.shop.view;

/**
 * Размеры и отступы UI лавки — без привязки к загрузке текстур.
 * Реализация Swing: {@link main.java.com.witcher.ui.shop.swing.ShopAssetCache}.
 */
public interface ShopUiMetrics {

    int hudX();

    int hudW();

    int hudH();

    int panelW();

    int panelHeaderH();

    int topRowCols();

    int bottomRowCols();

    int detailPanelW();

    int detailPanelH();

    int rowH();

    int btnW();

    int btnH();

    int cardArtSize();

    int dukeSealSize();
}
