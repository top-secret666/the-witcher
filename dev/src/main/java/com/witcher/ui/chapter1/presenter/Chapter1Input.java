package main.java.com.witcher.ui.chapter1.presenter;

/** Ввод кадра для {@link Chapter1Presenter}. */
public record Chapter1Input(
    int mouseX,
    int mouseY,
    boolean clicked,
    boolean escPressed,
    int wheelNotches
) {
}
