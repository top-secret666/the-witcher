package main.java.com.witcher.ui.shop.swing;

import main.java.com.witcher.chapter1.shop.Chapter1ShopBridge;
import main.java.com.witcher.ui.graphics.GameWindow;
import main.java.com.witcher.ui.shop.ShopIconsFactory;
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
        this(model, null);
    }

    public ShopScreen(ShopModel model, Chapter1ShopBridge chapterBridge) {
        ShopAssetCache cache = ShopAssetCache.get();
        this.presenter = new ShopPresenter(model, cache, ShopIconsFactory.create(cache.cardArtSize()), chapterBridge);
        this.view = new ShopSwingView(presenter);
    }

    public ShopPresenter presenter() {
        return presenter;
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed) {
        update(mouseX, mouseY, clicked, escPressed, 0);
    }

    public void update(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
        presenter.update(new ShopInput(mouseX, mouseY, clicked, escPressed, wheelNotches));
    }

    /** Доигрывает fly-in покупки/кошелька, даже если глава ушла в VN поверх лавки. */
    public void tickTimedScenes() {
        presenter.tickTimedScenes();
    }

    public boolean isChapterEventIdle() {
        return presenter.isChapterEventIdle();
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
