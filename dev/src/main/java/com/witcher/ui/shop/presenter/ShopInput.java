package main.java.com.witcher.ui.shop.presenter;

/** Ввод кадра для {@link ShopPresenter}. */
public record ShopInput(int mouseX, int mouseY, boolean clicked, boolean escPressed, int wheelNotches) {
}
