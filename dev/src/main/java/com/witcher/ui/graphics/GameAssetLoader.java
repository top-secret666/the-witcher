package main.java.com.witcher.ui.graphics;

import main.java.com.witcher.ui.shop.ShopIconsFactory;
import main.java.com.witcher.ui.shop.swing.ShopAssetCache;

import java.util.function.Consumer;

/**
 * Фоновая предзагрузка тяжёлых экранов и кэша лавки; прогресс отображается на сплэше.
 */
final class GameAssetLoader {

    static final class Bundle {
        final MainMenuScreen mainMenu;
        final IntroScreen intro;

        Bundle(MainMenuScreen mainMenu, IntroScreen intro) {
            this.mainMenu = mainMenu;
            this.intro = intro;
        }
    }

    private final SplashScreen splash;
    private final Consumer<Bundle> onComplete;
    private volatile boolean failed;

    private GameAssetLoader(SplashScreen splash, Consumer<Bundle> onComplete) {
        this.splash = splash;
        this.onComplete = onComplete;
    }

    static GameAssetLoader start(SplashScreen splash, Consumer<Bundle> onComplete) {
        GameAssetLoader loader = new GameAssetLoader(splash, onComplete);
        Thread thread = new Thread(loader::run, "game-asset-loader");
        thread.setDaemon(true);
        thread.setPriority(Thread.NORM_PRIORITY - 1);
        thread.start();
        return loader;
    }

    boolean hasFailed() {
        return failed;
    }

    private void run() {
        try {
            report(5);
            GameFonts.get();

            report(20);
            MainMenuScreen menu = new MainMenuScreen();

            report(55);
            IntroScreen intro = new IntroScreen();

            report(85);
            ShopIconsFactory.warmupHybridIcons();
            ShopAssetCache.resetAfterGdxBake();
            ShopAssetCache.get();
            UiChrome.warmup();

            report(100);
            splash.markLoadingComplete();
            onComplete.accept(new Bundle(menu, intro));
        } catch (Throwable error) {
            failed = true;
            System.err.println("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0435\u0434\u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0438:");
            error.printStackTrace();
            splash.markLoadingComplete();
        }
    }

    private void report(int percent) {
        splash.setLoadProgress(percent);
    }
}
