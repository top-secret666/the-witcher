package main.java.com.witcher.ui.shop;

import main.java.com.witcher.model.armour.Armour;
import main.java.com.witcher.model.armour.Chestpiece;
import main.java.com.witcher.ui.graphics.PixelScaler;
import main.java.com.witcher.ui.graphics.ShopScreen;
import main.java.com.witcher.ui.graphics.Sprite;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/** Иконки нагрудников по ключевым словам в названии доспеха. */
public final class ChestIconRegistry {

    private static final String MAP_PATH = "/chest_icon_map.properties";
    private static final String BAKED = "/assets/sprites/lavka/1x/icons/items/";
    private static final String SRC = "/assets/sprites/lavka/icons/items/";

    private static ChestIconRegistry instance;

    private final int iconSize;
    private final List<Rule> rules;
    private final Map<String, BufferedImage> cache = new HashMap<>();

    private record Rule(String keyword, String fileName) {
    }

    private ChestIconRegistry(int iconSize) {
        this.iconSize = iconSize;
        this.rules = loadRules();
    }

    public static ChestIconRegistry get(int iconSize) {
        if (instance == null || instance.iconSize != iconSize) {
            instance = new ChestIconRegistry(iconSize);
        }
        return instance;
    }

    public BufferedImage iconFor(Armour armour) {
        if (!(armour instanceof Chestpiece)) {
            return null;
        }
        return iconForName(armour.getName());
    }

    public BufferedImage iconForEntry(ShopCatalogEntry entry) {
        if (entry == null || entry.armour == null) {
            return null;
        }
        return iconFor(entry.armour);
    }

    public BufferedImage iconForName(String armourName) {
        if (armourName == null || armourName.isBlank()) {
            return null;
        }
        String file = resolveFile(armourName);
        if (file == null) {
            return null;
        }
        return cache.computeIfAbsent(file, this::loadIcon);
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

    private BufferedImage loadIcon(String fileName) {
        BufferedImage baked = loadRaw(BAKED + fileName);
        if (baked != null) {
            if (baked.getWidth() == iconSize && baked.getHeight() == iconSize) {
                return baked;
            }
            return PixelScaler.crispScale(baked, iconSize, iconSize);
        }
        BufferedImage src = loadRaw(SRC + fileName);
        if (src == null) {
            return null;
        }
        Rectangle box = ShopScreen.computeContentBoundsPublic(src);
        return PixelScaler.crispScaleRegion(src, box, iconSize, iconSize);
    }

    private static BufferedImage loadRaw(String path) {
        Sprite sprite = Sprite.loadOptional(path);
        return sprite != null ? sprite.getImage() : null;
    }

    private static List<Rule> loadRules() {
        Properties props = new Properties();
        try (InputStream in = ChestIconRegistry.class.getResourceAsStream(MAP_PATH)) {
            if (in != null) {
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(in, StandardCharsets.UTF_8))) {
                    props.load(reader);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load chest icon map", e);
        }
        List<Rule> out = new ArrayList<>();
        for (String key : props.stringPropertyNames()) {
            String file = props.getProperty(key).trim();
            if (!file.isEmpty()) {
                out.add(new Rule(key.toLowerCase(Locale.ROOT), file));
            }
        }
        return out;
    }
}
