package main.java.com.witcher.ui.shop.view;

/**
 * Пути к ассетам лавки — общий контракт для Swing ({@code ShopAssetCache})
 * и LibGDX ({@code GdxShopRuntimeAssets}, {@code GdxShopUiBaker}).
 */
public final class LavkaAssetPaths {

    /** Classpath-префикс для Swing {@link javax.imageio.ImageIO} / {@code Sprite.load}. */
    public static final String SWING_ROOT = "/assets/sprites/lavka/";
    public static final String SWING_1X = SWING_ROOT + "1x/";
    public static final String SWING_UI_BAKED = SWING_1X + "ui/";
    public static final String SWING_ICONS_SRC = SWING_ROOT + "icons/";
    public static final String SWING_ICONS_BAKED = SWING_1X + "icons/";
    public static final String SWING_UI_SRC = SWING_ROOT + "ui/";

    /** Относительные пути для LibGDX {@code FileHandle} / {@code PixelTextures}. */
    public static final String GDX_ROOT = "sprites/lavka/";
    public static final String GDX_1X = GDX_ROOT + "1x/";
    public static final String GDX_UI = GDX_ROOT + "ui/";

    private LavkaAssetPaths() {
    }

    public static String gdxLavka(String relativePath) {
        return GDX_ROOT + relativePath;
    }

    public static String gdxUi(String fileName) {
        return GDX_UI + fileName;
    }

    public static String gdxIcons(String fileName) {
        return GDX_ROOT + "icons/" + fileName;
    }
}
