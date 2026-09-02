package main.java.com.witcher.ui.intro;

/**
 * Цвета спикеров интро — RGB без AWT.
 */
public final class IntroTheme {

    public static final int NARRATOR_R = 160;
    public static final int NARRATOR_G = 145;
    public static final int NARRATOR_B = 120;

    public static final int GERALT_R = 160;
    public static final int GERALT_G = 205;
    public static final int GERALT_B = 235;

    public static final int STRANGER_R = 100;
    public static final int STRANGER_G = 130;
    public static final int STRANGER_B = 200;

    public static final int DUKE_R = 218;
    public static final int DUKE_G = 165;
    public static final int DUKE_B = 32;

    private IntroTheme() {
    }

    public static int packRgb(int r, int g, int b) {
        return (r << 16) | (g << 8) | b;
    }

    public static int narratorRgb() {
        return packRgb(NARRATOR_R, NARRATOR_G, NARRATOR_B);
    }

    public static int geraltRgb() {
        return packRgb(GERALT_R, GERALT_G, GERALT_B);
    }

    public static int strangerRgb() {
        return packRgb(STRANGER_R, STRANGER_G, STRANGER_B);
    }

    public static int dukeRgb() {
        return packRgb(DUKE_R, DUKE_G, DUKE_B);
    }
}
