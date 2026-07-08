package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.ArmourIconRegistry;
import main.java.com.witcher.ui.shop.ShopModel;
import main.java.com.witcher.ui.shop.presenter.ShopInput;
import main.java.com.witcher.ui.shop.presenter.ShopPresenter;
import main.java.com.witcher.ui.shop.view.ShopView;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

/**
 * Фасад экрана лавки для {@link GameWindow}.
 * Логика — {@link ShopPresenter}, отрисовка — {@link ShopSwingView}.
 */
public final class ShopScreen {

    private final ShopPresenter presenter;
    private final ShopView view;

    public ShopScreen() {
        this(ShopModel.createNewSession());
    }

    public ShopScreen(ShopModel model) {
        ShopAssetCache cache = ShopAssetCache.get();
        this.presenter = new ShopPresenter(model, cache, ArmourIconRegistry.get(cache.cardArtSize()));
        this.view = new ShopSwingView(presenter);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed) {
        update(mouseX, mouseY, clicked, escPressed, 0);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
        presenter.update(new ShopInput(mouseX, mouseY, clicked, escPressed, wheelNotches));
    }

    public void render(BufferedImage screen, int mouseX, int mouseY) {
        view.renderScene(screen, mouseX, mouseY);
    }

    public void renderTextOverlay(Graphics2D g, int mouseX, int mouseY) {
        view.renderTextOverlay(g, mouseX, mouseY);
    }

    public boolean isExitRequested() {
        return presenter.exitRequested();
    }

    public void clearExitRequest() {
        presenter.clearExitRequest();
    }

    /** @deprecated используйте {@link ShopImageBounds#compute} */
    @Deprecated
    public static Rectangle computeContentBoundsPublic(BufferedImage img) {
        return ShopImageBounds.compute(img);
    }
}
