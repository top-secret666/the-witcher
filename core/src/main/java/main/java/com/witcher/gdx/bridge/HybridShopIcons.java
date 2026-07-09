package main.java.com.witcher.gdx.bridge;

import main.java.com.witcher.ui.shop.ArmourIconRegistry;
import main.java.com.witcher.ui.shop.ShopEntryIcons;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Мост Swing ↔ LibGDX: при старте запекает иконки товаров через GDX,
 * дальше Swing рисует только {@link java.awt.image.BufferedImage}.
 */
public final class HybridShopIcons {

    private static final int EQUIP_ICON_SIZE = 30;

    private HybridShopIcons() {
    }

    public static void warmup() {
        configureAssetsRoot();
        try {
            GdxIconBakeSession.ensureBaked(38, 30);
        } catch (Throwable error) {
            System.err.println("[HybridShopIcons] bake failed: " + error.getMessage());
        }
    }

    public static ShopEntryIcons create(int iconSize) {
        GdxIconBakeSession.ensureBaked(iconSize, EQUIP_ICON_SIZE);
        if (GdxIconBakeSession.isReady()) {
            System.out.println("[HybridShopIcons] GDX+Swing icons (size " + iconSize + ")");
            return new DelegatingShopIcons(iconSize);
        }
        System.out.println("[HybridShopIcons] Swing-only icons");
        return ArmourIconRegistry.get(iconSize);
    }

    private static void configureAssetsRoot() {
        if (!System.getProperty("witcher.assets", "").isBlank()) {
            return;
        }
        Path cwd = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath().normalize();
        Path[] candidates = {
            cwd.resolve("src/main/resources/assets"),
            cwd.resolve("../src/main/resources/assets"),
            cwd.resolve("assets")
        };
        for (Path candidate : candidates) {
            if (Files.isDirectory(candidate)) {
                System.setProperty("witcher.assets", candidate.toString());
                System.out.println("[HybridShopIcons] assets=" + candidate);
                return;
            }
        }
    }
}
