package main.java.com.witcher.ui.shop.bridge;

import main.java.com.witcher.ui.shop.ArmourIconRegistry;
import main.java.com.witcher.ui.shop.ShopEntryIcons;

import java.awt.image.BufferedImage;

/**
 * Единая точка доступа Swing → LibGDX bridge (запекание иконок и HUD-ассетов).
 * Reflection сосредоточен здесь, а не в фабриках по всему UI.
 */
public final class ShopGdxBridge {

    private static final String HYBRID_ICONS = "main.java.com.witcher.gdx.bridge.HybridShopIcons";
    private static final String HYBRID_UI = "main.java.com.witcher.gdx.bridge.HybridShopUiAssets";

    private ShopGdxBridge() {
    }

    public static void warmupIcons() {
        invokeStatic(HYBRID_ICONS, "warmup");
    }

    public static ShopEntryIcons createIcons(int iconSize) {
        try {
            Class<?> hybrid = Class.forName(HYBRID_ICONS);
            Object icons = hybrid.getMethod("create", int.class).invoke(null, iconSize);
            if (icons instanceof ShopEntryIcons entryIcons) {
                return entryIcons;
            }
        } catch (Throwable error) {
            logFallback("icons", error);
        }
        return ArmourIconRegistry.get(iconSize);
    }

    public static BufferedImage getUiAsset(String key) {
        try {
            Object result = Class.forName(HYBRID_UI).getMethod("get", String.class).invoke(null, key);
            if (result instanceof BufferedImage image && image.getWidth() > 0 && image.getHeight() > 0) {
                return image;
            }
        } catch (Throwable ignored) {
            // GDX bridge недоступен
        }
        return null;
    }

    private static void invokeStatic(String className, String method) {
        try {
            Class.forName(className).getMethod(method).invoke(null);
        } catch (Throwable error) {
            logFallback(className, error);
        }
    }

    private static void logFallback(String what, Throwable error) {
        System.out.println("[ShopGdxBridge] " + what + " unavailable: " + error.getMessage());
    }
}
