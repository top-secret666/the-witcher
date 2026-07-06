package main.java.com.witcher.ui.graphics;

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
            report(5, "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430... \u0448\u0440\u0438\u0444\u0442\u044b");
            GameFonts.get();

            report(20, "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430... \u043c\u0435\u043d\u044e");
            MainMenuScreen menu = new MainMenuScreen();

            report(55, "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430... \u0437\u0430\u0441\u0442\u0430\u0432\u043a\u0430");
            IntroScreen intro = new IntroScreen();

            report(85, "\u0417\u0430\u0433\u0440\u0443\u0437\u043a\u0430... \u043b\u0430\u0432\u043a\u0430");
            ShopAssetCache.get();
            UiChrome.warmup();

            report(100, "\u0413\u043e\u0442\u043e\u0432\u043e");
            splash.markLoadingComplete();
            onComplete.accept(new Bundle(menu, intro));
        } catch (Throwable error) {
            failed = true;
            System.err.println("\u041e\u0448\u0438\u0431\u043a\u0430 \u043f\u0440\u0435\u0434\u0437\u0430\u0433\u0440\u0443\u0437\u043a\u0438:");
            error.printStackTrace();
            splash.markLoadingComplete();
        }
    }

    private void report(int percent, String label) {
        splash.setLoadProgress(percent, label);
    }
}
