package main.java.com.witcher.gdx.shop;

import com.badlogic.gdx.graphics.Texture;
import main.java.com.witcher.gdx.graphics.GdxTextureBridge;
import main.java.com.witcher.gdx.graphics.PixelTextures;
import main.java.com.witcher.ui.shop.ShopCatalogEntry;
import main.java.com.witcher.ui.shop.ShopCategory;
import main.java.com.witcher.ui.shop.ShopEntryIcons;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
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

/** LibGDX-иконки доспехов — аналог {@link main.java.com.witcher.ui.shop.ArmourIconRegistry}. */
public final class GdxArmourIconRegistry implements ShopEntryIcons {

    private static final String MAP_PATH = "/armor_icon_map.properties";
    private static final String BAKED_PREFIX = "icons/items/";
    private static final String SRC_PREFIX = "icons/items/";

    private static GdxArmourIconRegistry instance;

    private static final BufferedImage MISSING_ICON = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

    private final int iconSize;
    private final List<Rule> rules;
    private final Map<String, BufferedImage> cache = new HashMap<>();
    private final Set<String> loggedMissing = new HashSet<>();

    private record Rule(String keyword, String fileName) {
    }

    private GdxArmourIconRegistry(int iconSize) {
        this.iconSize = iconSize;
        this.rules = loadRules();
        preloadDistinctIcons();
    }

    public static GdxArmourIconRegistry get(int iconSize) {
        if (instance == null || instance.iconSize != iconSize) {
            if (instance != null) {
                instance.cache.clear();
            }
            instance = new GdxArmourIconRegistry(iconSize);
        }
        return instance;
    }

    @Override
    public BufferedImage iconForEntry(ShopCatalogEntry entry, ShopCategory category) {
        if (entry == null) {
            return null;
        }
        if (entry.armour != null) {
            return iconForName(entry.armour.getName(), category);
        }
        if (entry.name != null && !entry.name.isBlank()) {
            return iconForName(entry.name, category);
        }
        return null;
    }

    public BufferedImage iconForName(String armourName, ShopCategory category) {
        if (armourName == null || armourName.isBlank()) {
            return null;
        }
        String file = resolveFile(armourName);
        if (file == null || !matchesCategory(file, category)) {
            return null;
        }
        String cacheKey = file + "@" + iconSize;
        BufferedImage cached = cache.get(cacheKey);
        if (cached != null) {
            return cached == MISSING_ICON ? null : cached;
        }
        BufferedImage loaded = loadIcon(file, iconSize);
        cache.put(cacheKey, loaded != null ? loaded : MISSING_ICON);
        return loaded;
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
        System.out.println("GdxArmourIconRegistry: " + loaded + "/" + files.size() + " textures ready");
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
        Texture baked = PixelTextures.loadLavka(BAKED_PREFIX + fileName);
        if (baked != null) {
            BufferedImage img = GdxTextureBridge.toBufferedImage(baked);
            baked.dispose();
            if (img != null) {
                return scaleIfNeeded(img, size);
            }
        }
        Texture src = PixelTextures.loadLavka(SRC_PREFIX + fileName);
        if (src == null) {
            if (loggedMissing.add(fileName)) {
                System.err.println("GdxArmourIconRegistry: missing file " + fileName);
            }
            return null;
        }
        BufferedImage img = GdxTextureBridge.toBufferedImage(src);
        src.dispose();
        if (img == null) {
            return null;
        }
        int[] bounds = PixelTextures.computeOpaqueBounds("sprites/lavka/" + SRC_PREFIX + fileName);
        if (bounds != null) {
            img = img.getSubimage(bounds[0], bounds[1], bounds[2], bounds[3]);
        }
        return scaleIfNeeded(img, size);
    }

    private static BufferedImage scaleIfNeeded(BufferedImage src, int size) {
        if (src.getWidth() == size && src.getHeight() == size) {
            return src;
        }
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(src, 0, 0, size, size, null);
        g.dispose();
        return dst;
    }

    private static List<Rule> loadRules() {
        List<Rule> out = new ArrayList<>();
        try (InputStream in = GdxArmourIconRegistry.class.getResourceAsStream(MAP_PATH)) {
            if (in == null) {
                System.err.println("GdxArmourIconRegistry: map not found at " + MAP_PATH);
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
        System.out.println("GdxArmourIconRegistry: " + out.size() + " name rules loaded");
        return out;
    }
}
