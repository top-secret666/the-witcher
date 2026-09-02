package main.java.com.witcher.ui.shop.view;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

/**
 * Контракт отрисовки экрана лавки — реализация Swing: {@link main.java.com.witcher.ui.shop.swing.ShopSwingView}.
 */
public interface ShopView {

    /** Основной кадр сцены (виртуальное разрешение 480×360). */
    void renderScene(BufferedImage screen, int mouseX, int mouseY);

    /** Чёткий UI-текст поверх пост-обработки. */
    void renderTextOverlay(Graphics2D g, int mouseX, int mouseY);
}
