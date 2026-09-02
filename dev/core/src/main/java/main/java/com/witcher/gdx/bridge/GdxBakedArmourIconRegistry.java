package main.java.com.witcher.gdx.bridge;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.shop.ArmourIconMap;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEntryIcons;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Иконки товаров, запечённые LibGDX — отдаёт {@link BufferedImage} для Swing. */
public final class GdxBakedArmourIconRegistry implements ShopEntryIcons {

    private static final BufferedImage MISSING_ICON = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private static GdxBakedArmourIconRegistry instance;

    private final int iconSize;
    private final List<ArmourIconMap.Rule> rules;
    private final Map<String, BufferedImage> cache = new HashMap<>();

    private GdxBakedArmourIconRegistry(int iconSize) {
        this.iconSize = iconSize;
        this.rules = ArmourIconMap.loadRules();
    }

    public static GdxBakedArmourIconRegistry get(int iconSize) {
        if (instance == null || instance.iconSize != iconSize) {
            instance = new GdxBakedArmourIconRegistry(iconSize);
        }
        return instance;
    }

    @Override
    public BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category) {
        return iconForEntry(entry, category, iconSize);
    }

    @Override
    public BufferedImage iconForArmour(Armour armour, ShopCategory category, int size) {
        if (armour == null) {
            return null;
        }
        return iconForName(armour.getName(), category, size);
    }

    public BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category, int size) {
        if (entry == null) {
            return null;
        }
        if (entry.armourSet != null) {
            return iconForName(entry.armourSet.getName(), category, size);
        }
        if (entry.armour != null) {
            return iconForArmour(entry.armour, category, size);
        }
        if (entry.name != null && !entry.name.isBlank()) {
            return iconForName(entry.name, category, size);
        }
        return null;
    }

    @Override
    public BufferedImage iconForName(String armourName, ShopCategory category, int size) {
        if (armourName == null || armourName.isBlank()) {
            return null;
        }
        String file = ArmourIconMap.resolveFile(armourName, rules);
        if (file == null || !ArmourIconMap.matchesCategory(file, category)) {
            return null;
        }
        String cacheKey = GdxIconBaker.cacheKey(file, size);
        BufferedImage cached = cache.get(cacheKey);
        if (cached != null) {
            return cached == MISSING_ICON ? null : cached;
        }
        BufferedImage loaded = GdxIconBakeSession.cache().get(cacheKey);
        cache.put(cacheKey, loaded != null ? loaded : MISSING_ICON);
        return loaded;
    }

    public static int bakedFileCount() {
        Set<String> files = ArmourIconMap.distinctIconFiles();
        return files.size();
    }
}
