package main.java.com.witcher.gdx.bridge;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Однократный невидимый LibGDX-контекст для запекания иконок при старте Swing. */
public final class GdxIconBakeSession {

    private static volatile Map<String, BufferedImage> baked = Collections.emptyMap();
    private static volatile boolean attempted;
    private static volatile boolean success;
    private static volatile CountDownLatch doneLatch = new CountDownLatch(0);

    private GdxIconBakeSession() {
    }

    public static boolean isReady() {
        return success && !baked.isEmpty();
    }

    public static Map<String, BufferedImage> cache() {
        return baked;
    }

    public static void ensureBaked(int... sizes) {
        waitForBake();
        if (success) {
            return;
        }
        synchronized (GdxIconBakeSession.class) {
            if (success) {
                return;
            }
            if (!attempted) {
                attempted = true;
                doneLatch = new CountDownLatch(1);
                AtomicReference<Map<String, BufferedImage>> result =
                    new AtomicReference<>(Collections.emptyMap());
                Thread thread = new Thread(() -> {
                    try {
                        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
                        config.setWindowedMode(32, 32);
                        config.setInitialVisible(false);
                        config.setTitle("witcher-icon-bake");
                        config.disableAudio(true);
                        new Lwjgl3Application(new ApplicationAdapter() {
                            @Override
                            public void create() {
                                try {
                                    result.set(GdxIconBaker.bakeDistinctIcons(sizes));
                                    success = !result.get().isEmpty();
                                } catch (Throwable error) {
                                    success = false;
                                    Gdx.app.error("GdxIconBake", "bake failed", error);
                                }
                                Gdx.app.exit();
                            }
                        }, config);
                    } catch (Throwable error) {
                        success = false;
                        System.err.println("[GdxIconBake] " + error.getMessage());
                    } finally {
                        baked = result.get();
                        doneLatch.countDown();
                    }
                }, "gdx-icon-bake");
                thread.setDaemon(true);
                thread.start();
            }
        }
        waitForBake();
    }

    private static void waitForBake() {
        CountDownLatch latch = doneLatch;
        if (latch == null) {
            return;
        }
        try {
            latch.await(90, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
