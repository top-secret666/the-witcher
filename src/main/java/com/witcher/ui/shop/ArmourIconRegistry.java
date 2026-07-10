package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.graphics.PixelScaler;
import main.java.com.witcher.ui.shop.swing.ShopImageBounds;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Иконки доспехов по ключевым словам в названии (чистый Swing). */
public final class ArmourIconRegistry implements ShopEntryIcons {

    private static final String SRC = "/assets/sprites/lavka/icons/items/";

    private static ArmourIconRegistry instance;

    private static final BufferedImage MISSING_ICON = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private final int iconSize;
    private final List<ArmourIconMap.Rule> rules;
    private final Map<String, BufferedImage> cache = new HashMap<>();
    private final Map<String, BufferedImage> originalCache = new HashMap<>();
    private final Set<String> loggedMissing = new HashSet<>();

    private ArmourIconRegistry(int iconSize) {
        this.iconSize = iconSize;
        this.rules = ArmourIconMap.loadRules();
        preloadDistinctIcons();
    }

    private void preloadDistinctIcons() {
        Set<String> files = ArmourIconMap.distinctIconFiles();
        int loaded = 0;
        for (String file : files) {
            if (loadIcon(file, iconSize) != null) {
                loaded++;
            }
        }
        System.out.println("Armour icons: " + loaded + "/" + files.size() + " textures ready");
    }

    public static ArmourIconRegistry get(int iconSize) {
        if (instance == null || instance.iconSize != iconSize) {
            instance = new ArmourIconRegistry(iconSize);
        }
        return instance;
    }

    public BufferedImage iconFor(Armour armour) {
        if (armour == null) {
            return null;
        }
        return iconForName(armour.getName());
    }

    @Override
    public BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category) {
        return iconForEntry(entry, category, iconSize);
    }

    @Override
    public BufferedImage iconForArmour(Armour armour, ShopCategory category, int size) {
        return iconFor(armour, category, size);
    }

    public BufferedImage iconForEntry(ShopCatalogEntry entry) {
        return iconForEntry(entry, null, iconSize);
    }

    public BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category, int size) {
        if (entry == null) {
            return null;
        }
        if (entry.armour != null) {
            return iconFor(entry.armour, category, size);
        }
        if (entry.name != null && !entry.name.isBlank()) {
            return iconForName(entry.name, category, size);
        }
        return null;
    }

    public BufferedImage iconFor(Armour armour, int size) {
        return iconFor(armour, null, size);
    }

    public BufferedImage iconFor(Armour armour, ShopCategory category, int size) {
        if (armour == null) {
            return null;
        }
        return iconForName(armour.getName(), category, size);
    }

    public boolean hasMappedIcon(String armourName) {
        return armourName != null && !armourName.isBlank()
            && ArmourIconMap.resolveFile(armourName, rules) != null;
    }

    public BufferedImage iconForName(String armourName) {
        return iconForName(armourName, null, iconSize);
    }

    public BufferedImage iconForName(String armourName, ShopCategory category, int size) {
        if (armourName == null || armourName.isBlank()) {
            return null;
        }
        String file = ArmourIconMap.resolveFile(armourName, rules);
        if (file == null || !ArmourIconMap.matchesCategory(file, category)) {
            return null;
        }
        String cacheKey = size > 0 && size < iconSize ? file + "@" + size : file + "@full";
        BufferedImage cached = cache.get(cacheKey);
        if (cached != null) {
            return cached == MISSING_ICON ? null : cached;
        }
        BufferedImage loaded = loadIcon(file, size);
        cache.put(cacheKey, loaded != null ? loaded : MISSING_ICON);
        return loaded;
    }

    private BufferedImage loadIcon(String fileName, int size) {
        BufferedImage src = loadCroppedOriginal(fileName);
        if (src == null) {
            return null;
        }
        if (size > 0 && size < iconSize) {
            return PixelScaler.smoothScaleUniform(src, size);
        }
        return src;
    }

    private BufferedImage loadCroppedOriginal(String fileName) {
        BufferedImage cached = originalCache.get(fileName);
        if (cached != null) {
            return cached == MISSING_ICON ? null : cached;
        }
        BufferedImage src = loadRaw(SRC + fileName);
        if (src == null) {
            if (loggedMissing.add(fileName)) {
                System.err.println("Armour icons: missing file " + fileName);
            }
            originalCache.put(fileName, MISSING_ICON);
            return null;
        }
        Rectangle box = ShopImageBounds.compute(src);
        BufferedImage cropped = box != null && box.width > 0 && box.height > 0
            ? src.getSubimage(box.x, box.y, box.width, box.height)
            : src;
        originalCache.put(fileName, cropped);
        return cropped;
    }

    private static BufferedImage loadRaw(String path) {
        Sprite sprite = Sprite.loadOptional(path);
        return sprite != null ? sprite.getImage() : null;
    }
}
