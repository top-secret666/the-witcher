package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.ui.graphics.PixelScaler;
import main.java.com.witcher.ui.graphics.ShopImageBounds;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Иконки доспехов по ключевым словам в названии. */
public final class ArmourIconRegistry {

    private static final String MAP_PATH = "/armor_icon_map.properties";
    private static final String BAKED = "/assets/sprites/lavka/1x/icons/items/";
    private static final String SRC = "/assets/sprites/lavka/icons/items/";

    private static ArmourIconRegistry instance;

    private static final BufferedImage MISSING_ICON = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private final int iconSize;
    private final List<Rule> rules;
    private final Map<String, BufferedImage> cache = new HashMap<>();
    private final Set<String> loggedMissing = new HashSet<>();

    private record Rule(String keyword, String fileName) {
    }

    private ArmourIconRegistry(int iconSize) {
        this.iconSize = iconSize;
        this.rules = loadRules();
        preloadDistinctIcons();
    }

    private void preloadDistinctIcons() {
        HashSet<String> files = new HashSet<>();
        for (Rule rule : rules) {
            files.add(rule.fileName);
        }
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

    public BufferedImage iconForEntry(ShopCatalogEntry entry) {
        return iconForEntry(entry, null, iconSize);
    }

    public BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category) {
        return iconForEntry(entry, category, iconSize);
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
        return armourName != null && !armourName.isBlank() && resolveFile(armourName) != null;
    }

    public BufferedImage iconForName(String armourName) {
        return iconForName(armourName, null, iconSize);
    }

    public BufferedImage iconForName(String armourName, ShopCategory category, int size) {
        if (armourName == null || armourName.isBlank()) {
            return null;
        }
        String file = resolveFile(armourName);
        if (file == null || !matchesCategory(file, category)) {
            return null;
        }
        String cacheKey = file + "@" + size;
        BufferedImage cached = cache.get(cacheKey);
        if (cached != null) {
            return cached == MISSING_ICON ? null : cached;
        }
        BufferedImage loaded = loadIcon(file, size);
        cache.put(cacheKey, loaded != null ? loaded : MISSING_ICON);
        return loaded;
    }

    private static boolean matchesCategory(String fileName, ShopCategory category) {
        if (category == null) {
            return true;
        }
        return switch (category) {
            case CHEST -> isChestIcon(fileName);
            case LEGS -> isLegsIcon(fileName);
            case GLOVES -> isGlovesIcon(fileName);
            case BOOTS -> isBootsIcon(fileName);
            case SETS -> isSetIcon(fileName);
            case POTION -> isPotionIcon(fileName);
            case WEAPON -> isWeaponIcon(fileName);
        };
    }

    private static boolean isGlovesIcon(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("gauntlets") || lower.contains("gloves");
    }

    private static boolean isBootsIcon(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("boots") || lower.contains("shoes") || lower.contains("slippers");
    }

    private static boolean isSetIcon(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("school") || lower.contains("szkola") || lower.contains("szkoła")
            || lower.contains("cechu");
    }

    private static boolean isPotionIcon(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("potion") || lower.contains("elixir") || lower.contains("decoction");
    }

    private static boolean isWeaponIcon(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("sword") || lower.contains("crossbow") || lower.contains("laymore");
    }

    private static boolean isLegsIcon(String fileName) {
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.contains("trousers")
            || lower.contains("breeches")
            || lower.contains("sharovary");
    }

    private static boolean isChestIcon(String fileName) {
        if (isLegsIcon(fileName)) {
            return false;
        }
        String lower = fileName.toLowerCase(Locale.ROOT);
        return lower.startsWith("chest_")
            || lower.contains("armor")
            || lower.contains("cuirass")
            || lower.contains("aketon")
            || lower.contains("gambeson")
            || lower.contains("halberdier");
    }

    private String resolveFile(String armourName) {
        String lower = armourName.toLowerCase(Locale.ROOT);
        for (Rule rule : rules) {
            if (lower.contains(rule.keyword)) {
                return rule.fileName;
            }
        }
        return null;
    }

    private BufferedImage loadIcon(String fileName, int size) {
        BufferedImage baked = loadRaw(BAKED + fileName);
        if (baked != null) {
            if (baked.getWidth() == size && baked.getHeight() == size) {
                return baked;
            }
            return PixelScaler.crispScale(baked, size, size);
        }
        BufferedImage src = loadRaw(SRC + fileName);
        if (src == null) {
            if (loggedMissing.add(fileName)) {
                System.err.println("Armour icons: missing file " + fileName);
            }
            return null;
        }
        Rectangle box = ShopImageBounds.compute(src);
        return PixelScaler.crispScaleRegion(src, box, size, size);
    }

    private static BufferedImage loadRaw(String path) {
        Sprite sprite = Sprite.loadOptional(path);
        return sprite != null ? sprite.getImage() : null;
    }

    private static List<Rule> loadRules() {
        List<Rule> out = new ArrayList<>();
        try (InputStream in = ArmourIconRegistry.class.getResourceAsStream(MAP_PATH)) {
            if (in == null) {
                System.err.println("Armour icons: map not found at " + MAP_PATH);
                return out;
            }
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.strip();
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    int sep = line.indexOf('=');
                    if (sep <= 0) {
                        continue;
                    }
                    String keyword = line.substring(0, sep).strip().toLowerCase(Locale.ROOT);
                    String file = line.substring(sep + 1).strip();
                    if (!keyword.isEmpty() && !file.isEmpty()) {
                        out.add(new Rule(keyword, file));
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load armour icon map", e);
        }
        out.sort(Comparator.comparingInt((Rule rule) -> rule.keyword.length()).reversed());
        System.out.println("Armour icons: " + out.size() + " name rules loaded");
        return out;
    }
}
