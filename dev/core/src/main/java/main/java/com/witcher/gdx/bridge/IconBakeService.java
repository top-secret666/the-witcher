package main.java.com.witcher.gdx.bridge;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * Фасад жизненного цикла GPU-запекания при старте Swing.
 * Скрывает детали {@link GdxIconBakeSession} от UI-слоя.
 */
public final class IconBakeService {

    private IconBakeService() {
    }

    public static boolean isReady() {
        return GdxIconBakeSession.isReady();
    }

    public static void ensureBaked(int... sizes) {
        GdxIconBakeSession.ensureBaked(sizes);
    }

    public static BufferedImage get(String key) {
        if (!isReady()) {
            return null;
        }
        BufferedImage image = GdxIconBakeSession.cache().get(key);
        return GdxIconBaker.isUsable(image) ? image : null;
    }

    public static Map<String, BufferedImage> cache() {
        return GdxIconBakeSession.cache();
    }
}
